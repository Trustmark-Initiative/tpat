<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>

        <title>${grailsApplication.config.tf.org.toolheader} | System Variables</title>

        <style type="text/css">
        </style>

    </head>
    <body>
        <div id="page-body" role="main">
            <h1>System Variables</h1>
            <div class="text-muted">On this page, you can view and modify the values for any system variables.</div>

            <g:if test="${flash.message}">
                <div class="alert alert-success" style="margin-top: 2em;">${flash.message}</div>
            </g:if>
            <g:if test="${flash.error}">
                <div class="alert alert-danger" style="margin-top: 2em;">${flash.error}</div>
            </g:if>

            <div style="margin-top: 2em;">
                <g:if test="${sysVars.size() > 0}">
                    <table class="table table-condensed table-bordered table-striped">
                        <g:each in="${sysVars}" var="var">
                            <tr>
                                <td style="width: 10%; text-align: center;">
<!--
                                    <a href="#" title="Edit this system variable.">
                                        <span class="glyphicon glyphicon-pencil"></span>
                                    </a>
                                    &nbsp;   -->
                                    <a href="${createLink(action:'delete', params: [varName: var.name])}" onclick="return confirm('Really delete this system variable?');" title="Delete this system variable.">
                                        <span class="glyphicon glyphicon-remove"></span>
                                    </a>
                                </td>
                                <td style="width: 10%;">
                                    ${var.name} <br/>
                                    <div style="font-size: 80%;">
                                    <g:formatDate date="${var.lastUpdated}" format="yyyy-MM-dd HH:mm" />
                                    </div>
                                </td>
                                <td style="width: auto;">
                                    ${var.fieldValue}
                                </td>
                            </tr>
                        </g:each>
                    </table>
                </g:if>
                <g:else>
                    <div class="alert alert-warning">There are no system variables defined in the system.</div>
                </g:else>
            </div>

        </div>

    <script type="text/javascript">

    </script>
    </body>
</html>
