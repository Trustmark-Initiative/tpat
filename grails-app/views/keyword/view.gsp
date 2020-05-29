<h2 class="keywordTitle">Keyword: ${keyword.name}</h2>

<div>
    <h3>
        Trust Interoperability Profiles
        <small>(${tipCount})</small>
    </h3>
    <g:if test="${tipCount > 0}">
        <div style="max-height: 30em; overflow-y: auto; overflow-x: hidden;">
            <table class="table table-striped table-condensed table-bordered">
                <thead>
                <tr>
                    <th class="nameCol">TIP Name</th>
                    <th class="versionCol">Version</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${tips}" var="tip">
                    <tmpl:/tip/displayTipAsTableRow tip="${tip}" />
                </g:each>
                </tbody>
            </table>
        </div>
    </g:if><g:else>
        <div class="alert alert-warning">There are no Trust Interoperability Profiles</div>
    </g:else>
</div>

<div>
    <h3>
        Trustmark Definitions
        <small>(${tdCount})</small>
    </h3>
    <g:if test="${tdCount > 0}">
        <div style="max-height: 40em; overflow-y: auto; overflow-x: hidden;">
            <table class="table table-striped table-condensed table-bordered">
                <thead>
                    <tr>
                        <th class="nameCol">Trustmark Definition Name</th>
                        <th class="versionCol">Version</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${tds}" var="td">
                        <tmpl:/trustmarkDefinition/displayTdAsTableRow td="${td}" />
                    </g:each>
                </tbody>
            </table>
        </div>
    </g:if><g:else>
        <div class="alert alert-warning">There are no Trustmark Definitions</div>
    </g:else>
</div>
