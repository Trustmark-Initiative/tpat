package tmf.host

import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.security.DigestInputStream
import java.security.MessageDigest

/**
 * Represents any binary object in the system.
 */
class BinaryObject {

    private static final Logger logger = LoggerFactory.getLogger(BinaryObject.class);

    String checksum          // Checksum for the content for integrity purposes.
    String checksumAlgorithm // The algorithm used to calculate the given checksum
    String mimeType          // Ex, "image/png" or "application/zip"
    String originalFilename  // The original filename of the file, if given (may be null).
    String originalExtension // Ex. "png" or "zip"
    Long fileSize            // Size of the file in bytes
    Date dateCreated         // When the database entry was entered
    String createdBy         // The user responsible for this upload (may be null)

    BinaryData content       // The actual binary data.

    static constraints = {
        checksum(nullable: true, maxSize: 1024)
        checksumAlgorithm(nullable: true, maxSize: 64)
        mimeType(nullable: false, blank: false, maxSize: 128)
        originalFilename(nullable: true, blank: true, maxSize: 256)
        originalExtension(nullable: true, blank: true, maxSize: 32)
        fileSize(nullable: false)
        dateCreated(nullable: true)
        createdBy(nullable: true, blank: true, maxSize: 128)
        content(nullable: true) // Content isn't required to exist, but this object wouldn't make sense without it.
    }

    static mapping = {
        table(name:'binary_object')
        mimeType(column: 'mime_type')
        originalFilename(column: 'original_filename')
        originalExtension(column: 'original_extension')
        checksum(type: 'text')
    }

    public Map toJsonMap(boolean shallow = true){
        def json = [
            id: this.id,
            checksum: this.checksum,
            checksumAlgorithm: this.checksumAlgorithm,
            mimeType: this.mimeType,
            originalFilename: this.originalFilename,
            originalExtension: this.originalExtension,
            fileSize: this.fileSize,
            dateCreated: this.dateCreated?.getTime(),
            createdBy: this.createdBy,
            content: [
                id: this.content?.id
            ]
        ]
        return json;
    }//end toJsonMap

    public static final String CHECKSUM_ALGORITHM = "SHA-256";

    /**
     * Returns the checksum for the given file in a consistent way.  This checksum should be used to determine if a
     * file has changed or not.
     */
    public static String calculateChecksum(File file){
        try {
            MessageDigest md = MessageDigest.getInstance(CHECKSUM_ALGORITHM);
            FileInputStream is = new FileInputStream(file);
            DigestInputStream dis = new DigestInputStream(is, md)
            IOUtils.copy(dis, new org.apache.commons.io.output.NullOutputStream());
            dis.close();
            is.close();
            byte[] digest = md.digest();
            return Base64.encodeBase64String(digest);
        }catch(Throwable t){
            logger.error("ERROR creating ${CHECKSUM_ALGORITHM} checksum for file ${file.canonicalPath}!", t);
            return "-1";
        }
    }

}//end BinaryObject
