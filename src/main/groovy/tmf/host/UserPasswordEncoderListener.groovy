package tmf.host

import grails.plugin.springsecurity.SpringSecurityService
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import grails.events.annotation.gorm.Listener
import groovy.transform.CompileStatic

@CompileStatic
class UserPasswordEncoderListener implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(UserPasswordEncoderListener.class);

    @Autowired
    SpringSecurityService springSecurityService

    @Listener(User)
    void onPreInsertEvent(PreInsertEvent event) {
        encodePasswordForEvent(event)
    }

    @Listener(User)
    void onPreUpdateEvent(PreUpdateEvent event) {
        encodePasswordForEvent(event)
    }

    private void encodePasswordForEvent(AbstractPersistenceEvent event) {
        if (event.entityObject instanceof User) {
            User u = event.entityObject as User
            if (u.password && ((event instanceof  PreInsertEvent) || (event instanceof PreUpdateEvent && u.isDirty('password')))) {
                event.getEntityAccess().setProperty('password', encodePassword(u.password))
            }
        }else{
            log.warn("EntityObject in event is not a User!  It is: "+event?.entityObject);
        }
    }

    private String encodePassword(String password) {
        String passwordEncoded = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
        if( passwordEncoded.equals(password) ){
            log.error("Error - could not successfully encode password: "+password);
        }
        return passwordEncoded
    }

    @Override
    void afterPropertiesSet() throws Exception {
        if( springSecurityService == null )
            throw new UnsupportedOperationException("Cannot configure ${getClass().simpleName} because there is no SpringSecurityService assigned!");

        log.info("Successfully configured new "+this.getClass().getSimpleName()+"!");
    }

}
