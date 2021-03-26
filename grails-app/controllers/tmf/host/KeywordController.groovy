package tmf.host

import groovy.json.JsonOutput
import org.apache.commons.lang.StringUtils

import javax.servlet.ServletException
import java.util.regex.Pattern

/**
 * Responsible for returning JSON of keyword to TD mappings.
 */
class KeywordController extends AbstractTFObjectAwareController {

    //==================================================================================================================
    //  Public Web Methods
    //==================================================================================================================
    def index() {
        redirect(action: 'list');
    }

    def list() {
        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE));
        if( !vs ){
            throw new ServletException("Cannot find any keywords because version set is not available.");
        }
        log.debug("Building keywords index...");

        withFormat {
            json {
                // TODO Externalize the highlight sets somehow.
                Map responseJson = [
                        highlightSets: [
                                [
                                        name: "High-Level Keywords",
                                        keywords: ["Identity Assurance", "Interoperability", "Privacy", "Security", "Usability"]
                                ]
                        ]
                ]

                List<Keyword> keywords = findKeywords(vs);

                List keywordJsonList = []
                for( Keyword keyword : keywords ){
                    def keywordJson = [
                            name: keyword.name,
                            tdCount: KeywordTDLink.countByKeywordAndVersionSet(keyword, vs),
                            tipCount: KeywordTIPLink.countByKeywordAndVersionSet(keyword, vs),
                            ignore: isKeywordIgnored(keyword),
                            "_links" : buildLinks("keyword", "view", keyword.name, ["json", "html"])
                    ]
                    keywordJson.put("count", keywordJson.tdCount + keywordJson.tipCount);
                    keywordJsonList.add(keywordJson)
                }
                responseJson.put("keywords", keywordJsonList);

                return render(contentType: 'application/json', text: JsonOutput.toJson(responseJson));
            }
            '*' {
                log.info("DETECTED Format is HTML!");
            }
        }
    }


    def view() {
        if( StringUtils.isBlank(params.id) ){
            log.warn("Missing required parameter: id (The keyword's name)")
            throw new ServletException("Missing required parameter: id");
        }

        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE));
        if( !vs ){
            throw new ServletException("Cannot find any keywords because version set is not available.");
        }

        Keyword keyword = Keyword.findByName(params.id);
        if( keyword == null )
            keyword = Keyword.get(params.id);
        if( keyword == null ){
            log.warn("No such keyword: ${params.id}")
            return render(text: '<div class="alert alert-danger">No data found</div>');
        }

        log.debug("Loading TDs & TIPs for Keyword[@|green ${keyword.name}|@]...")
        List<KeywordTDLink> tdLinks = KeywordTDLink.findAllByKeywordAndVersionSet(keyword, vs);
        List<KeywordTIPLink> tipLinks = KeywordTIPLink.findAllByKeywordAndVersionSet(keyword, vs);

        if( response.format == "html" || response.format == "all" ) {
            List<TrustmarkDefinition> tds = []
            for( KeywordTDLink link : tdLinks ){
                tds.add(link.td);
            }
            Collections.sort(tds, { TrustmarkDefinition td1, TrustmarkDefinition td2 -> return td1.name.compareToIgnoreCase(td2.name)} as Comparator);
            List<TrustInteroperabilityProfile> tips = []
            for( KeywordTIPLink link : tipLinks ?: [] ){
                tips.add(link.tip);
            }
            Collections.sort(tips, { TrustInteroperabilityProfile tip1, TrustInteroperabilityProfile tip2 -> return tip1.name.compareToIgnoreCase(tip2.name)} as Comparator);
            [keyword: keyword, tds: tds, tdCount: tds.size(), tips: tips, tipCount: tips.size()]
        }else if( response.format == "json" ){

            log.debug("Found ${tdLinks.size()} TDs and ${tipLinks.size()} TIPs...");
            Map keywordJson = [keyword: keyword.name, tdCount: tdLinks.size(), tds: [], tipCount: tipLinks.size(), tips: []]
            for( KeywordTDLink link : tdLinks ){
                Map tdJson = [
                        shortId: parseShortId(link.td.identifier),
                        id: link.td.identifier,
                        name: link.td.name,
                        version: link.td.tdVersion,
                        description: link.td.description,
                        deprecated: link.td.deprecated,
                        relativeXmlPath: createLink(controller: 'trustmarkDefinition', action: 'view', id: link.td.id, params:[format: "xml"]),
                        relativeHtmlPath: createLink(controller: 'trustmarkDefinition', action: 'view', id: link.td.id, params:[format: "html"]),
                        relativeJsonPath: createLink(controller: 'trustmarkDefinition', action: 'view', id: link.td.id, params:[format: "json"])
                ]
                keywordJson.tds.add(tdJson);
            }

            for( KeywordTIPLink link : tipLinks ){
                Map tipJson = [
                        shortId: parseShortId(link.tip.identifier),
                        id: link.tip.identifier,
                        name: link.tip.name,
                        version: link.tip.tipVersion,
                        description: link.tip.description,
                        deprecated: link.tip.deprecated,
                        relativeXmlPath: createLink(controller: 'tip', action: 'view', id: link.tip.id, params:[format: "xml"]),
                        relativeHtmlPath: createLink(controller: 'tip', action: 'view', id: link.tip.id, params:[format: "html"]),
                        relativeJsonPath: createLink(controller: 'tip', action: 'view', id: link.tip.id, params:[format: "json"])
                ]
                keywordJson.tips.add(tipJson);
            }

            return render(contentType: 'application/json', text: JsonOutput.toJson(keywordJson));
        }else{
            throw new ServletException("Content Type[${response.format}] is not supported, only application/json is.")
        }


    }//end list()



    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================

    private List<Keyword> findKeywords(VersionSet vs){
        List<KeywordTDLink> tdLinks = KeywordTDLink.findAllByVersionSet(vs);
        List<KeywordTIPLink> tipLinks = KeywordTIPLink.findAllByVersionSet(vs);

        List<String> keywordsFound = []
        List<Keyword> keywords = []
        for( KeywordTDLink tdLink : tdLinks ?: []){
            if( !keywordsFound.contains(tdLink.keyword.name.toLowerCase()) ){
                keywordsFound.add(tdLink.keyword.name.toLowerCase());
                keywords.add(tdLink.keyword);
            }
        }
        for( KeywordTIPLink tipLink : tipLinks ?: []){
            if( !keywordsFound.contains(tipLink.keyword.name.toLowerCase()) ){
                keywordsFound.add(tipLink.keyword.name.toLowerCase());
                keywords.add(tipLink.keyword);
            }
        }

        Collections.sort(keywords, {Keyword k1, Keyword k2 -> return k1.name.compareToIgnoreCase(k2.name); } as Comparator);

        return keywords;
    }


    private String parseShortId(String id){
        return id;
    }


    private static Boolean KEYWORDS_IGNORE_LOCK = Boolean.TRUE;
    private static List<String> KEYWORDS_TO_IGNORE_CACHE = null;
    private static List<String> getKeywordsToIgnore() {
        synchronized (KEYWORDS_IGNORE_LOCK){
            if( KEYWORDS_TO_IGNORE_CACHE == null  ){
                KEYWORDS_TO_IGNORE_CACHE = []
                String keywordsToIgnoreList = ResourceBundle.getBundle("tpat_config")?.getString("keywords.to.ignore");
                if( keywordsToIgnoreList.length() > 0 ){
                    for( String keywordToIgnore : keywordsToIgnoreList.split(Pattern.quote("|"))) {
                        KEYWORDS_TO_IGNORE_CACHE.add( keywordToIgnore?.trim()?.toLowerCase() );
                    }
                }
            }
            return KEYWORDS_TO_IGNORE_CACHE;
        }
    }

    private Boolean isKeywordIgnored(Keyword keyword){
//        log.debug("tmf.host.Keyword Ignore List: "+getKeywordsToIgnore());
        return getKeywordsToIgnore().contains(keyword.name.toLowerCase());
    }


}//end tmf.host.KeywordController
