package tmf.host

/**
 * TODO: Write a description here
 * @user brad
 * @date 9/19/16
 */
class Role {

    static String ROLE_REVIEWER = "ROLE_REVIEWER"
    static String ROLE_DEVELOPER = "ROLE_DEVELOPER"
    static String ROLE_ORG_ADMIN = "ROLE_ORG_ADMIN"
    static String ROLE_ADMIN = "ROLE_ADMIN"

    Role(){}
    Role(String authority) {
        this()
        this.authority = authority
    }

    String authority

    static mapping = {
        cache true
    }

    static constraints = {
        authority blank: false, unique: true
    }

}

