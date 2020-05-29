package tmf.host

import grails.artefact.Interceptor
import grails.plugin.springsecurity.SpringSecurityService
import groovy.json.JsonOutput
import groovy.transform.CompileStatic

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

@CompileStatic
class UrlPrinterInterceptor implements Interceptor {

    int order = HIGHEST_PRECEDENCE

    SpringSecurityService springSecurityService;

    public UrlPrinterInterceptor(){
        matchAll()
    }

    boolean before() {
        if( log.isDebugEnabled() ){
            log.debug(buildDebugMessage())
        }else if( log.isInfoEnabled() ){
            log.info(buildInfoMessage())
        }
        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }

    String getUsername() {
        try{
            Object user = springSecurityService.currentUser;
            if( !user )
                return "<null>";
            return ((User) user).username;
        }catch(Throwable t){
            return "<error>";
        }
    }

    String buildInfoMessage(){
        Map json = [
                forwardUri: request.forwardURI,
                controller: controllerName,
                action: actionName,
                remoteIp: request.remoteAddr,
                user: getUsername(),
                params: request.parameterMap
        ]
        return String.format("HTTPRequest[%s]", JsonOutput.toJson(json));
    }

    String buildDebugMessage(){
        Map jsonMap = [
            forwardUri: request.forwardURI,
            controller: controllerName,
            action: actionName,
            remoteIp: request.remoteAddr,
            request: buildJsonMapFromRequest(),
            user: getUsername()
        ]
        String jsonData = JsonOutput.toJson(jsonMap)
        return String.format("HTTPRequest[%s]", JsonOutput.prettyPrint(jsonData));
    }

    Map buildJsonMapFromRequest(){
        Map json = [
                authType: request.authType,
                contextPath: request.contextPath,
                method: request.method,
                remoteUser: request.remoteUser,
                cookies: buildCookieArray(request.cookies),
                headers: buildHeadersMap(),
                parameters: request.parameterMap,
                attributes: buildAttributesMap()
        ]
        return json;
    }

    Map buildHeadersMap(){
        Map headers = [:]
        Enumeration<String> headerEnum = request.getHeaderNames();
        while (headerEnum.hasMoreElements() ){
            String header = headerEnum.nextElement();
            headers.put(header, request.getHeader(header));
        }
        return headers;
    }

    Map buildAttributesMap(){
        Map attributes = [:]
        Enumeration<String> attrEnum = request.getAttributeNames()
        while (attrEnum.hasMoreElements() ){
            String attributeName = attrEnum.nextElement();
            attributes.put(attributeName, request.getAttribute(attributeName)?.toString());
        }
        return attributes;
    }

    List buildCookieArray(Cookie[] cookies){
        if( cookies != null && cookies.length > 0 ){
            List cookiesList = []
            for( Cookie cookie : cookies ){
                cookiesList.add(cookieToJson(cookie));
            }
            return cookiesList;
        }else{
            return null;
        }
    }

    Map cookieToJson(Cookie cookie){
        Map json = [
                name: cookie.name,
                version: cookie.version,
                value: cookie.value
        ]
        if( cookie.comment != null )
            json.put("comment", cookie.comment);

        if( cookie.domain != null )
            json.put("domain", cookie.domain);

        if( cookie.httpOnly == true )
            json.put("httpOnly", cookie.httpOnly);

        if( cookie.secure == true )
            json.put("secure", cookie.secure);

        if( cookie.maxAge > -1 )
            json.put("maxAge", cookie.maxAge);

        if( cookie.path != null )
            json.put("path", cookie.path);

        return json;
    }

}
