<!doctype html>
<%@ page import="tmf.host.artifact_processing.*" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Version Set Edit - Choose Actions</title>
    <style type="text/css">


    </style>


</head>

<body>
<div id="page-body" role="main">
    <div class="row" style="margin-top: 3em;">
        <div class="col-md-9">
            <h1 style="margin: 0; padding: 0;">
                Choose Actions
%{--
                <small>(For <g:link action="index" id="${versionSet.name}">${versionSet.name}</g:link>)</small>
--}%
            </h1>
        </div>
        <div class="col-md-3" style="text-align: right;" id="topRightContainer">

        </div>
    </div>
    <div class="text-muted" style="font-size: 90%;">
        Uploaded File: <b>${upload.originalFilename}</b> (${org.apache.commons.io.FileUtils.byteCountToDisplaySize(upload.fileSize)})  <br />
        On this page, you specify the actions you would like to occur based
        on the file uploaded.  The system has populated actions that it thinks are optimal, you may override those
        selections here.
    </div>
    <g:if test='${flash.message}'>
        <div style="margin-top: 1em; margin-bottom: 1em;" class="alert alert-success">${flash.message}</div>
    </g:if>
    <g:if test='${flash.error}'>
        <div style="margin-top: 1em; margin-bottom: 1em;" class="alert alert-danger">${flash.error}</div>
    </g:if>
    <div id="processStatusContainer" style="margin-top: 2em;">

        <div>
            <g:if test="${mpd.getActionsByType(ActionType.ERROR).size() == 0}">
            <a href="javascript:applyActionsButtonClick()" class="btn btn-primary">Apply Actions &raquo;</a>
            <script type="text/javascript">
                function applyActionsButtonClick(){
                    $.ajax({
                        url: '${createLink(controller: 'versionSetEdit', action: 'applyChanges', id: upload.id, params: [versionSetName: versionSet.name])}',
                        method: "POST",
                        type: "POST",
                        data: {
                            timestamp: new Date().getTime(),
                            format: 'json'
                        },
                        dataType: 'json',
                        success: function(response){
                            console.log("Received response: "+JSON.stringify(response));
                            window.location.href = "${createLink(controller: 'versionSetEdit', action: 'applyChangesView', id: upload.id, params: [versionSetName: versionSet.name])}";
                        },
                        error: function(){
                            alert("An unexpected error occurred initiating the process.  Please refresh the page and try again.")
                        }
                    })
                }
            </script>
            </g:if>
            <g:else>
                <button class="btn btn-primary" disabled>Apply Actions &raquo;</button>
            </g:else>
            <g:link controller="versionSetEdit" action="cancelActionSummary" id="${upload.id}" class="btn btn-default">
                Cancel
            </g:link>
        </div>
        <div style="margin-top: 2em;">
            <!-- Nav tabs -->
            <ul class="nav nav-tabs" role="tablist">
                <g:each in="[ActionType.ERROR, ActionType.ADD, ActionType.OVERWRITE, ActionType.IGNORE]" var="actionType" status="actionTypeStatus">
                    <g:set var="actions" value="${mpd.getActionsByType(actionType)}" />
                    <g:if test="${actions.size() > 0 && request.getAttribute('foundActionWithData') == null}">
                        <li role="presentation" class="active"><a href="#${actionType}" aria-controls="${actionType}" role="tab" data-toggle="tab">${actionType} (${actions.size()})</a></li>
                        <% request.setAttribute("foundActionWithData", actionType); %>
                    </g:if>
                    <g:else>
                        <li role="presentation"><a href="#${actionType}" aria-controls="${actionType}" role="tab" data-toggle="tab">${actionType} (${actions.size()})</a></li>
                    </g:else>
                </g:each>
            </ul>

            <!-- Tab panes -->
            <div class="tab-content">
                <g:each in="[ActionType.ERROR, ActionType.ADD, ActionType.OVERWRITE, ActionType.IGNORE]" var="actionType" status="actionTypeStatus">
                    <g:set var="actions" value="${mpd.getActionsByType(actionType)}" />
                        <div role="tabpanel" class="tab-pane ${actionType == request.getAttribute("foundActionWithData") ? 'active' : ''}" id="${actionType}">

                            <g:if test="${actions.size() > 0}">
                                <div class="row" style="margin-top: 0.5em; margin-bottom: 0.5em;">
                                    <div class="col-md-6">
                                        <form class="form-inline">
                                            <div class="form-group">
                                                <label class="sr-only" for="${actionType}SearchBox">Search String</label>
                                                <input type="email" class="form-control" id="${actionType}SearchBox" placeholder="Search String">
                                            </div>
                                        </form>

                                    </div>
                                    <div class="col-md-6">
                                        <div class="pull-right">
                                            <a href="#" class="btn btn-default">Ignore All</a> &nbsp;
                                            <a href="#" class="btn btn-default">Hide TDs</a> &nbsp;
                                            <a href="#" class="btn btn-default">Hide TIPs</a>
                                        </div>
                                    </div>
                                </div>
                                <table class="table table-condensed table-bordered table-striped">
                                    <g:each in="${actions}" var="${action}">
                                        <tr>
                                            <td style="width: 10%; text-align: center;">
                                                <g:if test="${action.actionType == ActionType.IGNORE}">
                                                    <span class="glyphicon glyphicon-minus"></span>

                                                </g:if>
                                                <g:elseif test="${action.actionType == ActionType.ERROR}">
                                                    <span class="label label-danger">
                                                        <span class="glyphicon glyphicon-remove-sign"></span>
                                                        ERROR
                                                    </span>
                                                </g:elseif>
                                                <g:else>
                                                    <a href="${createLink(controller:'versionSetEdit', action: 'ignoreArtifactAction', id: action.uniqueId, params:[versionSetName: versionSet.name, uploadId: mpd.uploadId])}"
                                                       class="btn btn-default">
                                                        Ignore
                                                    </a>
                                                </g:else>
                                            </td>
                                            <td>
                                                <div>
                                                    <g:if test="${action.type == "TD"}">
                                                        <span class="glyphicon glyphicon-tag" title="Trustmark Definition"></span>
                                                    </g:if>
                                                    <g:else>
                                                        <span class="glyphicon glyphicon-th-list" title="Trust Interoperability Profile"></span>
                                                    </g:else>
                                                    ${action.artifact.name}, v${action.artifact.version}
                                                    <div class="pull-right">
                                                        <a href="${createLink(controller:'versionSetEdit', action: 'viewMemoryHtmlArtifact', id: action.uniqueId, params:[versionSetName: versionSet.name, uploadId: mpd.uploadId])}" target="_blank" title="Open the details in a new window">
                                                            (View HTML <span class="glyphicon glyphicon-new-window"></span>)
                                                        </a>
                                                    </div>
                                                </div>
                                                <g:if test="${action.actionType == ActionType.ERROR}">
                                                    <div class="" style="color: rgb(150, 0, 0); font-weight: bold;">
                                                        ${action.errorMessage}
                                                    </div>
                                                </g:if><g:else>
                                                    <div class="text-muted" style="font-size: 80%; padding-left: 1.5em;">
                                                        ${action.artifact.description}
                                                    </div>
                                                </g:else>
                                                <g:if test="${action.postActions != null && action.postActions.size() > 0}">
                                                    <div style="margin-left: 1.3em;">
                                                        <div>Post Actions(${action.postActions.size()}):</div>
                                                        <g:each in="${action.postActions}" var="postAction">
                                                            <div style="margin-left: 1em;">
                                                                ${postAction.actionType}
                                                                ${postAction.artifact.name}, v${postAction.artifact.version}
                                                            </div>
                                                        </g:each>
                                                    </div>
                                                </g:if>
                                            </td>
                                        </tr>
                                    </g:each>
                                </table>
                            </g:if>
                            <g:else>
                                <div class="alert alert-warning" style="margin-top: 1em;">
                                    The system didn't find anything for "${actionType}".
                                </div>
                            </g:else>

                        </div>
                </g:each>
            </div>

        </div>
    </div>
</div>
</body>
</html>