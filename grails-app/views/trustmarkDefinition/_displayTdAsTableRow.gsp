<%@ page import="tmf.host.util.LinkHelper" %>
<tr>
    <td class="nameCol">
        <div>
            <g:if test="${td.deprecated}">
                <span class="label label-danger" title="This Trustmark Definition should no longer be used.  There are likely replacements or newer Trustmark Definitions available.">Deprecated</span>
            </g:if>
            <tmf:createLink td="${td}" format="html">${td.name}</tmf:createLink>
            <div style="float: right">
                <tmf:createLink td="${td}" format="xml">XML</tmf:createLink>
                |
                <tmf:createLink td="${td}" format="json">JSON</tmf:createLink>
            </div>
        </div>
        <div class="text-muted" style="margin-left: 0.5em;"><small>${td.description}</small></div>
    </td>
    <td class="versionCol">
        ${td.tdVersion}
    </td>
</tr>