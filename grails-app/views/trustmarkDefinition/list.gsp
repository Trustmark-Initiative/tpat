<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Trustmark Definition List</title>
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
        Integer trustmarkDefinitionsCount = pageScope.getProperty("trustmarkDefinitionsCount");

        if( max + offset > trustmarkDefinitionsCount ){
            endIndex = trustmarkDefinitionsCount;
        }else{
            endIndex = max + offset;
        }
        pageScope.setProperty("endIndex", endIndex);
    %>
    <body>
        <div id="page-body" role="main">
            <div>
                <h1>Trustmark Definitions
                    <small>(${startIndex}-${endIndex} of ${trustmarkDefinitionsCount})</small>
                </h1>
                <div>
                    <div style="float: left;">
                        <page:paginate total="${trustmarkDefinitionsCount}" params="[showDeprecated: params.showDeprecated ?: 'false']"/>
                    </div>
                    <div style="float: right;">

                    </div>
                </div>
            </div>
            <div>
                <table class="table table-striped table-condensed table-bordered">
                    <thead>
                        <tr>
                            <th class="nameCol">Trustmark Definition Name</th>
                            <th class="versionCol">Version</th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:if test="${trustmarkDefinitions?.size() > 0}">
                            <g:each in="${trustmarkDefinitions}" var="td">
                                <tmpl:/trustmarkDefinition/displayTdAsTableRow td="${td}" />
                            </g:each>
                        </g:if><g:else>
                            <tr>
                                <td colspan="2">
                                    <em>There are no Trustmark Definitions defined.</em>
                                </td>
                            </tr>
                        </g:else>
                    </tbody>
                </table>
                <div style="text-align: right;">
                    <page:paginate total="${trustmarkDefinitionsCount}" params="[showDeprecated: params.showDeprecated ?: 'false']"/>
                </div>
            </div>

            <div style="margin-top: 2em;">
                This page is also available as <g:link controller="trustmarkDefinition" action="list" params="[format: 'json']">JSON</g:link> and
                <g:link controller="trustmarkDefinition" action="list" params="[format: 'xml']">XML</g:link>.
            </div>
        </div>
    </body>
</html>
