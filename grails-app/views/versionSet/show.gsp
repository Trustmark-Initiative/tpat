<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Version Set - Show</title>
        <style type="text/css">


        </style>

        <script type="text/javascript">
            var TD_DATA = null;
            var TIP_DATA = null;

            var DEFAULT_MAX = 5;

            $(document).ready(function(){
                setTimeout("loadTds(0, "+DEFAULT_MAX+")", 10);
                setTimeout("loadTips(0, "+DEFAULT_MAX+")", 25);

                setTimeout("loadKeywords()", 250);
            });


            function rebuildReferenceGraph() {
                $.ajax({
                    url: '${createLink(controller: 'versionSet', action: 'rebuildReferences', id: versionSet.name)}',
                    method: 'POST',
                    dataType: 'json',
                    data: {
                        timestamp: new Date().getTime()
                    },
                    success: function(data){
                        $('#rebuildReferenceGraphButton').addClass('disabled');
                        $('#feedbackWindow').html('<asset:image src="spinner.gif" /> Rebuilding...');
                        setTimeout('rebuildReferenceGraphFeedback()', 250);
                    },
                    error: function(){
                        $('#feedbackWindow').html('<div class="alert alert-danger">An unexpected error occurred!  Please refresh this page and try again.</div>');
                    }
                })

            }

            function rebuildReferenceGraphFeedback(){
                $.ajax({
                    url: '${createLink(controller: 'versionSet', action: 'checkOnRebuildReferencesProcess')}',
                    method: 'POST',
                    dataType: 'json',
                    data: {
                        timestamp: new Date().getTime()
                    },
                    success: function(data){
                        if( data.done ){
                            $('#rebuildReferenceGraphButton').removeClass('disabled');
                            $('#feedbackWindow').html('<div class="alert alert-success">Successfully rebuilt reference structure.</div>');
                        }else{
                            $('#feedbackWindow').html("["+data.percent+"% Complete] "+data.message);
                            setTimeout('rebuildReferenceGraphFeedback()', 2000);
                        }
                    },
                    error: function(){
                        $('#feedbackWindow').html('<div class="alert alert-danger">An unexpected error occurred!  Please refresh this page and try again.</div>');
                    }
                })
            }

            function buildKeywords(){
                $.ajax({
                    url: '${createLink(controller: 'versionSet', action: 'buildKeywordForTips', id: versionSet.name)}',
                    method: 'POST',
                    dataType: 'json',
                    data: {
                        timestamp: new Date().getTime()
                    },
                    success: function(data){
                        $('#buildKeywordsButton').addClass('disabled');
                        $('#feedbackWindow').html('<asset:image src="spinner.gif" /> Building...');
                        setTimeout('buildKeywordsFeedback()', 250);
                    },
                    error: function(){
                        $('#feedbackWindow').html('<div class="alert alert-danger">An unexpected error occurred!  Please refresh this page and try again.</div>');
                    }
                })

            }

            function buildKeywordsFeedback(){
                $.ajax({
                    url: '${createLink(controller: 'versionSet', action: 'checkOnBuildKeywordForTips')}',
                    method: 'POST',
                    dataType: 'json',
                    data: {
                        timestamp: new Date().getTime()
                    },
                    success: function(data){
                        if( data.done ){
                            $('#buildKeywordsButton').removeClass('disabled');
                            $('#feedbackWindow').html('<div class="alert alert-success">Successfully built keywords.</div>');
                        }else{
                            $('#feedbackWindow').html("["+data.percent+"% Complete] "+data.message);
                            setTimeout('buildKeywordsFeedback()', 2000);
                        }
                    },
                    error: function(){
                        $('#feedbackWindow').html('<div class="alert alert-danger">An unexpected error occurred!  Please refresh this page and try again.</div>');
                    }
                })
            }


            function loadKeywords() {
                $.ajax({
                    url: '${createLink(controller: 'versionSet', action: 'keywords', id: versionSet.name)}',
                    dataType: 'json',
                    method: 'GET',
                    data: {
                        timestamp: new Date().getTime(),
                        format: 'json'
                    },
                    success: function(data){
                        $('#keywordCount').html('<small>('+data.keywordsCount+')</small>');

                        var html = '';

                        html += '<div class="row">';
                        for( var i = 0; i < data.keywords.length; i++ ){
                            if( i > 0 && i % 6 == 0 ){
                                html += '</div><div class="row">';
                            }
                            var keyword = data.keywords[i];
                            html += '<div class="col-md-2" style="overflow: hidden; white-space: nowrap;">';
                            html += '(' + (keyword.tdCount + keyword.tipCount) + ') ';
                            html += keyword.name;
                            html += '</div>';
                        }
                        html += '</div>';
                        $('#keywordContainer').html(html);
                    },
                    error: function(){

                    }
                });
            }

            function loadTds(offset){
                loadTds(offset, DEFAULT_MAX);
            }

            function loadTds(offset, max){
                if( max == null )
                    max = DEFAULT_MAX;
                console.log("Displaying "+max+" TDS starting at "+offset);
                $.ajax({
                    url: '${createLink(controller: 'versionSet', action: 'trustmarkDefinitions', id: versionSet.name)}',
                    dataType: 'json',
                    method: 'GET',
                    data: {
                        timestamp: new Date().getTime(),
                        format: 'json',
                        offset: offset,
                        max: max
                    },
                    success: function(data){
                        console.log("Successfully loaded TD data: "+JSON.stringify(data));
                        TD_DATA = data;
                        renderTds(data);
                    },
                    error: function(){
                        console.log("ERROR Loading Trustmark Definitions!");
                    }
                });
            }

            function renderTds(data){
                var html = '';

                html += '<table class="table table-striped table-bordered table-condensed">\n';
                if( data && data.tds && data.tds.length > 0 ){
                    for( var i = 0; i < data.tds.length; i++ ){
                        var td = data.tds[i];
                        html += '<tr>';
                        html += '<td>';

                        html += '<div>';
                        html += '<h6 style="margin: 0; padding: 0">'+td.name+", v"+td.version+"</h6>";
                        html += '<div class="text-muted" style="font-size: 80%">'+td.description+'</div>';
                        html += '</div>';

                        html += '</td>';
                        html += '</tr>';
                    }
                }else{
                    html += '<tr><td><em>There are no TDs in this version set</em></td></tr>\n';
                }

                html += '</table>\n';

                if( data.total > 0 ){
                    html += buildPagination(data.offset, data.max, data.total, 'loadTds', false);
                }

                $('#tdCount').html(data.total);
                $('#tdContainer').html(html);
            }


            function loadTips(offset){
                loadTips(offset, DEFAULT_MAX);
            }

            function loadTips(offset, max){
                if( max == null )
                    max = DEFAULT_MAX;
                console.log("Displaying "+max+" TIPs starting at "+offset);
                $.ajax({
                    url: '${createLink(controller: 'versionSet', action: 'trustInteroperabilityProfiles', id: versionSet.name)}',
                    dataType: 'json',
                    method: 'GET',
                    data: {
                        timestamp: new Date().getTime(),
                        format: 'json',
                        offset: offset,
                        max: max
                    },
                    success: function(data){
                        console.log("Successfully loaded TIP data: "+JSON.stringify(data));
                        TIP_DATA = data;
                        renderTips(data);
                    },
                    error: function(){
                        console.log("ERROR Loading Trust Profiles!");
                    }
                });
            }

            function renderTips(data){
                var html = '';

                html += '<table class="table table-striped table-bordered table-condensed">\n';
                if( data && data.tips && data.tips.length > 0 ){
                    for( var i = 0; i < data.tips.length; i++ ){
                        var tip = data.tips[i];
                        html += '<tr>';
                        html += '<td>';

                        html += '<div>';
                        html += '<h6 style="margin: 0; padding: 0">'+tip.name+", v"+tip.version+"</h6>";
                        html += '<div class="text-muted" style="font-size: 80%">'+tip.description+'</div>';
                        html += '</div>';

                        html += '</td>';
                        html += '</tr>';
                    }
                }else{
                    html += '<tr><td><em>There are no TIPs in this version set</em></td></tr>\n';
                }

                html += '</table>\n';

                if( data.total > 0 ){
                    html += buildPagination(data.offset, data.max, data.total, 'loadTips', false);
                }

                $('#tipCount').html(data.total);
                $('#tipContainer').html(html);
            }

        </script>

    </head>

    <body>
        <div id="page-body" role="main">
            <div>

                <div class="row" style="margin-top: 2em;">
                    <div class="col-md-8">
                        <h1 style="margin: 0; padding: 0;">
                            <g:if test="${versionSet.lockedBy != null}">
                                <span class="glyphicon glyphicon-lock" title="Locked by user ${versionSet.lockedBy.username}"></span>
                            </g:if>
