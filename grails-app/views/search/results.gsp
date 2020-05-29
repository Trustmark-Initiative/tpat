<!-- For displaying HTML search results -->

<div class="searchResultsContainer">
    <h3>Trust Interoperability Profile Results <small>(${tipResults?.size() ?: 0} matching of ${tipCount} total)</small></h3>
    <g:if test="${tipResults?.size() > 0}">
        <table class="table table-condensed table-bordered table-striped">
            <g:each in="${tipResults}" var="vsTipLink" status="vsTipLinkIndex">
                <g:if test="${vsTipLinkIndex < 15}">
                    <tmpl:/tip/displayTipAsTableRow tip="${vsTipLink.trustInteroperabilityProfile}" />
                </g:if>
            </g:each>
            <g:if test="${tipResults.size() > 15}">
                <tr>
                    <td colspan="2">
                        <em>Additional rows (beyond 15) have been suppressed.  Please narrow your search criteria.</em>
                    </td>
                </tr>
            </g:if>
        </table>
    </g:if><g:else>
        <div style="margin-top: 1em;">
            <em>Did not match any Trust Interoperability Profiles.</em>
        </div>
    </g:else>
</div>

<hr />

<div class="searchResultsContainer">
    <h3>Trustmark Definition Results <small>(${tdResults?.size() ?: 0} matching of ${tdCount} total)</small></h3>
    <g:if test="${tdResults?.size() > 0}">
        <table class="table table-condensed table-bordered table-striped">
            <g:each in="${tdResults}" var="vsTdLink" status="vsTdLinkIndex">
                <g:if test="${vsTdLinkIndex < 15}">
                    <tmpl:/trustmarkDefinition/displayTdAsTableRow td="${vsTdLink.trustmarkDefinition}" />
                </g:if>
            </g:each>
            <g:if test="${tdResults.size() > 15}">
                <tr>
                    <td colspan="2">
                        <em>Additional rows (beyond 15) have been suppressed.  Please narrow your search criteria.</em>
                    </td>
                </tr>
            </g:if>
        </table>
    </g:if><g:else>
    <div style="margin-top: 1em;">
        <em>Did not match any Trustmark Definitions.</em>
    </div>
</g:else>
</div>











%{--Found a total of ${tdResults?.size()} TDs of ${tdCount} <br/>--}%

%{--Found a total of ${tipResults?.size()} TIPs of ${tipCount} <br/>--}%