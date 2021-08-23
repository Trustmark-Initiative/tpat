package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.Serializer
import edu.gatech.gtri.trustmark.v1_0.io.SerializerFactory
import edu.gatech.gtri.trustmark.v1_0.io.TrustInteroperabilityProfileResolver
import edu.gatech.gtri.trustmark.v1_0.io.TrustmarkDefinitionResolver
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkFrameworkIdentifiedObject
import groovy.json.JsonOutput
import org.apache.commons.lang.StringUtils
import org.json.JSONArray
import org.json.JSONObject
import tmf.host.artifact_processing.ActionType
import tmf.host.artifact_processing.ArtifactAction
import tmf.host.artifact_processing.MemoryProcessingData

/**
 * Contains the business logic to apply the given changes (in JSON) to the database.  Assumes the {@link tmf.host.artifact_processing.MemoryProcessingData}
 * class still contains necessary data.  The JSON given contains what the user saw and modified.
 * <br/><br/>
 * @user brad
 * @date 12/5/16
 */
class ApplyChangesService extends AbstractLongRunningService {
    //==================================================================================================================
    // Grails Service Directives
    //==================================================================================================================
    public static final String APPLY_CHANGES_EXECUTING = ApplyChangesService.class.getSimpleName()+".APPLY_CHANGES_EXECUTING"

    int MAX_SIZE = 127

    enum ApplyChangesPhase {
        APPLYING_ACTIONS("Applying Actions", "The system is adding all TDs and TIPs to the current Version Set."),
        PROCESS_KEYWORDS_TDS("TD Keywords", "The system is analyzing the given TDs for keywords."),
        PROCESS_KEYWORDS_TIPS("TIP Keywords", "The system is analyzing the given TIPs for keywords."),
        COMPLETE("Complete", "Everything has completed successfully.");

        public String displayName
        public String description
        private ApplyChangesPhase(String displayName, String desc){
            this.displayName = displayName
            this.description = desc
        }
    }

    //==================================================================================================================
    // Required Services
    //==================================================================================================================

    //==================================================================================================================
    // Service Methods
    //==================================================================================================================
    /**
     * A simple method to tell you if a thread is already executing.
     */
    public boolean isExecuting() {
        return isExecuting(APPLY_CHANGES_EXECUTING)
    }

    /**
     * An external controller can call this to obtain the global "lock" on processing.  This should be done
     * BEFORE ay calls to applyChanges are made.
     */
    public boolean setExecuting() {
        return setExecuting(APPLY_CHANGES_EXECUTING)
    }

    /**
     * Actually applies changes to the VersionSet, based on the given JSON data.
     */
    void applyChanges(Long uploadId){
        try {
            setStatus(uploadId, ApplyChangesPhase.APPLYING_ACTIONS, "Starting the apply changes service...", -1)
            log.info("Starting the ApplyChanges service...")

            MemoryProcessingData mpd = MemoryProcessingData.find(uploadId)

            log.debug("Successfully found @|green ${mpd.artifactActions.size()}|@ actions to perform, starting...")
            setStatus(uploadId, ApplyChangesPhase.APPLYING_ACTIONS, "Applying actions to the VersionSet...", 0)
            for (int i = 0; i < mpd.artifactActions.size(); i++) {
                ArtifactAction action = mpd.artifactActions.get(i)
                setStatus(uploadId, ApplyChangesPhase.APPLYING_ACTIONS, "Applying Action[${action.actionType} ${action.type} ${action.artifact?.name}, v${action.artifact?.version}]", getPercent(i, mpd.artifactActions.size()))
                doApplyAction(mpd, action)
                Thread.sleep(1)
            }

            rebuildKeywords(uploadId, mpd.versionSetId)

            setStatus(uploadId, ApplyChangesPhase.COMPLETE, "Successfully applied ${mpd.artifactActions.size()} actions.", -1)
        }catch(Throwable t){
            fatalError(uploadId, "There was an unexpected error while processing your actions!  Error Details: "+t.toString(), t)
            log.error("Error processing actions!", t)
        }
        SystemVariable.withTransaction {
            SystemVariable.storeProperty(APPLY_CHANGES_EXECUTING, "false")
        }
    }//end applyChanges()

