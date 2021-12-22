package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.Serializer
import edu.gatech.gtri.trustmark.v1_0.io.SerializerFactory
import edu.gatech.gtri.trustmark.v1_0.io.TrustInteroperabilityProfileResolver
import edu.gatech.gtri.trustmark.v1_0.io.TrustmarkDefinitionResolver
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkFrameworkIdentifiedObject
import groovy.json.JsonOutput
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants
import org.apache.commons.lang.StringUtils
import tmf.host.artifact_processing.ActionType
import tmf.host.artifact_processing.ArtifactAction
import tmf.host.artifact_processing.MemoryProcessingData

import java.text.SimpleDateFormat

/**
 * Contains the business logic to build a zip file with the entire contents of a Version Set.
 * <br/><br/>
 * @user brad
 * @date 12/5/16
 */
class BuildAllZipService extends AbstractLongRunningService {
    //==================================================================================================================
    // Grails Service Directives
    //==================================================================================================================
    static transactional = false;

    public static final String BUILD_ZIP_EXECUTING = BuildAllZipService.class.getSimpleName()+".BUILD_ZIP_EXECUTING.";
    public static final String STATUS = BuildAllZipService.class.getSimpleName()+".STATUS.";
    public static final String MESSAGE = BuildAllZipService.class.getSimpleName()+".MESSAGE.";
    public static final String PERCENTAGE = BuildAllZipService.class.getSimpleName()+".PERCENTAGE.";
    //==================================================================================================================
    // Required Services
    //==================================================================================================================

    //==================================================================================================================
    // Service Methods
    //==================================================================================================================
    /**
     * A simple method to tell you if a thread is already executing on the given version set.
     */
    public boolean isExecuting(String vsName) {
        return super.isExecuting(BUILD_ZIP_EXECUTING+vsName);
    }

    /**
     * An external controller can call this to obtain the global "lock" on processing.  This should be done
     * BEFORE ay calls to buildVersionSetZip are made.
     */
    public boolean setExecuting(String vsName) {
        return super.setExecuting(BUILD_ZIP_EXECUTING+vsName);
    }

    /**
     * Builds the zip of the given VersionSet.
     */
    public void buildVersionSetZip(String vsName){
        try {
            setStatus(vsName, "START", "Building a Zip file for VersionSet ["+vsName+"]...");
            log.info("Starting the BuildAllZipService service...");

            log.debug("Creating an output directory to put artifacts...");
            File tempFile = File.createTempFile(vsName+"_"+now()+"_", "");
            tempFile.delete(); tempFile.mkdirs();
            log.debug("Created temp directory ${tempFile.canonicalPath}")


            log.debug("Building a list of all artifacts currently in VersionSet @|cyan ${vsName}|@...")
            List tdLinkIds = []
            List tipLinkIds = []
            VersionSet.withTransaction {
                VersionSet vs = VersionSet.findByName(vsName);

                log.debug("Finding TDs...")
                List<VersionSetTDLink> tdLinks = VersionSetTDLink.findAllByVersionSet(vs);
                for( VersionSetTDLink tdLink : tdLinks ){
                    tdLinkIds.add(tdLink.id);
                }
                log.debug("Successfully found @|green ${tdLinkIds.size()}|@ TDs.")

                log.debug("Finding TIPs...")
                List<VersionSetTIPLink> tipLinks = VersionSetTIPLink.findAllByVersionSet(vs);
                for( VersionSetTIPLink tipLink : tipLinks ){
                    tipLinkIds.add(tipLink.id);
                }
                log.debug("Successfully found @|green ${tdLinkIds.size()}|@ TIPs.")
            }

            int artifactCount = tdLinkIds.size() + tipLinkIds.size();
            log.debug("Copying @|green ${artifactCount}|@ TD and TIP artifacts to the temporary directory...");
            int count = 0;
            setStatus(vsName, "COPYING", "Copying Trustmark Definitions into Zip file...", getPercent(count, artifactCount));

            count = writeTds(vsName, tempFile, tdLinkIds, artifactCount);
            int tips = writeTips(vsName, tempFile, tipLinkIds, artifactCount, count);


            setStatus(vsName, "COMPRESSING", "Creating a Zip file for VersionSet "+vsName+"...");
            File tempZip = File.createTempFile(vsName, ".zip");
            tempZip.delete();

            ZipFile zipFile = new ZipFile(tempZip);
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            zipParameters.setPassword("trustmark");
            zipFile.addFolder(tempFile, zipParameters);

            BinaryObject.withTransaction {
                BinaryObject binaryObject =
                        this.fileService.createBinaryObject(tempZip, BuildAllZipService.class.simpleName, "application/zip", "VersionSet_"+vsName+"_"+now()+".zip", "zip");
                VersionSet vs = VersionSet.findByName(vsName);
                VersionSetAll versionSetAll = new VersionSetAll(versionSet: vs, zipFile: binaryObject);
                versionSetAll.save(failOnError: true);
            }

            setStatus(vsName, "COMPLETE", "Successfully built an artifact containing ALL data from VersionSet "+vsName+".");
        }catch(Throwable t){
            fatalError(vsName, "There was an unexpected error while processing your actions!  Error Details: "+t.toString(), t);
            log.error("Error processing actions!", t);
        }
        SystemVariable.withTransaction {
            SystemVariable.storeProperty(BUILD_ZIP_EXECUTING+vsName, "false");
        }
    }//end applyChanges()


