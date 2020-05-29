package tmf.host

/**
 * Created by brad on 1/22/16.
 */
class IndexController {

    /**
     * Displays the index page.
     */
    def index() {
        log.info "Displaying the index page..."

        long tdCount = 0
        long tipCount = 0

        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if( vs ){
            tdCount = VersionSetTDLink.countByVersionSet(vs)
            tipCount = VersionSetTIPLink.countByVersionSet(vs)
        }

        [tipCount: tipCount, tdCount: tdCount, versionSet: vs]
    }//end index()

}
