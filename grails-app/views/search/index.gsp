<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Search</title>
        <style type="text/css"></style>

    </head>
    <body>
        <div id="page-body" role="main">

            <div class="header">
                <h1 style="margin: 0; padding: 0;">Search Artifacts</h1>
                <div class="text-muted">
                    On this page, you can search the database for artifacts.
                </div>
            </div>

            <div style="margin-top: 2em;">
                <form class="form-inline" onsubmit="return formSubmit();">
                    <div class="form-group">
                        <label class="sr-only" for="q">Search</label>
                        <input type="text" class="form-control" id="q" placeholder="Query String..." style="width: 40em;" />
                    </div>
                    <a id="searchButton" class="btn btn-default" href="javascript:performSearch();">Search</a>
                    <sec:ifAllGranted roles="ROLE_ORG_ADMIN">
                        <a class="btn btn-warning" href="javascript:rebuildIndex();">Rebuild Index</a>
                    </sec:ifAllGranted>
                </form>
            </div>
            <hr />
            <div id="searchResultsContainer">
                <div id="tdResultsContainer">
                    <em>Please perform your search.</em>
                </div>
                <div id="tipResultsContainer"></div>
            </div>
        </div>

        <script type="text/javascript">

            let MAX_DISPLAY = 10;
            var searchData = null;

            function formSubmit(){
                performSearch();
                return false;
            }

            function performSearch(){
                $('#tdResultsContainer').html('${raw(asset.image(src: 'spinner.gif'))} Searching...');
                var queryString = $('#q').val();
                if( !queryString || queryString.trim() === '' ){
                    $('#tdResultsContainer').html('<div class="alert alert-warning">Please enter some search text.</div>');
                    return;
                }

                $.ajax({
                    url: '${createLink(controller: 'search')}',
                    method: 'POST',
                    type: 'POST',
                    data: {
                        timestamp: new Date().getTime(),
                        q: queryString
                    },
                    dataType: 'json',
                    success: function(html){
                        searchData = html;
                        loadSearchList(1);
                    },
                    error: function(){
                        $('#tdResultsContainer').html('<div class="alert alert-danger">An unexpected error occurred communicating with the server.</div>');
                        $('#tipResultsContainer').html('');
                    }
                })

            }

            <sec:ifAllGranted roles="ROLE_ORG_ADMIN">
                function rebuildIndex(){
                    $('#tdResultsContainer').html('${raw(asset.image(src: 'spinner.gif'))} Rebuilding Index...');
                    $('#tipResultsContainer').html('');

                    $.ajax({
                        url: '${createLink(controller: 'search', action: 'rebuildIndex')}',
                        method: 'POST',
                        type: 'POST',
                        data: {
                            timestamp: new Date().getTime()
                        },
                        dataType: 'html',
                        success: function(html){
                            $('#tdResultsContainer').html(html);
                        },
                        error: function(){
                            $('#tdResultsContainer').html('<div class="alert alert-danger">An unexpected error occurred communicating with the server.</div>');
                        }
                    })

                }
            </sec:ifAllGranted>

            jQuery(function () { jQuery('#q').focus(); });

            function loadSearchList(offset)  {
                $('#tdResultsContainer').html(renderTds(offset));
                $('#tipResultsContainer').html(renderTips(offset));
            }

            function renderTds(offset) {
                var count = 1;
                var html = '<h3>Trustmark Definition Results <small>(' + searchData.results.tds.length +' matching of ' + searchData.tdCountTotal +' total)</small></h3>\n';
                html += '<table class="table table-striped table-bordered table-condensed">\n';
                if( searchData && searchData.results.tds && searchData.results.tds.length > 0 ){
                    searchData.results.tds.forEach(td => {
                        if(count >= offset && count < offset+MAX_DISPLAY)  {
                            html += '<tr><td class="nameCol"><div>';
                            if (td.Deprecated) {
                                html += '<span class="label label-danger" title="This Trustmark Definition should no longer be used.  There are likely replacements or newer Trustmark Definitions available.">Deprecated</span>';
                            }
                            html += '<a href=\"'+ td.Identifier+'?format=html\">' + td.Name + '</a><div style="float: right">';
                            html += '<a href=\"'+ td.Identifier+'?format=html\">XML</a> | <a href=\"'+ td.Identifier+'?format=json\">JSON</a>';
                            html += '</div></div><div class="text-muted" style="margin-left: 0.5em;"><small>'+ td.Description +'</small></div>';
                            html += '</td><td class="versionCol">'+td.Version+'</td></tr>\n';
                        }
                        ++count;
                    });
                } else {
                    html += '<tr><td><em>There are no TDs in this version set</em></td></tr>\n';
                }
                html += '</table>\n';

                if( searchData.results.tds.length > MAX_DISPLAY ) {
                    html += buildPagination(offset, MAX_DISPLAY, searchData.results.tds.length, 'renderTds', false);
                }
                $('#tdResultsContainer').html(html);
            }

            function renderTips(offset) {
                var count = 1;
                var html = '<h3>Trust Interoperability Profile Results <small>(' + searchData.results.tips.length + ' matching of ' + searchData.tipCountTotal + ' total)</small></h3>\n';
                html += '<table class="table table-striped table-bordered table-condensed">\n';
                if( searchData && searchData.results.tips && searchData.results.tips.length > 0 ){
                    searchData.results.tips.forEach(tip => {
                        if(count >= offset && count < offset+MAX_DISPLAY) {
                            html += '<tr><td class="nameCol"><div>';
                            if (tip.Deprecated) {
                                html += '<span class="label label-danger" title="This Trust Profile should no longer be used.  There are likely replacements or newer Trust Profiles available.">Deprecated</span>';
                            }
                            html += '<a href=\"' + tip.Identifier + '?format=html\">' + tip.Name + '</a><div style="float: right">';
                            html += '<a href=\"' + tip.Identifier + '?format=xml\">XML</a> | <a href=\"' + tip.Identifier + '?format=json\">JSON</a>';
                            html += '</div></div><div class="text-muted" style="margin-left: 0.5em;"><small>' + tip.Description + '</small></div>';
                            html += '</td><td class="versionCol">' + tip.Version + '</td></tr>\n';
                        }
                        ++count;
                    });
                }else{
                    html += '<tr><td><em>There are no TIPs in this version set</em></td></tr>\n';
                }
                html += '</table>\n';

                if( searchData.results.tips.length > MAX_DISPLAY ) {
                    html += buildPagination(offset, MAX_DISPLAY, searchData.results.tips.length, 'renderTips', false);
                }
                $('#tipResultsContainer').html(html);
            }
        </script>
    </body>
</html>
