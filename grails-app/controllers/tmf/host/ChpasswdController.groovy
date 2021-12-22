package tmf.host

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.core.io.Resource

import java.nio.file.Path

@Secured("ROLE_ADMIN")
class ChpasswdController {

    SpringSecurityService springSecurityService

    Resource resource
    Path bannerPath

    def assetResourceLocator

    def index() {
        log.debug("Showing chpasswd page...")
        [users: User.findAll()]
    }

    /**
     * handles the administrator password update
     * @return
     */
    def adminPswd() {
        User user = springSecurityService.currentUser
        log.debug("User @|green ${user.username}|@ called adminpswd @|cyan -> ${params.origPswd}")

        def status = [
            rc: 'success',
            message: 'Password updated!'
        ]

        if(params.newPswd == params.origPswd)  {
            status.rc = 'fail'
            status.message = 'Passwords are unchanged!'
        } else if(params.newPswd != params.renewPswd)  {
            status.rc = 'fail'
            status.message = 'Passwords do not match!'
        } else {
            user.password = params.newPswd
            log.debug("User @|green ${user.username}|@ saved @|cyan -> ${user.name} ${user.password}")
            User.withTransaction {
                user.save(failOnError: true)
            }
        }
        render status as JSON
    }

}
