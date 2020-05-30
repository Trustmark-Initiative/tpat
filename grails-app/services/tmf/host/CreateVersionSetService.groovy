package tmf.host

import org.apache.commons.lang.StringUtils

/**
 * Performs all the work necessary for creating a {@link VersionSet}.  This may be a quick process (ie, for a new
 * VersionSet) or a slow, painful one (if LOTS of artifacts must be copied over).  Thus, the service runs in a Thread
 * on the server outside conventional HTTP patterns, allowing the user to start it and then monitor it over time.
 * <br/><br/>
 * @user brad
 * @date 12/20/16
 */
class CreateVersionSetService extends AbstractLongRunningService {
    //==================================================================================================================
    //  Services and Variables
    //==================================================================================================================
    public static final String EXECUTING_VAR = CreateVersionSetService.class.simpleName+".EXECUTING";
    public static final String STATUS_VAR = CreateVersionSetService.class.simpleName+".STATUS";
    public static final String MESSAGE_VAR = CreateVersionSetService.class.simpleName+".MESSAGE";
    public static final String PERCENTAGE_VAR = CreateVersionSetService.class.simpleName+".PERCENTAGE";

    FileService fileService;
    //==================================================================================================================
    //  Service Methods (Public Methods)
    //==================================================================================================================
    /**
     * A simple method to tell you if a thread is already executing.
     */
    boolean isExecuting() {
        return isExecuting(EXECUTING_VAR)
    }

    /**
     * An external controller can call this to obtain the global "lock" on processing.  This should be done
     * BEFORE ay calls to applyChanges are made.
     */
    boolean setExecuting() {
        return setExecuting(EXECUTING_VAR)
    }

    void stopExecuting() {
        stopExecuting(EXECUTING_VAR)
    }

    /**
     * Called to actually begin the process of creating the version set.  Assumes that the calling thread has
     * gained the ability to execute, by first calling setExecuting() and getting a true response.
     */
    void createVersionSet(Long vsId, String name, String previousName, String username){
        log.info("User[@|cyan ${username}|@] is Creating new version set[@|green ${name}|@]...")
        setStatus("START", "Starting the create version set service...", 0)

        VersionSetLogEntry vsle = null
        try {
            setStatus("CREATE", "Creating Version Set[${name}]...", 0)

            if (StringUtils.isNotBlank(previousName)) {
                setStatus("COPY_OVER", "Copying Trustmark Definitions and TIPs from previous Version Set[${previousName}]...", 0);

                List tdLinks = []
                List tipLinks = []
                VersionSet.withTransaction {
                    VersionSet previous = VersionSet.findByName(previousName);
                    for( VersionSetTDLink link : VersionSetTDLink.findAllByVersionSet(previous) ?: [] ){
                        tdLinks.add([id: link.id, identifier: link.tdIdentifier]);
                    }
                    for( VersionSetTIPLink link : VersionSetTIPLink.findAllByVersionSet(previous) ?: [] ){
                        tipLinks.add([id: link.id, identifier: link.tipIdentifier]);
                    }
                }
                log.info("Copying over @|green ${tdLinks.size()}|@ TDs and @|green ${tipLinks.size()}|@ TIPs from previous version set[@|cyan ${previousName}|@]...")

                int currentCount = 0;
                for( Map tdData : tdLinks ?: []){
                    log.debug("Copying Over TD: "+tdData.identifier);
                    VersionSetTDLink.withTransaction {
                        VersionSet current = VersionSet.findByName(name);
                        VersionSetTDLink link = VersionSetTDLink.findById(tdData.id);
                        copyVersionSetTDLink(link, current);
                    }
                    currentCount++;
                    if( (currentCount % 15) == 0 ){
                        setStatus("COPY_OVER", "Copying Trustmark Definitions and TIPs ...", getPercent(currentCount, (tdLinks.size() + tipLinks.size())));
                        cleanUpGorm()
                    }
                }

                for( Map tipData : tipLinks ?: []){
                    log.debug("Copying Over TIP: "+tipData.identifier);
                    VersionSetTIPLink.withTransaction {
                        VersionSet current = VersionSet.findByName(name);
                        VersionSetTIPLink link = VersionSetTIPLink.findById(tipData.id);
                        copyVersionSetTIPLink(link, current);
                    }
                    currentCount++;
                    if( (currentCount % 15) == 0 ){
                        setStatus("COPY_OVER", "Copying Trustmark Definitions and TIPs ...", getPercent(currentCount, (tdLinks.size() + tipLinks.size())));
                        cleanUpGorm()
                    }
                }
            }

            setStatus("SUCCESSFUL", "Successfully Created Version Set[${name}]...", 100)

            VersionSet.withTransaction {
                VersionSet vs = VersionSet.findByName(name)
                vs.createdSuccessfully = true
                vs.save(failOnError: true)
            }

            VersionSetLogEntry.create(vsId, "CREATED", "Successfully created version set ${name}", [name: name, userid: username])
        } catch(Throwable t) {
            log.error("Error creating version set!", t)
            fatalError("Encountered unexpected error creating version set: "+t.message, t)
        }

        stopExecuting(EXECUTING_VAR)
    }//end createVersionSet()
    //==================================================================================================================
    //  Helper Methods (Protected or Private)
    //==================================================================================================================
    private void cleanUpGorm() {
        // This method should be unnecessary due to a separate transaction for each copy over.
    }


