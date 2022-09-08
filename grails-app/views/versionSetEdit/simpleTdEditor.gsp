<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${grailsApplication.config.tf.org.toolheader} | Create Simple TD</title>
    <style type="text/css">


    </style>

</head>

<body>
<div id="page-body" role="main">
    <div>
        <h1>Create Simple Trustmark Definition</h1>
        <p class="text-muted">
            On this page, you can quickly create a Trustmark Definition without worrying about many of the details which
            are required for a complete Trustmark Definition.
        </p>
    </div>

    <g:hasErrors bean="${command}">
        <div class="alert alert-danger" style="margin-top: 3em;">
            <ul class="errors" role="alert">
                <g:eachError bean="${command}" var="error">
                    <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>>
                        <g:message error="${error}"/>
                    </li>
                </g:eachError>
            </ul>
        </div>
    </g:hasErrors>


    <div style="margin-top: 3em;">
        <g:form action="saveSimpleTd" method="POST" class="form form-horizontal">
            <input type="hidden" name="id" id="id" value="${versionSet.name ?: ''}" />

            <g:set var="baseUrls" value="${tmf.host.util.TFAMPropertiesHolder.getBaseURLsAsStrings()}" />

            <g:if test="${baseUrls.size() < 2}">
                <input type="hidden" name="baseUrl" id="baseUrl" value="${baseUrls.get(0)}" />
            </g:if>
            <g:else>
                <div class="form-group ${hasErrors(bean: command, field: 'baseUrl', 'has-error')}">
                    <label for="baseUrl" class="col-sm-2 control-label">
                        Base URL <span class="glyphicon glyphicon-star"></span>
                    </label>
                    <div class="col-sm-10">
                        <g:select name="baseUrl" id="baseUrl" class="form-control" from="${baseUrls}" />
                    </div>
                </div>
            </g:else>

            <div class="form-group ${hasErrors(bean: command, field: 'name', 'has-error')}">
                <label for="name" class="col-sm-2 control-label">
                    Name <span class="glyphicon glyphicon-star"></span>
                </label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="name" name="name" placeholder="Name" value="${command?.name ?: ''}" />
                    <span class="help-block">
                        A concise name to represent this TD against others.  For example "Password Complexity Policy"
                    </span>
                </div>
            </div>

            <div class="form-group ${hasErrors(bean: command, field: 'description', 'has-error')}">
                <label for="assessmentStep" class="col-sm-2 control-label">
                    Description <span class="glyphicon glyphicon-star"></span>
                </label>
                <div class="col-sm-10">
                    <textarea class="form-control" name="description" id="description" rows="5">${command?.description ?: ''}</textarea>
                    <span class="help-block">
                        A few sentences about this Trustmark Definition, such as what information it captures and how
                        it can be used to validate conformance.
                    </span>
                </div>
            </div>

            <div class="form-group ${hasErrors(bean: command, field: 'assessmentStep', 'has-error')}">
                <label for="assessmentStep" class="col-sm-2 control-label">
                    Assessment Step <span class="glyphicon glyphicon-star"></span>
                </label>
                <div class="col-sm-10">
                    <textarea class="form-control" name="assessmentStep" id="assessmentStep" rows="5">${command?.assessmentStep ?: ''}</textarea>
                    <span class="help-block">
                        An assessment step should always start with a question that can be answered simply with yes or no.  Follow
                        this with additional explanatory text that can aid the assessor in understanding what is meant.
                    </span>
                </div>
            </div>

            <div class="form-group ${hasErrors(bean: command, field: 'source', 'has-error')}">
                <label for="source" class="col-sm-2 control-label">
                    Source
                </label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="source" name="source" placeholder="ie, NIST 800-53" value="${command?.source ?: ''}" />
                    <span class="help-block">
                        A simple description of the source where this requirement originates from.
                    </span>
                </div>
            </div>

            <div class="form-group ${hasErrors(bean: command, field: 'conformanceCriteria', 'has-error')}">
                <label for="conformanceCriteria" class="col-sm-2 control-label">
                    Conformance Statement
                </label>
                <div class="col-sm-10">
                    <textarea class="form-control" name="conformanceCriteria" id="conformanceCriteria" rows="5">${command?.conformanceCriteria ?: ''}</textarea>
                    <span class="help-block">
                        The conformance statement is the block of text taken from the original source material that the
                        assessed party must be in compliance with.  In most cases, this should be simply copied/pasted
                        from the original source.  If you must explain or clarify, it is recommended that you do so after
                        what you have copied and pasted as an additional paragraph.
                    </span>
                </div>
            </div>


            <div class="form-group" style="margin-top: 3em;">
                <div class="col-sm-12" style="text-align: center;">
                    <button type="submit" class="btn btn-primary">Save</button>
                    <g:if test="${grails.util.Environment.current == grails.util.Environment.DEVELOPMENT}">
                        <a href="javascript:autoPopulate()" class="btn btn-danger">Auto-Populate</a>
                    </g:if>
                </div>
            </div>

        </g:form>
    </div>

