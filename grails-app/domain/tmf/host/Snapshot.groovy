package tmf.host

/**
 * Represents a snapshot of TDs and TIPs for a particular version set frozen at a place in time.  Each version set can have
 * multiple Snapshots, and it's current set of TDs and TIPs need not be a particular snapshot.  Some snapshots are
 * made automatically before drastic changes, others are manually generated.
 * <br/><br/>
 * @user brad
 * @date 12/1/16
 */
class Snapshot {

    /**
     * A snapshot must belong to a particular {@link VersionSet}.
     */
    static belongsTo = [versionSet: VersionSet]

    /**
     * A descriptive name of this Snapshot, to aid others in determining if it's what they want.
     */
    String name;

    /**
     * An optional detailed description of this snapshot.  Information in here (if any) should be very clear about what
     * this snapshot contains and why it is important.
     */
    String description;

    /**
     * Auto timestamp of when this Snapshot was made.
     */
    Date dateCreated;

    /**
     * Which user created (either directly or indirectly) this snapshot.
     */
    User createdBy;

    /**
     * Whether this was an automatic system snapshot, or a user created it manually.
     */
    Boolean automatic;

    static constraints = {
        versionSet(nullable: false)
        name(nullable: false, blank: false, maxSize: 128)
        dateCreated(nullable: true)
        createdBy(nullable: false)
        automatic(nullable: false)
    }

    static mapping = {
        versionSet(column: 'version_set_ref')
        createdBy(column: 'created_by_ref')
    }

}
