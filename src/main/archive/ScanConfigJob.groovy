package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.ArtifactIdentification
import edu.gatech.gtri.trustmark.v1_0.io.ArtifactIdentificationHelper
import edu.gatech.gtri.trustmark.v1_0.io.TrustInteroperabilityProfileResolver
import edu.gatech.gtri.trustmark.v1_0.io.TrustmarkDefinitionResolver
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkFrameworkIdentifiedObject
import groovy.json.JsonOutput
import org.springframework.util.FileSystemUtils

import java.util.regex.Pattern

/**
 * Created by brad on 1/22/16.
 */
class ScanConfigJob {
    //==================================================================================================================
    // Job Specifics
    //==================================================================================================================
    def sessionRequired = true
    def concurrent = false
    def description = "Scans the configuration directories to detect any added or removed Trustmark Framework artifacts."
    def sessionFactory;
    //==================================================================================================================
    // Triggers
    //==================================================================================================================
    static triggers = {
        if( getTriggerType() == "cron" ){
            cron cronExpression: getCronTriggerCronExpression(), startDelay: getCronTriggerStartDelay()
        }else {
            simple repeatInterval: getSimpleTriggerRepeatInterval(), startDelay: getSimpleTriggerStartDelay(), repeatCount: getSimpleTriggerRepeatCount()
        }
    }


    public static String getTriggerType() {
        return getTfHostProperties().getString("scanjob.trigger");
    }
    public static Long getSimpleTriggerRepeatInterval(){
        return Long.parseLong(getTfHostProperties().getString("scanjob.simpletrigger.repeatInterval"));
    }
    public static Long getSimpleTriggerStartDelay(){
        return Long.parseLong(getTfHostProperties().getString("scanjob.simpletrigger.startDelay"));
    }
    public static Long getSimpleTriggerRepeatCount(){
        return Long.parseLong(getTfHostProperties().getString("scanjob.simpletrigger.repeatCount"));
    }

    public static Long getCronTriggerStartDelay(){
        return Long.parseLong(getTfHostProperties().getString("scanjob.crontrigger.startDelay"));
    }
    public static String getCronTriggerCronExpression(){
        return getTfHostProperties().getString("scanjob.crontrigger.cronExpression");
    }

    public static ResourceBundle getTfHostProperties() {
        return ResourceBundle.getBundle("tf_host");
    }


    //==================================================================================================================
    // Execute entry point
    //==================================================================================================================
    void execute() {
        log.info("Starting ${this.getClass().getSimpleName()}...");
        long overallStartTime = System.currentTimeMillis();

        initialize();

        long tdStartTime = System.currentTimeMillis();
        scanForTDChanges();
        long tdStopTime = System.currentTimeMillis();

        long tipStartTime = tdStopTime;
        scanForTIPChanges();
        long tipStopTime = System.currentTimeMillis();

        long superSedesStartTime = tipStopTime;
        updateSupersedesInformation()
        long superSedesStopTime = System.currentTimeMillis();

        doHardUpdates();


        if( !tdImportErrors.keySet().isEmpty() ){
            StringBuilder errorString = new StringBuilder();
            for( File file : tdImportErrors.keySet() ){
                errorString.append("  - File[${file.getName()}]: "+tdImportErrors.get(file).toString()+"\n");
            }
            log.error("****  Errors("+tdImportErrors.keySet().size()+") encountered during TD-import: \n\n"+errorString.toString()+"\n**** END ERRORS ****\n\n");
        }else{
            log.info("No errors reported during TD Import.")
        }
        if( !tipImportErrors.keySet().isEmpty() ){
            StringBuilder errorString = new StringBuilder();
            for( File file : tipImportErrors.keySet() ){
                errorString.append("  - File[${file.getName()}]: "+tipImportErrors.get(file).toString());
            }
            log.error("****  Errors("+tipImportErrors.keySet().size()+") encountered during TIP-import: \n\n"+errorString.toString()+"\n\n\n**** END ERRORS ****");
        }else{
            log.info("No errors reported during TIP Import.")
        }

        destroy();

        long overallStopTime = System.currentTimeMillis();
        log.info("Successfully Executed ${this.getClass().getSimpleName()} in ${(overallStopTime - tdStartTime)}ms.")
    }//end execute()