    private void copyVersionSetTDLink(VersionSetTDLink link, VersionSet newVs){
        VersionSetTDLink newLink = new VersionSetTDLink(versionSet: newVs);
        newLink.trustmarkDefinition = copyTrustmarkDefinition(link.trustmarkDefinition);
        newLink.copyOver = true;
        newLink.tdIdentifier = link.tdIdentifier;
        newLink.status = "EDITABLE";
        newLink.originalVersionSet = newVs.predecessor;
        newLink.save(failOnError: true);

        List<KeywordTDLink> keywords = KeywordTDLink.findAllByVersionSetAndTd(link.versionSet, link.trustmarkDefinition);
        if( keywords && keywords.size() > 0 ){
            for( KeywordTDLink oldKeywordLink : keywords ) {
                KeywordTDLink newKeywordLink = new KeywordTDLink(versionSet: newVs, td: newLink.trustmarkDefinition, keyword: oldKeywordLink.keyword);
                newKeywordLink.save(failOnError: true);
            }
        }

        // TODO Any other things that need copying over?

    }

    private void copyVersionSetTIPLink(VersionSetTIPLink link, VersionSet newVs){
        VersionSetTIPLink newLink = new VersionSetTIPLink(versionSet: newVs);
        newLink.trustInteroperabilityProfile = copyTrustInteroperabilityProfile(link.trustInteroperabilityProfile);
        newLink.copyOver = true;
        newLink.tipIdentifier = link.tipIdentifier;
        newLink.status = "EDITABLE";
        newLink.originalVersionSet = newVs.predecessor;
        newLink.primaryTIP = link.primaryTIP; // If it was before, it probably still is.
        newLink.save(failOnError: true);

        List<KeywordTIPLink> keywords = KeywordTIPLink.findAllByVersionSetAndTip(link.versionSet, link.trustInteroperabilityProfile);
        if( keywords && keywords.size() > 0 ){
            for( KeywordTIPLink oldKeywordLink : keywords ) {
                KeywordTIPLink newKeywordLink = new KeywordTIPLink(versionSet: newVs, tip: newLink.trustInteroperabilityProfile, keyword: oldKeywordLink.keyword);
                newKeywordLink.save(failOnError: true);
            }
        }

        // TODO Any other things that need copying over?
    }

    private TrustInteroperabilityProfile copyTrustInteroperabilityProfile(TrustInteroperabilityProfile oldTip){
        BinaryObject artifact = copyBinaryObject(oldTip.artifact);

        TrustInteroperabilityProfile newTip = new TrustInteroperabilityProfile();
        newTip.identifier = oldTip.identifier;
        newTip.subIdentifier = oldTip.subIdentifier;
        newTip.name = oldTip.name;
        newTip.tipVersion = oldTip.tipVersion;
        newTip.description = oldTip.description;
        newTip.deprecated = oldTip.deprecated;
        newTip.publicationDateTime = oldTip.publicationDateTime;
        newTip.issuerId = oldTip.issuerId;
        newTip.issuerName = oldTip.issuerName;
        newTip.artifact = artifact;
        newTip.save(failOnError: true);

        return newTip;
    }


    private TrustmarkDefinition copyTrustmarkDefinition(TrustmarkDefinition oldTd){
        BinaryObject artifact = copyBinaryObject(oldTd.artifact);

        TrustmarkDefinition newTd = new TrustmarkDefinition();
        newTd.identifier = oldTd.identifier;
        newTd.subIdentifier = oldTd.subIdentifier;
        newTd.name = oldTd.name;
        newTd.tdVersion = oldTd.tdVersion;
        newTd.description = oldTd.description;
        newTd.deprecated = oldTd.deprecated;
        newTd.publicationDateTime = oldTd.publicationDateTime;
        newTd.definingOrganizationId = oldTd.definingOrganizationId;
        newTd.definingOrganizationName = oldTd.definingOrganizationName;
        newTd.artifact = artifact;
        newTd.save(failOnError: true);

        return newTd;
    }

    private BinaryObject copyBinaryObject(BinaryObject bo){
        File file = bo.content.toFile();
        BinaryObject binaryObject = fileService.createBinaryObject(file, bo.createdBy, bo.mimeType, bo.originalFilename, bo.originalExtension);
        return binaryObject;
    }


    private void setStatus(String status, String msg){
        setStatus(CreateVersionSetService.class, status, msg);
    }
    private void setStatus(String status, String msg, Integer percent){
        setStatus(CreateVersionSetService.class, status, msg, percent);
    }
    private void fatalError(String errorMessage, Throwable t){
        fatalError(CreateVersionSetService.class, errorMessage, t);
    }


}// end CreateVersionSetService