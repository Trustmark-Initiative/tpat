package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.SerializerFactory
import edu.gatech.gtri.trustmark.v1_0.io.TrustmarkDefinitionResolver
import edu.gatech.gtri.trustmark.v1_0.model.Source
import edu.gatech.gtri.trustmark.v1_0.model.Term
import groovy.json.JsonOutput
import org.apache.commons.lang.StringUtils
import org.hibernate.SessionFactory
import tmf.host.util.LinkHelper

import javax.servlet.ServletException
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 * Responsible for displaying all tmf.host.TrustmarkDefinition pages.
 */
class TrustmarkDefinitionController extends AbstractTFObjectAwareController {

    public static final Integer DEFAULT_MAX = 25
    static final String href = "<a href=";

    SessionFactory sessionFactory

    /**
     * Returns the count of all TrustmarkDefinitions.
     */
    def count() {
        log.info("Returning the count of all trustmark definitions...")

        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if( vs ){
            int count = VersionSetTDLink.countByVersionSet(vs)
            if( response.format == "html" || response.format == "all" ){ // Note all is the special case format!
                return render(contentType: 'text/plain', text: "There are "+count+" trustmark definitions.")
            }else if( response.format == "xml" ){
                return render(contentType: 'text/xml', text: "<TrustmarkDefinitions total=\"${count}\" />")
            }else if( response.format == "json" ){
                return render(contentType: 'application/json', text: "{\"totalCount\": ${count}}")
            }else{
                throw new ServletException("ContentType ${response.format} is not supported.")
            }
        }else{
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }
    }

    /**
     * Lists all trustmark definitions in the database in all formats (ie, HTML, XML and JSON).
     */
    def list() {
        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if( !vs ){
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }
        log.debug("Showing the Trustmark Definition list...")
        if( params.max == null )
            params.max = DEFAULT_MAX.toString()
        if( params.offset == null )
            params.offset = "0"
        if( params.sort == null )
            params.sort = "trustmarkDefinition.name"
        if( params.order == null )
            params.order = "asc"

        params.max = Math.min(100, Integer.parseInt(params.max)).toString() // we will display at most 100.

        List<TrustmarkDefinition> tds = []
        Integer totalCount = -1
        log.debug("Selecting all TDs (including deprecated) for VersionSet[${vs.name}] params=${params}")

        tds = VersionSetTDLink.executeQuery(
                        "select link.trustmarkDefinition from VersionSetTDLink link where link.versionSet = :vs",
                        [vs: vs], params);

        totalCount = VersionSetTDLink.countByVersionSet(vs)

        if( response.format == "html" || response.format == "all" ){ // Note all is the special case format!
            log.debug("Displaying format HTML...")
            [trustmarkDefinitions : tds, trustmarkDefinitionsCount: totalCount]
        }else if( response.format == "xml" ){
            log.debug("Displaying format XML...")
            return render(contentType: 'text/xml', text: buildTdXmlList(tds, totalCount, params))
        }else if( response.format == "json" ){
            log.debug("Displaying format JSON...")
            Map responseJson = [
                    trustmarkDefinitionsCount: tds.size(),
                    totalCount: totalCount,
                    offset: Integer.parseInt(params.offset),
                    max: Integer.parseInt(params.max),
                    _links : [
                            "self" : [ href: createLink(controller: 'trustmarkDefinition', action: 'list', params: params, absolute: true)],
                            "next" : buildNextListLink(params, totalCount),
                            "prev" : buildPrevListLink(params, totalCount)
                    ]
            ]

            List tdJson = []
            for( TrustmarkDefinition td : tds ){
                tdJson.add(buildTdJson(td))
            }
            responseJson.put("trustmarkDefinitions", tdJson)


            return render(contentType: 'application/json', text: JsonOutput.prettyPrint(JsonOutput.toJson(responseJson)))
        }else{
            throw new ServletException("ContentType ${response.format} is not supported.")
        }

    }

