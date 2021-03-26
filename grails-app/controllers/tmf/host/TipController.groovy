package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.SerializerFactory
import edu.gatech.gtri.trustmark.v1_0.io.TrustInteroperabilityProfileResolver
import edu.gatech.gtri.trustmark.v1_0.model.AbstractTIPReference
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkFrameworkIdentifiedObject
import edu.gatech.gtri.trustmark.v1_0.util.TipTreeNode
import edu.gatech.gtri.trustmark.v1_0.util.TrustInteroperabilityProfileUtils
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonOutput
import org.apache.commons.lang.StringUtils
import tmf.host.util.LinkHelper

import javax.servlet.ServletException
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import java.util.regex.Pattern

/**
 * Responsible for displaying all tmf.host.TrustInteroperabilityProfile pages.
 */
class TipController extends AbstractTFObjectAwareController {

    public static final Integer DEFUALT_MAX = 25

    private static Boolean TOP_LEVEL_TIPS_LOCK = Boolean.TRUE
    private static List<String> TOP_LEVEL_TIPS_CACHE = null

    FileService fileService

    /**
     * Returns the count of all TrustInteroperabilityProfiles.
     */
    def count() {
        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if( !vs ){
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }
        log.info("Returning the count of all TrustInteroperabilityProfiles...")
        int count = VersionSetTIPLink.countByVersionSet(vs)

        if( response.format == "html" || response.format == "all" ){ // Note all is the special case format!
            return render(contentType: 'text/plain', text: "There are "+count+" TrustInteroperabilityProfiles.")
        }else if( response.format == "xml" ){
            return render(contentType: 'text/xml', text: "<TrustInteroperabilityProfiles total=\"${count}\" />")
        }else if( response.format == "json" ){
            return render(contentType: 'application/json', text: "{\"totalCount\": ${count}}")
        }else{
            throw new ServletException("ContentType ${response.format} is not supported.")
        }
    }

    /**
     * Lists all TrustInteroperabilityProfiles in the database in all formats (ie, HTML, XML and JSON).
     */
    def list() {

        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if( !vs ){
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }

        log.debug("Showing the tmf.host.TrustInteroperabilityProfile list...")
        if( params.max == null )
            params.max = DEFUALT_MAX.toString()
        if( params.offset == null )
            params.offset = "0"
        if( params.sort == null )
            params.sort = "trustInteroperabilityProfile.name"
        if( params.order == null )
            params.order = "asc"

        params.max = Math.min(100, Integer.parseInt(params.max)).toString(); // we will display at most 100.

        List<TrustInteroperabilityProfile> tips = []
        Integer totalCount = -1
        log.debug("Selecting all TIPs (including deprecated) for VersionSet[${vs.name}] params=${params}")
        tips = VersionSetTIPLink.executeQuery(
                "select link.trustInteroperabilityProfile from VersionSetTIPLink link where link.versionSet = :vs",
                [vs: vs], params)
        totalCount = VersionSetTIPLink.countByVersionSet(vs)

        if( response.format == "html" || response.format == "all" ){ // Note all is the special case format!
            log.debug("Displaying format HTML...")
            [tips : tips, tipCount: totalCount]
        }else if( response.format == "xml" ){
            log.debug("Displaying format XML...")
            return render(contentType: 'text/xml', text: buildTipXmlList(tips, totalCount, params))
        }else if( response.format == "json" ){
            log.debug("Displaying format JSON...")
            Map responseJson = [
                    trustInteroperabilityProfilesCount: tips.size(),
                    totalCount: totalCount,
                    offset: Integer.parseInt(params.offset),
                    max: Integer.parseInt(params.max),
                    _links : [
                            "self" : [ href: createLink(controller: 'tip', action: 'list', params: params, absolute: true)],
                            "next" : buildNextListLink(params, totalCount),
                            "prev" : buildPrevListLink(params, totalCount)
                    ]
            ]

            List tipJson = []
            for( TrustInteroperabilityProfile tip : tips ){
                tipJson.add(buildTipJson(tip))
            }
            responseJson.put("trustInteroperabilityProfiles", tipJson)


            return render(contentType: 'application/json', text: JsonOutput.prettyPrint(JsonOutput.toJson(responseJson)))
        }else{
            throw new ServletException("ContentType ${response.format} is not supported.")
        }

    }

