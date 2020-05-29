<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Version Set Manage Trustmark Definitions</title>
        <style type="text/css">

        </style>


        <script type="text/javascript">

            $(document).ready(function(){

            });


        </script>

    </head>

    <body>
        <div id="page-body" role="main">
            <div>
                <div class="row" style="margin-top: 2em;">
                    <div class="col-md-10">
                        <h1 style="margin: 0; padding: 0;">
                            Edit Trustmark Definitions for ${versionSet.name} <small>(${total})</small>
                        </h1>
                    </div>
                    <div class="col-md-2" style="text-align: right;">
                        <g:link controller="versionSetEdit" action="index" id="${versionSet.name}" class="btn btn-default">
                            &laquo; Back to Edit
                        </g:link>
                    </div>
                </div>

                <g:if test='${flash.message}'>
                    <div style="margin-top: 1em; margin-bottom: 1em;" class="alert alert-success">${flash.message}</div>
                </g:if>
                <g:if test='${flash.error}'>
                    <div style="margin-top: 1em; margin-bottom: 1em;" class="alert alert-danger">${flash.error}</div>
                </g:if>

                <table class="table table-striped table-bordered table-condensed">
                    <thead>
                        <tr>
                            <th style="text-align: center; width: 10%;">Actions</th>
                            <th style="width: auto;">Trustmark Definition</th>
                            <th style="width: 10%; text-align: center;">Formats</th>
                        </tr>
                    </thead>

                    <tbody>
                    <g:if test="${links?.size() > 0}">
                        <g:each in="${links}" var="link">
                            <g:set var="td" value="${link.trustmarkDefinition}" />
                            <tr>
                                <td style="text-align: center; width: 10%;">
                                    <g:if test="${link.copyOver}">
                                        <g:if test="${!td.deprecated}">
                                            <a href="#">
                                                <span class="glyphicon glyphicon-warning-sign" title="Deprecate"></span>
                                            </a>
                                        </g:if>
                                    </g:if>
                                    <g:else>
                                        <!--
                                        <a href="${createLink(controller: 'versionSetEdit', action: 'editTrustmarkDefinition', id: versionSet.name, params: [linkId: link.id])}">
                                            <span class="glyphicon glyphicon-edit" title="Edit"></span>
                                        </a>
                                        -->
                                        <a href="${createLink(controller: 'versionSetEdit', action: 'deleteTrustmarkDefinition', id: versionSet.name, params: [linkId: link.id])}">
                                            <span class="glyphicon glyphicon-remove" title="Delete"></span>
                                        </a>
                                    </g:else>
                                </td>
                                <td style="width: auto;">
                                    <div>
                                        <g:if test="${td.deprecated}">
                                            <span class="glyphicon glyphicon-warning-sign" title="Deprecated" style="color: #833;"></span>
                                            &nbsp;
                                        </g:if>
                                        <g:else>
                                            <span class="glyphicon glyphicon-ok" title="Usable" style="color: #383;"></span>
                                            &nbsp;
                                        </g:else>
                                        ${td.name}, v${td.tdVersion}
                                    </div>
                                    <div class="text-muted" style="font-size: 80%;">
                                        ${raw(td.description)}
                                    </div>
                                </td>
                                <td style="width: 10%; text-align: center;">
                                    <a href="${createLink(controller: 'versionSet', action: 'showTrustmarkDefinition', id: versionSet.name, params: [tdName: td.name, tdVersion: td.tdVersion, format: 'html'])}" title="Click to open the HTML format.">
                                        <span style="font-size: 180%;" class="glyphfiles glyphfiles-html"></span>
                                    </a>
                                    <a href="${createLink(controller: 'versionSet', action: 'showTrustmarkDefinition', id: versionSet.name, params: [tdName: td.name, tdVersion: td.tdVersion, format: 'xml'])}" title="Click to open the XML format.">
                                        <span style="font-size: 180%;" class="glyphfiles glyphfiles-xml"></span>
                                    </a>
                                    <a href="${createLink(controller: 'versionSet', action: 'showTrustmarkDefinition', id: versionSet.name, params: [tdName: td.name, tdVersion: td.tdVersion, format: 'json'])}" title="Click to open the JSON format.">
                                        <span style="font-size: 180%;" class="glyphfiles glyphfiles-json"></span>
                                    </a>

                                </td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="3">
                                <em>There are no trustmark definitions.</em>
                            </td>
                        </tr>
                    </g:else>
                    </tbody>

                </table>
                <div class="row">
                    <div class="col-md-6">
<!--
                        <div class="btn-group">
                            <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                Create <span class="caret"></span>
                            </button>
                            <ul class="dropdown-menu">
                                <li>
                                    <a href="${createLink(controller:'versionSetEdit', action: 'simpleTdEditor', id: versionSet.name)}">
                                        Simple
                                    </a>
                                </li>
                                <li>
                                    <a href="${createLink(controller:'versionSetEdit', action: 'createTrustmarkDefinitionComplete', id: versionSet.name)}">
                                        Complete
                                    </a>
                                </li>
                            </ul>
                        </div>
-->                    </div>
                    <div class="col-md-6" style="text-align: right;">
                        <page:paginate total="${total}" params="[id: versionSet.name]" />
                    </div>
                </div>


            </div>
        </div>

    </body>
</html>