    /**
     * Responsible for displaying a trustmark definition which has the given Database ID.
     */
    def view(){
        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if( !vs ){
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }

        log.debug("Displaying TD[@|cyan ${params.id}|@] on VersionSet[@|green ${vs.name}|@]...")

        if( StringUtils.isBlank(params.id) && StringUtils.isBlank(params.tdName) ){
            log.warn("Given blank ID, cannot display!")
            throw new ServletException("Invalid blank id, cannot display trustmark.")
        }

        TrustmarkDefinition td = null

        if( StringUtils.isNotBlank(params.id) ) {
            VersionSetTDLink link = VersionSetTDLink.findByVersionSetAndTdIdentifier(vs, params.id)
            if (link == null) {
                log.warn("Invalid trustmark definition identifier: ${params.id}")
                throw new ServletException("No such trustmark definition identifier [${params.id}] found.")
            }
            td = link.trustmarkDefinition
        }else{
            log.debug("Searching for TD[${params.tdName}, v ${params.tdVersion}] in version set[${vs.name}]")
            String id = "%/" + params.tdName + "/" + params.tdVersion + "%"
            List<VersionSetTDLink> links = VersionSetTDLink.executeQuery("from VersionSetTDLink where versionSet = :vs and tdIdentifier like :id", [vs: vs, id: id])
            if( links.size() > 1 ){
                log.warn("Ambiguous identification ${params.tdName}, ${params.tdVersion} given.")
                throw new ServletException("Ambiguous identification ${params.tdName}, ${params.tdVersion} given.")
            }else if( links.size() == 0 ){
                log.warn("TD identification ${params.tdName}, ${params.tdVersion} not found.")
                throw new ServletException("TD identification ${params.tdName}, ${params.tdVersion} not found.")
            }
            td = links.get(0).trustmarkDefinition
        }

        log.info("Successfully Found TD[@|green ${params.id ?: params.tdName}|@]: @|cyan ${td.name}|@, v. @|magenta ${td.version}|@ | Displaying to requester...")
        BinaryObject artifact = td.getArtifact()
        File artifactFile = artifact.content.toFile()

        TrustmarkDefinitionResolver tdResolver = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class)
        edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td2 = tdResolver.resolve(new FileReader(artifactFile))

        List<ArtifactReference> references = ArtifactReference.findAllByVersionSetAndDestinationTd(vs, td)

        SerializerFactory serializerFactory = FactoryLoader.getInstance(SerializerFactory.class)
        StringWriter contentWriter = new StringWriter()
        String contentType = null

        log.info "Returning format: [request=${request.format} | response=${response.format}]..."
        if( response.format == "html" || response.format == "all" ){ // Note all is the special case format!
            log.debug("Rendering HTML view...")

            linkifySourcesAndTerms(td2)

            return [databaseTd: td, td: td2, references: references]
        }else if( response.format == "json" ){
            contentType = "application/json"
            serializerFactory.getJsonSerializer().serialize(td2, contentWriter)
        }else if( response.format == "xml" ){
            contentType = "text/xml"
            serializerFactory.getXmlSerializer().serialize(td2, contentWriter)
        }else{
            throw new ServletException("ContentType ${response.format} is not supported.")
        }

        String content = contentWriter.toString()
        if( contentType == "text/html" ) {
            content = addJsonXmlLinks(td, content)
        }