    @Secured("ROLE_ADMIN")
    def listPrimary() {
        log.info("Displaying all Primary TIPs...")
        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if( !vs ){
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }

        log.debug("Listing all Top Level TIPs...")
        List<VersionSetTIPLink> topLevelTipLinks = VersionSetTIPLink.findAllByVersionSetAndPrimaryTIP(vs, true, [max: 1000, sort: 'trustInteroperabilityProfile.name', order: 'asc'])
        List<TrustInteroperabilityProfile> topLevelTips = []
        for( VersionSetTIPLink link : topLevelTipLinks ?: [] ){
            topLevelTips.add(link.trustInteroperabilityProfile)
        }
        log.debug("Discovered @|cyan ${topLevelTips.size()}|@ top level tips, rendering...")

        [topLevelTips: topLevelTips, vs: vs]
    }

    /**
     * Makes a TIP primary in the current version set.
     */
    @Secured("ROLE_ADMIN")
    def modifyPrimary() {
        log.info("Modifying primary TIP...")
        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if( !vs ){
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }

        if( StringUtils.isBlank(params.name) || StringUtils.isBlank(params.version) ){
            log.warn("Missing either parameter name or version - both are required")
            throw new ServletException("Missing parameter name or version, both are required.")
        }
        boolean primary = false
        if( StringUtils.isNotBlank(params.primary) ){
            primary = params.boolean('primary')
        }

        VersionSetTIPLink.withTransaction {
            log.debug("Primary = ${primary}")
            VersionSetTIPLink tipLink = VersionSetTIPLink.find(
                    "from VersionSetTIPLink link where " +
                            "link.versionSet = :vs and " +
                            "link.trustInteroperabilityProfile.name = :name and " +
                            "link.trustInteroperabilityProfile.tipVersion = :version",
                    [vs: vs, name: params.name, version: params.version])

            if (tipLink == null) {
                log.warn("Could not find any tip where name = ${params.name} and version = ${params.version}!")
                throw new ServletException("Could not find any tip where name = ${params.name} and version = ${params.version}!")
            }

            log.debug("Located TIP ${tipLink.trustInteroperabilityProfile.name} [linkId=${tipLink.id}] to set as primary...")

            tipLink.primaryTIP = primary
            tipLink.save(failOnError: true, flush: true)
        }

        if( primary ) {
            flash.message = 'Successfully marked TIP ' + params.name + ' as primary.'
        }else{
            flash.message = 'Successfully removed primary designation from TIP ' + params.name
        }

        log.info("Redirecting back to display page...")
        redirect(action: 'listPrimary')
    }

    /**
     * Displays tmf.host.TrustInteroperabilityProfile by tipTree.
     */
    def tipTree() {
        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if( !vs ){
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }


        log.debug("Listing all Top Level TIPs...")
        List<VersionSetTIPLink> topLevelTipLinks = VersionSetTIPLink.findAllByVersionSetAndPrimaryTIP(vs, true, [max: 1000, sort: 'trustInteroperabilityProfile.name', order: 'asc'])
        List<TrustInteroperabilityProfile> topLevelTips = []
        for( VersionSetTIPLink link : topLevelTipLinks ?: [] ){
            topLevelTips.add(link.trustInteroperabilityProfile)
        }
        log.debug("Discovered @|cyan ${topLevelTips.size()}|@ top level tips, rendering...")

        [topLevelTips: topLevelTips]

    }//end tipTree()

