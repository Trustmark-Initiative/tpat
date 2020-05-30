package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.TrustInteroperabilityProfileResolver
import edu.gatech.gtri.trustmark.v1_0.io.TrustmarkDefinitionResolver
import edu.gatech.gtri.trustmark.v1_0.io.bulk.BulkReader
import edu.gatech.gtri.trustmark.v1_0.io.bulk.BulkReaderFactory
import edu.gatech.gtri.trustmark.v1_0.io.bulk.BulkReadContext
import edu.gatech.gtri.trustmark.v1_0.io.bulk.BulkReadResult
import edu.gatech.gtri.trustmark.v1_0.io.bulk.BulkReadListener
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkFrameworkIdentifiedObject
import edu.gatech.gtri.trustmark.v1_0.util.TrustInteroperabilityProfileUtils
import edu.gatech.gtri.trustmark.v1_0.util.TrustmarkDefinitionUtils
import edu.gatech.gtri.trustmark.v1_0.util.ValidationResult
import edu.gatech.gtri.trustmark.v1_0.util.ValidationSeverity
import edu.gatech.gtri.trustmark.v1_0.util.diff.DiffSeverity
import edu.gatech.gtri.trustmark.v1_0.util.diff.TrustInteroperabilityProfileDiffResult
import edu.gatech.gtri.trustmark.v1_0.util.diff.TrustmarkDefinitionDiffResult
import groovy.json.JsonOutput
import org.apache.commons.collections.CollectionUtils
import org.springframework.context.MessageSource
import tmf.host.artifact_processing.ActionErrorType
import tmf.host.artifact_processing.ActionType
import tmf.host.artifact_processing.ArtifactAction
import tmf.host.artifact_processing.MemoryProcessingData
import tmf.host.util.*

/**
 * TODO: Insert Comment Here
 * <br/><br/>
 * @author brad
 * @date 11/28/16
 */
class ProcessUploadService extends AbstractLongRunningService {

    static enum ProcessPhase {
        RESOLVE("Reading Artifacts", "The system is parsing any uploaded files into memory."),
        PARSE_RAW_TD("Parse Raw TDs", "Reading all TIPs and making sure enough information is available to build a TD object."),
        PARSE_RAW_TIP("Parse Raw TIPs", "Reading all TIPs and making sure enough information is available to build a TIP object."),
        COLLISION_CHECK("Collision Check", "The system is making sure that there are no conflicts in name or identifier for the uploaded artifacts."),
        PROCESS_TDS("Processing TDs", "The system is performing validation on the Trustmark Definitions uploaded."),
        PROCESS_TIPS("Processing TIPs", "The system is performing validation on the Trust Interoperability Profiles uploaded."),
        COMPLETE("Complete", "Everything has completed successfully.")

        public String displayName
        public String description
        private ProcessPhase(String displayName, String desc) {
            this.displayName = displayName
            this.description = desc
        }
    }

    static transactional = false

    MessageSource messageSource

