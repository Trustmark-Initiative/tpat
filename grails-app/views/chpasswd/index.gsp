<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${grailsApplication.config.tf.org.toolheader} | Change Password</title>
    <style type="text/css"></style>

</head>
<body>
<div id="page-body" role="main">
    <div>
        <h1>Change Password</h1>
        <p class="text-muted">
            Configure the ${grailsApplication.config.tf.org.tooltitle} Tool.  You will need to logout and log back in to see your changes.
        </p>
    </div>

    <div id="processStatusContainer">
        <g:if test="${stats != null && stats.rc == 'success'}">
            <div class="alert alert-success" style="margin-top: 2em;">${stats.message}</div>
        </g:if>
        <g:if test="${stats != null && stats.rc == 'failure'}">
            <div class="alert alert-danger" style="margin-top: 2em;">${stats.message}</div>
        </g:if>
    </div>

    <div style="margin-top: 6em;">
            <div class="form-group">
                <div class="row">
                    <div class="col-md-4"><input id="origPswd" type="password" class="form-control" placeholder="Current Password" /></div>
                </div>
                <div class="row">
                    <div class="col-md-4"><input id="newPswd" type="password" class="form-control" placeholder="New Password" /></div>
                </div>
                <div class="row">
                    <div class="col-md-4"><input id="renewPswd" type="password" class="form-control" placeholder="Re-enter New Password" /></div>
                    <div class="col-md-2" style="text-align: center;">
                        <button onclick="updatePswd(document.getElementById('origPswd').value, document.getElementById('newPswd').value, document.getElementById('renewPswd').value)" type="add" class="btn btn-primary">Update</button>
                    </div>
                </div>
            </div>
    </div>

</div>
<script type="text/javascript">
    /**
     * Calls the chpasswd/adminPswd method remotely, gets the JSON, and displays success failure.
     */
    function updatePswd(origp, newp, renewp) {
        console.log('updatePswd -> '+origp +","+newp+","+renewp);
        $.ajax({
            url: '${createLink(controller: 'chpasswd', action: 'adminPswd')}',
            method: 'POST',
            dataType: 'json',
            data: {
                origPswd: origp,
                newPswd: newp,
                renewPswd: renewp,
                timestamp: new Date().getTime(),
                format: "JSON"
            },
            success: function(data, textStatus, jqXHR) {
                console.log("Password updated");
                if(data.rc == 'success')  {
                    $('#processStatusContainer').html('<div class="alert alert-success" style="margin-top: 2em;">'+data.message+'</div>');
                }  else  {
                    $('#processStatusContainer').html('<div class="alert alert-danger" style="margin-top: 2em;">'+data.message+'</div>');
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.log("Error updating administrator password!");
            }
        })

    }
</script>
</body>
</html>
