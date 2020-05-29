<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Provider - ${provider.name}</title>
        <style type="text/css">


        </style>

    </head>

    <body>
        <div id="page-body" role="main">
            <div>
                <h1>Provider: ${provider.name}</h1>
            </div>

            <div style="margin-top: 1em">
                <h3>Provider Information</h3>
                <div>
                    Name: ${provider.name}
                </div>
                <div>
                    URL: ${provider.uri}
                </div>

                <div>
                    Responder: ${provider.responder}
                </div>

                <div>
                    Email: ${provider.email}
                </div>

                <div>
                    Telephone: ${provider.telephone}
                </div>

                <div>
                    Mailing Address: ${provider.mailingAddress}
                </div>
                <div>
                    Notes: ${provider.notes}
                </div>
            </div>
        </div>

        <div style="margin-top: 1em">
            <g:link action="list" class="btn btn-primary">&laquo; Back to List</g:link>
            <g:link action="edit" id="${provider.name}" class="btn btn-default">Edit</g:link>
            <g:link action="delete" id="${provider.name}" class="btn btn-danger" onclick="return confirm('Really delete?  This action cannot be reversed!')">Delete</g:link>
        </div>

    </body>
</html>
