<%@ page import="edu.gatech.gtri.trustmark.v1_0.model.*; org.apache.commons.lang.StringUtils" %>
<!doctype html>
<html>
    <head>
        <%--
            NOTE:
            With <base target="_blank", you have to specify target="_self" on anchors used as buttons!
        --%>
        <base target="_blank"/>
        <meta name="layout" content="main"/>
        <title>View TD: ${td.metadata.name}</title>
        <style type="text/css">
            .URIList, .URIList li {
                list-style: none;
                margin: 0;
                padding: 0;
            }
        </style>
    </head>



    <body>
        <div id="page-body" role="main">
            <div class="row" style="margin-top: 1em;">
                <div class="col-md-11">
                    <h2 style="margin-top: 0; margin-bottom: 0;">${td.metadata.name}, v${td.metadata.version}</h2>
                    <div style="font-size: 110%;">${raw(td.metadata.description)}</div>
                </div>
                <div class="col-md-1" style="text-align: center; font-size: 350%;">
                    <span class="glyphicon glyphicon-tag" title="Trustmark Definition"></span>
                </div>
            </div>

            <g:if test="${td.metadata.deprecated}">
                <div class="alert alert-warning" style="margin-top: 1em;">
                    <div style="font-weight: bold; font-style: italic; font-size: 120%;">
                        DEPRECATED
                    </div>
                    <div>
                        This Trustmark Definition has been deprecated.  You should not use this definition for any reason.
                    </div>
                    <g:if test="${td.metadata.supersededBy?.size() > 0}">
                        <div>
                            This Trustmark Definition has been superseded by the following:
                            <ul>
                                <g:each in="${td.metadata.supersededBy}" var="ref">
                                    <g:if test="${ref.name && ref.name.trim().length() > 0}">
                                        <li>
                                            <a href="${ref.identifier}">
                                                ${ref.name}, v${ref.version}
                                            </a>
                                        </li>
                                    </g:if><g:else>
                                        <li>
                                            <a href="${ref.identifier}">${ref.identifier}</a>
                                        </li>
                                    </g:else>
                                </g:each>
                            </ul>
                        </div>
                    </g:if>
                </div>
            </g:if>

            <g:if test="${references?.size() > 0}">
                <div style="margin-top: 1em;">
                    <g:each in="${references}" var="ref">
                        <div>
                            Contained in Trust Profile: <a href="${ref.sourceTip.identifier}">${ref.sourceTip.name}, v${ref.sourceTip.tipVersion}</a>
                        </div>
                    </g:each>
                </div>
            </g:if>

            <div style="margin-top: 2em;">
                <div>
                    <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.assessmentStepPreface)}">
                        <h5 style="margin-bottom: 0"><b><i>${raw(td.assessmentStepPreface)}</i></b></h5>
                    </g:if>
                    <g:if test="${td.assessmentSteps.size() > 1}">
                        <h4 style="margin-bottom: 0;">Assessment Steps (${td.assessmentSteps.size()})</h4>
                    </g:if>
                    <g:else>
                        <h4 style="margin-bottom: 0;">Assessment Step</h4>
                    </g:else>
                    <table class="table table-condensed table-striped">
                        <g:each in="${td.assessmentSteps}" var="step">
                            <tr><td>
                                <div>
                                    <div class="row">
                                        <div class="col-md-1" style="text-align: center; font-size: 140%; font-weight: bold;">
                                            ${step.number}
                                        </div>
                                        <div class="col-md-11">
                                            <div style="font-size: 125%; font-weight: bold;">${step.name} <span style="font-size: 70%; font-weight: normal; margin-left: 1em;">(${step.id})</span></div>
                                            <div style="font-size: 120%;">${raw(step.description)}</div>
                                            <g:if test="${step.artifacts?.size() > 0}">
                                                <g:set var="artifactsList" value="${new ArrayList(step.artifacts)}" />
                                                ${java.util.Collections.sort(artifactsList, new Comparator<Artifact>(){
                                                    int compare(Artifact a1, Artifact a2) {
                                                        return a1.getName().compareToIgnoreCase(a2.getName());
                                                    }
                                                })}
                                                <div class="artifactContainer" style="margin-top: 1em;">
                                                    <div>
                                                        <div style="font-weight: bold;">
                                                            <g:if test="${step.artifacts.size() > 1}">Artifacts</g:if>
                                                            <g:else>Artifact</g:else>
                                                        </div>
                                                        <g:each in="${artifactsList}" var="artifact">
                                                            <div class="row">
                                                                <div class="col-md-2" style="text-align: left;">${artifact.name}</div>
                                                                <div class="col-md-10">${raw(artifact.description)}</div>
                                                            </div>
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </g:if>
                                            <g:if test="${step.parameters?.size() > 0}">
                                                <g:set var="paramList" value="${step.parameters.toList()}" />
                                                ${java.util.Collections.sort(paramList, new Comparator<TrustmarkDefinitionParameter>(){
                                                    int compare(TrustmarkDefinitionParameter a1, TrustmarkDefinitionParameter a2) {
                                                        return a1.getName().compareToIgnoreCase(a2.getName());
                                                    }
                                                })}
                                                <div class="parameterContainer" style="margin-top: 1em;">
                                                    <div>
                                                        <div style="font-weight: bold;">
                                                            <g:if test="${paramList.size() > 1}">Parameters</g:if>
                                                            <g:else>Parameter</g:else>
                                                        </div>
                                                        <g:each in="${paramList}" var="parameter">
                                                            <div class="row row-cols-2">
                                                                <div class="col-md-3" style="text-align: left;">
                                                                    ${parameter.name}<g:if test="${parameter.required}"><sup class="text-danger" title="This parameter must be populated for any trustmark issued.">required</sup></g:if>
                                                                </div>
                                                                <div class="col-md-8" style="text-align: left;" >${parameter.parameterKind} : ${raw(parameter.description)}
                                                                    <ul>
                                                                <g:each in="${parameter.enumValues}" var="evalue">
                                                                        <li style="text-align: left;"><i>${evalue}</i></li>
                                                                </g:each>
                                                                </ul>
                                                                </div>
                                                             </div>
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </g:if>
                                        </div>
                                    </div>
                                </div>
                            </td></tr>
                        </g:each>
                    </table>
                </div>

                <div>
                    <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.conformanceCriteriaPreface)}">
                        <h5 style="margin-bottom: 0;"><b><i>${raw(td.conformanceCriteriaPreface)}</i></b></h5>
                    </g:if>
                    <h4 style="margin-bottom: 0;">Conformance Criteria (${td.conformanceCriteria.size()})</h4>
                    <table class="table table-condensed table-striped">
                        <g:each in="${td.conformanceCriteria}" var="crit">
                            <tr><td>
                                <div>
                                    <div style="font-size: 125%; font-weight: bold;">${crit.name}</div>
                                    <div style="font-size: 120%;">${raw(crit.description)}</div>
                                    <g:if test="${crit.citations?.size() > 0}">
                                        <div style="font-weight: bold;">
                                            <g:if test="${crit.citations.size() > 1}">Citations</g:if>
                                            <g:else>Citation</g:else>
                                        </div>
                                        <g:each in="${crit.citations}" var="citation">
                                            <div class="row">
                                                <div class="col-md-2">
                                                    ${citation.source.identifier}
                                                </div>
                                                <div class="col-md-10">
                                                    ${raw(citation.description)}
                                                </div>
                                            </div>
                                        </g:each>
                                    </g:if>
                                </div>
                            </td></tr>
                        </g:each>
                    </table>
                </div>
            </div>

        </div>

    <hr />

            <div id="showMetadataButtonContainer">
                <a href="javascript:showMetadata()" target="_self" id="showMetadataButton" class="btn btn-primary">Show Metadata, Sources &amp; Terms</a>
            </div>
            <div id="hideMetadataButtonContainer">
                <a href="javascript:hideMetadata()" target="_self" id="hideMetadataButton" class="btn btn-primary">Hide Metadata, Sources &amp; Terms</a>
            </div>

            <div style="margin-top: 2em;" id="metadataAndOtherContainer">
                <div>
                    <h4 style="margin-bottom: 0;">Metadata</h4>
                    <table class="table table-condensed table-bordered table-striped">
                        <tr>
                            <td style="width: 20%;"><abbr title="The date and time at which this Trustmark Definition was published">Publication Date</abbr></td>
                            <td style="width: 80%;"><g:formatDate date="${td.metadata.publicationDateTime}" format="yyyy-MM-dd" /></td>
                        </tr>
                        <tr>
                            <td style="width: 20%;"><abbr title="A URI indicating the name of an attribute that can be used to refer to trustmarks issued under this Trustmark Definition">Trustmark Reference Attribute</abbr></td>
                            <td style="width: 80%;">${td.metadata.trustmarkReferenceAttributeName?.toString()}</td>
                        </tr>
                        <tr>
                            <td style="width: 20%;"><abbr title="The organization that published this Trustmark Definition">Issuing Organization</abbr></td>
                            <td style="width: 80%;">
                                <div>
                                    ${td.metadata.trustmarkDefiningOrganization.name} (<a href="${td.metadata.trustmarkDefiningOrganization.identifier.toString()}" target="_blank">${td.metadata.trustmarkDefiningOrganization.identifier.toString()}</a>)
                                    <a href="javascript:showContact()" target="_self" class="btn btn-xs btn-default">View Contact<g:if test="${td.metadata.trustmarkDefiningOrganization.contacts.size() > 1}">s</g:if></a>
                                </div>
                                <div id="contact">
                                    <table class="table table-condensed">
                                        <g:each in="${td.metadata.trustmarkDefiningOrganization.contacts}" var="contact">
                                            <tr style="border: none;">
                                                <td>${contact.responder ?: raw('<em>No Responder</em>')}</td>
                                                <td>${contact.defaultEmail ?: raw('<em>No Email Address</em>')}</td>
                                                <td>${contact.defaultTelephone ?: raw('<em>No telephone</em>')}</td>
                                                <td>${contact.defaultMailingAddress ?: raw('<em>No Mailing Address</em>')}</td>
                                            </tr>
                                        </g:each>
                                    </table>
                                </div>
                                <script type="text/javascript">
                                    $(document).ready(function(){
                                        $('#contact').hide();
                                    })

                                    function showContact(){
                                        $('#contact').show();
                                    }
                                </script>
                            </td>
                        </tr>
                        <tr>
                            <td style="width: 20%;">Keywords</td>
                            <td style="width: 80%;">
                                <g:if test="${td.metadata.keywords && td.metadata.keywords.size() > 0}">
                                    <g:each in="${td.metadata.keywords}" var="keyword" status="i">
                                        ${keyword}<g:if test="${i + 1 != td.metadata.keywords.size()}">,</g:if>
                                    </g:each>
                                </g:if><g:else>
                                <em>There are no keywords.</em>
                            </g:else>
                            </td>
                        </tr>
                        <g:if test="${td.metadata.supersedes?.size() > 0}">
                            <tr>
                                <td style="width: 20%;"><abbr title="A list of other Trustmark Definitions that this Trustmark Definition supersedes or replaces">Supersedes</abbr></td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    <ul class="URIList">
                                        <g:each in="${td.metadata.supersedes}" var="ref">
                                            <g:if test="${ref.name && ref.name.trim().length() > 0}">
                                                <li>
                                                    <a href="${ref.identifier}">
                                                        ${ref.name}, v${ref.version}
                                                    </a>
                                                </li>
                                            </g:if>
                                            <g:else>
                                                <li>
                                                    <a href="${ref.identifier}">${ref.identifier}</a>
                                                </li>
                                            </g:else>
                                        </g:each>
                                    </ul>
                                </td>
                            </tr>
                        </g:if>
                        <g:if test="${td.metadata.supersededBy?.size() > 0}">
                            <tr>
                                <td style="width: 20%;"><abbr title="A list of other Trustmark Definitions that supersede or replace this Trustmark Definition">Superseded By</abbr></td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    <ul class="URIList">
                                        <g:each in="${td.metadata.supersededBy}" var="ref">
                                            <g:if test="${ref.name && ref.name.trim().length() > 0}">
                                                <li>
                                                    <a href="${ref.identifier}">
                                                        ${ref.name}, v${ref.version}
                                                    </a>
                                                </li>
                                            </g:if>
                                            <g:else>
                                                <li>
                                                    <a href="${ref.identifier}">${ref.identifier}</a>
                                                </li>
                                            </g:else>
                                        </g:each>
                                    </ul>
                                </td>
                            </tr>
                        </g:if>
                        <g:if test="${td.metadata.satisfies?.size() > 0}">
                            <tr>
                                <td style="width: 20%;"><abbr title="A list of other Trustmark Definitions, indicating that if an entity satisfies the conformance criteria for this Trustmark Definition, then the entity may also satisfy the conformance criteria for the referenced Trustmark Definitions">Satisfies</abbr></td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    <ul class="URIList">
                                        <g:each in="${td.metadata.satisfies}" var="ref">
                                            <g:if test="${ref.name && ref.name.trim().length() > 0}">
                                                <li>
                                                    <a href="${ref.identifier}">
                                                        ${ref.name}, v${ref.version}
                                                    </a>
                                                </li>
                                            </g:if>
                                            <g:else>
                                                <li>
                                                    <a href="${ref.identifier}">${ref.identifier}</a>
                                                </li>
                                            </g:else>
                                        </g:each>
                                    </ul>
                                </td>
                            </tr>
                        </g:if>

                        <tr>
                            <td style="width: 20%;"><abbr title="A Boolean expression that describes the logical conditions under which a Trustmark Provider may issue a Trustmark to a Trustmark Recipient for this Trustmark Definition, in terms of assessment step results">Issuance Criteria</abbr></td>
                            <td style="width: 80%; font-size: 90%; font-family: monospace;" class="text-muted">
                                <div class="issuance_criteria">${raw(td.issuanceCriteria)}</div>
                            </td>
                        </tr>
                        <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.metadata.targetStakeholderDescription)}">
                            <tr>
                                <td style="width: 20%;">Target Stakeholder</td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    ${td.metadata.targetStakeholderDescription}
                                </td>
                            </tr>
                        </g:if>
                        <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.metadata.targetRecipientDescription)}">
                            <tr>
                                <td style="width: 20%;">Target Recipient</td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    ${td.metadata.targetRecipientDescription}
                                </td>
                            </tr>
                        </g:if>
                        <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.metadata.targetRelyingPartyDescription)}">
                            <tr>
                                <td style="width: 20%;">Target Relying Party</td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    ${td.metadata.targetRelyingPartyDescription}
                                </td>
                            </tr>
                        </g:if>
                        <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.metadata.targetProviderDescription)}">
                            <tr>
                                <td style="width: 20%;">Target Provider</td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    ${td.metadata.targetProviderDescription}
                                </td>
                            </tr>
                        </g:if>
                        <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.metadata.providerEligibilityCriteria)}">
                            <tr>
                                <td style="width: 20%;">Provider Eligibility Criteria</td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    ${td.metadata.providerEligibilityCriteria}
                                </td>
                            </tr>
                        </g:if>
                        <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.metadata.assessorQualificationsDescription)}">
                            <tr>
                                <td style="width: 20%;">Assessor Qualifications</td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    ${td.metadata.assessorQualificationsDescription}
                                </td>
                            </tr>
                        </g:if>
                        <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.metadata.trustmarkRevocationCriteria)}">
                            <tr>
                                <td style="width: 20%;">Trustmark Revocation Criteria</td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    ${td.metadata.trustmarkRevocationCriteria}
                                </td>
                            </tr>
                        </g:if>
                        <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.metadata.extensionDescription)}">
                            <tr>
                                <td style="width: 20%;">Extension Description</td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    ${raw(td.metadata.extensionDescription)}
                                </td>
                            </tr>
                        </g:if>


                        <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.metadata.legalNotice)}">
                            <tr>
                                <td style="width: 20%;">Legal Notice</td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    ${td.metadata.legalNotice}
                                </td>
                            </tr>
                        </g:if>
                        <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.metadata.notes)}">
                            <tr>
                                <td style="width: 20%;">Notes</td>
                                <td style="width: 80%; font-size: 90%;" class="text-muted">
                                    ${td.metadata.notes}
                                </td>
                            </tr>
                        </g:if>
                        %{-- TODO: need to show Known Conflicts when available --}%
                        %{--<g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(td.knownConflicts)}">--}%
                            %{--<tr>--}%
                                %{--<td style="width: 20%;" title="A list of other Trustmark Definitions, indicating that if an entity satisfies the conformance criteria for this Trustmark Definition, then the entity is unlikely to satisfy the conformance criteria for the referenced Trustmark Definitions">Known Conflicts: </td>--}%
                                %{--<td style="width: 80%; font-size: 90%;" class="text-muted">--}%
                                    %{--${td.knownConflicts}--}%
                                %{--</td>--}%
                            %{--</tr>--}%
                        %{--</g:if>--}%
                    </table>
                </div>


                <g:if test="${td.sources.size() > 0}">
                    <div style="margin-top: 2em;">
                        <h4 style="margin-bottom: 0;">Sources (${td.sources.size()})</h4>
                        <div>
                            <table class="table table-condensed table-bordered table-striped">
                            <g:each in="${td.sources}" var="source">
                                <tr>
                                    <td style="width: 20%; font-size: 120%; font-weight: bold;">${source.identifier}</td>
                                    <td>${raw(source.reference)}</td>
                                </tr>
                            </g:each>
                            </table>
                        </div>
                    </div>
                </g:if>

                <g:if test="${td.terms.size() > 0}">
                    <div style="margin-top: 2em;">
                        <h4 style="margin-bottom: 0;">Terms (${td.terms.size()})</h4>
                        <div>
                            <table class="table table-condensed table-bordered table-striped">
                                <thead>
                                <tr>
                                    <th>Term Name</th>
                                    <th>Abbreviations</th>
                                    <th>Definition</th>
                                </tr>
                                </thead>
                                <tbody>
                                <g:each in="${td.termsSorted}" var="term">
                                    <tr>
                                        <td style="width: 20%;">${term.name}</td>
                                        <td style="text-align: center; width: 10%;">
                                            <g:each in="${term.abbreviations}" status="index" var="abbr">
                                                ${abbr}<g:if test="${index < (term.abbreviations.size() - 1)}">, </g:if>
                                            </g:each>
                                        </td>
                                        <td>
                                            ${raw(term.definition)}
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>

                        </div>
                    </div>
                </g:if>

            </div>

            <div style="margin-top: 4em;">
                Also available as
                <a href="${td.metadata.identifier}?format=xml" target="_blank">XML</a> or
                <a href="${td.metadata.identifier}?format=json" target="_blank">JSON</a>
            </div>

            <script type="text/javascript">
                $(document).ready(function(){
                    $('#metadataAndOtherContainer').hide();
                    $('#showMetadataButtonContainer').hide();
                    $('#hideMetadataButtonContainer').hide();

                    <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(params.showMetadata)}">
                    setTimeout('showMetadata()', 100);
                    </g:if>
                    <g:else>
                    setTimeout('hideMetadata()', 100);
                    </g:else>
                })

                function showMetadata(){
                    $('#showMetadataButtonContainer').hide();
                    $('#hideMetadataButtonContainer').show();
                    $('#metadataAndOtherContainer').show();
                }

                function hideMetadata(){
                    $('#showMetadataButtonContainer').show();
                    $('#hideMetadataButtonContainer').hide();
                    $('#metadataAndOtherContainer').hide();
                }

            </script>


        </div>
    </body>
</html>
