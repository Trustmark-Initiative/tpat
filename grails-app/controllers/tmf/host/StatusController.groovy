package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.TrustmarkFramework
import edu.gatech.gtri.trustmark.v1_0.model.Contact
import edu.gatech.gtri.trustmark.v1_0.model.Entity
import grails.converters.XML
import groovy.json.JsonOutput
import org.apache.commons.lang.StringUtils
import tmf.host.util.TFAMPropertiesHolder
import tmf.host.util.TfamOwnerOrganization

import javax.servlet.ServletException

/**
 * Performs operations related to the status of this TF server.
 */
public class StatusController extends AbstractTFObjectAwareController {

    UserService userService;

    /**
     * Provides an overall view of the server's status.
     */
    def index() {
        log.info("Returning server status...");
        String timestampNow = formatDateAsString(Calendar.getInstance().getTime());

        List<String> baseUrls = TFAMPropertiesHolder.getBaseURLsAsStrings()
        def response = [
                timestamp: timestampNow,
                "\$TMF_VERSION": FactoryLoader.getInstance(TrustmarkFramework.class).getTrustmarkFrameworkVersion(),
                _links :[
                        _self: [href: createLink(controller:'status', action: 'index', params: params, absolute: true)]
                ],
                organization: getConfiguredOrganizationJsonMap(),
                baseUrls: baseUrls
        ];

        if( baseUrls.size() == 1) {
            response.put("baseUrl", baseUrls.get(0));
        }


        long countValidTDs = 0;
        long countAllTDs = 0;
        String mostRecentTdDate = null;
        long countValidTIPs = 0;
        long countAllTIPs = 0;
        String mostRecentTipDate = null;

        VersionSet vs = VersionSet.findByName(session.getAttribute(VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE));
        if( !vs ){
            log.error("There is no version set in session scope.");
            response.status = "ERROR";
            response.message = "There is no version set in memory.  This means the server does not have any TDs or TIPs to offer yet.";
            response.versionSet = null;
        }else{
            response.versionSet = vs.name;

            countValidTDs = (long) (VersionSetTDLink.executeQuery("select count(*) from VersionSetTDLink where versionSet = :vs and trustmarkDefinition.deprecated = false", [vs: vs]))?.get(0);
            countAllTDs = VersionSetTDLink.countByVersionSet(vs);
            mostRecentTdDate = "";
            if( countAllTDs > 0 ) {
                mostRecentTdDate = formatDateAsString(
                        VersionSetTDLink.findAll("from VersionSetTDLink link where versionSet = :vs order by link.trustmarkDefinition.publicationDateTime desc", [vs: vs], [max: 1]).get(0)
                                ?.trustmarkDefinition?.publicationDateTime);
            }else{
                mostRecentTdDate = null;
            }


            countValidTIPs = (long) (VersionSetTIPLink.executeQuery("select count(*) from VersionSetTIPLink where versionSet = :vs and trustInteroperabilityProfile.deprecated = false", [vs: vs]))?.get(0);
            countAllTIPs = VersionSetTIPLink.countByVersionSet(vs);
            mostRecentTipDate = "";
            if( countAllTIPs > 0 ) {
                mostRecentTipDate = formatDateAsString(
                        VersionSetTIPLink.findAll("from VersionSetTIPLink link where versionSet = :vs order by link.trustInteroperabilityProfile.publicationDateTime desc", [vs: vs], [max: 1]).get(0)
                                ?.trustInteroperabilityProfile?.publicationDateTime);
            }else{
                mostRecentTipDate = null;
            }

        }

        def versionSets = [];
        if( userService.isLoggedIn() ){
            log.debug("Listing available version sets...")
            for( VersionSet curVs : VersionSet.findAll() ){
                Map json = curVs.toJson(false);
                json.put("downloadAll", [
                        "_links" : buildLinks('downloadAll', 'build', curVs.getName(), ['json'])
                ])
                versionSets.add(json);
            }
        }else if( vs ){
            Map json = vs.toJson(false);
            json.put("downloadAll", [
                    "_links" : buildLinks('downloadAll', 'build', vs.getName(), ['json'])
            ])
            versionSets.add(json);
        }

        def dataMap = [
                // TODO Do we want to include other server facts here?
                VersionSets: versionSets,
                TrustmarkDefinitions: [
                        supported: true,
                        countAll: countAllTDs,
                        countNotDeprecated: countValidTDs,
                        mostRecentDate: mostRecentTdDate,
                        _links : buildLinks('trustmarkDefinition', 'list', ['html', 'json', 'xml'], [showDeprecated: 'true', max: '100']),
                        NotDeprecated: [
                                _links : buildLinks('trustmarkDefinition', 'list', ['html', 'json', 'xml'], [showDeprecated: 'false', max: '100'])
                        ],
                        ByName: [
                                _links : buildLinks('trustmarkDefinition', 'listByName', ['html', 'json', 'xml'], [nameRegex: '__NAME_REGEX__', versionRegex: '__VERS_REGEX__'])
                        ]
                ],
                TrustInteroperabilityProfiles : [
                        supported: true,
                        countAll: countAllTIPs,
                        countNotDeprecated: countValidTIPs,
                        mostRecentDate: mostRecentTipDate,
                        _links : buildLinks('tip', 'list', ['html', 'json', 'xml'], [showDeprecated: 'true', max: '100']),
                        NotDeprecated: [
                                _links : buildLinks('tip', 'list', ['html', 'json', 'xml'], [showDeprecated: 'false', max: '100'])
                        ],
                        ByName: [
                                _links : buildLinks('tip', 'listByName', ['html', 'json', 'xml'], [nameRegex: '__NAME_REGEX__', versionRegex: '__VERS_REGEX__'])
                        ]
                ],
                Trustmarks: [
                        supported: false
                ],
                Keywords: [
                    countAll: Keyword.count(),
                    _links: buildLinks('keyword', 'list', ['json', 'html'])
                ]
        ]
        response.putAll(dataMap);

        withFormat {
            json {
                log.debug("Displaying format JSON...");
                return render(contentType: 'application/json', text: JsonOutput.prettyPrint(JsonOutput.toJson(response)));
            }
            all {
                log.debug("Displaying format JSON...");
                return render(contentType: 'application/json', text: JsonOutput.prettyPrint(JsonOutput.toJson(response)));
            }
            xml {
                render response as XML
            }
        }

    }//end status() method



