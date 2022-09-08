<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>${grailsApplication.config.tf.org.toolheader} | Manage Trust Interoperability Profiles</title>
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
                            Edit Trust Interoperability Profiles for ${versionSet.name} <small>(${total})</small>
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
                            <th style="width: auto;">Trust Interoperability Profiles</th>
                            <th style="width: 10%; text-align: center;">Formats</th>
                        </tr>
                    </thead>

                    <tbody>
                    <g:if test="${links?.size() > 0}">
                        <g:each in="${links}" var="link">
                            <g:set var="tip" value="${link.trustInteroperabilityProfile}" />
                            <tr>
                                <td style="text-align: center; width: 10%;">
                                    <g:if test="${link.copyOver}">
                                        <g:if test="${!tip.deprecated}">
                                            <a href="#">
                                                <span class="glyphicon glyphicon-warning-sign" title="Deprecate"></span>
                                            </a>
                                        </g:if>
                                    </g:if>
                                    <g:else>
                                        <!--
                                        <a href="${createLink(controller: 'versionSetEdit', action: 'simpleTipEditor', id: versionSet.name, params: [tipId: link.trustInteroperabilityProfile.id])}">
                                            <span class="glyphicon glyphicon-pencil" title="Edit Simply"></span>
                                        </a>
                                        &nbsp;
                                        <a href="${createLink(controller: 'versionSetEdit', action: 'editTrustInteroperabilityProfile', id: versionSet.name, params: [linkId: link.id])}">
                                            <span class="glyphicon glyphicon-edit" title="Edit"></span>
                                        </a>
                                        &nbsp;-->
                                        <a href="${createLink(controller: 'versionSetEdit', action: 'deleteTIP', id: versionSet.name, params: [linkId: link.id])}" onclick="return confirm('Really delete this TIP?');">
                                            <span class="glyphicon glyphicon-remove" title="Delete"></span>
                                        </a>
                                        &nbsp;
                                        <g:if test="${link.primaryTIP}">
                                            <a href="javascript:toggleTipPrimary(${link.id})">
                                                <span id="primaryToggleButton${link.id}" class="glyphicon glyphicon-eye-close" title="Remove Primary TIP"></span>
                                            </a>
                                        </g:if><g:else>
                                            <a href="javascript:toggleTipPrimary(${link.id})">
                                                <span id="primaryToggleButton${link.id}" class="glyphicon glyphicon-eye-open" title="Set Primary TIP"></span>
                                            </a>
                                        </g:else>

                                    </g:else>
                                </td>
                                <td style="width: auto;">
                                    <div>
                                        <div style="font-weight: bold;">
                                            <g:if test="${tip.deprecated}">
                                                <span class="glyphicon glyphicon-warning-sign" title="Deprecated" style="color: #833;"></span>
                                                &nbsp;
                                            </g:if>
                                            <g:else>
                                                <span class="glyphicon glyphicon-ok" title="Usable" style="color: #383;"></span>
                                                &nbsp;
                                            </g:else>
                                            ${tip.name}, v${tip.tipVersion}
                                            <div class="pull-right">
                                                <g:if test="${link.primaryTIP}">
                                                    <span id="primaryIndicatorSpan${link.id}" class="label label-primary">PRIMARY</span>
                                                </g:if><g:else>
                                                <span id="primaryIndicatorSpan${link.id}" class="label label-default">DEFAULT</span>
                                            </g:else>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="text-muted" style="font-size: 80%; margin-left: 2.5em; margin-top: 0;">
                                        <%= LinkHelper.linkifyText(tip.description) %>
                                    </div>
                                </td>
                                <td style="width: 10%; text-align: center;">
                                    <g:link controller="versionSet" action="showTrustInteroperabilityProfile" id="${versionSet.name}" params="[tipName: tip.name, tipVersion: tip.tipVersion, format: 'html']" title="Click to open the HTML format.">
                                        <span style="font-size: 180%;" class="glyphfiles glyphfiles-html"></span>
                                    </g:link>
                                    <g:link controller="versionSet" action="showTrustInteroperabilityProfile" id="${versionSet.name}" params="[tipName: tip.name, tipVersion: tip.tipVersion, format: 'xml']" title="Click to open the XML format.">
                                        <span style="font-size: 180%;" class="glyphfiles glyphfiles-xml"></span>
                                    </g:link>
                                    <g:link controller="versionSet" action="showTrustInteroperabilityProfile" id="${versionSet.name}" params="[tipName: tip.name, tipVersion: tip.tipVersion, format: 'json']" title="Click to open the JSON format.">
                                        <span style="font-size: 180%;" class="glyphfiles glyphfiles-json"></span>
                                    </g:link>
                                </td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="3">
                                <em>There are no trust interoperability profiles.</em>
                            </td>
                        </tr>
                    </g:else>
                    </tbody>

                </table>
                <div class="row">
                    <div class="col-md-6">
<!--
                        <div class="btn-group">
                            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                Create <span class="caret"></span>
                            </button>
                            <ul class="dropdown-menu">
                                <li>
                                    <a href="${createLink(controller:'versionSetEdit', action: 'simpleTipEditor', id: versionSet.name)}">
                                        Simple
                                    </a>
                                </li>
                                <li>
                                    <a href="${createLink(controller:'versionSetEdit', action: 'createTrustInteroperabilityProfile', id: versionSet.name)}">
                                        Complete
                                    </a>
                                </li>
                            </ul>
                        </div>
-->
                    </div>
                    <div class="col-md-6" style="text-align: right;">
                        <page:paginate total="${total}" params="[id: versionSet.name]" />
                    </div>
                </div>


            </div>
        </div>

        <script type="text/javascript">
            function toggleTipPrimary(linkId){
                $.ajax({
                    url: '${createLink(controller: 'versionSetEdit', action: 'toggleTIPPrimary', id: versionSet.name)}',
                    method: "POST",
                    type: "POST",
                    data: {
                        timestamp: new Date().getTime(),
                        format: 'json',
                        linkId: linkId
                    },
                    dataType: 'json',
                    success: function(response){
                        console.log("Successfully received response: "+JSON.stringify(response));
                        $('#primaryToggleButton'+linkId).removeClass('glyphicon-eye-close');
                        $('#primaryToggleButton'+linkId).removeClass('glyphicon-eye-open');
                        $('#primaryIndicatorSpan'+linkId).removeClass('label-default');
                        $('#primaryIndicatorSpan'+linkId).removeClass('label-primary');

                        if( response && response.primary ){
                            $('#primaryToggleButton'+linkId).addClass('glyphicon-eye-close');
                            $('#primaryIndicatorSpan'+linkId).addClass('label-primary');
                            $('#primaryIndicatorSpan'+linkId).html('PRIMARY');
                        }else if( response && !response.primary ){
                            $('#primaryToggleButton'+linkId).addClass('glyphicon-eye-open');
                            $('#primaryIndicatorSpan'+linkId).addClass('label-default');
                            $('#primaryIndicatorSpan'+linkId).html('DEFAULT');
                        }

                        // TODO
                    },
                    error: function(){
                        alert("An unexpected error occurred.  Please refresh the page and try your request again, or contact support.");
                    }
                })
            }
        </script>

    </body>
</html>
