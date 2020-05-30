package tmf.host.artifact_processing

import edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkFrameworkIdentifiedObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tmf.host.ProcessUploadService

/**
 * A class for holding in-memory interactions between processing file uploads.  Note that this doesn't work if
 * the system is being load balanced (ie, memory may not co-exist on the same machine).  In that case, we may want
 * to move this to the database.  For now, this is the quickest way to do this.
 * <br/><br/>
 * @user brad
 * @date 12/1/16
 */
class MemoryProcessingData {

    private static final Logger log = LoggerFactory.getLogger(MemoryProcessingData.class);


    public static final Boolean HASHMAP_LOCK = false;
    private static final HashMap<Long, MemoryProcessingData> memoryProcessingDataHashMap = new HashMap<>();

    public static MemoryProcessingData find(Long uploadId){
        synchronized (HASHMAP_LOCK) {
            return memoryProcessingDataHashMap.get(uploadId);
        }
    }//end findForUpload()

    public static MemoryProcessingData create(String username, Long vsId, Long uploadId, String filename){
        MemoryProcessingData mpd = new MemoryProcessingData();
        mpd.reset(username, vsId, uploadId, filename);
        synchronized (HASHMAP_LOCK){
            memoryProcessingDataHashMap.put(uploadId, mpd);
        }
        return mpd;
    }

    /**
     * Removes the given MemoryProcessingData entry (when all is complete).
     * @param uploadId
     */
    public static void purge(Long uploadId){
        synchronized (HASHMAP_LOCK){
            if(memoryProcessingDataHashMap.containsKey(uploadId) )
                memoryProcessingDataHashMap.remove(uploadId);
        }
    }

    //==================================================================================================================
    //  Instance Vars & Methods
    //==================================================================================================================

    Integer actionIdCounter;
    String username;
    Long versionSetId;
    Long uploadId;
    Boolean alreadyChoseActions = false;
    String originalFilename;

    List<edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition> trustmarkDefinitions;
    List<edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile> trustInteroperabilityProfiles;
    List<String> invalidParameters


    Map collisionData = null;
    List<ArtifactAction> artifactActions;
    Map<String, ArtifactAction> actionMap;

    Boolean processingFinished;

    boolean alreadyDone(Long vsId, Long uId) {
        if( versionSetId != null && vsId == versionSetId && uploadId != null && uploadId == uId ){
            return processingFinished;
        }
        return false;
    }

    public List<ArtifactAction> getActionsByType(ActionType actionType){
        List<ArtifactAction> actions = [];
        for( ArtifactAction current : artifactActions ?: [] ){
            if( current.actionType == actionType )
                actions.add(current);
        }
        Collections.sort(actions, {ArtifactAction a1, ArtifactAction a2 ->
            String id1 = a1.getArtifact().get("name") + ", v"+a1.getArtifact().get("version");
            String id2 = a2.getArtifact().get("name") + ", v"+a2.getArtifact().get("version");
            return id1.compareToIgnoreCase(id2);
        } as Comparator);

        return actions;
    }


    /**
     * A helper method for returning the data contained here as JSON.
     * @return
     */
    Map buildJsonResponse() {
        Map json = [
                username: username,
                versionSetId: versionSetId,
                uploadId: uploadId,
                originalFilename: originalFilename,
                tdCount: getTdSize(),
                tipCount: getTipSize(),
                alreadyChoseActions: alreadyChoseActions
        ]

        def errorActions = []
        for( ArtifactAction action : artifactActions ){
            if( action.actionType == ActionType.ERROR ){
                errorActions.add(action.toJSON());
            }
        }
        json.put("errorActions", errorActions);
        json.put("errorActionCount", errorActions.size());

        def tdActions = []
        for( ArtifactAction action : artifactActions ){
            if( action.actionType != ActionType.ERROR && action.type == "TD" ){
                tdActions.add(action.toJSON());
            }
        }
        Collections.sort(tdActions, {a1, a2 ->
            return a1.artifact.name.compareToIgnoreCase(a2.artifact.name);
        } as Comparator);
        json.put("tdActions", tdActions);
        json.put("tdActionCount", tdActions.size());

        def tipActions = []
        for( ArtifactAction action : artifactActions ){
            if( action.actionType != ActionType.ERROR && action.type == "TIP" ){
                tipActions.add(action.toJSON());
            }
        }
        Collections.sort(tipActions, {a1, a2 ->
            return a1.artifact.name.compareToIgnoreCase(a2.artifact.name);
        } as Comparator);
        json.put("tipActions", tipActions);
        json.put("tipActionCount", tipActions.size());

        return json;
    }


    void reset() {
        actionIdCounter = 1;
        username = null;
        versionSetId = null;
        alreadyChoseActions = false;
        uploadId = null;
        originalFilename = null;
        trustmarkDefinitions = [];
        trustInteroperabilityProfiles = [];
        artifactActions = [];
        actionMap = [:];
        collisionData = null;
        processingFinished = false;
    }

