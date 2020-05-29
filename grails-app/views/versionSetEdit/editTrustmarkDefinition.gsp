<html>
<head>
    <meta name="layout" content="main"/>
    <title>Trustmark Definition Editor</title>

    <style type="text/css">
    #mainContainer {
        margin-top: 3em;
    }

    .mainTitle {
        margin: 0; padding: 0;
    }

    .tabsContainer {
        margin-top: 1em;
    }

    #metadataContainerController, #metadataContainerController:visited, #metadataContainerController:hover {
        color: inherit;
    }

    .buttonContainer {
        margin-top: 1em;
        margin-bottom: 1em;
    }


    .termControlsContainer {
        width: 3em;
        text-align: center;
    }
    .termNameContainer {
        width: 10em;
    }
    .termAbbrContainer {
        width: 10em;
    }
    .termDefinitionContainer {
        width: auto;
    }



    .sourceControlsContainer {
        width: 3em;
        text-align: center;
    }
    .sourceIdentifierContainer {
        width: 10em;
    }
    .sourceReferenceContainer {
        width: auto;
    }


    .criterionContainer {
        border-bottom: 1px solid #888;
        padding-top: 1em;
        padding-left: 0.5em;
        padding-right: 0.5em;
    }
    .criterionContainer h3,
    .criterionContainer h4,
    .criterionContainer h5 {
        margin: 0; padding: 0;
    }
    .criterionContainerFirst {
        border-top: 1px solid #888;
    }
    .criterionContainerEven {
        background-color: #f9f9f9;
    }

    .assStepContainer {
        border-bottom: 1px solid #888;
        padding-top: 1em;
        padding-left: 0.5em;
        padding-right: 0.5em;
    }
    .assStepContainer h3,
    .assStepContainer h4,
    .assStepContainer h5 {
        margin: 0; padding: 0;
    }
    .assStepContainerFirst {
        border-top: 1px solid #888;
    }
    .assStepContainerEven {
        background-color: #f9f9f9;
    }




    .keywordLabel {
        font-size: 110%;
        margin-right: 1em;
        margin-bottom: 0.5em;
        display: inline-block;
    }
    .keywordRemoveLink, .keywordRemoveLink:active, .keywordRemoveLink:visited {
        color: inherit;
    }

    .citationTable {
    }
    .citationSourceIdentifier {
        width: 15em;
    }

    </style>

    <script type="text/javascript">
        var TRUSTMARK_DEFINITION = null;

        var DEPRECATED = false;

        var METADATA = null;

        var TERMS = null;

        var SOURCES = null;

        var CRITERIA = null;

        var KEYWORDS = null;

        var ASSESSMENT_STEPS = null;

        var PARAMETERS = null;

        /**
         * Javascript entry point, called after all initial data has been loaded and the document is ready.
         */
        $(document).ready(function(){
            console.log("document.ready firing...");

            if (!String.prototype.trim) {
                String.prototype.trim = function () {
                    return this.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, '');
                };
            }

            // TODO ${raw(tdJson)} ;

            initHandlers();

            loadData(TRUSTMARK_DEFINITION);

            setTimeout('displayAssessmentSteps()', 200);
        })

        function displayMetadata(){
            displayTab('tabsMetadata');
        }
        function displayAssessmentSteps(){
            displayTab('tabsAssessmentSteps');
        }
        function displayConformanceCriteria(){
            displayTab('tabsConformanceCriteria');
        }
        function displaySources(){
            displayTab('tabsSources');
        }
        function displayTerms(){
            displayTab('tabsTerms');
        }
        function displayTab(id){
            $('#'+id+' a').tab('show');
        }

        /**
         * Initializes any special javascript handlers on elements that need to be setup.
         */
        function initHandlers(){
            $('#metadataContainer').on('hidden.bs.collapse', function () {
                console.log("Metadata container is NOT visible...")
//                    $('#metadataContainerControllerStatus').removeClass('glyphicon glyphicon-collapse-down');
//                    $('#metadataContainerControllerStatus').addClass('glyphicon glyphicon-expand');
                $('#metadataContainerControllerStatus').removeClass('glyphicon glyphicon-triangle-bottom');
                $('#metadataContainerControllerStatus').addClass('glyphicon glyphicon-triangle-right');

            });
            $('#metadataContainer').on('shown.bs.collapse', function () {
                console.log("Metadata container is visible...")
//                    $('#metadataContainerControllerStatus').removeClass('glyphicon glyphicon-expand');
//                    $('#metadataContainerControllerStatus').addClass('glyphicon glyphicon-collapse-down');
                $('#metadataContainerControllerStatus').removeClass('glyphicon glyphicon-triangle-right');
                $('#metadataContainerControllerStatus').addClass('glyphicon glyphicon-triangle-bottom');

            });
        }//end initHandlers()


        function saveTrustmarkDefinition() {
            console.log("Saving Trustmark Definition...");
            if( !doValidation() )
                return;
            var jsonString = JSON.stringify(TRUSTMARK_DEFINITION, null, 4);
            $.ajax({
                url: '${createLink(controller: 'versionSetEdit', action: 'saveTrustmarkDefinition', id: vs.name, params: [linkId: linkId])}',
                dataType: 'json',
                method: 'POST',
                data: jsonString,
                contentType: 'application/json',
                success: function(result){
                    if( result && result.status ){
                        if( result.status === "SUCCESS" ){
                            window.location = result.forwardUrl;
                        }else{
                            console.log("Error: "+result.message);
                            $('#feedbackWindow').html('<div class="alert alert-danger" style="margin-top: 2em; margin-bottom: 2em;">'+result.message+'</div>');
                        }
                    }else{
                        console.log("Invalid response from server!");
                        $('#feedbackWindow').html('<div class="alert alert-danger" style="margin-top: 2em; margin-bottom: 2em;">The server had an unknown problem handling this TD.  Please try again, or contact support.</div>');
                    }
                },
                error: function(){
                    console.log("Error!");
                }
            })
        }


        /**
         * Updates the entire view, based on the current TD Model.  Probably not what you want if the page has
         * already been uploaded.
         */
        function updateView() {
            console.log("Updating view...");
            updateMetadata();
            updateKeywords();
            updateTerms();
            updateSources();
            updateCriteria();
            updateAssessmentSteps();
            console.log("Updating view complete!");
        }//end updateView()

        /**
         * Loads the data present in the given trustmark definition object, and updates the view accordingly.
         */
        function loadData(td){
            console.log("Loading Data...");

            if( td )
                TERMS = td.Terms;
            if( TERMS == null ) { TERMS = new Array(); }

            if( td )
                SOURCES = td.Sources;
            if( SOURCES == null ) { SOURCES = new Array(); }

            if( td )
                CRITERIA = td.ConformanceCriteria;
            if( CRITERIA == null ) { CRITERIA = new Array(); }

            if( td )
                ASSESSMENT_STEPS = td.AssessmentSteps;
            if( ASSESSMENT_STEPS == null ) { ASSESSMENT_STEPS = new Array(); }

            if( td && td.Metadata )
                KEYWORDS = td.Metadata.Keywords;
            if( KEYWORDS == null ) { KEYWORDS = new Array(); }

            PARAMETERS = new Array();
            if( td ) {
                for (var i = 0; i < td.AssessmentSteps.length; i++) {
                    var assStep = td.AssessmentSteps[i];
                    if (assStep && assStep.ParameterDefinitions) {
                        for (var j = 0; j < assStep.ParameterDefinitions.length; j++) {
                            var param = assStep.ParameterDefinitions[j];
                            PARAMETERS.push(param);
                        }
                    }
                }
            }

            if( td && td.Metadata )
                DEPRECATED = td.Metadata.Deprecated;
            else
                DEPRECATED = false;

            fillMetadata(td);

            updateView();

            console.log("Load Data complete!");
        }//end loadData()

        /**
         * Rebuilds the TrustmarkDefinition object from the form based data.
         */
        function saveDataToJSON() {
            TRUSTMARK_DEFINITION                 = {};
            TRUSTMARK_DEFINITION["$TMF_VERSION"] = "1.1";
            TRUSTMARK_DEFINITION["$Type"]        = "TrustmarkDefinition";

            TRUSTMARK_DEFINITION.ConformanceCriteriaPreface   = $('#conformanceCriteriaPreface').val();
            TRUSTMARK_DEFINITION.AssessmentStepsPreface       = $('#assStepPreface').val();
            TRUSTMARK_DEFINITION.IssuanceCriteria             = $('#issuanceCriteria').val();

            TRUSTMARK_DEFINITION.Metadata                                   = {};
            TRUSTMARK_DEFINITION.Metadata.Deprecated                        = DEPRECATED;
            TRUSTMARK_DEFINITION.Metadata.Name                              = $('#tdName').val();
            TRUSTMARK_DEFINITION.Metadata.PublicationDateTime               = $('#tdPublicationDate').val();
            TRUSTMARK_DEFINITION.Metadata.Version                           = $('#tdVersion').val();
            TRUSTMARK_DEFINITION.Metadata.Identifier                        = $('#identifier').val();
            TRUSTMARK_DEFINITION.Metadata.TrustmarkReferenceAttributeName   = $('#refAttribute').val();
            TRUSTMARK_DEFINITION.Metadata.Description                       = $('#tdDescription').val();
            TRUSTMARK_DEFINITION.Metadata.TargetStakeholderDescription      = $('#TargetStakeholderDescription').val();
            TRUSTMARK_DEFINITION.Metadata.TargetProviderDescription         = $('#TargetProviderDescription').val();
            TRUSTMARK_DEFINITION.Metadata.TargetRelyingPartyDescription     = $('#TargetRelyingPartyDescription').val();
            TRUSTMARK_DEFINITION.Metadata.TargetRecipientDescription        = $('#TargetRecipientDescription').val();
            TRUSTMARK_DEFINITION.Metadata.ProviderEligibilityCriteria       = $('#ProviderEligibilityCriteria').val();
            TRUSTMARK_DEFINITION.Metadata.AssessorQualificationsDescription = $('#AssessorQualificationsDescription').val();
            TRUSTMARK_DEFINITION.Metadata.ExtensionDescription              = $('#ExtensionDescription').val();
            TRUSTMARK_DEFINITION.Metadata.TrustmarkRevocationCriteria       = $('#TrustmarkRevocationCriteria').val();
            TRUSTMARK_DEFINITION.Metadata.LegalNotice                       = $('#LegalNotice').val();
            TRUSTMARK_DEFINITION.Metadata.Notes                             = $('#Notes').val();
            TRUSTMARK_DEFINITION.Metadata.Keywords                          = KEYWORDS;

            TRUSTMARK_DEFINITION.Metadata.TrustmarkDefiningOrganization            = {};
            TRUSTMARK_DEFINITION.Metadata.TrustmarkDefiningOrganization.Name       = $('#tdDefinitionOrgName').val();
            TRUSTMARK_DEFINITION.Metadata.TrustmarkDefiningOrganization.Identifier = $('#tdDefinitionOrgId').val();

            var contact = {};
            contact.Responder      = $('#tdDefiningOrgResponder').val();
            contact.Email          = $('#tdDefiningOrgEmail').val();
            contact.Telephone      = $('#tdDefiningOrgPhone').val();
            contact.MailingAddress = $('#tdDefiningOrgMailingAddr').val();
            contact.Notes          = $('#tdDefiningOrgNotes').val();
            TRUSTMARK_DEFINITION.Metadata.TrustmarkDefiningOrganization.PrimaryContact = contact;


            TRUSTMARK_DEFINITION.AssessmentSteps = ASSESSMENT_STEPS;
            TRUSTMARK_DEFINITION.ConformanceCriteria = CRITERIA;
            TRUSTMARK_DEFINITION.Terms = TERMS;
            TRUSTMARK_DEFINITION.Sources = SOURCES;

            var supersedesObjects = new Array();
            var supersedesVal = $('#Supersedes').val();
            if( supersedesVal && supersedesVal.trim().length > 0 ){
                var supersedesValues = supersedesVal.split("\n");
                if( supersedesValues && supersedesValues.length > 0 ){
                    for( var i = 0; i < supersedesValues.length; i++ ){
                        supersedesObjects.push({Identifier: supersedesValues[i]});
                    }
                }
            }
            var supersededByObjects = new Array();
            var supersededByVal = $('#supersededBy').val();
            if( supersededByVal && supersededByVal.trim().length > 0 ){
                var supersededByValues = supersededByVal.split("\n");
                if( supersededByValues && supersededByValues.length > 0 ){
                    for( var i = 0; i < supersededByValues.length; i++ ){
                        supersededByObjects.push({Identifier: supersededByValues[i]});
                    }
                }
            }

            if( supersedesObjects.length > 0 || supersededByObjects.length > 0 ){
                TRUSTMARK_DEFINITION.Metadata.Supersessions = {};
                if( supersedesObjects.length > 0 ){
                    TRUSTMARK_DEFINITION.Metadata.Supersessions.Supersedes = supersedesObjects;
                }
                if( supersededByObjects.length > 0 ){
                    TRUSTMARK_DEFINITION.Metadata.Supersessions.SupersededBy = supersededByObjects;
                }
            }

        }
        //==========================================================================================================
        // Menu Callbacks
        //==========================================================================================================
        /**
         * A menu callback which will prompt and reset creating a new TD.
         */
        function menuFileNew() {
            if( confirm('Really start a new TD? ALL data will be lost!') ){
                console.log("Creating a new TD...");
                fillMetadata({});

                DEPRECATED = false;

                TERMS = new Array();
                SOURCES = new Array();
                CRITERIA = new Array();
                KEYWORDS = new Array();
                ASSESSMENT_STEPS = new Array();
                PARAMETERS = new Array();

                updateView();
            }
        }

        /**
         * A helper for testing
         */
        function menuAutofillTestData() {
            $('#tdName').val('Password Complexity Requirements');
            $('#tdPublicationDate').val('2017-01-17');
            $('#tdVersion').val('1.0-SNAPSHOT');
            $('#identifier').val('https://artifacts.trustmarkinitiative.org/lib/trustmark-definitions/password-complexity-requirements/1.0-SNAPSHOT/');
            $('#refAttribute').val('https://artifacts.trustmarkinitiative.org/lib/trustmark-definitions/password-complexity-requirements/1.0-SNAPSHOT/reference');
            $('#tdDefinitionOrgName').val('Georgia Tech Research Institute');
            $('#tdDefinitionOrgId').val('https://trustmarks.gtri.org/');
            $('#tdDefiningOrgResponder').val('Trustmark Framework');
            $('#tdDefiningOrgEmail').val('TrustmarkFeedback@gtri.gatech.edu');
            $('#tdDescription').val('This Trustmark Definition defines conformance and assessment criteria for compliance with minimum security requirements for periodic review and update of interconnection security agreements as related to overall certification accreditation and security assessments requirements.');

            $('#sourceEditDialogIdentifier').val('source1');
            $('#sourceEditDialogReference').val('This would describe the source document that this identifier represents, such as a URL on the Internet where it can be found.');
            saveSource();

        }


        /**
         * Menu Callback to calculate the JSON represented currently and display it.
         */
        function menuFileDownloadJSON() {
            saveDataToJSON();
            var jsonString = JSON.stringify(TRUSTMARK_DEFINITION, null, 4);
            $('#mainContainer').html('<div style="margin-top: 4em;"><h1>Raw JSON: </h1><pre>'+htmlEncode(jsonString)+'</pre></div>');
        }//end menuFileDownloadJSON()

        /**
         * Menu callback to validate the current data and make sure everything required is present.  Calls the "doValidation()"
         * method to do the heavy lifting, a method that can be leveraged by other logic.
         */
        function doValidationMenuItem() {
            if( doValidation() ){
                $('#feedbackWindow').html('<div class="alert alert-success">This Trustmark Definition validates successfully.</div>');
            }
        }

        /**
         * Performs validation in javascript, returning true if valid and false if not.  If not, then the page is updated
         * to reflect the error.
         */
        function doValidation() {
            console.log("Performing in-page validation checks...");
            clearFeedback();
            saveDataToJSON();

            if( !validateMetadata() ){
                return false;
            }

            if( TRUSTMARK_DEFINITION.IssuanceCriteria.trim().length == 0 ){
                setValidationError("Issuance Criteria must have a value.  One example can be 'yes(all)'.");
                displayAssessmentSteps();
                $('#issuanceCriteria').focus();
                return false;
            }

            if( TRUSTMARK_DEFINITION.AssessmentSteps == null || TRUSTMARK_DEFINITION.AssessmentSteps.length == 0 ){
                setValidationError("At least 1 Assessment Step is required.");
                displayAssessmentSteps();
                return false;
            }

            return true;
        }//end doValidation()


        /**
         * Validates all the form data on the Metadata tab to make sure all required data is present and accounted for.
         */
        function validateMetadata(){

            if( TRUSTMARK_DEFINITION.Metadata.Name.trim().length == 0 ){
                setValidationError("Name must not be empty.");
                displayMetadata();
                $('#tdName').focus();
                return false;
            }

            if( TRUSTMARK_DEFINITION.Metadata.PublicationDateTime.trim().length == 0 ){
                setValidationError("Publication Date must not be empty.");
                displayMetadata();
                $('#tdPublicationDate').focus();
                return false;
            }
            var pubDate = Date.parse(TRUSTMARK_DEFINITION.Metadata.PublicationDateTime);
            console.log("Date timestamp = "+pubDate);
            if( pubDate == null || isNaN(pubDate) ){
                setValidationError("Publication Date must match yyyy-mm-dd, like 2017-01-16");
                displayMetadata();
                $('#tdPublicationDate').focus();
                return false;
            }


            if( TRUSTMARK_DEFINITION.Metadata.Version.trim().length == 0 ){
                setValidationError("Version must not be empty.");
                displayMetadata();
                $('#tdVersion').focus();
                return false;
            }

            if( TRUSTMARK_DEFINITION.Metadata.Identifier.trim().length == 0 ){
                setValidationError("Identifier must not be empty.");
                displayMetadata();
                $('#identifier').focus();
                return false;
            }
            if( !validateURL(TRUSTMARK_DEFINITION.Metadata.Identifier) ){
                setValidationError("Identifier must be a valid URL (ie, internet address).");
                displayMetadata();
                $('#identifier').focus();
                return false;
            }
            if( TRUSTMARK_DEFINITION.Metadata.TrustmarkReferenceAttributeName.trim().length == 0 ){
                setValidationError("Reference Attribute must not be empty.");
                displayMetadata();
                $('#refAttribute').focus();
                return false;
            }
            if( !validateURL(TRUSTMARK_DEFINITION.Metadata.TrustmarkReferenceAttributeName) ){
                setValidationError("Reference Attribute must be a valid URL (ie, internet address).");
                displayMetadata();
                $('#refAttribute').focus();
                return false;
            }
            if( TRUSTMARK_DEFINITION.Metadata.TrustmarkDefiningOrganization.Name.trim().length == 0 ){
                setValidationError("Defining Organization Name must not be empty.");
                displayMetadata();
                $('#tdDefinitionOrgName').focus();
                return false;
            }

            if( TRUSTMARK_DEFINITION.Metadata.TrustmarkDefiningOrganization.Identifier.trim().length == 0 ){
                setValidationError("Defining Organization URI must not be empty.");
                displayMetadata();
                $('#tdDefinitionOrgId').focus();
                return false;
            }
            if( TRUSTMARK_DEFINITION.Metadata.TrustmarkDefiningOrganization.PrimaryContact.Responder.trim().length == 0 ){
                setValidationError("Defining Organization Responder must not be empty.");
                displayMetadata();
                $('#tdDefiningOrgResponder').focus();
                return false;
            }
            if( TRUSTMARK_DEFINITION.Metadata.TrustmarkDefiningOrganization.PrimaryContact.Email.trim().length == 0 ){
                setValidationError("Defining Organization Email must not be empty.");
                displayMetadata();
                $('#tdDefiningOrgEmail').focus();
                return false;
            }
            if( !validateEmail(TRUSTMARK_DEFINITION.Metadata.TrustmarkDefiningOrganization.PrimaryContact.Email.trim()) ){
                setValidationError("Defining Organization Email must be a valid email address, ie somebody@somewhere.com");
                displayMetadata();
                $('#tdDefiningOrgEmail').focus();
                return false;
            }
            if( TRUSTMARK_DEFINITION.Metadata.Description.trim().length == 0 ){
                setValidationError("Description must not be empty.");
                displayMetadata();
                $('#tdDescription').focus();
                return false;
            }

            return true;
        }

        // Validates URL values
        function validateURL(str) {
            // @see https://stackoverflow.com/a/22648406/563328
            var urlRegex = '^(?!mailto:)(?:(?:http|https|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?:(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[0-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})))|localhost)(?::\\d{2,5})?(?:(/|\\?|#)[^\\s]*)?$';
            var url = new RegExp(urlRegex, 'i');
            return str.length < 2083 && url.test(str);
        }

        // Validates Email Values
        function validateEmail(email) {
            var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
            return re.test(email);
        }

        // Simply clears the feedback window at the top of the page.
        function clearFeedback() {
            $('#feedbackWindow').html('');
        }

        // Displays an error message to the user.
        function setValidationError(msg) {
            $('#feedbackWindow').html('<div class="alert alert-danger">'+msg+'</div>');
        }
        //==========================================================================================================
        // Deprecated Changes
        //==========================================================================================================
        /**
         * Called to switch the value of DEPRECATED, and update the view.
         */
        function toggleDeprecated() {
            DEPRECATED = !DEPRECATED;
            updateDeprecated();
        }
        /**
         * Analyzes the "DEPRECATED" boolean and updates the view accordingly.
         */
        function updateDeprecated() {
            if( DEPRECATED ){
                $('#deprecatedIndicator').addClass('glyphicon-ok');
                $('#deprecatedIndicator').removeClass('glyphicon-remove');
                $('#deprecatedText').html("Deprecated");
                $('#supersedByContainer').show();
            }else{
                $('#deprecatedIndicator').removeClass('glyphicon-ok');
                $('#deprecatedIndicator').addClass('glyphicon-remove');
                $('#deprecatedText').html("Not Deprecated");
                $('#supersedByContainer').hide();
            }
        }//end updateDeprecated()
        /**
         * Updates the view to match the metadata object.
         */
        function updateMetadata(){
            $('#conformanceCriteriaPreface').val(valOrEmpty(METADATA.ConformanceCriteriaPreface));
            $('#assStepPreface').val(valOrEmpty(METADATA.AssessmentStepsPreface));
            $('#issuanceCriteria').val(valOrEmpty(METADATA.IssuanceCriteria));

            $('#tdName').val(valOrEmpty(METADATA.Name));
            $('#tdPublicationDate').val(valOrEmpty(METADATA.PublicationDateTime));
            $('#tdVersion').val(valOrEmpty(METADATA.Version));
            $('#identifier').val(valOrEmpty(METADATA.Identifier));
            $('#refAttribute').val(valOrEmpty(METADATA.TrustmarkReferenceAttributeName));
            $('#tdDescription').val(valOrEmpty(METADATA.Description));

            $('#tdDefinitionOrgName').val(valOrEmpty(METADATA.TrustmarkDefiningOrganization.Name));
            $('#tdDefinitionOrgId').val(valOrEmpty(METADATA.TrustmarkDefiningOrganization.Identifier));

            var contact = null;
            if( METADATA.TrustmarkDefiningOrganization.PrimaryContact ){
                contact = METADATA.TrustmarkDefiningOrganization.PrimaryContact;
            }else{
                // TODO Any other way to fill contact?
            }
            if( contact ){
                $('#tdDefiningOrgResponder').val(valOrEmpty(contact.Responder));
                $('#tdDefiningOrgEmail').val(valOrEmpty(getFirstEmail(contact)));
                $('#tdDefiningOrgPhone').val(valOrEmpty(getFirstTelephone(contact)));
                $('#tdDefiningOrgMailingAddr').val(valOrEmpty(getFirstMailingAddress(contact)));
                $('#tdDefiningOrgNotes').val(valOrEmpty(contact.Notes));
            }else{
                $('#tdDefiningOrgResponder').val('');
                $('#tdDefiningOrgEmail').val('');
                $('#tdDefiningOrgPhone').val('');
                $('#tdDefiningOrgMailingAddr').val('');
                $('#tdDefiningOrgNotes').val('');
            }

            if( METADATA.Supersessions && METADATA.Supersessions.Supersedes && METADATA.Supersessions.Supersedes.length > 0 ){
                var supersedesString = '';
                for( var i = 0; i < METADATA.Supersessions.Supersedes.length; i++ ){
                    supersedesString += METADATA.Supersessions.Supersedes[i].Identifier;
                    if( i < (METADATA.Supersessions.Supersedes.length - 1) ){
                        supersedesString += '\n';
                    }
                }
                $('#Supersedes').val(supersedesString);
            }

            if( METADATA.Supersessions && METADATA.Supersessions.SupersededBy && METADATA.Supersessions.SupersededBy.length > 0 ){
                var supersededByString = '';
                for( var i = 0; i < METADATA.Supersessions.SupersededBy.length; i++ ){
                    supersededByString += METADATA.Supersessions.SupersededBy[i].Identifier;
                    if( i < (METADATA.Supersessions.SupersededBy.length - 1) ){
                        supersededByString += '\n';
                    }
                }
                $('#supersededBy').val(supersededByString);
            }



            $('#TargetStakeholderDescription').val(valOrEmpty(METADATA.TargetStakeholderDescription));
            $('#TargetProviderDescription').val(valOrEmpty(METADATA.TargetProviderDescription));
            $('#TargetRelyingPartyDescription').val(valOrEmpty(METADATA.TargetRelyingPartyDescription));
            $('#TargetRecipientDescription').val(valOrEmpty(METADATA.TargetRecipientDescription));
            $('#ProviderEligibilityCriteria').val(valOrEmpty(METADATA.ProviderEligibilityCriteria));
            $('#AssessorQualificationsDescription').val(valOrEmpty(METADATA.AssessorQualificationsDescription));
            $('#ExtensionDescription').val(valOrEmpty(METADATA.ExtensionDescription));
            $('#TrustmarkRevocationCriteria').val(valOrEmpty(METADATA.TrustmarkRevocationCriteria));
            $('#LegalNotice').val(valOrEmpty(METADATA.LegalNotice));
            $('#Notes').val(valOrEmpty(METADATA.Notes));

            updateDeprecated();
            // TODO Supersedes
            // TODO Superseded By

        }//end updateMetadata()


        function getFirstEmail(contact){
            if( contact && contact.Email ){
                return contact.Email;
            }else if( contact && contact.Emails ){
                return contact.Emails[0];
            }else{
                return null;
            }
        }

        function getFirstMailingAddress(contact){
            if( contact && contact.MailingAddress ){
                return contact.MailingAddress;
            }else if( contact && contact.MailingAddresses ){
                return contact.MailingAddresses[0];
            }else{
                return null;
            }
        }

        function getFirstTelephone(contact){
            if( contact && contact.Telephone ){
                return contact.Telephone;
            }else if( contact && contact.Telephones ){
                return contact.Telephones[0];
            }else{
                return null;
            }
        }


        /**
         * Takes the metadata from the given TD and puts it into the form fields.
         */
        function fillMetadata(td){
            METADATA = {};
            METADATA.TrustmarkDefiningOrganization = {};
            if( !td ){return;}
            if( td.Metadata && "object" == typeof td.Metadata ){
                METADATA = doClone(td.Metadata);
            }

            METADATA['ConformanceCriteriaPreface'] = valOrEmpty(td.ConformanceCriteriaPreface);
            METADATA['AssessmentStepsPreface'] = valOrEmpty(td.AssessmentStepsPreface);
            METADATA['IssuanceCriteria'] = valOrEmpty(td.IssuanceCriteria);

            // TODO Deprecated
            // TODO Supersedes
            // TODO Superseded By

        }//end fillMetadata()


        function doClone(obj) {
            if (null == obj || "object" != typeof obj) return obj;
            var copy = obj.constructor();
            for (var attr in obj) {
                if (obj.hasOwnProperty(attr)) copy[attr] = doClone(obj[attr]);
            }
            return copy;
        }
        //==========================================================================================================
        // Keywords
        //==========================================================================================================
        /**
         * Returns a sorted version of the Keywords Array.
         */
        function getSortedKeywords(){
            KEYWORDS.sort(sortStrings);
            return KEYWORDS;
        }//end getSortedKeywords()
        /**
         * Will prompt the user for and add a new keyword to the list.
         */
        function addNewKeyword(){
            var keyword = prompt("Please enter your new keyword: ");
            if (/\S/.test(keyword)) {
                if( KEYWORDS && KEYWORDS.length > 0 ){
                    for( var i = 0; i < KEYWORDS.length; i++ ){
                        if( keyword == KEYWORDS[i] ){
                            alert("That keyword already exists.");
                            return;
                        }
                    }
                }
                KEYWORDS.push(keyword);
                updateKeywords();
            }else{
                alert("Keyword must have a value.");
            }
        }//end addNewKeyword()
        /**
         * Removes the value at in the KEYWORDS array at the given index.  If the index has a bad value, then no changes
         * will occur.
         */
        function removeKeyword(index){
            var newKeywords = new Array();
            if( KEYWORDS && KEYWORDS.length > 0 ){
                for( var i = 0; i < KEYWORDS.length; i++ ){
                    if( i != index ){
                        newKeywords.push(KEYWORDS[i]);
                    }
                }
            }
            KEYWORDS = newKeywords;
            updateKeywords();
        }//end removeKeyword()
        /**
         * Updates the view associated with keywords.
         */
        function updateKeywords(){
            var html = '';
            var keys = getSortedKeywords();
            if( keys != null && keys.length > 0 ){
                for( var index = 0; index < keys.length; index++ ){
                    var nextKeyword = keys[index];
                    html += '<span class="label label-default keywordLabel">'+nextKeyword+' | <a href="javascript:removeKeyword('+index+')" class="keywordRemoveLink" title="Click to remove this keyword."><span class="glyphicon glyphicon-remove"></span></a></span>'
                }
            }else{
                html += '<em>There are no keywords.</em>'
            }
            $('#keywordsContainer').html(html);
        }//end updateKeywords()
        //==========================================================================================================
        // Terms
        //==========================================================================================================
        var TERM_INDEX = -1;

        /**
         * Returns a sorted version of the Terms Array.
         */
        function getSortedTerms(){
            TERMS.sort(sortByName);
            return TERMS;
        }//end getSortedTerms()
        /**
         * Asserts the Terms window matches the model, and updates it if it does not.
         */
        function updateTerms() {
            var html = '';

            html += '<table class="table table-condensed table-striped table-bordered">'
            html += ' <thead>\n';
            html += '  <tr><th class="termControlsContainer">&nbsp;</th><th class="termNameContainer">Name</th><th class="termAbbrContainer">Abbreviations</th><th class="termDefinitionContainer">Definition</th></tr>\n';
            html += ' </thead>\n';
            html += ' <tbody>\n';

            var localTerms = getSortedTerms();
            if( localTerms != null && localTerms.length > 0 ){
                for( var i = 0; i < localTerms.length; i++ ){
                    var term = localTerms[i];
                    html += '<tr>';
                    html += '<td class="termControlsContainer">';
                    html += '  <a href="javascript:editTerm('+i+')" title="Edit this term"><span class="glyphicon glyphicon-pencil"></span></a>';
                    html += '  &nbsp;';
                    html += '  <a href="javascript:deleteTerm('+i+')" title="Delete this term"><span class="glyphicon glyphicon-remove"></span></a>';
                    html += '</td>\n';
                    html += '<td class="termNameContainer">'+term.Name+'</td>\n';
                    if( term.Abbreviations != null && term.Abbreviations.length > 0 ){
                        var abbreviationsList = "";
                        for( var j = 0; j < term.Abbreviations.length; j++ ){
                            abbreviationsList += term.Abbreviations[j];
                            if( j < term.Abbreviations.length - 1 )
                                abbreviationsList += ", ";
                        }
                        html += '<td class="termAbbrContainer">'+abbreviationsList+'</td>\n';
                    }else{
                        html += '<td class="termAbbrContainer">&nbsp;</td>\n';
                    }
                    html += '<td class="termDefinitionContainer">'+term.Definition+'</td>\n';
                    html += '</tr>';
                }
                $('#termsCount').html(TERMS.length);
            }else{
                html += '<tr><td colspan="4"><em>There are no terms to display.</em></td></tr>\n';
                $('#termsCount').html(0);
            }
            html += ' </tbody>\n';
            html += '</table>';

            $('#termsContainer').html(html);

        }//end updateTerms()

        /**
         * Opens the term dialog for creating a new term.
         */
        function createNewTerm(){
            TERM_INDEX = -1;
            $('#termEditDialogName').val('');
            $('#termEditDialogDefinition').val('');
            $('#termEditDialogAbbreviations').html('<em>There are no abbreviations.</em>');
            $('#termEditDialogAbbreviations').removeClass("HasAbbreviations");

            $('#termEditDialog').modal('show');
        }//end createNewTerm()

        /**
         * Given an index from the TERMS array, this method will setup the Term Edit dialog with that term's information
         * and allow you to change the values.
         */
        function editTerm(index){
            var term = TERMS[index];
            if( term ) {
                TERM_INDEX = index;
                $('#termEditDialogName').val(term.Name);
                $('#termEditDialogDefinition').val(term.Definition);

                var html = '';
                if( term.Abbreviations && term.Abbreviations.length > 0 ){
                    for( var i = 0; i < term.Abbreviations.length; i++ ){
                        var id = getUniqueDocumentId();

                        html += '<div id="'+id+'_Container" class="abbreviationContainer" style="margin-bottom: 0.5em;">\n';
                        html += '  <input type="text" class="form-control" style="width: 20%; display: inline-block;" placeholder="Abbr." value="'+term.Abbreviations[i]+'" />\n';
                        html += '  <a href="javascript:termEditDialogRemoveAbbreviation(\''+id+'\')"><span class="glyphicon glyphicon-remove"></span></a>\n';
                        html += '</div>\n';
                    }

                    $('#termEditDialogAbbreviations').addClass("HasAbbreviations");
                }else{
                    html += '<em>There are no abbreviations</em>';
                    $('#termEditDialogAbbreviations').removeClass("HasAbbreviations");
                }
                $('#termEditDialogAbbreviations').html(html);

                $('#termEditDialog').modal('show');
            }
        }

        /**
         * Once the TermEditDialog is open, if this method is called a new abbreviation Input will be added to the list.
         */
        function termEditDialogAddAbbreviation() {
            var html = '';

            if( !($('#termEditDialogAbbreviations').hasClass("HasAbbreviations")) ){
                $('#termEditDialogAbbreviations').html('');
                $('#termEditDialogAbbreviations').addClass("HasAbbreviations");
            }

            var id = getUniqueDocumentId();

            html += '<div id="'+id+'_Container" class="abbreviationContainer" style="margin-bottom: 0.5em;">\n';
            html += '  <input type="text" class="form-control" style="width: 20%; display: inline-block;" placeholder="Abbr." />\n';
            html += '  <a href="javascript:termEditDialogRemoveAbbreviation(\''+id+'\')"><span class="glyphicon glyphicon-remove"></span></a>\n';
            html += '</div>\n';

            $('#termEditDialogAbbreviations').append(html);
        }

        /**
         * Called to remove an Abbreviation from the TermEditDialog entry.
         */
        function termEditDialogRemoveAbbreviation(id) {
            $('#'+id+"_Container").remove();

            if( $('.abbreviationContainer').length == 0 ){
                $('#termEditDialogAbbreviations').html('<em>There are no abbreviations.</em>');
                $('#termEditDialogAbbreviations').removeClass("HasAbbreviations");
            }
        }

        /**
         * This method is invoked from the "Save" button on the term edit dialog.
         */
        function saveTerm() {
            $('#termEditDialogFeedback').html('');
            /*
             {
             "Abbreviations" : ["TEST1", "TEST2"],
             "Definition": "Official with the authority to formally assume responsibility for operating an information system at an acceptable level of risk to organization operations (including mission, functions, image, or reputation), organization assets, or individuals. Synonymous with Accreditation Authority.",
             "Name": "Authorizing Official"
             }
             */
            var name = $('#termEditDialogName').val();
            if( name )
                name = name.trim();
            else
                name = "";

            if( name === "" ){
                console.log("Name is required!");
                $('#termEditDialogFeedback').html('<div class="alert alert-danger">Name is a required field.</div>');
                return false;
            }

            var def = $('#termEditDialogDefinition').val();
            if( def )
                def = def.trim();
            else
                def = "";

            if( def === "" ){
                console.log("Definition is required!");
                $('#termEditDialogFeedback').html('<div class="alert alert-danger">Definition is a required field.</div>');
                return false;
            }

            var abbrArray = new Array();
            var abbrContainers = $('.abbreviationContainer');
            if( abbrContainers.length > 0 ){
                for( var i = 0; i < abbrContainers.length; i++ ){
                    var abbrValue = $(abbrContainers[i]).find(".form-control").val();
                    if( abbrValue.trim() === '' ){
                        console.log("Abbreviation at index "+i+" has no value.");
                        $('#termEditDialogFeedback').html('<div class="alert alert-danger">All abbreviation inputs must have a value.</div>');
                        return false;
                    }
                    abbrArray.push(abbrValue);
                }
            }

            if( TERM_INDEX == -1 ){  // Since this is a new term, we check for a name collision...
                var termIndex = findTerm(name);
                if( termIndex > -1 ){
                    console.log("Term with name '"+name+"' already exists!");
                    $('#termEditDialogFeedback').html('<div class="alert alert-danger">A term with name "'+name+'" already exists.</div>');
                    return false;
                }
            }else{
                var termIndex = findTermExcluding(name, TERM_INDEX);
                if( termIndex > -1 ){
                    console.log("Term with name '"+name+"' already exists!");
                    $('#termEditDialogFeedback').html('<div class="alert alert-danger">A term with name "'+name+'" already exists.</div>');
                    return false;
                }

                // We must remove the old term from the TERMS array before we add back this new one.
                removeTerm(TERM_INDEX);
            }


            $('#termEditDialog').modal('hide');
            TERMS.push({"Name" : name, "Definition": def, "Abbreviations": abbrArray});
            updateTerms();
        }//end saveTerm()

        /**
         * Given a term name, this method will find that term's index in the TERMS array
         */
        function findTerm(name){
            for( var i = 0; i < TERMS.length; i++ ){
                var nextTerm = TERMS[i];
                if( nextTerm.Name.toUpperCase() === name.toUpperCase() ){
                    return i;
                }
            }
            return -1;
        }
        function findTermExcluding(name, exclusionIndex){
            for( var i = 0; i < TERMS.length; i++ ){
                var nextTerm = TERMS[i];
                if( i != exclusionIndex ) {
                    if (nextTerm.Name.toUpperCase() === name.toUpperCase()) {
                        return i;
                    }
                }
            }
            return -1;
        }

        /**
         * Removes the term from the model, without updating the view.
         */
        function removeTerm(index){
            var NEW_TERMS = new Array();
            for( var i = 0; i < TERMS.length; i++ ){
                if( i != index ){
                    NEW_TERMS.push(TERMS[i]);
                }
            }
            TERMS = NEW_TERMS;
        }

        /**
         * Deletes the term from the model and then updates the view.
         */
        function deleteTerm(index){
            console.log("Removing term["+index+"]: "+TERMS[index].Name);
            removeTerm(index);
            updateTerms();
        }

        //==========================================================================================================
        // Sources
        //==========================================================================================================
        var SOURCE_INDEX = -1;

        /**
         * Returns a sorted version of the Sources Array.
         */
        function getSortedSources(){
            SOURCES.sort(sortByIdentifier);
            return SOURCES;
        }//end getSortedSources()

        /**
         * Asserts the Terms window matches the model, and updates it if it does not.
         */
        function updateSources() {
            var html = '';

            html += '<table class="table table-condensed table-striped table-bordered">'
            html += ' <thead>\n';
            html += '  <tr><th class="sourceControlsContainer">&nbsp;</th><th class="sourceIdentifierContainer">Identifier</th><th class="sourceReferenceContainer">Reference</th></tr>\n';
            html += ' </thead>\n';
            html += ' <tbody>\n';

            var sources = getSortedSources();
            if( sources != null && sources.length > 0 ){
                for( var i = 0; i < sources.length; i++ ){
                    var source = sources[i];
                    html += '<tr>';
                    html += '<td class="sourceControlsContainer">';
                    html += '  <a href="javascript:editSource('+i+')" title="Edit this source"><span class="glyphicon glyphicon-pencil"></span></a>';
                    html += '  &nbsp;';
                    html += '  <a href="javascript:deleteSource('+i+')" title="Delete this source"><span class="glyphicon glyphicon-remove"></span></a>';
                    html += '</td>\n';
                    html += '<td class="sourceIdentifierContainer">'+source.Identifier+'</td>\n';
                    html += '<td class="sourceReferenceContainer">'+source.Reference+'</td>\n';
                    html += '</tr>';
                }
                $('#sourcesCount').html(SOURCES.length);
            }else{
                html += '<tr><td colspan="3"><em>There are no sources to display.</em></td></tr>\n';
                $('#sourcesCount').html(0);
            }
            html += ' </tbody>\n';
            html += '</table>';

            $('#sourcesContainer').html(html);

        }//end updateSources()

        /**
         * Called to create a new source
         */
        function createNewSource() {
            $('#sourceEditDialogFeedback').html('');
            $('#sourceEditDialogIdentifier').val('');
            $('#sourceEditDialogReference').val('');

            SOURCE_INDEX = -1;

            $('#sourceEditDialog').modal('show');
        }//end createNewSource()

        /**
         * Called to edit an existing source, given the index of hte source in the SOURCES array.
         */
        function editSource(index){
            var source = SOURCES[index];
            if( source ) {
                SOURCE_INDEX = index;
                $('#sourceEditDialogFeedback').html('');
                $('#sourceEditDialogIdentifier').val(source.Identifier);
                $('#sourceEditDialogReference').val(source.Reference);
                $('#sourceEditDialog').modal('show');
            }
        }

        /**
         * Callback from the Save button on the sourceEditDialog.
         */
        function saveSource(){
            var id = idValOrEmpty('sourceEditDialogIdentifier');
            if( id === "" ){
                console.log("Identifier is required!");
                $('#sourceEditDialogFeedback').html('<div class="alert alert-danger">Identifier is a required field.</div>');
                return false;
            }

            var ref = idValOrEmpty('sourceEditDialogReference');
            if( ref === "" ){
                console.log("Reference is required!");
                $('#sourceEditDialogFeedback').html('<div class="alert alert-danger">Reference is a required field.</div>');
                return false;
            }

            var sourceIdUnique = null;
            if( SOURCE_INDEX == -1 ){  // Since this is a new source, we check for an id collision...
                var sourceIndex = findSourceIndex(id);
                if( sourceIndex > -1 ){
                    console.log("A Source with Identifier '"+id+"' already exists!");
                    $('#sourceEditDialogFeedback').html('<div class="alert alert-danger">A Source with Identifier "'+id+'" already exists.</div>');
                    return false;
                }
                sourceIdUnique = "Source_"+getUniqueDocumentId();
            }else{
                var sourceIndex = findSourceIndexExcluding(id, SOURCE_INDEX);
                if( sourceIndex > -1 ){
                    console.log("A Source with Identifier '"+id+"' already exists!");
                    $('#sourceEditDialogFeedback').html('<div class="alert alert-danger">A Source with Identifier "'+id+'" already exists.</div>');
                    return false;
                }

                sourceIdUnique = SOURCES[SOURCE_INDEX].id;
                // We must remove the old source from the SOURCES array before we add back this new one.
                removeSource(SOURCE_INDEX);
            }


            $('#sourceEditDialog').modal('hide');
            SOURCES.push({"$id": sourceIdUnique, "Identifier" : id, "Reference": ref});
            updateSources();
        }

        /**
         * Relies on the findSourceIndex method, and returns the actual SOURCE object using that.
         */
        function findSource(id){
            var index = findSourceIndex(id);
            if( index >= 0 ){
                return SOURCES[index];
            }else{
                console.log("Could not find any source with index: "+index);
                return null;
            }
        }

        /**
         * Searches through the SOURCES array for a Source with the given Identifier or $id value.
         */
        function findSourceIndex(id){
            for( var i = 0; i < SOURCES.length; i++ ){
                var s = SOURCES[i];
                if( s.Identifier.toUpperCase() === id.toUpperCase() ||
                    s['$id'].toUpperCase() === id.toUpperCase() ){
                    return i;
                }
            }
            return -1;
        }
        function findSourceIndexExcluding(id, exclusionIndex){
            for( var i = 0; i < SOURCES.length; i++ ){
                var s = SOURCES[i];
                if( i != exclusionIndex ) {
                    if (s.Identifier.toUpperCase() === id.toUpperCase() ||
                        s['$id'].toUpperCase() === id.toUpperCase()) {
                        return i;
                    }
                }
            }
            return -1;
        }

        /**
         * Removes a source from the model, but does not update the view.
         */
        function removeSource(index){
            var NEW_SOURCES = new Array();
            for( var i = 0; i < SOURCES.length; i++ ){
                if( i != index ){
                    NEW_SOURCES.push(SOURCES[i]);
                }
            }
            SOURCES = NEW_SOURCES;
        }//end removeSource()

        /**
         * Removes the source from the model and then updates the view.
         */
        function deleteSource(i) {
            if( confirm('Really delete source "'+SOURCES[i].Identifier+'"?') ) {
                removeSource(i);
                updateSources();
                updateCriteria();
                updateAssessmentSteps();
            }
        }//end deleteSource()
        //==========================================================================================================
        // Conformance Criteria
        //==========================================================================================================
        /**
         * Used by the create/edit criterion methods (along with save) to determine if this criterion should be an
         * update or an insert.
         */
        var CRITERION_INDEX = -1;

        /**
         * Sorts the criteria by number and then returns them.
         */
        function getSortedCriteria() {
            CRITERIA.sort(sortByNumber)
            return CRITERIA;
        }

        /**
         * Called to make sure the view matches the criteria model.
         */
        function updateCriteria() {
            var html = "";

            html += '<div>';

            var criteria = getSortedCriteria();
            if( criteria != null && criteria.length > 0 ){
                for( var i = 0; i < criteria.length; i++ ){
                    html += formatCriterion(criteria[i], i);
                }
                $('#criteriaCount').html(criteria.length);
            }else{
                html += '<em>There is no conformance criteria to display.</em>'
                $('#criteriaCount').html('0');
            }

            html += '</div>';

            $('#criteriaContainer').html(html);

        }//end updateCriteria()

        /**
         * A method which formats the given criterion entry for HTML display.
         */
        function formatCriterion(criterion, index){
            var html = '';
            var className = 'criterionContainer';
            if( index == 0 ){
                className += ' criterionContainerFirst first';
            }
            if(index % 2 == 0 ){
                className += ' criterionContainerEven even';
            }else{
                className += ' criterionContainerOdd odd';
            }
            html += '<div class="'+className+'" onmouseenter="showCriterionControls('+index+')" onmouseleave="hideCriterionControls('+index+')">\n';
//                html += '    <h3>'+criterion.Number+") "+criterion.Name+"</h3>\n";
//                html += '    <h3>'+criterion.Name+"</h3>\n";
            html += '    <h3>'+criterion.Name;
            html += '        <div style="float: right; font-size: 90%;" id="critControls'+index+'">';
            html += '            <a href="javascript:moveCriterionUp('+index+')" title="Move this criterion up"><span class="glyphicon glyphicon-arrow-up"></span></a>';
            html += '               &nbsp;';
            html += '            <a href="javascript:moveCriterionDown('+index+')" title="Move this criterion down"><span class="glyphicon glyphicon-arrow-down"></span></a>';
            html += '               &nbsp;';
            html += '            <a href="javascript:editCriterion('+index+')" title="Edit this criterion"><span class="glyphicon glyphicon-pencil"></span></a>';
            html += '               &nbsp;';
            html += '            <a href="javascript:deleteCriterion('+index+')" title="Delete this criterion"><span class="glyphicon glyphicon-remove"></span></a>';
            html += '        </div>\n';
            html += '    </h3>\n';
            html += '    <div class="crterionDescriptionContainer">'+criterion.Description+'</div>\n';
            html += '    <div class="criterionSourcesContainer"><h4>Citations</h4>';
            html +=          formatCriterionCitations(criterion.Citations);
            html += '    </div>';
            html += '</div>\n\n';

            setTimeout('hideCriterionControls('+index+')', 250);

            return html;
        }//end formatCriterion()

        function hideCriterionControls(index){
            $('#critControls' + index).hide();
        }
        function showCriterionControls(index){
            $('#critControls' + index).show();
        }

        function formatCriterionCitations(citations){
            var html = '';
            if( citations && citations.length > 0 ) {
                html += '<table class="table table-striped table-condensed table-bordered citationTable">';
                for (var citationIndex = 0; citationIndex < citations.length; citationIndex++) {
                    var citation = citations[citationIndex];
                    html += formatCriterionCitation(citation);
                }
                html += '</table>';
            }else{
                html += '<em>There are no citations.</em>';
            }
            return html;
        }

        function formatCriterionCitation(citation){
            var html = '';
            var sourceId = getSourceIdFromCitation(citation);
            var source = getSourceById(sourceId);
            if( source ){
                html += '<tr>';
                html += '  <td class="citationSourceIdentifier">'+source.Identifier+'</td>';
                html += '  <td class="citationDescription">'+citation.Description+'</td>';
                html += '</tr>';
            }else{
                html += '<tr><td colspan="2"><em>Could not find source "'+sourceId+'" for this citation!</td></tr>';
            }

            return html;
        }

        function getSourceIdFromCitation(citation){
            if( citation && citation.Source && citation.Source['$ref'] ){
                return citation.Source['$ref'].substring(1);
            }else{
                console.log("could not get source id from citation!")
                return null;
            }
        }

        function moveCriterionUp(index){
            if( index > 0 ){
                CRITERIA[index].Number = index;
                CRITERIA[index-1].Number = index+1;
                updateCriteria();
            }
        }//end moveCriterionUp()

        function moveCriterionDown(index){
            if( index < (CRITERIA.length - 1) ) {
                CRITERIA[index].Number = index + 2;
                CRITERIA[index + 1].Number = index + 1;
                updateCriteria();
            }
        }//end moveCriterionDown()

        /**
         * Searches through the CRITERIA array for a criterion with an id matching the id given.
         */
        function findCriteriaById(id){
            console.log("findCriteriaById("+id+")...");
            var criterion = null;
            for( var i = 0; i < CRITERIA.length; i++ ){
                var current = CRITERIA[i];
                if( current['$id'].toUpperCase() === id.toUpperCase() ){
                    criterion = current;
                    break;
                }
            }
            return criterion;
        }//end findCriteriaById()

        /**
         * Called when the user clicks the "Create New" button on the Conformance Criteria page
         */
        function createNewCriterion(){
            CRITERION_INDEX = -1;
            $('#critEditDialogName').val('');
            $('#critEditDialogDescription').val('');
            $('#critEditDialogCitations').removeClass("HasCitations");
            $('#critEditDialogCitations').html('<em>There are no citations.</em>');

            $('#critEditDialog').modal('show');
        }

        /**
         * Callback for the pencil icon on the criterion modify functions
         */
        function editCriterion(index){
            var criterion = CRITERIA[index];
            console.log("Editing Criterion: "+JSON.stringify(criterion));
            CRITERION_INDEX = index;
            $('#critEditDialogName').val(criterion.Name);
            $('#critEditDialogDescription').val(criterion.Description);
            $('#critEditDialogCitations').addClass("HasCitations");
            var html = '';
            for( var citationIndex = 0; citationIndex < criterion.Citations.length; citationIndex++ ){
                var citation = criterion.Citations[citationIndex];
                var sourceId = getSourceIdFromCitation(citation);
                var source = findSource(sourceId);

                var id = getUniqueDocumentId();
                html += '<div id="'+id+'Container" class="citationContainer" style="margin-bottom: 0.5em;">\n';
                html += '    <select id="'+id+'SourceSelect" class="form-control sourceSelect" style="display: inline-block;  width: 25%;">';
                html +=          buildSourcesAsSelectOptions(source);
                html += '    </select>\n'
                html += '    <input type="text" class="form-control descriptionText" style="display: inline-block; width: 60%;" placeholder="Description..." value="'+citation.Description+'" />'
                html += '    <a href="javascript:deleteCitation(\''+id+'\')"><span class="glyphicon glyphicon-remove"></span></a>';
                html += '</div>\n\n';

            }
            $('#critEditDialogCitations').html(html);

            $('#critEditDialog').modal('show');
        }

        /**
         * Callback for the "Add" button in the citations section of the edit conformance criterion dialog.
         */
        function addCitation() {
            if( !($('#critEditDialogCitations').hasClass('HasCitations')) ){
                $('#critEditDialogCitations').html('');
                $('#critEditDialogCitations').addClass("HasCitations");
            }

            var id = getUniqueDocumentId();

            var html = "";
            html += '<div id="'+id+'Container" class="citationContainer" style="margin-bottom: 0.5em;">\n';
            html += '    <select id="'+id+'SourceSelect" class="form-control sourceSelect" style="display: inline-block;  width: 25%;">';
            html +=          buildSourcesAsSelectOptions(null);
            html += '    </select>\n'
            html += '    <input type="text" class="form-control descriptionText" style="display: inline-block; width: 60%;" placeholder="Description..." />'
            html += '    <a href="javascript:deleteCitation(\''+id+'\')"><span class="glyphicon glyphicon-remove"></span></a>';
            html += '</div>\n\n';


            $('#critEditDialogCitations').append(html);
        }//end addCitation()

        /**
         * Removes the given citation (by id) from the list
         */
        function deleteCitation(id){
            $('#'+id+'Container').remove();

            if( $('#critEditDialogCitations .citationContainer').length == 0 ){
                $('#critEditDialogCitations').html('<em>There are no citations.</em>');
                $('#critEditDialogCitations').removeClass("HasCitations");
            }
        }//end deleteCitation()

        /**
         * This method will build a set of HTML option elements from the current sources list, for the purposes of
         * displaying in the edit criteria dialog.  If optionalSource is given, then that option will be highlighted.
         */
        function buildSourcesAsSelectOptions(optionalSource) {
            var html = '';
            var sources = getSortedSources();
            if( sources && sources.length > 0 ) {
                for (var i = 0; i < sources.length; i++) {
                    var source = sources[i];
                    if( optionalSource && source.Identifier == optionalSource.Identifier ) {
                        html += '<option value="' + i + '" selected="selected">' + source.Identifier + '</option>';
                    }else{
                        html += '<option value="'+i+'">'+source.Identifier+'</option>';
                    }
                }
            }else{
                html += '<option value="-1">NO SOURCES</option>';
            }

            return html;
        }

        /**
         * Callback from the save button on the Edit Conformance Criterion dialog.
         */
        function saveCriterion() {
            console.log("Call to saveCriterion()");
            $('#critEditDialogFeedback').html('');

            var name = idValOrEmpty('critEditDialogName');
            if( name === "" ){
                console.log("Name is required!");
                $('#critEditDialogFeedback').html('<div class="alert alert-danger">Name is a required field.</div>');
                return false;
            }
            console.log("Name set to: "+name);

            var def = idValOrEmpty('critEditDialogDescription');
            if( def === "" ){
                console.log("Description is required!");
                $('#critEditDialogFeedback').html('<div class="alert alert-danger">Description is a required field.</div>');
                return false;
            }
            console.log("Description set to: "+def);

            if( $('#critEditDialog .citationContainer').length == 0 ){
                console.log("A Citation is required!");
                $('#critEditDialogFeedback').html('<div class="alert alert-danger">You must define a Citation.</div>');
                return false;
            }

            var citationsArray = new Array();
            var citationsJQuery = $('#critEditDialog .citationContainer');
            for( var i = 0; i < citationsJQuery.length; i++ ){
                var citationJQuery = $(citationsJQuery[i]);
                var sourceIndex = citationJQuery.find(".sourceSelect").val();
                var descriptionText = citationJQuery.find(".descriptionText").val();
                if( descriptionText == null ){ descriptionText = ""; }
                descriptionText = descriptionText.trim();

                if( sourceIndex == -1 ){
                    console.log("A Citation is required, but given -1 index.  This means the user has not defined sources yet.");
                    $('#critEditDialogFeedback').html('<div class="alert alert-danger">You must define a Source to be used a citation.</div>');
                    return false;
                }

                var source = SOURCES[sourceIndex];

                if( descriptionText === "" ){
                    console.log("A Citation requires a description");
                    $('#critEditDialogFeedback').html('<div class="alert alert-danger">All Citations require a description.</div>');
                    return false;
                }

                console.log("Creating citation: source = $ref{"+source['$id']+"}, description: "+descriptionText);
                var citation = {
                    "Source" : { "$ref"  : "#"+source['$id'] },
                    "Description" : descriptionText
                };
                citationsArray.push(citation);
            }

            var uniqueCriteriaId = null;
            var criterionNumber = null;
            if( CRITERION_INDEX == -1 ){
                if( findCollision(name, CRITERIA, "Name") != -1 ) {
                    console.log("Name is not unique!");
                    $('#critEditDialogFeedback').html('<div class="alert alert-danger">Another criterion already exists with the name "'+name+'".</div>');
                    return false;
                }

                uniqueCriteriaId = "Criterion_"+getUniqueDocumentId();
                criterionNumber = CRITERIA.length+1;
            }else{
                // We need to make sure we don't conflict with another criterion element
                var collisionIndex = findCollision(name, CRITERIA, "Name");
                if( !(collisionIndex == -1 || collisionIndex == CRITERION_INDEX) ) {
                    console.log("Name is not unique!");
                    $('#critEditDialogFeedback').html('<div class="alert alert-danger">Another criterion already exists with the name "'+name+'".</div>');
                    return false;
                }

                uniqueCriteriaId = CRITERIA[CRITERION_INDEX]['$id'];
                criterionNumber = CRITERIA[CRITERION_INDEX].Number;
                removeCriterion(CRITERION_INDEX);
            }

            $('#critEditDialog').modal('hide');
            CRITERIA.push({"$id": uniqueCriteriaId, "Number": criterionNumber, "Name": name, "Description": def, "Citations": citationsArray});
            updateCriteria();
        }//end saveCriterion()

        /**
         * Removes the criterion by index from the CRITERIA array without updating the view.
         */
        function removeCriterion(index){
            var NEW_CRIT = new Array();
            for( var i = 0; i < CRITERIA.length; i++ ){
                if( i != index ){
                    NEW_CRIT.push(CRITERIA[i]);
                }
            }
            CRITERIA = NEW_CRIT;
        }

        /**
         * Removes the criterion after prompting, then updates the view.
         */
        function deleteCriterion(index){
            if( confirm('Do you really want to delete criterion "'+CRITERIA[index].Name+'"?') ){
                removeCriterion(index);
                updateCriteria();
                updateAssessmentSteps();
            }
        }
        //==========================================================================================================
        // Assessment Steps
        //==========================================================================================================
        /**
         * Used to record which assessment step is being edited.  If -1, then it is a NEW assessment step.
         */
        var ASSESSMENT_STEP_INDEX = -1;

        /**
         * Sorts assessment steps;
         */
        function getSortedSteps(){
            if( ASSESSMENT_STEPS == null )
                ASSESSMENT_STEPS = new Array();
            ASSESSMENT_STEPS.sort(sortByNumber)
            return ASSESSMENT_STEPS;
        }

        /**
         * Updates the UI to draw changes to the assessment step model
         */
        function updateAssessmentSteps(){
            console.log("Updating assessment steps...");

            var html = "";
            html += '<div>';

            var steps = getSortedSteps();
            if( steps != null && steps.length > 0 ){
                for( var i = 0; i < steps.length; i++ ){
                    html += formatAssessmentStep(steps[i], i);
                }
                $('#stepsCount').html(steps.length);
            }else{
                html += '<em>There are no assessment steps to display.</em>'
                $('#stepsCount').html('0');
            }

            html += '</div>';

            $('#assStepContainer').html(html);

        }//end updateAssessmentSteps()

        /**
         * Draws the HTML for any individual step.
         */
        function formatAssessmentStep(assStep, index){
            console.log("Formatting Assessment Step #"+index+": "+assStep.name);
            assStep.id = assStep['$id'];
            var html = '';
            var className = 'assStepContainer';
            if( index == 0 ){
                className += ' assStepContainerFirst first';
            }
            if(index % 2 == 0 ){
                className += ' assStepContainerEven even';
            }else{
                className += ' assStepContainerOdd odd';
            }
            html += '<div class="'+className+'" onmouseenter="showStepControls('+index+')" onmouseleave="hideStepControls('+index+')">\n';
//                html += '    <h3>'+assStep.Number+") "+assStep.Name+"</h3>\n";
//                html += '    <h3>'+assStep.Name+"</h3>\n";
            html += '    <h3>'+assStep.Number+") "+assStep.Name + ' <small><span class="assStepIdContainer">'+assStep['$id']+'</span></small>';
            html += '        <div style="float: right; font-size: 90%;" id="stepControls'+index+'">';
            html += '            <a href="javascript:moveAssessmentStepUp(\''+assStep.id+'\', '+index+')" title="Move this assessment step up"><span class="glyphicon glyphicon-arrow-up"></span></a>';
            html += '               &nbsp;';
            html += '            <a href="javascript:moveAssessmentStepDown(\''+assStep.id+'\', '+index+')" title="Move this assessment step down"><span class="glyphicon glyphicon-arrow-down"></span></a>';
            html += '               &nbsp;';
            html += '            <a href="javascript:editAssessmentStep('+index+')" title="Edit this assessment step"><span class="glyphicon glyphicon-pencil"></span></a>';
            html += '               &nbsp;';
            html += '            <a href="javascript:deleteAssessmentStep('+index+')" title="Delete this assessment step"><span class="glyphicon glyphicon-remove"></span></a>';
            html += '        </div>\n';
            html += '    </h3>\n';
            html += '    <div class="assStepDescriptionContainer">'+assStep.Description+'</div>\n';

            html += '    <div class="assStepCriteria">\n';
            html += '        <label>Criteria:</label> ';
            for( var i = 0; i < assStep.ConformanceCriteria.length; i++ ){
                var critId = getIdFromRef(assStep.ConformanceCriteria[i]["$ref"]);
                var criterion = findCriteriaById(critId);
                if( criterion ){
                    html += '<span class="label label-default">'+criterion.Name+'</span>';
                }else{
                    html += '<span class="label label-danger">'+critId+'</span>';
                }
                if( i < (assStep.ConformanceCriteria.length - 1) ){
                    html += ', ';
                }
            }
            html += '    </div>\n';

            html += '    <div class="row">\n';

            html += '        <div class="col-md-6">\n';
            html += formatArtifacts(assStep.Artifacts);
            html += '        </div>\n';

            html += '        <div class="col-md-6">\n';
            html += formatParameters(assStep.ParameterDefinitions);
            html += '        </div>\n';

            html += '    </div>\n';

            html += '</div>\n\n';

            setTimeout('hideStepControls('+index+')', 250);

            return html;
        }//end formatAssessmentStep()

        function hideStepControls(index){
            $('#stepControls' + index).hide();
        }
        function showStepControls(index){
            $('#stepControls' + index).show();
        }

        /**
         * Given an array of Artifacts, this method will format them as HTML for inclusion into the view.
         */
        function formatArtifacts(artifacts){
            var html = "";
            html += "<div>";
            html += "  <h4>Artifacts</h4>";
            if( artifacts && artifacts.length > 0 ){
                html += '<table class="table table-striped table-condensed table-bordered">';
                for( var i = 0; i < artifacts.length; i++ ){
                    html += '<tr><td>'+artifacts[i].Name+'</td><td>'+artifacts[i].Description+'</td></tr>'
                }
                html += '</table>'

            }else{
                html += "<div><em>There are no artifacts defined.</em></div>";
            }
            html += "</div>";
            return html;
        }//end formatArtifacts()

        /**
         * Given an array of Parameters, this method will format them as HTML for inclusion into the view.
         */
        function formatParameters(parameters){
            var html = "";
            html += "<div>";
            html += "  <h4>Parameters</h4>";
            console.log("Displaying parameters: "+JSON.stringify(parameters));
            if( parameters && parameters.length > 0 ){
                html += '<table class="table table-striped table-condensed table-bordered">';
                for( var i = 0; i < parameters.length; i++ ){
                    var parameter = parameters[i];
                    html += '<tr><td>' + parameter.Name + '</td><td>'+parameter.ParameterKind+'</td><td>' + parameter.Description + '</td></tr>'
                }
                html += '</table>'

            }else{
                html += "<div><em>There are no parameters defined.</em></div>";
            }
            html += "</div>";
            return html;
        }//end formatParameters()

        /**
         * Moves the assessment step up, as in making it's list index one less.
         */
        function moveAssessmentStepUp(stepId, index){
            if( index > 0 ){
                ASSESSMENT_STEPS[index].Number = index;
                ASSESSMENT_STEPS[index-1].Number = index+1;
                updateAssessmentSteps();
            }
        }//end moveAssessmentStepUp()

        /**
         * Moves the assessment step down, as in making it's list index one more.
         */
        function moveAssessmentStepDown(stepId, index){
            if( index < (ASSESSMENT_STEPS.length - 1) ) {
                ASSESSMENT_STEPS[index].Number = index + 2;
                ASSESSMENT_STEPS[index + 1].Number = index + 1;
                updateAssessmentSteps();
            }
        }//end moveAssessmentStepDown()

        /**
         * Tied to the create new button on the assessment step tab.
         */
        function createNewAssessmentStep(){
            ASSESSMENT_STEP_INDEX = -1;
            $('#assStepDialogName').val('');
            $('#assStepDialogIdentifier').val('');
            $('#assStepDialogDescription').val('');
            $('#assStepDialogArtifacts').html('<em>There are no artifacts.</em>');
            $('#assStepDialogArtifacts').removeClass("HasArtifacts");
            $('#assStepDialogParameters').html('<em>There are no parameters.</em>');
            $('#assStepDialogParameters').removeClass("HasParameters");
            $('#assStepDialogCriteria').html('<em>There are no criteria.</em>');
            $('#assStepDialogCriteria').removeClass("HasCriteria");

            $('#assessmentStepDialog').modal('show');
        }

        /**
         * Callback for the pencil icon for editing an assessment step.
         */
        function editAssessmentStep(index) {
            console.log("Edit assessment step["+index+"]: "+ASSESSMENT_STEPS[index].Name);
            var step = ASSESSMENT_STEPS[index];
            step.id = step['$id'];

            ASSESSMENT_STEP_INDEX = index;
            $('#assStepDialogName').val(step.Name);
            $('#assStepDialogIdentifier').val(step.id);
            $('#assStepDialogDescription').val(step.Description);

            var critHtml = '';
            for( var criterionIndex = 0; criterionIndex < step.ConformanceCriteria.length; criterionIndex++ ){
                var critId = step.ConformanceCriteria[criterionIndex]["$ref"].substring(1);
                var crit = findCriteriaById(critId);

                var id = getUniqueDocumentId();

                critHtml += '<div id="'+id+'Container" class="assStepDialogCriterionContainer" style="margin-bottom: 0.5em;">\n';
                critHtml += '  <select id="'+id+'Select" class="form-control criterionSelect" style="width: 95%; display: inline-block;">\n';
                critHtml +=         buildCriteriaOptions(crit);
                critHtml += '  </select>\n';
                critHtml += '  <a href="javascript:assStepDialogRemoveCriteria(\''+id+'\')"><span class="glyphicon glyphicon-remove"></span></a>\n';
                critHtml += '</div>\n';
            }
            $('#assStepDialogCriteria').html(critHtml);
            $('#assStepDialogCriteria').addClass("HasCriteria");

            if( step.Artifacts && step.Artifacts.length > 0 ){
                var html = '';
                for( var artifactIndex = 0; artifactIndex < step.Artifacts.length; artifactIndex++ ){
                    var artifact = step.Artifacts[artifactIndex];
                    var id = getUniqueDocumentId();

                    html += '<div id="'+id+'Container" class="assStepDialogArtifactContainer" style="margin-bottom: 0.5em;">\n';
                    html += '  <input id="'+id+'Name" type="text" class="form-control artifactName" style="width: 20%; display: inline-block;" placeholder="Name" value="'+htmlEncode(artifact.Name)+'" />\n';
                    html += '  <input id="'+id+'Description" type="text" class="form-control artifactDescription" style="width: 75%; display: inline-block;" placeholder="Description" value="'+htmlEncode(artifact.Description)+'" />\n';
                    html += '  <a href="javascript:assStepDialogRemoveArtifact(\''+id+'\')"><span class="glyphicon glyphicon-remove"></span></a>\n';
                    html += '</div>\n';
                }

                $('#assStepDialogArtifacts').html(html);
                $('#assStepDialogArtifacts').addClass("HasArtifacts");
            }else{
                $('#assStepDialogArtifacts').html('<em>There are no artifacts.</em>');
                $('#assStepDialogArtifacts').removeClass("HasArtifacts");
            }

            if( step.ParameterDefinitions && step.ParameterDefinitions.length > 0 ){
                var html = '';
                for( var pi = 0; pi < step.ParameterDefinitions.length; pi++ ){
                    var param = step.ParameterDefinitions[pi];
                    html += styleParameterHtml(param);
                }

                $('#assStepDialogParameters').html(html);
                $('#assStepDialogParameters').addClass("HasParameters");
            }else{
                $('#assStepDialogParameters').html('<em>There are no parameters.</em>');
                $('#assStepDialogParameters').removeClass("HasParameters");
            }

            $('#assessmentStepDialog').modal('show');
        }

        /**
         * Adds another artifact line to the ass step dialog.
         */
        function assStepDialogAddArtifact() {
            var html = '';

            if( !($('#assStepDialogArtifacts').hasClass("HasArtifacts")) ){
                $('#assStepDialogArtifacts').html('');
                $('#assStepDialogArtifacts').addClass("HasArtifacts");
            }

            var id = getUniqueDocumentId();

            html += '<div id="'+id+'Container" class="assStepDialogArtifactContainer" style="margin-bottom: 0.5em;">\n';
            html += '  <input id="'+id+'Name" type="text" class="form-control artifactName" style="width: 20%; display: inline-block;" placeholder="Name" />\n';
            html += '  <input id="'+id+'Description" type="text" class="form-control artifactDescription" style="width: 75%; display: inline-block;" placeholder="Description" />\n';
            html += '  <a href="javascript:assStepDialogRemoveArtifact(\''+id+'\')"><span class="glyphicon glyphicon-remove"></span></a>\n';
            html += '</div>\n';

            $('#assStepDialogArtifacts').append(html);
        }

        /**
         * Callback for the remove button on the assessment step dialog artifact.
         */
        function assStepDialogRemoveArtifact(id){
            $('#'+id+"Container").remove();

            if( $('#assStepDialogArtifacts .assStepDialogArtifactContainer').length == 0 ){
                $('#assStepDialogArtifacts').html('<em>There are no artifacts.</em>');
                $('#assStepDialogArtifacts').removeClass("HasArtifacts");
            }
        }

        /**
         * Styles the parameter HTML form for placement on the assStepDialog.  Note that param is optional, and it
         * will style "empty" if that field is not given.
         */
        function styleParameterHtml(param){
            var html = '';
            var id = getUniqueDocumentId();

            html += '<div id="'+id+'Container" class="assStepDialogParameterContainer" style="margin-bottom: 0.5em;">\n';
            var name = "";
            if( param ) name = param.Name;
            html += '  <input id="'+id+'Name" type="text" class="form-control paramName" style="width: 20%; display: inline-block;" placeholder="Name" value="'+htmlEncode(name)+'" />\n';
            html += '  <select id="'+id+'Type" class="form-control paramType" style="width: 25%; display: inline-block;">\n';
            var type = null;
            if( param ) type = param.ParameterKind;
            html += styleParameterTypesAsHtmlOption(type);
            html += '  </select>\n';
            var identifier = "";
            if( param ) identifier = param.Identifier;
            html += '  <input id="'+id+'Identifier" type="text" class="form-control paramId" style=" width: 20%; display: inline-block;" placeholder="Identifier" value="'+htmlEncode(identifier)+'" />\n';
            if( param == null || param.Required == true )
                html += '  <div style="width: 20%; display: inline-block;"><input id="'+id+'Required" class="paramRequired" type="checkbox" checked="checked" /> <label for="'+id+'Required" style="font-weight: normal;">Required</label></div>\n';
            else
                html += '  <div style="width: 20%; display: inline-block;"><input id="'+id+'Required" class="paramRequired" type="checkbox" /> <label for="'+id+'Required" style="font-weight: normal;">Required</label></div>\n';
            html += '  <a href="javascript:assStepDialogRemoveParameter(\''+id+'\')"><span class="glyphicon glyphicon-remove"></span></a>\n';
            var desc = "";
            if( param ) desc = param.Description;
            html += '  <input id="'+id+'Description" type="text" class="form-control paramDescription" style="margin-left: 5%; margin-top: 0.5em; width: 80%; display: inline-block;" placeholder="Description" value="'+htmlEncode(desc)+'" />\n';
            var enums = "";
            if( param && (param.ParameterKind === "ENUM" || param.ParameterKind === "ENUM_MULTI") ){
                for(var enumIndex = 0; enumIndex < param.EnumValues.length; enumIndex++ ){
                    enums += param.EnumValues[enumIndex];
                    if( enumIndex < (param.EnumValues.length - 1) ){
                        enums += ',';
                    }
                }
            }
            html += '  <input id="'+id+'Enums" type="text" class="form-control paramEnums" style="margin-left: 5%; margin-top: 0.5em; width: 80%; display: inline-block;" placeholder="Enumerated Values..." value="'+htmlEncode(enums)+'" />\n';
            html += '</div>\n';

            return html;
        }

        /**
         * Creates the HTML Option elements representing the types of parameters.  If type is given, then that value
         * will be selected.
         */
        function styleParameterTypesAsHtmlOption(type){
            var html = '';
            var types = ["STRING", "NUMBER", "BOOLEAN", "ENUM", "ENUM_MULTI", "DATETIME"];
            for( var i = 0; i < types.length; i++ ){
                var cur = types[i];
                if( type && cur === type.toUpperCase().trim() ){
                    html += '    <option selected="selected">'+cur+'</option>\n';
                }else{
                    html += '    <option>'+cur+'</option>\n';
                }
            }
            return html;
        }

        /**
         * Adds another parameter line to the ass step dialog.
         */
        function assStepDialogAddParameter() {
            var html = '';

            if( !($('#assStepDialogParameters').hasClass("HasParameters")) ){
                $('#assStepDialogParameters').html('');
                $('#assStepDialogParameters').addClass("HasParameters");
            }

            var html = styleParameterHtml(null);

            $('#assStepDialogParameters').append(html);
        }

        /**
         * Callback for the ass step dialog remove parameter button.
         */
        function assStepDialogRemoveParameter(id){
            $('#'+id+"Container").remove();

            if( $('#assStepDialogParameters .assStepDialogParameterContainer').length == 0 ){
                $('#assStepDialogParameters').html('<em>There are no parameters.</em>');
                $('#assStepDialogParameters').removeClass("HasParameters");
            }
        }//end assStepDialogRemoveParameter()

        /**
         * Adds a criteria drop down to the edit assessment step dialog.
         */
        function assStepDialogAddCriteria(){
            var html = '';

            if( !($('#assStepDialogCriteria').hasClass("HasCriteria")) ){
                $('#assStepDialogCriteria').html('');
                $('#assStepDialogCriteria').addClass("HasCriteria");
            }

            var id = getUniqueDocumentId();

            html += '<div id="'+id+'Container" class="assStepDialogCriterionContainer" style="margin-bottom: 0.5em;">\n';
            html += '  <select id="'+id+'Select" class="form-control criterionSelect" style="width: 95%; display: inline-block;">\n';
            html +=         buildCriteriaOptions(null);
            html += '  </select>\n';
            html += '  <a href="javascript:assStepDialogRemoveCriteria(\''+id+'\')"><span class="glyphicon glyphicon-remove"></span></a>\n';
            html += '</div>\n';

            $('#assStepDialogCriteria').append(html);

        }//end assStepDialogAddCriteria()

        /**
         * Callback for the remove button on the assessment step criteria for assessment step edit dialog.
         */
        function assStepDialogRemoveCriteria(id){
            $('#'+id+"Container").remove();

            if( $('#assStepDialogCriteria .assStepDialogCriterionContainer').length == 0 ){
                $('#assStepDialogCriteria').html('<em>There are no criteria.</em>');
                $('#assStepDialogCriteria').removeClass("HasCriteria");
            }
        }

        /**
         * Builds HTML Option elements for criteria, assuming you want optionalCriteria highlighted if given.
         */
        function buildCriteriaOptions(optionalCriterion){
            var html = '';
            var criteria = getSortedCriteria();
            if( criteria && criteria.length > 0 ) {
                for (var i = 0; i < criteria.length; i++) {
                    var crit = criteria[i];
                    var critId = crit['$id'];
                    if( optionalCriterion && optionalCriterion['$id'] == critId ){
                        html += '<option selected="selected" value="' + critId + '">' + crit.Name + '</option>';
                    }else {
                        html += '<option value="' + critId + '">' + crit.Name + '</option>';
                    }
                }
            }else{
                html += '<option value="-1">There are no criteria!</option>';
            }
            return html;
        }//end buildCriteriaOptions()

        /**
         * Removes the assessment step from the model, but does not update the view.
         */
        function removeAssessmentStep(index, updateNumbers){
            var NEW_STEPS = new Array();
            for( var i = 0; i < ASSESSMENT_STEPS.length; i++ ){
                var step = ASSESSMENT_STEPS[i];
                if( updateNumbers && i > index )
                    step.Number = step.Number - 1;
                if( i != index ){
                    NEW_STEPS.push(step);
                }
            }
            ASSESSMENT_STEPS = NEW_STEPS;
        }

        /**
         * Removes the assessment step, then updates the view (after prompting).
         */
        function deleteAssessmentStep(index){
            if( confirm('Really delete assessment step "'+ASSESSMENT_STEPS[index].Name+'"?') ){
                removeAssessmentStep(index, true);
                updateAssessmentSteps();
            }
        }

        /**
         * A helper method which builds the list of criteria from the assessment step edit dialog.
         */
        function buildCriteriaFromDialog() {
            var criteria = new Array();
            var criteriaJquery = $('#assStepDialogCriteria .assStepDialogCriterionContainer');
            for( var ci = 0; ci < criteriaJquery.length; ci++ ){
                var critJquery = $(criteriaJquery[ci]);
                var critSelect = $(critJquery.find(".criterionSelect"));
                var critId = critSelect.val();

                console.log("Pusing criteria: "+critId);
                criteria.push({"$ref": "#"+critId});
            }
            return criteria;
        }

        /**
         * Saves the assessment step to the model, then updates the view.  This method is a call back for the Save
         * button on the ass step dialog.
         */
        function saveAssessmentStep() {
            console.log("Call to saveAssessmentStep()");
            $('#assStepDialogFeedback').html('');

            var name = idValOrEmpty('assStepDialogName');
            if( name === "" ){
                console.log("Name is required!");
                $('#assStepDialogFeedback').html('<div class="alert alert-danger">Name is a required field.</div>');
                return false;
            }
            console.log("Name set to: "+name);

            var identifier = idValOrEmpty('assStepDialogIdentifier');
            if( identifier === "" ){
                console.log("Identifier is required!");
                $('#assStepDialogFeedback').html('<div class="alert alert-danger">Identifier is a required field.</div>');
                return false;
            }

            var description = idValOrEmpty('assStepDialogDescription');
            if( description === "" ){
                console.log("Description is required!");
                $('#assStepDialogFeedback').html('<div class="alert alert-danger">Description is a required field.</div>');
                return false;
            }
            console.log("Description set to: "+description);

            var criteria = buildCriteriaFromDialog();
            if( !criteria )
                criteria = [];

            var artifacts = new Array();
            var artifactsJquery = $('#assStepDialogArtifacts .assStepDialogArtifactContainer');
            for( var ai = 0; ai < artifactsJquery.length; ai++ ){
                var artifactJquery = $(artifactsJquery[ai]);
                try {
                    artifacts.push(buildArtifact(artifactJquery));
                }catch(e){
                    $('#assStepDialogFeedback').html('<div class="alert alert-danger">'+e+'</div>');
                    return false;
                }
            }

            var parameters = new Array();
            var paramsJquery = $('#assStepDialogParameters .assStepDialogParameterContainer');
            for( var pi = 0; pi < paramsJquery.length; pi++ ){
                var paramJquery = $(paramsJquery[pi]);
                try {
                    parameters.push(buildParam(paramJquery));
                }catch(e){
                    $('#assStepDialogFeedback').html('<div class="alert alert-danger">'+e+'</div>');
                    return false;
                }
            }
            // Check for two conflicting parameter names locally.
            for( var paramIndexI = 0; paramIndexI < parameters.length; paramIndexI++ ){
                var paramI = parameters[paramIndexI];
                for( var paramIndexJ = 0; paramIndexJ < parameters.length; paramIndexJ++ ){
                    if( paramIndexJ != paramIndexI ){
                        var paramJ = parameters[paramIndexJ];
                        if( paramI.Name.toUpperCase().trim() === paramJ.Name.toUpperCase().trim() ){
                            console.log("Two parameters share the name '"+paramI.Name+"' and they must be unique!");
                            $('#assStepDialogFeedback').html('<div class="alert alert-danger">Two parameters share the name "'+paramI.Name+'" and they must be unique!</div>');
                            return false;
                        }else if( paramI.Identifier.toUpperCase().trim() === paramJ.Identifier.toUpperCase().trim() ){
                            console.log("Two parameters share the Identifier '"+paramI.Identifier+"' and they must be unique!");
                            $('#assStepDialogFeedback').html('<div class="alert alert-danger">Two parameters share the Identifier "'+paramI.Identifier+'" and they must be unique!</div>');
                            return false;
                        }
                    }
                }
            }

            var step = {"Name" : name, "Description": description, "Number": -1, "$id" : identifier, "ConformanceCriteria": criteria, "Artifacts": artifacts};
            if( parameters && parameters.length > 0 ){
                step["ParameterDefinitions"] = new Array();
            }
            if( ASSESSMENT_STEP_INDEX == -1 ){
                // This is a brand new assessment step.
                if( findCollision(name, ASSESSMENT_STEPS, "Name") != -1 ) {
                    console.log("Assessment Step name '"+name+"' is not unique!");
                    $('#assStepDialogFeedback').html('<div class="alert alert-danger">Another assessment step already has the name "'+name+'"</div>');
                    return false;
                }
                if( findCollision(identifier, ASSESSMENT_STEPS, "id") != -1 ) {
                    console.log("Assessment Step id '"+identifier+"' is not unique!");
                    $('#assStepDialogFeedback').html('<div class="alert alert-danger">Another assessment step already has the identifier "'+identifier+'"</div>');
                    return false;
                }

                // Make sure no parameter has the same name or identifier as any parameter we have.
                if( parameters && parameters.length > 0 ) {
                    for (var paramIndex = 0; paramIndex < parameters.length; paramIndex++) {
                        var myParam = parameters[paramIndex];
                        if (findParameterByName(myParam.Name) != -1) {
                            console.log("Parameter name '" + myParam.Name + "' is not unique!");
                            $('#assStepDialogFeedback').html('<div class="alert alert-danger">Parameter name "' + myParam.Name + '" is not unique.  It must be unique in the entire TD.</div>');
                            return false;
                        }
                        if (findParameterByIdentifier(myParam.Identifier) != -1) {
                            console.log("Parameter Identifier '" + myParam.Identifier + "' is not unique!");
                            $('#assStepDialogFeedback').html('<div class="alert alert-danger">Parameter Identifier "' + myParam.Identifier + '" is not unique.  It must be unique in the entire TD.</div>');
                            return false;
                        }
                    }
                    for (var paramIndex = 0; paramIndex < parameters.length; paramIndex++) {
                        var param = parameters[paramIndex];
                        PARAMETERS.push(param);
                        step.ParameterDefinitions.push(param);
                    }
                }

                step.Number = ASSESSMENT_STEPS.length + 1;

            }else{

                if( findCollisionExcept(name, ASSESSMENT_STEPS, "Name", ASSESSMENT_STEP_INDEX) != -1 ) {
                    console.log("Assessment Step name '"+name+"' is not unique!");
                    $('#assStepDialogFeedback').html('<div class="alert alert-danger">Another assessment step already has the name "'+name+'"</div>');
                    return false;
                }
                if( findCollisionExcept(identifier, ASSESSMENT_STEPS, "id", ASSESSMENT_STEP_INDEX) != -1 ) {
                    console.log("Assessment Step id '"+identifier+"' is not unique!");
                    $('#assStepDialogFeedback').html('<div class="alert alert-danger">Another assessment step already has the identifier "'+identifier+'"</div>');
                    return false;
                }


                var oldStep = ASSESSMENT_STEPS[ASSESSMENT_STEP_INDEX];
                step.Number = oldStep.Number;

                if( parameters && parameters.length > 0 ) {
                    var paramsNotInCurrentStep = buildSetOfParamsNotReferencedInStep(oldStep);
                    for (var paramIndex = 0; paramIndex < parameters.length; paramIndex++) {
                        var myParam = parameters[paramIndex];
                        if( findInArray(myParam.Name, paramsNotInCurrentStep, "Name") != -1 ){
                            console.log("Parameter name '" + myParam.Name + "' is not unique!");
                            $('#assStepDialogFeedback').html('<div class="alert alert-danger">Parameter name "' + myParam.Name + '" is not unique.  It must be unique in the entire TD.</div>');
                            return false;
                        }
                        if( findInArray(myParam.Identifier, paramsNotInCurrentStep, "Identifier") != -1 ){
                            console.log("Parameter Identifier '" + myParam.Identifier + "' is not unique!");
                            $('#assStepDialogFeedback').html('<div class="alert alert-danger">Parameter Identifier "' + myParam.Identifier + '" is not unique.  It must be unique in the entire TD.</div>');
                            return false;
                        }
                    }
                    PARAMETERS = paramsNotInCurrentStep; // Effectively deletes the parameters from the global list if they are referenced by the step in question.
                    for (var paramIndex = 0; paramIndex < parameters.length; paramIndex++) {
                        var param = parameters[paramIndex];
                        PARAMETERS.push(param);
                        step.ParameterDefinitions.push(param);
                    }
                }

                removeAssessmentStep(ASSESSMENT_STEP_INDEX, false);
            }

            $('#assessmentStepDialog').modal('hide');

            console.log("Saving assessment step");
            ASSESSMENT_STEPS.push(step);
            updateAssessmentSteps();

        }

        /**
         * This method will build a set of parameters NOT referenced by the given step.
         */
        function buildSetOfParamsNotReferencedInStep(step){
            var params = new Array();
            for( var i = 0; i < PARAMETERS.length; i++ ){
                var param = PARAMETERS[i];
                var found = false;
                if( step.ParameterDefinitions && step.ParameterDefinitions.length > 0 ) {
                    for (var j = 0; j < step.ParameterDefinitions.length; j++) {
                        var curId = step.ParameterDefinitions[j].Identifier;
                        if (param.Identifier.toUpperCase().trim() === curId.toUpperCase().trim()) {
                            found = true;
                            break;
                        }

                    }
                }
                if( !found )
                    params.push(param);
            }
            return params;
        }//end buildSetOfParamsNotReferencedInStep()

        /**
         * Looks for the parameter with a Name matching the one given.
         */
        function findParameterByName(name){
            return findInArray(name, PARAMETERS, "Name");
        }

        /**
         * Looks for the parameter with an Identifier matching the one given.
         */
        function findParameterByIdentifier(identifier){
            return findInArray(identifier, PARAMETERS, "Identifier");
        }

        /**
         * Given an Artifact container as JQuery, this method will return an artifact object.  Throws exception for errors found.
         */
        function buildArtifact(artifactJQuery){
            var name = artifactJQuery.find(".artifactName").val();
            if( name == null ) name = "";
            name = name.trim();
            if( name === "" )
                throw "All artifacts require a name."

            var desc = artifactJQuery.find(".artifactDescription").val();
            if( desc == null ) desc = "";
            desc = desc.trim();
            if( desc === "" )
                throw "All artifacts require a description."

            return {"Name" : name, "Description" : desc};
        }

        /**
         * Given a Param container as JQuery, this method will return a param object.  Throws exception for errors found.
         */
        function buildParam(paramJquery) {
            var name = paramJquery.find(".paramName").val();
            if( name == null ) name = "";
            name = name.trim();
            if( name === "" )
                throw "All parameters require a name."

            var type = paramJquery.find(".paramType").val();
            if( type == null ) type = "";
            type = type.trim();

            var id = paramJquery.find(".paramId").val();
            if( id == null ) id = "";
            id = id.trim();
            if( id === "" )
                throw "All parameters require an Identifier."

            var desc = paramJquery.find(".paramDescription").val();
            if( desc == null ) desc = "";
            desc = desc.trim();
            if( desc === "" )
                throw "All parameters require a description."

            var enums = paramJquery.find(".paramEnums").val();
            if( enums == null ) enums = "";
            enums = enums.trim();
            if( (type === "ENUM" || type === "ENUM_MULTI") && enums === "" ){
                throw "If the parameter type contains ENUM, then you must provide a comma separated list of enumeration values."
            }

            var required = paramJquery.find(".paramRequired").is(':checked');

            var paramJson = {
                "ParameterKind" : type,
                "Identifier" : id,
                "Name" : name,
                "Description": desc,
                "Required": required
            }

            if( (type === "ENUM" || type === "ENUM_MULTI") ){
                paramJson["EnumValues"] = enums.split(',');
            }

            console.log("Adding parameter: "+JSON.stringify(paramJson));

            return paramJson;
        }
        //==========================================================================================================
        // General Functions
        //==========================================================================================================
        /**
         * Given a presumed string, returns the trimmed value if it exists, the empty string otherwise.  IE, asserts
         * that the given value is not null.
         */
        function valOrEmpty(val){
            if( val ){
                return (''+val).trim();
            }else{
                return '';
            }
        }

        /**
         * Assumes the incoming value is an ID in the DOM somewhere, uses JQuery to find it and get the val on it.
         * If not null, trims the value and returns it.  Otherwise, returns the empty string.
         */
        function idValOrEmpty(id){
            var val = $('#'+id).val();
            if( val )
                val = val.trim();
            else
                val = '';
            return val;
        }

        /**
         * Searches through the sources array to find the source with the given identifier.
         */
        function getSourceById(id){
            var source = null;
            if( SOURCES && SOURCES.length > 0 ){
                for( var i = 0; i < SOURCES.length; i++ ){
                    if( SOURCES[i]['$id'] == id ){
                        source = SOURCES[i];
                        break;
                    }
                }
            }
            return source;
        }

        /**
         * Interprets the $ref value to provide the identifier.
         */
        function getIdFromRef(refVal){
            return refVal.substring(1);
        }

        /**
         * This variable is used ONLY by the getUniqueDocumentId function to generate a unique ID.  Note that on a
         * page reload, this value will reset.
         */
        var ID_COUNTER = 10000;

        /**
         * This function generates an ID that *should* be unique in the document.  Based on the accepted answer from:
         * http://stackoverflow.com/questions/3231459/create-unique-id-with-javascript
         */
        function getUniqueDocumentId(){
            ID_COUNTER++;
            return "id_"+ID_COUNTER+"_"+Math.random().toString(16).slice(2);
        }

        /**
         * Sorts the given array by name.
         */
        function sortByName(a, b){
            var aName = a.Name.toLowerCase();
            var bName = b.Name.toLowerCase();
            return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
        }

        /**
         * Sorts the given array by Identifier
         */
        function sortByIdentifier(a, b){
            var aName = a.Identifier.toLowerCase();
            var bName = b.Identifier.toLowerCase();
            return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
        }

        /**
         * Sorts the given array by Number.
         */
        function sortByNumber(a, b){
            var aName = a.Number;
            var bName = b.Number;
            return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
        }

        /**
         * Performs a sort algorithm for two incoming strings.
         */
        function sortStrings(s1, s2){
            var s1Lower = s1.toLowerCase();
            var s2Lower = s2.toLowerCase();
            return ((s1Lower < s2Lower) ? -1 : ((s1Lower > s2Lower) ? 1 : 0));
        }

        /**
         * Checks for a collision on a given field from all objects in the array.
         */
        function findCollision(value, array, fieldName){
            var collisionIndex = -1;
            if( array && array.length > 0 ){
                for( var i = 0; i < array.length; i++ ){
                    var arrayName = array[i][fieldName].toUpperCase().trim();
                    if( value.toUpperCase().trim() === arrayName) {
                        collisionIndex = i;
                        break;
                    }
                }
            }
            return collisionIndex;
        }//end hasNameCollision()

        /**
         * Checks for a collision just like findCollision(), with the exception of the last value which is an index
         * in the array to skip.
         */
        function findCollisionExcept(value, array, fieldName, index){
            var collisionIndex = -1;
            if( array && array.length > 0 ){
                for( var i = 0; i < array.length; i++ ){
                    if( i != index ) {
                        var arrayName = array[i][fieldName].toUpperCase().trim();
                        if (value.toUpperCase().trim() === arrayName) {
                            collisionIndex = i;
                            break;
                        }
                    }
                }
            }
            return collisionIndex;
        }//end hasNameCollision()

        /**
         * Searches for the given value in the array.
         * @param value the String value to search for.
         * @param array the array to search in.
         * @param field the name of field on each index of the array.
         * @returns {number} an index in the array where the value can be found, or -1 if not found.
         */
        function findInArray(value, array, field){
            var index = -1;
            if( value != null ){
                if( array && array.length > 0 ){
                    for( var i = 0; i < array.length; i++ ){
                        var aVal = array[i][field];
                        if( aVal && aVal.toUpperCase().trim() === value.toUpperCase().trim() ){
                            index = i;
                            break;
                        }
                    }
                }
            }
            return index;
        }

        // Taken from http://stackoverflow.com/questions/1219860/html-encoding-in-javascript-jquery
        function htmlEncode(value){
            //create a in-memory div, set it's inner text(which jQuery automatically encodes)
            //then grab the encoded contents back out.  The div never exists on the page.
            return $('<div/>').text(value).html();
        }

    </script>