    /**
     * Returns JSON for all top level tips.
     */
    def topLevelTipsListing() {
        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if( !vs ){
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }

        log.debug("Listing all Top Level TIPs...")
        List<VersionSetTIPLink> topLevelTipLinks = VersionSetTIPLink.findAllByVersionSetAndPrimaryTIP(vs, true, [max: 1000, sort: 'trustInteroperabilityProfile.name', order: 'asc'])
        List<TrustInteroperabilityProfile> topLevelTips = []
        for( VersionSetTIPLink link : topLevelTipLinks ?: [] ){
            topLevelTips.add(link.trustInteroperabilityProfile)
        }
        log.debug("Discovered @|cyan ${topLevelTips.size()}|@ top level tips, rendering...")
        if( response.format == "json" ){
            log.debug("Displaying format JSON...")
            Map responseJson = [
                    trustInteroperabilityProfilesCount: topLevelTips.size(),
                    totalCount: topLevelTips.size(),
                    _links : [
                            "self" : [ href: createLink(controller: 'tip', action: 'topLevelTipsListing', params: params, absolute: true)],
                            "next" : null,
                            "prev" : null
                    ]
            ]

            List tipJson = []
            for( TrustInteroperabilityProfile tip : topLevelTips ){
                tipJson.add(buildTipJson(tip))
            }
            responseJson.put("trustInteroperabilityProfiles", tipJson)

            return render(contentType: 'application/json', text: JsonOutput.prettyPrint(JsonOutput.toJson(responseJson)))
        }else{
            throw new ServletException("ContentType ${response.format} is not supported.")
        }
    }

    /**
     * viewTipTree returns Tree JSON for the given TIP identifier.
     * <br/><br/>
     * @return
     */
    def viewTipTree(){
        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if( !vs ){
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }

        log.debug("Displaying Primary TIPs / TIP Tree for VersionSetTIPLink.TrustInteroperabilityProfile[id=@|cyan ${params.id}|@]...")
        if( StringUtils.isBlank(params.id) ){
            log.warn("Given blank ID, cannot display!")
            throw new ServletException("Invalid blank id, cannot display tmf.host.TrustInteroperabilityProfile.")
        }

        VersionSetTIPLink tipLink = VersionSetTIPLink.find("from VersionSetTIPLink where versionSet = :vs and trustInteroperabilityProfile.id = :id", [vs: vs, id: (long) params.int("id")])
        TrustInteroperabilityProfile databaseTip = tipLink?.trustInteroperabilityProfile
        if( databaseTip == null ){
            log.warn("Invalid tmf.host.TrustInteroperabilityProfile identifier: ${params.id}")
            throw new ServletException("No such tmf.host.TrustInteroperabilityProfile identifier [${params.id}] found.")
        }

        String jsonOutput = null
        TipTreeCache cache = TipTreeCache.findByTip(databaseTip)
        if( cache && StringUtils.isBlank(params.resetCache)){
            log.debug("Successfully found Primary TIPs / TIP Tree JSON in cache!")
            jsonOutput = cache.binaryObject.content.toFile().text
        }else{
            log.info("Successfully Found TIP[${params.id}]: ${databaseTip.name}, v. ${databaseTip.tipVersion} ${databaseTip.identifier} | Downloading Primary TIPs / TIP Tree...")
            TipTreeNode tipTreeNode = FactoryLoader.getInstance(TrustInteroperabilityProfileUtils.class).buildTipTree(new URI(databaseTip.getIdentifier()))

            log.debug("Displaying format JSON...")
            Map treeJson = buildTreeJson(tipTreeNode)
            Map responseJson = [
                    "id" : databaseTip.id,
                    "tree" : treeJson,
                    "tds" : buildTdListData(tipTreeNode),
                    "tips" : buildTipListData(tipTreeNode)
            ]

            jsonOutput = JsonOutput.prettyPrint(JsonOutput.toJson(responseJson))
            log.debug("Tree[${params.id}] JSON: \n"+jsonOutput)

            File tempFile = File.createTempFile("tip-tree-", ".json")
            tempFile << jsonOutput
            BinaryObject bo = fileService.createBinaryObject(tempFile, "System", "application/json", "tip-tree.json", "json")
            cache = new TipTreeCache(tip: databaseTip, binaryObject: bo)
            cache.save(failOnError: false); // Silently fail here.
        }

        // TODO Support other content types?
        return render(contentType: 'application/json', text: jsonOutput)
    }


