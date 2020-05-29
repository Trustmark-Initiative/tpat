package tmf.host

import grails.plugin.springsecurity.SpringSecurityService
import org.apache.commons.lang.StringUtils

/**
 * On all incoming requests, asserts that a Version Set name is set in the appropriate session parameter.
 * <br/><br/>
 * @user brad
 * @date 12/14/16
 */
class VersionSetSelectingInterceptor implements Interceptor {

    public static final String VERSION_SET_NAME_ATTRIBUTE = VersionSetSelectingInterceptor.class.name + ".VERSION_SET_NAME";
    public static final String VERSION_SET_NAME_PARAM = "VERSION_SET_NAME";

    SpringSecurityService springSecurityService;

    int order = HIGHEST_PRECEDENCE - 10; // Called directly after the Url Printer.

    public VersionSetSelectingInterceptor(){
        matchAll()
    }

    /**
     * we need to allow the user to be granted a 1-time token and it work once, followed by a clear after the view renders.
     * This is so that viewing TIP Trees in production actually works.
     */

    boolean before() {
        def session = request.getSession(true) // Force create a session
        if( session.getAttribute(VERSION_SET_NAME_ATTRIBUTE) != null && StringUtils.isBlank(params[VERSION_SET_NAME_PARAM]) ) {
            log.debug("Skipping VersionSetSelectingInterceptor[session=@|green ${session.getAttribute(VERSION_SET_NAME_ATTRIBUTE)}|@] [params[${VERSION_SET_NAME_PARAM}]=@|yellow ${params[VERSION_SET_NAME_PARAM]}|@]...")
            return true // Since a verison set is in session and not being overwritten, do nothing but return real quick.
        }
        VersionSet.withTransaction {
            log.debug("Setting a VersionSet in session scope...")
            VersionSet vs = null
            if (params[VERSION_SET_NAME_PARAM]) {
                log.debug("User has sent '@|green ${params[VERSION_SET_NAME_PARAM]}|@' in parameter '@|cyan ${VERSION_SET_NAME_PARAM}|@', searching for it...")
                String versionSetName = params[VERSION_SET_NAME_PARAM]?.toString().trim()
                vs = VersionSet.findByName(versionSetName)
                if (!vs) {
                    log.warn("Invalid version set name @|yellow ${versionSetName}|@ given.  Cannot switch version sets to that.")
                }
            }

            if (vs && (vs.production || (!vs.production && springSecurityService.isLoggedIn()))) {
                // For now, just having any account means you can set the version set.
                log.debug("User[@|cyan ${springSecurityService.currentUser?.username}|@] is switching to version set @|green ${vs.name}|@...")
                session.setAttribute(VERSION_SET_NAME_ATTRIBUTE, vs.name)
            } else if (vs) {
                log.warn("IP address @|red ${request.remoteAddr}|@ is requesting VersionSet @|yellow ${vs?.name}|@, but they do not have a current session!  Ignoring this request.")
                vs = null
            }

            if (!session.getAttribute(VERSION_SET_NAME_ATTRIBUTE)) {
                VersionSet productionVs = VersionSet.findByProduction(true)
                log.debug("No version set is in session scope, setting it to production[@|green ${productionVs?.name}|@]...")
                session.setAttribute(VERSION_SET_NAME_ATTRIBUTE, productionVs?.name)
            }
        }

        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
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
