package tmf.host

import edu.gatech.gtri.trustmark.v1_0.service.RemoteObject
import edu.gatech.gtri.trustmark.v1_0.service.RemoteSearchResult
import edu.gatech.gtri.trustmark.v1_0.service.RemoteStatus
import edu.gatech.gtri.trustmark.v1_0.service.RemoteTrustInteroperabilityProfile
import edu.gatech.gtri.trustmark.v1_0.service.RemoteTrustmarkDefinition
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang.StringUtils
import tmf.host.util.RemoteSearchRunnable

import javax.servlet.ServletException

/**
 * Created by brad on 2/17/17.
 */
class SearchController {
    final int MAX_SEARCH_RESULTS = 1000

    /**
     * The search index.  Called for both the display of a search form (GET) and an actual search (POST).
     */
    def index() {
        log.debug("Entered search controller...")
        String vsName = session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE);
        log.debug("Looking for version set: "+vsName);
        VersionSet vs = VersionSet.findByName(vsName);
        if( !vs )
            throw new ServletException("Operation is not supported until Version Sets exist in the database.")



        String queryString = (params.q ?: '').trim();
        log.debug("Given query string: @|green $queryString|@")


        if( StringUtils.isBlank(queryString) ){
            // TODO Display an HTML page for searching.
            log.info("Displaying the HTML search page...");
            [versionSet: vs]
        }else{
            log.info("Performing search...");
            log.debug("Query string: "+queryString);

            def page = [max: Math.min(params.max ? params.int('max') : 10, 50), offset: params.offset ? params.int('offset') : 0]

            RemoteSearchRunnable remoteSearchRunnable = new RemoteSearchRunnable(queryString);
            Thread remoteSearchThread = new Thread(remoteSearchRunnable);
            remoteSearchThread.setName("RemoteSearchThread_"+System.currentTimeMillis());
            remoteSearchThread.start();

            log.debug("Separating query terms...");
            List<String> terms = getQueryTerms(queryString);

            if( log.isDebugEnabled() ) {
                log.debug("Terms: ")
                for (String term : terms) {
                    log.debug("    TERM[" + term + "]");
                }
            }

            int tdCount = VersionSetTDLink.countByVersionSet(vs);
            log.debug("Querying @|cyan $tdCount|@ TrustmarkDefinitions...");
            def tdResults = VersionSetTDLink.search().list {
                must {
                    keyword "versionSet.name", vs.name
                }
                should {
                    terms.each {term ->
                        wildcard "trustmarkDefinition.name", term.toLowerCase()+"*"
                        wildcard "trustmarkDefinition.description", term.toLowerCase()+"*"
                    }
                }
            }
            log.debug("Found @|green ${tdResults?.size()}|@ TD Results!");

            int tipCount = VersionSetTIPLink.countByVersionSet(vs);
            log.debug("Querying @|cyan $tipCount|@ Trust Profiles...");
            def tipResults = VersionSetTIPLink.search().list {
                must {
                    keyword "versionSet.name", vs.name
                }
                should {
                    terms.each {term ->
                        wildcard "trustInteroperabilityProfile.name", term.toLowerCase()+"*"
                        wildcard "trustInteroperabilityProfile.description", term.toLowerCase()+"*"
                    }
                }
            }
            log.debug("Found @|green ${tipResults?.size()}|@ TIP Results!");

            log.debug("Waiting for remote search to finish...");
            remoteSearchThread.join();
            log.debug("Local and remote search finished, creating response...");

            Map<RemoteStatus, RemoteSearchResult> remoteSearchResults = remoteSearchRunnable.getSearchResultMap();
            for( RemoteStatus status : remoteSearchResults.keySet() ){
                RemoteSearchResult result = remoteSearchResults.get(status);
                log.info("Registry[${status.getOrganization().getName()}] has ${result.getTrustInteroperabilityProfileMatchCount()} TIP Matches and ${result.getTrustmarkDefinitionMatchCount()} TD Matches");
            }

            withFormat {
                html {
                    render(view: '/search/results', model: [tdCount: tdCount, tdResults: tdResults, tipCount: tipCount, tipResults: tipResults])
                }
                json {
                    Map results = [
                            _links : [
                                    _formats: [
                                            [
                                                    format: "json",
                                                    href: "${createLink(controller: 'search', action: 'index', params: [q: queryString, format: 'json'], absolute: true)}"
                                            ]
                                    ]
                            ],
                            queryString: queryString,
                            terms: terms,
                            tdCountTotal: tdCount,
                            tipCountTotal: tipCount,
                            versionSetId: vs.name,
                            results: [
                                    tdCount: tdResults?.size() ?: 0,
                                    tipCount: tipResults?.size() ?: 0,
                                    tds: formatTdsAsJsonList(tdResults),
                                    tips: formatTipsAsJsonList(tipResults)
                            ],
                            remoteResults : []
                    ]

                    for( RemoteStatus status: remoteSearchResults.keySet() ){
                        RemoteSearchResult result = remoteSearchResults.get(status)
                        results.remoteResults.add(formatRemoteResult(status, result))

                    }

                    render results as JSON
                }
                xml {
                    render(contentType: 'text/xml', text: "<search><status>ERROR</status><message>NOT YET IMPLEMENTED</message></search>")
                }
            }
        }

    }//end index()


    @Secured("ROLE_ORG_ADMIN")
    def rebuildIndex(){
        log.info("Rebuilding indexes...")

        log.debug("Building TD index...")
        VersionSetTDLink.search().createIndexAndWait()

        log.debug("Building TIP index...")
        VersionSetTIPLink.search().createIndexAndWait()

        log.info("Rebuild complete!")
        render(text: 'successfully rebuilt indexes')
    }

    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================

    private Map formatRemoteResult(RemoteStatus status, RemoteSearchResult result){
        Map remoteResult = [
                Organization: [
                        Name: status.getOrganization().getName(),
                        Identifier: status.getOrganization().getIdentifier().toString()
                ],
                tdCount: result.getTrustmarkDefinitionMatchCount(),
                tdTotalCount: result.getTrustmarkDefinitionTotalCount(),
                tds: [],
                tipCount: result.getTrustInteroperabilityProfileMatchCount(),
                tipTotalCount: result.getTrustInteroperabilityProfileTotalCount(),
                tips: []
        ]

        for(RemoteTrustmarkDefinition remoteTd : result.getTrustmarkDefinitions() ){
            remoteResult.tds.add(toJson(remoteTd))
        }

        for(RemoteTrustInteroperabilityProfile remoteTip: result.getTrustInteroperabilityProfiles() ){
            remoteResult.tips.add(toJson(remoteTip))
        }

        return remoteResult
    }

    private Map toJson(RemoteTrustmarkDefinition remoteTd){
        return [
                Type: "TrustmarkDefinition",
                Identifier: remoteTd.getIdentifier().toString(),
                Name: remoteTd.getName(),
                Version: remoteTd.getVersion(),
                Description: remoteTd.getDescription(),
                Deprecated: remoteTd.getDeprecated(),
                Keywords: remoteTd.getKeywords(),
                PublisherIdentifier: remoteTd.getPublisherIdentifier(),
                PublisherName: remoteTd.getPublisherName(),
                _links: formatLinks(remoteTd)
        ]
    }

    private Map toJson(RemoteTrustInteroperabilityProfile remoteTip){
        return [
                Type: "TrustInteroperabilityProfile",
                Identifier: remoteTip.getIdentifier().toString(),
                Name: remoteTip.getName(),
                Version: remoteTip.getVersion(),
                Description: remoteTip.getDescription(),
                Deprecated: remoteTip.getDeprecated(),
                Keywords: remoteTip.getKeywords(),
                PublisherIdentifier: remoteTip.getPublisherIdentifier(),
                PublisherName: remoteTip.getPublisherName(),
                _links: formatLinks(remoteTip)
        ]
    }

    private Map formatLinks(RemoteObject remoteObject ){
        Map links = [formats: []]
        Map<String, URL> formats = remoteObject.getFormats();
        for( String format : formats.keySet() ){
            links.formats.add([format: format, href: formats.get(format).toString()])
        }
        return links
    }


    private List<String> getQueryTerms(String q){
        List<String> terms = []

        // TODO We should make this more sophisticated, but I would prefer using elastic search or figuring out hibernate search for that.
        String[] splitTerms = q.split("\\s+")
        for( String t : splitTerms ){
            terms.add(t)
        }

        return terms
    }

    private Map toTmiJson( TrustInteroperabilityProfile tip ){
        return [
                Type : "TrustInteroperabilityProfile",
                Identifier: tip.identifier,
                Name: tip.name,
                Version: tip.tipVersion,
                Description: tip.description,
                Deprecated: tip.deprecated,
                _links: [
                        _formats: [
                                [
                                        format: 'json',
                                        href: "${tip.identifier.toString()}?format=json"
                                ],
                                [
                                        format: 'html',
                                        href: "${tip.identifier.toString()}?format=html"
                                ],
                                [
                                        format: 'xml',
                                        href: "${tip.identifier.toString()}?format=xml"
                                ]
                        ]
                ]
        ]
    }


    private Map toTmiJson( TrustmarkDefinition td ){
        return [
                Type : "TrustmarkDefinition",
                Identifier: td.identifier,
                Name: td.name,
                Version: td.tdVersion,
                Description: td.description,
                Deprecated: td.deprecated,
                _links: [
                        _formats: [
                                [
                                        format: 'json',
                                        href: "${td.identifier.toString()}?format=json"
                                ],
                                [
                                        format: 'html',
                                        href: "${td.identifier.toString()}?format=html"
                                ],
                                [
                                        format: 'xml',
                                        href: "${td.identifier.toString()}?format=xml"
                                ]
                        ]
                ]
        ]
    }

    private List<Map> formatTdsAsJsonList(List<VersionSetTDLink> tdLinks){
        List results = []
        if( tdLinks && tdLinks.size() > 0 ){
            int count = 0
            for( VersionSetTDLink link : tdLinks ){
                results.add(toTmiJson(link.trustmarkDefinition))
                count++
                if( count > MAX_SEARCH_RESULTS ){
                    break
                }
            }
        }
        return results
    }

    private List<Map> formatTipsAsJsonList(List<VersionSetTIPLink> tipLinks){
        List results = []
        if( tipLinks && tipLinks.size() > 0 ){
            int count = 0
            for( VersionSetTIPLink link : tipLinks ){
                Map json = toTmiJson(link.trustInteroperabilityProfile)
                json.put("primary", link.primaryTIP)
                results.add(json)
                count++
                if( count > MAX_SEARCH_RESULTS ){
                    break
                }
            }
        }
        return results
    }

}//end SearchController