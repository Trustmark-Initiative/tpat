<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${grailsApplication.config.tf.org.toolheader} | TD Defaults</title>
    <style type="text/css"></style>

</head>
<body>
<div id="page-body" role="main">
    <div>
        <h1>Change System TD Defaults</h1>
    </div>

    <div style="margin-top: 3em;">
        <h5><p>On this page, you can set default values for various fields that appear in your published trustmark definitions. Please note the
        following important caveats about setting and using these default values.<br><ul><li>
        Any changes you make to the default values on this page will be reflected in subsequently imported trustmark definitions, but
        changes on this page will NOT affect trustmark definitions imported prior to the change.</li>
        <li>The values shown on this page are system-wide defaults. You can override these default values for one or more specific trustmark
        definitions by specifying the desired values within an imported XLSX source file, or by using a properties file within an imported
        ZIP source archive.</li></ul></p></h5>
    </div>

    <div id="processStatusContainer">
        <g:if test="${stats != null && stats.rc == 'success'}">
            <div class="alert alert-success" style="margin-top: 2em;">${stats.message}</div>
        </g:if>
        <g:if test="${stats != null && stats.rc == 'failure'}">
            <div class="alert alert-danger" style="margin-top: 2em;">${stats.message}</div>
        </g:if>
    </div>

    <div style="margin-top: 3em;">
            <div class="form-group">
                    <g:each in="${sysvars}" var="sysvar">
                         <g:if test="${sysvar.tdRelated}">
                            <div class="row">
                                <div class="col-md-6"><h3>${sysvar.title}</h3></div>
                            </div>
                            <div class="row">
                                <div class="col-md-6">${sysvar.description}</div>
                            </div>
                            <div class="row">
                                <g:if test="${[ tmf.host.DefaultVariable.DEFAULT_ISSUANCE_CRITERIA ].contains(sysvar.name)}">
                                    <div class="col-md-6"><input type="text" id=${sysvar.name} class="form-control" value="${sysvar.fieldValue}" placeholder="New Value"></div>
                                </g:if>
                                <g:else>
                                    <div class="col-md-6"><textarea id=${sysvar.name} rows="6" cols="80" class="form-control" placeholder="New Value">${sysvar.fieldValue}</textarea></div>
                                </g:else>
                                <div class="col-md-2" style="text-align: center;">
                                    <button onclick="updateSysVar('${sysvar.name}', '${sysvar.title}', document.getElementById('${sysvar.name}').value, ${sysvar.id});" type="add" class="btn btn-primary">Update</button>
                                </div>
                            </div><br>
                         </g:if>
                     </g:each>
            </div>
    </div>

</div>
<script type="text/javascript">

    /**
     * Calls the appearance/sysDefault method remotely, gets the JSON, and displays success failure.
     */
    function updateSysVar(sysname, systitle, sysvalue, sysid) {
        console.log('updateSysVar -> '+sysname + " -> "+ sysvalue +", "+sysid);
        $.ajax({
            url: '${createLink(controller: 'appearance', action: 'sysDefault')}',
            method: 'POST',
            dataType: 'json',
            data: {
                name: sysname,
                title: systitle,
                value: sysvalue,
                id: sysid,
                timestamp: new Date().getTime(),
                format: "JSON"
            },
            success: function(data, textStatus, jqXHR) {
                if(data.rc == 'success')  {
                    $('#processStatusContainer').html('<div class="alert alert-success" style="margin-top: 2em;">'+data.message+'</div>');
                    document.getElementById(sysname).value = sysvalue;
                }  else  {
                    $('#processStatusContainer').html('<div class="alert alert-danger" style="margin-top: 2em;">'+data.message+'</div>');
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.log("Error updating system variable!");
            }
        })

    }
</script>
</body>
</html>
