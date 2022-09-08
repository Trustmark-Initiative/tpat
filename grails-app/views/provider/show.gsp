<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>${grailsApplication.config.tf.org.toolheader} | Organization : ${provider.name}</title>
        <style type="text/css">


        </style>

    </head>

    <body>
        <div id="page-body" role="main">
            <div>
                <h1>Organization Information: ${provider.name}</h1>
            </div>

            <table class='table table-condensed table-striped table-bordered'>
                <tr>
                    <td style='width: auto;'><b>Name</b></td>
                    <td style='width: auto;'>${provider.name}</td>
                </tr><tr>
                    <td style='width: auto;'><b>URL</b></td>
                    <td style='width: auto;'>${provider.uri}</td>
                </tr><tr>
                    <td style='width: auto;'><b>Email</b></td>
                    <td style='width: auto;'>${provider.email}</td>
                </tr>
                </tr><tr>
                    <td style='width: auto;'><b>Contact Name</b></td>
                    <td style='width: auto;'>${provider.responder}</td>
                </tr>
                </tr><tr>
                    <td style='width: auto;'><b>Telephone</b></td>
                    <td style='width: auto;'>${provider.telephone}</td>
                </tr>
                </tr><tr>
                    <td style='width: auto;'><b>Mailing Address</b></td>
                    <td style='width: auto;'>${provider.mailingAddress}</td>
                </tr>
                </tr><tr>
                    <td style='width: auto;'><b>TDO/TIP</b></td>
                    <td style='width: auto;'>${provider.td ? 'Yes': 'No'}</td>
                </tr>
                </tr><tr>
                    <td style='width: auto;'><b>TP</b></td>
                    <td style='width: auto;'>${provider.tp ? 'Yes': 'No'}</td>
                </tr>
            </table>
        </div>

        <div style="margin-top: 1em">
            <g:link action="list" class="btn btn-primary">&laquo; Back to List</g:link>
            <g:link action="edit" id="${provider.name}" class="btn btn-default">Edit</g:link>
            <g:link action="delete" id="${provider.name}" class="btn btn-danger" onclick="return confirm('Really delete?  This action cannot be reversed!')">Delete</g:link>
        </div>

    </body>
</html>
