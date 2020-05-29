<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>

        <title>Secured Page Test</title>

        <style type="text/css">

        </style>

    </head>
    <body>
        <div id="page-body" role="main">
            <div style="margin-top: 3em;" class="row">
                <div class="col-md-6">
                    <h2>Secure Test</h2>
                    <div class="description">
                        This page assures that you have logged in.  You're account is:
                        <div>
                            <ul>
                                <li>Username: <sec:username/></li>
                                <li>Name: ${user?.name ?: 'unknown'}</li>
                                <li>Roles: <sec:loggedInUserInfo field="authorities" /></li>
                            </ul>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">

                </div>
            </div>
        </div>
    </body>
</html>
