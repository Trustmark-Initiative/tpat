package tmf.host.artifact_processing

import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkFrameworkIdentifiedObject
import org.json.JSONArray
import org.json.JSONObject

/**
 * Represents an action that the system has determined.  Note that there can be "sub actions" that would occur as a
 * result of applying the given action.
 * <br/><br/>
 * @user brad
 * @date 12/1/16
 */
public class ArtifactAction {

    /**
     * All artifact actions will have a unique identifier.
     */
    Long uniqueId;

    /**
     * The ID of the artifact in question (which must be unique system wide).
     */
    String id;

    /**
     * This will be TD or TIP depending on what the artifact is.
     */
    String type;

    /**
     * The Artifact in question (TD or TIP) - note only the identifying information from it (ie, a TrustmarkFrameworkIdentifiedObject map).
     */
    Map artifact; // The TD or TIP information.

    /**
     * The action type to take.
     */
    ActionType actionType;

    /**
     * The list of actions which should occur BEFORE this action takes place.
     */
    List<ArtifactAction> preActions = [];

    /**
     * The list of actions which should occur AFTER this action takes place.
     */
    List<ArtifactAction> postActions = [];

    /**
     * A collection of warnings that the user should be aware of when reviewing this action on the process page.
     */
    List<String> warnings = []


    /**
     * If type is OVERWRITE, then this ist he previous artifactId we will be overwriting.  Note that it is the long id
     * of the VersionSetTDLink (or TIPLink) that will be overwritten.
     */
    Long previousLinkId;

    //==================================================================================================================
    // Error Fields - These fields may have a value depending on the ActionType == "ERROR".
    //==================================================================================================================
    ActionErrorType errorType;

    /**
     * Indicates that what error condition has occurred;
     */
    String errorMessage;


    public Map toJSON() {
        Map json = [
                uniqueId: uniqueId,
                id: id,
                type: type,
                actionType: actionType.toString(),
                artifact: artifact,
                preActions: [],
                postActions: [],
                warnings: [],
                previousLinkId: previousLinkId,
                errorType: errorType?.toString(),
                errorMessage: errorMessage
        ]

        if( warnings != null && warnings.size() > 0 ){
            for( String warning : warnings ){
                json.warnings.add(warning);
            }
        }

        if( preActions != null && preActions.size() > 0 ){
            for( ArtifactAction a : preActions ){
                json.preActions.add(a.toJSON());
            }
        }

        if( postActions != null && postActions.size() > 0 ){
            for( ArtifactAction a : postActions ){
                json.postActions.add(a.toJSON());
            }
        }

        return json;
    }


    public static ArtifactAction fromJson(JSONObject json){
        ArtifactAction action = new ArtifactAction();
        action.setUniqueId(json.optLong("uniqueId"));
        action.setId(json.optString("id"));
        action.setType(json.optString("type"));
        action.setActionType(ActionType.fromString(json.optString("actionType")));
        action.setPreviousLinkId(json.optLong("previousLinkId"));
        action.setErrorType(ActionErrorType.fromString(json.optString("errorType")));
        action.setErrorMessage(json.optString("errorMessage"));

        JSONObject artifact = json.optJSONObject("artifact");
        if( artifact ){
            action.setArtifact([
                    identifier: artifact.optString("identifier"),
                    name: artifact.optString("name"),
                    version: artifact.optString("version"),
                    description: artifact.optString("description"),
                    type: artifact.optString("type")
            ])
        }

        if( json.optJSONArray("warnings") ){
            action.setWarnings([]);
            JSONArray warnings = json.optJSONArray("warnings");
            for( int i = 0; i < warnings.length(); i++ ){
                action.getWarnings().add(warnings.optString(i));
            }
        }

        if( json.optJSONArray("preActions") ){
            action.setPreActions([]);
            JSONArray preActions = json.optJSONArray("preActions");
            for( int i = 0; i < preActions.length(); i++ ){
                JSONObject preActionJson = preActions.optJSONObject(i);
                if( preActionJson != null ){
                    action.preActions.add(ArtifactAction.fromJson(preActionJson));
                }
            }
        }

        if( json.optJSONArray("postActions") ){
            action.setPostActions([]);
            JSONArray postActions = json.optJSONArray("postActions");
            for( int i = 0; i < postActions.length(); i++ ){
                JSONObject postActionJson = postActions.optJSONObject(i);
                if( postActionJson != null ){
                    action.postActions.add(ArtifactAction.fromJson(postActionJson));
                }
            }
        }

        return action;
    }

}
