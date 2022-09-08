<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${grailsApplication.config.tf.org.toolheader} | Creation in Progress</title>
    <style type="text/css"></style>

    <script type="text/javascript">
        $(document).ready(function(){
            setTimeout("updateVersionSetStatus()", 50);
        });

        function updateVersionSetStatus(){
            $.ajax({
                url: '${createLink(controller: 'versionSet', action: 'createVersionSetStatus', id: versionSet.name)}',
                dataType: 'json',
                method: 'GET',
                data: {
                    timestamp: new Date().getTime(),
                    format: 'json'
                },
                success: function(data){
                    console.log("Successfully loaded version set data: "+JSON.stringify(data));
                    renderStatus(data);
                    if( data && data.status === "SUCCESSFUL" ){
                        window.location = '${createLink(controller: 'versionSetEdit', action: 'index', id: versionSet.name)}';
                    }else{
                        setTimeout("updateVersionSetStatus()", 500);
                    }
                },
                error: function(){
                    console.log("ERROR Loading Trustmark Definitions!");
                }
            });
        }

        function renderStatus(data){
            var html = '';

            html += '<div>';
            html += '<asset:image src="spinner.gif" /> &nbsp;';
            html += data.message;
            html += '</div>';

            html += '<div>';
            html += renderProgress(data.percentage);
            html += '</div>';

            $('#resultData').html(html);
        }

    </script>

</head>

<body>
<div id="page-body" role="main">
    <div id="resultData">
        <asset:image src="spinner.gif" /> Loading the version set creation status...
    </div>
</div>

</body>
</html>
