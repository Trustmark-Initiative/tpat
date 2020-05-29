package tmf.host

/**
 * This object represents a "Blessed" external artifact which is NOT found locally.
 * <br/><br/>
 * @author brad
 * @date 5/25/17
 */
class ExternalReference {

    Boolean isTrustmarkDefinition = false;
    Boolean isTrustProfile = false;
    String identifier;
    String name;
    String theVersion;
    String description;

    static constraints = {
        isTrustmarkDefinition(nullable: false)
        isTrustProfile(nullable: false)
        identifier(nullable: false, blank: false, maxSize: 65535)
        name(nullable: false, blank: false, maxSize: 1024)
        theVersion(nullable: false, blank: false, maxSize: 254)
        description(nullable: false, blank: false, maxSize: 65535)
    }

    static mapping = {
        identifier(type: 'text')
        name(type: 'text')
        description(type: 'text')
    }

}
