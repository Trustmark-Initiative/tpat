<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>

        <title>Download All</title>

        <style type="text/css">
        </style>

    </head>
    <body>
        <div id="page-body" role="main">
            <h1>Download All</h1>
            <div class="text-muted">On this page, you can download all artifacts from a VersionSet.</div>

            <div style="margin-top: 2em;">
                <g:if test="${versionSets.size() > 0}">
                    <table class="table table-condensed table-bordered table-striped">
                        <g:each in="${versionSets}" var="vs">
                            <tr>
                                <td style="width: 10%; text-align: center;">
                                    <a href="javascript:buildZip('${vs.name}')" title="Start Building Zip...">
                                        <span class="glyphicon glyphicon-cog"></span>
                                    </a>
                                </td>
                                <td style="width: auto;">
                                    <div class="row">
                                        <div class="col-md-2">
                                            ${vs.name}
                                        </div>
                                        <div class="col-md-10">
                                            <div id="${vs.name}_downloadAvailableStatus">
                                                <em>There is no download available.</em>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-12" id="${vs.name}_STATUS">
                                            <asset:image src="spinner.gif" /> Loading Status...
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            <script type="text/javascript">
                                $(document).ready(function(){
                                    loadStatus('${vs.name}');
                                    getLatestDownload('${vs.name}');
                                });
                            </script>
                        </g:each>
                    </table>
                </g:if>
                <g:else>
                    <div class="alert alert-warning">There is no data to download, as no Version Sets are defined in the system.</div>
                </g:else>
            </div>

        </div>

    <script type="text/javascript">
        function getLatestDownload(vsName) {
            console.log("Getting latest download link for VS "+vsName+"...");
            var url = '${createLink(controller: 'downloadAll', action: 'getLatestDownload', id: '__REPLACE__')}';
            url = url.replace("__REPLACE__", vsName);
            $.ajax({
                url: url,
                method: 'GET',
                type: 'GET',
                data: {
                    timestamp: new Date().getTime()
                },
                dataType: 'json',
                success: function(data){
                    if( data && data.status && data.status === "SUCCESS" ){
                        $('#'+vsName+"_downloadAvailableStatus").html("<a href=\""+data.url+"\"><span class=\"glyphicon glyphicon-download\"></span> Download Zip ("+data.humanSize+"), Built "+data.createDate+"</a>");
                    }else{
                        if( data && data.message ) {
                            $('#' + vsName + "_downloadAvailableStatus").html("<em>" + data.message + "</em>");
                        }else{
                            $('#' + vsName + "_downloadAvailableStatus").html("<em>There is no download available.  The server sent an invalid response.</em>");
                        }
                    }
                },
                error: function(){
                    $('#'+vsName+"_STATUS").html('<div class="alert alert-danger" style="margin-top: 1em; margin-bottom: 0em;">Error loading status.</div>');
                }
            })
        }


        function buildZip(vsName){
            console.log("Starting build Zip for VS "+vsName+"...");
            var url = '${createLink(controller: 'downloadAll', action: 'build', id: '__REPLACE__')}';
            url = url.replace("__REPLACE__", vsName);
            $.ajax({
                url: url,
                method: 'POST',
                type: 'POST',
                data: {
                    timestamp: new Date().getTime()
                },
                dataType: 'json',
                success: function(data){
                    if( data && data.status && data.status === "SUCCESS" ){

                        setTimeout("loadStatus('"+vsName+"')", 100);
                    }
                },
                error: function(){
                    $('#'+vsName+"_STATUS").html('<div class="alert alert-danger" style="margin-top: 1em; margin-bottom: 0em;">Error loading status.</div>');
                }
            })
        }


        function loadStatus(vsName){
            var url = '${createLink(controller: 'downloadAll', action: 'monitor', id: '__REPLACE__')}';
            url = url.replace("__REPLACE__", vsName);
            $.ajax({
                url: url,
                method: 'GET',
                type: 'GET',
                data: {
                    timestamp: new Date().getTime()
                },
                dataType: 'json',
                success: function(data){
                    console.log("Status of "+vsName+": \n"+JSON.stringify(data));
                    var continuePoll = true;
                    if( data ){
                        if( data.status ){
                            if( data.status === "ERROR" ) {
                                $('#' + vsName + "_STATUS").html('<div class="alert alert-danger" style="margin-top: 1em; margin-bottom: 0em;">' + data.message + '</div>');
                                continuePoll = false;
                            }else if( data.status === "START" ){
                                $('#'+vsName+"_STATUS").html('<asset:image src="spinner.gif" /> ' + data.message);

                            }else if( data.status === "COMPLETE" ){
                                $('#'+vsName+"_STATUS").html('');
                                continuePoll = false;
                                getLatestDownload(vsName);
                            }else if( data.status === "COPYING" ){
                                $('#'+vsName+"_STATUS").html('<asset:image src="spinner.gif" /> '+data.message+"<div>"+buildProgressBar(data.percentage)+"</div>");

                            }else if( data.status === "COMPRESSING" ){
                                $('#'+vsName+"_STATUS").html('<asset:image src="spinner.gif" /> ' + data.message);
                            }else{
                                $('#'+vsName+"_STATUS").html('<div class="alert alert-info" style="margin-top: 1em; margin-bottom: 0em;">'+data.status+": "+data.message+'</div>');
                            }

                        }else{
                            $('#'+vsName+"_STATUS").html('');
                            continuePoll = false;
                        }
                    }else{
                        continuePoll = false;
                        $('#'+vsName+"_STATUS").html('<div class="alert alert-danger" style="margin-top: 1em; margin-bottom: 0em;">The server sent an unexpected response.</div>');
                    }
                    if( continuePoll )
                        setTimeout("loadStatus('"+vsName+"')", 1000);
                },
                error: function(){
                    $('#'+vsName+"_STATUS").html('<div class="alert alert-danger" style="margin-top: 1em; margin-bottom: 0em;">Error loading status.</div>');
                }
            })
        }

        function buildProgressBar(percent){
            var html = "";

            html += '<div class="progress">\n'+
                '<div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="45" aria-valuemin="0" aria-valuemax="100" style="width: '+percent+'%">\n'+
                '<span class="sr-only">'+percent+'% Complete</span>\n'+
                '</div>\n'+
                '</div>';

            return html;
        }

    </script>
    </body>
</html>
