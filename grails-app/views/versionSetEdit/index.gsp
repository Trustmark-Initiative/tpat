<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>${grailsApplication.config.tf.org.toolheader} | Development Repository</title>
        <style type="text/css"></style>

        <script type="text/javascript">
            var TD_DATA = null;
            var TIP_DATA = null;

            var DEFAULT_MAX = 5;

            $(document).ready(function(){
                setTimeout("loadTds(0, "+DEFAULT_MAX+")", 10);
                setTimeout("loadTips(0, "+DEFAULT_MAX+")", 25);

                setTimeout("loadKeywords()", 100);
            });

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

            function confirmRelease() {
                return confirm("Are you sure?  Once you do this, you will no longer be able to edit this content.");
            }

            function confirmWipeContent()  {
                return confirm("Are you sure?  Once you remove all content, both your production and development repositories will be empty.");
            }

            function confirmResetDevelopment()  {
                return confirm("Are you sure?  Once you reset development, all differences between development and production will be lost.");
            }
        </script>

    </head>

    <body>
        <div id="page-body" role="main">
            <div>
                <div class="row" style="margin-top: 2em;">
                    <div class="col-md-5">
                        <h1 style="margin: 0; padding: 0;">
                            Development Repository
                        </h1>
                    </div>
                    <div class="col-md-7" style="text-align: left;">
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-5"></div>
                    <div class="col-md-7">
                    </div>
                </div>
                </div>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success" style="margin-top: 3em;">${raw(flash.message)}</div>
            </g:if>
            <g:if test="${flash.error}">
                <div class="alert alert-danger" style="margin-top: 3em;">${raw(flash.error)}</div>
            </g:if>

            <div class="row">
                <div class="col-md-6">
                    <h4>Previously Uploaded Files</h4>
                    <table class="table table-condensed table-bordered table-striped">
                        <g:if test="${uploadedFiles?.size() > 0}">
                            <g:each in="${uploadedFiles}" var="up">
                                <tr>
                                    <td>
                                        <g:link controller="binary" action="view" id="${up.artifact.id}">
                                            <span class="glyphicon glyphicon-download"></span>
                                        </g:link>
                                    </td><td>
                                        ${up.artifact.originalFilename}
                                    </td>
                                    <td style="text-align: center;">
                                        <g:if test="${up.processed}">
                                            <span class="glyphicon glyphicon-ok"></span>
                                        </g:if>
                                        <g:else>
                                            <span class="glyphicon glyphicon-remove"></span>
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:formatDate date="${up.dateCreated}" format="yyyy-MM-dd HH:mm" />
                                    </td>
                                </tr>
                            </g:each>
                        </g:if>
                        <g:else>
                            <tr>
                                <td><em>There are no uploads</em></td>
                            </tr>
                        </g:else>
                    </table>
                </div>

                <div class="col-md-6">
                    <div id="fileUploadName">
                        <a href="#" id="fileUploadButton1" class="btn btn-default" style="font-size:12px">
                            <span class="glyphicon glyphicon-upload"></span>
                            Import New Content
                        </a>
                        <a href="#" class="btn btn-primary disabled" id="processFileUploadButton" style="font-size:12px">
                            <span class="glyphicon glyphicon-cog"></span>
                            Process
                        </a>
                        <span id="fileName1">Select a File...</span>
                        <div id="fileUploadStatus1"></div>
                    </div>
                    <input type="hidden" name="binaryId1" id="binaryId1" value="-1" /><br>
                    <g:link controller="versionSet" action="resetDevelopment" id="${versionSet.name}" class="btn btn-primary" style="font-size:12px" onclick="return confirmResetDevelopment();">
                        Reset Development to Production
                    </g:link>
                    <g:link controller="versionSet" action="moveToProduction" id="${versionSet.name}" class="btn btn-primary" style="font-size:12px" onclick="return confirmRelease();">
                        Publish Development to Production
                    </g:link>
                    <g:link controller="versionSet" action="removeProduction" id="${versionSet.name}" class="btn btn-danger" style="font-size:12px" onclick="return confirmWipeContent();">
                        Wipe All Content
                    </g:link>
                </div>
            </div>

            <!-- TDs and TIPs -->
            <div style="margin-top: 3em;" class="row">
                <div class="col-md-6">
                    <div class="row" style="margin-bottom: 0.5em;">
                        <div class="col-md-7">
                            <h3 style="margin: 0em;">Trustmark Definitions <small>(<span id="tdCount">-1</span>)</small></h3>
                        </div>
                    </div>
                    <div id="tdContainer">
                        <asset:image src="spinner.gif" /> Loading...
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="row" style="margin-bottom: 0.5em;">
                        <div class="col-md-8">
                            <h3 style="margin: 0em;">Trust Interoperability Profiles <small>(<span id="tipCount">-1</span>)</small></h3>
                        </div>
                    </div>
                    <div id="tipContainer">
                        <asset:image src="spinner.gif" /> Loading...
                    </div>
                </div>

            </div>

            <!-- log entries -->
%{--
            <div style="margin-top: 3em;" class="row">
                <div class="col-md-12">
                    <h3 style="margin: 0;">Log Entries <small>(${tmf.host.VersionSetLogEntry.countByVersionSet(versionSet)})</small></h3>
                    <table class="table table-condensed table-striped table-bordered">
                        <tbody>
                        <g:if test="${logEntries?.size() > 0}">
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
--}%
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
                        These TIPs show up on the "Primary TIPs" page and are intended to give users a high-level overview of the TIP and TD artifacts hosted in this version set.
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
        </div>

    <script type="text/javascript">

        /**
         * Called by pluploader template code after file added (because we specified it on the template inclusion.)
         */
        function enableProcessButton(up){
            var url = '${createLink(controller: 'versionSetEdit', action: 'processFileUpload', id: '__ID__', params: ['versionSetName': versionSet.name])}';
            url = url.replace('__ID__', $('#binaryId1').val());
            console.log("URL: "+url);
            $('#processFileUploadButton').attr('href', url);
            $('#processFileUploadButton').removeClass('disabled');

            var assignUrl = '${createLink(controller:'versionSetEdit', action: 'assignFile', id:'__ID__', params: ['versionSetName': versionSet.name])}';
            assignUrl = assignUrl.replace('__ID__', $('#binaryId1').val());
            $.ajax({
                url: assignUrl,
                method: 'POST',
                dataType: 'json',
                data: {
                    timestamp: new Date().getTime()
                },
                error: function(){
                    console.log("An unexpected error occurred trying to assign the upload to this version set.");
                },
                success: function(){
                    console.log("Successfully assigned.");
                }
            })

        }//end setUploadedFilename()

    </script>


    <tmpl:/templates/pluploadJavascript
            pluploadCounter="1"
            uploadCompleteCallback="enableProcessButton"
            context="Upload Artifacts to VersionSet ${versionSet.name}" />

    </body>
</html>
