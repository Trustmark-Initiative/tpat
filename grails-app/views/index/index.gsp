<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Home Page</title>
        <style type="text/css">
            .overallIndexLinkTable td {
                padding: 1em;
            }

            .overallIndexLinkTableButtonContainer {
                padding: 1em;
                font-size: 110%;
                font-weight: bold;
            }
        </style>
    </head>
    <body>
        <div id="page-body" role="main">

            <g:if test="${session.getAttribute(tmf.host.VersionSetSelectingInterceptor.VERSION_SET_NAME_ATTRIBUTE) == null}">
                <div class="alert alert-warning" style="margin-top: 3em;">
                    <b>No Data</b>
                    <div>
                        This system has no Trustmark Definition or Trust Interoperability Profile data to display.

                        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_ADMIN,ROLE_DEVELOPER">
                            <div style="margin-top: 1em;">
                                <g:if test="${tmf.host.VersionSet.count() == 0}">
                                    <g:link controller="versionSet" action="save" class="btn btn-primary">Get Started &raquo;</g:link>
                                </g:if>
                                <g:else>
                                <g:link controller="versionSetEdit" action="index" id="${tmf.host.VersionSet.findByDevelopment(true).name}" class="btn btn-primary">
                                        Load TIPs and TDs &raquo;
                                    </g:link>
                                </g:else>
                            </div>
                        </sec:ifAnyGranted>
                        <sec:ifNotLoggedIn>
                            <div style="margin-top: 1em;">
                                <g:link controller="login" class="btn btn-primary">Login to Create Data &raquo;</g:link>
                            </div>
                        </sec:ifNotLoggedIn>
                    </div>
                </div>
            </g:if>
            <g:else>
                <sec:ifLoggedIn>
                <g:if test="${versionSet.production}">
                    <div style="margin-top: 2em;" class="row">
                        <div class="col-md-6"><h1>Production Repository</h1></div>
                        <div class="col-md-6"></div>
                    </div>
                </g:if><g:else>
                    <div style="margin-top: 2em;" class="row">
                        <div class="col-md-6"><h1>Development Repository</h1>
                            <g:link controller="versionSetEdit" action="index" id="${tmf.host.VersionSet.findByDevelopment(true).name}" class="btn btn-primary">
                                Edit
                            </g:link>
                        </div>
                        <div class="col-md-6"></div>
                    </div>
                </g:else>
                </sec:ifLoggedIn>
                <div style="margin-top: 2em;" class="row">
                <div class="col-md-6">
                    <h2>Trustmark Definitions <small>(${tdCount ?: 0} Total)</small></h2>
                    <div class="description">
                    <p>Within the <em>trustmark framework</em>, a <em>trustmark definition</em> (TD) is a machine-readable artifact that defines a particular type of <em>trustmark</em>.</p>
                    <p>A TD includes normative conformance requirements that must be satisfied in order to qualify for the trustmark.
                    It also includes instructions that a <em>trustmark provider</em> must follow when performing an assessment to determine whether a prospective <em>trustmark recipient</em> qualifies.
                    If the prospective recipient qualifies for the trustmark, then the trustmark provider may issue the trustmark to the recipient.</p>
                    <p>There can be many different types of trustmarks, and each type of trustmark has its own published TD.
                    This system publishes TDs in three formats: HTML, XML, and JSON. Use the link below or the navigation menu above to browse the TDs that have been published on this system.</p>
                    </div>
                    <div style="margin-top: 2em;">
                        <a class="btn btn-default" href="${createLink(controller: 'trustmarkDefinition', action: 'list')}">View All TDs &raquo;</a>
                    </div>

                </div>
                <div class="col-md-6">
                    <h2>Trust Interoperability Profiles <small>(${tipCount ?: 0} Total)</small></h2>
                    <div class="description">
                        <p>Within the <em>trustmark framework</em>, a <em>trust interoperability profile</em> (TIP) is a machine-readable artifact that defines a trust policy.
                        A TIP can include zero or more references to <em>trustmark definitions</em>. A TIP can also include zero or more non-circular references to other TIPs.</p>
                        <p>This system publishes TIPs in three formats: HTML, XML, and JSON. Use the links below or the navigation menu above to browse the TIPs that have been published on this system.</p>
                    </div>
                    <div style="margin-top: 2em;">
                        <a class="btn btn-default" href="${createLink(controller: 'tip', action: 'list')}">View All TIPs &raquo;</a>
                        <a class="btn btn-default" href="${createLink(controller: 'tip', action: 'tipTree')}">View Primary TIPs &raquo;</a>
                    </div>

                </div>
            </div>

            </g:else>
        </div>
    </body>
</html>
