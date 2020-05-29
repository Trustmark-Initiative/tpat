package tmf.host.security

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.security.core.Authentication

import javax.servlet.http.HttpServletRequest

/**
 * A class for implementing custom methods for use in TFAM @Secured annotations used to secure methods.
 * <br/><br/>
 * @author brad
 * @date 3/1/17
 */
class TfamSecurity {

    static Logger log = LoggerFactory.getLogger(TfamSecurity.class);

    public boolean hasLock(Authentication auth, HttpServletRequest request) {
        log.info("Calling TfamSecurity.hasLock(${auth.principal.username}, ${request.getServletPath()})!")

        return true;
    }


}