    void reset(String u, Long vsId, Long upId, String name) {
        reset();
        username = u;
        versionSetId = vsId;
        uploadId = upId;
        originalFilename = name;
    }

    int getTdSize() {
        if( trustmarkDefinitions == null )
            trustmarkDefinitions = []
        return trustmarkDefinitions.size();
    }
    edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition getTd(String identifier){
        if( trustmarkDefinitions == null )
            trustmarkDefinitions = []
        for( edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition curTd : trustmarkDefinitions){
            if( curTd.getMetadata().getIdentifier().toString().equalsIgnoreCase(identifier) )
                return curTd;
        }
        return null;
    }
    edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition getTd(int index){
        if( trustmarkDefinitions == null )
            trustmarkDefinitions = []
        if( trustmarkDefinitions.size() > index && index >= 0 ){
            return trustmarkDefinitions.get(index);
        }else{
            throw new ArrayIndexOutOfBoundsException("Index ${index} is not in bounds of 0-${trustmarkDefinitions.size()}")
        }
    }
    void add(edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td){
        if( trustmarkDefinitions == null )
            trustmarkDefinitions = []
        trustmarkDefinitions.add(td); // TODO Check for collisions?
    }

    int getTipSize() {
        if( trustInteroperabilityProfiles == null )
            trustInteroperabilityProfiles = []
        return trustInteroperabilityProfiles.size();
    }
    edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile getTip(String identifier){
        if( trustInteroperabilityProfiles == null )
            trustInteroperabilityProfiles = []
        for( edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile curTip : trustInteroperabilityProfiles ){
            if( curTip.getIdentifier().toString().equalsIgnoreCase(identifier) )
                return curTip;
        }
        return null;
    }
    edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile getTip(int index){
        if( trustInteroperabilityProfiles == null )
            trustInteroperabilityProfiles = []
        if( trustInteroperabilityProfiles.size() > index && index >= 0 ){
            return trustInteroperabilityProfiles.get(index);
        }else{
            throw new ArrayIndexOutOfBoundsException("Index ${index} is not in bounds of 0-${trustInteroperabilityProfiles.size()}")
        }
    }
    void add(edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip){
        if( trustInteroperabilityProfiles == null )
            trustInteroperabilityProfiles = []
        trustInteroperabilityProfiles.add(tip); // TODO Check for collisions?
    }

    void add(ArtifactAction action){
        if( artifactActions == null )
            artifactActions = []
        action.uniqueId = actionIdCounter++;
        artifactActions.add(action); // TODO Check for collisions?
        addActionMapping(action.id, action);
    }

    void addActionMapping(String id, ArtifactAction action){
        if( actionMap == null )
            actionMap = [:]
        actionMap.put(id, action); // TODO Check for collisions?
    }


    boolean isCollisionTdIndex(int i){
        if( collisionData == null )
            checkForCollisions();
        return collisionData.affectedTdIndexes.contains(i);
    }
    boolean isCollisionTipIndex(int i){
        if( collisionData == null )
            checkForCollisions();
        return collisionData.affectedTipIndexes.contains(i);
    }