</head>
<body>

<div id="page-body" role="main">

    <div class="row mainTitle">
        <div class="col-md-10">
            <h3 id="mainTitle">
                Trustmark Definition Editor
            </h3>
            <div id="feedbackWindow">

            </div>
        </div>
        <div class="col-md-2" id="topRightContainer">
            <div class="pull-right" style="margin-top: 2em;"><a href="javascript:saveTrustmarkDefinition()" class="btn btn-primary">Save</a></div>
        </div>
    </div>

    <div class="row" style="margin-top: 1em;">
        <div class="col-md-12">
            <div>
                <!-- Nav tabs -->
                <ul id="editorTabs" class="nav nav-tabs" role="tablist">
                    <li id="tabsMetadata" role="presentation"><a href="#metadataContainer" aria-controls="metadataContainer" role="tab" data-toggle="tab">Metadata</a></li>
                    <li id="tabsAssessmentSteps" role="presentation"><a href="#assessmentStepsList" aria-controls="assessmentStepsList" role="tab" data-toggle="tab">Assessment Steps (<span id="stepsCount">-1</span>)</a></li>
                    <li id="tabsConformanceCriteria" role="presentation"><a href="#criteriaList" aria-controls="criteriaList" role="tab" data-toggle="tab">Conformance Criteria (<span id="criteriaCount">-1</span>)</a></li>
                    <li id="tabsSources" role="presentation"><a href="#sourcesList" aria-controls="sourcesList" role="tab" data-toggle="tab">Sources (<span id="sourcesCount">-1</span>)</a></li>
                    <li id="tabsTerms" role="presentation"><a href="#termsList" aria-controls="termsList" role="tab" data-toggle="tab">Terms (<span id="termsCount">-1</span>)</a></li>
                </ul>

                <!-- Tab panes -->
                <div class="tab-content" style="margin-top: 1em;">

                    <div role="tabpanel" class="tab-pane" id="metadataContainer">
                        <form class="form-horizontal">

                            <div class="form-group">
                                <label for="tdName" class="col-md-2 control-label">Name</label>
                                <div class="col-md-4">
                                    <input type="text" class="form-control" id="tdName" placeholder="">
                                </div>
                                <label for="tdPublicationDate" class="col-md-1 control-label" title="When this TD is considered published.">Pub. Date</label>
                                <div class="col-md-2">
                                    <input type="text" class="form-control" id="tdPublicationDate" placeholder="2015-05-21">
                                </div>
                                <label for="tdVersion" class="col-md-1 control-label">Version</label>
                                <div class="col-md-2">
                                    <input type="text" class="form-control" id="tdVersion" placeholder="1.0-SNAPSHOT">
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="tdName" class="col-md-2 control-label">Deprecated</label>
                                <div class="col-md-2">
                                    <a href="javascript:toggleDeprecated()" id="deprecatedButton" class="btn btn-default">
                                        <span id="deprecatedIndicator" class="glyphicon glyphicon-remove"></span>
                                        <span id="deprecatedText">Not Deprecated</span>
                                    </a>
                                </div>

                                <div id="supersedByContainer">
                                    <label for="supersededBy" class="col-md-2 control-label">Superseded By</label>
                                    <div class="col-md-6">
                                        <textarea class="form-control" id="supersededBy" name="supersededBy"></textarea>
                                    </div>
                                </div>
                            </div>


                            <div class="form-group">
                                <label for="identifier" class="col-md-2 control-label">Identifier</label>
                                <div class="col-md-10">
                                    <input type="text" class="form-control" id="identifier" placeholder="https://...">
                                </div>
                            </div>


                            <div class="form-group">
                                <label for="refAttribute" class="col-md-2 control-label">Reference Attribute</label>
                                <div class="col-md-10">
                                    <input type="text" class="form-control" id="refAttribute" placeholder="https://...">
                                </div>
                            </div>


                            <div class="form-group">
                                <label for="tdDefinitionOrgName" class="col-md-2 control-label">Defining Organization</label>
                                <div class="col-md-10">
                                    <div class="form-group">
                                        <label for="tdDefinitionOrgName" class="col-md-2 control-label">Name</label>
                                        <div class="col-md-10">
                                            <input id="tdDefinitionOrgName" type="text" class="form-control" placeholder="Name" />
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="tdDefinitionOrgId" class="col-md-2 control-label">URI</label>
                                        <div class="col-md-10">
                                            <input id="tdDefinitionOrgId" type="text" class="form-control" placeholder="http://www..." />
                                        </div>
                                    </div>

                                    <hr />

                                    <div class="form-group">
                                        <label for="tdDefiningOrgResponder" class="col-md-2 control-label">Responder</label>
                                        <div class="col-md-10">
                                            <input id="tdDefiningOrgResponder" type="text" class="form-control" placeholder="Name" />
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="tdDefiningOrgEmail" class="col-md-2 control-label">Email</label>
                                        <div class="col-md-10">
                                            <input id="tdDefiningOrgEmail" type="text" class="form-control" placeholder="user@domain.com" />
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="tdDefiningOrgPhone" class="col-md-2 control-label">Phone Number</label>
                                        <div class="col-md-10">
                                            <input id="tdDefiningOrgPhone" type="text" class="form-control" placeholder="xxx-xxx-xxxx" />
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="tdDefiningOrgMailingAddr" class="col-md-2 control-label">Address</label>
                                        <div class="col-md-10">
                                            <input id="tdDefiningOrgMailingAddr" type="text" class="form-control" placeholder="123 Main St..." />
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="tdDefiningOrgNotes" class="col-md-2 control-label">Notes</label>
                                        <div class="col-md-10">
                                            <input id="tdDefiningOrgNotes" type="text" class="form-control" placeholder="..." />
                                        </div>
                                    </div>

                                </div>
                            </div>


                            <div class="form-group">
                                <label for="tdDescription" class="col-md-2 control-label">Description</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="tdDescription" name="tdDescription"></textarea>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-md-2 control-label">Keywords</label>
                                <div class="col-md-10">
                                    <div class="form-control-static" id="keywordsContainer">
                                        Rendering keywords...
                                    </div>
                                    <div class="keywordAddButtonContainer">
                                        <a href="javascript:addNewKeyword()" class="btn btn-primary" title="Add a new keyword.">
                                            <span class="glyphicon glyphicon-plus"></span>
                                            Add
                                        </a>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="Supersedes" class="col-md-2 control-label">Supersedes</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="Supersedes" name="Supersedes"></textarea>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="TargetStakeholderDescription" class="col-md-2 control-label">Target Stakeholder Description</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="TargetStakeholderDescription" name="TargetStakeholderDescription"></textarea>
                                </div>
                            </div>


                            <div class="form-group">
                                <label for="TargetRelyingPartyDescription" class="col-md-2 control-label">Target Relying Party Description</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="TargetRelyingPartyDescription" name="TargetRelyingPartyDescription"></textarea>
                                </div>
                            </div>


                            <div class="form-group">
                                <label for="TargetRecipientDescription" class="col-md-2 control-label">Target Recipient Description</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="TargetRecipientDescription" name="TargetRecipientDescription"></textarea>
                                </div>
                            </div>


                            <div class="form-group">
                                <label for="TargetProviderDescription" class="col-md-2 control-label">Target Provider Description</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="TargetProviderDescription" name="TargetProviderDescription"></textarea>
                                </div>
                            </div>


                            <div class="form-group">
                                <label for="ProviderEligibilityCriteria" class="col-md-2 control-label">Provider Eligibility Criteria</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="ProviderEligibilityCriteria" name="ProviderEligibilityCriteria"></textarea>
                                </div>
                            </div>


                            <div class="form-group">
                                <label for="AssessorQualificationsDescription" class="col-md-2 control-label">Assessor Qualifications Description</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="AssessorQualificationsDescription" name="AssessorQualificationsDescription"></textarea>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="TrustmarkRevocationCriteria" class="col-md-2 control-label">Revocation Criteria</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="TrustmarkRevocationCriteria" name="TrustmarkRevocationCriteria"></textarea>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="ExtensionDescription" class="col-md-2 control-label">Extension Description</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="ExtensionDescription" name="ExtensionDescription"></textarea>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="LegalNotice" class="col-md-2 control-label">Legal Notice</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="LegalNotice" name="LegalNotice"></textarea>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="Notes" class="col-md-2 control-label">Notes</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="Notes" name="Notes"></textarea>
                                </div>
                            </div>

                        </form>
                    </div>

                    <div role="tabpanel" class="tab-pane" id="assessmentStepsList">
                        <div class="text-muted" style="margin-bottom: 0.5em;">Put some text in here to help people understand what assessment steps are.</div>
                        <form>
                            <div class="form-group">
                                <label for="assStepPreface" class="control-label">Preface</label>
                                <div>
                                    <textarea class="form-control" id="assStepPreface" name="assStepPreface"></textarea>
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="issuanceCriteria" class="control-label">Issuance Criteria</label>
                                <div>
                                    <textarea class="form-control" id="issuanceCriteria" name="issuanceCriteria"></textarea>
                                </div>
                            </div>
                        </form>
                        <div class="buttonContainer">
                            <a href="javascript:createNewAssessmentStep()" class="btn btn-primary">
                                <span class="glyphicon glyphicon-plus"></span>
                                Create New
                            </a>
                        </div>
                        <div id="assStepContainer">
                            Assessment Steps are Loading...
                        </div>
                        <div class="buttonContainer">
                            <a href="javascript:createNewAssessmentStep()" class="btn btn-primary">
                                <span class="glyphicon glyphicon-plus"></span>
                                Create New
                            </a>
                        </div>

                    </div>

                    <div role="tabpanel" class="tab-pane" id="criteriaList">
                        <div class="text-muted" style="margin-bottom: 0.5em;">Put some text in here to help people understand what conformance criteria are.</div>
                        <form>
                            <div class="form-group">
                                <label for="conformanceCriteriaPreface" class="control-label">Preface</label>
                                <div>
                                    <textarea class="form-control" id="conformanceCriteriaPreface" name="conformanceCriteriaPreface"></textarea>
                                </div>
                            </div>
                        </form>
                        <div class="buttonContainer">
                            <a href="javascript:createNewCriterion()" class="btn btn-primary">
                                <span class="glyphicon glyphicon-plus"></span>
                                Create New
                            </a>
                        </div>
                        <div id="criteriaContainer">
                            Conformance Criteria are Loading...
                        </div>
                        <div class="buttonContainer">
                            <a href="javascript:createNewCriterion()" class="btn btn-primary">
                                <span class="glyphicon glyphicon-plus"></span>
                                Create New
                            </a>
                        </div>
                    </div>

                    <div role="tabpanel" class="tab-pane" id="sourcesList">
                        <div class="text-muted" style="margin-bottom: 0.5em;">This section defines the sources of the criteria that make up this TD.</div>
                        <div class="buttonContainer">
                            <a href="javascript:createNewSource()" class="btn btn-primary">
                                <span class="glyphicon glyphicon-plus"></span>
                                Create New
                            </a>
                        </div>
                        <div id="sourcesContainer">
                            The sources are curently loading...
                        </div>
                        <div class="buttonContainer">
                            <a href="javascript:createNewSource()" class="btn btn-primary">
                                <span class="glyphicon glyphicon-plus"></span>
                                Create New
                            </a>
                        </div>
                    </div>

                    <div role="tabpanel" class="tab-pane" id="termsList">
                        <div class="text-muted" style="margin-bottom: 0.5em;">This section defines terms that are important to understand or clarify for this Trustmark Definition.</div>
                        <div class="buttonContainer">
                            <a href="javascript:createNewTerm()" class="btn btn-primary">
                                <span class="glyphicon glyphicon-plus"></span>
                                Create New
                            </a>
                        </div>
                        <div id="termsContainer">
                            The terms are currently loading...
                        </div>
                        <div class="buttonContainer">
                            <a href="javascript:createNewTerm()" class="btn btn-primary">
                                <span class="glyphicon glyphicon-plus"></span>
                                Create New
                            </a>
                        </div>
                    </div>

                </div>

            </div>
        </div>
    </div>