    //==================================================================================================================
    // Helper Methods
    //==================================================================================================================
    /**
     * A method which writes TIP JSON to the given directory, as well as an Index JSON file.
     */
    private Integer writeTips(String vsName, File outDir, List<Long> tipLinkIds, Integer artifactCount, int offset){
        int count = 0;
        Map indexData = [tipCount: tipLinkIds.size(), timestamp: now(), tips: []]
        File tipDir = new File(outDir, "trust-interoperability-profiles");
        tipDir.mkdirs();
        for( Long id : tipLinkIds ){
            VersionSetTIPLink.withTransaction {
                VersionSetTIPLink tipLink = VersionSetTIPLink.get(id);
                indexData.tips.add(writeTIPJson(tipLink, tipDir));
            }
            count++;
            setStatus(vsName, "COPYING", "Copying Trust Interoperability Profiles into Zip file...", getPercent(count+offset, artifactCount));
        }
        File tdIndex = new File(tipDir, "index.json");
        tdIndex << JsonOutput.toJson(indexData);
        return count;
    }

    /**
     * Writes a single TIP to a File, and returns a metadata Map about what was written (for an index file).
     */
    private Map writeTIPJson(VersionSetTIPLink tipLink, File tipDir){
        String tdFilename = makeTDFileName(tipLink.trustInteroperabilityProfile.name, tipLink.trustInteroperabilityProfile.tipVersion, tipDir, "json");
        File tipFile = new File(tipDir, tdFilename);
        log.debug("Saving TIP [@|cyan ${tipLink.trustInteroperabilityProfile.name}|@] to File [@|green ${tipFile.canonicalPath}|@]...")

        BinaryObject binaryObject = tipLink.getTrustInteroperabilityProfile().getArtifact();
        File content = binaryObject.getContent().toFile();
        TrustInteroperabilityProfileResolver resolver = FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class);
        edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip = resolver.resolve(content);

        FileWriter fileWriter = new FileWriter(tipFile);
        SerializerFactory serializerFactory = FactoryLoader.getInstance(SerializerFactory.class);
        Serializer jsonSerializer = serializerFactory.getJsonSerializer();
        jsonSerializer.serialize(tip, fileWriter);
        fileWriter.flush();
        fileWriter.close();

