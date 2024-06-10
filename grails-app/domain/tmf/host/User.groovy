package tmf.host

import org.apache.commons.lang.StringUtils
import org.gtri.fj.data.Option
import org.gtri.fj.function.Effect0
import org.json.JSONArray

import static org.gtri.fj.data.Option.fromNull

class User {

    String username
    String nameFamily
    String nameGiven
    String contactEmail
    String roleArrayJson

    static constraints = {
        username blank: false, unique: true
        nameFamily nullable: true, length: 1000
        nameGiven nullable: true, length: 1000
        contactEmail nullable: true, length: 1000
        roleArrayJson nullable: true, length: 1000
    }

    static mapping = {
        table name: 'tfam_user'
    }


    static final Option<User> findByUsernameHelper(final String username) {
        fromNull(findByUsername(username))
    }

    User saveAndFlushHelper() {
        User.withTransaction {
            save(flush: true, failOnError: true)
        }
    }

    static final void withTransactionHelper(final Effect0 effect0) {
        withTransaction({ return effect0.f() })
    }
    
    public Boolean isAdmin() {

        if (StringUtils.isNotEmpty(this.roleArrayJson)) {
            JSONArray rolesJsonArray = new JSONArray(this.roleArrayJson);

            return rolesJsonArray.toList()
                    .stream()
                    .filter(role -> Role.fromValue((String) role).isPresent())
                    .map(role -> Role.fromValue((String) role).get())
                    .anyMatch(role -> Role.ROLE_ADMIN == role)
        }

        return false
    }

    public String toString() {
        return username;
    }

    public Map toJsonMap(boolean shallow = true) {
        def json = [
                id: this.id,
                username: this.username,
                nameFamily: this.nameFamily,
                nameGiven: this.nameGiven,
                contactEmail: this.contactEmail,
                admin: this.isAdmin()
        ]
        if( !shallow ){
            // TODO Create rest of data model...
        }
        return json;
    }//end toJsonMap




}