        return render(contentType: contentType, text: content)
    }//end view()

    /**
     * Responsible for displaying lists of tmf.host.TrustmarkDefinitions by regular expression match.
     */
    def listByName() {
        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE))
        if (!vs) {
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }

        log.debug("Displaying TrustmarkDefinition[name ~= @|green ${params.nameRegex}|@, version ~= @|green ${params.version}|@] on VersionSet[@cyan ${vs.name}|@]...")
        if (StringUtils.isBlank(params.nameRegex) ) {

            if( response.format == "html" || response.format == "all" ) { // Note all is the special case format!
                return [versionSet: vs, trustmarkDefinitions: [], trustmarkDefinitionsCount: 0]
            }else{
                throw new ServletException("Missing required parameter 'nameRegex'")
            }
        }
        if( StringUtils.isBlank(params.versionRegex) ){
            log.warn("No 'versionRegex' parameter was given, assuming you didn't want to search that field...")
        }

        def max = Math.min(100, params.int("max") ?: 10)
        def offset = params.int("offset") ?: 0

        List<VersionSetTDLink> tdLinks = []
        Integer totalCount = -1
        if( StringUtils.isNotBlank(params.versionRegex) ){
            log.info("Searching TDs for name[@|green ${params.nameRegex}|@] and version[@|cyan ${params.versionRegex}|@]...")
            tdLinks = VersionSetTDLink.executeQuery(
                    "from VersionSetTDLink link where link.versionSet = :vs and "+
                            "regexp(link.trustmarkDefinition.name, :nameRegex) = 1 and "+
                            "regexp(link.trustmarkDefinition.tdVersion, :versionRegex) = 1 "+
                            "order by link.trustmarkDefinition.name asc",
                    [vs: vs, nameRegex: params.nameRegex, versionRegex: params.versionRegex],
                    [max: max, offset: offset]
            );
            def countResult = VersionSetTDLink.executeQuery(
                    "select count(*) from VersionSetTDLink link where link.versionSet = :vs and "+
                            "regexp(link.trustmarkDefinition.name, :nameRegex) = 1 and "+
                            "regexp(link.trustmarkDefinition.tdVersion, :versionRegex) = 1",
                    [vs: vs, nameRegex: params.nameRegex, versionRegex: params.versionRegex]
            );
            totalCount = countResult?.get(0)
        }else{
            log.info("Searching TDs for name[@|green ${params.nameRegex}|@]...")
            tdLinks = VersionSetTDLink.executeQuery(
                    "from VersionSetTDLink link where link.versionSet = :vs and "+
                            "regexp(link.trustmarkDefinition.name, :nameRegex) = 1 "+
                            "order by link.trustmarkDefinition.name asc",
                    [vs: vs, nameRegex: params.nameRegex],
                    [max: max, offset: offset]
            );
            def countResult = VersionSetTDLink.executeQuery(
                    "select count(*) from VersionSetTDLink link where link.versionSet = :vs and "+
                            "regexp(link.trustmarkDefinition.name, :nameRegex) = 1",
                    [vs: vs, nameRegex: params.nameRegex]
            );
            totalCount = countResult?.get(0)
        }

        log.info("Successfully resolved @|cyan ${tdLinks.size()}|@ TDs of @|green ${totalCount}|@")
        List<TrustmarkDefinition> tds = []
        for( VersionSetTDLink link : tdLinks ){
            tds.add(link.trustmarkDefinition)
        }

        if( response.format == "html" || response.format == "all" ){ // Note all is the special case format!
            log.debug("Displaying format HTML...")
            [trustmarkDefinitions : tds, trustmarkDefinitionsCount: totalCount]
        }else if( response.format == "xml" ){
            log.debug("Displaying format XML...")
            return render(contentType: 'text/xml', text: buildTdXmlList(tds, totalCount, params))
        }else if( response.format == "json" ){
            log.debug("Displaying format JSON...")
            Map responseJson = [
                    trustmarkDefinitionsCount: tds.size(),
                    totalCount: totalCount,
                    offset: Integer.parseInt(params.offset ?: '0'),
                    max: Integer.parseInt(params.max ?: '10'),
                    nameRegex: params.nameRegex ?: '.*',
                    versionRegex: params.versionRegex ?: '.*',
                    _links : [
                            "self" : [ href: createLink(controller: 'trustmarkDefinition', action: 'list', params: params, absolute: true)],
                            "next" : buildNextListLink(params, totalCount),
                            "prev" : buildPrevListLink(params, totalCount)
                    ]
            ]

            List tdJson = []
            for( TrustmarkDefinition td : tds ){
                tdJson.add(buildTdJson(td))
            }
            responseJson.put("trustmarkDefinitions", tdJson)


            return render(contentType: 'application/json', text: JsonOutput.prettyPrint(JsonOutput.toJson(responseJson)))
        }else{
            throw new ServletException("ContentType ${response.format} is not supported.")
        }

    }


    /**
     * This method will allow you to download ALL Trustmark Definitions at once, in a ZIP file.  The goal is that
     * import code which needs to go ahead and download ALL TDs can just do so in one fell swoop and save a good bit of time.
     * This method has no true HTML format, only a ZIP containing HTML can be downloaded.
     */
    def allPackage() {

        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE));
        if( !vs ){
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")
        }


        log.info("Downloading ALL Trustmark Definitions...")
        File tempDir = File.createTempFile("all-tds.", ".dir")
        tempDir.delete(); tempDir.mkdirs()
        File tdsDir = new File(tempDir, "tds-"+nowAsDateString())
        tdsDir.mkdirs()

        String format = params.tdFormat ?: null
        if( StringUtils.isBlank(format) ){
            log.debug("Defaulting to JSON output for 'tdFormat'...")
            format = "json"
        }
        log.debug("Rendering each TrustmarkDefinition as ${format}...")
        for( TrustmarkDefinition td : TrustmarkDefinition.findAll() ) {
            File tdFile = new File(tdsDir, td.id+"."+format)

            TrustmarkDefinitionResolver tdResolver = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class)
            edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td2 = tdResolver.resolve(new StringReader(td.artifact.content))

            SerializerFactory serializerFactory = FactoryLoader.getInstance(SerializerFactory.class)
            FileWriter fileWriter = new FileWriter(tdFile)

            if( format.equalsIgnoreCase("json") ){
                serializerFactory.getJsonSerializer().serialize(td2, fileWriter)
            }else if( format.equalsIgnoreCase("xml") ){
                serializerFactory.getXmlSerializer().serialize(td2, fileWriter)
            }else if( format.equalsIgnoreCase("html") ){
                serializerFactory.getHtmlSerializer().serialize(td2 , fileWriter)
            }else{
                throw new ServletException("Unsupported TD Format: " + format)
            }

            fileWriter.flush()
            fileWriter.close()
        }

        log.debug("Creating ZIP archive...")
        File zipFile = File.createTempFile("tds-", ".zip")
        zipFile.delete()
        def ant = new AntBuilder()
        ant.zip( destFile: zipFile.canonicalPath, basedir: tempDir.canonicalPath )

        log.debug("Successfully created Zip file: ${zipFile.canonicalPath}")

        return render(file: zipFile, contentType: 'application/zip', fileName: tdsDir.name)
    }

    /**
     * A dumb method for forwarding to the list page.
     */
    def index() {
        redirect(action: 'list')
    }

    static void quickElement(XMLStreamWriter writer, String element, Object value){
        writer.writeStartElement(element)
        writer.writeCharacters(value?.toString())
        writer.writeEndElement()
    }

    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================
    private String dateAsString(Date date, String format){
        return new SimpleDateFormat(format).format(date)
    }
    private String nowAsDateString() {
        return dateAsString(Calendar.getInstance().getTime(), "yyyy-MM-dd")
    }

    private String buildTdXmlList(List<TrustmarkDefinition> tds, int totalCount, Object params){
        StringWriter xmlStringOut = new StringWriter()
        XMLOutputFactory xof = XMLOutputFactory.newInstance()
        XMLStreamWriter xmlWriter = xof.createXMLStreamWriter(xmlStringOut)

        xmlWriter.writeStartDocument("UTF-8", "1.0")
        xmlWriter.writeStartElement("TrustmarkDefinitions")

        xmlWriter.writeAttribute("count", tds.size()+"")
        xmlWriter.writeAttribute("total", totalCount + "")
        xmlWriter.writeAttribute("max", params.max + "")
        xmlWriter.writeAttribute("offset", params.offset + "")

        xmlWriter.writeAttribute("self", createLink(controller: 'trustmarkDefinition', action: 'list', params: params, absolute: true))

        Map nextLink = buildNextListLink(params, totalCount)
        if( nextLink != null )
            xmlWriter.writeAttribute("next", nextLink.href)
        Map prevLink = buildPrevListLink(params, totalCount)
        if( prevLink != null )
            xmlWriter.writeAttribute("prev", prevLink.href)

        for( TrustmarkDefinition td : tds ){

            xmlWriter.writeStartElement("TrustmarkDefinition")
            xmlWriter.writeAttribute("html", LinkHelper.getLink(request, td, 'html'))
            xmlWriter.writeAttribute("json", LinkHelper.getLink(request, td, 'json'))
            xmlWriter.writeAttribute("xml", LinkHelper.getLink(request, td, 'xml'))


            quickElement(xmlWriter, "Identifier", td.identifier)
            quickElement(xmlWriter, "SubIdentifier", td.subIdentifier)
            quickElement(xmlWriter, "BaseUrl", td.baseIdentifier)
            quickElement(xmlWriter, "Name", td.name)
            quickElement(xmlWriter, "Version", td.tdVersion)
            quickElement(xmlWriter, "Description", td.description)
            quickElement(xmlWriter, "Deprecated", td.deprecated)
            quickElement(xmlWriter, "PublicationDateTime", formatDateAsString(td.publicationDateTime))

            addTmiElementsList(xmlWriter, td.getSatisfies(), "Satisfies")
            addTmiElementsList(xmlWriter, td.getSupersededBy(), "SupersededBy")
            addTmiElementsList(xmlWriter, td.getSupersedes(), "Supersedes")

            List<KeywordTDLink> keywords = KeywordTDLink.findAllByTd(td)
            xmlWriter.writeStartElement("Keywords")
            if( keywords != null && keywords.size() > 0 ) {
                for (KeywordTDLink keywordLink : keywords) {
                    xmlWriter.writeStartElement("Keyword")
                    xmlWriter.writeCharacters(keywordLink.keyword.name)
                    xmlWriter.writeEndElement()
                }
            }
            xmlWriter.writeEndElement() // End Keywords

            xmlWriter.writeEndElement() // End TrustmarkDefinition

        }//end each TD

        xmlWriter.writeEndElement() // TrustmarkDefinitions
        xmlWriter.writeEndDocument()
        xmlWriter.close()

        xmlStringOut.flush()
        return xmlStringOut.toString()
    }

    private static void addTmiElementsList(XMLStreamWriter writer, String uriList, String containingElement){
        if( uriList != null && uriList.trim().length() > 0 ){
            for( String uri : uriList.split("\n") ){
                if( uri != null && uri.trim().length() > 0 ){
                    writer.writeStartElement(containingElement)
                    quickElement(writer, "Identifier", uri.trim())
                    writer.writeEndElement()
                }
            }
        }
    }

    private Map buildPrevListLink(Object params, Integer totalCount){
        Integer offset = params.offset ? Integer.parseInt(params.offset) : 0
        Integer max = params.max ? Integer.parseInt(params.max) : DEFAULT_MAX

        if( offset > 0 ){
            if( offset - max >= 0 ){
                return [href: createLink(controller: 'trustmarkDefinition', action: 'list', params: buildNewParams(params, offset - max), absolute: true )]
            }else{
                return [href: createLink(controller: 'trustmarkDefinition', action: 'list', params: buildNewParams(params, 0), absolute: true )]
            }
        }else{
            return null
        }
    }
    private Map buildNextListLink(Object params, Integer totalCount){
        Integer offset = params.offset ? Integer.parseInt(params.offset) : 0
        Integer max = params.max ? Integer.parseInt(params.max) : DEFAULT_MAX

        if( max + offset <= totalCount ){ // TODO Is this an equals?
            return [href: createLink(controller: 'trustmarkDefinition', action: 'list', params: buildNewParams(params, offset + max), absolute: true )]
        }else{
            return null
        }
    }

    private static Map buildNewParams(Object params, Integer newOffset) {
        Map newParams = [:]
        for( Object param : params.keySet() ){
            Object paramVal = params.get(param)
            newParams.put(param, paramVal)
        }
        newParams.put("offset", newOffset.toString())
        return newParams
    }

    private void linkifySourcesAndTerms(edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td)   {
        Collection<Source> sources = td.sources.collect { mapSources(it) }
        td.sources.clear()
        td.sources.addAll(sources)

        Collection<Term> terms = td.terms.collect {mapTerms(it)}
        td.terms.clear()
        td.terms.addAll(terms)
    }


    /**
     * check Source references and make any url test into urls
     * @param content
     * @return
     */
    private Source mapSources(Source content)  {
        content.reference = mapUrl(content.reference)
        return content
    }

    /**
     * check Term definitions and make any url text into urls
     * @param content
     * @return
     */
    private Term mapTerms(Term content)  {
        content.definition = mapUrl(content.definition)
        return content
    }

    /**
     * find a url within the text and wrap it with an <a></a>, linkify
     * @param content
     * @return
     */
    private String mapUrl(String content)  {
        String s = content
        def regx = s =~ /\b(https|http|ftp|file):\/\/[\/\-\w_+\.]+\b/
        if(regx.find())  {
            if(!s.contains("<a href=\""+regx[0][0]))  {
                log.debug("REGEX  ${regx.size()} - ${regx[0]}")
                String replacement = stripTrailing(regx[0][0], '.' as char)  // hack to remove trailing periods
                content = s.replaceAll(replacement, "<a href=${replacement}>${replacement}</a>" )
                log.debug("REPLACED ${content}")
            }
        }
        return content
    }

    /**
     * return the string minus the specified trailing character
     * @param s
     * @param c
     * @return
     */
    private static String stripTrailing(String s, char c)  {
        int x = s.length()
        while (x > 0)  {
            if(s.charAt(x-1) != c)  {
                break
            }
           --x
        }
        return s.substring(0, x)
    }

    /**
     *
     * @param td
     * @param content
     * @return
     */
    private String addJsonXmlLinks(TrustmarkDefinition td, String content){
        StringWriter newContent = new StringWriter()

        String menuReplaceText =  "<!-- TODO Navbar insert -->"

        for( String line : content.split(Pattern.quote("\n")) ){
            if( line.contains(menuReplaceText) ){
                String xmlLink = LinkHelper.getLink(request, td, 'xml')
                String jsonLink = LinkHelper.getLink(request, td, 'json')

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