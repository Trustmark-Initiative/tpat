package tmf.host

import tmf.host.util.TFAMPropertiesHolder

import javax.servlet.http.HttpServletRequest

/**
 * If the user is logged in, this session sharing interceptor makes sure that any underlying TMF API Calls will share
 * the user's current session identifier.
 * <br/><br/>
 * @user brad
 * @date 12/14/16
 */
class BaseUrlCheckInterceptor implements Interceptor {

    int order = HIGHEST_PRECEDENCE - 15; // Called directly after the Url Printer.

    public BaseUrlCheckInterceptor(){
        matchAll()
    }

    /**
     * If the user is logged in, then their session id is set in the {@link edu.gatech.gtri.trustmark.v1_0.io.SessionResolver}
     * ThreadLocal scope variable.
     */
    boolean before() {
        if( controllerName.equalsIgnoreCase('errors') ){
            return true; // We don't check this one.
        }

        synchronized (request.session){
            Boolean b = (Boolean) request.session.getAttribute("FOUND_BASE_URL_MATCH");
            if( b != null && b.booleanValue() ){
                return true;
            }
        }

        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if( isNotStandardPort(request) )
            baseUrl += ":"+request.getServerPort();
        baseUrl += request.getContextPath();
        log.debug("Base URL calculated to be: "+baseUrl);

        synchronized (request.session) {
            request.session.setAttribute("BASE_URL", baseUrl);
        }

        log.debug("Checking base URL...")
        if( !isAcceptedBaseUrl(baseUrl) ){
            redirect(controller: 'errors', action: 'baseUrlNotMatch')
            return false;
        }else{
            synchronized (request.session){
                request.session.setAttribute("FOUND_BASE_URL_MATCH", Boolean.TRUE);
            }
            return true
        }
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }

    private boolean isAcceptedBaseUrl(String baseUrl){
        for( String cur : TFAMPropertiesHolder.baseURLsAsStrings ){
            if( cur.equalsIgnoreCase(baseUrl) || cur.equalsIgnoreCase(baseUrl+"/") ){
                log.info("BaseURL[${baseUrl}] matches [${cur}], accepting...")
                return true;
            }
        }

        return false;
    }


    private boolean isNotStandardPort(HttpServletRequest request){
        boolean matchesHttps = matchesSchemeAndPort(request, "https", 443);
        boolean matchesHttp = matchesSchemeAndPort(request, "http", 80);
        boolean isStandardPort = matchesHttps || matchesHttp;
        log.info("Is Incoming Request Standard Port = [${isStandardPort}], [matchesHttps = ${matchesHttps}, matcheshttp = ${matchesHttp}]")
        return !isStandardPort;
    }

    private boolean matchesSchemeAndPort(HttpServletRequest request, String scheme, Integer port){
        boolean matches = request.getScheme().equalsIgnoreCase(scheme) && request.getServerPort() == port;
        log.info("Does request[scheme=${request.getScheme()}, port=${request.getServerPort()}] match default[scheme=${scheme}, port=${port}]? answer = ${matches}")
        return matches;
    }

    @Override
    Object beforeInvoke(Object o, String s, Object[] objects) {
        return null
    }

    @Override
    Object afterInvoke(Object o, String s, Object[] objects, Object o1) {
        return null
    }

    @Override
    boolean doInvoke() {
        return false
    }
}