    //==================================================================================================================
    // Instance Variables
    //==================================================================================================================
    private Map<String, Collection<String>> supersedesInformation;
    private Map<File, Throwable> tdImportErrors;
    private Map<File, Throwable> tipImportErrors;
    //==================================================================================================================
    // General Helper Methods
    //==================================================================================================================
    private void doHardUpdates() {
        String hardUpdateDirPath = SystemVariable.quickFindPropertyValue(SystemVariable.HARD_UPDATE_DIR);
        File hardUpdateDir = new File(hardUpdateDirPath);
        if( hardUpdateDir.exists() && hardUpdateDir.isDirectory() ){
            log.debug("Scanning hard update directory for changes...");
            def updatesMade = []
            _doHardUpdateHelper(hardUpdateDir, updatesMade);
            if( updatesMade.size() > 0 ) {
                log.debug("Writing hard update summary...");
                File updateSummary = new File(hardUpdateDir, "summary.json." + getNowString() + ".LOADED");
                updateSummary << JsonOutput.toJson(updatesMade);
            }
        }else{
            log.debug("Could not find directory ${hardUpdateDirPath}.  No hard updates made.");
        }

    }//end doHardUpdates()

    private void _doHardUpdateHelper(File dir, List updatesMade){
        File[] files = dir.listFiles();
        for( File file : files ){
            if( file.isDirectory() ){
                _doHardUpdateHelper(file, updatesMade);
            }else if( !file.getName().toUpperCase().endsWith(".LOADED") ){
                log.info("Hard update file found: "+file.getCanonicalPath());
                Map updateInfo = doHardUpdate(file);
                updatesMade.add(updateInfo);
                // This operation will make sure the process doesn't keep running on the same file.
                File loadedFileName = new File(file.getParentFile(), file.getName()+"."+getNowString()+".LOADED");
                file.renameTo(loadedFileName);
            }
        }
    }//end _doHardUpdateHelper()

    /**
     * doHardUpdate() will process the file and learn what resource it is supposed to be.  Next, it will load the file
     * contents directly in the database without updating cache timestamps.  Thus, dangerous to use.
     */
    private Map doHardUpdate(File file){
        log.debug("Hard updating file: "+file.canonicalPath);
        try {
            String fileContents = file.text;
            ArtifactIdentification artifactId = FactoryLoader.getInstance(ArtifactIdentificationHelper.class).getArtifactIdentification(file);
            if( artifactId.getArtifactType() == ArtifactIdentification.ArtifactType.TRUSTMARK_DEFINITION ){
                edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class).resolve(file, false);

                TrustmarkDefinition fromDb = TrustmarkDefinition.findByIdentifier(td.getMetadata().getIdentifier().toString());
                if( fromDb == null ){
                    log.warn("Cannot hard update [${td.getMetadata().getIdentifier().toString()}], since it is not in the database.")
                    throw new RuntimeException("Cannot hard update [${td.getMetadata().getIdentifier().toString()}], since it is not in the database.")
                }

                log.info("Updating TD[${fromDb.identifier}] Artifact[${fromDb.artifact.id}] with file contents from [${file.canonicalPath}]...")
                Artifact artifact = fromDb.artifact;
                artifact.content = fileContents;
                artifact.save(failOnError: true);

            }else if( artifactId.getArtifactType() == ArtifactIdentification.ArtifactType.TRUST_INTEROPERABILITY_PROFILE ){
                edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip = FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class).resolve(file, false);

                TrustInteroperabilityProfile fromDb = TrustInteroperabilityProfile.findByIdentifier(tip.getIdentifier().toString());
                if( fromDb == null ){
                    log.warn("Cannot hard update [${tip.getIdentifier().toString()}], since it is not in the database.")
                    throw new RuntimeException("Cannot hard update [${tip.getIdentifier().toString()}], since it is not in the database.")
                }

                log.info("Updating TIP[${fromDb.identifier}] Artifact[${fromDb.artifact.id}] with file contents from [${file.canonicalPath}]...")
                Artifact artifact = fromDb.artifact;
                artifact.content = fileContents;
                artifact.save(failOnError: true);

            }else{
                log.error("Cannot process ${file.canonicalPath} because it is not supported at this time: "+artifactId.toString());
                throw new RuntimeException("Cannot process ${file.canonicalPath} because it is not supported at this time: "+artifactId.toString());
            }

