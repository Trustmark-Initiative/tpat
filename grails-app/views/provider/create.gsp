<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Provider - Create</title>
        <style type="text/css">


        </style>

    </head>

    <body>
        <div id="page-body" role="main">
            <div>
                <h1>Create Organization</h1>
                <p class="text-muted">
                    This page allows you to define a new TDO/TIP and/or TP.
                    Providers are those organizations in the Trustmark Framework which are capable of granting new Trustmarks.
                </p>
            </div>

            <g:hasErrors bean="${command}">
                <div class="alert alert-danger" style="margin-top: 3em;">
                    <ul class="errors" role="alert">
                        <g:eachError bean="${command}" var="error">
                            <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>>
                                <g:message error="${error}"/>
                            </li>
                        </g:eachError>
                    </ul>
                </div>
            </g:hasErrors>


            <div style="margin-top: 3em;">
                <g:form action="save" method="POST" class="form form-horizontal">

                    <div class="form-group ${hasErrors(bean: command, field: 'name', 'has-error')}">
                        <label for="name" class="col-sm-2 control-label">
                            Name <span class="glyphicon glyphicon-star"></span>
                        </label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="name" name="name" placeholder="Name of the Organization" value="${command?.name ?: ''}" />
                        </div>
                    </div>

                    <div class="form-group ${hasErrors(bean: command, field: 'uri', 'has-error')}">
                        <label for="uri" class="col-sm-2 control-label">
                            URL <span class="glyphicon glyphicon-star"></span>
                        </label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="uri" name="uri" placeholder="https://..." value="${command?.uri ?: ''}" />
                        </div>
                    </div>

                    <div class="form-group ${hasErrors(bean: command, field: 'responder', 'has-error')}">
                        <label for="responder" class="col-sm-2 control-label">
                            Contact Name
                        </label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="responder" name="responder" placeholder="Name of a Contact Person" value="${command?.responder ?: ''}" />
                        </div>
                    </div>

                    <div class="form-group ${hasErrors(bean: command, field: 'email', 'has-error')}">
                        <label for="email" class="col-sm-2 control-label">
                            Email
                        </label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="email" name="email" placeholder="user@domain.ext" value="${command?.email ?: ''}" />
                        </div>
                    </div>

                    <div class="form-group ${hasErrors(bean: command, field: 'telephone', 'has-error')}">
                        <label for="telephone" class="col-sm-2 control-label">
                            Telephone
                        </label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="telephone" name="telephone" placeholder="xxx-xxx-xxxx" value="${command?.telephone ?: ''}" />
                        </div>
                    </div>

                    <div class="form-group ${hasErrors(bean: command, field: 'mailingAddress', 'has-error')}">
                        <label for="mailingAddress" class="col-sm-2 control-label">
                            Mailing Address
                        </label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="mailingAddress" name="mailingAddress" placeholder="PO Box 123..." value="${command?.mailingAddress ?: ''}" />
                        </div>
                    </div>

                    <div class="form-group ${hasErrors(bean: command, field: 'notes', 'has-error')}">
                        <label for="notes" class="col-sm-2 control-label">
                            Notes
                        </label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="notes" name="notes" placeholder="Any valuable information here..." value="${command?.notes ?: ''}" />
                        </div>
                    </div>

                    <div class="form-group" style="margin-top: 3em;">
                        <div class="col-sm-12" style="text-align: center;">
                            <g:link action="list" class="btn btn-primary">&laquo; Back to List</g:link>
                            <button type="submit" class="btn btn-primary">Save</button>
                        </div>
                    </div>

                </g:form>
            </div>

        </div>
    </body>
</html>
