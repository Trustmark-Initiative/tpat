%{--<ol class="property-list ${domainClass.propertyName}" style="list-style: none;">--}%
%{--<g:each in="${domainClass.persistentProperties}" var="p">--}%
%{--<li class="fieldcontain">--}%
%{--<span id="${p.name}-label" class="property-label"><g:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></span>--}%
%{--<div class="property-value" aria-labelledby="${p.name}-label">${body(p)}</div>--}%
%{--</li>--}%
%{--</g:each>--}%
%{--</ol>--}%

<div class="container list-container">
    <g:each in="${domainClass.persistentProperties}" var="p">
        <div class="row">
            <div class="col-md-2">
                <span id="${p.name}-label">
                    <g:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" />
                </span>
            </div>
            <div class="col-md-10">
                ${body(p)}
            </div>
        </div>
    </g:each>
</div>
