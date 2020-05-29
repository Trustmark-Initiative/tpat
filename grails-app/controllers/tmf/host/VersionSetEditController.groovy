package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.*
import edu.gatech.gtri.trustmark.v1_0.model.*
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonOutput

import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.json.JSONArray
import org.json.JSONObject

import org.springframework.validation.ObjectError
import tmf.host.artifact_processing.ActionType
import tmf.host.artifact_processing.ArtifactAction
import tmf.host.artifact_processing.MemoryProcessingData
import tmf.host.util.DefaultEntityImpl
import tmf.host.util.TFAMPropertiesHolder

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 * Manages all of the editing activities that can occur on a VersionSet.
 * <br/><br/>
 * @user brad
 * @date 11/22/16
 */
@Secured("ROLE_ORG_ADMIN")
class VersionSetEditController extends AbstractVersionSetController {
    //==================================================================================================================
    // Services
    //==================================================================================================================
    SpringSecurityService springSecurityService
    ProcessUploadService processUploadService
    ApplyChangesService applyChangesService
    FileService fileService
    //==================================================================================================================
    // WEB METHODS
    //   Each of these methods represents a URL in the web server.
    //==================================================================================================================
    /**
     * Puts the current version set under "edit".
     */
    def index() {
        User user = (User) springSecurityService.currentUser
        log.debug("Request to edit contents of VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        List<VersionSetLogEntry> logEntries = new ArrayList<>()
        if( vs.createdSuccessfully ){
            logEntries = VersionSetLogEntry.createCriteria().list(max: 10) {
                eq("versionSet", vs)
                order("counter", "desc")
            } ?: []

            List<VersionSetTIPLink> primaryTIPLinks = VersionSetTIPLink.findAllByVersionSetAndPrimaryTIP(vs, true)
            Collections.sort(primaryTIPLinks, { VersionSetTIPLink link1, VersionSetTIPLink link2 ->
                return link1.getTrustInteroperabilityProfile().getName().compareToIgnoreCase(link2.getTrustInteroperabilityProfile().getName())
            } as Comparator)
        } else {
            log.warn("Cannot show VersionSet[${vs.name}] since it is not finished creating.  Showing the showCreating page...")
            return redirect(controller: 'versionSet', action: 'showCreating', id: vs.name)
        }

        if( vs.lockedDate != null && vs.lockedBy != null){
            if( !user.username.equalsIgnoreCase(vs.lockedBy.username) ){
                log.warn("User[@|yellow ${user.username}|@] cannot edit version @|cyan ${vs.name}|@ because it is already locked by User[@|yellow ${vs.lockedBy.username}|@]!")
                flash.error = "This version set is already locked by ${vs.lockedBy.username} as of ${vs.lockedDate.toString()}.  They must release the version set before you can edit it."
                return redirect(controller: 'versionSet', action: 'show', id: vs.name)
            }
        }else{
            log.debug("Marking version set @|cyan ${vs.name}|@ as locked by user @|green ${user.username}|@...")
            vs.lockedDate = Calendar.getInstance().getTime()
            vs.lockedBy = user
            vs.save(failOnError: true)
            VersionSetLogEntry.create(vs.id, "LOCKED", "User[${user.username}] has locked version set ${vs.name}...", [user: user.username])
        }

        List<VersionSetUpload> uploadedFiles = VersionSetUpload.findAllByVersionSet(vs, [max: 5, offset: 0, sort: 'dateCreated', order: 'desc'])
        if( !uploadedFiles )
            uploadedFiles = []

        List<VersionSetTIPLink> primaryTIPLinks = VersionSetTIPLink.findAllByVersionSetAndPrimaryTIP(vs, true)
        Collections.sort(primaryTIPLinks, { VersionSetTIPLink link1, VersionSetTIPLink link2 ->
            return link1.getTrustInteroperabilityProfile().getName().compareToIgnoreCase(link2.getTrustInteroperabilityProfile().getName())
        } as Comparator)

        log.info("Editing version set @|cyan ${vs.name}|@")
        [versionSet: vs, uploadedFiles: uploadedFiles, logEntries: logEntries, primaryTIPLinks: primaryTIPLinks]
    }

    /**
     * Called by ajax after a file is uploaded on the edit page to associate that file with the version set.
     */
    def assignFile() {
        User user = springSecurityService.currentUser
        log.debug("User @|green ${user.username}|@ called assign file upload[id=@|cyan ${params.id}|@] to [@|magenta ${params.versionSetName}|@]...")
        VersionSet vs = resolveVersionSet(params.versionSetName)

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot assign file upload, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        BinaryObject upload = BinaryObject.get(params.id)
        if( upload == null )
            throw new ServletException("No such binary: "+params.id)

        VersionSetUpload uploadAssociation = new VersionSetUpload()
        uploadAssociation.versionSet = vs
        uploadAssociation.uploadedBy = user
        uploadAssociation.artifact = upload
        uploadAssociation.processed = false
        uploadAssociation.save(failOnError: true)

        def responseMap = [
                status: "SUCCESS",
                message: "Successfully assigned file ${upload.originalFilename} to version set ${vs.name}"
        ]

        VersionSetLogEntry.create(vs.id, "FILE_UPLOAD",
                "User[${user.username}] has uploaded file[id=${upload.id}, name=${upload.originalFilename}, size=${FileUtils.byteCountToDisplaySize(upload.fileSize)}]...",
                [user: user.username, upload: [id: upload.id, name: upload.originalFilename, size: upload.fileSize]])

        return responseMap as JSON
    }

    /**
     * Called when user clicks the "Process" button under the file upload control on the index page.
     */
    def processFileUpload() {
        User user = springSecurityService.currentUser
        log.debug("User @|green ${user.username}|@ called process file upload[id=@|cyan ${params.id}|@]...")
        VersionSet vs = resolveVersionSet(params.versionSetName)

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot process file upload, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        BinaryObject upload = BinaryObject.get(params.id)
        if( upload == null )
            throw new ServletException("No such binary: "+params.id)

        UploadProcessFeedback uploadProcessFeedback = UploadProcessFeedback.findByUpload(upload)
        if( uploadProcessFeedback != null ){
            log.info("Already found an existing UploadProcessFeedback object, assuming we are currently processing this upload in another thread.")
        }else{
            final User fUser = user
            final VersionSet fVersionSet = vs
            final BinaryObject fUpload = upload

            Thread processUploadThread = new Thread(new Runnable() {
                @Override
                void run() {
                    Thread.sleep(250); // Give the database just a second to make sure the UploadProcessFeedback write occurs.
                    processUploadService.handleUpload(fUser.username, fVersionSet.id, fUpload.id, fUpload.originalFilename)
                }
            })
            processUploadThread.setName("PROCESS_"+user.getUsername().hashCode()+"_"+System.currentTimeMillis())
            processUploadThread.start()

            uploadProcessFeedback = new UploadProcessFeedback(upload: upload)
            uploadProcessFeedback.threadName = processUploadThread.getName()
            uploadProcessFeedback.threadPid = processUploadThread.getId()
            uploadProcessFeedback.uploadingUser = user?.username ?: request.getRemoteAddr()
            uploadProcessFeedback.hasError = false
            uploadProcessFeedback.percentage = -1
            uploadProcessFeedback.message = "Starting upload processing..."
            uploadProcessFeedback.save(failOnError: true)

            VersionSetLogEntry.create(vs.id, "FILE_PROCESS",
                    "User[${user.username}] has started processing file[id=${upload.id}, name=${upload.originalFilename}]...",
                    [user: user.username, upload: [id: upload.id, name: upload.originalFilename]])

        }

        log.debug("Successfully initialized process thread, sending to process upload page...")
        [versionSet: vs, upload: upload]
    }//end processFileUpload()

    /**
     * Returns a JSON response with the current status of the file upload processing.
     */
    def processStatus() {
        // TODO Update to take in uploadId param and return status based on database table instead of system variables.
        log.debug("Returning process file status[uploadId=${params.uploadId}]...")

        BinaryObject upload = BinaryObject.get(params.uploadId)
        if( !upload ){
            def msg = [status: "ERROR", message: "Cannot find any upload for id ${params.uploadId}."]
            render msg as JSON
            return
        }

        UploadProcessFeedback feedback = UploadProcessFeedback.findByUpload(upload)
        if( !feedback ){
            def msg = [status: "ERROR", message: "Cannot find any feedback for the given upload."]
            render msg as JSON
            return
        }


        def status = [
                status: feedback.hasError ? "ERROR" : "SUCCESS",
                hasError: feedback.hasError,
                phaseJson: feedback.phaseJson,
                message: feedback.message,
                upload: [
                        name: upload.originalFilename,
                        size: upload.fileSize
                ],
                percentage: feedback.percentage,
                stacktraceJson: feedback.stacktraceJson
        ]
        render status as JSON
    }

    /**
     * After a file upload has finished processing (ie, we know what the system wants to do) this action is called
     * to show the user exactly what options are available.
     */
    def chooseActionSummary() {
        User user = springSecurityService.currentUser
        log.debug("User @|green ${user.username}|@ called process file upload[id=@|cyan ${params.id}|@]...")
        VersionSet vs = resolveVersionSet(params.versionSetName)

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot process file upload, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        BinaryObject upload = BinaryObject.get(params.id)
        if( upload == null )
            throw new ServletException("No such binary: "+params.id)

        MemoryProcessingData mpd = MemoryProcessingData.find(upload.id)
        if( !mpd ){
            throw new ServletException("Cannot find any upload data for upload #"+upload.id+": "+upload.originalFilename)
        }

        if( mpd.alreadyChoseActions ){
            log.warn("Cannot show 'chooseActionSummary' becuase the user has already clicked apply.  Showing apply status screen...")
            redirect(action: 'applyChangesView', id: upload.id, params: [versionSetName: vs.name])
        }
        // TODO Validate that we are in the appropriate state.  If not, exit with error.

        [versionSet: vs, upload: upload, mpd: mpd]
    }

    /**
     * When a file upload is in process (not yet committed to database) this method will display artifacts that are
     * associated with it.
     */
    def viewMemoryHtmlArtifact() {
        User user = springSecurityService.currentUser
        log.debug("User @|green ${user.username}|@ viewing artifact for action[id=@|cyan ${params.id}|@]...")
        VersionSet vs = resolveVersionSet(params.versionSetName)
        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot process file upload, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }


        Long uploadId = params.long("uploadId")
        MemoryProcessingData mpd = MemoryProcessingData.find(uploadId)
        if( !mpd )
            throw new ServletException("Cannot find any MPD for uploadId: "+uploadId)

        Integer id = params.int("id")
        ArtifactAction action = null
        for(ArtifactAction current : mpd.artifactActions ?: []){
            if( current.uniqueId == id ){
                action = current
                break
            }
        }

        if( action == null ){
            log.warn("Invalid ID[${id}] given, cannot display.")
            throw new ServletException("Invalid Action ID[${id}] given, there is nothing to display.")
        }

        // TODO We could change based on request type if necessary.  Right now, though, we only care about showing HTML for transient artifacts.
        Serializer serializer = FactoryLoader.getInstance(SerializerFactory.class).getHtmlSerializer()

        StringWriter html = new StringWriter()
        if( action.type == "TD" ){
            def td = mpd.getTd(action.id)
            serializer.serialize(td, html)
        }else {
            def tip = mpd.getTip(action.id)
            serializer.serialize(tip, html)
        }

        log.debug("Displaying artifact HTML...")
        render(text: html.toString(), contentType: 'text/html')
    }

