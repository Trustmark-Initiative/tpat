package tmf.host

import grails.gorm.transactions.Transactional
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import org.springframework.web.multipart.MultipartFile

import java.security.DigestInputStream
import java.security.MessageDigest

@Transactional
class FileService {

    BinaryObject createBinaryObject(User user, MultipartFile file, File actualContent) {
        throw new UnsupportedOperationException("NOT YET IMPLEMENTED")
    }

    BinaryObject createBinaryObject(User user, Map requestData, MultipartFile file, File actualContent) {
        log.info("Creating binary object for file[${file.originalFilename}, ${file.size} bytes]...")
        String originalFilename = requestData.name
        String extension = parseFileExtension(originalFilename);
        String mimeType = getMimeByFileExtension(extension);
        return createBinaryObject(actualContent, user.username, mimeType, originalFilename, extension);
    }//end createBinaryObject

    /**
     * Stores just a file and user given string createdBy.
     */
    BinaryObject createBinaryObject(File file, String createdBy){
        String extension = parseFileExtension(file.name)
        String mimeType = getMimeByFileExtension(extension)

        return createBinaryObject(file, createdBy, mimeType, file.name, extension);
    }//end createBinaryObject()


    BinaryObject createBinaryObject(File file, String createdBy, String mimeType, String originalFilename, String originalExtension) {
        log.info("Creating binary object for file[${originalFilename}, ${file.length()} bytes]...")
        String checksum = digestFile(file);

        log.debug("Creating BinaryObject in database...")
        BinaryObject binaryObject = new BinaryObject();
        binaryObject.createdBy = createdBy
        binaryObject.fileSize = file.length()
        binaryObject.mimeType = mimeType
        binaryObject.originalExtension = originalExtension
        binaryObject.originalFilename = originalFilename
        binaryObject.checksum = checksum;
        binaryObject.checksumAlgorithm = BinaryObject.CHECKSUM_ALGORITHM;
        binaryObject.save(failOnError: true);

        log.debug("Creating BinaryData object in database...")
        BinaryData data = new BinaryData(binaryObject: binaryObject);
        data.setContent(file);
        data.save(failOnError: true);

        binaryObject.setContent(data);
        binaryObject.save(failOnError: true);

        return binaryObject;
    }


    /**
     * Simplest of all create methods, this one simply uses a file and nothing else.
     */
    BinaryObject createBinaryObject(File file){
        log.info("Creating binary object for file[${file.name}, ${file.length()} bytes]...")
        String extension = parseFileExtension(file.name)
        String mimeType = getMimeByFileExtension(extension)
        String checksum = digestFile(file);

        log.debug("Creating BinaryObject in database...")
        BinaryObject binaryObject = new BinaryObject();
        binaryObject.createdBy = "unknown"
        binaryObject.fileSize = file.length()
        binaryObject.mimeType = mimeType
        binaryObject.originalExtension = extension
        binaryObject.originalFilename = file.name
        binaryObject.checksum = checksum;
        binaryObject.checksumAlgorithm = BinaryObject.CHECKSUM_ALGORITHM;
        binaryObject.save(failOnError: true);

        log.debug("Creating BinaryData object in database...")
        BinaryData data = new BinaryData(binaryObject: binaryObject);
        data.setContent(file);
        data.save(failOnError: true);

        binaryObject.setContent(data);
        binaryObject.save(failOnError: true);

        return binaryObject;
    }//end createBinaryObject()


    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================

    private String parseFileExtension(String filename){
        int lastIndexOfDot = filename.lastIndexOf('.');
        if( lastIndexOfDot > 0 ){
            return filename.substring(lastIndexOfDot+1)?.toLowerCase();
        }
        return null;
    }//end parseFileExtension

    public static Map<String, String> EXTENSION_MIME_MAPPINGS = [
            "xml" : "application/xml",
            "json" : "application/json",
            "zip" : "application/zip",
            "txt" : "text/plain",
            "png" : "image/png",
            "jpg" : "image/jpg",
            "jepg" : "image/jpg",
            "gif" : "image/gif",
            "tgz" : "application/tar+gzip",
            "dwf" : "application/dwf",
            "pdf" : "application/pdf"
    ]

    private String getMimeByFileExtension(String extension){
        if( EXTENSION_MIME_MAPPINGS.containsKey(extension) )
            return EXTENSION_MIME_MAPPINGS.get(extension);
        return "application/octet-stream"
    }//end getMimeByFileExtension()


    private String digestFile(File file){
        return BinaryObject.calculateChecksum(file);
    }//end digestFile

}//end FileService
