<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Version Set - Create</title>
        <style type="text/css">


        </style>

    </head>

    <body>
        <div id="page-body" role="main">
            <div>
                <h1>Create Version Set</h1>
                <p class="text-muted">
                    Declare the name of a version set to store trustmark definitions and trust interoperability profiles.
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
                            <input type="text" class="form-control" id="name" name="name" placeholder="Name" value="${command?.name ?: ''}" />
                        </div>
                    </div>

                    <div class="form-group" style="margin-top: 3em;">
                        <div class="col-sm-12" style="text-align: center;">
                            <button type="submit" class="btn btn-primary">Save</button>
                        </div>
                    </div>

                </g:form>
            </div>

        </div>
    </body>
</html>
