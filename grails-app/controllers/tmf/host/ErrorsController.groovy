package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.HttpResponse
import edu.gatech.gtri.trustmark.v1_0.io.NetworkDownloader
import grails.converters.JSON
import grails.converters.XML
import org.apache.commons.lang.StringUtils
import tmf.host.util.TFAMPropertiesHolder

import javax.servlet.ServletException
import javax.servlet.http.HttpServletResponse

class ErrorsController {

    def baseUrlNotMatch() {
        String baseUrl = request.session.getAttribute("BASE_URL");
        log.info("Displaying that base URL ${baseUrl} is not accepted.")

        withFormat {
            html {
                [currentBaseUrl: baseUrl, baseUrlList: TFAMPropertiesHolder.baseURLsAsStrings]
            }
            json {
                def responseMap = [status: 'FAILURE', message: 'Base URL is not accepted', baseUrl: baseUrl, acceptedList: TFAMPropertiesHolder.baseURLsAsStrings]
                render responseMap as JSON
            }
            xml {
                def responseMap = [status: 'FAILURE', message: 'Base URL is not accepted', baseUrl: baseUrl, acceptedList: TFAMPropertiesHolder.baseURLsAsStrings]
                render responseMap as XML
            }
        }
    }

    def notFound() {
        String uri = request.getAttribute('javax.servlet.forward.request_uri')
        if( log.isDebugEnabled() )
            log.debug("Not Found Processing URI: "+uri);

        log.info("Page Not Found: ${uri}");
        withFormat {
            html {}
            json {
                return render(contentType: 'application/json') {
                    [
                            status: 404,
                            message: 'Page Not Found',
                            missing_uri: uri
                    ]
                }
            }
        }
    }//end notFound()


    def servletError() {
        log.info("Servlet Error");
        Throwable t = request.getAttribute('javax.servlet.error.exception');
        Throwable cause = null;
        if( t != null ){
            cause = t.getCause();
            while( cause?.getCause() != null )
                cause = cause.getCause();
            log.error("ERROR 500: [${t.toString()}] caused by [${cause?.toString()}]")
        }else{
            log.error("ERROR 500: [${t.toString()}]")
        }
        withFormat {
            html{
                log.info("Displaying error as HTML...");
                [exception: t]
            }
            json{
                log.info("Displaying error as JSON...");
                Map response = [
                        status: 500,
                        message: t?.getMessage() ?: 'An unknown error has occurred',
                ]
                if( t != null ){
                    Map errorJson = [
                            className: t.getClass().getName(),
                            message: t.getMessage(),
                            stackTrace: stacktraceToJson(t)
                    ]
                    if( cause != null ){
                        Map causeJson = [
                                className: cause.getClass().getName(),
                                message: cause.getMessage(),
                                stackTrace: stacktraceToJson(cause)
                        ]
                        errorJson.put("cause", causeJson);
                    }
                    response.put("error", errorJson);
                }
                return render(contentType: 'application/json') {
                    response
                }
            }
        }
    }

    def test500Error() {
        log.info("Testing the 500 internal servlet error...")
        throw new ServletException("This error was planned, please disregard it.")
    }


    private List stacktraceToJson(Throwable t){
        List elements = []
        for( StackTraceElement stackTraceElement : t.getStackTrace() ){
            elements.add(stacktraceAsJson(stackTraceElement));
        }
        return elements;
    }
    private Map stacktraceAsJson(StackTraceElement ste){
        return [
                className: ste.getClassName(),
                line: ste.getLineNumber(),
                fileName: ste.getFileName(),
                methodName: ste.getMethodName()
        ]
    }

    private String getStacktraceDump(Throwable t){
        StringWriter stringWriter = new StringWriter();
        PrintWriter pw = new PrintWriter(stringWriter);
        t.printStackTrace(pw);
        pw.flush();
        stringWriter.flush();
        stringWriter.close();
        return stringWriter.toString();
    }
}