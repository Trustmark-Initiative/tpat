package tmf.host;

import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.springframework.web.multipart.MultipartFile

import javax.servlet.ServletException

/**
 * Handles uploads and administrative tasks.
 * <br/><br/>
 * Created by brad on 9/8/14.
 */
@Secured("permitAll()")
class BinaryController {

    SpringSecurityService springSecurityService;
    FileService fileService;

    def list() {
        throw new ServletException("NOT YET IMPLEMENTED");
    }//end list()

    def view() {
        log.debug("view binary called...")

        User user = springSecurityService.currentUser;

        if( StringUtils.isBlank(params.id) ){
            log.warn "Missing required parameter id"
            throw new ServletException("Missing required parameter: 'id")
        }

        BinaryObject binaryObject = BinaryObject.findById(params.id);
        if( !binaryObject ){
            log.warn("No such binary: ${params.id}")
            throw new ServletException("No such binary: ${params.id}")
        }

        log.debug("User @|yellow ${user?.username}|@ is downloading binary[id=@|cyan ${binaryObject.id}|@, name=@|green ${binaryObject.originalFilename}|@]...");

        response.setHeader("Content-length", binaryObject.fileSize.toString())
        response.setHeader("Content-Disposition", "attachment; filename= ${URLEncoder.encode(binaryObject.originalFilename ?: "", "UTF-8")}")
        String mimeType = binaryObject.mimeType;
        if( mimeType == "text/xhtml" ){
            mimeType = "text/html"; // A hack around XHTML display in browsers.
        }
        response.setContentType(mimeType);
        File outputFile = binaryObject.content.toFile();
        FileInputStream fileInputStream = new FileInputStream(outputFile);

        log.info("Rendering binary data...")
        return render(file: fileInputStream, contentType: mimeType);
    }//end view()

    /**
     * Handles a file upload.  Will work with Chunked Plupload style uploads, as well as legacy form upload data.
     * <Br/><br/>
     * Parameters:
     *   <b>chunks</b> - Total number of chunks being uploaded, integer value.
     *   <b>chunk</b> - The current chunk being uploaded, integer value.
     *   <b>name</b> - The name of the file being uploaded.
     *   <b>file</b> - The multipart file being uploaded.
     */
    @Secured("permitAll()")
    def upload() {
        User user = springSecurityService.getCurrentUser();

        log.info("Handling file upload...");
        MultipartFile file = request.getFile("file");
        if( !file ) {
            log.error("Missing required 'file' parameter with multipart file data.")
            throw new ServletException("Missing required Multipart file in 'file' parameter.")
        }
        if( StringUtils.isBlank(params.chunks) )
            params.chunks = "1";
        if( StringUtils.isBlank(params.chunk) )
            params.chunk = "1";
        if( StringUtils.isBlank(params.name) )
            params.name = file.getName()

        int chunk = Integer.parseInt(params.chunk);
        int chunks = Integer.parseInt(params.chunks);

        log.info("Handling Chunk #$chunk of $chunks, filename: ${params.name}")

        // We assume that the name is unique enough to be a key (for the time the file is being uploaded only)
        String sessionAttributeName = "FILE_UPLOAD_${params.name.hashCode()}";
        def fileData = null
        synchronized (session) {
            fileData = session.getAttribute(sessionAttributeName);
        }
        if( !fileData )
            fileData = initializeFileUpload(file, params);

        File tempDirectory = new File(fileData.tempDir);
        if( !tempDirectory.exists() ){
            log.error("Could not find file upload temp directory: ${fileData.tempDir}.  Was it deleted somehow?")
            throw new ServletException("Could not find file upload temp directory: ${fileData.tempDir}")
        }

        File chunkFile = new File(tempDirectory, "chunk."+chunk);
        log.debug("Copying data to chunk file: ${chunkFile.canonicalPath}")
        file.transferTo(chunkFile);
        fileData.chunkFiles.put( chunk, chunkFile.canonicalPath );
        synchronized (session) {
            session.setAttribute(sessionAttributeName, fileData);
        }

        String statusMessage = "File Chunk ${chunk} stored successfully."
        int binaryId = -1;
        if( allChunksReceived(fileData) ){
            log.info("Successfully RECEIVED ALL CHUNK FILES!!");
            log.debug("Reconstructing file...");
            File reconstructedFile = new File(tempDirectory, fileData.name);
            for( int i = 0; i < fileData.chunks; i++ ){
                File currentChunkFile = new File(tempDirectory, "chunk."+i);
                FileOutputStream fileOutputStream = FileUtils.openOutputStream(reconstructedFile, true);
                FileInputStream fileInputStream = FileUtils.openInputStream(currentChunkFile);
                IOUtils.copy(fileInputStream, fileOutputStream);
                fileInputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();
            }

            BinaryObject binaryObject = fileService.createBinaryObject(user, params, file, reconstructedFile);
            log.debug("Successfully created binary: ${binaryObject.id}")
            binaryId = binaryObject.id;
            statusMessage = "File [${binaryObject.originalFilename}, size: ${binaryObject.fileSize}] stored successfully.  ${binaryObject.checksumAlgorithm} CHECKSUM: ${binaryObject.checksum}"

            synchronized (session) {
                session.removeAttribute(sessionAttributeName);
            }
        }

        log.debug("Returning successful response.")
        def responseVal = [OK: 1, message: statusMessage, binaryId: binaryId]

        withFormat {
            html {
                throw new ServletException("NOT YET IMPLEMENTED")
            }
            xml {
                render responseVal as XML
            }
            json {
                render responseVal as JSON
            }
        }
    }//end upload()

    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================

    private boolean allChunksReceived(Map fileData){
        log.debug("Checking if ${fileData.chunkFiles.keySet().size()} is equal to ${fileData.chunks}");
        return fileData.chunkFiles.keySet().size() == fileData.chunks;
    }

    /**
     * When the first chunk of a file upload comes in, this method is called to create a place on the filesystem to hold
     * it, as well as store metadata about it.  The method will also create the session data structured used to keep
     * up with this file upload.
     */
    private Map initializeFileUpload( MultipartFile file, Map params ){
        // This is the first mention of this file.
        File tempDirectory = File.createTempFile("upload-", ".dir")
        tempDirectory.delete();
        tempDirectory.mkdirs();

        def fileData = [
                name: params.name,
                chunks: Integer.parseInt(params.chunks),
                tempDir: tempDirectory.canonicalPath,
                chunkFiles: [:]
        ]

        File metadataFile = new File(tempDirectory, "upload.meta.xml");
        metadataFile << """<?xml version="1.0"?>

<fileData>
    <name>${params.name}</name>
    <size>${file.size}</size>
    <contentType>${file.contentType}</contentType>
    <originalFilename>${file.originalFilename}</originalFilename>
    <chunks>${params.chunks}</chunks>
</fileData>

"""

        return fileData;
    }//end initializeFileUpload()

}//end BinaryController