    private String sanitizeRegex(String regex){
        // TODO Sanitize this for use in database query directly.
        return regex
    }

    /**
     * Responsible for displaying a tmf.host.TrustInteroperabilityProfile which has the given Database ID.
     */
    def listByName() {
        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if (!vs) {
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }

        log.debug("Displaying TrustInteroperabilityProfiles[name ~= @|green ${params.nameRegex}|@, version ~= @|green ${params.version}|@] on VersionSet[@cyan ${vs.name}|@]...")
        if (StringUtils.isBlank(params.nameRegex) ) {

            if( response.format == "html" || response.format == "all" ) { // Note all is the special case format!
                return [versionSet: vs, tips: [], tipCount: 0]
            }else{
                throw new ServletException("Missing required parameter 'nameRegex'")
            }
        }
        if( StringUtils.isBlank(params.versionRegex) ){
            log.warn("No 'versionRegex' parameter was given, assuming you didn't want to search that field...")
        }

        def max = Math.min(100, params.int("max") ?: 10)
        def offset = params.int("offset") ?: 0

        List<VersionSetTIPLink> tipLinks = []
        Integer totalCount = -1
        if( StringUtils.isNotBlank(params.versionRegex) ){
            log.info("Searching TIPs for name[@|green ${params.nameRegex}|@] and version[@|cyan ${params.versionRegex}|@]...")
            tipLinks = VersionSetTIPLink.executeQuery(
                    "from VersionSetTIPLink link where link.versionSet = :vs and "+
                            "regexp(link.trustInteroperabilityProfile.name, :nameRegex) = 1 and "+
                            "regexp(link.trustInteroperabilityProfile.tipVersion, :versionRegex) = 1 "+
                            "order by link.trustInteroperabilityProfile.name asc",
                    [vs: vs, nameRegex: params.nameRegex, versionRegex: params.versionRegex],
                    [max: max, offset: offset]
            )
            def countResult = VersionSetTIPLink.executeQuery(
                    "select count(*) from VersionSetTIPLink link where link.versionSet = :vs and "+
                            "regexp(link.trustInteroperabilityProfile.name, :nameRegex) = 1 and "+
                            "regexp(link.trustInteroperabilityProfile.tipVersion, :versionRegex) = 1",
                    [vs: vs, nameRegex: params.nameRegex, versionRegex: params.versionRegex]
            )
            totalCount = countResult?.get(0)
        }else{
            log.info("Searching TIPs for name[@|green ${params.nameRegex}|@]...")
            tipLinks = VersionSetTIPLink.executeQuery(
                    "from VersionSetTIPLink link where link.versionSet = :vs and "+
                            "regexp(link.trustInteroperabilityProfile.name, :nameRegex) = 1 "+
                            "order by link.trustInteroperabilityProfile.name asc",
                    [vs: vs, nameRegex: params.nameRegex],
                    [max: max, offset: offset]
            )
            def countResult = VersionSetTIPLink.executeQuery(
                    "select count(*) from VersionSetTIPLink link where link.versionSet = :vs and "+
                            "regexp(link.trustInteroperabilityProfile.name, :nameRegex) = 1",
                    [vs: vs, nameRegex: params.nameRegex]
            )
            totalCount = countResult?.get(0)
        }

        log.info("Successfully resolved @|cyan ${tipLinks.size()}|@ TIPs of @|green ${totalCount}|@")
        List<TrustInteroperabilityProfile> tips = []
        for( VersionSetTIPLink link : tipLinks ){
            tips.add(link.trustInteroperabilityProfile)
        }

        if( response.format == "html" || response.format == "all" ){ // Note all is the special case format!
            log.debug("Displaying format HTML...")
            [versionSet: vs, tips : tips, tipCount: totalCount]
        }else if( response.format == "xml" ){
            log.debug("Displaying format XML...")
            return render(contentType: 'text/xml', text: buildTipXmlList(tips, totalCount, params))
        }else if( response.format == "json" ){
            log.debug("Displaying format JSON...")
            Map responseJson = [
                    trustInteroperabilityProfilesCount: tips.size(),
                    totalCount: totalCount,
                    offset: offset,
                    max: max,
                    _links : [
                            "self" : [ href: createLink(controller: 'tip', action: 'list', params: params, absolute: true)],
                            "next" : buildNextListLink(params, totalCount),
                            "prev" : buildPrevListLink(params, totalCount)
                    ]
            ]

            List tipJson = []
            for( TrustInteroperabilityProfile tip : tips ){
                tipJson.add(buildTipJson(tip))
            }
            responseJson.put("trustInteroperabilityProfiles", tipJson)

            return render(contentType: 'application/json', text: JsonOutput.prettyPrint(JsonOutput.toJson(responseJson)))
        }else{
            throw new ServletException("ContentType ${response.format} is not supported.")
        }



    }

