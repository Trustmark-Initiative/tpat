package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.Serializer
import edu.gatech.gtri.trustmark.v1_0.io.SerializerFactory
import edu.gatech.gtri.trustmark.v1_0.io.TrustInteroperabilityProfileResolver
import edu.gatech.gtri.trustmark.v1_0.io.TrustmarkDefinitionResolver
import edu.gatech.gtri.trustmark.v1_0.model.AbstractTIPReference
import edu.gatech.gtri.trustmark.v1_0.util.UrlUtils
import groovy.json.JsonOutput

/**
 * Responsible for building Keywords for TIPs (under the assumption they don't have their own).  Will NOT build keyword
 * if the TIP already has them.
 * <br/><br/>
 * @author brad
 * @date 5/26/17
 */
class TipKeywordsService {

    static transactional = false

    FileService fileService

    //==================================================================================================================
    //  Service Methods
    //==================================================================================================================
    /**
     * Given a Version Set name, this method will build keywords for all of the TIPs from the encompassed TDs.  It
     * recurses UP the tree from the bottom.  Note that this builds from the ArtifactReference table, and won't work properly
     * unless that table has data.
     * <br/><br/>
     * @param vsName
     */
    void buildKeywords(String vsName){
        setStatus("Building keywords...", 0)

        def tipsDone = [] // A list of VersionSetTIPLink ids that are already finished (for caching/speed purposes)

        log.debug("Collecting TIP Link IDs...")
        List<Long> tipLinkIds = []
        VersionSet.withTransaction {
            VersionSet vsIn = VersionSet.findByName(vsName)
            List<VersionSetTIPLink> tipLinks = VersionSetTIPLink.findAllByVersionSet(vsIn)
            for( VersionSetTIPLink tipLink : tipLinks ){
                tipLinkIds.add(tipLink.id)
            }
        }

        log.info("Successfully found ${tipLinkIds.size()} TIP Links, going through them and building keywords...")
        for( int i = 0; i < tipLinkIds.size(); i++ ){
            Long tipLinkId = tipLinkIds.get(i)
            try {
                VersionSetTIPLink.withTransaction {
                    VersionSetTIPLink tipLink = VersionSetTIPLink.get(tipLinkId)
                    if( !tipsDone.contains(tipLink.trustInteroperabilityProfile.id) ) {
                        GraphNode graphNode = buildGraph(tipLink)
                        addKeywordsBreadthFirst(graphNode, tipsDone)
                    }
                }
            }catch(Throwable T){
                log.error("Error saving keywords on TIP[id=${tipLinkId}]", T)
            }

            setStatus("Building references...", getPercent(i, tipLinkIds.size()))
        }


        setStatus("Successfully built TIP keywords.", 100)
        log.info("Successfully built TIP keywords.")
    }

    static String getStatusVariable(){
        return TipKeywordsService.class.name + ".STATUS"
    }

    //==================================================================================================================
    //  Helper Methods
    //==================================================================================================================
    protected void addKeywordsBreadthFirst(GraphNode graphNode, List<Long> tipsDone){
        // TODO
    }


    protected GraphNode buildGraph(VersionSetTIPLink tipLink){
        log.info("Building graph for TIPLink: "+tipLink.tipIdentifier)
        GraphNode gn = new GraphNode(tip: tipLink.trustInteroperabilityProfile)
        List<ArtifactReference> references = ArtifactReference.findAllBySourceTip(tipLink.trustInteroperabilityProfile)
        for( ArtifactReference ref : references ){
            if( ref.destinationTd != null ){
                GraphNode child = new GraphNode(td: ref.destinationTd)
                gn.getChildren().add(child)
            }else if( ref.destinationTip != null ){
                gn.getChildren().add(buildGraph(VersionSetTIPLink.findByVersionSetAndTrustInteroperabilityProfile(tipLink.versionSet, ref.destinationTip)))
            }
        }
    }//end buildGraph


