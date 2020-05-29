package tmf.host

import org.hibernate.search.annotations.DocumentId
import org.hibernate.search.annotations.Indexed
import org.hibernate.search.annotations.IndexedEmbedded

/**
 * Mapping between a {@link VersionSet} and a {@link TrustInteroperabilityProfile}.
 */
@Indexed
class VersionSetTIPLink implements VersionSetLink {

    @DocumentId
    Long id

    /**
     * The VersionSet link.
     */
    @IndexedEmbedded
    VersionSet versionSet;

    /**
     * A link to the TIP in the database.
     */
    @IndexedEmbedded
    TrustInteroperabilityProfile trustInteroperabilityProfile;

    /**
     * The identifier for the TIP.  It must exist here so that we can perform searches on the current
     * versionSet to find TIPs.  If we relied on it existing only in the trustInteroperabilityProfile, it would mean a more
     * complex query to go in and get it (can't be done with gorm finders easily).
     */
    String tipIdentifier;

    /**
     * If this value is set true, then this TIP was copied over from a previous version set, and only certain things on
     * it can be changed (like deprecated, superseded by, etc).  This MUST be honored, as not to impact existing Trustmarks
     * and such out on the Internet at large.
     */
    Boolean copyOver;

    /**
     * A TIP can be in one of these states:
     *   COPY_OVER - Indicates that the TIP was persent in a previous revision, and may only be updated to add deprecated/supersession info
     *   EDITABLE - Indicates td TIP is able to be modified in anyway.
     *   FINALIZED - Indicates the TIP is ready for production, and any attempts to modify it should be met with errors.
     *
     *   Note that when the VersionSet is moved to production, ALL links will be marked FINALIZED.
     */
    String status;

    /**
     * Indicates that the TIP existed prior to this revision.  Here you can get exactly what revision the original was.
     */
    VersionSet originalVersionSet;

    /**
     * If true, it indicates that this TIP should be included in the "TIP Tree" page.  In otherwords, the TIP is marked
     * as very special or an important high-level TIP that should drive the user's view of the system.  An entry point.
     */
    Boolean primaryTIP;

    Date dateCreated
    Date lastUpdated

    static constraints = {
        versionSet(nullable: false)
        trustInteroperabilityProfile(nullable: false)
        tipIdentifier(nullable: false, blank: false, maxSize: 65535)
        copyOver(nullable: false)
        status(nullable: false, blank: false, maxSize: 32)
        originalVersionSet(nullable: true)
        primaryTIP(nullable: false)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
    }

    static mapping = {
        table(name: 'version_set_tip_link')
        versionSet(column: 'version_set_ref')
        trustInteroperabilityProfile(column: 'tip_ref')
        originalVersionSet(column: 'original_version_set_ref')
        tipIdentifier(column: 'tip_identifier', type: 'text')
        primaryTIP(column: 'primary_tip')
    }


    @Override
    boolean isTdLink() {
        return false;
    }

    @Override
    boolean isTipLink() {
        return true;
    }


}//end VersionSet