package tmf.host.security

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent

/**
 * TODO: Write a description here
 * @user brad
 * @date 9/19/16
 */
class AuthFailListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    private static final Logger log = LoggerFactory.getLogger(AuthSuccessListener.class);

    @Override
    void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        log.error("AUTHENTICATION ERROR: "+event.getException().toString());
    }


}