<!--                            Version Set: ${versionSet.name}   -->
                        </h1>
                    </div>
                    <div class="col-md-4" style="text-align: right;">
                        <g:if test="${(versionSet.lockedBy == null || versionSet.lockedBy.username.equals(user.username)) && versionSet.isEditable()}">
                            <g:link controller="versionSetEdit" action="index" id="${versionSet.name}" class="btn btn-primary">Edit</g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="versionSet" action="save" class="btn btn-primary">
                                Edit Development &raquo;
                            </g:link>
                        </g:else>
                        <g:if test="${versionSet.lockedBy != null && versionSet.lockedBy.username.equals(user.username)}">
                            <g:link controller="versionSet" action="unlock" id="${versionSet.name}" class="btn btn-success" title="Click here to make this available to others to edit.">
                                Unlock
                            </g:link>
                        </g:if>
                        <g:if test="${versionSet.lockedBy != null && !versionSet.lockedBy.username.equals(user.username)}">
                            <a href="#" class="btn btn-warning">
                                Request Unlock
                            </a>
                            <g:link action="unlock" class="btn btn-danger" id="${versionSet.name}" params="[force: true]">
                                Force Unlock
                            </g:link>
                        </g:if>
                    </div>
                </div>

                <table class="table table-condensed">
                    <tr>
                        <td>Status: </td>
                        <td colspan="3">
                            <g:if test="${versionSet.production}">
                                <span class="label label-danger">PRODUCTION</span>
                            </g:if>
                            <g:elseif test="${versionSet.releasedDate != null}">
                                <span class="label label-warning">LEGACY</span>
                            </g:elseif>
                            <g:else>
                                <span class="label label-primary">DEVELOPMENT</span>
                            </g:else>

                            <g:if test="${versionSet.editable && versionSet.lockedBy == null}">
                                <span class="label label-success">EDITABLE</span>
                            </g:if>
                            <g:elseif test="${versionSet.editable && versionSet.lockedBy != null && versionSet.lockedBy.username.equalsIgnoreCase(user.username)}">
                                <span class="label label-success">EDITABLE</span>
                            </g:elseif>
                            <g:elseif test="${versionSet.editable && versionSet.lockedBy != null && !versionSet.lockedBy.username.equalsIgnoreCase(user.username)}">
                                <span class="label label-danger">
                                    <span class="glyphicon glyphicon-lock"></span>
                                    Locked
                                </span>
                            </g:elseif>
                            <g:else>
                                <span class="label label-danger">NOT EDITABLE</span>
                            </g:else>
                        </td>
                    </tr>
                    <tr>
                        <td>Create Date: </td><td><g:formatDate date="${versionSet.dateCreated}" /></td>
                        <td>Created By: </td><td>${versionSet.createdBy.username}</td>
                    </tr>

                    <tr>
                        <td>Locked Date: </td>
                        <td>
                            <g:if test="${versionSet.lockedDate != null}">
                                <g:formatDate date="${versionSet.lockedDate}" />
                            </g:if>
                            <g:else>
                                <em>Not Locked</em>
                            </g:else>
                        </td>
                        <td>Locked By: </td><td>
                            ${versionSet.lockedBy?.username ?: raw("<em>Not Locked</em>")}
                        </td>
                    </tr>


                    <tr>
                        <td>Release Date: </td>
                        <td>
                            <g:if test="${versionSet.releasedDate != null}">
                                <g:formatDate date="${versionSet.releasedDate}" />
                            </g:if><g:else>
                                <em>Not released yet</em>
                            </g:else>
                        </td>
                        <td>Released By: </td>
                        <td>
                            <g:if test="${versionSet.releasedBy != null}">
                                ${versionSet.releasedBy.username}
                            </g:if><g:else>
                                <em>Not released yet</em>
                            </g:else>
                        </td>
                    </tr>

                    <tr>
                        <td>Predecessor: </td>
                        <td>
                            <g:if test="${versionSet.predecessor != null}">
                                <g:link action="show" id="${versionSet.predecessor.name}">${versionSet.predecessor.name}</g:link>
                            </g:if><g:else>
                                <em>None</em>
                            </g:else>
                        </td>
                        <td>Successor: </td>
                        <td>
                            <g:if test="${versionSet.successor != null}">
                                <g:link action="show" id="${versionSet.successor.name}">${versionSet.successor.name}</g:link>
                            </g:if><g:else>
                                <em>None</em>
                            </g:else>
                        </td>
                    </tr>

                </table>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success" style="margin-top: 3em;">${raw(flash.message)}</div>
            </g:if>
            <g:if test="${flash.error}">
                <div class="alert alert-danger" style="margin-top: 3em;">${raw(flash.error)}</div>
            </g:if>


            <div style="margin-top: 3em;">
