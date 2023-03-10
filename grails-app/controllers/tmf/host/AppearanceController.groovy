package tmf.host

import grails.converters.JSON
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.multipart.MultipartFile
import tmf.host.util.TFAMPropertiesHolder

import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@PreAuthorize('hasAuthority("tpat-admin")')
class AppearanceController {


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

    def sysDefault() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())

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
