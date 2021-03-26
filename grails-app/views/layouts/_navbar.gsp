<%@ page import="tmf.host.VersionSetSelectingInterceptor" %>

<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" target="_self" href="${createLink(uri:'/')}">
                ${grailsApplication.config.tf.org.organization}
            </a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false" target="_self">Hosted Artifacts <span class="caret"></span></a>
                    <ul class="dropdown-menu">
                        <g:if test="${session[VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE] != null}">
                            <li><a target="_self" href="${createLink(controller:'search')}">Search</a></li>
                            <li><a target="_self" href="${createLink(controller:'trustmarkDefinition', action: 'list')}">All TDs</a></li>
                            <li><a target="_self" href="${createLink(controller:'tip', action: 'list')}">All TIPs</a></li>
                            <li><a target="_self" href="${createLink(controller:'keyword', action: 'list')}">Keywords</a></li>
                            <li><a target="_self" href="${createLink(controller:'tip', action: 'tipTree')}">Primary TIPs</a></li>
                            <li><a target="_self" href="${createLink(controller:'downloadAll')}">Download All</a></li>
                        </g:if><g:else>
                            <li><a target="_self" href="#"><em>None</em></a></li>
                        </g:else>
                    </ul>
                </li>
                <sec:ifLoggedIn>
                    <li class="dropdown">
                        <a target="_self" href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Repositories<span class="caret"></span></a>
                        <ul class="dropdown-menu">
                            <li><a target="_self" href="${createLink(controller:'index', params: [VERSION_SET_NAME: tmf.host.VersionSet.findByDevelopment(true)?.name])}">Development</a></li>
                            <g:if test="${tmf.host.VersionSet.findByProduction(true)?.name != null}">
                            <li><a target="_self" href="${createLink(controller:'index', params: [VERSION_SET_NAME: tmf.host.VersionSet.findByProduction(true)?.name])}">Production</a></li>
                            </g:if>
                        </ul>
                    </li>
                    <sec:ifAllGranted roles="ROLE_ADMIN">
                        <li class="dropdown">
                            <a target="_self" href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Administration <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li><a target="_self" href="${createLink(controller:'provider', action: 'list')}">Manage Organizations</a></li>
                                <li><a target="_self" href="${createLink(controller:'appearance', action: 'tddefaults')}">Change TD Defaults</a></li>
                                <li><a target="_self" href="${createLink(controller:'appearance', action: 'tipdefaults')}">Change TIP Defaults</a></li>
                                <li><a target="_self" href="${createLink(controller:'chpasswd', action: 'index')}">Change Password</a></li>
                                <li><a target="_self" href="${createLink(controller:'taxonomyTerm', action: 'index')}">Taxonomy</a></li>
                                <li><a target="_self" href="${createLink(controller:'systemVariable')}">System Variables</a></li>
                                <li><a target="_self" href="${createLink(controller: 'email', action: 'settings')}">Email</a></li>
                            </ul>
                        </li>
                    </sec:ifAllGranted>
                </sec:ifLoggedIn>
            %{--<li><a target="_self" href="#about">About</a></li>--}%
            %{--<li><a target="_self" href="#contact">Contact</a></li>--}%
            </ul>

            <ul class="nav navbar-nav navbar-right">
                <sec:ifLoggedIn>
                    <li>
                        <g:link controller="logout">Logout</g:link>
                    </li>
                </sec:ifLoggedIn>
                <sec:ifNotLoggedIn>
                    <li>
                        <g:link controller="login" action="auth">Login</g:link>
                    </li>
                </sec:ifNotLoggedIn>
            </ul>
        </div><!--/.nav-collapse -->
    </div>
</nav>