%{--
                <g:link action="index" class="btn btn-default">
                    &laquo; Version Set List
                </g:link>
--}%
                <g:if test="${versionSet.editable}">
                    <g:if test="${versionSet.lockedBy != null && user.username.equalsIgnoreCase(versionSet.lockedBy.username)}">
                        <g:link action="moveToProduction" id="${versionSet.name}" class="btn btn-danger" onclick="return confirmRelease();">
                            Release to Production
                        </g:link>
                    </g:if>
                    <g:elseif test="${versionSet.lockedBy == null}">
                        <g:link action="moveToProduction" id="${versionSet.name}" class="btn btn-danger" onclick="return confirmRelease();">
                            Release to Production
                        </g:link>
                    </g:elseif>
                </g:if>
                <g:else>
                    <sec:ifAllGranted roles="ROLE_ADMIN">
                        <a href="javascript:rebuildReferenceGraph();" id="rebuildReferenceGraphButton" class="btn btn-default">Rebuild References</a>
                        %{--<a href="javascript:buildKeywords();" id="buildKeywordsButton" class="btn btn-default">Build TIP Keywords</a>--}%
                    </sec:ifAllGranted>
                </g:else>
            </div>
            <div id="feedbackWindow">&nbsp;</div>

            <div style="margin-top: 2em;" class="row">

                <div class="col-md-6">
                    <h3 style="margin-bottom: 0em;">Trustmark Definitions <small>(<span id="tdCount">-1</span>)</small></h3>
                    <div id="tdContainer">
                        <asset:image src="spinner.gif" /> Loading...
                    </div>
                </div>

                <div class="col-md-6">
                    <h3 style="margin-bottom: 0em;">Trust Interoperability Profiles <small>(<span id="tipCount">-1</span>)</small></h3>
                    <div id="tipContainer">
                        <asset:image src="spinner.gif" /> Loading...
                    </div>
                </div>

            </div>

            <!-- Log Entries
            <div style="margin-top: 3em;" class="row">
                <div class="col-md-12">
                    <h3 style="margin: 0;">Log Entries <small>(${tmf.host.VersionSetLogEntry.countByVersionSet(versionSet)})</small></h3>
                    <table class="table table-condensed table-striped table-bordered">
                        <tbody>
                            <g:if test="${logEntries.size() > 0}">
                                <g:each in="${logEntries}" var="le">
                                    <tr>
                                        <td>${le.type}</td>
                                        <td>
                                            <g:formatDate date="${le.dateCreated}" format="yyyy-MM-dd HH:mm" />
                                        </td>
                                        <td>${le.message}</td>
                                    </tr>
                                </g:each>
                            </g:if>
                            <g:else>
                                <tr>
                                    <td>
                                        <em>There are no log entries.</em>
                                    </td>
                                </tr>
                            </g:else>
                        </tbody>
                    </table>

                    <div class="pull-right">
                        <a href="#" class="btn btn-default">More &raquo;</a>
                    </div>
                </div>
            </div>
