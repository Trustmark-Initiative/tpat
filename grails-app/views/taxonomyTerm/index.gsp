<%@ page import="tmf.host.TaxonomyTerm" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>

        <title>${grailsApplication.config.tf.org.toolheader} | Taxonomy Terms</title>

        <style type="text/css">
        </style>

    </head>
    <body>
        <div id="page-body" role="main">
            <h1>Taxonomy Terms <small>(${tmf.host.TaxonomyTerm.count()} Total)</small></h1>
            <div class="text-muted">On this page, you can view and modify the values for any taxonomy terms.</div>

            <g:if test="${flash.message}">
                <div class="alert alert-success" style="margin-top: 2em;">${flash.message}</div>
            </g:if>
            <g:if test="${flash.error}">
                <div class="alert alert-danger" style="margin-top: 2em;">${flash.error}</div>
            </g:if>

            <div style="margin-top: 2em;"  id="termsContainer">
                <div>
                    <a href="javascript:expandAll()" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-plus"></span> Expand All</a>
                    <a href="javascript:collapseAll()" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-minus"></span> Collapse All</a>
                </div>
                <g:if test="${terms?.size() > 0}">
                    <div class="row">
                        <g:each in="${terms}" var="term" status="status">
                            <div style="margin-top: 2em;" class="col-md-3">
                                <table id="taxonomyTermTreeTable_${term.id}" class="table table-condensed table-bordered">
                                    <tmf:treeTableRowForTaxonomyTerm term="${term}" />
                                </table>
                                <script type="text/javascript">
                                    $(document).ready(function(){
                                        $('#taxonomyTermTreeTable_${term.id}').treetable({expandable: true, clickableNodeNames: true});
                                    })
                                </script>
                            </div>
                            <g:if test="${status % 4 == 3}">
                            </div><div class="row">
                            </g:if>
                        </g:each>
                    </div>
                </g:if>
                <g:else>
                    No Terms
                </g:else>
            </div>
        </div>

    <script type="text/javascript">
        function collapseAll(){
<g:each in="${terms}" var="term" status="status">
            collapseTree(${term.id});
</g:each>
        }
        function expandAll(){
            <g:each in="${terms}" var="term" status="status">
            expandTree(${term.id});
            </g:each>
        }

        function collapseTree(id){
            console.log("Collapsing: "+id);
            $('#taxonomyTermTreeTable_'+id).treetable("collapseAll");
        }
        function expandTree(id){
            console.log("Expanding: "+id);
            $('#taxonomyTermTreeTable_'+id).treetable("expandAll");
        }



        function synchronize(){
            var url = '${createLink(controller:'taxonomyTerm', action:'synchronize')}';
            $('#termsContainer').html('<asset:image src="spinner.gif" /> Contact remote server and downloading taxonomy...');
            $.ajax({
                url: url,
                dataType: 'json',
                data: {
                    timestamp: new Date().getTime(),
                    format: 'json'
                },
                success: function(data){
                    if( data && data.status && data.status === "SUCCESS" ) {
                        $('#termsContainer').html('<div class="alert alert-success">'+data.message+'</div>');
                        setTimeout('window.location.reload()', 200);
                    }else{
                        $('#termsContainer').html('<div class="alert alert-danger">'+data.message+'</div>');
                    }
                },
                error: function(jqXHR, textStatus, errorThrown){
                    console.log("Error!");
                    console.log("Text Status: "+textStatus);
                    console.log("Error Thrown: "+errorThrown);
                    $('#termsContainer').html('<div class="alert alert-danger">Error generating data!</div>');
                }
            })
        }

    </script>
    </body>
</html>
