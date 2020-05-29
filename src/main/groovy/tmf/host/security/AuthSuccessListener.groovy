package tmf.host.security

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AuthenticationSuccessEvent

/**
 * TODO: Write a description here
 * @user brad
 * @date 9/19/16
 */
class AuthSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private static final Logger log = LoggerFactory.getLogger(AuthSuccessListener.class);

    @Override
    void onApplicationEvent(AuthenticationSuccessEvent event) {
        log.error("AUTHENTICATION SUCCESS: "+event);
    }

}
