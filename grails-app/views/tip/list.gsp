<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>TIP Index</title>
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
                <div>
                    <div style="float: left;">
                        <page:paginate total="${tipCount}" />
                    </div>
                </div>
            </div>
            <div>
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
                    <page:paginate total="${tipCount}" />
                </div>
            </div>

            <div style="margin-top: 2em;">
                This page is also available as <g:link controller="tip" action="list" params="[format: 'json']">JSON</g:link> and
                <g:link controller="tip" action="list" params="[format: 'xml']">XML</g:link>.
            </div>

        </div>
    </body>
</html>
