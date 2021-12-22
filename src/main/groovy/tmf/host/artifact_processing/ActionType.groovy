package tmf.host.artifact_processing

/**
 * TODO: Write a description here
 * @user brad
 * @date 12/1/16
 */
enum ActionType {
    /**
     * Simply indicates that no conflicts were found and that we should "add" the artifact to the version set.
     */
    ADD,
    /**
     * Indicates that we are going to overwrite an artifact with a different one.
     */
    OVERWRITE,
    /**
     * Indicates we are Deprecating the artifact, and marking it as superseded by something else.
     */
    DEPRECATE,
    /**
     * Indicates we are Superseding the artifact, and marking it as deprecated by something else.
     */
    SUPERSEDE,
    /**
     * Indicates that we are going to remove an artifact.
     */
    REMOVE,
    /**
     * Indicates that there was an error with the TD or TIP, and that no action can occur.  Error message
     * should have some meaningful information in it.
     */
    ERROR,
    /**
     * Indicates that nothing should be done with this artifact (it should be "dropped" from all processing).
     */
    IGNORE;

    static ActionType fromString( String str ){
        ActionType type = null;
        for( ActionType at : ActionType.values() ){
            if( at.toString().equalsIgnoreCase(str) ){
                type = at;
                break;
            }
        }
        return type;
    }

}