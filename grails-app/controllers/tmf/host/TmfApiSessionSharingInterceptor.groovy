package tmf.host

import edu.gatech.gtri.trustmark.v1_0.io.SessionResolver
import grails.artefact.Interceptor
import grails.plugin.springsecurity.SpringSecurityService
import org.apache.commons.lang.StringUtils

/**
 * If the user is logged in, this session sharing interceptor makes sure that any underlying TMF API Calls will share
 * the user's current session identifier.
 * <br/><br/>
 * @user brad
 * @date 12/14/16
 */
class TmfApiSessionSharingInterceptor implements Interceptor {

    SpringSecurityService springSecurityService;

    int order = HIGHEST_PRECEDENCE - 20; // Called directly after the Url Printer.

    public TmfApiSessionSharingInterceptor(){
        matchAll()
    }

    /**
     * If the user is logged in, then their session id is set in the {@link edu.gatech.gtri.trustmark.v1_0.io.SessionResolver}
     * ThreadLocal scope variable.
     */
    boolean before() {
        if( springSecurityService.isLoggedIn() ){
            def session = request.getSession(true); // Force create a session
            SessionResolver.setSessionResolver(session.id);
        }
        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }



}
