package tmf.host

/**
 * Very similar to a VersionSetTDLink, this is a snapshot link meant to save that information so it can be restored later.
 * <br/><br/>
 * @user brad
 * @date 12/1/16
 */
class SnapshotTDLink {

    static belongsTo = [snapshot: Snapshot]

    /**
     * The trustmarkDefinition.
     */
    TrustmarkDefinition trustmarkDefinition;

    /**
     * The identifier for the trustmarkDefinition.  It must exist here so that we can perform searches on the current
     * versionSet to find Tds.  If we relied on it existing only in the trustmarkDefinition, it would mean a more
     * complex query to go in and get it (can't be done with gorm finders easily).
     */
    String tdIdentifier;

    /**
     * If this value is set true, then this TD was copied over from a previous version set, and only certain things on
     * it can be changed (like deprecated, superseded by, etc).  This MUST be honored, as not to impact existing Trustmarks
     * and such out on the Internet at large.
     */
    Boolean copyOver;

    /**
     * A TD can be in one of these states:
     *   EDITABLE - Indicates td TD is able to be modified in anyway.
     *   FINALIZED - Indicates the TD is ready for production, and any attempts to modify it should be met with errors.
     *
     *   Note that when the VersionSet is moved to production, ALL links will be marked FINALIZED.
     */
    String status;

    /**
     * Indicates that the TD existed prior to this revision.  Here you can get exactly what revision the original was.
     */
    VersionSet originalVersionSet;


    static constraints = {
        snapshot(nullable: false)
        tdIdentifier(nullable: false, blank: false, maxSize: 65535)
        trustmarkDefinition(nullable: false)
        copyOver(nullable: false)
        status(nullable: false, blank: false, maxSize: 32)
        originalVersionSet(nullable: true)
    }

    static mapping = {
        table(name: 'snapshot_td_link')
        tdIdentifier(column: 'td_identifier', type: 'text')
        snapshot(column: 'snapshot_ref')
        trustmarkDefinition(column: 'trustmark_definition_ref')
        originalVersionSet(column: 'original_version_set_ref')
    }

}
