package tmf.host

/**
 * Used to communicate what the status of applying a group of changes is.
 * <br/><br/>
 * @author brad
 * @date 4/18/17
 */
class ApplyChangesFeedback {

    static belongsTo = [
        upload: BinaryObject
    ]

    /**
     * When this Upload Process was started.
     */
    Date dateCreated;

    /**
     * Who initiated this upload.
     */
    String uploadingUser;

    /**
     * This field contains formated JSON describing the phases, and which ones are complete and active.  It is an array of following entries:
     * {
     *     "name" : "phaseId",
     *     "displayName" : "Human Readable Name",
     *     "complete" : true,
     *     "active": false
     * }
     */
    String phaseJson;

    /**
     * A status message, like what's going on right now.
     */
    String message;

    /**
     * An integer from -1 to 100.  If -1, it indicates an unknown amount of time.  Otherwise, a percentage of completion
     * towards a goal (presumably linear).
     */
    Integer percentage;

    /**
     * Name of the thread processing this upload.
     */
    String threadName;

    /**
     * The Thread's process identifier.
     */
    long threadPid;

    /**
     * Set to true if the system ran into an error while processing.  If true, then message will have some user feedback.
     */
    Boolean hasError = false;

    /**
     * If an error occurred, this field will contain the detailed stack trace.
     */
    String stacktraceJson;

    static constraints = {
        upload(nullable: false)
        uploadingUser(nullable: false, blank: false, maxSize: 254)
        dateCreated(nullable: true)
        phaseJson(nullable: true, blank: true, maxSize: 65535)
        message(nullable: true, blank: true, maxSize: 65535)
        percentage(nullable: false)
        threadName(nullable: false, blank: false, maxSize: 254);
        threadPid(nullable: false)
        hasError(nullable: false)
        stacktraceJson(nullable: true, blank: true, maxSize: 65535)
    }

    static mapping = {
        phaseJson(type: 'text')
        message(type: 'text')
        stacktraceJson(type: 'text')
    }

}//end UploadProcessFeedback