    private Map getConfiguredOrganizationJsonMap() {
        TfamOwnerOrganization ownerOrganization = TFAMPropertiesHolder.getDefaultEntity();
        return [
                Name: ownerOrganization.name,
                Identifier: ownerOrganization.identifier.toString(),
//                Abbreviation: ownerOrganization.getAbbreviation(),
//                LogoPath: g.createLink(uri: ownerOrganization.getLogoImagePath(), absolute: true),
                PrimaryContact: getContactJsonMap(ownerOrganization.getDefaultContact())
        ]
    }

    private Map getContactJsonMap(Contact c){
        Map data = [:]

        data.put("Kind", c.getKind().toString());

        if( StringUtils.isNotBlank(c.getResponder()) )
            data.put("Responder", c.getResponder());

        if( StringUtils.isNotBlank(c.getDefaultEmail()) )
            data.put("Email", c.getDefaultEmail());

        if( StringUtils.isNotBlank(c.getDefaultTelephone()) )
            data.put("Telephone", c.getDefaultTelephone());

        if( StringUtils.isNotBlank(c.getDefaultPhysicalAddress()) )
            data.put("PhysicalAddress", c.getDefaultPhysicalAddress());

        if( StringUtils.isNotBlank(c.getDefaultMailingAddress()) )
            data.put("MailingAddress", c.getDefaultMailingAddress());

        if( StringUtils.isNotBlank(c.getNotes()) )
            data.put("Notes", c.getNotes());

        return data;
    }

}//end tmf.host.StatusController