    /**
     * We will scan for any 2 TDs and TIPs that share either a) an ID or b) Name and Version, and return any collisions
     * that we find.
     */
    void checkForCollisions(ProcessUploadService pus) {
        log.debug("Checking ${getTdSize()} TDs for collisions...");

        int total = getTdSize() + getTipSize();
        int current = 0;

        List tdCollisions = []
        List affectedTdIndexes = []
        for( int i = 0; i < getTdSize(); i++ ) {
            TrustmarkDefinition td = getTd(i);

            for (int j = 0; j < getTdSize(); j++) {
                if (i == j || alreadyHasCollision(tdCollisions, i, j))
                    continue;

                TrustmarkDefinition td2 = getTd(j);
                if( sameId(td.getMetadata(), td2.getMetadata()) ) {
                    log.warn("Found TD ID Collision ($i, $j): "+td2.getMetadata().getIdentifier().toString());
                    log.warn("     [TD1 Name: "+td.getMetadata().getName()+", v"+td.getMetadata().getVersion()+"]");
                    log.warn("     [TD2 Name: "+td2.getMetadata().getName()+", v"+td2.getMetadata().getVersion()+"]");
                    tdCollisions.add([
                            type  : "TD",
                            i     : i,
                            j     : j,
                            artifact_i  : td,
                            artifact_j  : td2,
                            reason: "ID",
                            collision: td.getMetadata().getIdentifier().toString()
                    ])
                    affectedTdIndexes.add(i);
                    affectedTdIndexes.add(j);
                } else if (areNameAndVersionTheSame(td.getMetadata(), td2.getMetadata())) {
                    log.warn("Found TD Name Collision ($i, $j): "+td2.getMetadata().getName())
                    tdCollisions.add([
                            type  : "TD",
                            i     : i,
                            j     : j,
                            artifact_i  : td,
                            artifact_j  : td2,
                            reason: "NAME_VERSION",
                            collision: td.getMetadata().getName()+", v"+td.getMetadata().getVersion()
                    ])
                    affectedTdIndexes.add(i);
                    affectedTdIndexes.add(j);
                }
            }

            current++;
            if( (current % 50) == 0 ) {
                pus.setProcessStatus(this.uploadId, ProcessUploadService.ProcessPhase.COLLISION_CHECK, "The system has resolved your ${getTdSize()} TDs and ${getTipSize()} TIPs, and they are being checked for any collisions...", ProcessUploadService.getPercent(current, total));
            }
        }

        log.debug("Checking ${getTipSize()} TIPs for collisions...");
        List tipCollisions = []
        List affectedTipIndexes = []
        for( int i = 0; i < getTipSize(); i++ ) {
            TrustInteroperabilityProfile tip = getTip(i);
            for (int j = 0; j < getTipSize(); j++) {
                if (i == j || alreadyHasCollision(tipCollisions, i, j))
                    continue;

                TrustInteroperabilityProfile tip2 = getTip(j);

                if ( sameId(tip, tip2) ) {
                    log.warn("Found TIP ID Collision ($i, $j): "+tip.getIdentifier().toString());
                    log.warn("     [TIP1 Name: "+tip.getName()+", v"+tip.getVersion()+"]");
                    log.warn("     [TIP2 Name: "+tip2.getName()+", v"+tip2.getVersion()+"]");
                    tipCollisions.add([
                            type  : "TIP",
                            i     : i,
                            j     : j,
                            artifact_i  : tip,
                            artifact_j  : tip2,
                            reason: "ID",
                            collision: tip.getIdentifier().toString()
                    ])
                    affectedTipIndexes.add(i);
                    affectedTipIndexes.add(j);
                } else if (areNameAndVersionTheSame(tip, tip2)) {
                    log.warn("Found TIP Name Collision ($i, $j): "+tip.getName()+", v"+tip.getVersion())
                    tipCollisions.add([
                            type  : "TIP",
                            i     : i,
                            j     : j,
                            artifact_i  : tip,
                            artifact_j  : tip2,
                            reason: "NAME_VERSION",
                            collision: tip.getName()+", v"+tip.getVersion()
                    ])
                    affectedTipIndexes.add(i);
                    affectedTipIndexes.add(j);
                }

            }

            current++;
            if( (current % 50) == 0 ) {
                pus.setProcessStatus(this.uploadId, ProcessUploadService.ProcessPhase.COLLISION_CHECK, "The system has resolved your ${getTdSize()} TDs and ${getTipSize()} TIPs, and they are being checked for any collisions...", ProcessUploadService.getPercent(current, total));
            }
        }

        pus.setProcessStatus(this.uploadId, ProcessUploadService.ProcessPhase.COLLISION_CHECK, "The system has resolved your ${getTdSize()} TDs and ${getTipSize()} TIPs, and they are being checked for any collisions...", 100);

        List allCollisions = []
        allCollisions.addAll(tdCollisions);
        allCollisions.addAll(tipCollisions);
        collisionData = [
                hasCollisions: (tdCollisions.size() > 0 || tipCollisions.size() > 0),
                tdCollisions: tdCollisions,
                tipCollisions: tipCollisions,
                allCollisions: allCollisions,
                affectedTdIndexes: affectedTdIndexes,
                affectedTipIndexes: affectedTipIndexes
        ];
    }

    Map getCollisionData(ProcessUploadService pus) {
        if( collisionData == null )
            checkForCollisions(pus);
        return collisionData;
    }


    private boolean alreadyHasCollision(List collisions, int i, int j) {
        for( Map collision : collisions ){
            if( indexMatch(collision, i, j) ){
                return true;
            }
        }
        return false;
    }

    private boolean indexMatch(Map collision, int i, int j ){
        return (collision.i == i && collision.j == j) ||
                (collision.i == j && collision.j == i);
    }

    private boolean sameId(TrustmarkFrameworkIdentifiedObject t1, TrustmarkFrameworkIdentifiedObject t2){
        String id1 = t1?.getIdentifier()?.toString() ?: '';
        String id2 = t2?.getIdentifier()?.toString() ?: '';
        boolean same = id1.equalsIgnoreCase(id2);
        return same;
    }

    private boolean areNameAndVersionTheSame(TrustmarkFrameworkIdentifiedObject t1, TrustmarkFrameworkIdentifiedObject t2 ){
        String name1 = t1?.getName()?.toLowerCase() ?: '';
        String name2 = t2?.getName()?.toLowerCase() ?: '';
        String version1 = t1?.getVersion()?.toLowerCase() ?: '';
        String version2 = t2?.getVersion()?.toLowerCase() ?: '';

        return name1.equals(name2) && version1.equals(version2);
    }


}