    /**
     * Responsible for displaying a tmf.host.TrustInteroperabilityProfile which has the given Database ID.
     */
    def view(){
        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if( !vs ){
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }

        log.debug("Displaying TrustInteroperabilityProfile[${params.id}] on VersionSet[${vs.name}]...")

        if( StringUtils.isBlank(params.id) && StringUtils.isBlank(params.tipName)){
            log.warn("Given blank ID, cannot display!")
            throw new ServletException("Either parameter 'id' or parameter 'tipName' is required.  Cannot display TrustInteroperabilityProfile due to these missing parameters.")
        }

        TrustInteroperabilityProfile tip = null

        if( StringUtils.isNotBlank(params.id) ){
            VersionSetTIPLink tipLink = VersionSetTIPLink.findByVersionSetAndTipIdentifier(vs, params.id)
            if( tipLink == null ){
                log.warn("Invalid TrustInteroperabilityProfile identifier: ${params.id} on VersionSet[${vs.name}]")
                throw new ServletException("No such TrustInteroperabilityProfile identifier [${params.id}] found.")
            }
            tip = tipLink.trustInteroperabilityProfile
        }else{ // using name/id combo
            log.debug("Searching for TIP[${params.tipName}, v ${params.tipVersion}] in version set[${vs.name}]")
            String id = "%/" + params.tipName + "/" + params.tipVersion + "%"
            List<VersionSetTIPLink> links = VersionSetTIPLink.executeQuery("from VersionSetTIPLink where versionSet = :vs and tipIdentifier like :id", [vs: vs, id: id])
            if( links.size() > 1 ){
                log.warn("Ambiguous identification ${params.tipName}, ${params.tipVersion} given.")
                throw new ServletException("Ambiguous identification ${params.tipName}, ${params.tipVersion} given.")
            }else if( links.size() == 0 ){
                log.warn("TIP identification ${params.tipName}, ${params.tipVersion} not found.")
                throw new ServletException("TIP identification ${params.tipName}, ${params.tipVersion} not found.")
            }
            tip = links.get(0).trustInteroperabilityProfile
        }

        log.info("Successfully Found TIP[${params.id}]: ${tip.name}, v. ${tip.tipVersion} ${tip.identifier} | Displaying to requester...")
        BinaryObject artifact = tip.getArtifact()

        List<ArtifactReference> references = ArtifactReference.findAllByVersionSetAndDestinationTip(vs, tip)

        TrustInteroperabilityProfileResolver tipResolver = FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class)
        edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip2 = tipResolver.resolve(new FileReader(artifact.content.toFile()))

        for(AbstractTIPReference ref : tip2.references) {
            log.info( "Returning TIP: [name=${ref.name} | version=${ref.version} | identifier=${ref.identifier} | description=${ref.description}]...")
        }

        SerializerFactory serializerFactory = FactoryLoader.getInstance(SerializerFactory.class)
        StringWriter contentWriter = new StringWriter()
        String contentType = null