    /**
     * Entry point for Thread to handle a file upload.  Note that this method is called OUTSIDE of the normal transaction
     * context and needs to utilize the database as such.  All passed objects are detached, and should not be relied upon
     * outside of their identifiers.
     */
    void handleUpload(String username, Long versionSetId, Long uploadId, String originalFilename){
        if( MemoryProcessingData.find(uploadId) != null ) {
            MemoryProcessingData mpd = MemoryProcessingData.find(uploadId)
            if( mpd.alreadyDone(versionSetId, uploadId) ) {
                log.warn("The memory object has already processed this file, so we are skipping a re-process of it.")
                setProcessStatus(uploadId, ProcessPhase.COMPLETE, "Successfully processed file: ${originalFilename}")
                return
            } else {
                log.error("Error - cannot process upload that is already executing but not finished yet!")
                throw new UnsupportedOperationException("Cannot process upload that is already executing and not finished yet!")
            }
        }

        MemoryProcessingData mpd = MemoryProcessingData.create(username, versionSetId, uploadId, originalFilename)

        setProcessStatus(uploadId, ProcessPhase.RESOLVE, "The system is processing upload [${originalFilename}]...")

        // FIXME Starting here, use mpd object in a not-static way and update the feedback to use UploadProcessFeedback instead of the SystemVariable table.

        log.info("User @|green ${username}|@ is processing file @|magenta ${originalFilename}|@ on version set @|yellow ${versionSetId}|@...")
        mpd.reset(username, versionSetId, uploadId, originalFilename)

        Map results = null
        try {
            setProcessStatus(uploadId, ProcessPhase.RESOLVE, "The system is resolving all TD and TIP artifacts from upload [${originalFilename}]...", -1)
            File uploadedFile = null
            String uploadedFileOriginalName = null
            BinaryObject.withTransaction {
                BinaryObject upload = BinaryObject.get(uploadId)
                uploadedFileOriginalName = upload.originalFilename
                uploadedFile = upload.content.toFile()
            }
            results = resolveArtifacts(uploadId, uploadedFile, uploadedFileOriginalName)
        }catch(Throwable error){
            log.error("Error processing file ${originalFilename}", error)
            setErrorStatus(uploadId, error.getMessage(), error)
            return; // Stops processing the file
        }


        mpd.trustmarkDefinitions = results.tds
        int tdCount = mpd.getTdSize()
        mpd.trustInteroperabilityProfiles = results.tips
        int tipCount = mpd.getTipSize()
        mpd.invalidParameters = results.invalidParms

        setProcessStatus(uploadId, ProcessPhase.COLLISION_CHECK, "The system has resolved your ${tdCount} TDs and ${tipCount} TIPs, and they are being checked for any collisions...")

        log.debug("Checking for local collisions...")
        Map collisionData = mpd.getCollisionData(this); // Causes collision data to be calculated.
        if( collisionData.hasCollisions ){
            StringBuilder listData = new StringBuilder()
            log.error("Found collisions(${collisionData.allCollisions.size()}): ")
            for( Map collision : collisionData.allCollisions ?: []){
                log.error("  ${collision.type} Collision(${collision.i}, ${collision.j}) over ${collision.reason}: ${collision.collision}")
                listData.append(
                        String.format("  %s Collision over %s.<br/>\n",
                                collision.type, collision.reason+": "+collision.collision
                        )
                )
            }
            // FIXME Technically, we don't have to error here.  But it makes our lives easier later.
            setErrorStatus(uploadId, "Your set of TDs and TIPs have collisions.  Here is a list: <br/>"+listData.toString())
            return; // Stops processing the file
        }else{
            log.info("No collisions found!")
        }

        log.info("From upload[@|magenta ${originalFilename}|@], successfully resolved @|cyan ${tdCount}|@ TDs and @|cyan ${tipCount}|@ TIPs.")
        setProcessStatus(uploadId, ProcessPhase.PROCESS_TDS, "The system is now processing the ${tdCount} TDs that were uploaded...")

        for( int i = 0; i < tdCount; i++ ){
            edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td = mpd.getTd(i)
            setProcessStatus(uploadId, ProcessPhase.PROCESS_TDS, "Processing Trustmark Definition[${td.getMetadata().getName()}, v.${td.getMetadata().getVersion()}]...", getPercent(i, tdCount))
            processTd(uploadId, td)
        }


        setProcessStatus(uploadId, ProcessPhase.PROCESS_TIPS, "The system is now processing the ${tipCount} TIPs that were uploaded...")
        for( int i = 0; i < tipCount; i++ ){
            edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip = mpd.getTip(i)
            setProcessStatus(uploadId, ProcessPhase.PROCESS_TIPS, "Processing Trust Interoperability Profile[${tip.getName()}, v.${tip.getVersion()}]...", getPercent(i, tipCount))
            processTip(uploadId, tip)
        }

        // NOTE: We aren't complete with the upload action, nothing has been applied yet.
        setProcessStatus(uploadId, ProcessPhase.COMPLETE, "Successfully processed file: ${originalFilename}")
        mpd.processingFinished = true

        VersionSetUpload.withTransaction {
            VersionSet vs = VersionSet.get(versionSetId)
            BinaryObject artifact = BinaryObject.get(uploadId)
            VersionSetUpload upload = VersionSetUpload.findByVersionSetAndArtifact(vs, artifact)
            upload.processed = true
            upload.save(failOnError: true)
        }
    }

    /**
     * Checks the given URL against base URLs to see if it is local.  If it is, then true is returned, if it is not,
     * then false is returned.
     */
    private boolean isLocalUrl(String url){
        for( String baseUrl : TFAMPropertiesHolder.getBaseURLsAsStrings() ){
            if(url.startsWith(baseUrl) ){
                return true
            }
        }
        return false
    }