</div><!-- /.container -->

<div style="height: 5em;">&nbsp;</div>

<!-- All of the modal dialogs are defined here -->
<div id="assessmentStepDialog" class="modal fade" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title">Edit Assessment Step</h4>
            </div>
            <div class="modal-body">
                <div id="assStepDialogFeedback"></div>
                <div class="">
                    <form class="form-horizontal">
                        <div class="form-group">
                            <label for="assStepDialogName" class="col-md-2 control-label">Name</label>
                            <div class="col-md-5">
                                <input type="text" class="form-control" id="assStepDialogName" placeholder="" />
                            </div>
                            <label for="assStepDialogIdentifier" class="col-md-1 control-label">Identifier</label>
                            <div class="col-md-4">
                                <input type="text" class="form-control" id="assStepDialogIdentifier" placeholder="" />
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="assStepDialogDescription" class="col-md-2 control-label">Description</label>
                            <div class="col-md-10">
                                <textarea class="form-control" id="assStepDialogDescription"></textarea>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="col-md-2 control-label">Criteria</label>
                            <div class="col-md-10">
                                <div class="form-control-static">
                                    <div id="assStepDialogCriteria">
                                        <em>There are no criteria.</em>
                                    </div>
                                    <div>
                                        <a href="javascript:assStepDialogAddCriteria()" class="btn btn-default btn-sm">Add</a>
                                    </div>
                                </div>
                            </div>
                        </div>


                        <div class="form-group">
                            <label class="col-md-2 control-label">Artifacts</label>
                            <div class="col-md-10">
                                <div class="form-control-static">
                                    <div id="assStepDialogArtifacts">
                                        <em>There are no artifacts</em>
                                    </div>
                                    <div>
                                        <a href="javascript:assStepDialogAddArtifact()" class="btn btn-default btn-sm">Add</a>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="col-md-2 control-label">Parameters</label>
                            <div class="col-md-10">
                                <div class="form-control-static">
                                    <div id="assStepDialogParameters">
                                        <em>There are no parameters.</em>
                                    </div>
                                    <div>
                                        <a href="javascript:assStepDialogAddParameter()" class="btn btn-default btn-sm">Add</a>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </form>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" onclick="saveAssessmentStep()">Save</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->


