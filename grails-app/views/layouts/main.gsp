<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page import="tmf.host.VersionSetSelectingInterceptor" %>
<!doctype html>
<html lang="en" class="no-js">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title><g:layoutTitle default="${grailsApplication.config.tf.org.toolheader}"/></title>
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <asset:stylesheet src="application.css" />

        <asset:javascript src="application.js" />

        <g:layoutHead/>
    </head>
    <body>

        <tmpl:/layouts/navbar />

        <a name="top"></a>
        <div class="container" style="margin-top: 5em; margin-bottom: 5em;">
            <div class="header">
                <div class="topImageContainer">
                    <asset:image height="90em" src="${grailsApplication.config.tf.org.banner}" />
                </div>
            </div>

        %{--        Warning banner for users with no TPAT admin role. --}%
            <sec:authorize access="isAuthenticated()">
                <sec:authorize access="!hasAuthority('tpat-admin')">
                    <div class="container pt-4" id="users-with-no-tbr-roles-warning-message">
                        <div class="alert alert-warning d-flex " role="alert">
                            <i class="bi bi-exclamation-triangle-fill"></i>
                            <div>
                                You have no TPAT administrator role assigned. You must have the TPAT administrator role in order to use the TPAT. Contact your TPAT administrator for help.
                            </div>
                        </div>
                    </div>
                </sec:authorize>
            </sec:authorize>

            <div>
                <g:layoutBody/>
            </div>

            <div id="footer">
                <div>
                    Copyright &copy; Georgia Tech Research Institute
                    <g:if test="${session.getAttribute(tmf.host.VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE) == null}">
                        <span class="glyphicon glyphicon-alert" title="There are no version sets!"></span>
                    </g:if><g:else>
                        <span class="glyphpro glyphpro-circle_info" title="Current Version Set: ${session.getAttribute(tmf.host.VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE) ?: '<NONE>'}"></span>
                    </g:else>
                </div>
                <div>
                    v.<g:meta name="info.app.version"/>,
                    Build Date: <g:meta name="info.app.buildDate"/>
                </div>
        </div><!-- /.container -->

        <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
    </body>
</html>
