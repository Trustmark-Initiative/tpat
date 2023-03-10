package tmf.host

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource

@SpringBootApplication
@ComponentScan("tmf.host.security")
class Application extends GrailsAutoConfiguration implements EnvironmentAware {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment env) {
        def res = getClass().classLoader.getResourceAsStream('tpat_config.properties')
        if( res ) {
            Properties props = new Properties()
            props.load(res)
            env.propertySources.addFirst( new MapPropertySource('tpat_config.properties', props) )
        }
    }

}