-->


            <!-- Keywords -->
            <div style="margin-top: 2em" class="row">
                <div class="col-md-12">
                    <h3>Keywords <span id="keywordCount"></span></h3>
                    <div id="keywordContainer">
                        <asset:image src="spinner.gif" /> Loading...
                    </div>
                </div>
            </div>

            <!-- Primary Tips -->
            <div style="margin-top: 2em" class="row">
                <div class="col-md-12">
                    <h3 style="margin: 0;">Primary Trust Interoperability Profiles <span id="primaryTipCount"><small>(${primaryTIPLinks.size()})</small></span></h3>
                    <div class="text-muted">
                        These TIPs show up on the "TIP Tree" page and are intended to give users a high-level overview of the
                        TIP and TD artifacts hosted in this version set.
                    </div>
                    <div id="primaryTIPLinksContainer" style="margin-top: 1em">
                        <g:if test="${primaryTIPLinks.size() > 0}">
                            <g:each in="${primaryTIPLinks}" var="link">
                                <div style="margin-bottom: 0.5em;">
                                    <div class="btn-group">
                                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                            View <span class="caret"></span>
                                        </button>
                                        <ul class="dropdown-menu">
                                            <li><a href="${createLink(controller: 'versionSet', action: 'showTrustInteroperabilityProfile', id: versionSet.name, params: [tipName: link.trustInteroperabilityProfile.name, tipVersion: link.trustInteroperabilityProfile.tipVersion, format: 'html'])}">HTML</a></li>
                                            <li><a href="${createLink(controller: 'versionSet', action: 'showTrustInteroperabilityProfile', id: versionSet.name, params: [tipName: link.trustInteroperabilityProfile.name, tipVersion: link.trustInteroperabilityProfile.tipVersion, format: 'json'])}">JSON</a></li>
                                            <li><a href="${createLink(controller: 'versionSet', action: 'showTrustInteroperabilityProfile', id: versionSet.name, params: [tipName: link.trustInteroperabilityProfile.name, tipVersion: link.trustInteroperabilityProfile.tipVersion, format: 'xml'])}">XML</a></li>
                                        </ul>
                                    </div>
                                    ${link.trustInteroperabilityProfile.name}, ${link.trustInteroperabilityProfile.tipVersion}
                                </div>
                            </g:each>
                        </g:if>
                        <g:else>
                            <em>There are no primary Trust Interoperability Profiles.</em>
                        </g:else>
                    </div>
                </div>
            </div>
            <sec:ifAllGranted roles="ROLE_ADMIN">
%{--
                <div>
                    <a href="${createLink(controller:'tip', action: 'listPrimary')}" class="btn btn-default">Edit Primary Tips</a>
                </div>
--}%
            </sec:ifAllGranted>


        </div>

    <script type="text/javascript">
        function confirmRelease(){
            return confirm("Are you sure?  Once you do this, you will no longer be able to edit this Version Set, and the previous version set will be marked as legacy.");
        }
    </script>
    </body>
</html>