        log.info "Returning format: [request=${request.format} | response=${response.format}]..."
        if( response.format == "html" || response.format == "all" ){ // Note all is the special case format!
            return [databaseTip: tip, tip: tip2, references: references]
        }else if( response.format == "json" ){
            contentType = "application/json"
            serializerFactory.getJsonSerializer().serialize(tip2, contentWriter)
        }else if( response.format == "xml" ){
            contentType = "text/xml"
            serializerFactory.getXmlSerializer().serialize(tip2, contentWriter)
        }else{
            throw new ServletException("ContentType ${response.format} is not supported.")
        }

        String content = contentWriter.toString()
        if( contentType == "text/html" ) {
            content = addJsonXmlLinks(tip, content)
        }

        return render(contentType: contentType, text: content)
    }//end view()

    /**
     * A dumb method for forwarding to the list page.
     */
    def index() {
        redirect(action: 'list')
    }
    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================
    private Map buildTipListData(TipTreeNode tipTreeNode){
        Map tipData = [:]
        _buildTipListDataRecursively(tipTreeNode, tipData)
        return tipData
    }


    private void _buildTipListDataRecursively(TipTreeNode tipTreeNode, Map tdData){
        if( !tdData.containsKey(tipTreeNode.getIdentifier().toString()) ){
            tdData.put(tipTreeNode.getIdentifier().toString().hashCode(), buildTipJson(tipTreeNode.getTrustInteropProfile()))
        }
        for( TipTreeNode child : tipTreeNode.getChildren() ){
            if( child.isTrustInteropProfile() ){
                if( !tdData.containsKey(child.getIdentifier().toString()) ){
                    _buildTipListDataRecursively(child, tdData)
                }
            }
        }
    }

    private Map buildTdListData(TipTreeNode tipTreeNode){
        Map tdData = [:]
        _buildTdListDataRecursively(tipTreeNode, tdData)
        return tdData
    }//end buildTdListData()

    private void _buildTdListDataRecursively(TipTreeNode tipTreeNode, Map tdData){
        for( TipTreeNode child : tipTreeNode.getChildren() ){
            if( child.isTrustmarkDefinition() ){
                if( !tdData.containsKey(child.getIdentifier().toString()) ){
                    tdData.put(child.getIdentifier().toString().hashCode(), buildTdJson(child.getTrustmarkDefinition()))
                }
            }else{
                _buildTdListDataRecursively(child, tdData)
            }
        }
    }


    private String nextUniqueId(Map counterData){
        String id = counterData.baseId + "_" + counterData.count
        counterData.count++
        return id
    }

    private Map buildTreeJson(TipTreeNode tipTreeNode){
        Map treeJson = [uniqueId: tipTreeNode.getIdentifier().toString().hashCode(), Identifier: tipTreeNode.getIdentifier().toString(), tds: [], tips: []]

        for( TipTreeNode child : tipTreeNode.getChildren() ){
            if( child.isTrustInteropProfile() ){
                treeJson.tips.add(buildTreeJson(child))
            }else{ // Is TD
                treeJson.tds.add([
                        uniqueId: child.getIdentifier().toString().hashCode(),
                        Identifier: child.getIdentifier().toString()
                ])
            }
        }

        return treeJson
    }//end buildTreeJson()


    private String buildTipXmlList(List<TrustInteroperabilityProfile> tips, int totalCount, Object params){
        StringWriter xmlStringOut = new StringWriter()
        XMLOutputFactory xof = XMLOutputFactory.newInstance()
        XMLStreamWriter xmlWriter = xof.createXMLStreamWriter(xmlStringOut)

        xmlWriter.writeStartDocument("UTF-8", "1.0")
        xmlWriter.writeStartElement("TrustInteroperabilityProfiles")

        xmlWriter.writeAttribute("count", tips.size()+"")
        xmlWriter.writeAttribute("total", totalCount + "")
        xmlWriter.writeAttribute("max", params.max + "")
        xmlWriter.writeAttribute("offset", params.offset + "")
        xmlWriter.writeAttribute("self", createLink(controller: 'tip', action: 'list', params: params, absolute: true))
        xmlWriter.writeAttribute("next", buildNextListLink(params, totalCount)?.href+"")
        xmlWriter.writeAttribute("prev", buildPrevListLink(params, totalCount)?.href+"")

        for( TrustInteroperabilityProfile tip : tips ){

            xmlWriter.writeStartElement("tmf.host.TrustmarkDefinition")
            xmlWriter.writeAttribute("html",  LinkHelper.getLink(request, tip, 'html'))
            xmlWriter.writeAttribute("json",  LinkHelper.getLink(request, tip, 'json'))
            xmlWriter.writeAttribute("xml",  LinkHelper.getLink(request, tip, 'xml'))


            quickElement(xmlWriter, "Identifier", tip.identifier)
            quickElement(xmlWriter, "SubIdentifier", tip.subIdentifier)
            quickElement(xmlWriter, "BaseUrl", tip.baseIdentifier)
            quickElement(xmlWriter, "Name", tip.name)
            quickElement(xmlWriter, "Version", tip.tipVersion)
            quickElement(xmlWriter, "Description", tip.description)
            quickElement(xmlWriter, "PublicationDateTime", formatDateAsString(tip.publicationDateTime))

            xmlWriter.writeEndElement()

        }//end each TD

        xmlWriter.writeEndElement(); // TrustInteroperabilityProfiles
        xmlWriter.writeEndDocument()
        xmlWriter.close()

        xmlStringOut.flush()
        return xmlStringOut.toString()
    }

    public static void quickElement(XMLStreamWriter writer, String element, Object value){
        writer.writeStartElement(element)
        writer.writeCharacters(value?.toString())
        writer.writeEndElement()
    }


    private Map buildTipJson(edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip ){
        if( tip == null )
            return [:]
        Map json = [
                uniqueId: tip.getIdentifier().toString().hashCode(),
                Name: tip.getName(),
                Identifier: tip.getIdentifier().toString(),
                Version: tip.getVersion(),
                Description: tip.getDescription(),
                Deprecated: tip.isDeprecated() ?: false,
                PublicationDateTime: formatDateAsString(tip.getPublicationDateTime()),
                _links : [
                        "self" : [href: tip.getIdentifier().toString()+"?format=json"],
                        _formats :[
                                [format: "json", href: tip.getIdentifier().toString()+"?format=json"],
                                [format: "xml", href: tip.getIdentifier().toString()+"?format=xml"],
                                [format: "html", href: tip.getIdentifier().toString()+"?format=html"]
                        ]
                ]
        ]

        if( tip.getSatisfies() != null && tip.getSatisfies().size() > 0 ){
            json.put("Satisfies", [])
            for(TrustmarkFrameworkIdentifiedObject tmfi : tip.getSatisfies() ){
                json.Satisfies.add([Identifier: tmfi.getIdentifier().toString()])
            }
        }
        if( tip.getSupersededBy() != null && tip.getSupersededBy().size() > 0 ){
            json.put("SupersededBy", [])
            for(TrustmarkFrameworkIdentifiedObject tmfi : tip.getSupersededBy() ){
                json.SupersededBy.add([Identifier: tmfi.getIdentifier().toString()])
            }
        }
        if( tip.getSupersedes() != null && tip.getSupersedes().size() > 0 ){
            json.put("Supersedes", [])
            for(TrustmarkFrameworkIdentifiedObject tmfi : tip.getSupersedes() ){
                json.Supersedes.add([Identifier: tmfi.getIdentifier().toString()])
            }
        }

        json.put("Keywords", [])
        for( String keyword : tip.getKeywords() ){
            json.Keywords.add(keyword)
        }

        return json
    }

    private Map buildTipJson(TrustInteroperabilityProfile tip ){
        Map json = [
                uniqueId: tip.id,
                Name: tip.name,
                Identifier: tip.identifier,
                subIdentifier: tip.subIdentifier,
                baseUrl: tip.getBaseIdentifier(),
                Version: tip.tipVersion,
                Description: tip.description,
                Deprecated: tip.deprecated ?: false,
                PublicationDateTime: formatDateAsString(tip.publicationDateTime),
                _links : [
                    "self" : [href: LinkHelper.getLink(request, tip, 'json')],
                    _formats :[
                            [format: "json", href: LinkHelper.getLink(request, tip, 'json')],
                            [format: "xml", href: LinkHelper.getLink(request, tip, 'xml')],
                            [format: "html", href: LinkHelper.getLink(request, tip, 'html')]
                    ]
                ]
        ]

        addTmfiUriList(json, tip.getSatisfies(), "Satisfies")
        addTmfiUriList(json, tip.getSupersededBy(), "SupersededBy")
        addTmfiUriList(json, tip.getSupersedes(), "Supersedes")

        List<KeywordTIPLink> keywordLinks = KeywordTIPLink.findAllByTip(tip)
        if( keywordLinks != null ){
            def keywordList = []
            for( KeywordTIPLink link : keywordLinks ){
                keywordList.add(link.keyword.name)
            }
            json.put("Keywords", keywordList)
        }


        return json
    }

    public static String formatDateAsString(Date date){
        GregorianCalendar calendar = new GregorianCalendar()
        calendar.setTime(date)
        DatatypeFactory df = DatatypeFactory.newInstance()
        XMLGregorianCalendar dateTime = df.newXMLGregorianCalendar(calendar)
        return dateTime.toString()
    }

    private Map buildPrevListLink(Object params, Integer totalCount){
        Integer offset = params.offset ? Integer.parseInt(params.offset) : 0
        Integer max = params.max ? Integer.parseInt(params.max) : DEFUALT_MAX

        if( offset > 0 ){
            if( offset - max >= 0 ){
                return [href: createLink(controller: 'tip', action: 'list', params: buildNewParams(params, offset - max), absolute: true )]
            }else{
                return [href: createLink(controller: 'tip', action: 'list', params: buildNewParams(params, 0), absolute: true )]
            }
        }else{
            return null
        }
    }
    private Map buildNextListLink(Object params, Integer totalCount){
        Integer offset = params.offset ? Integer.parseInt(params.offset) : 0
        Integer max = params.max ? Integer.parseInt(params.max) : DEFUALT_MAX

        if( max + offset <= totalCount ){ // TODO Is this an equals?
            return [href: createLink(controller: 'tip', action: 'list', params: buildNewParams(params, offset + max), absolute: true )]
        }else{
            return null
        }
    }

    private Map buildNewParams(Object params, Integer newOffset) {
        Map newParams = [:]
        for( Object param : params.keySet() ){
            Object paramVal = params.get(param)
            newParams.put(param, paramVal)
        }
        newParams.put("offset", newOffset.toString())
        return newParams
    }


    private String addJsonXmlLinks(TrustInteroperabilityProfile tip, String content){
        StringWriter newContent = new StringWriter()

        String menuReplaceText =  "<!-- TODO Navbar insert -->"

        for( String line : content.split(Pattern.quote("\n")) ){
            if( line.contains(menuReplaceText) ){
                String xmlLink = LinkHelper.getLink(request, tip, 'xml')
                String jsonLink = LinkHelper.getLink(request, tip, 'json')
                String xmlJsonContent = """
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
      <ul class="nav navbar-nav">
        <li><a href="${createLink(uri: '/')}">Home</a></li>
        <li><a href="javascript:history.back();">&laquo; Back</a></li>
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Formats <span class="caret"></span></a>
          <ul class="dropdown-menu">
            <li><a href="${xmlLink}">XML</a></li>
            <li><a href="${jsonLink}">JSON</a></li>
          </ul>
        </li>
      </ul>
    </div>
"""
                line = line.replace(menuReplaceText, xmlJsonContent)
            }
            newContent.append(line).append("\n")
        }
        return newContent.toString()
    }//end addJsonXmlLinks()


}//end tmf.host.TrustmarkDefinitionController()
