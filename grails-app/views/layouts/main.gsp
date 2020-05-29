<%@ page import="tmf.host.VersionSetSelectingInterceptor" %>
<!doctype html>
<html lang="en" class="no-js">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title>${grailsApplication.config.tf.org.organization}</title>
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
                    <asset:image height="70em" src="${grailsApplication.config.tf.org.banner}" />
                </div>
            </div>

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
