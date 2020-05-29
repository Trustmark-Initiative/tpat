package tmf.host.artifact_processing

/**
 * Defines the errors that can occur based on processing a file.
 * <br/><br/>
 * @user brad
 * @date 12/1/16
 */
enum ActionErrorType {

    /**
     * Indicates that the TD you are trying to take an action on is NOT a local TD; you are not allowed to edit those
     * TDs that have a base URL you don't own locally.
     */
    NO_LOCAL_BASE,

    /**
     * Indicates that the user is attempting to overwrite aspects of a TD that are read-only.
     */
    CHANGE_PREVIOUSLY_RELEASED,

    /**
     * Validation failed on the TD.
     */
    VALIDATION_ERROR,

    /**
     * If this error condition is set, it means the user is trying to upload but a TD already exists with the same NAME
     * and VERSION (although the ID is different).
     */
    COLLISION_NAME_ID;

    public static ActionErrorType fromString( String str ){
        ActionErrorType type = null;
        for( ActionErrorType aet : ActionErrorType.values() ){
            if( aet.toString().equalsIgnoreCase(str) ){
                type = aet;
                break;
            }
        }
        return type;
    }

}