    protected void buildKeywords(VersionSetTIPLink tipLink, edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip){
        VersionSet vs = tipLink.versionSet
        List<ArtifactReference> references = ArtifactReference.findAllBySourceTip(tipLink.trustInteroperabilityProfile)
        List<String> keywords = []
        List<String> keywordsCaseInsensitive = []
        for( ArtifactReference reference : references ){
            if( reference.destinationTd != null ){
                List<KeywordTDLink> keywordLinks = KeywordTDLink.findAllByTd(reference.destinationTd)
                for( KeywordTDLink keywordLink : keywordLinks ){
                    String name = keywordLink.getKeyword().getName()?.trim()
                    if( !keywordsCaseInsensitive.contains(name.toLowerCase()) ){
                        keywords.add(name)
                        keywordsCaseInsensitive.add(name.toLowerCase())
                    }
                }
            }else if( reference.destinationTip != null ){
                List<KeywordTIPLink> keywordLinks = KeywordTIPLink.findAllByTip(reference.destinationTip)
                for( KeywordTIPLink keywordLink : keywordLinks ){
                    String name = keywordLink.getKeyword().getName()?.trim()
                    if( !keywordsCaseInsensitive.contains(name.toLowerCase()) ){
                        keywords.add(name)
                        keywordsCaseInsensitive.add(name.toLowerCase())
                    }
                }
            }else{ // Download 3rd party artifact...
                ExternalReference externalReference = reference.externalReference
                URL url = UrlUtils.ensureFormatParameter(new URL(externalReference.identifier), "json")
                if( externalReference.isTrustProfile ){
                    edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile remoteTip =
                        FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class).resolve(url)
                    for( String keyword : remoteTip.getKeywords() ){
                        if( !keywordsCaseInsensitive.contains(keyword.toLowerCase()) ){
                            keywords.add(keyword)
                            keywordsCaseInsensitive.add(keyword.toLowerCase())
                        }
                    }
                }else{
                    edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition remoteTd =
                            FactoryLoader.getInstance(TrustmarkDefinitionResolver.class).resolve(url)
                    for( String keyword : remoteTd.getMetadata().getKeywords() ){
                        if( !keywordsCaseInsensitive.contains(keyword.toLowerCase()) ){
                            keywords.add(keyword)
                            keywordsCaseInsensitive.add(keyword.toLowerCase())
                        }
                    }
                }
            }
        }

        for( String keyword : keywords ) {
            tip.getKeywords().add(keyword)
        }
        Serializer serializer = FactoryLoader.getInstance(SerializerFactory.class).getJsonSerializer()
        File tempJson = File.createTempFile("tip-", ".json")
        serializer.serialize(tip, new FileWriter(tempJson))
        BinaryObject newBinary = fileService.createBinaryObject(tempJson, "SYSTEM", "application/json", "tip.json", "json")
        tipLink.trustInteroperabilityProfile.artifact = newBinary
        tipLink.trustInteroperabilityProfile.save(failOnError: true)


        for( String keyword : keywords ) {
            Keyword keywordInDb = Keyword.find("from Keyword where name ilike :name", keyword)
            if( keywordInDb == null ){
                keywordInDb = new Keyword(name: keyword)
                keywordInDb.save(failOnError: true)
            }

            KeywordTIPLink link = new KeywordTIPLink(versionSet: vs, keyword: keywordInDb, tip: tipLink.trustInteroperabilityProfile)
            link.save(failOnError: true)
        }
    }

    protected List<VersionSetTIPLink> orderTipLinks(List<VersionSetTIPLink> links){
        log.debug("Ordering TIP Links...")
        int total = links.size()
        List<VersionSetTIPLink> orderedLinks = []
        List<VersionSetTIPLink> notOrderedLinks = []
        notOrderedLinks.addAll(links)

        while( notOrderedLinks.size() > 0 ){
            VersionSetTIPLink next = notOrderedLinks.remove(0) // Remove the first.
            if( usesOnlyTdsOrTheseTips(next, orderedLinks) ){
                orderedLinks.add(next)
            }else{
                notOrderedLinks.add(next) // Put it back at the end of the list.
            }
            setStatus("Ordering TIP links...", getPercent(orderedLinks.size(), total))
            log.debug("Not ordered links size: "+notOrderedLinks.size())
        }

        return orderedLinks
    }//end orderTipLinks()

    protected boolean usesOnlyTdsOrTheseTips(VersionSetTIPLink link, List<VersionSetTIPLink> orderedAlready){
        List<ArtifactReference> references = ArtifactReference.findAllBySourceTip(link.trustInteroperabilityProfile)
        for( ArtifactReference ref : references ){
            if(ref.getDestinationTip() != null){
                if( !containsTip(orderedAlready, ref.getDestinationTip()) ){
                    return false
                }
            }
        }
        return true
    }

    protected boolean containsTip(List<VersionSetTIPLink> links, TrustInteroperabilityProfile tip){
        for( VersionSetTIPLink link : links){
            if(link.getTrustInteroperabilityProfile().getIdentifier().equalsIgnoreCase(tip.getIdentifier()) ){
                return true
            }
        }
        return false
    }



    protected int getPercent(int top, int bottom){
        return (int) Math.floor((((double)top)/((double) bottom) * 100.0d))
    }
    protected void setStatus(String message, int percent){
        setStatus("SUCCESS", message, percent)
    }
    protected void setStatus(String statusTxt, String message, int percent){
        Map status = [status: statusTxt, message: message, percent: percent, done: percent == 100]
        String json = JsonOutput.toJson(status)
        SystemVariable.withTransaction {
            SystemVariable.storeProperty(getStatusVariable(), json)
        }
    }

    class GraphNode {
        TrustInteroperabilityProfile tip
        TrustmarkDefinition td
        List<GraphNode> children = []
    }

}//end RebuildReferencesService
