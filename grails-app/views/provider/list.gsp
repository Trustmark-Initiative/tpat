<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>${grailsApplication.config.tf.org.toolheader} | Organizations</title>
        <style type="text/css">


        </style>

    </head>

    <body>
        <div id="page-body" role="main">
            <div>
                <h1>Organizations <small>(Total: ${providerCount})</small></h1>
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
                                    <g:sortableColumn property="name"       title="Name" />
                                    <g:sortableColumn property="uri"        title="URL" />
                                    <g:sortableColumn property="email"      title="Email" />
                                    <g:sortableColumn property="telephone"  title="Telephone" />
                                    <g:sortableColumn property="td"         title="TDO/TIP Issuer" />
                                    <g:sortableColumn property="tp"         title="Trustmark Provider" />
                                </tr>
                            </thead>
                            <tbody>
                                <g:each var="provider" in="${providers}">
                                    <tr>
                                        <td>
                                            <g:link action="show" id="${provider.name}">${provider.name}</g:link>
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
                                        <td>
                                            <g:link action="setTd" id="${provider.name}" elementId="setTd.${provider.name}" class="form-control-static">
                                                <input
                                                    type="radio"
                                                    onchange="window.location.href=document.getElementById('setTd.${provider.name}').href"
                                                    name="provider.td" value="td.${provider.name}" ${provider.td ? 'checked' : ''}/>&nbsp;${provider.td ? 'Yes': 'No'}
                                            </g:link>
                                        </td>
                                        <td>
                                            <g:link action="setTp" id="${provider.name}" elementId="setTp.${provider.name}" class="form-control-static">
                                                <input
                                                    type="checkbox"
                                                    onclick="window.location.assign(document.getElementById('setTp.${provider.name}').href)"
                                                    name="provider.tp.${provider.name}" value="provider.tp.${provider.name}" ${provider.tp ? 'checked' : ''}/>&nbsp;${provider.tp ? 'Yes': 'No'}
                                            </g:link>
                                        </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                    </div>
                </g:if><g:else>
                    <div class="alert alert-warning" style="margin-top: 4em; margin-bottom: 4em;">
                        There are no organizations to display.
                    </div>
                </g:else>
                <div class="row">
                    <div class="col-md-5">
                        <sec:authorize access="hasAuthority('tpat-admin')">
                            <g:link action="create" class="btn btn-primary">Add Organization</g:link>
                            <g:if test="${grails.util.Environment.current == grails.util.Environment.DEVELOPMENT}">
                                <g:link action="stuffDatabase" class="btn btn-warning" onclick="return confirm('Really create data?');">Stuff Database</g:link>
                                <g:link action="clearDatabase" class="btn btn-danger" onclick="return confirm('Really delete all provider data?');">Clear Database</g:link>
                            </g:if>
                        </sec:authorize>
                    </div>
                    <div class="col-md-7" style="text-align: right;">
                        <page:paginate total="${providerCount}" />
                    </div>
                </div>

            </div>

        </div>
    </body>
</html>
