<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Base URL Not Matching Error</title>

        <asset:stylesheet src="errors.css" />

    </head>
    <body>
        <div id="page-body" role="main">
            <h1>Server Configuration Error: Base URL Mismatch</h1>
            <div class="text-muted">
                Your browser URL and server's configured base URL list to not match.  This will affect your ability to
                publish artifacts, since this server will attempt to publish artifacts at a base url location that this
                server does not represent.  Please update your tfam_config.properties file located at /WEB-INF/classess/tfam_config.properties
                so that it contains the current base URL[${currentBaseUrl}].
            </div>
            <div style="margin-top: 3em;">
                <dl class="error-details">
                    <dt>Current Base URL</dt>
                    <dd>${currentBaseUrl}</dd>

                    <dt>Configured Base URL List</dt>
                    <dd>${baseUrlList}</dd>

                </dl>
            </div>
        </div>
    </body>
</html>
