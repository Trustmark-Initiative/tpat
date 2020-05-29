<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Version Set Edit - Process File Upload</title>
    <style type="text/css">


    </style>


</head>

<body>
<div id="page-body" role="main">
    <div class="row" style="margin-top: 3em;">
        <div class="col-md-12">
            <h1 style="margin: 0; padding: 0;">
                Process Upload ${upload.originalFilename}
%{--
                <small>(For <g:link action="index" id="${versionSet.name}">${versionSet.name}</g:link>)</small>
--}%
            </h1>
        </div>
    </div>
    <div id="processStatusContainer">
        <asset:image src="spinner.gif" /> Loading server data...
    </div>
</div>

<script type="text/javascript">

    $(document).ready(function(){
        console.log("Starting status update thread...")
        updateProcessStatus();
    })

    /**
     * Calls the versionSetEdit/processStatus method remotely, gets the JSON, and stores it locally.  Calls the loadActionSummary() method.
     */
    function updateProcessStatus() {
        $.ajax({
            url: '${createLink(controller: 'versionSetEdit', action: 'processStatus', params: [uploadId: upload.id])}',
            method: 'GET',
            dataType: 'json',
            data: {
                timestamp: new Date().getTime()
            },
            success: function(data, textStatus, jqXHR){
                console.log("Successfully received data: "+JSON.stringify(data));
                renderProcessingStatus(data);
                if( isFinishedProcessing(data) ){
                    console.log("Process completed successfully.");
                    window.location = '${createLink(controller: 'versionSetEdit', action: 'chooseActionSummary', id: upload.id, params: [versionSetName: versionSet.name])}';
                }else if( data && data.hasError ){
                    console.log("Process had an error: "+data.message);
                }else{
                    setTimeout("updateProcessStatus()", 1000);
                }
            },
            error: function(jqXHR, textStatus, errorThrown){
                console.log("Received error!");
            }
        })

    }

    function isFinishedProcessing(data){
        if( data && data.phaseJson ){
            var phaseInfo = JSON.parse(data.phaseJson);
            var lastPhase = phaseInfo[phaseInfo.length - 1];
            return lastPhase.active;
        }else{
            return false;
        }
    }

    /**
     * Builds the HTML and displays it in the processStatusContainer.
     */
    function renderProcessingStatus(response){
        var html = '';

        if( response.hasError ){
            html += '<div style="margin-top: 1em; margin-bottom: 1em;" class="alert alert-danger">';
            html += response.message;
            html += '</div>';
            html += '<g:link controller="versionSetEdit" action="index" id="${tmf.host.VersionSet.findByDevelopment(true).name}" class="btn btn-default">Back</g:link>';
        } else {
            var phaseInfo = JSON.parse(response.phaseJson);
            if( phaseInfo != null ) {
                html += '<div style="margin-top: 1em; margin-bottom: 1em;">';
                for (var i = 0; i < phaseInfo.length; i++) {
                    var phase = phaseInfo[i];
                    if (phase.active) {
                        html += '<span class="label label-info">' + phase.displayName + '</span>&nbsp;';
                    } else if (phase.complete) {
                        html += '<span class="label label-success">' + phase.displayName + '</span>&nbsp;';
                    } else {
                        html += '<span class="label label-default">' + phase.displayName + '</span>&nbsp;';
                    }
                    if (i < (phaseInfo.length - 1)) {
                        html += '<span class="glyphicon glyphicon-arrow-right"></span>&nbsp;'
                    }
                }
                html += '</div>\n';
            }


            html += '<div>';
            html += '<asset:image src="spinner.gif" /> &nbsp;';
            html += response.message;
            html += '</div>';

            if (response.percentage > -1) {
                html += '<div>';
                html += renderProgress(response.percentage);
                html += '</div>';
            }
        }

        $('#processStatusContainer').html(html);
    }//end renderProcessingStatus()

</script>

</body>
</html>
