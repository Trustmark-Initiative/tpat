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
                Applying Changes from ${upload.originalFilename}
%{--
                <small>(For <g:link action="index" id="${versionSet.name}">${versionSet.name}</g:link>)</small>
--}%
                <small>(For <g:link action="index" id="${versionSet.name}">Development</g:link>)</small>
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
        updateStatus();
    })

    /**
     * Calls the versionSetEdit/processStatus method remotely, gets the JSON, and stores it locally.  Calls the loadActionSummary() method.
     */
    function updateStatus() {
        $.ajax({
            url: '${createLink(controller: 'versionSetEdit', action: 'applyChangesStatus', id: upload.id, params: [versionSetName: versionSet.name])}',
            method: 'GET',
            dataType: 'json',
            data: {
                timestamp: new Date().getTime(),
                format: "JSON"
            },
            success: function(data, textStatus, jqXHR){
                renderProcessingStatus(data);
                if( isFinishedProcessing(data) ) {
                    console.log("Process completed successfully.");
                    $('#processStatusContainer').html('<div class="alert alert-success" style="margin-top: 2em;">'+data.message+'</div>'+
                        '<div style="margin-top: 2em;">\n'+
                            '<g:link action="index" id="${versionSet.name}" class="btn btn-primary">Return to Development</g:link>'+
                        '</div>'
                    );
                }else if( data && data.hasError ){
                    console.log("Process had an error: "+data.message);
                }else{
                    setTimeout("updateStatus()", 1000);
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
            console.log("LAST PHASE: "+JSON.stringify(lastPhase));
            return !lastPhase.complete && lastPhase.active;
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
        }else {
            var phaseInfo = response.phaseJson;
            if( phaseInfo != null){
                var phaseList = JSON.parse(phaseInfo);
                html += '<div style="margin-top: 1em; margin-bottom: 1em;">';
                var i = phaseList.length;
                phaseList.forEach(phase => {
                    if (phase.active) {
                        html += '<span class="label label-info">' + phase.displayName + '</span>&nbsp;';
                    } else if (phase.complete) {
                        html += '<span class="label label-success">' + phase.displayName + '</span>&nbsp;';
                    } else {
                        html += '<span class="label label-default">' + phase.displayName + '</span>&nbsp;';
                    }
                    if (--i > 0) {
                        html += '<span class="glyphicon glyphicon-arrow-right"></span>&nbsp;'
                    }
                });
                html += '</div>\n';

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
        }


        $('#processStatusContainer').html(html);
    }//end renderProcessingStatus()

</script>

</body>
</html>