            return [file: file.canonicalPath, status: "SUCCESS", type: artifactId.artifactType.toString()];
        }catch(Throwable t){
            log.error("Error processing hard update file[${file.canonicalPath}]: "+t);
            return [file: file.canonicalPath, status: "FAILURE", reason: t.getMessage(), errorClass: t.getClass().getName()];
        }
    }//end doHardUpdate()

    private String getNowString() {
        return System.currentTimeMillis() + "";
    }

    // Called when the job has some initialize work to do.
    private void initialize() {
        supersedesInformation = [:] // Create an empty hash to store supersedes information.
        tdImportErrors = [:]
        tipImportErrors = [:]
    }

    private void destroy() {
        supersedesInformation = null;
        tdImportErrors = null;
        tipImportErrors = null;
    }

    /**
     * Unzips the given file to the tmp directory and returns a pointer to the unzip root.
     * @param zipFile
     * @return
     */
    private File unzip(File zipFile){
        File tmpDir = File.createTempFile("zip-extract-", ".dir");
        tmpDir.delete();
        tmpDir.mkdirs();

        def ant = new AntBuilder()
        ant.project.buildListeners.firstElement().messageOutputLevel = 0
        ant.unzip(src: zipFile.canonicalPath, dest: tmpDir.canonicalPath, overwrite: "true")

        return tmpDir;
    }

    private static final FileFilter ALL_FILE_FILTER = {File file ->
        return file.isFile();
    } as FileFilter;
    private static final FileFilter ZIP_EXTENSION_FILTER = {File file ->
        return file.getName().toLowerCase().endsWith(".zip");
    } as FileFilter;
    private static final FileFilter XML_EXTENSION_FILTER = {File file ->
        return file.getName().toLowerCase().endsWith(".xml");
    } as FileFilter;
    private static final FileFilter JSON_EXTENSION_FILTER = {File file ->
        return file.getName().toLowerCase().endsWith(".xml");
    } as FileFilter;


    private List<File> collectFiles(File directory){
        return collectFiles(directory, {File file -> return true;} as FileFilter);
    }
    private List<File> collectFiles(File directory, FileFilter filter){
        List<File> files = []
        File[] matchingFiles = directory.listFiles(filter)
        if( matchingFiles && matchingFiles.length > 0 )
            files.addAll(matchingFiles);
        File[] subdirs = directory.listFiles({File file -> return file.isDirectory()} as FileFilter);
        if( subdirs && subdirs.length > 0 ){
            for( File subdir : subdirs )
                files.addAll(collectFiles(subdir, filter));
        }
        return files;
    }
    //==================================================================================================================
    //  Trust Interoperability Profile Methods
    //==================================================================================================================
    private void scanForTIPChanges(){
        TrustInteroperabilityProfile.findAll().each{ TrustInteroperabilityProfile tip ->
            try {
                File tdFile = new File(tip.filePath);
                if (!tdFile.exists()) {
                    log.warn("File[${tip.filePath}] does not exist, removing old Database information for TIP[name:${tip.name}, version: ${tip.tipVersion}]...");
                    TrustInteroperabilityProfile.executeUpdate("delete TrustInteroperabilityProfile tip where tip.id = :id", ["id": tip.id])
                }
            }catch(Throwable t){
                log.error("Unable to delete TIP[${tip.name}:${tip.tipVersion}]", t);
            }
        }

        int fileNum = 0;
        String tipDir = SystemVariable.quickFindPropertyValue(SystemVariable.TIP_SOURCE);
        log.info("Scanning for new/changed TIPs in Directory[${tipDir}]...")
        File directory = new File(tipDir);
        if( directory.exists() ) {
            List<File> tipFiles = collectFiles(directory, ALL_FILE_FILTER);
            log.debug("Encountered ${tipFiles.size()} possible trust interoperability profile files, parsing each...");
            for( File tipZipFile : tipFiles ?: [] ){
                try {
                    checkTipFile(tipZipFile);
                }catch(Throwable t){
                    log.error("Unable to import TIP from File[${tipZipFile}]", t);
                    tipImportErrors.put(tipZipFile, getCause(t));
                }
                fileNum++;
                if( fileNum % 50 == 0 ){
                    cleanUpGorm();
                }
            }
        }else{
            log.warn("No such directory: "+directory.path);
        }
    }//end scanForTIPChanges()


    /**
     * Checks to see if there is a record of the given zip file, and if not, tries to create a tmf.host.TrustInteroperabilityProfile from
     * it.  If there is, but it is out of date, the old record is deleted and a new one added.
     * <br/><br/>
     * @param tipFile
     */
    private void checkTipFile(File tipFile){
        TrustInteroperabilityProfile tip = TrustInteroperabilityProfile.findByFilePath( tipFile.canonicalPath );
        if( tip && (tip.getCacheDate().getTime() < tipFile.lastModified()) ){

            log.info("Update found to file: "+tipFile.canonicalPath)
            log.warn("Removing old Database information for TIP[name:${tip.name}, version: ${tip.tipVersion}] based on update...");
            TrustInteroperabilityProfile.executeUpdate("delete TrustInteroperabilityProfile tip where tip.id = :id", ["id": tip.id])

        }else if( tip && (tip.getCacheDate().getTime() >= tipFile.lastModified()) ) {
            log.debug("TIP File ${tipFile.canonicalPath} does not need to be imported.")
            return;
        }

        if( tipFile.getName().toLowerCase().endsWith(".zip") ) {
            initialTipImportFromZip(tipFile);
        }else if( tipFile.getName().toLowerCase().endsWith(".xml") ) {
            initialTipImportFromXml(tipFile, tipFile.canonicalPath);
        }
    }//end checkTdZipFile()

    /**
     * Supports a Zip file containing a tmf.host.TrustInteroperabilityProfile.xml file following the release pattern created by GTRI.
     */
    private void initialTipImportFromZip(File tipZipFile) {
        log.info("Initial import of TIP from ZIP file[${tipZipFile.canonicalPath}]...")

        log.debug("Unzipping file...")
        File unzipDir = unzip(tipZipFile);

        log.debug("Finding the TIP xml file...")
        List<File> xmlFiles = collectFiles(unzipDir, XML_EXTENSION_FILTER);
        if (xmlFiles.size() != 1) {
            log.error("Refusing to process TIP Zip file[${tipZipFile.canonicalPath}], since it has ${xmlFiles.size()} XML Files.  Expecting exactly 1.")
            return;
        }
        File tipXmlFile = xmlFiles.get(0);

        initialTipImportFromXml(tipXmlFile, tipZipFile.canonicalPath);

        FileSystemUtils.deleteRecursively(unzipDir);
    }

    private void initialTipImportFromXml(File tipXmlFile, String originalSourcePath){
        log.debug("Parsing TIP using tf-api...");
        TrustInteroperabilityProfileResolver resolver = FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class);
        edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip = resolver.resolve(tipXmlFile, false); // Parse without validating
        String identifier = tip.getIdentifier().toString()

        TrustInteroperabilityProfile tipInDatabase = TrustInteroperabilityProfile.findByIdentifier(identifier);
        if( tipInDatabase != null ){
            log.error("Refusing to process TIP ${originalSourcePath}, the Identifier[${identifier}] already exists in the database, tied to file[${tipInDatabase.filePath}].");
            return;
        }

        log.debug("Putting TIP[${tip.getIdentifier().toString()}] into database...");
        TrustInteroperabilityProfile databaseTip = new TrustInteroperabilityProfile();
        String startingUrl = findStartingUrl(identifier);
        if( startingUrl == null ){
            log.error("Refusing to process TIP ${originalSourcePath}, the Identifier[${identifier}] starts with an unknown BaseURL, not supported by this server.")
            return;
        }
        databaseTip.identifier = identifier;
        databaseTip.subIdentifier = identifier.substring(startingUrl.length()); // effectively removes the starting URL from the identifier.
        databaseTip.name = tip.getName();
        databaseTip.tipVersion = tip.getVersion();
        databaseTip.description = tip.getDescription();
        databaseTip.publicationDateTime = tip.getPublicationDateTime();
        databaseTip.issuerId = tip.getIssuer().getIdentifier().toString();
        databaseTip.issuerName = tip.getIssuer().getName();

        databaseTip.cacheDate = Calendar.getInstance().getTime(); // Now
        databaseTip.filePath = originalSourcePath ?: tipXmlFile.canonicalPath;

        Artifact artifact = new Artifact();
        artifact.format = "text/xml";
        artifact.content = tipXmlFile.text;
        artifact.save();

        databaseTip.artifact = artifact;
        databaseTip.save();

        log.info("Successfully stored tmf.host.TrustInteroperabilityProfile[${databaseTip.getName()} : ${databaseTip.getTipVersion()}]!")
    }//end initialTipImportFromZip

    //==================================================================================================================
    //  Trustmark Definition Methods
    //==================================================================================================================
    private void scanForTDChanges(){
        // First, we check to make sure the database referenced files still exist, and delete them if they don't.
        TrustmarkDefinition.findAll().each{ TrustmarkDefinition td ->
            try {
                File tdFile = new File(td.filePath);
                if (!tdFile.exists()) {
                    log.warn("File[${td.filePath}] does not exist, removing old Database information for TD[name:${td.name}, version: ${td.tdVersion}]...");
                    KeywordTDLink.executeUpdate("delete KeywordTDLink link where link.td = :td", ["td": td]);
                    TdSupersedes.executeUpdate("delete TdSupersedes supersedes where supersedes.superseder = :td or supersedes.superseded = :td", ["td": td]);
                    TrustmarkDefinition.executeUpdate("delete TrustmarkDefinition td where td.id = :id", ["id": td.id])
                }
            }catch(Throwable t){
                log.error("Unable to remove TrustmarkDefinition[${td.name}:${td.tdVersion}]!", t);
            }
        }

        String tdDir = SystemVariable.quickFindPropertyValue(SystemVariable.TD_SOURCE);
        log.info("Scanning for new/changed TDs in Directory[${tdDir}]...")
        File directory = new File(tdDir);
        if( directory.exists() ) {
            List<File> tdFiles = collectFiles(directory, ALL_FILE_FILTER);
            int fileNum = 0;
            log.debug("Encountered ${tdFiles.size()} possible trustmark definition files, parsing each...");
            for( File tdFile : tdFiles ?: [] ){
                try{
                    checkTdFile(tdFile);
                    fileNum++;
                    if( fileNum % 50 == 0 ){
                        cleanUpGorm();
                    }
                }catch(Throwable t){
                    log.error("Unable to import TD from File[${tdFile}]: "+t.toString());
                    tdImportErrors.put(tdFile, getCause(t));
                }
            }
        }else{
            log.warn("No such directory: "+directory.path);
        }
    }//end scanForTDChanges()

    private Throwable getCause(Throwable t){
        Throwable cause = t;
        while( cause.getCause() != null )
            cause = cause.getCause();
        return cause;
    }

    /**
     * Checks to see if there is a record of the given zip file, and if not, tries to create a tmf.host.TrustmarkDefinition from
     * it. If there is, but it is out of date, the old record is deleted and a new one added.
     * <br/><br/>
     * @param tdZipFile
     */
    private void checkTdFile(File tdFile){
        TrustmarkDefinition td = TrustmarkDefinition.findByFilePath( tdFile.canonicalPath );
        if( td && (td.getCacheDate().getTime() < tdFile.lastModified()) ){

            log.info("Update found to file: "+tdFile.canonicalPath)
            log.warn("Removing old Database information for TD[name:${td.name}, version: ${td.tdVersion}] based on update...");
            KeywordTDLink.executeUpdate("delete KeywordTDLink link where link.td = :td", ["td": td]);
            TdSupersedes.executeUpdate("delete TdSupersedes supersedes where supersedes.superseder = :td or supersedes.superseded = :td", ["td": td]);
            TrustmarkDefinition.executeUpdate("delete TrustmarkDefinition td where td.id = :id", ["id": td.id])

        }else if( td && (td.getCacheDate().getTime() >= tdFile.lastModified()) ) {
            log.debug("TD File ${tdFile.canonicalPath} does not need to be imported.")
            return;
        }

        if( tdFile.name.toLowerCase().endsWith(".zip") ){
            initialTdImportFromZip(tdFile);
        }else if( tdFile.name.toLowerCase().endsWith(".xml") ){
            initialTdImportFromXml(tdFile, tdFile.getCanonicalPath());
        }
    }//end checkTdZipFile()

    /**
     * Supports a Zip file containing a tmf.host.TrustmarkDefinition.xml file (and potentially a DEPRECATED.txt file) as is the
     * release pattern created by GTRI.
     */
    private void initialTdImportFromZip(File tdZipFile) {
        log.info("Initial import of TD from ZIP file[${tdZipFile.canonicalPath}]...")

        log.debug("Unzipping file...")
        File unzipDir = unzip(tdZipFile);

        log.debug("Finding tmf.host.TrustmarkDefinition.xml...")
        List<File> xmlFiles = collectFiles(unzipDir, XML_EXTENSION_FILTER);
        if (xmlFiles.size() != 1) {
            log.warn("Refusing to process TD Zip file[${tdZipFile.canonicalPath}], since it has ${xmlFiles.size()} XML Files.  Expecting exactly 1.")
            return;
        }

        File tdXmlFile = xmlFiles.get(0);

        initialTdImportFromXml(tdXmlFile, tdZipFile.canonicalPath);

        FileSystemUtils.deleteRecursively(unzipDir);
    }

    /**
     * Supports loading an XML file into the database, assuming it doesn't already exist.
     */
    private void initialTdImportFromXml(File tdXmlFile, String originalFilePath){
        log.debug("Parsing TD using tf-api...");
        TrustmarkDefinitionResolver resolver = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class);
        edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td = resolver.resolve(tdXmlFile, false); // Parse without validating
        String identifier = td.getMetadata().getIdentifier().toString()

        TrustmarkDefinition tdInDatabase = TrustmarkDefinition.findByIdentifier(identifier);
        if( tdInDatabase != null ){
            log.error("Refusing to process TD ${originalFilePath}, the Identifier[${identifier}] already exists in the database, tied to file[${tdInDatabase.filePath}].");
            return;
        }

        log.debug("Putting TD[${td.getMetadata().getIdentifier().toString()}] into database...");
        TrustmarkDefinition databaseTd = new TrustmarkDefinition();
        String startingUrl = findStartingUrl(identifier);
        if( startingUrl == null ){
            log.warn("Refusing to process TD ${originalFilePath}, the TD Identifier[${identifier}] starts with an unknown BaseURL, not supported by this server.")
            return;
        }
        databaseTd.identifier = identifier;
        databaseTd.subIdentifier = identifier.substring(startingUrl.length()); // effectively removes the starting URL from the identifier.
        databaseTd.name = td.getMetadata().getName();
        databaseTd.tdVersion = td.getMetadata().getVersion();
        databaseTd.description = td.getMetadata().getDescription();
        databaseTd.publicationDateTime = td.getMetadata().getPublicationDateTime();
        databaseTd.deprecated = td.getMetadata().isDeprecated();
        databaseTd.definingOrganizationId = td.getMetadata().getTrustmarkDefiningOrganization().getIdentifier().toString();
        databaseTd.definingOrganizationName = td.getMetadata().getTrustmarkDefiningOrganization().getName();

        databaseTd.cacheDate = Calendar.getInstance().getTime(); // Now
        databaseTd.filePath = originalFilePath ?: tdXmlFile.canonicalPath;

        Artifact artifact = new Artifact();
        artifact.format = "text/xml";
        artifact.content = tdXmlFile.text;
        artifact.save();

        databaseTd.artifact = artifact;
        databaseTd.save();

        if( td.getMetadata().getKeywords()?.size() > 0 ){
            for( String keyword : td.getMetadata().getKeywords() ){
                Keyword keywordFromDb = Keyword.findByName(keyword);
                if( !keywordFromDb ){
                    keywordFromDb = new Keyword(name: keyword);
                    keywordFromDb.save();
                }

                log.debug("Storing link from tmf.host.Keyword[${keyword}] to TD[${databaseTd.identifier}]")
                KeywordTDLink link = new KeywordTDLink(keyword: keywordFromDb, td: databaseTd);
                link.save();
            }
        }


        if( td.getMetadata().getSupersedes()?.size() > 0 ){
            List<String> supersedesInfo = []
            for(TrustmarkFrameworkIdentifiedObject tfio : td.getMetadata().getSupersedes() ){
                supersedesInfo.add(tfio.getIdentifier().toString());
            }
            this.supersedesInformation.put(td.getMetadata().getIdentifier().toString(), supersedesInfo);
        }

        log.info("Successfully stored TrustmarkDefinition[${databaseTd.getName()} : ${databaseTd.getTdVersion()}]!")
    }//end initialTdImportFromZip

    /**
     * Uses the supersedesInformation Map to update the database links between TDs.
     */
    private void updateSupersedesInformation() {
        log.debug("Updating supersedes information...");
        if( this.supersedesInformation && this.supersedesInformation.keySet().size() > 0 ){
            int counter = 0;
            for( String supersederId : this.supersedesInformation.keySet() ){
                TrustmarkDefinition superseder = TrustmarkDefinition.findByIdentifier(supersederId);
                if( !superseder ){
                    log.error("Cannot find TD with superseder ID[${supersederId}] in database!")
                    continue;
                }
                Collection<String> supersededIds = this.supersedesInformation.get(supersederId);
                for( String supersededId : supersededIds ?: [] ){
                    TrustmarkDefinition superseded = TrustmarkDefinition.findByIdentifier(supersededId);
                    if( superseded == null ){
                        log.error("Cannot find TD with superseded ID[${supersededId}] in database!")
                        continue;
                    }
                    log.debug("Adding superseder[${supersederId}] link to superseded[${supersededId}]...")
                    TdSupersedes supersedesLink = new TdSupersedes(superseder: superseder, superseded: superseded);
                    supersedesLink.save();
                    counter++;
                    if( counter % 50 == 0 ){
                        cleanUpGorm();
                    }

                }
            }
            log.info("Successfully added ${counter} superseseded links in database.");
        }
    }//end updateSupersedesInformation()


    /**
     * Returns the url if one of the baseUrls defined in BASE_URL tmf.host.SystemVariable matches the identifier.  Otherwise returns
     * null indicating no match of the base url.
     * <br/><br/>
     * @param baseUrlProp
     * @param identifier
     * @return
     */
    private String findStartingUrl(String identifier){
        String baseUrlProp = SystemVariable.quickFindPropertyValue(SystemVariable.BASE_URL);
        String[] baseUrls = null;
        if( baseUrlProp.contains(",") ){
            baseUrls = baseUrlProp.split(Pattern.quote(","));
        }else{
            baseUrls = [baseUrlProp] as String[]
        }

        for( String url : baseUrls ){
            if( identifier.toLowerCase().startsWith(url.toLowerCase()) ){
                return url;
            }
        }

        return null;
    }


    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
//        propertyInstanceMap.get().clear()
    }


}/* End tmf.host.ScanConfigJob */