        return [
                Type: "TrustInteroperabilityProfile",
                Identifier: tip.getIdentifier().toString(),
                Name: tip.getName(),
                Version: tip.getVersion(),
                Description: tip.getDescription(),
                Deprecated: tip.isDeprecated(),
                Primary: tipLink.getPrimaryTIP(),
                _file: tipFile.getName()
        ];
    }


    /**
     * A method which writes TD JSON to the given directory, as well as an Index JSON file.
     */
    private Integer writeTds(String vsName, File outDir, List<Long> tdLinkIds, Integer artifactCount){
        int count = 0;
        Map indexData = [tdCount: tdLinkIds.size(), timestamp: now(), tds: []]
        File tdDir = new File(outDir, "trustmark-definitions");
        tdDir.mkdirs();
        for( Long id : tdLinkIds ){
            VersionSetTDLink.withTransaction {
                VersionSetTDLink tdLink = VersionSetTDLink.get(id);
                indexData.tds.add(writeTDJson(tdLink, tdDir));
            }
            count++;
            setStatus(vsName, "COPYING", "Copying Trustmark Definitions into Zip file...", getPercent(count, artifactCount));
        }
        File tdIndex = new File(tdDir, "index.json");
        tdIndex << JsonOutput.toJson(indexData);
        return count;
    }

    /**
     * Writes a single TD to a File, and returns a metadata Map about what was written (for an index file).
     */
    private Map writeTDJson(VersionSetTDLink tdLink, File tdDir){
        String tdFilename = makeTDFileName(tdLink.trustmarkDefinition.name, tdLink.trustmarkDefinition.tdVersion, tdDir, "json");
        File tdFile = new File(tdDir, tdFilename);
        log.debug("Saving TD [@|cyan ${tdLink.trustmarkDefinition.name}|@] to File [@|green ${tdFile.canonicalPath}|@]...")

        BinaryObject binaryObject = tdLink.getTrustmarkDefinition().getArtifact();
        File content = binaryObject.getContent().toFile();
        TrustmarkDefinitionResolver resolver = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class);
        edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td = resolver.resolve(content);

        FileWriter fileWriter = new FileWriter(tdFile);
        SerializerFactory serializerFactory = FactoryLoader.getInstance(SerializerFactory.class);
        Serializer jsonSerializer = serializerFactory.getJsonSerializer();
        jsonSerializer.serialize(td, fileWriter);
        fileWriter.flush();
        fileWriter.close();

        return [
                Type: "TrustmarkDefinition",
                Identifier: td.getMetadata().getIdentifier().toString(),
                Name: td.getMetadata().getName(),
                Version: td.getMetadata().getVersion(),
                Description: td.getMetadata().getDescription(),
                Deprecated: td.getMetadata().isDeprecated(),
                _file: tdFile.getName()
        ];
    }


    /**
     * Creates a unique in the given directory file name to use for this artifact.  Usually done by grabbing parts of
     * id given and appending the format.
     */
    private String makeTDFileName(String name, String version, File containingDir, String format){
        String fileName = makeSafe(name?.toLowerCase()) + "_" + makeSafe(version) + "." + format;
        File file = new File(containingDir, fileName);
        int count = 1;
        while( file.exists() ){
            fileName = makeSafe(name?.toLowerCase()) + "_" + makeSafe(version) + "." + count + "." + format;
            file = new File(containingDir, fileName);
            count++;
        }

        return fileName;
    }//end makeFileName

    /**
     * Makes a string into a safe filename by removing any non word characters (except hyphen).
     */
    private String makeSafe(String s){
        if( StringUtils.isNotBlank(s) ){
            return s.replaceAll("[^\\w]", "-");
        }else{
            return "_null_";
        }
    }

    /**
     * Returns the current date string in the format yyyy-MM-dd_HHmm
     */
    private String now(){
        return new SimpleDateFormat("yyyy-MM-dd_HHmm").format(Calendar.getInstance().getTime());
    }

    private void setStatus(String vs, String status, String msg){
        setStatus(vs, status, msg, -1);
    }
    private void setStatus(String vs, String status, String msg, Integer percent){
        SystemVariable.withTransaction {
            SystemVariable.storeProperty(STATUS+vs, status);
            SystemVariable.storeProperty(MESSAGE+vs, msg);
            SystemVariable.storeProperty(PERCENTAGE+vs, percent?.toString() ?: "-1");
        }
    }
    private void fatalError(String vs, String errorMessage, Throwable t){
        setStatus(vs, "ERROR", errorMessage);
        // TODO Should we do anything with this stack trace?
    }

}/* end ApplyChangesService */