<div id="termEditDialog" class="modal fade" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title">Edit Term</h4>
            </div>
            <div class="modal-body">
                <div id="termEditDialogFeedback"></div>

                <div class="">
                    <form class="form-horizontal">
                        <div class="form-group">
                            <label for="termEditDialogName" class="col-md-2 control-label">Name</label>
                            <div class="col-md-5">
                                <input type="text" class="form-control" id="termEditDialogName" placeholder="" />
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="col-md-2 control-label">Abbreviations</label>
                            <div class="col-md-10">
                                <div class="form-control-static">
                                    <div id="termEditDialogAbbreviations">
                                        <em>There are no abbreviations.</em>
                                    </div>
                                    <div>
                                        <a href="javascript:termEditDialogAddAbbreviation()" class="btn btn-default btn-sm">Add</a>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="termEditDialogDefinition" class="col-md-2 control-label">Definition</label>
                            <div class="col-md-10">
                                <textarea class="form-control" id="termEditDialogDefinition"></textarea>
                            </div>
                        </div>

                    </form>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" onclick="saveTerm();">Save</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->


<div id="sourceEditDialog" class="modal fade" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title">Edit Source</h4>
            </div>
            <div class="modal-body">
                <div id="sourceEditDialogFeedback"></div>

                <div class="">
                    <form class="form-horizontal">
                        <div class="form-group">
                            <label for="sourceEditDialogIdentifier" class="col-md-2 control-label">Identifier</label>
                            <div class="col-md-5">
                                <input type="text" class="form-control" id="sourceEditDialogIdentifier" placeholder="" />
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="sourceEditDialogReference" class="col-md-2 control-label">Reference</label>
                            <div class="col-md-10">
                                <textarea class="form-control" id="sourceEditDialogReference"></textarea>
                            </div>
                        </div>

                    </form>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" onclick="saveSource();">Save</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->


<div id="critEditDialog" class="modal fade" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title">Edit Conformance Criterion</h4>
            </div>
            <div class="modal-body">
                <div id="critEditDialogFeedback"></div>

                <div class="">
                    <form class="form-horizontal">
                        <div class="form-group">
                            <label for="critEditDialogName" class="col-md-2 control-label">Name</label>
                            <div class="col-md-5">
                                <input type="text" class="form-control" id="critEditDialogName" placeholder="" />
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="critEditDialogCitations" class="col-md-2 control-label">Citations</label>
                            <div class="col-md-10">
                                <div class="form-control-static">
                                    <div id="critEditDialogCitations"></div>
                                    <div>
                                        <a href="javascript:addCitation()" class="btn btn-default">Add</a>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="critEditDialogDescription" class="col-md-2 control-label">Description</label>
                            <div class="col-md-10">
                                <textarea class="form-control" id="critEditDialogDescription"></textarea>
                            </div>
                        </div>

                    </form>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" onclick="saveCriterion();">Save</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->


</body>
</html>
