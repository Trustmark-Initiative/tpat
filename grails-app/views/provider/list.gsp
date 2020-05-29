<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Provider List</title>
        <style type="text/css">


        </style>

    </head>

    <body>
        <div id="page-body" role="main">
            <div>
                <h1>Providers <small>(${providerCount} Total)</small></h1>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success" style="margin-top: 3em;">
                    ${raw(flash.message)}
                </div>
            </g:if>

            <div style="margin-top: 3em;">

                <g:if test="${providerCount > 0 && providers.size() > 0}">
                    <div class="tableContainer">
                        <table class="table table-condensed table-striped table-bordered">
                            <thead>
                                <tr>
                                    <g:sortableColumn property="name" title="Name" />
                                    <g:sortableColumn property="uri" title="URL" />
                                    <g:sortableColumn property="email" title="Email" />
                                    <g:sortableColumn property="telephone" title="Telephone" />
                                </tr>
                            </thead>
                            <tbody>
                                <g:each var="provider" in="${providers}">
                                    <tr>
                                        <td>
                                            <g:link action="show" id="${provider.name}">
                                                ${provider.name}
                                            </g:link>
                                        </td>
                                        <td>
                                            ${provider.uri}
                                        </td>
                                        <td>
                                            ${provider.email}
                                        </td>
                                        <td>
                                            ${provider.telephone ?: '--'}
                                        </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                    </div>
                </g:if><g:else>
                    <div class="alert alert-warning" style="margin-top: 4em; margin-bottom: 4em;">
                        There are no providers to display.
                    </div>
                </g:else>

                <div class="row">
                    <div class="col-md-5">
                        <sec:ifAnyGranted roles="ROLE_ORG_ADMIN">
                            <g:link action="create" class="btn btn-primary">Create Provider</g:link>
                            <g:if test="${grails.util.Environment.current == grails.util.Environment.DEVELOPMENT}">
                                <g:link action="stuffDatabase" class="btn btn-warning" onclick="return confirm('Really create data?');">Stuff Database</g:link>
                                <g:link action="clearDatabase" class="btn btn-danger" onclick="return confirm('Really delete all provider data?');">Clear Database</g:link>
                            </g:if>
                        </sec:ifAnyGranted>
                    </div>
                    <div class="col-md-7" style="text-align: right;">
                        <page:paginate total="${providerCount}" />
                    </div>
                </div>

            </div>

        </div>
    </body>
</html>
