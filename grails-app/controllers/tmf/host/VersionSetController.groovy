package tmf.host


import grails.converters.JSON
import grails.converters.XML
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonOutput
import org.apache.commons.lang.StringUtils

import javax.servlet.ServletException
import java.text.SimpleDateFormat

/**
 * TODO: Insert Comment Here
 * <br/><br/>
 * @author brad
 * @date 11/21/16
 */
@Secured("ROLE_ORG_ADMIN")
@Transactional
class VersionSetController extends AbstractVersionSetController {

    FileService fileService
    SpringSecurityService springSecurityService
    CreateVersionSetService createVersionSetService
    RebuildReferencesService rebuildReferencesService
    TipKeywordsService tipKeywordsService

    def index() {
        log.debug("Listing version sets...")
        params.max = Math.min(params.max ?: 10, 100)
        params.sort = params.sort ?: 'dateCreated'
        params.order = params.order ?: 'desc'

        Integer count = VersionSet.count()
        boolean canCreateVersionSet = false
        if( count == 0 ){
            canCreateVersionSet = true
        }else{
            VersionSet latest = resolveLatestVersionSet()
            if( latest.isProduction() ){
                canCreateVersionSet = true
            }else{
                log.warn("Cannot create a version set because the latest[@|green ${latest.name}|@] is not production.")
            }
        }

        List<VersionSet> versionSets = VersionSet.list(params)

        log.debug("Displaying @|cyan ${count}|@ version sets (versionSets.size() = ${versionSets.size()})...")
        [versionSets: versionSets, versionSetCount: count, canCreateVersionSet: canCreateVersionSet]
    }