    /**
     * When a file upload is in process (not yet committed to database) this method will display artifacts that are
     * associated with it.
     */
    def ignoreArtifactAction() {
        User user = springSecurityService.currentUser
        log.debug("User @|green ${user.username}|@ ignoring action[id=@|cyan ${params.id}|@]...")
        VersionSet vs = resolveVersionSet(params.versionSetName)
        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot process file upload, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        Long uploadId = params.long("uploadId")
        MemoryProcessingData mpd = MemoryProcessingData.find(uploadId)
        if( !mpd )
            throw new ServletException("Cannot find any MPD for uploadId: "+uploadId)


        Integer id = params.int("id")
        ArtifactAction action = null
        for(ArtifactAction current : mpd.artifactActions ?: []){
            if( current.uniqueId == id ){
                action = current
                break
            }
        }

        if( action == null ){
            log.warn("Invalid ID[${id}] given, cannot display.")
            throw new ServletException("Invalid Action ID[${id}] given, there is nothing to display.")
        }

        ActionType previous = action.actionType
        action.actionType = ActionType.IGNORE

        // FIXME We should probably have a way to reverse this and put it back to "previous"

        flash.message = "Successfully marked action as IGNORE instead of ${previous}."
        redirect(action: 'chooseActionSummary', id: mpd.uploadId, params: [versionSetName: vs.name])
    }

    /**
     * Returns the status of the MemoryProcessingData, by returning it in JSON form.
     */
    def processActionStatus() {
        log.debug("Returning memory processing data for upload file...")
        def status = MemoryProcessingData.buildJsonResponse()
        render status as JSON
    }

    /**
     * A "hidden" method that can clear out the current MemoryProcessData object, so a file is re-processed.  Useful
     * mainly for troubleshooting or debugging purposes.
     */
    def clearMemoryProcessingData() {
        Long id = params.long("id")
        MemoryProcessingData mpd = MemoryProcessingData.find(id)
        if( mpd )
            mpd.reset()
        def response = [
                status: "SUCCESS",
                message: "Successfully reset the MemoryProcessingData used for file upload processing."
        ]
        render response as JSON
    }

    /**
     * This method is called from the process file page, and will apply all the changes from the JSON provided.  Note
     * that the JSON is originally produced from the MemoryProcessingData object - but it contains the changes from the
     * user interface.
     */
    def applyChanges() {
        User user = springSecurityService.currentUser
        String uploadedJSONString = request.inputStream.text
        log.debug("User @|green ${user.username}|@ called apply changes to file upload [id=@|cyan ${params.id}|@]: \n"+uploadedJSONString)
        VersionSet vs = resolveVersionSet(params.versionSetName)

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot process file upload, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        Long uploadId = params.long("id")
        MemoryProcessingData mpd = MemoryProcessingData.find(uploadId)
        if( !mpd )
            throw new ServletException("Cannot find any MPD for uploadId: "+uploadId)
        mpd.alreadyChoseActions = true

        BinaryObject upload = BinaryObject.get(params.id)
        if( upload == null )
            throw new ServletException("No such binary: "+params.id)

        log.info("Successfully resolved all necessary pieces, performing apply...")
        if( !applyChangesService.setExecuting() ){
            log.error("Another ApplyChangesService thread is already executing!")
            def status = [status: "FAILURE", message: "Cannot start Apply Changes Service, because another one is already running.  It must be finished before we can execute another."]
            render status as JSON
            return
        }

        String threadName = "APPLY_CHANGES_"+user.getUsername().hashCode()+"_"+System.currentTimeMillis()

        ApplyChangesFeedback feedback = new ApplyChangesFeedback(upload: upload)
        feedback.uploadingUser = user.username
        feedback.threadPid = 0l
        feedback.threadName = threadName
        feedback.hasError = false
        feedback.percentage = -1
        feedback.message = "Applying changes..."
        feedback.phaseJson = null
        feedback.save(failOnError: true)

        VersionSetLogEntry.create(vs.id, "APPLY_CHANGES",
                "User[${user.username}] is applying changes from file[id=${upload.id}, name=${upload.originalFilename}]...",
                [user: user.username, upload: [id: upload.id, name: upload.originalFilename], actionCount: mpd.artifactActions.size()])

        Thread processUploadThread = new Thread(new Runnable() {
            @Override
            void run() {
                Thread.sleep(500); // Give the system time to create the feedback record.
                applyChangesService.applyChanges(uploadId)
            }
        })
        processUploadThread.setName(threadName)
        processUploadThread.start()

        def status = [
                status: "SUCCESS",
                message: "Successfully started application of changes."
        ]
        render status as JSON
    }//end applyChanges

    /**
     * Responsible for displaying the status of applying changes, after the user has clicked "Apply Actions" on the choose
     * page.
     */
    def applyChangesView() {
        User user = springSecurityService.currentUser
        log.debug("User @|green ${user.username}|@ called view apply changes for file upload[id=@|cyan ${params.id}|@]...")
        VersionSet vs = resolveVersionSet(params.versionSetName)

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot process file upload, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        BinaryObject upload = BinaryObject.get(params.id)
        if( upload == null )
            throw new ServletException("No such binary: "+params.id)

        [upload: upload, versionSet: vs]
    }

    /**
     * After the applyChanges() method kicks off changes to the current version set, then this method can be used
     * to check the status of those changes from the database.
     */
    def applyChangesStatus() {
        BinaryObject upload = BinaryObject.get(params.id)
        if( upload == null )
            throw new ServletException("No such binary: "+params.id)

        ApplyChangesFeedback feedback = ApplyChangesFeedback.findByUpload(upload)
        if( feedback == null )
            throw new ServletException("No such feedback for binary: "+params.id)

        def status = [
                upload: [
                        id: upload.id,
                        filename: upload.originalFilename,
                        size: upload.fileSize
                ],
                uploadingUser: feedback.uploadingUser,
                dateCreated: feedback.dateCreated.getTime(),
                message: feedback.message,
                phaseJson: feedback.phaseJson,
                percentage: feedback.percentage,
                hasError: feedback.hasError,
                thread: [
                        name: feedback.threadName,
                        pid: feedback.threadPid
                ],
                stacktraceJson: feedback.stacktraceJson,
                executing: SystemVariable.quickFindPropertyValue(ApplyChangesService.APPLY_CHANGES_EXECUTING)
        ]
        log.debug("Returning process file status...")
        render status as JSON
    }

