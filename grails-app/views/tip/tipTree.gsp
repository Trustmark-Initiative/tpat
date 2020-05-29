<html>
    <head>
        <meta name="layout" content="main"/>

        <title>TIP Tree Index</title>


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

    <body>

        <div>
            <h1>High-Level Trust Interoperability Profiles</h1>
            <div>
                A list of prominent trust interoperability profiles (TIPs) that have been created to date.
            </div>

            <div id="topLevelTipsContainer" style="margin-top: 2em;">
                <table class="table table-striped table-condensed table-bordered">
                    <thead>
                        <tr>
                            <th class="nameCol">Trust Interoperability Profile</th>
                            <th class="versionCol">Version</th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:if test="${topLevelTips?.size() > 0}">
                        <g:each in="${topLevelTips}" var="tip">
                            <tmpl:/tip/displayTipAsTableRow tip="${tip}" />
                        </g:each>
                    </g:if><g:else>
                        <tr>
                            <td colspan="2">
                                <em>There are no High-Level Trust Interoperability Profiles defined.</em>
                            </td>
                        </tr>
                    </g:else>
                    </tbody>
                </table>
            </div>

            <div style="margin-top: 2em;">
                <a href="../index.html" class="btn btn-default">&laquo; Index Page</a>
            </div>

        </div>

    </body>

</html>