    /**
     * Displays a page for the given VersionSet listed in "id" parameter.
     */
    def show() {
        log.debug("Request to display VersionSet: @|cyan ${params.id}|@")

        VersionSet vs = resolveVersionSet(params.id)
        User user = springSecurityService.currentUser

        String vsName = session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE)
        if( !vsName ){
            session.setAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE, vs.name)
        }

        if( vs.createdSuccessfully ){
            List<VersionSetLogEntry> logEntries = VersionSetLogEntry.createCriteria().list(max: 10) {
                eq("versionSet", vs)
                order("counter", "desc")
            } ?: []

            List<VersionSetTIPLink> primaryTIPLinks = VersionSetTIPLink.findAllByVersionSetAndPrimaryTIP(vs, true)
            Collections.sort(primaryTIPLinks, { VersionSetTIPLink link1, VersionSetTIPLink link2 ->
                return link1.getTrustInteroperabilityProfile().getName().compareToIgnoreCase(link2.getTrustInteroperabilityProfile().getName())
            } as Comparator)

            [versionSet: vs, logEntries: logEntries, user: user, primaryTIPLinks: primaryTIPLinks]
        }else{
            log.warn("Cannot show VersionSet[${vs.name}] since it is not finished creating.  Showing the showCreating page...")
            redirect(action: 'showCreating', id: vs.name)
        }
    }//end show()

    /**
     * Displays a page which shows the status of creating the current version set.
     */
    def showCreating() {
        log.debug("Showing the creating page for VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        User user = springSecurityService.currentUser
        [versionSet: vs, user: user]
    }

    /**
     * Returns JSON for the current status of creating the version set.
     * @return
     */
    def createVersionSetStatus() {
        log.debug("Showing the status for creating VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)

        String executingVar = SystemVariable.quickFindPropertyValue(CreateVersionSetService.EXECUTING_VAR)?.trim()
        if( StringUtils.isBlank(executingVar) || (!executingVar.equalsIgnoreCase("true") && !executingVar.equalsIgnoreCase("false") ) ){
            executingVar = "false"
        }
        String percentageString = SystemVariable.quickFindPropertyValue(CreateVersionSetService.PERCENTAGE_VAR)?.trim()
        if( StringUtils.isBlank(percentageString) )
            percentageString = "0"

        Map response = [
                executing: Boolean.parseBoolean(executingVar),
                status: SystemVariable.quickFindPropertyValue(CreateVersionSetService.STATUS_VAR),
                message: SystemVariable.quickFindPropertyValue(CreateVersionSetService.MESSAGE_VAR),
                percentage: Integer.parseInt(percentageString)
        ]
        render response as JSON
    }

    /**
     * Unlocks a version set.  Only the user who owns the lock can perform this operation, unless the force parameter
     * is set and the user can perform that force (ie, admins only?)
     */
    def unlock() {
        log.debug("Request to forcibly unlock VersionSet: @|cyan ${params.id}|@")
        VersionSet vs = resolveVersionSet(params.id)
        User user = springSecurityService.currentUser
        if( vs.lockedBy == null )
            throw new ServletException("Can't unlock, this version set is not locked.")

        boolean forced = false
        if( !vs.lockedBy.username.equalsIgnoreCase(user.username) ){
            if( params.boolean("force") ){
                // TODO Check to make sure user can do this...
                forced = true
            }else{
                log.error("User[${user.username}] does not own the lock for vs[${vs.name}], cannot unlock.")
                throw new ServletException("Error - you don't own the lock!")
            }
        }

        vs.lockedBy = null
        vs.lockedDate = null
        vs.save(failOnError: true)

        if( !forced ) {
            VersionSetLogEntry.create(vs.id, "UNLOCKED", "User[${user.username}] has unlocked version set ${vs.name}...", [user: user.username])
        }else{
            VersionSetLogEntry.create(vs.id, "FORCIBLY_UNLOCKED", "User[${user.username}] has forcibly unlocked version set ${vs.name}...", [user: user.username])
        }
        redirect(action: 'show', id: vs.name)
    }

    /**
     * called from the Wipe All Content action, creates 2 empty version sets, one for development and one for production
     * @return
     */
    def removeProduction()  {
        log.info("Request to remove Development VersionSet ${params.id} and reset production by ${springSecurityService.currentUser}")

        VersionSet vs = VersionSet.findByProduction(true);

        VersionSet emptyProductionVs = saveEmptyVersionSet(vs, true)

        VersionSet vsDev = resolveVersionSet(params.id)

        VersionSet emptyVs = saveEmptyVersionSet(vsDev, true)

        session.setAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE, emptyVs.name)

        VersionSetLogEntry.create(vs.id, "CLEARED_PRODUCTION",
                "Cleared version set ${vs.name} and Production", [name: vs.name, userid: springSecurityService.currentUser.username])

        flash.message = "Successfully wiped Production and Development."
        redirect(controller: 'versionSetEdit', action: 'index', id: emptyVs.name)
    }

    /**
     * reset the development branch to the contents of the production branch
     * @return
     */
    def resetDevelopment()  {
        log.info("Request to remove VersionSet ${params.id} by ${springSecurityService.currentUser}")

        User user = springSecurityService.currentUser

        if( !createVersionSetService.setExecuting() ){
            log.error("Cannot create version set ${params.id}, because one is already being created!")
            throw new ServletException("Cannot create new version set because one is already executing!")
        }

        // We have the lock, so time to create.
        VersionSet vs = resolveVersionSet(params.id)
        VersionSet devVs = saveEmptyVersionSet(vs, false)
        final String userName = user.username
        final String fName = devVs.name
        final Long fVersionSetId = devVs.id

        VersionSet prodVS = VersionSet.findByProduction(true)
        if(prodVS != null) {
            final String fPreviousName = prodVS.name
            devVs.predecessor = prodVS
            devVs.save(failOnError: true)

            log.debug("About to thread ${fVersionSetId} ${devVs.name} ${fPreviousName} ")
            Thread createThread = new Thread(new Runnable() {
                @Override
                void run() {
                    createVersionSetService.createVersionSet(fVersionSetId, fName, fPreviousName, userName)
                }
            })
            createThread.setName("CreateVersionSetThread_" + System.currentTimeMillis())
            createThread.start()
        }

        session.setAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE, devVs.name)

        VersionSetLogEntry.create(vs.id, "RESET_TO_PRODUCTION",
                "Version set ${vs.name} reset to Production", [name: vs.name, userid: springSecurityService.currentUser.username])

        flash.message = "Successfully reset Development."
        redirect(controller: 'versionSetEdit', action: 'index', id: devVs.name)
    }

    /**
     * When executed, moves the given version set (based on id) into production.  Any predecessor will be deprecated
     * and no longer be production.
     */
    def moveToProduction() {
        log.info("Request to move VersionSet ${params.id} to production by ${springSecurityService.currentUser}")

        VersionSet vs = resolveVersionSet(params.id)
        boolean prodPredecessor = false

        int tdCount = VersionSetTDLink.countByVersionSet(vs)
        int tipCount = VersionSetTIPLink.countByVersionSet(vs)

        if( tdCount == 0 && tipCount == 0 )  {
            flash.error = "You can't move this version set to production because it contains no TDs or TIPs.  Please add some and try again."
            return redirect(action: 'show', id: vs.name)
        }

        VersionSet.withTransaction  {
            if( vs.predecessor != null)  {
                VersionSet predecessor = vs.predecessor
                prodPredecessor = predecessor.production
                predecessor.development = false
                predecessor.production = false
                predecessor.save(failOnError: true)

                VersionSetLogEntry.create(predecessor.id, "MARKED_LEGACY",
                        "Marked version set ${predecessor.name} as Legacy (no longer production)",
                        [name: predecessor.name, userid: springSecurityService.currentUser.username])
            }

            if(!prodPredecessor)  {     // the development set was not immediately preceded by the production set
                VersionSet productionVS = VersionSet.findByProduction(true)   // is there a production set, mark it non production
                if(productionVS != null)  {
                    productionVS.production = false
                    productionVS.lockedDate = Calendar.getInstance().getTime()
                    productionVS.lockedBy = springSecurityService.currentUser
                    productionVS.save(failOnError: true)
                }
            }

            vs.production = true
            vs.development = false
            vs.editable = false
            vs.releasedBy = springSecurityService.currentUser
            vs.releasedDate = Calendar.getInstance().getTime()
            vs.save(failOnError: true)
        }

        VersionSetLogEntry.create(vs.id, "MARKED_PRODUCTION",
                "Marked version set ${vs.name} as Production", [name: vs.name, userid: springSecurityService.currentUser.username])

        flash.message = "Successfully published to production."

        copyVS(vs.name)   // make a new development set
    }

    /**
     * Calls to this action will result in a new version set form being displayed.
     */
    def create(CreateVersionSetCommand cmd) {
        log.debug("Displaying new version set form...")

        [command: new CreateVersionSetCommand(name: generateNextVsName())]
    }

    /**
     * This action is called from the form shown on the create page.
     */
    def save() {
        User user = springSecurityService.currentUser

        CreateVersionSetCommand command = new CreateVersionSetCommand(name: generateNextVsName())

        log.debug("Saving new VersionSet... ${command.name}")
//        if( command.hasErrors() ){
//            log.warn("Encountered errors[${command.errors?.allErrors?.size()}] with CreateVersionSetCommand:")
//            for(ObjectError oe : command.errors.allErrors ){
//                log.warn("  "+oe)
//            }
//            return render(view: 'create', model: [command: command])
//        }

        if( !createVersionSetService.setExecuting() ){
            log.error("Cannot create version set ${command.name}, because one is already being created!")
            throw new ServletException("Cannot create new version set because one is already executing!")
        }

        // We have the lock, so time to create.
        final String fName = command.name // TODO Other fields?
        VersionSet predecessor = resolveLatestVersionSet()
        String fPreviousName = predecessor?.name
        final String fUsername = user.username

        if(predecessor != null)  {
            predecessor.development = false
            predecessor.editable = false
            predecessor.save(failOnError: true)
        }

        VersionSet vs = new VersionSet(name: command.name)
        VersionSet.withTransaction  {
            vs.createdBy = user
            vs.editable = true
            vs.development = true
            vs.production = false
            vs.predecessor = predecessor
            vs.save(failOnError: true)
        }
        Long fVersionSetId = vs.id

        log.debug("About to thread ${fVersionSetId} ${fName} ${fPreviousName} ")
        Thread createThread = new Thread(new Runnable() {
            @Override
            void run() {
                createVersionSetService.createVersionSet(fVersionSetId, fName, fPreviousName, fUsername)
            }
        })
        createThread.setName("CreateVersionSetThread_"+System.currentTimeMillis())
        createThread.start()

        redirect(controller: 'versionSetEdit', action: 'index', id: fName)
    }

    /**
     * create a new version set and copy tds and tips over from the passed in version set.
     */
    def copyVS(String vsName) {
        User user = springSecurityService.currentUser

        CreateVersionSetCommand command = new CreateVersionSetCommand(name: generateNextVsName())

        log.debug("Saving new VersionSet... ${command.name}")

        if( !createVersionSetService.setExecuting() ){
            log.error("Cannot create version set ${command.name}, because one is already being created!")
            throw new ServletException("Cannot create new version set because one is already executing!")
        }

        // We have the lock, so time to create.
        final String fName = command.name // TODO Other fields?
        VersionSet predecessor = resolveVersionSet(vsName)
        final String fPreviousName = predecessor?.name
        final String fUsername = user.username

        if(predecessor != null)  {
            predecessor.development = false
            predecessor.editable = false
            predecessor.save(failOnError: true)
        }

        VersionSet vs = new VersionSet(name: command.name)
        VersionSet.withTransaction  {
            vs.createdBy = user
            vs.editable = true
            vs.development = true
            vs.production = false
            vs.predecessor = predecessor
            vs.save(failOnError: true)
        }
        Long fVersionSetId = vs.id

        log.debug("About to thread ${fVersionSetId} ${fName} ${fPreviousName} ")
        Thread createThread = new Thread(new Runnable() {
            @Override
            void run() {
                createVersionSetService.createVersionSet(fVersionSetId, fName, fPreviousName, fUsername)
            }
        })
        createThread.setName("CreateVersionSetThread_"+System.currentTimeMillis())
        createThread.start()

        redirect(controller: 'versionSetEdit', action: 'index', id: fName)
    }

    /**
     * This action is called after moving a version set to production, to create an empty development set
     *  or wiping a production and development set and creating 2 empty repos
     */
    def saveEmptyVersionSet(VersionSet predecessor, boolean noCopyTdsAndTips) {
        log.debug("saveEmptyVersionSet...")
        User user = springSecurityService.currentUser

        boolean productionVS = predecessor.production;

        CreateVersionSetCommand command = new CreateVersionSetCommand(name: generateNextVsName())

        log.debug("saveEmptyVersionSet... new name "+ command.name)

        VersionSet.withTransaction {
            if(productionVS)  {
                predecessor.production = false
            }
            predecessor.development = false
            predecessor.editable = false
            predecessor.save(failOnError: true)

            VersionSet vs = new VersionSet(name: command.name)
            vs.createdBy = user
            if(productionVS)  {
                vs.editable = false
                vs.development = false
                vs.production = true
            } else {
                vs.editable = true
                vs.development = true
                vs.production = false
            }
            vs.predecessor = predecessor
            vs.createdSuccessfully = noCopyTdsAndTips;  // true = no need to copy tds and tips
            vs.save(failOnError: true)

            return vs
        }
    }

    /**
     * Returns the trustmark definitions assigned to the version set as HTML, SJON, etc.
     * @return
     */
    def trustmarkDefinitions() {
        User user = springSecurityService.currentUser
        VersionSet vs = resolveVersionSet(params.id)
        log.info("Request to view TrustmarkDefinitions for VersionSet @|green ${vs.name}|@ to production by @|cyan ${user.username}|@...")

        int offset = params.int("offset") ?: 0
        int max = params.int("max") ?: 10
        Long count = VersionSetTDLink.countByVersionSet(vs)

        log.debug("Selecting ${offset}-${offset+max} TDs for VS[${vs.name}]...")
        List<VersionSetTDLink> links = VersionSetTDLink.findAllByVersionSet(vs, [max: max, offset: offset, sort: 'trustmarkDefinition.name', order: 'asc'])
        List<TrustmarkDefinition> tds = []
        for( VersionSetTDLink link : links ){
            tds.add(link.trustmarkDefinition)
        }

        log.debug("Successfully selected @|cyan ${tds.size()}|@ trustmark definitions.")

        Map response = [total: count, max: max, offset: offset, tds: []]
        for( TrustmarkDefinition td : tds ){
            response.tds.add([
                    identifier: td.identifier,
                    name: td.name,
                    version: td.tdVersion,
                    description: td.description,
                    deprecated: td.deprecated,
                    publicationDateTime: td.publicationDateTime.toString()
            ])
        }

        withFormat {
            html {
                throw new UnsupportedOperationException("Not yet implemented.")
            }
            json {
                render response as JSON
            }
            xml {
                render response as XML
            }
        }

    }

    def keywords() {
        User user = springSecurityService.currentUser
        VersionSet vs = resolveVersionSet(params.id)
        log.info("Request to view Keywords for VersionSet @|green ${vs.name}|@ by @|cyan ${user.username}|@...")

        List<KeywordTIPLink> tipLinks = KeywordTIPLink.findAllByVersionSet(vs)
        List<KeywordTDLink> tdLinks = KeywordTDLink.findAllByVersionSet(vs)

        List<String> keywordLowercaseList = []
        List<Keyword> uniqueKeywords = []
        for( KeywordTIPLink tipLink : tipLinks ?: [] ){
            if( !keywordLowercaseList.contains(tipLink.getKeyword().getName().toLowerCase()) ){
                keywordLowercaseList.add(tipLink.getKeyword().getName().toLowerCase())
                uniqueKeywords.add(tipLink.getKeyword())
            }
        }
        for( KeywordTDLink tdLink : tdLinks ?: [] ){
            if( !keywordLowercaseList.contains(tdLink.getKeyword().getName().toLowerCase()) ){
                keywordLowercaseList.add(tdLink.getKeyword().getName().toLowerCase())
                uniqueKeywords.add(tdLink.getKeyword())
            }
        }

        List keywordsList = []
        for( Keyword keyword : uniqueKeywords ){
            Map data = [
                    id: keyword.id,
                    name: keyword.name,
                    tdCount: KeywordTDLink.countByKeyword(keyword),
                    tipCount: KeywordTIPLink.countByKeyword(keyword)
            ]
            keywordsList.add(data)
        }

        Collections.sort(keywordsList, {Map m1, Map m2 ->
            return m2.tdCount.compareTo(m1.tdCount)
        } as Comparator)

        def response = [
                status: "SUCCESS",
                keywordsCount: uniqueKeywords.size(),
                keywords: keywordsList
        ]
        render response as JSON
    }


    /**
     * A method to show the current state of a TD relative to the VersionSet it is on.
     */
    def showTrustmarkDefinition(){
        User user = springSecurityService.currentUser
        VersionSet vs = resolveVersionSet(params.id)
        log.info("Request to show TrustmarkDefinition @|cyan ${params.tdName}|@ v@|cyan ${params.tdVersion}|@ for VersionSet @|green ${vs.name}|@ by @|cyan ${user.username}|@...")

        if( StringUtils.isBlank(params.tdName) || StringUtils.isBlank(params.tdVersion) ){
            log.warn("Parameters tdName[${params.tdName}] or tdVersion[${params.tdVersion}] are either blank or missing.  Both are Required!")
            throw new ServletException("Parameters tdName[${params.tdName}] or tdVersion[${params.tdVersion}] are either blank or missing.  Both are Required!")
        }

        VersionSetTDLink tdLink = VersionSetTDLink.executeQuery(
                "from VersionSetTDLink link where link.versionSet = :vs and link.trustmarkDefinition.name = :name and link.trustmarkDefinition.tdVersion = :version",
                [vs: vs, name: params.tdName, version: params.tdVersion])?.get(0)
        if( tdLink == null ){
            log.warn("Could not find TD ${params.tdName} v${params.tdVersion} on version set ${vs.name}")
            throw new ServletException("No such TD[${params.tdName} v${params.tdVersion}] on version set ${vs.name}")
        }

        redirect(controller: 'trustmarkDefinition', action: 'view', id: tdLink.tdIdentifier, params: params)
    }

    /**
     * Returns the trustInteroperabilityProfiles assigned to the version set as HTML, JSON, etc.
     * @return
     */
    def trustInteroperabilityProfiles() {
        User user = springSecurityService.currentUser
        VersionSet vs = resolveVersionSet(params.id)
        log.info("Request to view trustInteroperabilityProfiles for VersionSet @|green ${vs.name}|@ to production by @|cyan ${user.username}|@...")

        int offset = params.int("offset") ?: 0
        int max = params.int("max") ?: 10
        Long count = VersionSetTIPLink.countByVersionSet(vs)

        log.debug("Selecting ${offset}-${offset+max} TIPs for VS[${vs.name}]...")
        List<VersionSetTIPLink> links = VersionSetTIPLink.findAllByVersionSet(vs, [max: max, offset: offset, sort: 'trustInteroperabilityProfile.name', order: 'asc'])
        List<TrustInteroperabilityProfile> tips = []
        for( VersionSetTIPLink link : links ){
            tips.add(link.trustInteroperabilityProfile)
        }

        log.debug("Successfully selected @|cyan ${tips.size()}|@ trustInteroperabilityProfiles.")

        Map response = [total: count, max: max, offset: offset, tips: []]
        for( TrustInteroperabilityProfile tip : tips ){
            response.tips.add([
                    identifier: tip.identifier,
                    name: tip.name,
                    version: tip.tipVersion,
                    description: tip.description,
                    deprecated: tip.deprecated,
                    publicationDateTime: tip.publicationDateTime.toString()
            ])
        }

        withFormat {
            html {
                throw new UnsupportedOperationException("Not yet implemented.")
            }
            json {
                render response as JSON
            }
            xml {
                render response as XML
            }
        }

    }

    @Secured("ROLE_ADMIN")
    def rebuildReferences() {
        User user = springSecurityService.currentUser
        VersionSet vs = resolveVersionSet(params.id)
        log.info("Request to rebuild references for VersionSet @|green ${vs.name}|@ by @|cyan ${user.username}|@...")

        log.debug("Removing any existing artifact references...")
        ArtifactReference.executeUpdate("delete ArtifactReference where versionSet = :vs", [vs: vs])

        log.debug("Kicking off rebuild thread...")
        final String vsName = vs.name
        Thread t = new Thread(new Runnable() {
            @Override
            void run() {
                rebuildReferencesService.rebuildReferences(vsName)
            }
        })
        t.setName("TFAM_RebuildReferences")
        t.start()

        def response = [status: "SUCCESS", message: "Successfully kicked off rebuild references thread."]
        render response as JSON
    }//end rebuildReferences()

    @Secured("ROLE_ADMIN")
    def checkOnRebuildReferencesProcess() {
        String response = SystemVariable.quickFindPropertyValue(RebuildReferencesService.getStatusVariable())
        if( response == null || StringUtils.isBlank(response) ){
            response = JsonOutput.toJson([status: "FAILURE", message: "There is no status information available", done: true])
        }
        render(contentType: 'application/json', text: response)
    }

    @Secured("ROLE_ADMIN")
    def buildKeywordForTips() {
        User user = springSecurityService.currentUser;
        VersionSet vs = resolveVersionSet(params.id);
        log.info("Request to build keywords for VersionSet @|green ${vs.name}|@ by @|cyan ${user.username}|@...")

        log.debug("Kicking off rebuild thread...");
        final String vsName = vs.name;
        Thread t = new Thread(new Runnable() {
            @Override
            void run() {
                tipKeywordsService.buildKeywords(vsName)
            }
        })
        t.setName("TFAM_BuildKeywords")
        t.start()

        def response = [status: "SUCCESS", message: "Successfully kicked off build keywords thread."]
        render response as JSON
    }//end rebuildReferences()

    @Secured("ROLE_ADMIN")
    def checkOnBuildKeywordForTips() {
        String response = SystemVariable.quickFindPropertyValue(TipKeywordsService.getStatusVariable())
        if( response == null || StringUtils.isBlank(response) ){
            response = JsonOutput.toJson([status: "FAILURE", message: "There is no status information available", done: true])
        }
        render(contentType: 'application/json', text: response)
    }


    /**
     * A method to show the current state of a TIP relative to the VersionSet it is on.
     */
    def showTrustInteroperabilityProfile(){
        User user = springSecurityService.currentUser
        VersionSet vs = resolveVersionSet(params.id)
        log.info("Request to show TIP @|cyan ${params.tipName}|@ v@|cyan ${params.tipVersion}|@ for VersionSet @|green ${vs.name}|@ by @|cyan ${user.username}|@...")

        if( StringUtils.isBlank(params.tipName) || StringUtils.isBlank(params.tipVersion) ){
            log.warn("Parameters tdName[${params.tipName}] or tipVersion[${params.tipVersion}] are either blank or missing.  Both are Required!")
            throw new ServletException("Parameters tipName[${params.tipName}] or tipVersion[${params.tipVersion}] are either blank or missing.  Both are Required!")
        }

        VersionSetTIPLink tipLink = VersionSetTIPLink.executeQuery("from VersionSetTIPLink link where link.versionSet = :vs and link.trustInteroperabilityProfile.name = :name and link.trustInteroperabilityProfile.tipVersion = :version",
                [vs: vs, name: params.tipName, version: params.tipVersion])?.get(0)
        if( tipLink == null ){
            log.warn("Could not find TIP ${params.tipName} v${params.tipVersion} on version set ${vs.name}")
            throw new ServletException("No such TIP[${params.tipName} v${params.tipVersion}] on version set ${vs.name}")
        }


        redirect(controller: 'tip', action: 'view', id: tipLink.tipIdentifier, params: params)
    }


    //==================================================================================================================
    //  HELPER METHODS
    //==================================================================================================================

    /**
     * Generates a name based on the current date.
     */
    private String generateNextVsName() {
        String baseName = "VS_"+(new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()))
        String name = baseName
        int counter = 1
        while( VersionSet.findByName(name) != null ){
            name = baseName + "_" + counter
            counter++
        }
        return name
    }

}/* end VersionSetController */


class CreateVersionSetCommand {
    String name

    static constraints = {
        name(nullable: false, blank: false, maxSize: 64, matches: "[a-zA-Z_][a-zA-Z0-9_\\-]*", validator: {val, obj, errors->
            VersionSet.withTransaction {
                VersionSet fromDb = VersionSet.findByName(val)
                if( fromDb ){
                    errors.rejectValue("name", "createVersionSetCommand.name.already.taken", "That name is already taken.  Please choose another.")
                }
            }
        })
    }
}