package tmf.host
import grails.plugin.springsecurity.SpringSecurityService

class User {

    transient SpringSecurityService springSecurityService;

    String username
    String password
    String name
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired

    static transients = ['springSecurityService']

    static constraints = {
        username blank: false, unique: true
        name blank: false
        password blank: false, password: true
    }

    static mapping = {
        table name: 'tfam_user'
        password column: '`pass_hash`'
    }


    public Boolean hasRole(Role role) {
        return hasRole(role.getAuthority());
    }

    public Boolean hasRole(String roleName) {
        Set<UserRole> roles = UserRole.findAllByUser(this);
        boolean hasRole = false;
        roles.each { UserRole role ->
            if( role.role.authority == roleName )
                hasRole = true;
        }
        return hasRole;
    }

    public Boolean isAdmin() {
        Set<UserRole> roles = UserRole.findAllByUser(this);
        boolean hasRole = false;
        roles.each { UserRole role ->
            if( role.role.authority == Role.ROLE_ADMIN )
                hasRole = true;
        }
        return hasRole;
    }

    public Boolean isReviewer() {
        Set<UserRole> roles = UserRole.findAllByUser(this);
        boolean hasRole = false;
        roles.each { UserRole role ->
            if( role.role.authority == Role.ROLE_REVIEWER )
                hasRole = true;
        }
        return hasRole;
    }

    public Boolean isOrgAdmin() {
        Set<UserRole> roles = UserRole.findAllByUser(this);
        boolean hasRole = false;
        roles.each { UserRole role ->
            if( role.role.authority == Role.ROLE_ORG_ADMIN )
                hasRole = true;
        }
        return hasRole;
    }

    public Boolean isDeveloper() {
        Set<UserRole> roles = UserRole.findAllByUser(this);
        boolean hasRole = false;
        roles.each { UserRole role ->
            if( role.role.authority == Role.ROLE_DEVELOPER )
                hasRole = true;
        }
        return hasRole;
    }



    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this).collect { it.role } as Set
    }


    public String toString() {
        return username;
    }




    public Map toJsonMap(boolean shallow = true) {
        def json = [
                id: this.id,
                username: this.username,
                enabled: this.enabled,
                admin: this.isAdmin(),
                orgAdmin: this.isOrgAdmin(),
                reviewer: this.isReviewer(),
                developer: this.isDeveloper()
        ]
        if( !shallow ){
            // TODO Create rest of data model...
        }
        return json;
    }//end toJsonMap




}
