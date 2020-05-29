
<div id="tdRefSearch${tdRefId ?: ''}_tipReferenceSelectorDialog" class="modal fade" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title">${dialogTitle ?: 'Select Trust Interoperability Profile Reference'}</h4>
            </div>
            <div class="modal-body">
                <div>
                    <form class="form-inline">
                        <div class="form-group" style="width: 100%;">
                            <input type="text" style="width: 100%;" class="form-control" id="tdRefSearch${tdRefId ?: ''}_tipRefSearchText" placeholder="Search..." />
                        </div>
                    </form>
                    <div style="font-size: 90%; color: #888;">
                        Please type your search in the box above.  Results will appear below, please select the one you
                        wish to add as a reference to this TIP.
                    </div>
                    <hr />
                    <div id="tdRefSearch${tdRefId ?: ''}_tipSearchResultSelectionContainer">

                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" id="tdRefSearch${tdRefId ?: ''}_addRefButton" class="btn btn-primary disabled">${addRefText ?: 'Add Reference'}</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->


<div id="tdRefSearch${tdRefId ?: ''}_addButtonsContainer">
    <a href="javascript:tdRefSearch${tdRefId ?: ''}_addTIPReferenceButtonClickHandler()" class="btn btn-primary">
        <span class="glyphicon glyphicon-plus"></span>
        Add
    </a>
</div>

