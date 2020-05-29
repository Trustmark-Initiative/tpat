package tmf.host

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.io.FileUtils

import javax.servlet.ServletException
import java.text.SimpleDateFormat

/**
 * This controller enables an endpoint which allows a client to download ALL data from the current version set at once,
 * as a zip file.  A slow running operation, there are a few endpoints (one to initiate, one to get status, and one to
 * download the final product).  The system attempts to be "smart" about building these zip files, as they can be pretty
 * large.  Thus, they are cached and reused whenever possible, and checks on the database are compared with timestamps
 * as well.
 */
class DownloadAllController {

    BuildAllZipService buildAllZipService;

    @Secured("ROLE_ADMIN")
    def index() {
        log.info("Displaying download all index page...");
        List<VersionSet> versionSets = VersionSet.findAll([sort: 'name']);
        [versionSets: versionSets]
    }

    /**
     * Called to build a new Zip file for the current {@link VersionSet}.  If one is already being built, this method
     * does NOT build another, it simply says successfully started building as though it started it.
     */
    def build() {
        VersionSet vs = VersionSet.findByName(params.id);
        if( !vs ){
            vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE));
        }
        if( !vs ){
            throw new ServletException("Missing Required Parameter for version set name.")
        }

        log.info("Request to build a zip file of all content for VersionSet[@|green ${vs.name}|@]...")

        // TODO We may not need to build a new zip file.  Best to run a few queries comparing the last create date of
        //      a VersionSetAll object vs. the latest update date of a TD or TIP Link.


        if( this.buildAllZipService.setExecuting(vs.name) ){
            log.info("Kicking off a new BuildAllZipService.buildVersionSetZip(@|green ${vs.name}|@)...")
            Thread t = new Thread({
                buildAllZipService.buildVersionSetZip(vs.name);
            } as Runnable);
            t.setName("DownloadAll_"+vs.name);
            t.start();
        }else{
            // It is already executing.
            log.debug("VersionSet[@|green ${vs.name}|@] already has a Zip build executing.");
        }
        def response = [status: "SUCCESS", message: "Successfully started building a Zip File.", monitorUrl: createLink(controller:'downloadAll', action:'monitor', id: vs.name, absolute: true)]

        render response as JSON
    }//end notFound()

    /**
     * Checks the database for status information and returns it as JSON.
     */
    def monitor() {
        log.debug("Monitor called for VS[@|cyan ${params.id}|@]")
        VersionSet vs = VersionSet.findByName(params.id);
        if( !vs ){
            throw new ServletException("Missing Required Parameter for version set name.")
        }

        def status = [:]
        log.debug("Returning build zip file status...");
        String executing = SystemVariable.quickFindPropertyValue(BuildAllZipService.BUILD_ZIP_EXECUTING+vs.name);
        log.debug("Executing: "+executing);
        status.put("executing", executing ? Boolean.parseBoolean(executing) : false);
        status.put("status", SystemVariable.quickFindPropertyValue(BuildAllZipService.STATUS+vs.name));
        status.put("message", SystemVariable.quickFindPropertyValue(BuildAllZipService.MESSAGE+vs.name));
        status.put("percentage", Integer.parseInt(SystemVariable.quickFindPropertyValue(BuildAllZipService.PERCENTAGE+vs.name) ?: '-1'));

        if( status.status?.equalsIgnoreCase('COMPLETE') ){
            status.put("latestDownloadInfoURL", createLink(absolute: true, controller: 'downloadAll', action: 'getLatestDownload', id: vs.name, params: [format: 'json']));
        }

        render status as JSON;
    }

    def getLatestDownload() {
        boolean warn = false;
        VersionSet vs = VersionSet.findByName(params.id);
        if( !vs ){
            log.warn("Could not find any version set '${params.id}', using latest...")
            warn = true;
            vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE));
        }
        if( !vs ){
            throw new ServletException("Missing Required Parameter for version set name.")
        }

        log.info("Finding latest downlaod all ZIP for VersionSet[@|green ${vs.name}|@]...")

        Map response = null;
        List<VersionSetAll> zipList = VersionSetAll.findAllByVersionSet(vs, [sort: 'dateCreated', order: 'desc', max: 1, offset: 0]);
        if( zipList?.size() > 0 ){
            VersionSetAll latestAll = zipList.get(0);
            def url = createLink(absolute: true, controller: 'binary', action: 'view', id: latestAll.zipFile.id)
            response = [
                    status: "SUCCESS",
                    message: "The latest ZIP file is available from: "+url,
                    url: url,
                    versionSet: [name: vs.name],
                    size: latestAll.zipFile.fileSize,
                    humanSize: FileUtils.byteCountToDisplaySize(latestAll.zipFile.fileSize),
                    createDate: new SimpleDateFormat("yyyy-MM-dd HH:mm").format(latestAll.zipFile.dateCreated)
            ]
        }else{
            response = [
                    status: "FAILURE",
                    message: "There is no download available.",
                    versionSet: [name: vs.name]
            ]
        }

        if( warn )
            response.put("warning", "The version set with id '${params.id}' could not be found, so the latest[${vs.name}] was used instead.");

        render response as JSON
    }

}//end DownloadAllController