</div>
<script type="text/javascript">

    /**
     * A method which will auto popuate the fields here with some interesting data.  Allows for demo'ing TDs quickly.
     */
    function autoPopulate() {
        var tdCount = ${tmf.host.TrustmarkDefinition.count()};
        var tdName = loremText(randomBetween(3, 5), Lorem.TYPE.WORD)+ " "+(tdCount+1);
        $('#name').val(tdName);
        $('#source').val(randomStandard());
        $('#conformanceCriteria').val(loremText(randomBetween(1, 3), Lorem.TYPE.PARAGRAPH));
        $('#assessmentStep').val(randomStartQuestion()+" "+loremText(randomBetween(5, 15), Lorem.TYPE.WORD)+"?\n\n"+loremText(randomBetween(1, 3), Lorem.TYPE.PARAGRAPH));
    }//end autoPopulate()

    function randomBetween(low, high){
        var top = high - low;
        var random = Math.floor(Math.random()*top)
        return low + random;
    }

    function loremText(count, type){
        var lorem = new Lorem;
        return lorem.createText(count, type);
    }

    function randomStartQuestion(){
        var questions = ['Is the', 'Is it possible to count', 'When the server is communication with the', 'Can you'];
        var item = questions[Math.floor(Math.random()*questions.length)];
        return item;
    }

    function randomStandard() {
        var standards = [
            'NIST Publication 800-53',
            'NIST Publication 800-63',
            'American Standard Code For Information Interchange',
            'Atom 	1.0',
            'Cascading Style Sheets (CSS) 	2.1 	July 19, 2007',
            'COLLADA 	1.5.0[11] 	August 2008',
            'Common Information Model (CIM) 	2.22 	June 25, 2009',
            'Common Gateway Interface (CGI) 	1.1',
            'DocBook 	5.0',
            'ECMAScript 	5.1 	June 2011',
            'Executable and Linking Format (ELF) 	1.2',
            'Filesystem Hierarchy Standard (FHS) 	2.3 	January 29, 2004',
            'HTML 	4.01 	December 24, 1999',
            'HTTP 	1.1 	June 1999',
            'ICC profile 	4.2 	October 2004',
            'Linux Standard Base (LSB) 	4.0 	November 11, 2008',
            'MathML 	2.0 	October 2003',
            'Message Passing Interface (MPI) 	2.2[12] 	September 4, 2009',
            'Metalink 	4.0 	June 2010',
            'Multiboot Specification 	0.6.96[13] 	2009',
            'OAuth 	1.0 	October 3, 2007',
            'OEmbed 	1.0 	March 21, 2008',
            'Office Open XML 	1.0 	December 2006',
            'OpenAL 	1.1 	July 12, 2007',
            'OpenCL 	1.1 	June 11, 2010',
            'OpenDocument (ODF) 	1.2[14] 	September 30, 2011',
            'OpenEXR 	1.6.1 	October 22, 2007',
            'OpenGL 	4.0[15] 	March 11, 2010',
            'OpenGL ES 	2.0 	March 2007',
            'OpenML 	1.0',
            'OpenSL ES 	1.0.1 	September 24, 2009',
            'OpenVG 	1.1 	December 9, 2008',
            'OpenWF 	1.0 	November 9, 2009',
            'Open XML Paper Specification 	First Edition 	June 16, 2009',
            'Portable Document Format (PDF) 	1.7 	November 2006',
            'Portable Network Graphics (PNG) 	1.2 	August 11, 1999',
            'POSIX 	POSIX:2008 	2008',
            'PostScript 	3 	1997',
            'RenderMan (RISpec) 	3.2.1 	November 2005',
            'Rich Text Format (RTF) 	1.9.1 	March 2008',
            'RSS 	2.0 	September 2002',
            'Security Assertion Markup Language (SAML) 	2.0 	March 2005',
            'Scalable Vector Graphics (SVG) 	1.2T 	August 10, 2006',
            'Simple Network Management Protocol (SNMP) 	3 	2004',
            'Single UNIX Specification (SUS) 	3 	January 30, 2002',
            'SOAP 	1.2 	June 24, 2003',
            'Standard Configuration File Format 		1991',
            'Storage Management Initiative - Specification (SMI-S) 	1.1.0 	2005',
            'Synchronized Multimedia Integration Language (SMIL) 	2.1 	December 13, 2005',
            'SyncML 	1.1 	April 2, 2002',
            'SQL 	SQL:2008 	2008',
            'Transport Layer Security (TLS) 	1.2[16] 	August 2008',
            'Unified Modeling Language (UML) 	2.3[17] 	May 2010',
            'Unicode 	6.2.0[18] 	September 2012',
            'Universal 3D (U3D) 	ECMA-363 4th edition 	June 2007',
            'Universal Disk Format (UDF) 	2.60 	March 1, 2005',
            'WebGL 	1.0 	March 3, 2011',
            'Wireless Application Protocol (WAP) 	2.0 	November 6, 2002',
            'Wireless Markup Language (WML) 	2.0 	2001',
            'XHTML 	1.1 	May 31, 2001',
            'XML 	1.1 	February 4, 2004'
        ];
        var item = standards[Math.floor(Math.random()*standards.length)];
        return item;
    }
</script>
</body>
</html>