    /**
     * Given a TD that exists only in memory, and the current versionSet identifier.  Performs some logic to determine the
     * recommended course of action on the given TD WRT the current vesrion set.  Ie, should the TD be added, overwritten,
     * etc.  Also analyzes any side affection actions as well.  For example, if a TD is added, and it supersedes another
     * TD, then a side affect action is that the other TD will be deprecated and a superseded link be put to this TD.
     * Note that this method also performs some sanity checks, such as no TDs having the same identifiers, etc.
     * <br/><br/>
     * @param td
     */
    private void processTd(Long uploadId, edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td){
        log.debug("td[${td.getMetadata().getIdentifier().toString()}] Deprecated: "+td.getMetadata().isDeprecated())
        String id = td.getMetadata().getIdentifier().toString()
        MemoryProcessingData mpd = MemoryProcessingData.find(uploadId)
        log.debug("Processing TD @|green ${id}|@...")

        VersionSetTDLink.withTransaction {
            VersionSet vs = VersionSet.get(mpd.versionSetId)
            if( !vs ) throw new UnsupportedOperationException("Error processing!  VersionSet[${mpd.versionSetId}] could not be found!")

            ArtifactAction action = new ArtifactAction(type: 'TD', id: id, artifact: createSummary(td))
            if( !isLocalUrl(id) ){
                action.actionType = ActionType.ERROR
                action.errorType = ActionErrorType.NO_LOCAL_BASE
                action.errorMessage = "Artifact[${td.getMetadata().getName()}, v${td.getMetadata().getVersion()}, ID=${td.getMetadata().getIdentifier().toString()}] does not have a local base URL and cannot be updated.  You may only update those Identifiers which start with one of your pre-configured Base URLs."
                mpd.add(action)
                return
            }

            TrustmarkDefinitionUtils utils = FactoryLoader.getInstance(TrustmarkDefinitionUtils.class)
            Collection<ValidationResult> validationResults = utils.validate(td)
            if( validationResults.size() > 0 ){
                for( ValidationResult vr : validationResults ){
                    if( vr.severity == ValidationSeverity.FATAL ){
                        action.actionType = ActionType.ERROR
                        action.errorType = ActionErrorType.VALIDATION_ERROR
                        action.errorMessage = "Artifact[${td.getMetadata().getName()}, v${td.getMetadata().getVersion()}] is not valid: "+vr.getMessage()+" [location: "+vr.getLocation()+"]"
                        mpd.add(action)
                        return
                    }
                }
            }

            // TODO Check registry for known collision (with name and identifier)

            VersionSetTDLink tdLink = findMatchingTDLink(vs, td)
            if( tdLink ) { // Basically saying "If it is already in my version set, then..."
                if (areTdsSame(td, tdLink)) { // Nothing changed, so just ignore this upload
                    action.actionType = ActionType.IGNORE

                } else if (tdLink.copyOver && tdHasChangedSignificantly(td, tdLink)) {
                    action.actionType = ActionType.ERROR
                    action.errorType = ActionErrorType.CHANGE_PREVIOUSLY_RELEASED
                    action.errorMessage = "TD[${tdLink.trustmarkDefinition.name}, v${tdLink.trustmarkDefinition.tdVersion}] was previously released and you are trying to significantly change it; this is not allowed."
                    mpd.add(action)
                    return

                } else {
                    action.actionType = ActionType.OVERWRITE
                    action.previousLinkId = tdLink.id
                    // TODO Should we report via the action what has changed?
                }

                // TODO Check to see if there are pre-actions we should take (like removing the previous one) - also might need to roll back previous deprecation.

            }else{ // The name/version don't conflict, so we are going to add the empty TD.
                action.actionType = ActionType.ADD
                if( CollectionUtils.isNotEmpty(td.getMetadata().getSupersedes()) ){
                    for( TrustmarkFrameworkIdentifiedObject tmfi : td.getMetadata().getSupersedes() ){
                        VersionSetLink link = isLocalToVersionSet(vs, tmfi)
                        if( link ){
                            ArtifactAction deprecateAction = createDeprecateAction(vs, link)
                            deprecateAction.artifact.put("SupersededBy", td.getMetadata().getIdentifier().toString())
                            action.postActions.add(deprecateAction)
                        }else{
                            action.warnings.add("Supersedes TD[${tmfi.identifier.toString()}], but it is not local.  Cannot deprecate that TD or mark it as superseded.")
                        }
                    }
                }else if( findTDByName(vs, td.getMetadata().getName()) != null ){
                    log.info("Found matching TD by name '@|cyan ${td.getMetadata().getName()}|@'!")
                    VersionSetTDLink tdToDeprecate = findTDByName(vs, td.getMetadata().getName())
                    TmfiObjImpl tmfi = new TmfiObjImpl(typeName: 'TrustmarkDefinition', identifier: new URI(tdToDeprecate.getTdIdentifier()))
                    td.getMetadata().getSupersedes().add(tmfi)
                    ArtifactAction deprecateAction = createDeprecateAction(vs, tdToDeprecate)
                    deprecateAction.artifact.put("SupersededBy", td.getMetadata().getIdentifier().toString())
                    action.postActions.add(deprecateAction)
                }
            }

            mpd.add(action)

            // TODO Check for any action post conditions (like a deprecation of previous TD)

        }
    }

    /**
     * Analyzes the 2 TDs against each other generating a diff.  Examines the diff to determine if any major changes
     * have occurred.
     */
    private boolean tdHasChangedSignificantly(edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition tdNew, VersionSetTDLink tdLink){
        File td2File = tdLink.getTrustmarkDefinition().getArtifact().getContent().toFile()
        edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition tdOld = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class).resolve(td2File)

