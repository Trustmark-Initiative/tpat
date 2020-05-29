package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.TrustInteroperabilityProfileResolver
import edu.gatech.gtri.trustmark.v1_0.io.TrustmarkDefinitionResolver
import edu.gatech.gtri.trustmark.v1_0.model.AbstractTIPReference
import edu.gatech.gtri.trustmark.v1_0.util.UrlUtils
import groovy.json.JsonOutput

/**
 * TODO: Insert Comment Here
 * <br/><br/>
 * @author brad
 * @date 5/26/17
 */
class RebuildReferencesService {

    static transactional = false;

    //==================================================================================================================
    //  Service Methods
    //==================================================================================================================
    /**
     * Given a Version Set name, this method will rebuild the ArtifactReference table associated with that verison set
     * by scanning each TIP and downloading any needed references.
     * <br/><br/>
     * @param vsName
     */
    public void rebuildReferences(String vsName){
        setStatus("Building references...", 0);
        log.debug("Collecting TIP Link IDs...");
        List<Long> tipLinkIds = []
        VersionSet.withTransaction {
            VersionSet vsIn = VersionSet.findByName(vsName);
            List<VersionSetTIPLink> tipLinks = VersionSetTIPLink.findAllByVersionSet(vsIn);
            for( VersionSetTIPLink tipLink : tipLinks ){
                tipLinkIds.add(tipLink.id);
            }
        }

        log.info("Successfully found ${tipLinkIds.size()} TIP Links, going through them and building a graph...");
        for( int i = 0; i < tipLinkIds.size(); i++ ){
            Long tipLinkId = tipLinkIds.get(i);
            try {
                VersionSetTIPLink.withTransaction {
                    VersionSetTIPLink tipLink = VersionSetTIPLink.get(tipLinkId);
                    log.debug("Reading TIP[${tipLink.trustInteroperabilityProfile.name}]...")
                    edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip =
                            FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class).resolve(tipLink.trustInteroperabilityProfile.artifact.content.toFile());
                    handleReferences(tipLink, tip);
                }
            }catch(Throwable T){
                log.error("Error saving references on TIP[id=${tipLinkId}]", T)
            }

            setStatus("Building references...", getPercent(i, tipLinkIds.size()));
        }

        setStatus("Successfully rebuilt ALL references.", 100);
        log.info("Successfully rebuilt ALL references.");
    }

    public static String getStatusVariable(){
        return RebuildReferencesService.class.name + ".STATUS";
    }

    //==================================================================================================================
    //  Helper Methods
    //==================================================================================================================

    protected void handleReferences(VersionSetTIPLink tipLink, edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip){
        for( AbstractTIPReference ref : tip.getReferences() ){
            ArtifactReference artifactReference = new ArtifactReference(versionSet: tipLink.versionSet, sourceTip: tipLink.trustInteroperabilityProfile);
            try {
                log.debug("  -> Processing Reference[${ref.getIdentifier().toString()}]...")
                if (ref.isTrustmarkDefinitionRequirement()) {
                    handleTdReference(tipLink.getVersionSet(), artifactReference, ref);
                } else { // Is trust profile...
                    handleTipReference(tipLink.getVersionSet(), artifactReference, ref);
                }
            }catch(Throwable t2){
                log.error("Error saving reference[${ref.getIdentifier()}]!", t2);
            }
            artifactReference.save(failOnError: true);
        }
    }

    protected void handleTdReference(VersionSet vs, ArtifactReference artifactReference, AbstractTIPReference ref){
        String hql = "from VersionSetTDLink where "+
                        "versionSet = :vs and "+
                            "((tdIdentifier = :id) or "+
                            "(trustmarkDefinition.name = :name and trustmarkDefinition.tdVersion = :version))";
        VersionSetTDLink link = VersionSetTDLink.find(hql, [vs: vs, id: ref.getIdentifier().toString(), name: ref.getName(), version: ref.getVersion()])
        if( link ){
            artifactReference.destinationTd = link.trustmarkDefinition;
        }else{
            ExternalReference externalReference = new ExternalReference();
            URL remoteURL = UrlUtils.ensureFormatParameter(ref.getIdentifier().toURL(), "json");
            edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition remoteTd =
                    FactoryLoader.getInstance(TrustmarkDefinitionResolver.class).resolve(remoteURL);
            externalReference.isTrustmarkDefinition = true;
            externalReference.identifier = remoteTd.getMetadata().getIdentifier().toString();
            externalReference.name = remoteTd.getMetadata().getName();
            externalReference.theVersion = remoteTd.getMetadata().getVersion();
            externalReference.description = remoteTd.getMetadata().getDescription();
            externalReference.save(failOnError: true);

            artifactReference.externalReference = externalReference;
        }
    }


    protected void handleTipReference(VersionSet vs, ArtifactReference artifactReference, AbstractTIPReference ref){
        String hql = "from VersionSetTIPLink where "+
                        "versionSet = :vs and "+
                            "((tipIdentifier = :id) or "+
                            "(trustInteroperabilityProfile.name = :name and trustInteroperabilityProfile.tipVersion = :version))";
        VersionSetTIPLink link = VersionSetTIPLink.find(hql, [vs: vs, id: ref.getIdentifier().toString(), name: ref.getName(), version: ref.getVersion()])
        if( link ){
            artifactReference.destinationTip = link.trustInteroperabilityProfile;
        } else {
            ExternalReference externalReference = new ExternalReference();
            URL remoteURL = UrlUtils.ensureFormatParameter(ref.getIdentifier().toURL(), "json");
            edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile remoteTip2 =
                    FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class).resolve(remoteURL);
            externalReference.isTrustProfile = true;
            externalReference.identifier = remoteTip2.getIdentifier().toString();
            externalReference.name = remoteTip2.getName();
            externalReference.theVersion = remoteTip2.getVersion();
            externalReference.description = remoteTip2.getDescription();
            externalReference.save(failOnError: true);

            artifactReference.externalReference = externalReference;
        }
    }

    protected int getPercent(int top, int bottom){
        return (int) Math.floor((((double)top)/((double) bottom) * 100.0d));
    }
    protected void setStatus(String message, int percent){
        setStatus("SUCCESS", message, percent);
    }
    protected void setStatus(String statusTxt, String message, int percent){
        Map status = [status: statusTxt, message: message, percent: percent, done: percent == 100]
        String json = JsonOutput.toJson(status);
        SystemVariable.withTransaction {
            SystemVariable.storeProperty(getStatusVariable(), json);
        }
    }

}//end RebuildReferencesService