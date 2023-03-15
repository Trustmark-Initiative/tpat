package tmf.host;

import grails.gorm.transactions.Transactional
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.gtri.fj.data.List
import org.json.JSONArray
import org.springframework.security.core.context.SecurityContextHolder


@Transactional
public class UserService {

    public void insertOrUpdateHelper(
            final String username,
            final String nameFamily,
            final String nameGiven,
            final String contactEmail,
            final List<String> roleList) {

        User.withTransaction {
            User user = User.findByUsernameHelper(username).orSome(new User());
            user.setUsername(username);
            user.setNameFamily(nameFamily);
            user.setNameGiven(nameGiven);
            user.setContactEmail(contactEmail);

            user.setRoleArrayJson(new JSONArray(roleList.toList()).toString());

            user.saveAndFlushHelper();
        };
    }

    boolean isLoggedIn() {
        return SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                &&
                //when Anonymous Authentication is enabled
                !(SecurityContextHolder.getContext().getAuthentication()
                        instanceof AnonymousAuthenticationToken)
    }
}