<script type="text/javascript">
    var tdRefSearch${tdRefId ?: ''}_RESPONSE = null;
    var tdRefSearch${tdRefId ?: ''}_SEARCH_URL = '${searchUrl ?: createLink(controller:'search', action: 'index')}';
    var tdRefSearch${tdRefId ?: ''}_tipRefSearchTextTimerID = null;

    $(document).ready(function(){
        var addButtonsHtml = $('#tdRefSearch${tdRefId ?: ''}_addButtonsContainer').html();
        $('#tdRefSearch${tdRefId ?: ''}_addButtonsContainer').hide();

        $('#tdRefSearch${tdRefId ?: ''}_tipReferenceSelectorDialog').on('shown.bs.modal', function(){
            $('#tdRefSearch${tdRefId ?: ''}_tipRefSearchText').focus();
        });

        $('#tdRefSearch${tdRefId ?: ''}_tipRefSearchText').on("input", function(e){
            var value = $(this).val();
            if( $(this).data("lastval") != value ){
                $(this).data("lastval", value);
                clearTimeout(tdRefSearch${tdRefId ?: ''}_tipRefSearchTextTimerID);
                tdRefSearch${tdRefId ?: ''}_tipRefSearchTextTimerID = setTimeout(function(){
                    tdRefSearch${tdRefId ?: ''}_searchTIPReferences(value);
                }, 600);
            }
        });

        $('#${buttonContainerId}').html(addButtonsHtml);
    })



    /**
     * Called when the user clicks the "add" button under TIP References.
     */
    function tdRefSearch${tdRefId ?: ''}_addTIPReferenceButtonClickHandler() {
        console.log("Add button for TIP references clicked.");
        $('#tdRefSearch${tdRefId ?: ''}_tipRefSearchText').val('');
        $('#tdRefSearch${tdRefId ?: ''}_tipSearchResultSelectionContainer').html('');
        $('#tdRefSearch${tdRefId ?: ''}_tipReferenceSelectorDialog').modal("show");
    }//end addTIPReferenceButtonClickHandler()

    /**
     * Called when the user has entered text to search for, the search text is given.
     */
    function tdRefSearch${tdRefId ?: ''}_searchTIPReferences(searchText){

        if( $('#tdRefSearch${tdRefId ?: ''}_tipRefSearchText').val().trim() === '' ) {
            $('#tdRefSearch${tdRefId ?: ''}_tipSearchResultSelectionContainer').html('<em>Please enter your search query</em>');
            return;
        }

        console.log("Performing search for TIPs on: "+searchText);
        $('#tipSearchResultSelectionContainer').html('<asset:image src="spinner.gif" /> Searching...');
        $('#remoteSearchTipResults').html('');

        tdRefSearch${tdRefId ?: ''}_TDs = [];
        tdRefSearch${tdRefId ?: ''}_TIPs = [];

        tdRefSearch${tdRefId ?: ''}_updateAddReferenceButtonStatus();

        if( tdRefSearch${tdRefId ?: ''}_SEARCH_URL != null ){
            $.ajax({
                url: tdRefSearch${tdRefId ?: ''}_SEARCH_URL,
                method: 'POST',
                type: 'POST',
                data: {
                    timestamp: new Date().getTime(),
                    format: 'json',
                    q: searchText
                },
                dataType: 'json',
                success: function(response){
                    console.log("Response: \n"+JSON.stringify(response, null, 2));
                    tdRefSearch${tdRefId ?: ''}_RESPONSE = response;
                    var html = tdRefSearch${tdRefId ?: ''}_styleResponse(response);
                    $('#tdRefSearch${tdRefId ?: ''}_tipSearchResultSelectionContainer').html(html);
                },
                error: function(){
                    $('#tdRefSearch${tdRefId ?: ''}_tipSearchResultSelectionContainer').html('<div class="alert alert-danger">An unexpected error occurred communicating with the server.</div>');
                }
            })
        }else{
            $('#tdRefSearch${tdRefId ?: ''}_tipSearchResultSelectionContainer').html('<div class="alert alert-danger">The search URL has not been defined correctly.</div>');
        }
    }

    /*
    var RESULT_EXAMPLE_1 = {
        "_links": {
            "_formats": [
                {
                    "format": "json",
                    "href": "http://localhost:8090/search/index.json?q=asfd"
                }
            ]
        },
        "queryString": "asfd",
        "terms": [
            "asfd"
        ],
        "tdCountTotal": 1,
        "tipCountTotal": 0,
        "versionSetId": "VS_20170601",
        "results": {
            "tdCount": 0,
            "tipCount": 0,
            "tds": [
                {
                    "Type": "TrustmarkDefinition",
                    "Identifier": "http://localhost:8090/trustmark-definitions/test-td-1/1.0/",
                    "Name": "Test TD 1",
                    "Version": "1.0",
                    "Description": "A test Trustmark definition based on requirements that are vague, intentionally.  This should not be used in production environments.",
                    "Deprecated": false,
                    "_links": {
                        "_formats": [
                            {
                                "format": "json",
                                "href": "http://localhost:8090/trustmark-definitions/test-td-1/1.0/?format=json"
                            },
                            {
                                "format": "html",
                                "href": "http://localhost:8090/trustmark-definitions/test-td-1/1.0/?format=html"
                            },
                            {
                                "format": "xml",
                                "href": "http://localhost:8090/trustmark-definitions/test-td-1/1.0/?format=xml"
                            }
                        ]
                    }
                }
            ],
            "tips": []
        },
        "remoteResults": [
            {
                "Organization": {
                    "Name": "Trustmark Initiative",
                    "Identifier": "https://trustmarkinitiative.org/"
                },
                "tdCount": 1,
                "tdTotalCount": 2282,
                "tds": [
                    {
                        "Type": "TrustmarkDefinition",
                        "Identifier": "https://cjis.trustmarkinitiative.org/lib/trustmark-definitions/revocation-of-tokens-and-credentials-within-72-hours/1.0/",
                        "Name": "Revocation of Tokens and Credentials Within 72 Hours",
                        "Version": "1.0",
                        "Description": "This Trustmark Definition covers requirements on Credential Service Providers (CSPs) for the revocation of tokens and credentials within 72 hours of a CSP becoming notified of a compromise.",
                        "Deprecated": false,
                        "Keywords": [],
                        "PublisherIdentifier": "https://trustmarkinitiative.org/",
                        "PublisherName": "Trustmark Initiative",
                        "_links": {
                            "formats": [
                                {
                                    "format": "json",
                                    "href": "https://cjis.trustmarkinitiative.org/lib/trustmark-definitions/revocation-of-tokens-and-credentials-within-72-hours/1.0/?format=json"
                                },
                                {
                                    "format": "html",
                                    "href": "https://cjis.trustmarkinitiative.org/lib/trustmark-definitions/revocation-of-tokens-and-credentials-within-72-hours/1.0/?format=html"
                                },
                                {
                                    "format": "xml",
                                    "href": "https://cjis.trustmarkinitiative.org/lib/trustmark-definitions/revocation-of-tokens-and-credentials-within-72-hours/1.0/?format=xml"
                                }
                            ]
                        }
                    }
                ],
                "tipCount": 1,
                "tipTotalCount": 927,
                "tips": [
                    {
                        "Type": "TrustInteroperabilityProfile",
                        "Identifier": "https://cjis.trustmarkinitiative.org/lib/trust-interoperability-profiles/nist-800-63-loa-3-applicant-tracking-profile/1.0/",
                        "Name": "NIST 800-63 LOA 3 Applicant Tracking Profile",
                        "Version": "1.0",
                        "Description": "This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on in-person and remote tracking of registration applicants by CSPs.",
                        "Deprecated": false,
                        "Keywords": [],
                        "PublisherIdentifier": "https://trustmarkinitiative.org/",
                        "PublisherName": "Trustmark Initiative",
                        "_links": {
                            "formats": [
                                {
                                    "format": "json",
                                    "href": "https://cjis.trustmarkinitiative.org/lib/trust-interoperability-profiles/nist-800-63-loa-3-applicant-tracking-profile/1.0/?format=json"
                                },
                                {
                                    "format": "html",
                                    "href": "https://cjis.trustmarkinitiative.org/lib/trust-interoperability-profiles/nist-800-63-loa-3-applicant-tracking-profile/1.0/?format=html"
                                },
                                {
                                    "format": "xml",
                                    "href": "https://cjis.trustmarkinitiative.org/lib/trust-interoperability-profiles/nist-800-63-loa-3-applicant-tracking-profile/1.0/?format=xml"
                                }
                            ]
                        }
                ]
            }
        ]
    };

    */

    function tdRefSearch${tdRefId ?: ''}_styleResponse(response){
        var html = '';

        html += '<div>';
        html += tdRefSearch${tdRefId ?: ''}_styleTabs(response);

        html += '<div class="tab-content">';

        html += '<div role="tabpanel" class="tab-pane active" id="localResultContainer">';
        html += tdRefSearch${tdRefId ?: ''}_styleResults(response.results);
        html += '</div>';

        if( response.remoteResults && response.remoteResults.length > 0 ){
            for( var i = 0; i < response.remoteResults.length; i++ ){
                var result = response.remoteResults[i];

                html += '<div role="tabpanel" class="tab-pane" id="resultContainer'+i+'">';
                html += tdRefSearch${tdRefId ?: ''}_styleResults(result);
                html += '</div>';

            }
        }


        html += '</div>';

        html += '</div>';

        return html;
    }

    function tdRefSearch${tdRefId ?: ''}_styleTabs(response){
        var html = '';

        html += '<ul class="nav nav-tabs" role="tablist">';
        html += '<li role="presentation" class="active"><a href="#localResultContainer" aria-controls="localResultContainer" role="tab" data-toggle="tab">Local ('+tdRefSearch${tdRefId ?: ''}_resultCount(response.results)+')</a></li>';
        if( response.remoteResults && response.remoteResults.length > 0 ){
            for( var i = 0; i < response.remoteResults.length; i++ ){
                var result = response.remoteResults[i];
                html += '<li role="presentation"><a href="#resultContainer'+i+'" aria-controls="resultContainer'+i+'" role="tab" data-toggle="tab">'+result.Organization.Name+' ('+tdRefSearch${tdRefId ?: ''}_resultCount(result)+')</a></li>';
            }
        }
        html += '</ul>';

        return html;
    }

    function tdRefSearch${tdRefId ?: ''}_resultCount(results){
        return results.tdCount + results.tipCount;
    }

    function tdRefSearch${tdRefId ?: ''}_hasResults(results){
        return results.tdCount > 0 || results.tipCount > 0;
    }

    function tdRefSearch${tdRefId ?: ''}_styleResults(results){
        var html = '';

        var uniqueId = 'Local';
        if( results.Organization ){
            uniqueId = results.Organization.Name.hashCode();
        }

        if( results.tipCount == 0 && results.tdCount == 0){
            html += '<div id="resultsDataContainer'+uniqueId+'" style="margin-top: 1em;">';
            html += '  <em>There are no results.</em>';
            html += '</div>';
            return html;
        }

        var tdActiveClass = '';
        var tipActiveClass = 'active';
        if( results.tipCount == 0 ){
            tipActiveClass = '';
            tdActiveClass = 'active';
        }

        html += '<div id="resultsDataContainer'+uniqueId+'" style="margin-top: 1em;">';
        html += '<ul class="nav nav-pills" role="tablist">';
        html += '  <li role="presentation" class="'+tipActiveClass+'"><a href="#trustProfilesFor'+uniqueId+'" aria-controls="trustProfilesFor'+uniqueId+'" role="tab" data-toggle="tab">Trust Profiles ('+results.tipCount+')</a></li>';
        html += '  <li role="presentation" class="'+tdActiveClass+'"><a href="#trustmarkDefinitionsFor'+uniqueId+'" aria-controls="trustmarkDefinitionsFor'+uniqueId+'" role="tab" data-toggle="tab">Trustmark Definitions ('+results.tdCount+')</a></li>';
        html += '</ul>';

        html += '<div class="tab-content" style="margin-top: 1em;">';
        html += '  <div role="tabpanel" class="tab-pane '+tipActiveClass+'" id="trustProfilesFor'+uniqueId+'">';
        if( results.tipCount > 0 ){
            html += '<div id="trustProfilesContainerFor'+uniqueId+'" style="max-height: 40em; overflow-y: auto; overflow-x: hidden;">';
            html += '  <table class="table table-condensed table-striped table-bordered">';
            for( var tipIndex = 0; tipIndex < results.tips.length; tipIndex++ ){
                var tip = results.tips[tipIndex];
                html += tdRefSearch${tdRefId ?: ''}_styleTipItem(tip);
            }
            html += '  </table>';
            html += '</div>';
        }else{
            html += '    <div class="alert alert-warning">No Trust Profiles Found.</div>';
        }
        html += '  </div>';
        html += '  <div role="tabpanel" class="tab-pane '+tdActiveClass+'" id="trustmarkDefinitionsFor'+uniqueId+'">';
        if( results.tdCount > 0 ){
            html += '<div id="tdContainerFor'+uniqueId+'" style="max-height: 40em; overflow-y: auto; overflow-x: hidden;">';
            html += '  <table class="table table-condensed table-striped table-bordered">';
            for( var tdIndex = 0; tdIndex < results.tds.length; tdIndex++ ){
                var tip = results.tds[tdIndex];
                html += tdRefSearch${tdRefId ?: ''}_styleTdItem(tip);
            }
            html += '  </table>';
            html += '</div>';
        }else{
            html += '    <div class="alert alert-warning">No Trustmark Definitions Found.</div>';
        }
        html += '  </div>';
        html += '</div>';

        html += '</div>';


        return html;
    }//end results()

    function tdRefSearch${tdRefId ?: ''}_doesTipContainReference(ref){
        <g:if test="${hasTipReferenceFunction}">
        if( true )
            return ${hasTipReferenceFunction}(ref);
        </g:if>
        return false;
    }//end doesTipContainReference()

    function tdRefSearch${tdRefId ?: ''}_findById(id){
        var thing = null;
        if( tdRefSearch${tdRefId ?: ''}_RESPONSE ){
            if( tdRefSearch${tdRefId ?: ''}_RESPONSE.results ) {
                thing = tdRefSearch${tdRefId ?: ''}_findInIdentifiedList(tdRefSearch${tdRefId ?: ''}_RESPONSE.results.tds, id);
                if( thing ) return thing;
                thing = tdRefSearch${tdRefId ?: ''}_findInIdentifiedList(tdRefSearch${tdRefId ?: ''}_RESPONSE.results.tips, id);
                if( thing ) return thing;
            }
            if( !thing && tdRefSearch${tdRefId ?: ''}_RESPONSE.remoteResults){
                for( var i = 0; i < tdRefSearch${tdRefId ?: ''}_RESPONSE.remoteResults.length; i++ ){
                    var remoteResult = tdRefSearch${tdRefId ?: ''}_RESPONSE.remoteResults[i];
                    thing = tdRefSearch${tdRefId ?: ''}_findInIdentifiedList(remoteResult.tds, id);
                    if( thing ) return thing;
                    thing = tdRefSearch${tdRefId ?: ''}_findInIdentifiedList(remoteResult.tips, id);
                    if( thing ) return thing;
                }
            }
        }
        return thing;
    }

    function tdRefSearch${tdRefId ?: ''}_findInIdentifiedList(list, id){
        if( list ){
            for( var i = 0; i < list.length; i++ ){
                if( list[i].Identifier === id ){
                    console.log("Found in list ["+list[i].Identifier+"] === ["+id+"]")
                    return list[i];
                }
            }
        }
        return null;
    }

    function tdRefSearch${tdRefId ?: ''}_styleTipItem(tip){
        var html = '';

        html += '  <tr>';
        html += '    <td style="text-align: center;">';
        if (tdRefSearch${tdRefId ?: ''}_doesTipContainReference(tip)) {
            html += '      <span class="label label-default">Exists</span>';
        } else {
            html += '    <input type="checkbox" onchange="tdRefSearch${tdRefId ?: ''}_onInputCheckboxChangeTip(\''+tip.Identifier+'\')" />';
        }
        html += '    </td>';
        html += '    <td>';
        html += '      <div style="font-weight: bold;">' + tip.Name + ", v" + tip.Version + '</div>';
        html += '      <div class="text-muted" style="font-size: 90%;"><a href="' + tip.Identifier + '" target="_blank">' + tip.Identifier + '</a></div>';
        html += '      <div style="margin-left: 1em; font-size: 90%; color: #666;">';
        html += tip.Description;
        html += '      </div>';
        html += '    </td>';
        html += '  </tr>';

        return html;
    }

    function tdRefSearch${tdRefId ?: ''}_styleTdItem(td){
        var html = '';

        html += '  <tr>';
        html += '    <td style="text-align: center;">';
        if (tdRefSearch${tdRefId ?: ''}_doesTipContainReference(td)) {
            html += '      <span class="label label-default">Exists</span>';
        } else {
            html += '    <input type="checkbox" onchange="tdRefSearch${tdRefId ?: ''}_onInputCheckboxChangeTd(\''+td.Identifier+'\')" />';
        }
        html += '    </td>';
        html += '    <td>';
        html += '      <div style="font-weight: bold;">' + td.Name + ", v" + td.Version + '</div>';
        html += '      <div class="text-muted" style="font-size: 90%;"><a href="' + td.Identifier + '" target="_blank">' + td.Identifier + '</a></div>';
        html += '      <div style="margin-left: 1em; font-size: 90%; color: #666;">';
        html += td.Description;
        html += '      </div>';
        html += '    </td>';
        html += '  </tr>';

        return html;
    }


    var tdRefSearch${tdRefId ?: ''}_TDs = [];
    function tdRefSearch${tdRefId ?: ''}_onInputCheckboxChangeTd(tdIdentifier){
        if( $.inArray(tdIdentifier, tdRefSearch${tdRefId ?: ''}_TDs) > -1 ){
            tdRefSearch${tdRefId ?: ''}_TDs.splice($.inArray(tdIdentifier, tdRefSearch${tdRefId ?: ''}_TDs), 1);
            console.log("Removed TD: "+tdIdentifier);
        }else{
            tdRefSearch${tdRefId ?: ''}_TDs.push(tdIdentifier);
            console.log("Added TD: "+tdIdentifier);
        }

        tdRefSearch${tdRefId ?: ''}_updateAddReferenceButtonStatus();
    }

    var tdRefSearch${tdRefId ?: ''}_TIPs = [];
    function tdRefSearch${tdRefId ?: ''}_onInputCheckboxChangeTip(tipIdentifier){
        if( $.inArray(tipIdentifier, tdRefSearch${tdRefId ?: ''}_TIPs) > -1 ){
            tdRefSearch${tdRefId ?: ''}_TIPs.splice($.inArray(tipIdentifier, tdRefSearch${tdRefId ?: ''}_TIPs), 1);
            console.log("Removed TIP: "+tipIdentifier);
        }else{
            tdRefSearch${tdRefId ?: ''}_TIPs.push(tipIdentifier);
            console.log("Added TIP: "+tipIdentifier);
        }

        tdRefSearch${tdRefId ?: ''}_updateAddReferenceButtonStatus();
    }


    function tdRefSearch${tdRefId ?: ''}_updateAddReferenceButtonStatus(){
        if( tdRefSearch${tdRefId ?: ''}_TIPs.length > 0 || tdRefSearch${tdRefId ?: ''}_TDs.length > 0 ){
            $('#tdRefSearch${tdRefId ?: ''}_addRefButton').removeClass('disabled');
            $('#tdRefSearch${tdRefId ?: ''}_addRefButton').off().on('click.addRef', tdRefSearch${tdRefId ?: ''}_handleAddRefClick);
        }else{
            $('#tdRefSearch${tdRefId ?: ''}_addRefButton').off();
            $('#tdRefSearch${tdRefId ?: ''}_addRefButton').addClass('disabled');
        }
    }


    function tdRefSearch${tdRefId ?: ''}_handleAddRefClick(){
        console.log("Calling Add Reference Click Handler...");
        if( tdRefSearch${tdRefId ?: ''}_TIPs.length < 1 && tdRefSearch${tdRefId ?: ''}_TDs.length < 1){
            return; // Nothing to do here!  Somehow we entered a bad state.
        }

        var objects = [];
        for (var i = 0; i < tdRefSearch${tdRefId ?: ''}_TIPs.length; i++){
            var tipId = tdRefSearch${tdRefId ?: ''}_TIPs[i];
            var obj = tdRefSearch${tdRefId ?: ''}_findById(tipId);
            objects.push(obj);
        }
        for (var i = 0; i < tdRefSearch${tdRefId ?: ''}_TDs.length; i++){
            var tdId = tdRefSearch${tdRefId ?: ''}_TDs[i];
            var obj = tdRefSearch${tdRefId ?: ''}_findById(tdId);
            objects.push(obj);
        }

        <g:if test="${onTipReferenceAddFunction}">
            ${onTipReferenceAddFunction}(objects);
        </g:if>

        $('#tdRefSearch${tdRefId ?: ''}_tipRefSearchText').val('');
        $('#tdRefSearch${tdRefId ?: ''}_tipSearchResultSelectionContainer').html('');
        $('#tdRefSearch${tdRefId ?: ''}_tipReferenceSelectorDialog').modal("hide");
    }

</script>