    /**
     * Allows you to edit the trustmark definitions for this verison set.
     */
    def trustmarkDefinitions() {
        User user = springSecurityService.currentUser
        log.debug("Request to manage TrustmarkDefinitions of VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot process file upload, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        log.info("Resolving parameters...")
        int offset = params.int("offset") ?: 0
        int max = params.int("max") ?: 25
        String sort = params.sort ?: 'trustmarkDefinition.name'
        String order = params.order ?: 'asc'

        params.max = max.toString()
        params.offset = offset.toString()
        params.sort = sort
        params.order = order

        Long total = VersionSetTDLink.countByVersionSet(vs)

        log.debug("Selecting ${offset}-${offset+max} TDs for VS[${vs.name}]...")
        List<VersionSetTDLink> links = VersionSetTDLink.findAllByVersionSet(vs, params)

        log.debug("Rendering manage trustmark definitions page...")
        [links: links, total: total, versionSet: vs, user: user]

    }

    /**
     * Removes a trustmark definition.  Performs the actual operation, so user interfaces had better make sure they
     * double check.
     */
    def deleteTrustmarkDefinition() {
        User user = springSecurityService.currentUser
        log.debug("Request to manage TrustmarkDefinitions of VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot process file upload, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        VersionSetTDLink link = VersionSetTDLink.get(params.linkId)
        if( !link ){
            log.warn("Was not given any valid version set link id in the linkId parameter!")
            throw new ServletException("Was not given any valid version set link id in the linkId parameter!")
        }
        log.info("User[@|green ${user.username}|@] is removing link[@|cyan ${link.id}|@] to TD[@|green ${link.trustmarkDefinition.identifier}|@] from VersionSet[@|green ${vs.name}|@]...")
        link.delete()

        log.debug("Removing keywords related to TrustmarkDefinition[${link.trustmarkDefinition.identifier}]...")
        List<KeywordTDLink> links = KeywordTDLink.findAllByVersionSetAndTd(vs, link.trustmarkDefinition)
        if( links != null && links.size() > 0 ){
            for( KeywordTDLink kwLink : links ){
                kwLink.delete()
            }
        }

        VersionSetLogEntry.create(vs.id, "DELETE_TD", "User[${user.username}] has deleted Trustmark Definition[${link.trustmarkDefinition.name}] from version set ${vs.name}...",
                [user: user.username, trustmarkDefinition: [identifier: link.trustmarkDefinition.identifier, name: link.trustmarkDefinition.name, version: link.trustmarkDefinition.tdVersion]])

        flash.message = "Successfully removed trustmark definition \"${link.trustmarkDefinition.name}, v${link.trustmarkDefinition.tdVersion}\"."
        redirect(action: 'trustmarkDefinitions', id: vs.name)
    }

    /**
     * Displays the Trustmark Definition edit page, with the given trustmark definition.
     */
    def createTrustmarkDefinitionComplete() {
        User user = springSecurityService.currentUser
        log.info("Request to create complete TrustmarkDefinition on VersionSet: @|cyan ${params.id}|@")
        return render(view: 'deprecatedEditor')  // SHORT CIRCUIT this page to prevent editing TI-1806

        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot edit trustmark definition, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }


        log.debug("Rendering HTML template...")
        BuilderFactory builderFactory = FactoryLoader.getInstance(BuilderFactory.class)
        TrustmarkDefinitionBuilder tdBuilder = builderFactory.createTrustmarkDefinitionBuilder()
        tdBuilder.setPublicationDateTime(new Date())
        tdBuilder.setIdentifier(TFAMPropertiesHolder.getBaseURLs().get(0).toString()+"/trustmark-definitions/_name_/_version_")
        tdBuilder.setTrustmarkReferenceAttributeName(TFAMPropertiesHolder.getBaseURLs().get(0).toString()+"/trustmark-definitions/_name_/_version_/refAttribute")
        tdBuilder.setTrustmarkDefiningOrganization(TFAMPropertiesHolder.getDefaultEntity())
        tdBuilder.setIssuanceCriteria("yes(all)")
        edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td = tdBuilder.buildWithNoValidation()
        StringWriter jsonWriter = new StringWriter()
        FactoryLoader.getInstance(SerializerFactory.class).getJsonSerializer().serialize(td, jsonWriter, [:])
        render(view: '/versionSetEdit/editTrustmarkDefinition', model: [vs: vs, trustmarkDefinition:  td, tdJson: "\n\nTRUSTMARK_DEFINITION = " + jsonWriter.toString(), linkId: -1])
    }

    /**
     * Displays the Trustmark Definition edit page, with the given trustmark definition.
     */
    def editTrustmarkDefinition() {
        User user = springSecurityService.currentUser
        log.debug("Request to edit TrustmarkDefinition ${params.linkId} of VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot edit trustmark definition, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        VersionSetTDLink link = VersionSetTDLink.get(params.linkId)
        if( !link ){
            log.warn("Was not given any valid version set link id in the linkId parameter!")
            throw new ServletException("Was not given any valid version set link id in the linkId parameter!")
        }
        log.info("User[@|green ${user.username}|@] is editing link[@|cyan ${link.id}|@] to TD[@|green ${link.trustmarkDefinition.identifier}|@] from VersionSet[@|green ${vs.name}|@]...")

        log.debug("Rendering HTML template...")
        TrustmarkDefinitionResolver trustmarkDefinitionResolver = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class)
        edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td = trustmarkDefinitionResolver.resolve(link.trustmarkDefinition.artifact.content.toFile())
        StringWriter jsonWriter = new StringWriter()
        FactoryLoader.getInstance(SerializerFactory.class).getJsonSerializer().serialize(td, jsonWriter, [:])
        [vs: vs, trustmarkDefinition:  td, tdJson: "\n\nTRUSTMARK_DEFINITION = "+jsonWriter.toString(), linkId: link.id]
    }

    /**
     * Save the trustmark definition
     */
    def saveTrustmarkDefinition(){
        User user = springSecurityService.currentUser
        log.debug("Request to save TrustmarkDefinition ${params.linkId} of VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot save trustmark definition, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        def response = [:]

        edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition uploadedTd = null
        try {
            log.debug("Resolving uploaded JSON Text to a TD object...")
            uploadedTd = resolveTdFromJson(request)
        }catch(Throwable t){
            log.warn("Error resolving TrustmarkDefinition from uploaded json", t)
            response["status"] = "ERROR"
            response["message"] = t.getMessage()
            render response as JSON
            return
        }

        // Remove the existing VersionSetTDLink if it exists...
        VersionSetTDLink.withTransaction {
            if (StringUtils.isNotBlank(params.linkId) && !params.linkId.equals("-1")) {
                log.debug("Removing existing VersionSetTDLink...")
                VersionSetTDLink link = VersionSetTDLink.get(params.linkId)
                if (!link) {
                    log.warn("Was not given any valid version set link id in the linkId parameter!")
                    throw new ServletException("Was not given any valid version set link id in the linkId parameter!")
                }

                if (link.copyOver) {
                    log.warn("Cannot edit TD ${link.id}, because it's a copy over from a previous verison set.")
                    throw new ServletException("Cannot edit this Trustmark Definition, because it's a copy over from a previous verison set.")
                }

                if (!link.getStatus().equalsIgnoreCase("EDITABLE")) {
                    log.warn("Cannot edit TD ${link.id}, because it's status is ${link.status} instead of EDITABLE.")
                    throw new ServletException("Cannot edit this Trustmark Definition, as it's status is not EDITABLE.")
                }

                // Assuming the TD Previously existed, then we should remove any of those previously existing keyword links.
                List<KeywordTDLink> tdKeywordLinks = KeywordTDLink.findAllByVersionSetAndTd(vs, link.trustmarkDefinition)
                for (KeywordTDLink previousKeywordLink : tdKeywordLinks ?: []) {
                    previousKeywordLink.delete()
                }

                link.delete(flush: true)
            }
        }

        VersionSetTDLink.withTransaction {
            VersionSetTDLink link = new VersionSetTDLink(versionSet: vs)
            link.copyOver = false
            link.status = "EDITABLE"

            log.info("User[@|green ${user.username}|@] is saving link[@|cyan ${link.id}|@] to TD[@|green ${link.trustmarkDefinition?.identifier}|@] from VersionSet[@|green ${vs.name}|@]...")

            try {
                // TODO Consider link.status as well.
                TrustmarkDefinition databaseTd = applyChangesService.storeTd(user.username, uploadedTd)
                databaseTd.save(failOnError: true)
                link.trustmarkDefinition = databaseTd
                link.tdIdentifier = databaseTd.identifier
                link.save(failOnError: true)

                log.info("Adding keywords...")
                def keywordList = uploadedTd.getMetadata().getKeywords()
                for (String keyword : keywordList ?: []) {
                    Keyword keywordFromDb = Keyword.findByNameIlike(keyword)
                    if (!keywordFromDb) {
                        keywordFromDb = new Keyword(name: keyword)
                        keywordFromDb.save(failOnError: true)
                    }

                    KeywordTDLink keywordLink = new KeywordTDLink(versionSet: vs, keyword: keywordFromDb, td: link.trustmarkDefinition)
                    keywordLink.save(failOnError: true)
                }

                response["status"] = "SUCCESS"
                response["message"] = "Successfully uploaded TD!"
                response["forwardUrl"] = createLink(controller: 'versionSetEdit', action: 'trustmarkDefinitions', id: vs.name, params: [linkId: link.id])

                VersionSetLogEntry.create(vs.id, "SAVED_TD", "User[${user.username}] has saved Trustmark Definition[${uploadedTd.getMetadata().getName()}] for version set ${vs.name}...",
                        [user: user.username, trustmarkDefinition: [identifier: link.trustmarkDefinition.identifier, name: link.trustmarkDefinition.name, version: link.trustmarkDefinition.tdVersion]])

                flash.message = "Successfully updated Trustmark Definition[${uploadedTd.getMetadata().getName()}, v${uploadedTd.getMetadata().getVersion()}]"
            } catch (Throwable t2) {
                log.error("Invalid TD!", t2)
                response["status"] = "ERROR"
                response["message"] = t2.getMessage()
            }
        }

        render response as JSON
    }

    /**
     * Displays the "Simple" Trustmark Definition edit page.
     */
    @Secured("@tfamSecurity.hasLock(authentication, request)")
    def simpleTdEditor() {
        User user = springSecurityService.currentUser
        log.info("Request to create simple TrustmarkDefinition on VersionSet: @|cyan ${params.id}|@")

        return render(view: 'deprecatedEditor')  // SHORT CIRCUIT this page to prevent editing TI-1806

        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot edit trustmark definition, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        log.debug("Displaying simple TD form...")
        [versionSet: vs, command: new SimpleTrustmarkDefinitionCommand()]
    }

    /**
     * Processes the simpleTdEditor form.
     */
    def saveSimpleTd(SimpleTrustmarkDefinitionCommand command) {
        User user = springSecurityService.currentUser
        log.info("Request to save simple TrustmarkDefinition on VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot edit trustmark definition, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        if( command.hasErrors() ){
            log.error("Simple TD Command has errors: ")
            for( ObjectError oe : command.errors.allErrors ){
//                log.error("   "+message(error: oe))
                log.error("   "+oe)
            }
            return render(view: 'simpleTdEditor', model: [command: command, versionSet: vs])
        }

        edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition uploadedTd = buildSimpleTd(command, user)
        try {
            TrustmarkDefinition databaseTd = applyChangesService.storeTd(user.username, uploadedTd)

            VersionSetTDLink link = new VersionSetTDLink(versionSet: vs)
            link.copyOver = false
            link.status = "EDITABLE"
            link.trustmarkDefinition = databaseTd
            link.tdIdentifier = databaseTd.identifier
            link.save(failOnError: true)

            try{
                for ( String keyword : uploadedTd.getMetadata().getKeywords() ){
                    Keyword keywordFromDb = Keyword.findByNameIlike(keyword)
                    if (keywordFromDb == null) {
                        keywordFromDb = new Keyword(name: keyword)
                        keywordFromDb.save(failOnError: true)
                    }

                    log.debug("Storing keyword link to: @|cyan ${keyword}|@")
                    KeywordTDLink keywordLink = new KeywordTDLink(versionSet: vs, keyword: keywordFromDb, td: databaseTd)
                    keywordLink.save(failOnError: true)
                }
            }catch(Throwable t2){
                log.warn("error saving keywords - IGNORING!", t2)
            }

            VersionSetLogEntry.create(vs.id, "CREATE_SIMPLE_TD", "User[${user.username}] has created Trustmark Definition[${uploadedTd.getMetadata().getName()}] for version set ${vs.name}...",
                    [user: user.username, trustmarkDefinition: [identifier: link.trustmarkDefinition.identifier, name: link.trustmarkDefinition.name, version: link.trustmarkDefinition.tdVersion]])

            flash.message = "Successfully created simple Trustmark Definition[${uploadedTd.getMetadata().getName()}, v${uploadedTd.getMetadata().getVersion()}]"
            return redirect(action: 'trustmarkDefinitions', id: vs.name)
        }catch(Throwable t){
            log.error("Invalid TD!", t)
            command.errors.reject("unexpected.error.saving.td", [t.toString(), t.message] as Object[], "An unexpected error occurred saving Trustmark Definition: "+t.toString())
            return render(view: 'simpleTdEditor', model: [command: command, versionSet: vs])
        }
    }


    /**
     * Allows you to edit the TIPs for this verison set.
     */
    def trustInteroperabilityProfiles() {
        User user = springSecurityService.currentUser
        log.debug("Request to manage TIPs of VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot manage TIPs, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        log.info("Resolving parameters...")
        int offset = params.int("offset") ?: 0
        int max = params.int("max") ?: 25
        String sort = params.sort ?: 'trustInteroperabilityProfile.name'
        String order = params.order ?: 'asc'

        params.max = max.toString()
        params.offset = offset.toString()
        params.sort = sort
        params.order = order

        Long total = VersionSetTIPLink.countByVersionSet(vs)

        /*
         * I feel the need to explain this goofy code section.  Hibernate started acting crazy and returning invalid values that
         * were inconsistent with the database when joining the TIP table to the TIP Link table.  I'm not really sure why this
         * was, I didn't have the time to figure out.  When I select each one individually, however, it seems to be fine.
         */
        log.debug("Selecting ${offset}-${offset+max} TIPs for VS[${vs.name}]...")
        def ids = VersionSetTIPLink.executeQuery("select id, trustInteroperabilityProfile.id from VersionSetTIPLink where versionSet = :vs", [vs: vs], params)
        log.info("IDs: "+ids)

        def links = []
        def tips = []
        for( def obj : ids ){
            links.add(VersionSetTIPLink.get(obj[0]))
            tips.add(TrustInteroperabilityProfile.get(obj[1]))
        }

        log.debug("Rendering manage TIPs page...")
        [links: links, total: total, versionSet: vs, user: user]
    }

    /**
     * Removes a TIP.  Performs the actual operation, so user interfaces had better make sure they
     * double check.
     */
    def deleteTIP() {
        User user = springSecurityService.currentUser
        log.debug("Request to delete TIP @|yellow ${params.linkId}|@ from VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot delete tip, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        VersionSetTIPLink.withTransaction {
            VersionSetTIPLink link = VersionSetTIPLink.get(params.linkId)
            if (!link) {
                log.warn("Was not given any version set link id in the linkId parameter!")
                throw new ServletException("Was not given any version set link id in the linkId parameter!")
            }
            log.info("User[@|green ${user.username}|@] is removing link[@|cyan ${link.id}|@] to TIP[@|green ${link.trustInteroperabilityProfile.identifier}|@] from VersionSet[@|green ${vs.name}|@]...")
            link.delete()
            flash.message = "Successfully removed trust interoperability profile \"${link.trustInteroperabilityProfile.name}, v${link.trustInteroperabilityProfile.tipVersion}\"."
        }

        redirect(action: 'trustInteroperabilityProfiles', id: vs.name)
    }

    /**
     * Marks a TIP as primary or not.  Note that tip/listPrimary has similar functionality (after release)
     */
    def toggleTIPPrimary() {
        User user = springSecurityService.currentUser
        log.debug("Request to toggle primary for TIP @|yellow ${params.linkId}|@ from VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot toggle primary on tip, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        def response = null
        VersionSetTIPLink.withTransaction {
            VersionSetTIPLink link = VersionSetTIPLink.get(params.linkId)
            if (!link) {
                log.warn("Was not given any version set link id in the linkId parameter!")
                throw new ServletException("Was not given any version set link id in the linkId parameter!")
            }
            log.info("User[@|green ${user.username}|@] is toggling primary on link[@|cyan ${link.id}|@] to TIP[@|green ${link.trustInteroperabilityProfile.identifier}|@] from VersionSet[@|green ${vs.name}|@]...")

            boolean primary = link.primaryTIP
            link.primaryTIP = !primary
            link.save(failOnError: true)

            response = [status: "SUCCESS", message: "Successfully set primary to " + (!primary ? "Yes" : "No"), primary: !primary]
        }

        withFormat {
            json {
                render response as JSON
            }
            xml {
                render response as XML
            }
            '*' {
                render response as JSON
            }
        }
    }

    /**
     * Displays the Trustmark Definition edit page, with the given trustmark definition.
     */
    def createTrustInteroperabilityProfile() {
        User user = springSecurityService.currentUser
        log.info("Request to create complete TIP on VersionSet: @|cyan ${params.id}|@")
        return render(view: 'deprecatedEditor')  // SHORT CIRCUIT this page to prevent editing TI-1806

        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot edit TIP, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        TrustInteroperabilityProfileBuilder builder = FactoryLoader.getInstance(BuilderFactory.class).createTrustInteroperabilityProfileBuilder()
        builder.setIdentifier(TFAMPropertiesHolder.getBaseURLs().get(0).toString()+"/trust-interoperability-profiles/_name_/_version_/")
        builder.setIssuerOrganization(TFAMPropertiesHolder.getDefaultEntity())
        builder.setPublicationDateTime(new Date())
        builder.addTrustInteroperabilityProfileReference("tip1", "http://gtri.org/tip1")
        edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip = builder.buildWithNoValidation()

        Serializer jsonOut = FactoryLoader.getInstance(SerializerFactory.class).getJsonSerializer()
        StringWriter jsonWriter = new StringWriter()
        jsonOut.serialize(tip, jsonWriter)

        JSONObject json = new JSONObject(jsonWriter.toString())
        json.put("References", new JSONArray())

        Map model = [vs: vs, user: user, "SET_TIP_HERE" : "\n\nTIP = "+json.toString(2), linkId: -1]
        String searchUrl =  createLink(controller:'search', action:'index')
        log.debug("Search URL: "+searchUrl)
        model.put("SEARCH_URL", searchUrl)

        return model
    }

    /**
     * Displays the TrustInteroperabilityProfile edit page, with the given TrustInteroperabilityProfile.
     */
    def editTrustInteroperabilityProfile() {
        User user = springSecurityService.currentUser
        log.debug("Request to edit TrustInteroperabilityProfile ${params.linkId} of VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot edit TrustInteroperabilityProfile, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        VersionSetTIPLink link = VersionSetTIPLink.get(params.linkId)
        if( !link ){
            log.warn("Was not given any valid version set link id in the linkId parameter!")
            throw new ServletException("Was not given any valid version set link id in the linkId parameter!")
        }
        log.info("User[@|green ${user.username}|@] is editing link[@|cyan ${link.id}|@] to TD[@|green ${link.trustInteroperabilityProfile.identifier}|@] from VersionSet[@|green ${vs.name}|@]...")

        def model = [SEARCH_URL: "${createLink(controller:'search', action: 'index')}", vs: vs, user: user, linkId: link.id]

        BinaryObject object = link.trustInteroperabilityProfile.artifact
        File f = object.content.toFile()
        edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tipData =
                FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class).resolve(f)

        Serializer jsonOut = FactoryLoader.getInstance(SerializerFactory.class).getJsonSerializer()
        StringWriter jsonWriter=  new StringWriter()
        jsonOut.serialize(tipData, jsonWriter)
        jsonWriter.toString()

        model.put("SET_TIP_HERE", "\n\nTIP = "+jsonWriter.toString())

        log.debug("Showing edit TIP form...")
        return render(view: 'createTrustInteroperabilityProfile', model: model)
    }

    /**
     * Called to save a TIP.
     */
    def saveTrustInteroperabilityProfile() {
        User user = springSecurityService.currentUser
        log.debug("Request to save TrustInteroperabilityProfile ${params.linkId} of VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")
        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot save TIP, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        def response = [:]

        edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile uploadedTip = null
        try {
            log.debug("Resolving uploaded JSON Text to a TIP object...")
            uploadedTip = resolveTipFromJson(request)
        }catch(Throwable t){
            log.warn("Error resolving TrustInteropProfile from uploaded json", t)
            response["status"] = "ERROR"
            response["message"] = t.getMessage()
            render response as JSON
            return
        }

        log.info("User[@|green ${user.username}|@] is saving link to TIP[@|green ${uploadedTip?.identifier}|@] from VersionSet[@|green ${vs.name}|@]...")

        // Remove any existing keywords or links...
        VersionSetTIPLink.withTransaction {
            if (StringUtils.isNotBlank(params.linkId) && !params.linkId.equals("-1")) {
                VersionSetTIPLink link = VersionSetTIPLink.get(params.linkId)
                if (!link) {
                    log.warn("Was not given any valid version set link id in the linkId parameter!")
                    throw new ServletException("Was not given any valid version set link id in the linkId parameter!")
                }

                if (link.copyOver) {
                    log.warn("Cannot edit TIP ${link.id}, because it's a copy over from a previous verison set.")
                    throw new ServletException("Cannot edit this Trust Profile, because it's a copy over from a previous version set.")
                }

                if (!link.getStatus().equalsIgnoreCase("EDITABLE")) {
                    log.warn("Cannot edit TIP ${link.id}, because it's status is ${link.status} instead of EDITABLE.")
                    throw new ServletException("Cannot edit this Trust Profile, as it's status is not EDITABLE.")
                }

                // Assuming the TD Previously existed, then we should remove any of those previously existing keyword links.
                List<KeywordTIPLink> tipKeywordLinks = KeywordTIPLink.findAllByVersionSetAndTip(vs, link.trustInteroperabilityProfile)
                for (KeywordTIPLink previousKeywordLink : tipKeywordLinks ?: []) {
                    previousKeywordLink.delete()
                }

                link.delete()
            }
        }

        VersionSetTIPLink.withTransaction {
            VersionSetTIPLink link = new VersionSetTIPLink(versionSet: vs)
            link.copyOver = false
            link.primaryTIP = false
            link.status = "EDITABLE"

            try {
                log.debug("Storing as a database TIP...")
                TrustInteroperabilityProfile databaseTip = applyChangesService.storeTip(user.username, uploadedTip)
                link.trustInteroperabilityProfile = databaseTip
                link.tipIdentifier = databaseTip.identifier
                log.debug("Storing VersionSetTIPLink...")
                link.save(failOnError: true)

                log.info("Adding keywords...")
                def keywordList = uploadedTip.getKeywords()
                for (String keyword : keywordList ?: []) {
                    Keyword keywordFromDb = Keyword.findByNameIlike(keyword)
                    if (!keywordFromDb) {
                        keywordFromDb = new Keyword(name: keyword)
                        keywordFromDb.save(failOnError: true)
                    }

                    KeywordTIPLink keywordLink = new KeywordTIPLink(versionSet: vs, keyword: keywordFromDb, tip: link.trustInteroperabilityProfile)
                    keywordLink.save(failOnError: true)
                }

                response["status"] = "SUCCESS"
                response["message"] = "Successfully uploaded TIP!"
                response["forwardUrl"] = createLink(controller: 'versionSetEdit', action: 'trustInteroperabilityProfiles', id: vs.name, params: [linkId: link.id])

                VersionSetLogEntry.create(vs.id, "SAVED_TIP", "User[${user.username}] has saved Trust Interoperability Profile[${uploadedTip.getName()}] for version set ${vs.name}...",
                        [user: user.username, trustInteroperabilityProfile: [identifier: link.trustInteroperabilityProfile.identifier, name: link.trustInteroperabilityProfile.name, version: link.trustInteroperabilityProfile.tipVersion]])

                flash.message = "Successfully updated Trust Interoperability Profile[${uploadedTip.getName()}, v${uploadedTip.getVersion()}]"
            } catch (Throwable t) {
                log.error("Invalid TIP!", t)
                response["status"] = "ERROR"
                response["message"] = t.getMessage()
            }
        }

        log.info("Successfully stored TIP!")
        render response as JSON
    }

    /**
     * Displays the "Simple" TIP edit page.
     */
    def simpleTipEditor() {
        User user = springSecurityService.currentUser
        log.info("Request to create simple Trust Profile on VersionSet: @|cyan ${params.id}|@")
        return render(view: 'deprecatedEditor')  // SHORT CIRCUIT this page to prevent editing TI-1806

        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot edit trust profile, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        SimpleTrustProfileCommand command = null

        if( params.tipId ){
            TrustInteroperabilityProfile tip = TrustInteroperabilityProfile.get(params.tipId)
            if( !tip ){
                log.error("Cannot find any tip with id: "+params.tipId)
                throw new ServletException("Cannot find any TIP with the given ID.")
            }
            command = new SimpleTrustProfileCommand()
            command.tipId = tip.id
            command.name = tip.name
            command.description = tip.description
            command.referenceJson = buildReferenceJsonString(tip)
        }else{
            command = new SimpleTrustProfileCommand()
        }

        log.debug("Displaying simple TIP form...")
        [versionSet: vs, command: command]
    }

    /**
     * Called to save a simple TIP.
     */
    def saveSimpleTip(SimpleTrustProfileCommand command) {
        User user = springSecurityService.currentUser
        log.info("Request to save simple Trust Profile on VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        if( vs.isProduction() || !vs.isEditable() )
            throw new ServletException("Cannot edit version set ${vs.name} because it is either production or not editable.")

        if( !user.username.equalsIgnoreCase(vs.lockedBy?.username) ){
            log.error("User[@|green ${user.username}|@] cannot edit trust profile, since user[@|yellow ${vs.lockedBy?.username}|@] has the version set locked.")
            throw new ServletException("This version set is locked by another user[${vs.lockedBy?.username}].  Please have it unlocked first.")
        }

        if( command.hasErrors() ){
            log.error("Simple TIP Command has errors: ")
            for( ObjectError oe : command.errors.allErrors ){
//                log.error("   "+message(error: oe))
                log.error("   "+oe)
            }
            return render(view: 'simpleTipEditor', model: [command: command, versionSet: vs])
        }


        log.debug("Reference JSON: \n"+command.referenceJson)

        log.debug("Building TIP from Web form...")
        edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip = buildSimpleTip(command, user)

        log.debug("Caching as Database Tip...")
        TrustInteroperabilityProfile databaseTip = applyChangesService.storeTip(user.username, tip)

        log.debug("Removing old TIP if necessary...")
        if( StringUtils.isNotBlank(command.tipId) ){
            VersionSetTIPLink.withTransaction {
                TrustInteroperabilityProfile oldTip = TrustInteroperabilityProfile.get(command.tipId)
                KeywordTIPLink.executeUpdate("delete from KeywordTIPLink where tip = :tip", [tip: oldTip])
                VersionSetTIPLink tipLink = VersionSetTIPLink.findByTrustInteroperabilityProfile(oldTip)
                tipLink.delete(failOnError: true)
                TipTreeCache.findByTip(oldTip)?.delete(failOnError: true)
                oldTip.delete(failOnError: true)
            }
        }

        log.debug("Saving TIP Link...")
        VersionSetTIPLink tipLink = new VersionSetTIPLink(versionSet: vs)
        tipLink.trustInteroperabilityProfile = databaseTip
        tipLink.tipIdentifier = tip.identifier.toString()
        tipLink.copyOver = false
        tipLink.status = "EDITABLE"
        tipLink.primaryTIP = false
        tipLink.save(failOnError: true)

        log.debug("Saving keyword links...")
        for( String keyword : tip.getKeywords() ?: [] ){
            Keyword databaseKeyword = Keyword.findByNameIlike(keyword)
            if( !databaseKeyword ) {
                databaseKeyword = new Keyword(name: keyword)
                databaseKeyword.save(failOnError: true)
            }
            KeywordTIPLink keywordTIPLink = new KeywordTIPLink(versionSet: vs, keyword: databaseKeyword)
            keywordTIPLink.tip = databaseTip
            keywordTIPLink.save(failOnError: true)
        }

        new Thread(new Runnable(){
            public void run(){
                VersionSetTIPLink.withTransaction {
                    Thread.sleep(1000)
                    log.debug("Building TIP index...")
                    VersionSetTIPLink.search().createIndexAndWait()
                }
            }
        }).start(); // Kick this bad boy off so our TIPs are found in the searches.

        if( StringUtils.isBlank(command.id) ) {
            flash.message = "Successfully saved Trust Profile: " + tip.getName()
        }else{
            flash.message = "Successfully updated Trust Profile: " + tip.getName()
        }
        return redirect(action: 'trustInteroperabilityProfiles', id: vs.name)
    }

    //==================================================================================================================
    //  HELPER METHODS
    //==================================================================================================================

    private edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile resolveTipFromJson(HttpServletRequest request){
        String jsonText = request.inputStream.text
        log.debug("JSON: "+jsonText)
        JSONObject json = new JSONObject(jsonText)
        if( json.opt("PublicationDateTime") != null ){
            Object val = json.opt("PublicationDateTime")
            Calendar c = null
            try{
                Long timestamp = Long.parseLong(val.toString())
                c = Calendar.getInstance()
                c.setTimeInMillis(timestamp)
            }catch(Throwable t){
                c = javax.xml.bind.DatatypeConverter.parseDateTime(val.toString())
            }
            log.info("Publication date time value[@|green ${val.getClass().getName()}|@]: ${c.getTime()}")
            TimeZone tz = TimeZone.getTimeZone("UTC")
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            df.setTimeZone(tz)
            String nowAsISO = df.format(c.getTime())
            json.put("PublicationDateTime", nowAsISO)
            jsonText = json.toString()
        }

        json = new JSONObject(jsonText)
        JSONObject referencesJson = json.optJSONObject("References")
        JSONArray tdReqArray = referencesJson.optJSONArray("TrustmarkDefinitionRequirements")
        if( tdReqArray != null && tdReqArray.length() > 0 ){
            for( int i = 0; i < tdReqArray.length(); i++ ){
                JSONObject tdReq = tdReqArray.optJSONObject(i)
                JSONArray providerReferences = tdReq.optJSONArray("ProviderReferences")
                if( providerReferences?.length() > 0 ){
                    for( int j = 0; j < providerReferences?.length(); j++ ){
                        JSONObject providerObj = providerReferences.optJSONObject(j)
                        expandProviderJson(providerObj)
                    }
                }
            }
        }
        jsonText = json.toString()

        TrustInteroperabilityProfileResolver resolver = FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class)
        edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile uploadedTip = resolver.resolve(jsonText, true)

        String tipId = uploadedTip.getIdentifier().toString()
        boolean matches = false
        for(String url : TFAMPropertiesHolder.baseURLsAsStrings ){
            if( tipId.startsWith(url) ){
                matches = true
            }
        }
        if( !matches ){
            log.warn("The user is trying to store a TIP with a bad base url: "+tipId)
            throw new ResolveException("The base URL for Identifier[${tipId}] is not supported.  You may only use a base URL supported: "+TFAMPropertiesHolder.baseURLsAsStrings)
        }

        return uploadedTip
    }

    private void expandProviderJson(JSONObject providerObj){
        if( providerObj == null )
            return
        if( providerObj.optString("\$id") != null && providerObj.optString("Identifier") != null){
            Provider p = Provider.findByUri(providerObj.optString("Identifier"))
            if( p ){
                providerObj.put("Name", p.name)
                JSONObject primaryContactJson = new JSONObject()
                primaryContactJson.put("Kind", "PRIMARY")
                if( StringUtils.isNotBlank(p.responder) )
                    primaryContactJson.put("Responder", p.responder)
                primaryContactJson.put("Email", p.email)
                if( StringUtils.isNotBlank(p.telephone) )
                    primaryContactJson.put("Telephone", p.telephone)
                if( StringUtils.isNotBlank(p.mailingAddress) )
                    primaryContactJson.put("MailingAddress", p.mailingAddress)
                if( StringUtils.isNotBlank(p.notes) )
                    primaryContactJson.put("Notes", p.notes)
                providerObj.put("PrimaryContact", primaryContactJson)
            }
        }
    }

    private edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition resolveTdFromJson(HttpServletRequest request){
        String jsonText = request.inputStream.text
        log.debug("JSON: " + jsonText)
        JSONObject json = new JSONObject(jsonText)
        if (json.optJSONObject("Metadata")?.opt("PublicationDateTime") != null) {
            Object val = json.optJSONObject("Metadata")?.opt("PublicationDateTime")
            Calendar c = null
            try {
                Long timestamp = Long.parseLong(val.toString())
                c = Calendar.getInstance()
                c.setTimeInMillis(timestamp)
            } catch (Throwable t) {
                c = javax.xml.bind.DatatypeConverter.parseDateTime(val.toString())
            }
            log.debug("Publication date time value[@|green ${val.getClass().getName()}|@]: ${c.getTime()}")
            TimeZone tz = TimeZone.getTimeZone("UTC")
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            df.setTimeZone(tz)
            String nowAsISO = df.format(c.getTime())
            json.optJSONObject("Metadata").put("PublicationDateTime", nowAsISO)
            jsonText = json.toString()
        }

        TrustmarkDefinitionResolver resolver = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class)
        def uploadedTd = resolver.resolve(jsonText, true)


        String tdId = uploadedTd.getMetadata().getIdentifier().toString()
        boolean matches = false
        for(String url : TFAMPropertiesHolder.baseURLsAsStrings ){
            if( tdId.startsWith(url) ){
                matches = true
            }
        }
        if( !matches ){
            log.warn("The user is trying to store a TD with a bad base url: "+tdId)
            throw new ResolveException("The base URL for Identifier[${tdId}] is not supported.  You may only use a base URL supported: "+TFAMPropertiesHolder.baseURLsAsStrings)
        }

        return uploadedTd
    }


    protected String buildReferenceJsonString(TrustInteroperabilityProfile databaseTip){
        Map json = [ references: [] ]

        edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip =
                FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class).resolve(databaseTip.artifact.content.toFile())

        for(AbstractTIPReference tipReference : tip.getReferences() ){
            Map refJson = [
                    Type: tipReference.isTrustmarkDefinitionRequirement() ? "TrustmarkDefinition" : "TrustInteroperabilityProfile",
                    Identifier: tipReference.getIdentifier().toString(),
                    Name: tipReference.getName(),
                    Version: tipReference.getVersion(),
                    Description: tipReference.getDescription()
            ]

            if( tipReference.isTrustmarkDefinitionRequirement() ){
                refJson.put("Providers", buildProvidersJson( (TrustmarkDefinitionRequirement) tipReference))
            }

            // TODO - missing Deprecated, Keywords, PublisherIdentifier, PublisherName, and _links

            json.references.add(refJson)
        }

        return JsonOutput.toJson(json)
    }

    protected List buildProvidersJson(TrustmarkDefinitionRequirement tdReq){
        def providers = []
        for( Entity e : tdReq.getProviderReferences() ){
            Map json = [
                    Identifier: e.getIdentifier().toString(),
                    Name: e.getName(),
                    Contact: [
                            Responder: e.getDefaultContact()?.getResponder(),
                            Email: e.getDefaultContact()?.getDefaultEmail(),
                            Telephone: e.getDefaultContact()?.getDefaultTelephone()
                    ]
            ]
            providers.add(json)
        }
        return providers
    }

    protected edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile buildSimpleTip(SimpleTrustProfileCommand command, User user) {
        String tipId = cleanseMoniker(generateMoniker(command.name))
        String id = buildId(command.baseUrl, "trust-interoperability-profiles", tipId,"1.0")

        TrustInteroperabilityProfileBuilder builder = FactoryLoader.getInstance(BuilderFactory.class).createTrustInteroperabilityProfileBuilder()
        builder.setIdentifier(id)
        builder.setName(command.name)
        builder.setDescription(command.description)
        builder.setPublicationDateTime(new Date())
        builder.setIssuerOrganization(TFAMPropertiesHolder.getDefaultEntity())
        builder.setNotes("Generated Simply by the Trustmark Policy Authoring Tool (TPAT) by "+user.username)

        List<String> keywords = autoGenerateKeywordsForTrustProfile(command)
        for( String keyword : keywords )
            builder.addKeyword(keyword)

        int tdCount = 0
        int tipCount = 0
        JSONObject json = new JSONObject(command.referenceJson)
        if( json.optJSONArray("references") != null ){
            JSONArray references = json.optJSONArray("references")
            for( int i = 0; i < references.length(); i++ ){
                JSONObject ref = references.optJSONObject(i)
                if( ref.optString("Type", "").equalsIgnoreCase("TrustmarkDefinition") ){
                    tdCount++
                    String refId = "TD_"+tdCount

                    List<Provider> providers = []
                    JSONArray providersJsonList = ref.optJSONArray("Providers")
                    if( providersJsonList?.size() > 0 ){
                        for( int j = 0; j < providersJsonList.length(); j++ ){
                            JSONObject jsonProviderObj = providersJsonList.optJSONObject(j)
                            Provider provider = Provider.findByUri(jsonProviderObj.optString("Identifier"))
                            providers.add(provider)
                        }
                    }

                    builder.addTrustmarkDefinitionRequirement(refId,
                            ref.optString("Identifier", ""),
                            ref.optString("Name", ""),
                            ref.optString("Version", ""),
                            ref.optString("Description", ""),
                            buildEntitiesArray(providers))
                }else{ // Must be a tip then.
                    tipCount++
                    String refId = "TIP_"+tipCount
                    builder.addTrustInteroperabilityProfileReference(refId,
                            ref.optString("Identifier", ""),
                            ref.optString("Name", ""),
                            ref.optString("Version", ""),
                            ref.optString("Description", ""))
                }
            }
        }

        builder.setTrustExpression(buildTrustExpression(tdCount, tipCount))

        return builder.build()
    }

    private String buildTrustExpression(int tdCount, int tipCount) {
        StringBuilder trustExpression = new StringBuilder()
        for( int i = 0; i < tdCount; i++ ){
            trustExpression.append("TD_"+(i+1))
            if( i < (tdCount - 1) )
                trustExpression.append(" AND ")
        }
        if( tdCount > 0 && tipCount > 0 )
            trustExpression.append(" AND ")
        for( int i = 0; i < tipCount; i++ ){
            trustExpression.append("TIP_"+(i+1))
            if( i < (tipCount - 1) )
                trustExpression.append(" AND ")
        }
        return trustExpression.toString()
    }

    protected Entity[] buildEntitiesArray(List<Provider> providers){
        Entity[] entities = new Entity[providers.size()]
        for(int i = 0; i < providers.size(); i++ ){
            Entity e = new DefaultEntityImpl(providers.get(i))
            entities[i] = e
        }
        return entities
    }

    protected edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition buildSimpleTd(SimpleTrustmarkDefinitionCommand command, User user){
        String tdId = cleanseMoniker(generateMoniker(command.name))
        String id = buildId(command.baseUrl, tdId,"1.0")

        TrustmarkDefinitionBuilder builder = FactoryLoader.getInstance(BuilderFactory.class).createTrustmarkDefinitionBuilder()
        builder.setIdentifier(id)
        builder.setTrustmarkReferenceAttributeName(id+"/reference")
        builder.setName(command.name)
        builder.setDescription(command.description)
        builder.setTrustmarkDefiningOrganization(TFAMPropertiesHolder.getDefaultEntity())
        builder.setDeprecated(false)
        builder.setPublicationDateTime(new Date())
        builder.setVersion("1.0")
        builder.setIssuanceCriteria("yes(all)")
        builder.setNotes("Generated by the Trustmark Policy Authoring Tool (TPAT) by "+user.username)
        List<String> keywords = autoGenerateKeywordsForTrustmarkDefinition(command)
        for( String keyword : keywords )
            builder.addKeyword(keyword)

        String sourceId = null
        if( StringUtils.isNotBlank(command.source) ) {
            sourceId = buildRandomId()
            builder.addSource(sourceId, command.source)
        }

        String criteriaName = null
        if( StringUtils.isNotBlank(command.conformanceCriteria) ) {
            criteriaName = "Criterion 1"
            builder.addCriterion(criteriaName, command.conformanceCriteria)
            if (sourceId != null) {
                ConformanceCriterion crit1 = builder.getCriterionByName(criteriaName)
                Source s1 = builder.getSourceByIdentifier(sourceId)
                builder.addCitation(crit1, s1, "")
            }
        }

        String assStepName = "Assessment Step 1"
        builder.addAssessmentStep(assStepName, command.assessmentStep)
        if( StringUtils.isNotBlank(criteriaName) ){
            AssessmentStep assStep = builder.getAssessmentStepByName(assStepName)
            ConformanceCriterion crit1 = builder.getCriterionByName(criteriaName)
            builder.addCriterionLink(assStep, crit1)
        }

        return builder.build()
    }

    /**
     * Generates a moniker from the given name.  Monikers are "URL Safe" representations of the name.
     */
    public static String generateMoniker(String name) {
        String moniker = name
        moniker = moniker.toLowerCase()
        moniker = moniker.replaceAll("\\s+", "-") // Replace any whitespace with a single dash
        return moniker
    }//end generateMoniker()

    /**
     * Given something that someone thinks is a valid moniker, this function replaces it with something that actually is.
     */
    public static String cleanseMoniker(String moniker) {
        String newMoniker = StringUtils.isNotBlank(moniker) ? moniker : ""
        newMoniker = newMoniker.toLowerCase() // Make sure it's consistent accross the board.
        StringBuilder monikerBuilder = new StringBuilder()
        for( int i = 0; i < newMoniker.length(); i++ ){
            if( isValidMonikerCharacter(newMoniker.charAt(i)) ){
                monikerBuilder.append(newMoniker.charAt(i))
            }else{
                monikerBuilder.append("_") // Instead of removing the character, we underscore it.
            }
        }
        return monikerBuilder.toString()
    }//end cleanseMoniker()

    /**
     * Returns true if you can use this character in a moniker, and false if you cannot.
     */
    public static boolean isValidMonikerCharacter(Character c){
        boolean canUse = false
        if( c?.toString().matches("[a-zA-Z0-9]") ){
            canUse = true
        }else if( c == '.' || c == '-' || c == '_' ){
            canUse = true
        }
        return canUse
    }


    private Map getPrimaryContactJSON(Contact c, User user){
        if( c == null ){
            return [
                    responder: user.username,
                    email: user.username
            ]
        }

        def json = [Kind: ContactKindCode.PRIMARY.toString()]

        if( StringUtils.isNotBlank(c.getResponder()) )
            json.put("Responder", c.getResponder())

        if( StringUtils.isNotBlank(c.getDefaultEmail()) )
            json.put("Email", c.getDefaultEmail())

        if( StringUtils.isNotBlank(c.getDefaultTelephone()) )
            json.put("Telephone", c.getDefaultTelephone())

        if( StringUtils.isNotBlank(c.getDefaultMailingAddress()) )
            json.put("MailingAddress", c.getDefaultMailingAddress())

        if( StringUtils.isNotBlank(c.getDefaultPhysicalAddress()) )
            json.put("PhysicalAddress", c.getDefaultPhysicalAddress())

        if( StringUtils.isNotBlank(c.getNotes()) )
            json.put("Notes", c.getNotes())

        return json
    }

    private String getOrgName() {
        return  "SAFECOM"
    }

    private String getOrgId() {
        return "https://safecom.gtri.gatech.edu/"
    }

    private String buildId(String baseUrl, String moniker, String version){
        return buildId(baseUrl, "trustmark-definitions", moniker, version)
    }
    private String buildId(String baseUrl, String type, String moniker, String version){
        if( baseUrl.endsWith("/") )
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1)
        return "${baseUrl}/${type}/${moniker}/${version}/"
    }

    private String buildRandomId() {
        String id = UUID.randomUUID().toString().toUpperCase()
        id = id.replaceAll(Pattern.quote("-"), "")
        return id
    }

    private List<String> _autoGenerateKeywords(String... values){
        List<String> keywords = []
        List<String> allTheWords = []
        for( String str : values ?: []){
            allTheWords.addAll(keywordsFromFieldValue(str))
        }
        for( String word : allTheWords ){
            String nextWord = word?.trim()
            if( StringUtils.isNotEmpty(nextWord) ){
                if( !keywords.contains(nextWord) ){
                    if( isCommon(nextWord) ){
                        if( TaxonomyTerm.findByNameIlike(nextWord) != null ){
                            keywords.add(nextWord) // We add it because although it is common, it is a taxonomy term.
                        }
                    }else{
                        keywords.add(nextWord) // We add it because it is an uncommon word in the english language.
                    }
                }
            }
        }
        return keywords
    }


    private List<String> autoGenerateKeywordsForTrustProfile(SimpleTrustProfileCommand command) {
        return _autoGenerateKeywords(
                command.name,
                command.description
        )
    }


    private List<String> autoGenerateKeywordsForTrustmarkDefinition(SimpleTrustmarkDefinitionCommand command) {
        return _autoGenerateKeywords(
                command.name,
                command.source,
                command.description,
                command.conformanceCriteria,
                command.assessmentStep
        )
    }

    private List<String> keywordsFromFieldValue(String value){
        List<String> keywords = []
        if( StringUtils.isNotBlank(value) ){
            String normalizedValue = normalizeFieldValue(value)
            String[] tokens = normalizedValue.split(" ")
            if( tokens != null && tokens.length > 0 ){
                for( int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++ ){
                    String token = tokens[tokenIndex]?.trim()
                    if( StringUtils.isNotBlank(token) ) {
                        keywords.add(token)
                        addParentTerms(token, keywords)

                        if( tokenIndex < (tokens.length - 2) ){
                            String nextToken = tokens[tokenIndex+1]?.trim()
                            String twoWordToken = token + ' ' + nextToken
                            addParentTerms(twoWordToken, keywords)
                        }
                        if( tokenIndex < (tokens.length - 3) ){
                            String nextToken = tokens[tokenIndex+1]?.trim()
                            String nextNextToken = tokens[tokenIndex+2]?.trim()
                            String threeWordToken = token + ' ' + nextToken + ' ' + nextNextToken
                            addParentTerms(threeWordToken, keywords)
                        }

                    }
                }
            }
        }
        return keywords
    }

    private void addParentTerms(String token, List<String> keywords){
        TaxonomyTerm matchingTaxonomyTerm = TaxonomyTerm.findByNameIlike(token)
        if( matchingTaxonomyTerm != null ){
            log.debug("Found matching term: "+token)
            TaxonomyTerm parent = matchingTaxonomyTerm.getParent()
            while( parent != null ){
                log.debug("  Adding parent @|cyan ${parent.name}|@...")
                keywords.add(parent.name)
                parent = parent.getParent()
            }
        }
    }

    private String normalizeFieldValue(String incoming){
        String value = incoming
        if( StringUtils.isNotBlank(value) ){
            value = value.trim().replaceAll("[^A-Za-z0-9-_]", " ") // Clear punctuation and most special characters.
            value = value.replaceAll("\\s+", " ") // Normalize space in this value
        }else{
            value = ""
        }
        return value
    }


    /**
     * Responsible for returning whether or not this word is very common in the english language - a useful thing for
     * stuff like automatically derived keywords (which are by definition, not very common).
     */
    static List<String> COMMON_WORDS = null
    protected boolean isCommon(String word){
        if( COMMON_WORDS == null ) {
            String commonWordsPath = request.getSession().getServletContext().getRealPath("/WEB-INF/config/common_words.text")
            log.info("Common words[@|green ${commonWordsPath}|@] not yet cached, reading into memory...")
            File commonWordsFile = new File(commonWordsPath)
            if( commonWordsFile.exists() ){
                COMMON_WORDS = []
                String commonWordsText = commonWordsFile.text
                String[] words = commonWordsText.split("\\s+")
                for( String commonWord : words ?: [] ){
                    COMMON_WORDS.add(commonWord?.trim()?.toLowerCase())
                }
                log.info("Successfully read @|green ${COMMON_WORDS.size()}|@ common words.")
            }else{
                log.warn("File not found: @|yellow ${commonWordsPath}|@")
                return false; // Default to saying it is NOT common if we can't resolve the file.
            }
        }
        return COMMON_WORDS.contains(word.toLowerCase())
    }

}//end index()


class SimpleTrustProfileCommand {
    String id
    String tipId
    String baseUrl
    String name
    String description
    String referenceJson

