<!doctype html>
<html>
    <head>
        <%--
            NOTE:
            With <base target="_blank", you have to specify target="_self" on anchors used as buttons!
        --%>
        <base target="_blank"/>
        <meta name="layout" content="main"/>
        <title>View TIP: ${tip.name}</title>
        <style type="text/css">
        </style>


    </head>



    <body>
        <div id="page-body" role="main">
            <div>
                <h2>${tip.name}, v${tip.version}</h2>
            </div>
            <div style="font-size: 110%;">${raw(tip.description)}</div>

            <g:if test="${tip.deprecated}">
                <div class="alert alert-warning" style="margin-top: 1em;">
                    <div style="font-weight: bold; font-style: italic; font-size: 120%;">
                        DEPRECATED
                    </div>
                    <div>
                        This Trust Profile has been deprecated.  You should not use this profile for any reason.
                    </div>
                    <g:if test="${tip.supersededBy?.size() > 0}">
                        <div>
                            This Trust Profile has been superseded by the following:
                            <ul>
                                <g:each in="${tip.supersededBy}" var="ref">
                                    <g:if test="${ref.name && ref.name.trim().length() > 0}">
                                        <li>
                                            <a href="${ref.identifier}">
                                                ${ref.name}, v${ref.version}
                                            </a>
                                        </li>
                                    </g:if><g:else>
                                        <li>
                                            <a href="${ref.identifier}">${ref.identifier}</a>
                                        </li>
                                    </g:else>
                                </g:each>
                            </ul>
                        </div>
                    </g:if>
                </div>
            </g:if>

            <g:if test="${references?.size() > 0}">
                <div style="margin-top: 1em;">
                    <g:each in="${references}" var="ref">
                        <div>
                            Contained in Trust Profile: <a href="${ref.sourceTip.identifier}">${ref.sourceTip.name}, v${ref.sourceTip.tipVersion}</a>
                        </div>
                    </g:each>
                </div>
            </g:if>

            <div style="margin-top: 1em;">
                <table class="table table-condensed table-bordered table-striped">
                    <tr>
                        <td style="width: 20%;"><abbr title="The date and time at which this Trust Interoperability Profile was published">Publication Date:</abbr></td>
                        <td style="width: 80%;"><g:formatDate date="${tip.publicationDateTime}" format="yyyy-MM-dd" /></td>
                    </tr>
                    <tr>
                        <td style="width: 20%;"><abbr title="The organization that published this Trust Interoperability Profile">Issuing Organization: </abbr></td>
                        <td style="width: 80%;">
                            <div>
                                ${tip.issuer.name} (<a href="${tip.issuer.identifier.toString()}" target="_blank">${tip.issuer.identifier.toString()}</a>)
                                <a href="javascript:showContact()" target="_self" class="btn btn-xs btn-default">View Contact<g:if test="${tip.issuer.contacts.size() > 1}">s</g:if></a>
                            </div>
                            <div id="contact">
                                <table class="table table-condensed">
                                    <g:each in="${tip.issuer.contacts}" var="contact">
                                        <tr style="border: none;">
                                            <td>${contact.responder ?: raw('<em>No Responder</em>')}</td>
                                            <td>${contact.defaultEmail ?: raw('<em>No Email Address</em>')}</td>
                                            <td>${contact.defaultTelephone ?: raw('<em>No telephone</em>')}</td>
                                            <td>${contact.defaultMailingAddress ?: raw('<em>No Mailing Address</em>')}</td>
                                        </tr>
                                    </g:each>
                                </table>
                            </div>
                            <script type="text/javascript">
                                $(document).ready(function(){
                                    $('#contact').hide();
                                })

                                function showContact(){
                                    $('#contact').show();
                                }
                            </script>
                        </td>
                    </tr>
                    <tr>
                        <td style="width: 20%;">Keywords: </td>
                        <td style="width: 80%;">
                            <g:if test="${tip.keywords && tip.keywords.size() > 0}">
                                <g:each in="${tip.keywords}" var="keyword">
                                    ${keyword},
                                </g:each>
                            </g:if><g:else>
                                <em>There are no keywords.</em>
                            </g:else>
                        </td>
                    </tr>
                    <g:if test="${tip.supersedes?.size() > 0}">
                        <tr>
                            <td style="width: 20%;"><abbr title="A list of other Trust Interoperability Profiles that this Trust Interoperability Profile supersedes or replaces">Supersedes: </abbr></td>
                            <td style="width: 80%; font-size: 90%;" class="text-muted">
                                <ul>
                                    <g:each in="${tip.supersedes}" var="ref">
                                        <g:if test="${ref.name && ref.name.trim().length() > 0}">
                                            <li>
                                                <a href="${ref.identifier}">
                                                    ${ref.name}, v${ref.version}
                                                </a>
                                            </li>
                                        </g:if>
                                        <g:else>
                                            <li>
                                                <a href="${ref.identifier}">${ref.identifier}</a>
                                            </li>
                                        </g:else>
                                    </g:each>
                                </ul>
                            </td>
                        </tr>
                    </g:if>
                    <g:if test="${tip.supersededBy?.size() > 0}">
                        <tr>
                            <td style="width: 20%;"><abbr title="A list of other Trust Interoperability Profiles that supersede or replace this Trust Interoperability Profile">Superseded By: </abbr></td>
                            <td style="width: 80%; font-size: 90%;" class="text-muted">
                                <ul>
                                    <g:each in="${tip.supersededBy}" var="ref">
                                        <g:if test="${ref.name && ref.name.trim().length() > 0}">
                                            <li>
                                                <a href="${ref.identifier}">
                                                    ${ref.name}, v${ref.version}
                                                </a>
                                            </li>
                                        </g:if>
                                        <g:else>
                                            <li>
                                                <a href="${ref.identifier}">${ref.identifier}</a>
                                            </li>
                                        </g:else>
                                    </g:each>
                                </ul>
                            </td>
                        </tr>
                    </g:if>
                    <g:if test="${tip.satisfies?.size() > 0}">
                        <tr>
                            <td style="width: 20%;"><abbr title="A list of Trustmark Definitions, indicating that if an entity satisfies the conformance criteria for this Trust Interoperability Profile, then the entity may also satisfy the conformance criteria for the referenced Trustmark Definitions">Satisfies: </abbr></td>
                            <td style="width: 80%; font-size: 90%;" class="text-muted">
                                <ul>
                                    <g:each in="${tip.satisfies}" var="ref">
                                        <g:if test="${ref.name && ref.name.trim().length() > 0}">
                                            <li>
                                                <a href="${ref.identifier}">
                                                    ${ref.name}, v${ref.version}
                                                </a>
                                            </li>
                                        </g:if>
                                        <g:else>
                                            <li>
                                                <a href="${ref.identifier}">${ref.identifier}</a>
                                            </li>
                                        </g:else>
                                    </g:each>
                                </ul>
                            </td>
                        </tr>
                    </g:if>
                    <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(tip.legalNotice)}">
                        <tr>
                            <td style="width: 20%;">Legal Notice: </td>
                            <td style="width: 80%; font-size: 90%;" class="text-muted">
                                ${tip.legalNotice}
                            </td>
                        </tr>
                    </g:if>
                    <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(tip.notes)}">
                        <tr>
                            <td style="width: 20%;">Notes: </td>
                            <td style="width: 80%; font-size: 90%;" class="text-muted">
                                ${tip.notes}
                            </td>
                        </tr>
                    </g:if>
                    %{-- TODO: need to show Known Conflicts when available --}%
                    %{--<g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(tip.knownConflicts)}">--}%
                        %{--<tr>--}%
                            %{--<td style="width: 20%;"><abbr title="A list of Trust Interoperability Profiles, indicating that if an entity satisfies the conformance criteria for this Trust Interoperability Profile, then the entity is unlikely to satisfy the conformance criteria for the referenced Trust Interoperability Profiles">Known Conflicts: </abbr></td>--}%
                            %{--<td style="width: 80%; font-size: 90%;" class="text-muted">--}%
                                %{--${tip.knownConflicts}--}%
                            %{--</td>--}%
                        %{--</tr>--}%
                    %{--</g:if>--}%
                </table>
            </div>

            <div>
                <!-- Nav tabs -->
                <ul class="nav nav-tabs" role="tablist">
                    <li role="presentation" class="active"><a href="#tree" target="_self" aria-controls="tree" role="tab" data-toggle="tab">Tree View</a></li>
                    <li role="presentation"><a href="#details" target="_self" aria-controls="profile" role="tab" data-toggle="tab">Details View</a></li>
                </ul>

                <!-- Tab panes -->
                <div class="tab-content">
                    <div role="tabpanel" class="tab-pane active" id="tree">
                        <div style="margin-top: 1em;" id="treeData">
                            <asset:image src="spinner.gif" /> Loading...
                        </div>
                    </div>
                    <div role="tabpanel" class="tab-pane" id="details">
                        <div style="margin-top: 1em;">
                            <h4>Trust Expression:</h4>
                            <div id="tipExpression" class="trexp" style="border: 1px solid #aaa; padding: 0.5em; font-family: monospace; margin-bottom: 2em;">${tip.trustExpression}</div>
                            <div>
                                <h4>References (${tip.references.size()})</h4>
                                <table class="table table-condensed table-bordered table-striped">
                                    <g:each in="${tip.references}" var="reference">
                                        <tr>
                                            <td>${reference.id}</td>
                                            <td>
                                                <div style="font-size: 110%; font-weight: bold">
                                                    <g:if test="${reference.isTrustInteroperabilityProfileReference()}">
                                                        <span class="glyphicon glyphicon-th-list" title="Trust Interoperability Profile"></span>
                                                    </g:if>
                                                    <g:if test="${reference.isTrustmarkDefinitionRequirement()}">
                                                        <span class="glyphicon glyphicon-tag" title="Trustmark Definition Requirement"></span>
                                                    </g:if>
                                                    <a href="${reference.identifier.toString()}">
                                                        ${reference.name}, v${reference.version}
                                                    </a>
                                                </div>
                                                <div style="padding-left: 1.5em;">
                                                    ${reference.description}
                                                </div>
                                            </td>
                                        </tr>
                                    </g:each>
                                </table>
                            </div>

                            %{-- TODO: enable Sources and Terms inside of TIPs --}%
                            %{--<g:if test="${tip.sources.size() > 0}">--}%
                                %{--<div style="margin-top: 2em;">--}%
                                    %{--<h4 style="margin-bottom: 0;">Sources (${tip.sources.size()})</h4>--}%
                                    %{--<div>--}%
                                        %{--<table class="table table-condensed table-bordered table-striped">--}%
                                            %{--<g:each in="${tip.sources}" var="source">--}%
                                                %{--<tr>--}%
                                                    %{--<td style="width: 20%; font-size: 120%; font-weight: bold;">${source.identifier}</td>--}%
                                                    %{--<td>${raw(source.reference)}</td>--}%
                                                %{--</tr>--}%
                                            %{--</g:each>--}%
                                        %{--</table>--}%
                                    %{--</div>--}%
                                %{--</div>--}%
                            %{--</g:if>--}%

                            %{--<g:if test="${tip.terms.size() > 0}">--}%
                                %{--<div style="margin-top: 2em;">--}%
                                    %{--<h4 style="margin-bottom: 0;">Terms (${tip.terms.size()})</h4>--}%
                                    %{--<div>--}%
                                        %{--<table class="table table-condensed table-bordered table-striped">--}%
                                            %{--<thead>--}%
                                            %{--<tr>--}%
                                                %{--<th>Term Name</th>--}%
                                                %{--<th>Abbreviations</th>--}%
                                                %{--<th>Definition</th>--}%
                                            %{--</tr>--}%
                                            %{--</thead>--}%
                                            %{--<tbody>--}%
                                            %{--<g:each in="${tip.termsSorted}" var="term">--}%
                                                %{--<tr>--}%
                                                    %{--<td style="width: 20%;">${term.name}</td>--}%
                                                    %{--<td style="text-align: center; width: 10%;">--}%
                                                        %{--<g:each in="${term.abbreviations}" status="index" var="abbr">--}%
                                                            %{--${abbr}<g:if test="${index < (term.abbreviations.size() - 1)}">, </g:if>--}%
                                                        %{--</g:each>--}%
                                                    %{--</td>--}%
                                                    %{--<td>--}%
                                                        %{--${raw(term.definition)}--}%
                                                    %{--</td>--}%
                                                %{--</tr>--}%
                                            %{--</g:each>--}%
                                            %{--</tbody>--}%
                                        %{--</table>--}%

                                    %{--</div>--}%
                                %{--</div>--}%
                            %{--</g:if>--}%
                        </div>
                    </div>
                </div>

            </div>


            <div style="margin-top: 4em;">
                Also available as
                <a href="${tip.identifier}?format=xml" target="_blank">XML</a> or
                <a href="${tip.identifier}?format=json" target="_blank">JSON</a>
            </div>

            <script type="text/javascript">

                let tipTree = null;

                $(document).ready(function()  {
                    $('#treeData').html('<asset:image src="spinner.gif" /> Loading Tree...');
                    loadTree(${databaseTip.id});
                });


                function loadTree(id)  {
                    var url = '${createLink(controller:'tip', action:'viewTipTree', id: "__UNIQUE_ID__")}';
                    url = url.replace("__UNIQUE_ID__", id);
                    console.log("Loading tree data: "+url);
                    $.ajax({
                        url: url,
                        dataType: 'json',
                        data: {'timestamp' : new Date().getTime(), format: 'json'},
                        error: function(jqXHR, textStatus, err) {
                            $('#treeData').html('<div class="alert alert-danger">Error downloading Trust Profile Tree!  Response: <br/>'+jqXHR.responseText+'</div>');
                        },
                        success: function(data, textStatus, jqXHR){
                            if( data ) {
                                console.log("Successfully received Server Tree Response for: "+data.tree.uniqueId);
                                addGuid(data.tree);
                                tipTree = styleTree(data);
                            }else{
                                $('#treeData').html('<div class="alert alert-danger">Error downloading TIP information!  Response: <br/>'+jqXHR.responseText+'</div>');
                            }
                        }
                    });
                }  // end loadTree()

                /**
                 * The incoming parameter is a TIP Tree built by the server (ie, data.tree).  It represents a TIP, and assumes
                 * that it only has a uniqueId field throughout it.  The goal here is to add a GUID field to each node in this
                 * tree, so that it can be styled as HTML with no overlapping ids (in case a TIP is used twice in this HTML document).
                 * The tip and subtree is modified inline.
                 */
                function addGuid(tip) {
                    tip.guid = "tipInTree_"+guid();
                    if( tip.tips && tip.tips.length > 0 ) {
                        for (var i = 0; i < tip.tips.length; i++) {
                            var subtip = tip.tips[i];
                            addGuid(subtip);
                        }
                    }

                    if( tip.tds && tip.tds.length > 0 ) {
                        for (var i = 0; i < tip.tds.length; i++) {
                            var td = tip.tds[i];
                            td.guid = "tdInTree_" + guid();
                        }
                    }
                }  //  end addGuids()

                var GUID_COUNTER = 1;
                // @see http://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
                function guid() {
                    return GUID_COUNTER++;
                }

                function expandAll(){
                    $('#tipTreeTable_'+ROOT_GUID).treetable('expandAll');
                }

                function collapseAll(){
                    $('#tipTreeTable_'+ROOT_GUID).treetable('collapseAll');
                }

                var ROOT_GUID = null;
                /**
                 * Styles the given TIP Tree data as a "tree table" in HTML and javascript.
                 */
                function styleTree(data)  {
                    var html = "";
                    console.log("Constructing Tree Table HTML: "+data.tree.guid);
                    html += '<table id="tipTreeTable_'+data.tree.guid+'" class="table table-condensed table-bordered">';
                    ROOT_GUID = data.tree.guid;
                    html += recursivelyBuildTreeData(null, data.tree, data.tds, data.tips);
                    html += '</table>';
                    $('#treeData').html(html); // Top level tips only exist 1 time, so it's safe to use the uniqueId instead of the guid
                    console.log("Calling JQuery TreeTable method["+data.tree.guid+"]...");
                    let tipTree = $("#tipTreeTable_"+data.tree.guid).treetable({expandable: true, clickableNodeNames: true});
                    $("#tipTreeTable_"+data.tree.guid).treetable('expandNode', ROOT_GUID);
                    return tipTree;
                }  //  end styleTree()

                /**
                 *  loads tips into the existing tree, not used, yet
                 */
                function loadTipIntoTree(id, parent, tipTree)  {
                    var url = '${createLink(controller:'tip', action:'viewTipTree', id: "__UNIQUE_ID__")}';
                    url = url.replace("__UNIQUE_ID__", id);
                    console.log("Loading tree data: "+url);
                    $.ajax({
                        url: url,
                        dataType: 'json',
                        data: {'timestamp' : new Date().getTime(), format: 'json'},
                        error: function(jqXHR, textStatus, err) {
                            $('#treeData').html('<div class="alert alert-danger">Error downloading Trust Profile Tree!  Response: <br/>'+jqXHR.responseText+'</div>');
                        },
                        success: function(data, textStatus, jqXHR){
                            if( data ) {
                                console.log("Successfully received Server Tree Response for: "+data.tree.uniqueId);
                                addGuid(data.tree);
                                addTip(tipTree, parent, data.tree, data.tips, data.tds)
                            }else{
                                $('#treeData').html('<div class="alert alert-danger">Error downloading TIP information!  Response: <br/>'+jqXHR.responseText+'</div>');
                            }
                        }
                    });
                }  // end loadTree()

                /**
                 * creates a tip tree and loads it with the corresponding data, not used
                 */
                function createTipTree(data)  {
                    console.log('loadTipTree '+data.tips[data.tree.uniqueId].Name+ ' -> '+data.tree.guid);
                    $('#treeData').html('<table id="tipTreeTable_'+data.tree.guid+'" class="table table-condensed table-bordered"></table>');
                    let tipTree = $("#tipTreeTable_"+data.tree.guid).treetable({expandable: true, clickableNodeNames: true});
                    addTip(tipTree, null, data.tree, data.tips, data.tds);
                    tipTree.treetable('collapseAll');
                    tipTree.treetable('expandNode', data.tree.guid);
                    return tipTree;
                }

                /**
                 * recursive function to load a tip tree with corresponding TIPS and TDS, future use
                 * @param tipTree
                 * @param parent
                 * @param tree
                 * @param tips
                 * @param tds
                 * @returns {*}
                 */
                function addTip(tipTree, parent, tree, tips, tds)  {
                    let node = loadTipNode(tipTree, parent, tree.guid, tips[tree.uniqueId]);
                    tree.tips.forEach(tp => {
                        addTip(tipTree, node, tp, tips, tds);
                    });
                    tree.tds.forEach(td => {
                        loadTdNode(tipTree, node, td.guid, tds[td.uniqueId]);
                    });
                    return node;
                }

                /**
                 * loads a single TIP into the tree table
                 * @param tipTree
                 * @param parent
                 * @param id
                 * @param tip
                 * @returns {jQuery}
                 */
                function loadTipNode(tipTree, parent, id, tip)  {
                    var html = '<tr data-tt-id="'+id+'"';
                    if(parent == null)  {
                        html += '>\n';
                    }  else  {
                        html += ' data-tt-parent-id="'+parent.id+'">\n';
                    }
                    html += "    <td><span class=\"glyphicon glyphicon-list\" title=\"Trust Interoperability Profile\"></span> &nbsp;"+tip.Name+"</td>";
                    html += '    <td style=\"width: 7%; text-align: center;\">'+tip.Version+'</td>';
                    html += "    <td style=\"width: 4%; text-align: center;\"><a href=\""+findLinkFormat(tip._links, "html")+"\">HTML</a></td>";
                    html += "    <td style=\"width: 4%; text-align: center;\"><a href=\""+findLinkFormat(tip._links, "xml")+"\">XML</a></td>";
                    html += "    <td style=\"width: 4%; text-align: center;\"><a href=\""+findLinkFormat(tip._links, "json")+"\">JSON</a></td>";
                    html += '</tr>'
                    tipTree.treetable('loadBranch', parent, html);
                    return tipTree.treetable('node', id);
                }

                /**
                 * loads a single TD into the tree table
                 * @param tipTree
                 * @param parent
                 * @param id
                 * @param td
                 * @returns {jQuery}
                 */
                function loadTdNode(tipTree, parent, id, td)  {
                    var html = '<tr data-tt-id="'+id+'" data-tt-parent-id="'+parent.id+'">';
                    html += "<td>";
                    html += "<span class=\"glyphicon glyphicon-tag\" title=\"Trustmark Definition\"></span> &nbsp;";
                    html += td.Name;
                    html += "</td>";
                    html += '<td style=\"width: 7%; text-align: center;\">'+td.Version+'</td>';
                    html += "<td style=\"width: 4%; text-align: center;\"><a href=\""+findLinkFormat(td._links, "html") + '">HTML</a></td>';
                    html += "<td style=\"width: 4%; text-align: center;\"><a href=\""+findLinkFormat(td._links, "xml") + '">XML</a></td>';
                    html += "<td style=\"width: 4%; text-align: center;\"><a href=\""+findLinkFormat(td._links, "json") + '">JSON</a></td>';
                    html += '</tr>';
                    tipTree.treetable('loadBranch', parent, html);
                    return tipTree.treetable('node', id);
                }

                /**
                 * recursively builds an html table of TIPs and TDs
                 * @param parent
                 * @param tip
                 * @param tds
                 * @param tips
                 * @returns {string|null}
                 */
                function recursivelyBuildTreeData(parent, tip, tds, tips) {
                    var html = "";

                    var actualTip = tips[tip.uniqueId];
                    if( !actualTip )  {
                        console.log("ERROR!  Cannot find any TIP with id = "+tip.uniqueId);
                        return null;
                    }

                    html += '<tr data-tt-id="'+tip.guid+'"';
                    if( parent != null )
                        html += ' data-tt-parent-id="'+parent.guid+'"';
                    html += '>\n';

                    html += "    <td><span class=\"glyphicon glyphicon-list\" title=\"Trust Interoperability Profile\"></span> &nbsp;"+actualTip.Name+"</td>";
                    html += '    <td style=\"width: 7%; text-align: center;\">'+actualTip.Version+'</td>';
                    html += "    <td style=\"width: 4%; text-align: center;\"><a href=\""+findLinkFormat(actualTip._links, "html")+"\">HTML</a></td>";
                    html += "    <td style=\"width: 4%; text-align: center;\"><a href=\""+findLinkFormat(actualTip._links, "xml")+"\">XML</a></td>";
                    html += "    <td style=\"width: 4%; text-align: center;\"><a href=\""+findLinkFormat(actualTip._links, "json")+"\">JSON</a></td>";
                    html += '</tr>'

                    if( tip.tips && tip.tips.length > 0 ){
                        for( var i = 0; i < tip.tips.length; i++ ){
                            html += recursivelyBuildTreeData(tip, tip.tips[i], tds, tips);
                        }
                    }

                    if( tip.tds && tip.tds.length > 0 ){
                        for( var i = 0; i < tip.tds.length; i++ ){
                            var td = tds[tip.tds[i].uniqueId];

                            html += '<tr data-tt-id="'+td.guid+'" data-tt-parent-id="'+tip.guid+'">';
                            html += "<td>";
                            html += "<span class=\"glyphicon glyphicon-tag\" title=\"Trustmark Definition\"></span> &nbsp;";
                            html += td.Name;
                            html += "</td>";
                            html += '<td style=\"width: 7%; text-align: center;\">'+td.Version+'</td>';
                            html += "<td style=\"width: 4%; text-align: center;\"><a href=\""+findLinkFormat(td._links, "html") + '">HTML</a></td>';
                            html += "<td style=\"width: 4%; text-align: center;\"><a href=\""+findLinkFormat(td._links, "xml") + '">XML</a></td>';
                            html += "<td style=\"width: 4%; text-align: center;\"><a href=\""+findLinkFormat(td._links, "json") + '">JSON</a></td>';
                            html += '</tr>';
                        }
                    }
                    // TODO Display TDs...

                    return html;
                }//end recursivelyBuildTreeData

                function findLinkFormat( links, format ){
                    if( links != null && links._formats.length > 0 ) {
                        for (var i = 0; i < links._formats.length; i++) {
                            var link = links._formats[i];
                            if ( link.format == format) {
                                return link.href;
                            }
                        }
                    }else{
                        return null;
                    }
                }

            </script>
        </div>
    </body>
</html>
