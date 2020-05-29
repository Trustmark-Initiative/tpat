<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Version Set List</title>
        <style type="text/css"></style>

    </head>

    <body>
        <div id="page-body" role="main">
            <div>
<!--                <h1>Version Sets <small>(${versionSetCount} Total)</small></h1>  -->
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success" style="margin-top: 3em;">
                    ${raw(flash.message)}
                </div>
            </g:if>

            <div style="margin-top: 3em;">

                <g:if test="${versionSetCount > 0 && versionSets.size() > 0}">
                    <div class="tableContainer">
                        <table class="table table-condensed table-striped table-bordered">
                            <thead>
                                <tr>
                                    <th colspan="2">Status</th>
                                    <g:sortableColumn property="name" title="Name" />
                                    <g:sortableColumn property="dateCreated" title="Date Created" />
                                </tr>
                            </thead>
                            <tbody>
                                <g:each var="vs" in="${versionSets}">
                                    <tr>
                                        <td style="width: 10%">
                                            <g:if test="${vs.production}">
                                                <span class="label label-danger" style="display: block">PRODUCTION</span>
                                            </g:if>
                                            <g:elseif test="${vs.releasedDate != null}">
                                                <span class="label label-warning" style="display: block">LEGACY</span>
                                            </g:elseif>
                                            <g:else>
                                                <span class="label label-primary" style="display: block">DEVELOPMENT</span>
                                            </g:else>
                                        </td><td style="width: 7%">
                                            <g:if test="${vs.editable}">
                                                <span class="label label-success" style="display: block">
                                                    <span class="glyphicon glyphicon-ok"></span> EDIT
                                                </span>
                                            </g:if><g:else>
                                                <span class="label label-danger" style="display: block">
                                                    <span class="glyphicon glyphicon-remove "></span> NO EDIT
                                                </span>
                                            </g:else>
                                        </td>
                                        <td>
                                            <g:if test="${vs.lockedBy != null}">
                                                <span class="glyphicon glyphicon-lock" title="Locked by user ${vs.lockedBy.username}"></span>
                                            </g:if>
                                            <g:link action="show" id="${vs.name}">
                                                ${vs.name}
                                            </g:link>
                                        </td>
                                        <td>
                                            <g:formatDate date="${vs.dateCreated}" />
                                        </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                    </div>
                </g:if><g:else>
                    <div class="alert alert-warning" style="margin-top: 4em; margin-bottom: 4em;">
                        There are no version sets to display.
                    </div>
                </g:else>

                <div class="row">
                    <div class="col-md-4">
                        <g:if test="${canCreateVersionSet}">
                            <g:link action="create" class="btn btn-primary">Create Version Set</g:link>
                        </g:if><g:else>
                            <span title="This operation is disabled, because your latest version set is not production.">
                                <a href="#" class="btn btn-primary disabled">Create Version Set</a>
                            </span>
                        </g:else>

                    </div>
                    <div class="col-md-8" style="text-align: right;">
                        <page:paginate total="${versionSetCount}" />
                    </div>
                </div>

            </div>

        </div>
    </body>
</html>
