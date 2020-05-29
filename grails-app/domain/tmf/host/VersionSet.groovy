package tmf.host

import java.sql.Timestamp

/**
 * A logical representation of a set of TDs, TIPs, and Business Objects.
 */
class VersionSet {

    static search = {
        name index: 'yes'
    }


    /**
     * The name of this version set, as given by the users/developers of the system.  Sort of like a branch name in GIT.
     * As such, it is limited to [a-Z_][a-Z0-9_-]* as a regular expression to define it.
     */
    String name;

    /**
     * When a VersionSet is created, this value is set to false while all previous TDs and TIPs are copied over to it.
     * Until it is finished, you are sent to a waiting page.
     */
    boolean createdSuccessfully = false;

    /**
     * When this VersionSet was created.
     */
    Date dateCreated;

    /**
     * The user account which created this VersionSet.
     */
    User createdBy;

    /**
     * The last updated date.
     */
    Date lastUpdated;

    /**
     * Version sets will eventually be released to the public.  Once this occurs, then this field has a value, which is the
     * date that it was released.
     */
    Date releasedDate;

    /**
     * The user account responsible for releasing this version set.
     */
    User releasedBy;

    /**
     * If true, then this is the production VersionSet.  Note that only 1 version set in the system can be set to production = true.
     */
    boolean production = false;

    /**
     * Once a version set has been released, it is no longer able to be edited (a new one must be created instead).  Thus,
     * we have a field to determine if it can be edited or not.
     */
    boolean editable = false;

    /**
     * If true, then this is the development VersionSet.  Note that only 1 version set in the system can be set to development = true.
     */
    boolean development = false;
    /**
     * The VersionSet which comes before this one in chronological order.
     */
    VersionSet predecessor;

    /**
     * The VersionSet which comes after this one in chronological order.
     */
    VersionSet successor;

    /**
     * When the current user locked the version set.
     */
    Date lockedDate;

    /**
     * The user who is currently editing this version set.  No other users may edit while it is locked.
     */
    User lockedBy;

    static constraints = {
        name(nullable: false, blank: false, maxSize: 254, unique: true, matches: "[a-zA-Z_][a-zA-Z0-9_\\-]*")
        createdSuccessfully(nullable: false)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
        production(nullable: false)
        development(nullable: false)
        predecessor(nullable: true)
        successor(nullable: true)
        editable(nullable: false)
        releasedDate(nullable: true)
        releasedBy(nullable: true)
        createdBy(nullable: false)
        lockedDate(nullable: true)
        lockedBy(nullable: true)
    }

    static mapping = {
        table(name: 'version_set')
        production(column: 'production')
        development(column: 'development')
        predecessor(column: 'predecessor_ref')
        successor(column: 'successor_ref')
        releasedBy(column: 'released_by_ref')
        createdBy(column: 'created_by_ref')
    }


    static namedQueries = {
        latestEntry {
            max('dateCreated')
        }
    }

    /**
     * Produces a Map which can be used to render this VersionSet's information as JSON.
     * <br/><br/>
     * Note that verbose=true may leak sensitive data to a potentially anonymous user.
     */
    public Map toJson(boolean verbose){
        Map json = [
                name: this.name,
                production: this.production ?: false,
                editable: this.editable ?: false,
                development: this.development ?: false,
                predecessor: this.predecessor?.name ?: null,
                successor: this.successor?.name ?: null
        ]
        if( verbose ){
            json.put("id", this.id);
            if( this.dateCreated )
                json.put("dateCreated", AbstractTFObjectAwareController.formatDateAsString(this.dateCreated));
            json.put("createdBy", this.createdBy?.username  ?: null);
            if( this.lastUpdated )
                json.put("lastUpdated", AbstractTFObjectAwareController.formatDateAsString(this.lastUpdated));
        }

        if( this.releasedDate != null ){
            json.put("releasedDate", AbstractTFObjectAwareController.formatDateAsString(this.releasedDate));
            if( verbose )
                json.put("releasedBy", this.releasedBy?.username ?: null);
        }
        if( verbose && this.lockedDate != null ){
            json.put("lockedDate", AbstractTFObjectAwareController.formatDateAsString(this.lockedDate));
            json.put("lockedBy", this.lockedBy?.username ?: null);
        }
        return json;
    }


}//end VersionSet