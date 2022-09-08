<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${grailsApplication.config.tf.org.toolheader} | Create Simple TIP</title>
    <style type="text/css">


    </style>

</head>

<body>
<div id="page-body" role="main">
    <div>
        <h1>Create Simple Trust Profile</h1>
        <p class="text-muted">
            On this page, you can quickly create a Trust Profile without worrying about many of the details which
            are required for one.  All references in this profile will simply be required, with no additional logic.
        </p>
    </div>

    <div id="feedbackContainer">
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
    </div>

    <div style="margin-top: 3em;">
        <g:form action="saveSimpleTip" method="POST" class="form form-horizontal">
            <input type="hidden" name="id" id="id" value="${versionSet.name ?: ''}" />
            <input type="hidden" name="tipId" id="tipId" value="${command.tipId ?: ''}" />

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

            <div class="form-group ${hasErrors(bean: command, field: 'name', 'has-error')}" id="nameContainer">
                <label for="name" class="col-sm-2 control-label">
                    Name <span class="glyphicon glyphicon-star"></span>
                </label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="name" name="name" placeholder="Name" value="${command?.name ?: ''}" />
                    <span class="help-block">
                        A concise name to represent this Trust Profile against others.  For example "CJIS Security Policy"
                    </span>
                </div>
            </div>

            <div class="form-group ${hasErrors(bean: command, field: 'description', 'has-error')}" id="descriptionContainer">
                <label for="description" class="col-sm-2 control-label">
                    Description <span class="glyphicon glyphicon-star"></span>
                </label>
                <div class="col-sm-10">
                    <textarea class="form-control" name="description" id="description" rows="3">${command?.description ?: ''}</textarea>
                    <span class="help-block">
                        A few sentences about this Trust Profile, such as what information it captures and how
                        it can be used to validate conformance.
                    </span>
                </div>
            </div>

            <div class="form-group" id="referencesJsonContainer">
                <label class="col-sm-2 control-label">
                    References <span class="glyphicon glyphicon-star"></span>
                </label>
                <div class="col-sm-10">
                    <input type="hidden" name="referenceJson" id="referenceJson" value="${command.referenceJson ?: '{}'}" />
                    <div id="referencesContainer">
                        <p class="form-control-static">
                            <em>There are no references.</em>
                        </p>
                    </div>
                    <div style="margin-top: 1em;" id="tipReferenceButtons">

                    </div>
                </div>
            </div>


            <div class="form-group" style="margin-top: 3em;">
                <div class="col-sm-12" style="text-align: center;">
                    <button type="submit" class="btn btn-primary" onclick="return formSubmittal();">Save</button>
                    <g:if test="${grails.util.Environment.current == grails.util.Environment.DEVELOPMENT}">
                        <a href="javascript:autoPopulate()" class="btn btn-danger">Auto-Populate</a>
                    </g:if>
                </div>
            </div>

        </g:form>
    </div>



</div>

<tmpl:/templates/tipReferenceSearch buttonContainerId="tipReferenceButtons" onTipReferenceAddFunction="addTipReferences" hasTipReferenceFunction="hasTipReference" />

