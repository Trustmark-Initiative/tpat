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
                |
                <a title="Copy TD identifier URL to clipboard" onclick="copyFunction('${td.identifier}')">
                    <span class="glyphicon glyphicon-copy"></span>
                </a>
            </div>
        </div>
        <div class="text-muted" style="margin-left: 0.5em;"><small><%= LinkHelper.linkifyText(td.description) %></small></div>
    </td>
    <td class="versionCol">
        ${td.tdVersion}
    </td>
</tr>