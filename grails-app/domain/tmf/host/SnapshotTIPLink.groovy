package tmf.host

/**
 * Very similar to a VersionSetTIPLink, this is a snapshot link meant to save that information so it can be restored later.
 * <br/><br/>
 * @user brad
 * @date 12/1/16
 */
class SnapshotTIPLink {

    static belongsTo = [snapshot: Snapshot]

    /**
     * A link to the TIP in the database.
     */
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


    static constraints = {
        snapshot(nullable: false)
        trustInteroperabilityProfile(nullable: false)
        tipIdentifier(nullable: false, blank: false, maxSize: 65535)
        copyOver(nullable: false)
        status(nullable: false, blank: false, maxSize: 32)
        originalVersionSet(nullable: true)
    }

    static mapping = {
        table(name: 'snapshot_link')
        snapshot(column: 'snapshot_ref')
        trustInteroperabilityProfile(column: 'tip_ref')
        originalVersionSet(column: 'original_version_set_ref')
        tipIdentifier(column: 'tip_identifier', type: 'text')
    }


}
