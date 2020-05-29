package tmf.host

import grails.plugin.springsecurity.annotation.Secured

/**
 * TODO: Write a description here
 * @user brad
 * @date 9/19/16
 */
class SecureTestController {

    def springSecurityService

    @Secured("isAuthenticated()")
    def index() {
        log.debug("Displaying secured page...");
        [user: springSecurityService.currentUser]
    }

}
