package tmf.host.util

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.compressors.CompressorException
import org.apache.commons.compress.compressors.CompressorInputStream
import org.apache.commons.compress.compressors.CompressorStreamFactory

/**
 * Created by brad on 12/23/14.
 */
class ExtractionUtils {
    static Logger logger = LoggerFactory.getLogger(ExtractionUtils.class);

    /**
     * Collects all files from the given directory recursively.
     */
    public static List<File> collectAllFiles(File directory) {
        return collectAllFiles(directory, {file -> return file.isFile();} as FileFilter);
    }//end collectAllFiles()

    /**
     * Collects all files from the given directory recursively.
     */
    public static List<File> collectAllFiles(File directory, FileFilter filter) {
        List<File> files = []
        _doCollection(directory, filter, files);
        return files;
    }//end collectAllFiles()

    private static void _doCollection(File directory, FileFilter filter, List<File> collectedFiles) {
        File[] files = directory.listFiles(filter);
        files.each{ file ->
            collectedFiles.add(file);
        }
        File[] subdirectories = directory.listFiles({ File file -> return file.isDirectory(); } as FileFilter);
        subdirectories.each{ File subdir ->
            _doCollection(subdir, filter, collectedFiles);
        }
    }//end _doCollection

    /**
     * Extracts the archive to a temp directory, and returns the temp directory.
     */
    public static File extract(File archive){
        File temp = File.createTempFile("extract-", ".dir");
        temp.delete();
        if( !temp.mkdirs() ){
            logger.error("Cannot create temp directory: "+temp.canonicalPath);
            throw new UnsupportedOperationException("Cannot create temp directory: "+temp.canonicalPath);
        }
        extract(archive, temp);
        return temp;
    }

    /**
     * Extracts the given file to the given directory.  Uses the Commons Compress API to make extractions easy.
     */
    public static void extract(File archive, File directory) {
        logger.info(String.format("Performing extraction of File[@|cyan %s|@] to Directory[@|green %s|@]...", archive.name, directory.name));
        if( !FileTypeChecker.isArchive(archive) ){
            logger.error("File[@|yellow ${archive.canonicalPath}|@] is not a supported archive.");
            throw new UnsupportedOperationException("The given file is not a supported archive file, like ZIP.");
        }

        logger.debug("Opening archive input stream...");
        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(archive));
        inputStream = bufferedInputStream;
        try {
            CompressorInputStream compressorInputStream = new CompressorStreamFactory().createCompressorInputStream(inputStream);
            logger.debug(String.format("Successfully opened compressed input stream: @|cyan %s|@", compressorInputStream?.getClass().getSimpleName()));
            inputStream = new BufferedInputStream(compressorInputStream);
        }catch(CompressorException ce){
            logger.warn("Error opening compressor input stream: "+ce.toString());
        }

        ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(inputStream);
        logger.debug(String.format("Successfully opened archive input stream: @|cyan %s|@", archiveInputStream?.getClass().getSimpleName()));

        int filesExtracted = 0;
        int directoriesExtracted = 0;
        ArchiveEntry entry = null;
        while( (entry = archiveInputStream.getNextEntry()) != null ){
            if( archiveInputStream.canReadEntryData(entry) ){
                if(entry.getName().startsWith(".") || entry.getName().contains("System Volume Information") || entry.getName().contains("/.")){
                    logger.debug(String.format("Skipping directory for archive entry [@|cyan %s|@]...", entry.getName()));
                    continue;
                }
                File newFile = new File(directory, entry.getName());
                newFile.getParentFile().mkdirs(); // Ensure parent exists.
                if( entry.isDirectory() ){
                    logger.debug(String.format("Creating directory for archive entry [@|cyan %s|@]...", entry.getName()));
                    newFile.mkdirs(); // Might have been created, might not.
                    directoriesExtracted++;
                }else{
                    logger.debug(String.format("Extracting archive entry [@|cyan %s|@]...", entry.getName()));
                    FileOutputStream currentEntryOut = new FileOutputStream(newFile);
                    long amountToRead = entry.getSize();
                    byte[] buffer = new byte[5012]; // 5k bytes at a time.
                    long read = 0l;
                    while( read < amountToRead ) {
                        int currentlyRead = archiveInputStream.read(buffer);
                        read += currentlyRead;
                        currentEntryOut.write(buffer, 0, currentlyRead);
                        currentEntryOut.flush();
                    }
                    currentEntryOut.close();
                    filesExtracted++;
                }
            }else{
                logger.warn(String.format("ArchiveEntry[@|cyan %s|@] is using something that Commons Compress does not support.  Skipping...", entry.getName()));
            }
        }

        logger.info(String.format("Successfully extracted %d files in %d directories from archive %s.", filesExtracted, directoriesExtracted, archive.name));

        archiveInputStream.close();
        inputStream.close();
    }//end extract

}//end ExtractionUtils