<script type="text/javascript">

    $(document).ready(function(){
        loadProviderReferences();
    });

    var PROVIDER_REFERENCES = []

    /**
     * Loads the full list of provider refs from the server.
     */
    function loadProviderReferences(){
        $.ajax({
            url: '${createLink(controller: 'provider', action: 'list')}',
            dataType: 'json',
            data: {
                timestamp: new Date().getTime(),
                format: 'json',
                max: 10000
            },
            success: function(data){
                if( data && data.providers ) {
                    PROVIDER_REFERENCES = data.providers;
                }
                loadRefData();
            },
            error: function(jqXHR, textStatus, errorThrown){
                // Do nothing here, we just silently fail.
                loadRefData();
            }
        })
    }

    function loadRefData(){
        var refJsonString = $('#referenceJson').val();
        var referenceData = JSON.parse(refJsonString);
        TIP_REFERENCES = referenceData.references;
        if( TIP_REFERENCES == null )
            TIP_REFERENCES = [];
        setTimeout("updateTipReferenceView()", 150);
    }

    /**
     * called when user clicks the save button.
     */
    function formSubmittal(){
        clearError();
        $('#nameContainer').removeClass('has-error');
        $('#descriptionContainer').removeClass('has-error');
        $('#referencesJsonContainer').removeClass('has-error');

        if( $('#name').val().trim() === '' ){
            $('#nameContainer').addClass('has-error');
            showError("Name is a required field");
            return false;
        }
        if( $('#description').val().trim() === '' ){
            $('#descriptionContainer').addClass('has-error');
            showError("Description is a required field");
            return false;
        }

        if( !TIP_REFERENCES || TIP_REFERENCES.length == 0 ){
            $('#referencesJsonContainer').addClass('has-error');
            showError("You must choose at least 1 reference.");
            return false;
        }

        for( var i = 0; i < TIP_REFERENCES.length; i++ ){
            TIP_REFERENCES[i].Providers = [];
        }
        parseProviders(TIP_REFERENCES);

        var referenceJsonText = JSON.stringify({"references": TIP_REFERENCES});
        $('#referenceJson').val(referenceJsonText);

        return true;
    }//end formSubmittal()

    function parseProviders(references){
        for( var i = 0; i < references.length; i++ ){
            var ref = references[i];
            var providers = [];
            var providerSelects = $('#providerContainer'+ref.Identifier.hashCode()+' .providerSelect');
            for( var j = 0; j < providerSelects.length; j++ ){
                var select = providerSelects[j];
                var selectedValue = $(select).val();
                if( selectedValue.trim().length > 0 ) {
                    console.log("For TD: " + ref.Name + ", found Select: [" + selectedValue + "]");
                    providers.push({Identifier: selectedValue});
                }
            }
            if( providers.length > 0 ){
                ref.Providers = providers;
            }
        }
    }

    function clearError(){
        $('#feedbackContainer').html('');
    }
    function showError(msg){
        $('#feedbackContainer').html('<div class="alert alert-danger" style="margin-top: 3em;">'+msg+'</div>');
    }


    function autoPopulate(){
        $('#name').val("Test "+new Date().getTime());
        $('#description').val("This is a sample Trust Profile used for testing.  Please do not reference it from any production or meaningful use artifact.");
    }

    /**
     * Holds all of the TIP references that are active.
     */
    var TIP_REFERENCES = [];

    /**
     * Returns true if the given object is already being referenced.
     */
    function hasTipReference(ref){
        if( TIP_REFERENCES ){
            for( var i = 0; i < TIP_REFERENCES.length; i++ ){
                var cur = TIP_REFERENCES[i];
                if( ref.Identifier === cur.Identifier ){
                    return true;
                }
            }
        }
        return false;
    }//end hasTipReference()

    /**
     * Called when the user selects a reference in the search dialog.
     */
    function addTipReferences(ref) {
        if( ref && ref.length ){
            for( var i = 0; i < ref.length; i++ ){
                console.log("User selected: "+ref[i].Name+", v."+ref[i].Version);
                TIP_REFERENCES.push(ref[i]);
            }
        }else if( ref ) {
            console.log("User selected: "+ref.Name+", v."+ref.Version);
            TIP_REFERENCES.push(ref);
        }

        updateTipReferenceView();
    }//end addTipReference()

    /**
     * Redraws the view of TIP references.
     */
    function updateTipReferenceView(){
        $('#referencesContainer').html('');
        console.log("Updating TIP Reference view...");

        if( TIP_REFERENCES && TIP_REFERENCES.length > 0 ){
            for( var i = 0; i < TIP_REFERENCES.length; i++ ){
                console.log("Handling TIP reference #"+i);
                var ref = TIP_REFERENCES[i];
                var html = [];
                html.push('<div class="row" style="margin-bottom: 1em;">');
                html.push('  <div class="col-md-1" style="text-align: center;">');
                html.push('    <a href="javascript:removeReference('+i+')" class="btn btn-danger btn-xs">Remove</a>');
                html.push('  </div>');
                html.push('  <div class="col-md-11">');
                html.push('    <div>');
                var typeIcon = 'th-list';
                if( ref.Type === "TrustmarkDefinition" ){
                    typeIcon = 'tag';
                }
                html.push('      <div style="font-weight: bold;"><span class="glyphicon glyphicon-'+typeIcon+'"></span> '+ref.Name+', v'+ref.Version+'</div>');
                html.push('      <div style="font-size: 90%; margin-left: 1.5em;" class="text-muted">'+ref.Description+'</div>');
                html.push('    </div>');

                if( ref.Type === "TrustmarkDefinition" ){
                    html.push('    <div>');
                    html.push('        <div class="pull-right">');
                    html.push('            <a href="javascript:addProvider('+ref.Identifier.hashCode()+')" class="btn btn-default btn-xs">Add Provider</a>');
                    html.push('        </div>');
                    html.push('    </div>\n ');
                    html.push('    <div id="providerContainer'+ref.Identifier.hashCode()+'" style="margin-left: 1.5em;">');
                    if( ref.Providers && ref.Providers.length > 0){
                        for( var j = 0; j < ref.Providers.length; j++ ){
                            html.push('<div>');
                            html.push('  Provider: ' + buildProviderSelect(ref.Providers[j]));
                            html.push(" <a class=\"removeProviderButton btn btn-default btn-xs\"><span class=\"glyphicon glyphicon-remove\"></span> Remove</a>");
                            html.push('</div>');
                        }
                    }
                    html.push('    </div>');
                }

                html.push('  </div>');
                html.push('</div>');
                if( i < (TIP_REFERENCES.length - 1) )
                    html.push("<hr />");
                $('#referencesContainer').append(html.join("\n"));
            }
        }else{
            $('#referencesContainer').html('<p class="form-control-static"><em>There are no references.</em></p>');
        }

        $(".removeProviderButton").click(removeSelect);
    }//end updateTipReferenceView()

    function addProvider(hash){
        var html = "<div>Provider: "+buildProviderSelect(null)+" <a class=\"removeProviderButton btn btn-default btn-xs\"><span class=\"glyphicon glyphicon-remove\"></span> Remove</a></div>";
        $('#providerContainer'+hash).prepend(html);
        $(".removeProviderButton").click(removeSelect);
    }

    function removeSelect(){
        $(this).parent().remove();
    }

    function buildProviderSelect(provider){
        var html = [];

        html.push('<select class="providerSelect">');
        html.push('  <option value="">&nbsp;</option>');
        for( var i = 0; i < PROVIDER_REFERENCES.length; i++ ){
            var curProvider = PROVIDER_REFERENCES[i];
            if( provider && provider.Identifier === curProvider.Identifier ){
                html.push('<option value="'+curProvider.Identifier+'" selected="selected">'+curProvider.Name+'</option>');
            }else{
                html.push('<option value="'+curProvider.Identifier+'">'+curProvider.Name+'</option>');
            }
        }
        html.push('</select>');

        return html.join("\n");
    }

    function removeReference(index){
        var newReferences = [];
        for( var i = 0; i < TIP_REFERENCES.length; i++ ){
            if( i != index ){
                newReferences.push(TIP_REFERENCES[i]);
            }
        }
        TIP_REFERENCES = newReferences;
        updateTipReferenceView();
    }

</script>
</body>
</html>
