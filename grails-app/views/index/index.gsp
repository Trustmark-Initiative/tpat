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
                        This system has no Trustmark Definition or Trust Interoperability Profile data to
                        display.

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
                        A <b>Trustmark Definition</b> specifies the normative conformance criteria that a
                        <b>Trustmark Recipient</b> must meet, as well as the formal assessment process that
                    a <b>Trustmark Provider</b> must perform to assess whether the Trustmark Recipient
                    qualifies for the trustmark. There can be many different types of trustmarks,
                    and each type of trustmark has its own Trustmark Definition. Formally, a
                    Trustmark Definition exists as an eXtensible Markup Language (XML) object that
                    conforms to the normative structure and rules for Trustmark Definitions as defined
                    by the <b>Trustmark Framework Technical Specification</b>, which is located at
                        <a href="https://trustmarkinitiative.org/specifications/trustmark-framework">https://trustmarkinitiative.org/specifications/trustmark-framework</a>.
                    In addition, a Trustmark Definition may be represented in other formats,
                    e.g., HTML, for greater human readability. Use the link below to visit the index
                    page for all Trustmark Definitions developed under this project. Each Trustmark
                    Definition is provided in both XML and HTML format.
                    </div>
                    <div style="margin-top: 2em;">
                        <a class="btn btn-default" href="${createLink(controller: 'trustmarkDefinition', action: 'list')}">View All TDs &raquo;</a>
                    </div>

                </div>
                <div class="col-md-6">
                    <h2>Trust Interoperability Profiles <small>(${tipCount ?: 0} Total)</small></h2>
                    <div class="description">
                        A <b>Trustmark Relying Party</b> is any entity (organization or individual) that
                    relies on trustmarks as a means for establishing third-party-based trust. A
                    Trustmark Relying Party, or a group that represents the interests of multiple
                    Trustmark Relying Parties with similar requirements, may define a
                        <b>Trust Interoperability Profile</b> that expresses a trust and interoperability
                    policy in terms of a set of trustmarks that a <b>Trustmark Recipient</b> must
                    possess, in order to meet the policy's requirements. A Trust Interoperability
                    Profile may specify not only which trustmarks are required (based on one or more
                        <b>Trustmark Definitions</b>), but also which <b>Trustmark Provider(s)</b> are deemed
                    trustworthy for each trustmark. Formally, a Trust Interoperability Profile exists as
                    an eXtensible Markup Language (XML) object that conforms to the normative structure
                    and rules for Trustmark Definitions as defined by the <b>Trustmark Framework
                    Technical Specification</b>, which is located at
                        <a href="https://trustmarkinitiative.org/specifications/trustmark-framework">https://trustmarkinitiative.org/specifications/trustmark-framework</a>.
                    In addition, a Trust Interoperability Profile may be represented in other formats,
                    e.g., HTML, for greater human readability. Use the link below to visit the index
                    page for all Trust Interoperability Profiles developed under this project. Each
                    Trust Interoperability Profile is provided in both XML and HTML format.
                    </div>
                    <div style="margin-top: 2em;">
                        <a class="btn btn-default" href="${createLink(controller: 'tip', action: 'list')}">View All TIPs &raquo;</a>
                        <a class="btn btn-default" href="${createLink(controller: 'tip', action: 'tipTree')}">View Primary TIPs &raquo;</a>
                    </div>

                </div>
            </div>


                <div style="margin-top: 2em;">
                    <h3>Other Actions:</h3>
                    <a class="btn btn-default" href="${createLink(controller: 'search')}">Search &raquo;</a>
                    <a class="btn btn-default" href="${createLink(controller: 'keyword', action: 'list')}">View Keywords &raquo;</a>
                    <a class="btn btn-default" href="${createLink(controller: 'downloadAll', action: 'index')}">Download All &raquo;</a>
                </div>

                <sec:ifAllGranted roles="ROLE_ADMIN">
                    <div style="margin-top: 2em;">
                        <h3>Administration:</h3>
                        <a class="btn btn-default" href="${createLink(controller:'provider', action: 'list')}">Providers</a>
                        <a class="btn btn-default" href="${createLink(controller:'systemVariable')}">System Variables</a>
                        <a class="btn btn-default" href="${createLink(controller:'taxonomyTerm', action: 'index')}">Taxonomy</a>
                        <a class="btn btn-default" href="${createLink(controller:'appearance', action: 'index')}">Configuration</a>
                    </div>
                </sec:ifAllGranted>

            </g:else>
        </div>
    </body>
</html>
