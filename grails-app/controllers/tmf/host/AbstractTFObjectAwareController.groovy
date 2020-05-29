package tmf.host

import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkFrameworkIdentifiedObject
import tmf.host.util.LinkHelper

import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

public abstract class AbstractTFObjectAwareController {


    public static String formatDateAsString(Date date){
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        DatatypeFactory df = DatatypeFactory.newInstance();
        XMLGregorianCalendar dateTime = df.newXMLGregorianCalendar(calendar);
        String val = dateTime.toString();
        if( val.endsWith("+0000") ){
            return val.replace("+0000", "");
        }else {
            return val;
        }
    }

    private void addTmfiUriList(Map obj, String uriList, String fieldName){
        if( uriList != null && uriList.trim().length() > 0 ){
            obj[fieldName] = []
            for( String uri : uriList.split("\n") ){
                if( uri != null && uri.trim().length() > 0 ){
                    obj[fieldName].add([Identifier: uri.trim()]);
                }
            }
        }
    }

    protected Map buildTdJson(edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td ){
        if( td == null )
            return [:]
        Map json = [
                uniqueId: td.getMetadata().getIdentifier().toString().hashCode(),
                Name: td.getMetadata().getName(),
                Identifier: td.getMetadata().getIdentifier().toString(),
                Version: td.getMetadata().getVersion(),
                Description: td.getMetadata().getDescription(),
                Deprecated: td.getMetadata().isDeprecated(),
                PublicationDateTime: formatDateAsString(td.getMetadata().getPublicationDateTime()),
                _links : [
                        self : [href: td.getMetadata().getIdentifier().toString()+"?format=json"],
                        _formats : [
                                [format: "html", href:  td.getMetadata().getIdentifier().toString()+"?format=html"],
                                [format: "json", href:  td.getMetadata().getIdentifier().toString()+"?format=json"],
                                [format: "xml", href:   td.getMetadata().getIdentifier().toString()+"?format=xml"]
                        ]
                ]
        ]

        if( td.getMetadata().getSatisfies() != null && td.getMetadata().getSatisfies().size() > 0 ){
            json.put("Satisfies", []);
            for(TrustmarkFrameworkIdentifiedObject tmfi : td.getMetadata().getSatisfies() ){
                json.Satisfies.add([Identifier: tmfi.getIdentifier().toString()]);
            }
        }
        if( td.getMetadata().getSupersededBy() != null && td.getMetadata().getSupersededBy().size() > 0 ){
            json.put("SupersededBy", []);
            for(TrustmarkFrameworkIdentifiedObject tmfi : td.getMetadata().getSupersededBy() ){
                json.SupersededBy.add([Identifier: tmfi.getIdentifier().toString()]);
            }
        }
        if( td.getMetadata().getSupersedes() != null && td.getMetadata().getSupersedes().size() > 0 ){
            json.put("Supersedes", []);
            for(TrustmarkFrameworkIdentifiedObject tmfi : td.getMetadata().getSupersedes() ){
                json.Supersedes.add([Identifier: tmfi.getIdentifier().toString()]);
            }
        }

        json.put("Keywords", []);
        for( String keyword : td.getMetadata().getKeywords() ){
            json.Keywords.add(keyword);
        }

        return json;
    }


    protected Map buildTdJson(TrustmarkDefinition td ){
        Map json = [
                uniqueId: td.id,
                Name: td.name,
                Identifier: td.identifier,
                subIdentifier: td.subIdentifier,
                baseUrl: td.getBaseIdentifier(),
                Version: td.tdVersion,
                Description: td.description,
                Deprecated: td.deprecated,
                PublicationDateTime: formatDateAsString(td.publicationDateTime),
                _links : [
                        self : [href: LinkHelper.getLink(request, td, 'json')],
                        _formats : [
                                [format: "html", href: LinkHelper.getLink(request, td, 'html')],
                                [format: "json", href: LinkHelper.getLink(request, td, 'json')],
                                [format: "xml", href:  LinkHelper.getLink(request, td, 'xml')]
                        ]
                ]
        ]

        addTmfiUriList(json, td.getSatisfies(), "Satisfies");
        addTmfiUriList(json, td.getSupersededBy(), "SupersededBy");
        addTmfiUriList(json, td.getSupersedes(), "Supersedes");

        List<KeywordTDLink> keywordLinks = KeywordTDLink.findAllByTd(td);
        if( keywordLinks != null ){
            def keywordList = []
            for( KeywordTDLink link : keywordLinks ){
                keywordList.add(link.keyword.name);
            }
            json.put("Keywords", keywordList);
        }

        // TODO supersedes and superseded by lists

        return json;
    }

    private Map copyMap(Map map){
        Map copy = [:]
        copy.putAll(map);
        return copy
    }

    protected Map buildLinks( String controller, String action, String id, List<String> formats, Map params = [:]){
        Map links = [:]
        List formatMap = []
        for( String format : formats ){
            Map paramsCopy = copyMap(params)
            paramsCopy.put("format", format);
            String link = createLink(controller: controller, action: action, id: id, params: paramsCopy, absolute: true);
            formatMap.add([format: format, href: link]);
        }
        links.put("_formats", formatMap);
        return links;
    }
    protected Map buildLinks( String controller, String action, List<String> formats, Map params = [:]){
        Map links = [:]
        List formatMap = []
        for( String format : formats ){
            Map paramsCopy = copyMap(params)
            paramsCopy.put("format", format);
            String link = createLink(controller: controller, action: action, params: paramsCopy, absolute: true);
            formatMap.add([format: format, href: link]);
        }
        links.put("_formats", formatMap);
        return links;
    }


}
