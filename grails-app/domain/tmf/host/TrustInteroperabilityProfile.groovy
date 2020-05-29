package tmf.host

import org.hibernate.search.annotations.DocumentId
import org.hibernate.search.annotations.Field
import org.hibernate.search.annotations.Index
import org.hibernate.search.annotations.Indexed

/**
 * Holds cached information about a tmf.host.TrustInteroperabilityProfile that this tool knows about.
 */
@Indexed
class TrustInteroperabilityProfile {

    @DocumentId
    Long id

    /**
     * The identifier of this trustmark definition.
     */
    String identifier;
    /**
     * The identifier of this TIP, less the base URI supported by this tool.  In other words, if the identifier is:
     *   http://test/context/trust-interoperability-profiles/tip-name/1.0
     * and the base URI is defined to be:
     *   http://test/context
     * Then this value will hold:
     *   /trust-interoperability-profiles/tip-name/1.0
     */
    String subIdentifier;
    /**
     * The tmf.host.TrustInteroperabilityProfile name
     */
    @Field(index=Index.YES)
    String name
    /**
     * The tmf.host.TrustInteroperabilityProfile version
     */
    String tipVersion
    /**
     * The tmf.host.TrustInteroperabilityProfile description
     */
    @Field(index=Index.YES)
    String description
    /**
     * The publication date of this tmf.host.TrustInteroperabilityProfile
     */
    Date publicationDateTime;

    /**
     * Whether or not this TIP is deprecated.
     */
    Boolean deprecated = false;

    /**
     * Organization unique identifier which issued this TIP.
     */
    String issuerId;
    /**
     * Name of the issuing organization.
     */
    String issuerName;

    /**
     * The Raw data (JSON, XML, etc) for this tmf.host.TrustInteroperabilityProfile.
     */
    BinaryObject artifact;

    /**
     * A list of URL identifiers for TIP objects which are superseded by this TIP. Newline separated.
     */
    String supersedes;
    /**
     * A list of URL identifiers for TIP objects which supersede this TIP. Newline separated.
     */
    String supersededBy;
    /**
     * A list of URL identifiers for TIPs which this TIP claims equivalency to.  Newline separated.
     */
    String satisfies


    static constraints = {
        identifier(nullable: false, blank: false, maxSize: 65535)
        subIdentifier(nullable: false, blank: false, maxSize: 2056)
        name(nullable: false, blank: false, maxSize: 256)
        tipVersion(nullable: false, blank: false, maxSize: 128)
        description(nullable: true, blank: true, maxSize: 65535)
        publicationDateTime(nullable: false)
        issuerId(nullable: false, blank: false, maxSize: 65535)
        issuerName(nullable: true, blank: true, maxSize: 512)

        deprecated(nullable: false)
        artifact(nullable: false)

        supersedes(nullable: true, maxSize: 65535)
        supersededBy(nullable: true, maxSize: 65535)
        satisfies(nullable: true, maxSize: 65535)
    }

    static mapping = {
        table 'trust_interoperability_profile'
        identifier(type: 'text', column: 'tip_identifier')
        subIdentifier(type: 'text', column: 'sub_identifier')
        name(column: 'tip_name')
        tipVersion(column: 'tip_version')
        description(type: 'text', column: 'description')
        publicationDateTime(column: 'publication_date')
        issuerId(type:'text', column: 'issuer_id')
        issuerName(column: 'issuer_name')
        artifact(column: 'artifact_ref')

        supersedes(type: 'text')
        supersededBy(type: 'text')
        satisfies(type: 'text')
    }

    static transients = [ "baseIdentifier" ]


    public String getBaseIdentifier() {
        return this.getIdentifier().replace(this.getSubIdentifier(), "");
    }

    String getVer()  {
        return this.tipVersion
    }

    public String toString(){
        return "TIP[${name}, v.${tipVersion}]"
    }

    public int hashCode(){
        return this.getIdentifier().hashCode();
    }

    public boolean equals(Object other){
        if( other != null && other instanceof TrustInteroperabilityProfile ){
            TrustInteroperabilityProfile otherTip = (TrustInteroperabilityProfile) other;
            return this.getIdentifier().equalsIgnoreCase(otherTip.getIdentifier()) ||
                    (this.getName().equalsIgnoreCase(otherTip.getName()) &&
                            this.getTipVersion().equalsIgnoreCase(otherTip.getTipVersion()))
        }
        return false;
    }

}//end tmf.host.TrustmarkDefinition