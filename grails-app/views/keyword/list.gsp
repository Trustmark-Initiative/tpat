<html>
    <head>
        <meta name="layout" content="main"/>
        <title>${grailsApplication.config.tf.org.toolheader} | Keyword Index</title>

        <style type="text/css">
            .overallIndexLinkTable td {
                padding: 1em;
            }

            .overallIndexLinkTableButtonContainer {
                padding: 1em;
                font-size: 110%;
                font-weight: bold;
            }

            .keywordContainer {
            }

            .keywordContainerTitle {
                margin-bottom: 0.2em;
            }

            .keywordListContainer {
                border-top: 0.05em solid #AAA;
                height: auto;
                max-height: 50em;
                overflow: scroll;
            }

            .keywordList {
                margin: 0;
                padding: 0;
                list-style: none;
            }

            .keywordList li {
                margin: 0;
                padding: 0.5em;
                padding-left: 0;
                overflow: hidden;
                text-wrap: none;
            }

        </style>
    </head>

    <body>

        <div class="containerBody" style="margin-top: 2em;">
            <h1>Keyword Index</h1>
            <div class="text-muted">
                A listing of all TDs and TIPs on this server, grouped by keyword.  All keywords
                appear to the right, just click to display the relevant artifacts.
            </div>

            <div style="margin-top: 0.5em;">
                <div class="row">
                    <div class="col-md-9" id="mainKeywordContent">
                        <asset:image src="spinner.gif" /> Content Loading...
                    </div>
                    <div class="col-md-3" id="keywordList">
                        <asset:image src="spinner.gif" /> Keyword List Loading...
                    </div>

                </div>
            </div>

        </div>


        <script type="text/javascript">
            var KEYWORDS = null;
            var KEYWORD_SETS = null;
            var CURRENT_KEYWORD_DATA = null;

            $(document).ready(function(){
                CURRENT_KEYWORD_DATA = null;
                setTimeout('getData()', 250);
            })//end document ready function

            function getData(){
                console.log("Getting data...");

                $.ajax({
                    url: '${createLink(controller:'keyword', action: 'list')}',
                    dataType: 'json',
                    headers: {
                        Accept : "application/json"
                    },
                    data: {timestamp: new Date().getTime(), format: 'json'},
                    error: function(jqXHR, textStatus, err) {
                        displayError('Error downloading keyword List!  '+jqXHR.responseText);
                    },
                    success: function(data, textStatus, jqXHR){
                        console.log("received keyword data: "+JSON.stringify(data));
                        if( data != null && data.keywords != null ) {
                            KEYWORDS = data.keywords;
                            KEYWORD_SETS = data.highlightSets;
                            setTimeout('styleKeywordList()', 200);
                        } else
                            displayError('No keyword data found!')
                    }
                })
            }

            function displayError( errormsg ){
                $('#mainKeywordContent').html('<div class="errorText">'+errormsg+'</div>');
            }

            function styleKeywordList(){
                var html = "";
                var firstKeyword = null;
                html += '<div class="keywordContainer">';
                if( KEYWORDS != null && KEYWORDS.length > 0 ) {
                    var keywordNotIgnoredCount = 0;
                    for( var i = 0; i < KEYWORDS.length; i++ ){
                        var keywordData = KEYWORDS[i];
                        if( keywordData.ignore == false )
                            keywordNotIgnoredCount++;
                    }


                    if( KEYWORD_SETS != null ){
                        for( var keywordSetIndex = 0; keywordSetIndex < KEYWORD_SETS.length; keywordSetIndex++ ){
                            var keywordSet = KEYWORD_SETS[keywordSetIndex];
                            html += '    <h3 class="keywordContainerTitle">'+keywordSet.name+' <small>(' +keywordSet.keywords.length+ ')</small></h3>';
//                            html += '<hr/>';
                            html += '    <div class="keywordListContainer">\n';
                            html += '        <ul class="keywordList">';
                            for( var i = 0; i < keywordSet.keywords.length; i++ ){
                                var keyword = keywordSet.keywords[i];
                                var artifactCount = 0;
                                for( var j = 0; j < KEYWORDS.length; j++ ) {
                                    var keywordData = KEYWORDS[j];
                                    if( keyword.toUpperCase() === keywordData.name.toUpperCase() ){
                                        artifactCount = keywordData.tdCount + keywordData.tipCount;
                                    }
                                }

                                html += '        <li><a href="javascript:loadKeyword(\'' + keyword + '\')">' + keyword + ' ('+artifactCount+')</a></li>';
                                if( firstKeyword == null && artifactCount > 0 )
                                    firstKeyword = keyword;

                            }
                            html += '        </ul>';
                            html += '    </div>\n';
                        }
                    }


                    html += '    <h3 class="keywordContainerTitle">All Keywords <small>(' + keywordNotIgnoredCount + ')</small></h3>';
//                    html += ' <hr />';
                    html += '    <div class="keywordListContainer">\n';
                    html += '        <ul class="keywordList">';
                    for( var i = 0; i < KEYWORDS.length; i++ ){
                        var keywordData = KEYWORDS[i];
                        if( keywordData.ignore == false ) {
                            html += '        <li><a href="javascript:loadKeyword(\'' + keywordData.name + '\')">' + keywordData.name + ' (' + keywordData.count + ')</a></li>';

                            if( firstKeyword == null && keywordData.count > 0 )
                                firstKeyword = keywordData.name;
                        }
                    }
                    html += '        </ul>';
                    html += '    </div>\n';

                }else{
                    html += '    <div class="keywordContainerTitle">Keywords</div>';
                    html += '    <div class="keywordData"><em>There are no keywords on the server.</em></div>';
                }

                html += '</div>';

                if( firstKeyword ){
                    loadKeyword(firstKeyword);
                }

                $('#keywordList').html(html);
            }//end styleKeywordList()

            function loadKeyword(keyword){
                var url = "${createLink(controller:'keyword', action: 'view', id: '_REPLACEME_')}";
                url = url.replace("_REPLACEME_", keyword);
                $('#mainKeywordContent').html('<asset:image src="spinner.gif" /> Loading...');
                $.ajax({
                    "url" : url,
                    dataType: 'html',
                    data: {timestamp : new Date().getTime(), format: "html"},
                    error: function(jqXHR, textStatus, err) {
                        displayError(jqXHR.responseText);
                    },
                    success: function(data, textStatus, jqXHR){
                        $('#mainKeywordContent').html(data);

//                        if( data != null ) {
//                            CURRENT_KEYWORD_DATA = data;
//                            setTimeout('styleKeywordData()', 100);
//                        }else{
//                            CURRENT_KEYWORD_DATA = null;
//                            displayError('No keyword data found!')
//                        }
                    }
                });
            }

            function styleKeywordData(){
                var html = '';

                html += '<div class="keywordData">';
                if( CURRENT_KEYWORD_DATA != null ){
                    if( CURRENT_KEYWORD_DATA.tds.length == 1 ){
                        html += '<h2 class="keywordTitle">' + CURRENT_KEYWORD_DATA.keyword + " <small>(" + CURRENT_KEYWORD_DATA.tdCount + " Trustmark Definition)</small></h2>"
                    }else if( CURRENT_KEYWORD_DATA.tds.length > 1 ) {
                        html += '<h2 class="keywordTitle">' + CURRENT_KEYWORD_DATA.keyword + " <small>(" + CURRENT_KEYWORD_DATA.tdCount + " Trustmark Definitions)</small></h2>"
                    }else{
                        html += '<h2 class="keywordTitle">' + CURRENT_KEYWORD_DATA.keyword + " <small>(<em>NO</em> Trustmark Definitions)</small></h2>"
                    }
                    html += '<table class="table table-striped table-bordered table-condensed">';
                    html += '<thead><tr>';
                    html += '<th style="width: 70%;">Trustmark Definition Name</th>';
                    html += '<th style="width: 10%; text-align: center;">Version</th>';
                    html += '<th style="width: 10%; text-align: center;">HTML</th>';
                    html += '<th style="width: 10%; text-align: center;">XML</th>';
                    html += '</tr></thead><tbody>';
                    for( var i = 0; i < CURRENT_KEYWORD_DATA.tds.length; i++ ){
                        var td = CURRENT_KEYWORD_DATA.tds[i];
                        html += '<tr>';
                        html += '<td>'+td.name+'</td>';
                        html += '<td style="width: 10%; text-align: center;">'+td.version+'</td>';
                        html += '<td style="width: 10%; text-align: center;"><a href="'+td.relativeHtmlPath+'">HTML</a></td>';
                        html += '<td style="width: 10%; text-align: center;"><a href="'+td.relativeXmlPath+'">XML</a></td>';
                        html += '</tr>';
                    }
                    html += '</tbody></table>';
                }else{
                    html += '<em>No valid data found!</em>';
                }
                html += '</div>';

                $('#mainKeywordContent').html(html);
            }//end styleKeywordData()

        </script>
    </body>

</html>