        Collection<TrustmarkDefinitionDiffResult> diffResults = FactoryLoader.getInstance(TrustmarkDefinitionUtils.class).diff(tdNew, tdOld)
        if( diffResults.size() > 0 ){
            for( TrustmarkDefinitionDiffResult diffResult : diffResults ){
                if( diffResult.getSeverity() == DiffSeverity.MAJOR ){
                    return true
                }
            }

        }else{
            return false; // It hasn't changed at all...
        }
    }

    private TrustmarkFrameworkIdentifiedObject resolveTmfiForTd(TrustmarkDefinition td){
        File file = td.getArtifact().getContent().toFile()
        edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition fullTd = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class).resolve(file, false)
        return fullTd.getMetadata()
    }


    private TrustmarkFrameworkIdentifiedObject resolveTmfiForTip(TrustInteroperabilityProfile tip){
        File file = tip.getArtifact().getContent().toFile()
        edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile fullTip = FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class).resolve(file, false)
        return fullTip
    }


    private ArtifactAction createDeprecateAction(VersionSet vs, VersionSetLink link){
        String id = null
        String type = null
        TrustmarkFrameworkIdentifiedObject artifact = null
        if( link.isTdLink() ){
            id = ((VersionSetTDLink) link).getTrustmarkDefinition().getIdentifier()
            type = "TD"
            artifact = resolveTmfiForTd(((VersionSetTDLink) link).getTrustmarkDefinition())
        }else{
            id = ((VersionSetTIPLink) link).getTrustInteroperabilityProfile().getIdentifier()
            type = "TIP"
            artifact = resolveTmfiForTip(((VersionSetTIPLink) link).getTrustInteroperabilityProfile())
        }

        ArtifactAction deprecation = new ArtifactAction(id: id, type: type, artifact: createSummary(artifact))
        deprecation.actionType = ActionType.DEPRECATE
        deprecation.uniqueId = -1

        return deprecation
    }

    private VersionSetLink isLocalToVersionSet(VersionSet vs, TrustmarkFrameworkIdentifiedObject tmfi){
        VersionSetLink link = null
        link = VersionSetTDLink.findByVersionSetAndTdIdentifier(vs, tmfi.identifier.toString())
        if( link ) return link

        link = VersionSetTIPLink.findByVersionSetAndTipIdentifier(vs, tmfi.identifier.toString())
        if( link ) return link

        log.warn("Cannot find Artifact[@|yellow ${tmfi.identifier.toString()}|@] on Version Set[@|cyan ${vs.name}|@]!")
        return null
    }

    private boolean areTdsSame(edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td, VersionSetTDLink tdLink ){
        File tdFile = tdLink.trustmarkDefinition.artifact.content.toFile()
        edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition fromDatabaseTd =
                FactoryLoader.getInstance(TrustmarkDefinitionResolver.class).resolve(tdFile, false)

        Collection<TrustmarkDefinitionDiffResult> differences = FactoryLoader.getInstance(TrustmarkDefinitionUtils.class).diff(td, fromDatabaseTd)
        if( differences.isEmpty() ){
            return true
        }else{
            return false
        }
    }

    private Map createSummary(edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td){
        return createSummary(td.getMetadata())
    }
    private Map createSummary(edu.gatech.gtri.trustmark.v1_0.model.TrustmarkFrameworkIdentifiedObject tfi){
        return [
                identifier: tfi.getIdentifier().toString(),
                name: tfi.getName(),
                version: tfi.getVersion(),
                description: tfi.getDescription(),
                type: tfi.getTypeName()
        ]
    }

    private boolean urlAlreadyExistsOnInternet(String urlString){
        try {
            final URL url = new URL(urlString)
            HttpURLConnection huc = (HttpURLConnection) url.openConnection()
            huc.setRequestMethod("HEAD")
            int responseCode = huc.getResponseCode()
            if (responseCode == 200) {
                return true
            } else {
                return false
            }
        }catch(Throwable t){
            return false
        }
    }

    private VersionSetTDLink findTDByName(VersionSet vs, String name){
        List<VersionSetTDLink> links =
                VersionSetTDLink.executeQuery(
                        "from VersionSetTDLink link where link.versionSet = :vs and lower(link.trustmarkDefinition.name) = :name",
                        [vs: vs, name: name.toLowerCase()])
        if( links.isEmpty() )
            return null
        else
            return links.get(0)
    }

    private VersionSetTDLink findMatchingTDLink(VersionSet vs, edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td){
        List<VersionSetTDLink> links =
                VersionSetTDLink.executeQuery("from VersionSetTDLink link where link.versionSet = :vs and ((lower(link.trustmarkDefinition.name) = :name and lower(link.trustmarkDefinition.tdVersion) = :version) or lower(link.tdIdentifier) = :id)",
                    [vs: vs, name: td.getMetadata().getName().toLowerCase(), version: td.getMetadata().getVersion().toLowerCase(),
                    id: td.getMetadata().getIdentifier().toString().toLowerCase()])
        if( links.isEmpty() )
            return null
        else
            return links.get(0)
    }

    private void processTip(Long uploadId, edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip){
        String id = tip.getIdentifier().toString()
        MemoryProcessingData mpd = MemoryProcessingData.find(uploadId)
        log.debug("Processing TIP @|green ${id}|@")

        VersionSetTIPLink.withTransaction {
            VersionSet vs = VersionSet.get(mpd.versionSetId)
            if( !vs ) throw new UnsupportedOperationException("Error processing!  VersionSet[${mpd.versionSetId}] could not be found!")

            ArtifactAction action = new ArtifactAction(type: 'TIP', id: id, artifact: createSummary(tip))

            if( !isLocalUrl(tip.getIdentifier().toString()) ){
                action.actionType = ActionType.ERROR
                action.errorType = ActionErrorType.NO_LOCAL_BASE
                action.errorMessage = "Artifact[${tip.getIdentifier().toString()}] is not hosted at a local URL, and you cannot publish it here."
                mpd.add(action)
                return
            }

            // TODO Check registry for known collision

            VersionSetTIPLink tipLink = findMatchingTIPLink(vs, tip)
            if( tipLink ){
                if( tipLink.copyOver ){
                    action.actionType = ActionType.ERROR
                    action.errorMessage = "You have requested to overwrite a TIP[${tipLink.trustInteroperabilityProfile.name}, v${tipLink.trustInteroperabilityProfile.tipVersion}] which was previously released.  This is not allowed."
                }else{
                    if( areTipsSame(tip, tipLink) ){
                        action.actionType = ActionType.IGNORE
                    }else{
                        action.actionType = ActionType.OVERWRITE
                        action.previousLinkId = tipLink.id
                    }

                    // TODO Check to see if there are pre-actions we should take (like removing the previous one) - also might need to roll back previous deprecation.

                }

            }else{ // The name/version don't conflict, so we are going to add the empty TD.
                action.actionType = ActionType.ADD
                if( CollectionUtils.isNotEmpty(tip.getSupersedes()) ){
                    for( TrustmarkFrameworkIdentifiedObject tmfi : tip.getSupersedes() ){
                        VersionSetLink link = isLocalToVersionSet(vs, tmfi)
                        if( link ){
                            ArtifactAction deprecateAction = createDeprecateAction(vs, link)
                            deprecateAction.artifact.put("SupersededBy", tip.getIdentifier().toString())
                            action.postActions.add(deprecateAction)
                        }else{
                            action.warnings.add("TIP[${tip.getIdentifier()}] Supersedes TIP[${tmfi.identifier.toString()}], but it is not local.  Cannot deprecate that TIP or mark it as superseded.")
                        }
                    }
                }else if( findTIPByName(vs, tip.getName()) != null ){
                    log.info("Found matching TIP by name '@|cyan ${tip.getName()}|@'!")
                    VersionSetTIPLink tipToDeprecate = findTIPByName(vs, tip.getName())
                    TmfiObjImpl tmfi = new TmfiObjImpl(typeName: 'TrustInteroperabilityProfile', identifier: new URI(tipToDeprecate.getTipIdentifier()))
                    tip.getSupersedes().add(tmfi)
                    ArtifactAction deprecateAction = createDeprecateAction(vs, tipToDeprecate)
                    deprecateAction.artifact.put("SupersededBy", tip.getIdentifier().toString())
                    action.postActions.add(deprecateAction)
                }
            }

            mpd.add(action)

        }
    }

    private VersionSetTIPLink findTIPByName(VersionSet vs, String name){
        List<VersionSetTIPLink> links =
                VersionSetTIPLink.executeQuery(
                        "from VersionSetTIPLink link where link.versionSet = :vs and lower(link.trustInteroperabilityProfile.name) = :name",
                        [vs: vs, name: name.toLowerCase()])
        if( links.isEmpty() )
            return null
        else
            return links.get(0)
    }

    private boolean areTipsSame(edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip, VersionSetTIPLink tipLink ){
        File tipFile = tipLink.trustInteroperabilityProfile.artifact.content.toFile()
        edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile fromDatabaseTip =
                FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class).resolve(tipFile, false)

        Collection<TrustInteroperabilityProfileDiffResult> differences = FactoryLoader.getInstance(TrustInteroperabilityProfileUtils.class).diff(tip, fromDatabaseTip)
        if( differences.isEmpty() ){
            return true
        }else{
            log.info("Found ${differences.size()} differences: ")
            for( TrustInteroperabilityProfileDiffResult diff : differences )
                log.info("    ${diff.diffType} {${diff.getLocation()}}: "+diff.getDescription())
            return false
        }
    }


    protected VersionSetTIPLink findMatchingTIPLink(VersionSet vs, edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip){
        List<VersionSetTIPLink> links =
                VersionSetTIPLink.executeQuery("from VersionSetTIPLink link where link.versionSet = :vs and ((lower(link.trustInteroperabilityProfile.name) = :name and lower(link.trustInteroperabilityProfile.tipVersion) = :version) or lower(link.tipIdentifier) = :id)",
                        [vs: vs, name: tip.getName().toLowerCase(), version: tip.getVersion().toLowerCase(),
                         id: tip.getIdentifier().toString().toLowerCase()])
        if( links.isEmpty() )
            return null
        else
            return links.get(0)
    }

    void setProcessStatus(Long uploadId, ProcessPhase phase, String message) {
        setProcessStatus(uploadId, phase, message, -1)
    }
    void setProcessStatus(Long uploadId, ProcessPhase phase, String message, Integer percentComplete) {
        UploadProcessFeedback.withTransaction {
            BinaryObject upload = BinaryObject.get(uploadId)
            UploadProcessFeedback feedback = UploadProcessFeedback.findByUpload(upload)

            feedback.phaseJson = buildPhaseJson(phase)
            feedback.message = message
            feedback.percentage = percentComplete

            feedback.save(failOnError: true)
        }
    }


    private void setErrorStatus(Long uploadId, String message) {
        setErrorStatus(uploadId, message, null)
    }
    private void setErrorStatus(Long uploadId, String message, Throwable t) {
        UploadProcessFeedback.withTransaction {
            BinaryObject upload = BinaryObject.get(uploadId)
            UploadProcessFeedback feedback = UploadProcessFeedback.findByUpload(upload)

            feedback.phaseJson = null; // Clear any phase
            feedback.percentage = -1; // Clear any progress
            feedback.message = message
            feedback.hasError = true
            if( t != null )
                feedback.stacktraceJson = buildStacktraceJson(t)

            feedback.save(failOnError: true)
        }
    }

    String buildPhaseJson(ProcessPhase phase) {
        List phaseJson = []
        if( phase ) {
            for (ProcessPhase cur : ProcessPhase.values()) {
                phaseJson.add([
                        name       : cur.name(),
                        displayName: cur.displayName,
                        description: cur.description,
                        active     : cur == phase,
                        complete   : cur.ordinal() < phase.ordinal()
                ])
            }
        }
        return JsonOutput.toJson(phaseJson)
    }

    /**
     * Responsible for creating a list of TDs and TIPs from the given file upload.  The tds and tips params are modified.
     */
    protected Map resolveArtifacts(Long uploadId, File uploadedFile, String uploadedFileOriginalName){
        Map data = [tds: [], tips: [], invalidParms: []]
        String normalizedName = uploadedFileOriginalName.toLowerCase()
        if( normalizedName.endsWith(".xml") || normalizedName.endsWith(".json") ) {
            log.info("handling xml/json upload...")
            setFileInResultMap(uploadedFileOriginalName, uploadedFile, data)

        } else if( normalizedName.endsWith(".xls") ||  normalizedName.endsWith(".xlsx") ) {
            log.info("Handling XLS (Excel Spreadsheet) upload...")

            BulkReaderFactory bulkReaderFactory = FactoryLoader.getInstance(BulkReaderFactory.class)
            BulkReader bulkReader = bulkReaderFactory.createExcelBulkReader()

            setProcessStatus(uploadId, ProcessPhase.RESOLVE, "Processing Microsoft Excel file ${uploadedFileOriginalName}...")
            BulkReadListenerImpl listenerImpl = new BulkReadListenerImpl(uploadId)
            bulkReader.addListener(listenerImpl)
            BulkReadContext context = new BulkReadContextFromTfamProperties()
            BulkReadResult bulkReadResult = bulkReader.readBulkFrom(context, [uploadedFile])

//            BulkReadResult bulkReadResult = bulkReader.readBulkFrom(bulkReaderFactory.createBulkReadContextFromProperties(TFAMPropertiesHolder.getProperties()), [uploadedFile])

            data.tds.addAll(bulkReadResult.getResultingTrustmarkDefinitions())
            data.tips.addAll(bulkReadResult.getResultingTrustInteroperabilityProfiles())
            data.invalidParms.addAll(bulkReadResult.getResultingInvalidParameters())

            if( listenerImpl.hasError() ) {
                throw listenerImpl.getErrorDuringRead()
            }

        } else if( FileTypeChecker.isArchive(uploadedFile) ) {
            log.info("Handling ZIP upload...")
            File tempDir = ExtractionUtils.extract(uploadedFile)
            List<File> files = ExtractionUtils.collectAllFiles(tempDir)
            BulkReaderFactory bulkReaderFactory = FactoryLoader.getInstance(BulkReaderFactory.class)

            List<File> bulkReadInputFiles = []
            Properties properties = getPropertiesFile(files)
            BulkReader bulkReader = null

            if( hasExcelStructure(files) ){
                log.debug("Performing bulk upload using excel files...")

                bulkReadInputFiles = collectFiles(files, {File f -> return f.getName().toLowerCase().endsWith(".xls");} as FileFilter)
                bulkReadInputFiles.addAll(collectFiles(files, {File f -> return f.getName().toLowerCase().endsWith(".xlsx");} as FileFilter))
                bulkReader = bulkReaderFactory.createExcelBulkReader()

            }else if( hasOnlyXmlJsonFiles(files) ){
                log.debug("Performing bulk upload using XML/JSON files...")
                bulkReadInputFiles = collectFiles(files, {File f -> return !(f.getName().toLowerCase().endsWith(".properties"));} as FileFilter)
                bulkReader = bulkReaderFactory.createXmlJsonBulkReader()

            }else{
                String error = messageSource.getMessage("upload.bad.structure", [uploadedFileOriginalName] as Object[], Locale.getDefault())
                throw new UnsupportedOperationException(error)
            }


            log.debug("Successfully collected @|cyan "+bulkReadInputFiles.size()+"|@ files to process, "+
                    "and properties file "+(properties == null ? '@|red IS NULL|@' : '@|green IS NOT NULL|@'))

            BulkReadContext context = null
            if( properties != null )
                context = bulkReaderFactory.createBulkReadContextFromProperties(properties)
            setProcessStatus(uploadId, ProcessPhase.RESOLVE, "Processing ${files.size()} files from archive ${uploadedFileOriginalName}...")
            BulkReadListenerImpl listenerImpl = new BulkReadListenerImpl(uploadId)
            bulkReader.addListener(listenerImpl)
            BulkReadResult bulkReadResult = bulkReader.readBulkFrom(context, bulkReadInputFiles)

            data.tds.addAll(bulkReadResult.getResultingTrustmarkDefinitions())
            data.tips.addAll(bulkReadResult.getResultingTrustInteroperabilityProfiles())

            if( listenerImpl.hasError() ) {
                throw listenerImpl.getErrorDuringRead()
            }

        } else {
            String error = messageSource.getMessage("error.uploading", [uploadedFileOriginalName] as Object[], Locale.getDefault())
            throw new UnsupportedOperationException(error)
        }
        return data
    }//end resolveArtifacts()

    protected List<File> collectFiles(List<File> files, FileFilter filter){
        List<File> newFiles = new ArrayList<>()
        for( File f : files ){
            if( filter.accept(f) )
                newFiles.add(f)
        }
        return newFiles
    }

    protected Properties getPropertiesFile(List<File> files ) throws IOException {
        List<File> propertiesFiles = collectFiles(files, {File f -> return f.getName().toLowerCase().endsWith(".properties"); } as FileFilter)
        if( propertiesFiles.size() == 0 )
            return null

        Properties props = new Properties()
        for( File f : propertiesFiles ) {
            props.load(new FileReader(f))
        }
        return props
    }

    protected String relativeName(File f, File dir){
        String fname = f.canonicalPath
        String dirName = dir.canonicalPath
        fname = fname.replace(dirName, "")
        if( fname.startsWith(File.separator) )
            fname = fname.substring(1)
        return fname
    }

    protected boolean hasOnlyXmlJsonFiles(List<File> files){
        int propertiesFileCount = 0
        int jsonFileCount = 0
        int xmlFileCount = 0
        for( File f : files ){
            if( f.isFile() ) {
                if( f.getName().toLowerCase().endsWith(".properties") ){
                    propertiesFileCount++
                }else if( f.getName().toLowerCase().endsWith(".xml") ){
                    xmlFileCount++
                }else if( f.getName().toLowerCase().endsWith(".json") ) {
                    jsonFileCount++
                }
            }
        }

        if( propertiesFileCount > 1 ){
            log.warn("A zip file cannot contain more than 1 properties file!  This zip file contains: "+propertiesFileCount)
            return false
        }

        if( jsonFileCount > 0 && xmlFileCount == 0 ){
            return true
        }else if( xmlFileCount > 0 && jsonFileCount == 0 ){
            return true
        }else if( xmlFileCount > 0 && jsonFileCount > 0 ){
            log.warn("Cannot mix JSON and XML files in an archive!")
            return false
        }else{
            log.warn("No XML or JSON files detected.")
            return false
        }
    }

    protected boolean hasExcelStructure(List<File> files){
        boolean containsContextPropertiesFile = false
        for( File f : files ){
            if( f.isFile() ) {
                if (f.getName().toLowerCase().endsWith(".properties")) {
                    if (!containsContextPropertiesFile) {
                        containsContextPropertiesFile = true
                    } else {
                        log.warn("Archive does not have excel structure because of a duplicate properties file found.")
                        return false
                    }
                } else if (!f.getName().toLowerCase().endsWith(".xls") && !f.getName().toLowerCase().endsWith(".xlsx")) {
                    log.warn("Archive does not have excel structure because there is a file @|yellow ${f.getName()}|@ which is neither properties or xls.")
                    return false
                }
            }
        }
        if( !containsContextPropertiesFile ){
            log.warn("Archive does not have excel structure because there is no context properties file found.")
            return false
        }
        return true
    }

    /**
     * Attempts to set the file as a TD or a TIP.  Returns true if that happens, false if the file cannot be parsed.
     */
    protected void setFileInResultMap(String sourceName, File file, Map data){
        log.debug("Calling readObject(${file.canonicalPath} from ${sourceName})")
        Object o = readObject(file)
        log.debug("Past readObject(${file.canonicalPath})")
        if( o == null ){
            log.error("Cannot read file[@|yellow ${sourceName}|@] to a TD or a TIP!")
            throw new UnsupportedOperationException("Cannot read file ${sourceName} to a Trustmark Definition or a Trust Interoperability Profile.")
        }
        log.debug("Checking object type..")
        if( o instanceof edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition ){
            log.debug("Successfully read @|magenta ${sourceName}|@ to a @|green TD|@: @|cyan ${((edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition) o).getMetadata().getIdentifier().toString()}|@")
            data.tds.add(o)
        }else{
            log.debug("Successfully read @|magenta ${sourceName}|@ to a @|green TIP|@: @|cyan ${((edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile) o).getIdentifier().toString()}|@")
            data.tips.add(o)
        }
    }

    /**
     * Responsible for reading the given file to a TrustmarkDefinition or TrustInteroperabilityProfile object.  Note that
     * this method will return null if the file cannot be read.
     */
    private Object readObject(File f){
        TrustmarkDefinitionResolver tdResolver = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class)
        TrustInteroperabilityProfileResolver tipResolver = FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class)

        try {
            return tdResolver.resolve(f, true)
        }catch(Throwable T){
            log.info("NOT a TD because: "+T)
        }

        try {
            return tipResolver.resolve(f, true)
        }catch(Throwable T){
            log.info("NOT a TIP because: "+T)
        }

        return null
    }


    //==================================================================================================================
    //  BulkReadListener methods
    //==================================================================================================================
    class BulkReadListenerImpl implements BulkReadListener {
        BulkReadListenerImpl(Long uploadId){
            this.uploadId = uploadId
        }
        private Long uploadId = null

        private File fileNotSupported = null
        private Throwable errorDuringRead = null
        File getFileNotSupported(){return file;}
        Throwable getErrorDuringRead(){return errorDuringRead;}

        Boolean hasError(){
            return errorDuringRead != null
        }
        Boolean hasFileNotSupported(){
            return fileNotSupported != null
        }

        @Override
        void start() {
            this.lastPhase = ProcessPhase.RESOLVE
        }

        @Override
        void finished() {}

        @Override
        void checkingFiles(List<File> list) {}

        @Override
        void startReadingFile(File file) {}

        ProcessPhase lastPhase = null
        String messageFromExcel
        Integer lastPercentComplete = 0

        @Override
        void setMessage(String s) {
            this.messageFromExcel = s
            setProcessStatus(this.uploadId, this.lastPhase, s, this.lastPercentComplete)
        }

        @Override
        void setPercentage(Integer integer) {
            setProcessStatus(this.uploadId, this.lastPhase, this.messageFromExcel, integer)
            this.lastPercentComplete = integer
        }

        @Override
        void finishedReadingFile(File file) {}

        @Override
        void startProcessingRawTDs() {
            this.lastPhase = ProcessPhase.PARSE_RAW_TD
            setProcessStatus(this.uploadId, ProcessPhase.PARSE_RAW_TD, "Processing Trustmark Definitions from Raw Data...")
        }

        @Override
        void finishedProcessingRawTDs(List<edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition> list) {}

        @Override
        void startProcessingRawTIPs() {
            this.lastPhase = ProcessPhase.PARSE_RAW_TIP
            setProcessStatus(this.uploadId, ProcessPhase.PARSE_RAW_TIP, "Processing Trust Interoperability Profiles from Raw Data...")
        }

        @Override
        void finishedProcessingRawTIPs(List<edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile> list) {}

        @Override
        void errorDuringBulkRead(Throwable throwable) {
            this.errorDuringRead = throwable
        }

        @Override
        void fileNotSupported(File file, Throwable throwable) {
            this.fileNotSupported = file
            this.errorDuringRead = throwable
        }

    }

}