    /**
     * Given a TrustmarkDefinition in memory, this method will either 1) find an exact copy from the database and return
     * that or 2) store the new TrustmarkDefinition.
     */
    TrustmarkDefinition storeTd(String username, edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td){
        Serializer jsonSerializer = FactoryLoader.getInstance(SerializerFactory.class).jsonSerializer
        StringWriter stringWriter = new StringWriter()
        jsonSerializer.serialize(td, stringWriter)
        File tempFile = File.createTempFile("td-", ".json")
        String tdJson = stringWriter.toString()
        tempFile << tdJson

        log.debug("TD JSON: \n"+tdJson)

        String checksum = BinaryObject.calculateChecksum(tempFile)
        BinaryObject existing = BinaryObject.findByChecksumAndChecksumAlgorithm(checksum, BinaryObject.CHECKSUM_ALGORITHM)
        if( existing ){
            log.warn("WE COULD IMPROVE THIS BY USING AN EXISTING OBJECT!")
        }

        log.debug("Building binary object...")
        BinaryObject tdObject = buildBinary(username, tempFile)
        TrustmarkDefinition databaseTd = new TrustmarkDefinition()
        databaseTd.artifact = tdObject
        setData(databaseTd, td)
        databaseTd.save(failOnError: true)

        log.info("Storing TD[${databaseTd.identifier}] with Deprecated=${databaseTd.deprecated}")
        return databaseTd
    }
    //==================================================================================================================
    // Helper Methods
    //==================================================================================================================
    protected Map resolveAllObjectIds(Long versionSetId, boolean clearKeywords){
        Map objectIds = [tds: [], tips: []]
        VersionSet.withTransaction {
            VersionSet vs = VersionSet.get(versionSetId)
            log.debug("Removing all old keyword links for VersionSet[@|cyan ${vs.name}|@]...")
            if( clearKeywords ) {
                KeywordTDLink.executeUpdate("delete KeywordTDLink link where link.versionSet = :vs", [vs: vs])
                KeywordTIPLink.executeUpdate("delete KeywordTIPLink link where link.versionSet = :vs", [vs: vs])
            }
            log.debug("Collecting all TD and TIP Link unique ids...")
            List<VersionSetTDLink> tdLinks = VersionSetTDLink.findAllByVersionSet(vs)
            for( VersionSetTDLink tdLink : tdLinks ?: []){
                objectIds.tds.add(tdLink.id)
            }
            List<VersionSetTIPLink> tipLinks = VersionSetTIPLink.findAllByVersionSet(vs)
            for( VersionSetTIPLink tipLink : tipLinks ?: []){
                objectIds.tips.add(tipLink.id)
            }
        }
        return objectIds
    }

