package tmf.host

import groovy.json.JsonOutput
import org.apache.commons.lang.StringUtils
import org.springframework.context.MessageSource

/**
 * Provides common methods to the long running processes.
 * <br/><br/>
 * @user brad
 * @date 12/20/16
 */
abstract class AbstractLongRunningService {

    public static final String SYNC_VAR = "SYNC";

    MessageSource messageSource;
    FileService fileService;

    /**
     * A simple method to tell you if a thread is already executing.
     */
    protected boolean isExecuting(String property) {
        SystemVariable.withTransaction {
            String value = SystemVariable.quickFindPropertyValue(property);
            if (StringUtils.isBlank(value))
                value = "false";
            return Boolean.parseBoolean(value);
        }
    }

    protected boolean setExecuting(String property) {
        synchronized (SYNC_VAR){
            if( isExecuting(property) ){
                return false;
            }else{
                SystemVariable.withTransaction {
                    SystemVariable.storeProperty(property, "true");
                }
                return true;
            }
        }
    }

    protected void stopExecuting(String property) {
        SystemVariable.withTransaction {
            SystemVariable.storeProperty(property, "false");
        }
    }

    protected void setStatus(Class c, String status, String msg){
        SystemVariable.withTransaction {
            SystemVariable.storeProperty(c.simpleName.toString()+".STATUS", status);
            SystemVariable.storeProperty(c.simpleName.toString()+".MESSAGE", msg);
        }
    }
    protected void setStatus(Class c, String status, String msg, Integer percent){
        SystemVariable.withTransaction {
            SystemVariable.storeProperty(c.simpleName.toString()+".STATUS", status);
            SystemVariable.storeProperty(c.simpleName.toString()+".MESSAGE", msg);
            SystemVariable.storeProperty(c.simpleName.toString()+".PERCENTAGE", percent);
        }
    }
    protected void fatalError(Class c, String errorMessage, Throwable t){
        SystemVariable.withTransaction {
            SystemVariable.storeProperty(c.simpleName.toString()+".STATUS", "ERROR");
            SystemVariable.storeProperty(c.simpleName.toString()+".MESSAGE", errorMessage);
            SystemVariable.storeProperty(c.simpleName.toString()+".PERCENTAGE", "-1");
        }
    }


    public static int getPercent(int index, int total){
        double d = ( ((double) index) / ((double) total) ) * 100.0d;
        return (int) Math.floor(d);
    }


    protected String buildStacktraceJson(Throwable t){
        Map json = [
                className: t.getClass().getName(),
                message: t.getMessage(),
                stacktrace: stacktraceToString(t)
        ]
        Throwable cause = t.getCause();
        if( cause ){
            while( cause.getCause() != null )
                cause = cause.getCause();

            json.put("cause", [
                    className: cause.getClass().getName(),
                    message: cause.getMessage(),
                    stacktrace: stacktraceToString(cause)
            ])
        }
        return JsonOutput.toJson(json);
    }

    protected String stacktraceToString(Throwable t){
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        sw.flush();
        return sw.toString();
    }

}//end AbstractLongRunningService