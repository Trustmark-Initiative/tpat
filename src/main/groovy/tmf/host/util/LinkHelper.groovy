package tmf.host.util

import org.apache.commons.lang.StringUtils
import tmf.host.TrustInteroperabilityProfile
import tmf.host.TrustmarkDefinition;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils
import java.util.regex.Pattern;

/**
 * Designed to manage complexities around what Grails wants a link to be (ie, /trustmarks/5?format=html) versus what
 * the Trustmark Framework wants links to be (ie, /trustmarks/trustmark-name/1.0/).  It is a strict requirement that
 * URLs always follow the later (we shouldn't be displaying the former in the URL bar ever, as it pisses people around
 * here off).
 * <br/><br/>
 * Created by brad on 2/24/16.
 */
public class LinkHelper {

    private static String buildUriStart(HttpServletRequest request){
        StringBuilder builder = new StringBuilder();
        builder.append("http");
        if( request.isSecure() ){
            builder.append("s");
        }
        builder.append("://");
        builder.append(request.serverName)
        if( !([80, 443].contains(request.serverPort)) ){
            builder.append(":").append(request.serverPort);
        }
        return builder.toString();
    }

    private static String buildBaseURL( HttpServletRequest request ){
        String forwardURI = request?.forwardURI ?: "<UNKNOWN BASE>";
        String baseURI = forwardURI;
        String urlStart = buildUriStart(request);

        if (baseURI.contains(request?.contextPath)){
            baseURI = baseURI.substring(0, baseURI.indexOf(request ?.contextPath))+request ?.contextPath;
        }
        if( !(baseURI?.toLowerCase()?.startsWith("http://") || baseURI?.toLowerCase().startsWith("https://")) ) {
            if( baseURI.startsWith("/") )
                baseURI = urlStart + baseURI;
            else
                baseURI = urlStart + "/" + baseURI;
        }

        if( baseURI.endsWith("/") ){
            baseURI = baseURI.substring(0, baseURI.length() - 1);
        }

        return baseURI;
    }


    /**
     * Given a sub-identifier for an object, such as a TIP, TD or Trustmark, this method will return a fully formed
     * URL created by appending this URL to the request.forwardURI URL (the URL in the browser), replacing the sub-path.
     * We have to do this in development or non-production environments because the TD Identifier does not necessarily
     * match.
     */
    public static String getLink(HttpServletRequest request, String subId){ return getLink(request, subId, "html"); }
    public static String getLink(HttpServletRequest request, String subId, String format){
        return getLink(request, subId, [format: format]);
    }//end getLink()
    public static String getLink(HttpServletRequest request, String subId, Map params){
        String baseURI = buildBaseURL(request);

        if( params == null )
            params = [:]
        if (StringUtils.isBlank(params.format))
            params.format = "html";

        StringBuilder paramString = new StringBuilder();
        Iterator paramNameIter = params?.keySet()?.iterator();
        while( paramNameIter.hasNext() ){
            String paramName = paramNameIter.next()?.toString();
            String paramValue = params.get(paramName)?.toString() ?: '<null>';
            paramString.append(URLEncoder.encode(paramName, "UTF-8")).append("=").append(URLEncoder.encode(paramValue, "UTF-8"));
            if( paramNameIter.hasNext() )
                paramString.append("&");
        }

        if( subId.startsWith("http://") || subId.startsWith("https://") ){
            // TODO - this is a dirty hack for resolving locally when the artifact is not here!  This may cause issues with "blessed" references.
            if( subId.contains("/tds/") ){
                subId = subId.substring(subId.indexOf("/tds/"));
            }else if( subId.contains("/tips/") ){
                subId = subId.substring(subId.indexOf("/tips/"));
            }else{
                if( !subId.contains("?") ){
                    return subId + "?" + paramString.toString();
                }else{
                    // FIXME: This ignores passed parameters.  What we should really do is parse the URL and append / resolve param conflicts.
                    return subId;
                }
            }
        }

        if( subId.startsWith("/") ){
            subId = subId.substring(1);
        }

        return baseURI + "/" + subId + "?${paramString.toString()}";
    }

    public static String getLink(HttpServletRequest request, TrustmarkDefinition td){ return getLink(request, td, "html"); }
    public static String getLink(HttpServletRequest request, TrustmarkDefinition td, String format){
        return getLink(request, td.subIdentifier, format);
    }
    public static String getLink(HttpServletRequest request, TrustmarkDefinition td, Map params){
        return getLink(request, td.subIdentifier, params);
    }

    public static String getLink(HttpServletRequest request, TrustInteroperabilityProfile tip){ return getLink(request, tip, "html"); }
    public static String getLink(HttpServletRequest request, TrustInteroperabilityProfile tip, String format){
        return getLink(request, tip.subIdentifier, format);
    }
    public static String getLink(HttpServletRequest request, TrustInteroperabilityProfile tip, Map params){
        return getLink(request, tip.subIdentifier, params);
    }

    public static String linkifyText(Object content) {
        if (content == null) return content;
        return linkifyText(content.toString());
    }

    public static String linkifyText(URI content) {
        if (content == null) return content;
        return linkifyText(content.toString());
    }

    public static String linkifyText(URL content) {
        if (content == null) return content;
        return linkifyText(content.toString());
    }

    public static String linkifyText(String content){
        if(StringUtils.isBlank(content))
            return content;

        String htmlRegex = "<\\/?[a-z][\\s\\S]*>";
        if (Pattern.matches(htmlRegex, content)) {
            //content already contains HTML - return as is.
            return content;
        }

        String[] tokens = content.split(" ");

        for (int i = 0; i < tokens.length; i++){
            String regex = "\\(?\\bhttps?://[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";

            // if valid match replace this token with desired A HREF string
            if(Pattern.matches(regex, tokens[i])){
                tokens[i] = "<a target='_blank' href='"+tokens[i]+"'>"+tokens[i]+"</a>";
            }
        }

        StringBuilder sbStr = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0)
                sbStr.append(" ");
            sbStr.append(tokens[i]);
        }

        return sbStr.toString();
    }
}//end LinkHelper