    protected void rebuildKeywords(Long uploadId, Long versionSetId){
        log.debug("Rebuilding keywords for VersionSet...")
        setStatus(uploadId, ApplyChangesPhase.PROCESS_KEYWORDS_TDS, "Rebuilding keywords...", -1)

        Map objectIds = resolveAllObjectIds(versionSetId, true)
        log.info("Successfully resolved @|green ${objectIds.tds.size()}|@ TD and @|green ${objectIds.tips.size()}|@ TIP identifiers for VersionSet[@|cyan ${versionSetId}|@], building keywords...")

        log.debug("Updating keywords for TDs... ${objectIds.tds.size()}")
        for( int i = 0; i < objectIds.tds.size(); i++ ){
            setStatus(uploadId, ApplyChangesPhase.PROCESS_KEYWORDS_TDS, "Rebuilding keywords for Trustmark Definitions...", getPercent(i, objectIds.tds.size()))
            Long id = objectIds.tds.get(i)
            VersionSetTDLink.withTransaction {
                VersionSet vs = VersionSet.get(versionSetId)
                VersionSetTDLink tdLink = VersionSetTDLink.get(id)
                File tdFile = tdLink.getTrustmarkDefinition().getArtifact().getContent().toFile()
                edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td =
                        FactoryLoader.getInstance(TrustmarkDefinitionResolver.class).resolve(tdFile)

                Collection<String> keywords = td.getMetadata().getKeywords()
                for( String keyword : keywords ){
                    Keyword keywordDb = Keyword.findByNameIlike(keyword)
                    if( !keywordDb ){
                        keywordDb = new Keyword(name: keyword)
                        keywordDb.save(failOnError:true)
                    }
                    KeywordTDLink link = new KeywordTDLink(versionSet: vs, keyword: keywordDb, td: tdLink.getTrustmarkDefinition())
                    link.save(failOnError: true)
                    log.debug("Successfully associated Keyword[@|yellow ${keyword}|@] with TD[@|cyan ${td.getMetadata().getIdentifier().toString()}|@]")
                }
            }
        }
        log.debug("Updating keywords for TIPs...  ${objectIds.tips.size()}")
        for( int i = 0; i < objectIds.tips.size(); i++ ){
            setStatus(uploadId, ApplyChangesPhase.PROCESS_KEYWORDS_TIPS, "Rebuilding keywords for Trust Interoperability Profiles...", getPercent(i, objectIds.tips.size()))
            Long id = objectIds.tips.get(i)
            VersionSetTDLink.withTransaction {
                VersionSet vs = VersionSet.get(versionSetId)
                VersionSetTIPLink tipLink = VersionSetTIPLink.get(id)
                File tipFile = tipLink.getTrustInteroperabilityProfile().getArtifact().getContent().toFile()
                edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip =
                        FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class).resolve(tipFile)
                Collection<String> keywords = tip.getKeywords()
                for( String keyword : keywords ){
                    Keyword keywordDb = Keyword.findByNameIlike(checkKeywordLength(keyword))
                    if( !keywordDb ){
                        keywordDb = new Keyword(name: checkKeywordLength(keyword))
                        keywordDb.save(failOnError:true)
                    }
                    KeywordTIPLink link = new KeywordTIPLink(versionSet: vs, keyword: keywordDb, tip: tipLink.getTrustInteroperabilityProfile())
                    link.save(failOnError: true)
                    log.debug("Successfully associated Keyword[@|yellow ${keyword}|@] with TIP[@|cyan ${tip.getIdentifier().toString()}|@]")
                }
            }
        }

