package tmf.host.util


import org.apache.commons.compress.archivers.ArchiveException
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.compressors.CompressorException
import org.apache.commons.compress.compressors.CompressorInputStream
import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Knows if a file is of a particular format.
 * <br/><br/>
 * Created by brad on 12/23/14.
 */
class FileTypeChecker {

    static Logger logger = LoggerFactory.getLogger(FileTypeChecker.class);

    /**
     * Returns true if the file is an archive supported by this system.
     */
    public static boolean isArchive(File file){
        if( !file )
            throw new NullPointerException("Required file is null.");
        String filename = file.name.toLowerCase();
        logger.debug("Examining if File[@|cyan ${file.name}|@] can be 'unarchived'...");
        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        inputStream = bufferedInputStream;
        try {
            CompressorInputStream compressorInputStream = new CompressorStreamFactory().createCompressorInputStream(inputStream);
            logger.debug("Successfully opened compressed input stream: @|cyan ${compressorInputStream?.getClass().getSimpleName()}|@");
            inputStream = new BufferedInputStream(compressorInputStream);
        }catch(CompressorException ce){
            logger.warn("Error opening compressor input stream: "+ce.toString());
        }

        try {
            ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(inputStream);
            logger.debug("Successfully opened archive input stream: @|cyan ${archiveInputStream?.getClass().getSimpleName()}|@");
            archiveInputStream.close();
            inputStream.close();
            return true;
        }catch(ArchiveException ae){
            logger.warn("Error while opening archive stream: "+ae.toString());
        }

        logger.info("File[@|cyan ${file.name}|@] is @|red not|@ a supported archive file.")
        return false;
    }//end isArchive()



}//end FileTypeChecker()
