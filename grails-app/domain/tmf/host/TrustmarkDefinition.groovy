package tmf.host

import org.hibernate.search.annotations.DocumentId
import org.hibernate.search.annotations.Field
import org.hibernate.search.annotations.Index
import org.hibernate.search.annotations.Indexed

/**
 * Holds cached information about a tmf.host.TrustmarkDefinition that this tool knows about.
 */
@Indexed
class TrustmarkDefinition {

    @DocumentId
    Long id

    /**
     * The identifier of this trustmark definition.
     */
    String identifier;
    /**
     * The identifier of this TD, less the base URI supported by this tool.  In other words, if the identifier is:
     *   http://test/context/trustmark-definitions/td-name/1.0
     * and the base URI is defined to be:
     *   http://test/context
     * Then this value will hold:
     *   /trustmark-definitions/td-name/1.0
     */
    String subIdentifier;
    /**
     * The tmf.host.TrustmarkDefinition name
     */
    @Field(index=Index.YES)
    String name
    /**
     * The tmf.host.TrustmarkDefinition version
     */
    String tdVersion
    /**
     * The tmf.host.TrustmarkDefinition description
     */
    @Field(index=Index.YES)
    String description
    /**
     * If true, then this TD is deprecated (a warning message should appear).
     */
    boolean deprecated;
    /**
     * The publication date of this trustmark definition.
     */
    Date publicationDateTime;

    /**
     * The TDO's identifier.
     */
    String definingOrganizationId;
    /**
     * The TDO's name.
     */
    String definingOrganizationName;

    /**
     * The Raw data (JSON, XML, etc) for this tmf.host.TrustmarkDefinition.
     */
    BinaryObject artifact;

    /**
     * A list of URL identifiers for TD objects which are superseded by this TD. Newline separated.
     */
    String supersedes;
    /**
     * A list of URL identifiers for TD objects which supersede this TD. Newline separated.
     */
    String supersededBy;
    /**
     * A list of URL identifiers for TDs which this TD claims equivalency to.  Newline separated.
     */
    String satisfies;

    static constraints = {
        identifier(nullable: false, blank: false, maxSize: 65535)
        subIdentifier(nullable: false, blank: false, maxSize: 2056)
        name(nullable: false, blank: false, maxSize: 256)
        tdVersion(nullable: false, blank: false, maxSize: 128)
        description(nullable: true, blank: true, maxSize: 65535)
        deprecated(nullable: false)
        publicationDateTime(nullable: false)
        definingOrganizationId(nullable: false, blank: false, maxSize: 65535)
        definingOrganizationName(nullable: true, blank: true, maxSize: 512)
        artifact(nullable: false)

        supersedes(nullable: true, maxSize: 65535)
        supersededBy(nullable: true, maxSize: 65535)
        satisfies(nullable: true, maxSize: 65535)
    }

    static mapping = {
        table 'trustmark_definition'
        identifier(type: 'text', column: 'trustmark_identifier')
        subIdentifier(type: 'text', column: 'sub_identifier')
        name(column: 'td_name')
        tdVersion(column: 'td_version')
        description(type: 'text', column: 'description')
        publicationDateTime(column: 'publication_date')
        definingOrganizationId(type: 'text', column: 'tdo_id')
        definingOrganizationName(column: 'tdo_name')
        artifact(column: 'artifact_ref')

        supersedes(type: 'text')
        supersededBy(type: 'text')
        satisfies(type: 'text')
    }

    static transients = [ "baseIdentifier" ]

    String getVer()  {
        return this.tdVersion
    }

    public String getBaseIdentifier() {
        return this.getIdentifier().replace(this.getSubIdentifier(), "");
    }

    public String toString(){
        return "TrustmarkDefinition[${name}, v.${tdVersion}]"
    }

    public int hashCode(){
        return this.getIdentifier().hashCode();
    }

    public boolean equals(Object other){
        if( other != null && other instanceof TrustmarkDefinition ){
            TrustmarkDefinition otherTd = (TrustmarkDefinition) other;
            return this.getIdentifier().equalsIgnoreCase(otherTd.getIdentifier()) ||
                    (this.getName().equalsIgnoreCase(otherTd.getName()) &&
                        this.getTdVersion().equalsIgnoreCase(otherTd.getTdVersion()))
        }
        return false;
    }

}//end tmf.host.TrustmarkDefinition