<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>${grailsApplication.config.tf.org.toolheader} | All TIPs</title>
        <style type="text/css">
            .xmlLink {
                text-align: center;
                width: 10%;
            }
            .htmlLink {
                text-align: center;
                width: 10%;
            }
            .versionCol {
                text-align: center;
                width: 10%;
            }
        </style>


    </head>
    <%
        Integer startIndex = Integer.parseInt(params.offset ?: '0') + 1;
        pageScope.setProperty('startIndex', startIndex);

        Integer endIndex = -1;
        Integer max = Integer.parseInt(params.max ?: '20');
        Integer offset = startIndex - 1;
        Integer tipCount = pageScope.getProperty("tipCount");

        if( max + offset > tipCount ){
            endIndex = tipCount;
        }else{
            endIndex = max + offset;
        }
        pageScope.setProperty("endIndex", endIndex);
    %>


    <body>
        <div id="page-body" role="main">
            <div>
                <h1>Trust Interoperability Profiles
                    <g:if test="${tips?.size() > 0}">
                        <small>(${startIndex}-${endIndex} of ${tipCount})</small>
                    </g:if><g:else>
                        <small>(none loaded)</small>
                    </g:else>
                </h1>
                <div style="margin-bottom: 1em;">
                    <form class="form-inline" action="${createLink(action: 'listByName')}">
                        <div class="form-group">
                            <label for="nameRegex">Name Regex</label>
                            <input type="text" class="form-control" id="nameRegex" name="nameRegex" placeholder=".*" value="${params.nameRegex ?: ''}"/>
                        </div>
                        <div class="form-group">
                            <label for="versionRegex">Version Regex</label>
                            <input type="text" class="form-control" id="versionRegex" name="versionRegex" placeholder=".*" value="${params.versionRegex ?: ''}" />
                        </div>

                        <button type="submit" class="btn btn-default">Find</button>
                    </form>
                </div>
            </div>
            <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(params.nameRegex)}">
                <div>
                    <div style="float: left;">
                        <page:paginate total="${tipCount}" params="${params}"/>
                    </div>
                    <table class="table table-striped table-condensed table-bordered">
                        <thead>
                            <tr>
                                <th class="nameCol">Trust Interoperability Profile Name</th>
                                <th class="versionCol">Version</th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:if test="${tips?.size() > 0}">
                                <g:each in="${tips}" var="tip">
                                    <tmpl:/tip/displayTipAsTableRow tip="${tip}" />
                                </g:each>
                            </g:if><g:else>
                                <tr>
                                    <td colspan="2">
                                        <em>There are no Trust Interoperability Profiles defined.</em>
                                    </td>
                                </tr>
                            </g:else>
                        </tbody>
                    </table>
                    <div style="text-align: right;">
                        <page:paginate total="${tipCount}" params="${params}" />
                    </div>
                </div>

                <div style="margin-top: 2em;">
                    This page is also available as <g:link controller="tip" action="list" params="[nameRegex: params.nameRegex, versionRegex: params.versionRegex ?: '', format: 'json']">JSON</g:link> and
                    <g:link controller="tip" action="list" params="[nameRegex: params.nameRegex, versionRegex: params.versionRegex ?: '', format: 'xml']">XML</g:link>.
                </div>
            </g:if>
            <g:else>
                <div class="alert alert-warning" style="margin-top: 2em;">
                    You have not defined any name expression yet.  Please do so in the form above and click "find".
                </div>
            </g:else>

        </div>
    </body>
</html>
