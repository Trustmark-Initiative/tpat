package tmf.host

import org.json.JSONObject

/**
 * All actions taken on a version set are stored in this table as a log entry.  They are ordered by a specific counter,
 * and contain a "Type" field which allows particular actions based on the data.
 * <br/><br/>
 * @author brad
 * @date 11/21/16
 */
class VersionSetLogEntry implements Comparable<VersionSetLogEntry>{

    static belongsTo = [versionSet: VersionSet]

    /**
     * Automatically set to when this log entry was created.
     */
    Date dateCreated;

    /**
     * The counter of events, for ordering.
     */
    Integer counter;

    /**
     * The type of this event.  Useful if you are going to program based on the JSON data.
     */
    String type;

    /**
     * The human-readable description of this event, and what happened.
     */
    String message;

    /**
     * The data of this event.
     */
    String json;


    static constraints = {
        versionSet(nullable: false)
        dateCreated(nullable: true)
        counter(nullable: false)
        type(nullable: false, blank: false, maxSize: 64)
        message(nullable: false, blank: false, maxSize: 65535)
        json(nullable: false, blank: false, maxSize: Integer.MAX_VALUE)
    }

    static mapping = {
        table(name: 'version_set_log_entry')
        versionSet(column: 'version_set_ref')
        message(type: 'text')
        json(type: 'text')
    }


    //==================================================================================================================
    //  Instance Methods
    //==================================================================================================================
    JSONObject getJsonObject() {
        return new JSONObject(this.json ?: "{}");
    }

    String toString() {
        return "[${this.type}] " + this.message;
    }

    @Override
    int compareTo(VersionSetLogEntry other) {
        if( other != null )
            return this.counter.compareTo(other.counter);
        return -1;
    }
//==================================================================================================================
//  Static Helper Methods
//==================================================================================================================
    static void create(Long versionSetId, String type, String message) {
        create(versionSetId, type, message, [:]);
    }

    static void create(Long versionSetId, String type, String message, Map json) {

        VersionSetLogEntry.withTransaction {
            if( json == null ) json = [:];
            VersionSet vs = VersionSet.get(versionSetId)
            if( vs != null)  {
                Integer lastId = VersionSetLogEntry.createCriteria().get {
                    projections {
                        max "counter"
                    }
                } as Integer
                if( lastId == null )
                    lastId = 0
                VersionSetLogEntry vsle = new VersionSetLogEntry(versionSet: vs)
                vsle.type = type
                vsle.message = message
                vsle.json = new JSONObject(json).toString(2)
                vsle.counter = lastId++
                vsle.save(failOnError: true)
            } else {
                log.warn("Cannot create log entry on non-existant version set: ${versionSetId}")
            }
        }
    }
}
