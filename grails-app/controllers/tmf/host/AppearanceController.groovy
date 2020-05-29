package tmf.host

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import tmf.host.util.TFAMPropertiesHolder

import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Secured("ROLE_ADMIN")
class AppearanceController {

    SpringSecurityService springSecurityService

    final String BANNER_PATH = "grails-app/assets/images/tmi-header.png"
    final String APPLICATION_TITLE= "tf.org.organization"

    Resource resource
    Path bannerPath

    def assetResourceLocator

    def index() {
        log.debug("Showing appearance page...")
        [users: User.findAll()]
    }

    def tddefaults() {
        log.debug("Showing TD defaults...")
        [sysvars: DefaultVariable.findAll()]
    }

    def tipdefaults() {
        log.debug("Showing TIP defaults...")
        [sysvars: DefaultVariable.findAll()]
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

    def sysDefault() {
        User user = springSecurityService.currentUser
        log.debug("User @|green ${user.username}|@ called systemDefault @|cyan -> ${params.name} ${params.value}")

        def status = [
                rc: 'success',
                message: params.title +' updated!'
        ]

        if(params.name == null || params.name == "")  {
            status.rc = 'fail'
            status.message = 'Default name cannot be blank!'
        } else if(params.value == null || params.value == "")  {
            status.rc = 'fail'
            status.message = 'Default value cannot be blank!'
        } else if(params.id == null || params.id == "")  {
            status.rc = 'fail'
            status.message = 'Default id cannot be blank!'
        } else {
            DefaultVariable sysvar = DefaultVariable.get(params.id)
            sysvar.name = params.name
            sysvar.fieldValue = params.value

            log.debug("User @|green ${user.name}|@ saved @|cyan -> ${sysvar.name} ${sysvar.fieldValue}")
            DefaultVariable.withTransaction {
                sysvar.save(failOnError: true)
            }
        }
        render status as JSON
    }

}
