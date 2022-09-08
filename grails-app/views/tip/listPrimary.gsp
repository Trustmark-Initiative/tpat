<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>${grailsApplication.config.tf.org.toolheader} | Primary TIPs</title>
        <style type="text/css">

        </style>


    </head>
    <body>
        <div id="page-body" role="main">
            <div>
                <h1>Primary Trust Interoperability Profiles <small>(${vs.name})</small></h1>
                <div>
                    <div style="float: left;">

                    </div>
                </div>
            </div>
            <g:if test="${flash.message}">
                <div style="margin-top: 2em; margin-bottom: 2em;" class="alert alert-success">${flash.message}</div>
            </g:if>
            <g:if test="${flash.error}">
                <div style="margin-top: 2em; margin-bottom: 2em;" class="alert alert-danger">${flash.error}</div>
            </g:if>
            <div>
                <table class="table table-striped table-condensed table-bordered">
                    <tbody>
                        <g:if test="${topLevelTips?.size() > 0}">
                            <g:each in="${topLevelTips}" var="tip">
                                <tr>
                                    <td>
                                        <g:link controller="tip" action="modifyPrimary" params="[name: tip.name, version: tip.tipVersion, primary: false]" class="btn btn-default">
                                            <span class="glyphicon glyphicon-eye-close" title="Remove Primary"></span>
                                        </g:link>
                                    </td>
                                    <td>
                                        <div style="font-size: 110%; font-weight: bold;">${tip.name}, v${tip.tipVersion}</div>
                                        <div><%= LinkHelper.linkifyText(tip.description) %></div>
                                    </td>
                                </tr>
                            </g:each>
                        </g:if>
                        <g:else>
                            <tr>
                                <td colspan="2">
                                    <em>There are no Trust Interoperability Profiles defined.</em>
                                </td>
                            </tr>
                        </g:else>
                    </tbody>
                </table>
            </div>
            <div style="margin-top: 2em;">
                <div>
                    <h4>Add Primary TIP</h4>
                    <form class="form-inline" onsubmit="search(); return false;">
                        <div class="form-group">
                            <input type="searchBox" class="form-control" id="searchBox" placeholder="Search...">
                        </div>
                        <a href="javascript:search();" class="btn btn-primary">Search</a>
                    </form>
                </div>
                <div id="searchFeedback" style="margin-top: 2em;">

                </div>
            </div>
        </div>

        <script type="text/javascript">
            function search() {
                var q = $('#searchBox').val();
                if( q != null && q.trim().length > 0  ){
                    $('#searchFeedback').html('<asset:image src="spinner.gif" /> Searching...');
                    $.ajax({
                        url: '${createLink(controller: 'search')}',
                        data: {
                            q: q,
                            format: 'json',
                            timestamp: new Date().getTime()
                        },
                        dataType: 'json',
                        success: function(data){
                            console.log("Received response!  Of "+data.tipCountTotal+" total tips, matched "+data.results.tipCount);
                            renderResults(data.tipCountTotal, data.results.tipCount, data.results.tips);
                            RESULTS = data.results.tips;
                        },
                        error: function(jqXHR, textStatus, errorThrown){
                            alert("ERROR");
                        }
                    })
                }
            }

            var RESULTS = null;

            function renderResults(total, matchCount, tips) {
                var html = '';

                html += '<div>';
                if( matchCount > 0 ) {
                    html += '  <h4>Select Primary TIPs</h4>'

                    html += '<table class="table table-condensed table-bordered table-striped">';
                    for( var i = 0; i < tips.length; i++ ){
                        var tip = tips[i];
                        var url = '${createLink(controller: 'tip', action: 'modifyPrimary')}?name='+encodeURIComponent(tip.Name)+'&version='+encodeURIComponent(tip.Version)+'&primary=true';
                        html += '<tr>';
                        html += '  <td><a href="'+url+'" class="btn btn-default"><span class="glyphicon glyphicon-eye-open" title="Make Primary"></span></a></td>\n'
                        html += '  <td>';
                        html += '     <div style="font-size: 110%; font-weight: bold;">'+tip.Name+', v'+tip.Version+'</div>';
                        html += '     <div>'+tip.Description+'</div>';
                        html += '  </td>';
                        html += '</tr>';
                    }
                    html += '</table>\n';

                }else{
                    html += '<div class="alert alert-warning">There were no Trust Profiles matching your query.</div>\n';
                }
                html += '</div>';

                $('#searchFeedback').html(html);
            }

        </script>

    </body>
</html>