    static constraints = {
        id(nullable: false, blank: false, maxSize: 1024)
        name(nullable: false, blank: false, maxSize: 1024, validator: {val, obj, errors ->
            VersionSet.withTransaction {
                VersionSet vs = VersionSet.findByName(obj.id)
                List<VersionSetTIPLink> linkList = VersionSetTIPLink.executeQuery("from VersionSetTIPLink where versionSet = :vs and trustInteroperabilityProfile.name = :val and trustInteroperabilityProfile.tipVersion = :ver",
                        [vs: vs, val: val, ver: '1.0'])
                if( StringUtils.isBlank(obj.tipId) && linkList.size() > 0 ){
                    errors.rejectValue("name", "tip.name.already.taken", [val] as Object[], "A Trust Profile with name ${val} already exists.  Please choose another.")
                }else if( StringUtils.isNotBlank(obj.tipId) ){
                    boolean same = true
                    for( VersionSetTIPLink cur : linkList ){
                        if( !obj.tipId.equalsIgnoreCase(cur.trustInteroperabilityProfile.id.toString()) ){
                            println("ERROR: ID[${cur.trustInteroperabilityProfile.id}] not equal Given[${obj.tipId}]")
                            same = false
                            break
                        }
                    }
                    if( !same )
                        errors.rejectValue("name", "tip.name.already.taken", [val] as Object[], "A Trust Profile with name ${val} already exists.  Please choose another.")
                }
            }
        })
        tipId(nullable: true, blank: true, maxSize: 1024)
        baseUrl(nullable: false, blank: false, maxSize: 65535)
        description(nullable: false, blank: false, maxSize: 65535)
        referenceJson(nullable: true, blank: true, maxSize: 65535)
    }
}

class SimpleTrustmarkDefinitionCommand {
    String id
    String baseUrl
    String name
    String description
    String source
    String conformanceCriteria
    String assessmentStep

    static constraints = {
        name(nullable: false, blank: false, maxSize: 1024, validator: {val, obj, errors ->
            VersionSet.withTransaction {
                VersionSet vs = VersionSet.findByName(obj.id)
                List<VersionSetTDLink> linkList = VersionSetTDLink.executeQuery("from VersionSetTDLink where versionSet = :vs and trustmarkDefinition.name = :val and trustmarkDefinition.tdVersion = :ver",
                        [vs: vs, val: val, ver: '1.0'])
                if( linkList.size() > 0 ){
                    errors.rejectValue("name", "td.name.already.taken", [val] as Object[], "A Trustmark Definition with name ${val} already exists.  Please choose another.")
                }
            }
        })
        baseUrl(nullable: false, blank: false, maxSize: 65535)
        source(nullable: true, blank: true, maxSize: 65535)
        description(nullable: false, blank: false, maxSize: 65535)
        conformanceCriteria(nullable: true, blank: true, maxSize: 65535)
        assessmentStep(nullable: false, blank: false, maxSize: 65535)
    }
}