        log.info("Successfully rebuilt keywords.")
    }

    /**
     * Given a list of keywords, and the VersionSet and Link they go with, this method will assert the keywords are
     * in the database, and will create the appropriate TD/TIP links.
     */
    private void applyKeywords(List<String> keywords, VersionSet vs, VersionSetLink vsLink){
        if( keywords?.size() > 0 ){
            for( String keywordName : keywords ){
                Keyword keyword = Keyword.findByName(checkKeywordLength(keywordName.toLowerCase()))
                if( !keyword)  {
                    keyword = new Keyword(name: checkKeywordLength(keywordName.toLowerCase()))
                    keyword.save(failOnError: true)
                }
                if( vsLink.isTdLink() ){
                    KeywordTDLink link = new KeywordTDLink(versionSet: vs, keyword: keyword)
                    link.td = ((VersionSetTDLink) vsLink).getTrustmarkDefinition()
                    link.save(failOnError: true)
                }else{
                    KeywordTIPLink link = new KeywordTIPLink(versionSet: vs, keyword: keyword)
                    link.tip = ((VersionSetTIPLink) vsLink).getTrustInteroperabilityProfile()
                    link.save(failOnError: true)
                }
            }
        }
    }

    /**
     * check for keywords exceeding the max db length, truncate to max limit if they do
     * @param keywordName
     * @return
     */
    private String checkKeywordLength(String keywordName) {
        if(keywordName.length() > MAX_SIZE) {
            return keywordName.substring(0, MAX_SIZE)
        }
        return keywordName
    }

    /**
     * Performs the simple task of creating a new VersionSetTDLink based on the incoming parameters and returning it.
     */
    private VersionSetTDLink addNewTdLink(VersionSet vs, TrustmarkDefinition databaseTd, edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td){
        VersionSetTDLink tdLink = new VersionSetTDLink(versionSet: vs)
        tdLink.trustmarkDefinition = databaseTd
        tdLink.tdIdentifier = databaseTd.identifier
        tdLink.copyOver = false
        tdLink.status = "EDITABLE"
        // TODO We should log that this artifact was modified by a spreadsheet upload somehow.
        tdLink.save(failOnError: true)
        return tdLink
    }

    /**
     * Performs the simple task of creating a new VersionSetTIPLink based on the incoming parameters and returning it.
     */
    private VersionSetTIPLink addNewTipLink(VersionSet vs, TrustInteroperabilityProfile databaseTip, edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip){
        VersionSetTIPLink tipLink = new VersionSetTIPLink(versionSet: vs)
        tipLink.trustInteroperabilityProfile = databaseTip
        tipLink.tipIdentifier = databaseTip.identifier
        tipLink.copyOver = false
        tipLink.status = "EDITABLE"
        tipLink.primaryTIP = tip.primary // By default, a TIP is not a high level TIP.  This must be made explicitly.
        // TODO We should log that this artifact was modified by a spreadsheet upload somehow.
        tipLink.save(failOnError: true)
        return tipLink
    }

    /**
     * This method is responsible for applying an add action to the database.  Note that add actions are simple and have
     * few side affects.
     */
    private void doAddAction(MemoryProcessingData mpd, VersionSet vs, ArtifactAction action){
        log.debug("Performing add @|cyan ${action.id}|@ to VersionSet @|green ${vs.name}|@...")
        VersionSetLink link = null
        List<String> keywords = []
        if( action.type == "TD" ){
            def td = mpd.getTd(action.id)
            TrustmarkDefinition databaseTd = storeTd(mpd.username, td)
            link = addNewTdLink(vs, databaseTd, td)
            keywords = td.getMetadata().getKeywords()
        }else if( action.type == "TIP" ){
            def tip = mpd.getTip(action.id)
            TrustInteroperabilityProfile databaseTip = storeTip(mpd.username, tip)
            link = addNewTipLink(vs, databaseTip, tip)
            keywords = tip.getKeywords()
        }else{
            throw new UnsupportedOperationException("Unknown ADD action type: "+action.type)
        }

        applyKeywords(keywords, vs, link)
    }

    private void doOverwriteAction(MemoryProcessingData mpd, VersionSet vs, ArtifactAction action) {
        log.debug("Performing overwrite @|cyan ${action.id}|@ to VersionSet @|green ${vs.name}|@...")
        if( action.type == "TD" ){
            def td = mpd.getTd(action.id)
            TrustmarkDefinition databaseTd = storeTd(mpd.username, td)

            VersionSetTDLink tdLink = VersionSetTDLink.get(action.previousLinkId)
            if( !tdLink ) throw new UnsupportedOperationException("Cannot overwrite TD Link ${action.previousLinkId}, because it could not be found.")
            tdLink.trustmarkDefinition = databaseTd
            tdLink.tdIdentifier = databaseTd.identifier
            tdLink.copyOver = false
            tdLink.status = "EDITABLE"
            // TODO There is some amount of source information that should be saved here.
            tdLink.save(failOnError: true)

        }else if( action.type == "TIP" ){
            def tip = mpd.getTip(action.id)
            TrustInteroperabilityProfile databaseTip = storeTip(mpd.username, tip)

            VersionSetTIPLink tipLink = VersionSetTIPLink.get(action.previousLinkId)
            if( !tipLink ) throw new UnsupportedOperationException("Cannot overwrite TIP Link ${action.previousLinkId}, because it could not be found.")
            tipLink.trustInteroperabilityProfile = databaseTip
            tipLink.tipIdentifier = databaseTip.identifier
            tipLink.copyOver = false
            tipLink.status = "EDITABLE"
            // TODO There is some amount of source information that should be saved here.
            tipLink.save(failOnError: true)

        }else{
            throw new UnsupportedOperationException("Unknown OVERWRITE action type: "+action.type)
        }
    }

    /**
     * Called to apply an action to the database.
     */
    private void doApplyAction(MemoryProcessingData mpd, ArtifactAction action){
        VersionSet.withTransaction {
            VersionSet vs = VersionSet.get(mpd.versionSetId)
            if( !vs ) throw new UnsupportedOperationException("Cannot find VS: "+mpd.versionSetId)
            log.debug("Performing ApplyAction @|cyan ${action.id}|@ to VersionSet @|green ${vs.name}|@...")

            // TODO PRE Actions

            if( action.actionType == ActionType.ADD ){
                doAddAction(mpd, vs, action)
            } else if( action.actionType == ActionType.OVERWRITE ) {
                doOverwriteAction(mpd, vs, action)
            } else if( action.actionType == ActionType.DEPRECATE ) {
                doDeprecateAction(mpd, vs, action)


                // TODO Handle other add types (ie, besides ADD or OVERWRITE)

            } else {
                log.error("Unknown action type: "+action.actionType)
            }

            if( action.postActions?.size() > 0 ){
                for( ArtifactAction postAction : action.postActions ){
                    doApplyAction(mpd, postAction)
                }
            }
        }
    }//end doApplyAction()

    private void doDeprecateAction(MemoryProcessingData mpd, VersionSet vs, ArtifactAction action){
        String id = action.artifact.identifier
        String supersededBy = action.artifact.SupersededBy
        log.debug("Performing DeprecateAction for id @|cyan ${id}|@ and supersededBy @|cyan ${supersededBy}|@")
        if(StringUtils.isNotBlank(id)){
            doDeprecateTd(vs, id, supersededBy)
            doDeprecateTip(vs, id, supersededBy)
        }else{
            log.error("Error - cannot deprecate because action.artifact.identifier is not found!")
        }

    }//end doDeprecateAction

    private void doDeprecateTd(VersionSet vs, String id, String supersededById) {
        VersionSetTDLink tdLink = VersionSetTDLink.findByVersionSetAndTdIdentifier(vs, id)
        if (tdLink) {
            TrustmarkDefinition databaseTd = tdLink.trustmarkDefinition
            log.debug("Performing DeprecateTd for TD @|cyan ${databaseTd.identifier}|@, id@|cyan ${id}|@ to supersededById @|cyan ${supersededById}|@")
            File tdFile = databaseTd.artifact.content.toFile()
            String originalJson = null
            if( databaseTd.artifact.mimeType.contains("json") ){
                originalJson = tdFile.text
            }else{
                edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class).resolve(tdFile)
                StringWriter writer = new StringWriter()
                FactoryLoader.getInstance(SerializerFactory.class).getJsonSerializer().serialize(td, writer)
                originalJson = writer.toString()
            }
            JSONObject json = new JSONObject(originalJson)
            json.getJSONObject("Metadata").put("Deprecated", true)
            if( StringUtils.isNotBlank(supersededById) ) {
                JSONObject tmiRefObj = new JSONObject()
                tmiRefObj.put("Identifier", supersededById)
                JSONArray jsonArray = new JSONArray()
                jsonArray.put(tmiRefObj)
                JSONObject supersededByObj = new JSONObject()
                supersededByObj.put("SupersededBy", jsonArray)
                json.getJSONObject("Metadata").put("Supersessions", supersededByObj)
                databaseTd.supersededBy = supersededById
            }
            String jsonStr = json.toString()
            File tempFile = File.createTempFile("td-", ".json")
            tempFile << jsonStr
            BinaryObject newArtifact = this.fileService.createBinaryObject(
                    tempFile, "SYSTEM", "application/json", "trustmarkDefinition.json", "json")

            databaseTd.artifact = newArtifact
            databaseTd.deprecated = true
            databaseTd.save(failOnError: true)

        }
    }//end doDeprecateTd()

    private void doDeprecateTip(VersionSet vs, String id, String supersededById) {
        VersionSetTIPLink tipLink = VersionSetTIPLink.findByVersionSetAndTipIdentifier(vs, id)
        if( tipLink ) {
            TrustInteroperabilityProfile databaseTip = tipLink.trustInteroperabilityProfile
            File tipFile = databaseTip.artifact.content.toFile()
            String originalJson = null
            if( databaseTip.artifact.mimeType.contains("json") ){
                originalJson = tipFile.text
            }else{
                edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip = FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class).resolve(tipFile)
                StringWriter writer = new StringWriter()
                FactoryLoader.getInstance(SerializerFactory.class).getJsonSerializer().serialize(tip, writer)
                originalJson = writer.toString()
            }
            JSONObject json = new JSONObject(originalJson)
            json.put("Deprecated", true)
            if( StringUtils.isNotBlank(supersededById) ){
                JSONObject tmiRefObj = new JSONObject()
                tmiRefObj.put("Identifier", supersededById)
                JSONArray jsonArray = new JSONArray()
                jsonArray.put(tmiRefObj)
                JSONObject supersededByObj = new JSONObject()
                supersededByObj.put("SupersededBy", jsonArray)
                json.put("Supersessions", supersededByObj)
                databaseTip.supersededBy = supersededById
            }
            String jsonStr = json.toString()
            File tempFile = File.createTempFile("tip-", ".json")
            tempFile << jsonStr
            BinaryObject newArtifact = this.fileService.createBinaryObject(
                    tempFile, "SYSTEM", "application/json", "trustInteroperabilityProfile.json", "json")

            databaseTip.artifact = newArtifact
            databaseTip.deprecated = true
            databaseTip.save(failOnError: true)
        }
    }//end doDeprecateTip()


    TrustInteroperabilityProfile storeTip(String username, edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip){
        Serializer jsonSerializer = FactoryLoader.getInstance(SerializerFactory.class).jsonSerializer
        StringWriter stringWriter = new StringWriter()
        jsonSerializer.serialize(tip, stringWriter)
        File tempFile = File.createTempFile("tip-", ".json")
        tempFile << stringWriter.toString()

        String checksum = BinaryObject.calculateChecksum(tempFile)
        BinaryObject existing = BinaryObject.findByChecksumAndChecksumAlgorithm(checksum, BinaryObject.CHECKSUM_ALGORITHM)
        if( existing ){
            log.warn("WE COULD IMPROVE THIS BY USING AN EXISTING OBJECT!")
        }

        log.debug("Building binary object...")
        BinaryObject tipObject = buildBinary(username, tempFile)
        TrustInteroperabilityProfile databaseTip = new TrustInteroperabilityProfile()
        databaseTip.artifact = tipObject
        setData(databaseTip, tip)

        return databaseTip
    }

    private void setData(TrustInteroperabilityProfile databaseTip, edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip){
        databaseTip.identifier = tip.getIdentifier().toString()
        databaseTip.subIdentifier = databaseTip.identifier // TODO Fix this
        databaseTip.name = tip.getName()
        databaseTip.tipVersion = tip.getVersion()
        databaseTip.description = tip.getDescription()
        databaseTip.deprecated = tip.isDeprecated()
        databaseTip.publicationDateTime = tip.getPublicationDateTime()
        databaseTip.issuerId = tip.getIssuer().getIdentifier().toString()
        databaseTip.issuerName = tip.getIssuer().getName()

        if( tip.getSatisfies() != null && tip.getSatisfies().size() > 0 )
            databaseTip.satisfies = toStringList(tip.getSatisfies())

        if( tip.getSupersededBy() != null && tip.getSupersededBy().size() > 0 )
            databaseTip.supersededBy = toStringList(tip.getSupersededBy())

        if( tip.getSupersedes() != null && tip.getSupersedes().size() > 0 )
            databaseTip.supersedes = toStringList(tip.getSupersedes())

    }

    private static String toStringList(List<TrustmarkFrameworkIdentifiedObject> tfiList){
        StringBuilder stringBuilder = new StringBuilder()
        if( tfiList != null && tfiList.size() > 0 ) {
            for (int i = 0; i < tfiList?.size(); i++) {
                TrustmarkFrameworkIdentifiedObject tfObj = tfiList.get(i)
                stringBuilder.append(tfObj.getIdentifier().toString())
                if (i < (tfiList.size() - 1)) {
                    stringBuilder.append("\n")
                }
            }
        }
        return stringBuilder.toString()
    }

    private void setData(TrustmarkDefinition databaseTd, edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td){
        databaseTd.identifier = td.getMetadata().getIdentifier().toString()
        databaseTd.subIdentifier = databaseTd.identifier // TODO Fix this
        databaseTd.name = td.getMetadata().getName()
        databaseTd.tdVersion = td.getMetadata().getVersion()
        databaseTd.description = td.getMetadata().getDescription()
        databaseTd.deprecated = td.getMetadata().isDeprecated()
        databaseTd.publicationDateTime = td.getMetadata().getPublicationDateTime()
        databaseTd.definingOrganizationId = td.getMetadata().getTrustmarkDefiningOrganization().getIdentifier().toString()
        databaseTd.definingOrganizationName = td.getMetadata().getTrustmarkDefiningOrganization().getName()

        if( td.getMetadata().getSatisfies() != null && td.getMetadata().getSatisfies().size() > 0 )
            databaseTd.satisfies = toStringList(td.getMetadata().getSatisfies())

        if( td.getMetadata().getSupersededBy() != null && td.getMetadata().getSupersededBy().size() > 0 )
            databaseTd.supersededBy = toStringList(td.getMetadata().getSupersededBy())

        if( td.getMetadata().getSupersedes() != null && td.getMetadata().getSupersedes().size() > 0 )
            databaseTd.supersedes = toStringList(td.getMetadata().getSupersedes())

    }

    private BinaryObject buildBinary(String username, File tempFile){
        return fileService.createBinaryObject(
                tempFile, username, "application/json", "trustmark-definition.json", "json")
    }


    private void setStatus(Long uploadId, ApplyChangesPhase phase, String msg){
        setStatus(uploadId, phase, msg, -1)
    }
    private void setStatus(Long uploadId, ApplyChangesPhase phase, String msg, Integer percent){
        ApplyChangesFeedback.withTransaction {
            BinaryObject upload = BinaryObject.get(uploadId)
            ApplyChangesFeedback feedback = ApplyChangesFeedback.findByUpload(upload)

            feedback.phaseJson = buildPhaseJson(phase)
            feedback.message = msg
            feedback.percentage = percent

            feedback.save(failOnError: true)
        }
    }
    private void fatalError(Long uploadId, String errorMessage, Throwable t){
        ApplyChangesFeedback.withTransaction {
            BinaryObject upload = BinaryObject.get(uploadId)
            ApplyChangesFeedback feedback = ApplyChangesFeedback.findByUpload(upload)

            feedback.phaseJson = null // Clear any phase
            feedback.percentage = -1 // Clear any progress
            feedback.message = errorMessage
            feedback.hasError = true
            if( t != null )
                feedback.stacktraceJson = buildStacktraceJson(t)

            feedback.save(failOnError: true)
        }
    }

    private String buildPhaseJson(ApplyChangesPhase phase) {
        List phaseJson = []
        if( phase ) {
            for (ApplyChangesPhase cur : ApplyChangesPhase.values()) {
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
}/* end ApplyChangesService */