<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>${grailsApplication.config.tf.org.toolheader} | 500 Internal Server Error</title>

        <asset:stylesheet src="errors.css" />
    </head>
    <body>
        <div id="page-body" role="main">
            <div>
                <g:if test="${Throwable.isInstance(exception)}">
                    <g:renderException exception="${exception}" />
                </g:if>
                <g:elseif test="${request.getAttribute('javax.servlet.error.exception')}">
                    <g:renderException exception="${request.getAttribute('javax.servlet.error.exception')}" />
                </g:elseif>
                <g:else>
                    <h1>500: Internal Server Error</h1>
                    <div>
                        The server has encountered an unexpected error.  This can be due to many factors, including an invalid
                        submission.  Please check your request and try again, and if the problem persists contact support.
                    </div>
                    <ul class="errors">
                        <li>An error has occurred</li>
                        <li>Exception: ${exception}</li>
                        <li>Message: ${message}</li>
                        <li>Path: ${path}</li>
                    </ul>
                </g:else>
            </div>
            <div>
                <a href="${createLink(uri: '/')}" class="btn btn-primary">Go Back Home</a>
            </div>
        </div>
    </body>
</html>
