// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better
// to create separate JavaScript files as needed.
//
//= require jquery-2.2.0.min.js
//= require bootstrap-3.3.6/js/bootstrap.min
//= require jquery-treetable-3.2.0/jquery.treetable.js
//= require plupload-2.2.0/js/plupload.full.min.js
//= require plupload-2.2.0/js/moxie.min.js
//= require lorem.js
//= require highlight.pack.js
//= require highlight-at-runtime.js
//= require highlight-trexp.js
//= require highlight-issuance_criteria.js
//= require_self


if (typeof jQuery !== 'undefined') {
    (function($) {
        $('#spinner').ajaxStart(function() {
            $(this).fadeIn();
        }).ajaxStop(function() {
            $(this).fadeOut();
        });
    })(jQuery);
}


/**
 * Given a percentage complete (ie, an integer from 0-100) this method will build HTML for a progress bar and return it.
 */
function renderProgress(progress){
    var html = '';
    html += '<div class="progress">';
    html += '<div class="progress-bar" role="progressbar" aria-valuenow="'+progress+'" aria-valuemin="0" aria-valuemax="100" style="width: '+progress+'%;">';
    html += '<span class="sr-only">'+progress+'% Complete</span>';
    html += '</div>';
    html += '</div>';

    return html;
}


/**
 * @see http://stackoverflow.com/q/7616461/940217
 * @return {number}
 */
String.prototype.hashCode = function(){
    if (Array.prototype.reduce){
        return this.split("").reduce(function(a,b){a=((a<<5)-a)+b.charCodeAt(0);return a&a},0);
    }
    var hash = 0;
    if (this.length === 0) return hash;
    for (var i = 0; i < this.length; i++) {
        var character  = this.charCodeAt(i);
        hash  = ((hash<<5)-hash)+character;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
}



function buildPagination(offset, max, total, callbackFunction){
    return buildPagination(offset, max, total, callbackFunction, true);
}


function buildPagination(offset, max, total, callbackFunction, displayCounts){
    var html = '';
    var pageCount = Math.floor(total / max) + 1;
    var curPage = getCurrentPage(offset, max);
    console.log("Displaying page "+curPage+" of "+pageCount+" pages.");

    var pagesToDisplay = new Array();
    if( pageCount < 11 ){
        for( var i = 1; i <= pageCount; i++ ){
            pagesToDisplay.push(i);
        }
    }else{
        if( curPage < 6 ){
            for( var i = 1; i < 8; i++ ){
                pagesToDisplay.push(i);
            }
            pagesToDisplay.push(pageCount);
        }else if( curPage > (pageCount-6) ){
            pagesToDisplay.push(1);
            for( var i = (pageCount - 6); i <= pageCount; i++ ){
                pagesToDisplay.push(i);
            }
        }else{
            pagesToDisplay.push(1);
            for( var i = (curPage-2); i < (curPage + 3); i++ ){
                pagesToDisplay.push(i);
            }
            pagesToDisplay.push(pageCount);
        }
    }

    var paginationHtml = '';
    paginationHtml += '<nav aria-label="Page navigation">\n';
    paginationHtml += '<ul class="pagination" style="margin-top: 0; margin-bottom: 0;">';
    if( curPage == 1 ){
        paginationHtml += '<li class="disabled"><a href="#" aria-label="Previous"><span aria-hidden="true">&laquo;</span></a></li>';
    }else{
        var lastOffset = offset - max;
        paginationHtml += '<li><a href="javascript:'+callbackFunction+'('+lastOffset+')" aria-label="Previous"><span aria-hidden="true">&laquo;</span></a></li>';
    }
    for( var i = 0; i < pagesToDisplay.length; i++ ){
        var page = pagesToDisplay[i];
        if( page == curPage ){
            paginationHtml += '<li class="active"><a href="#">'+page+'</a></li>';
        }else{
            var curOffset = (page-1) * max;
            paginationHtml += '<li><a href="javascript:'+callbackFunction+'('+curOffset+')">'+page+'</a></li>';
        }
        if( page == 1 && pagesToDisplay[i+1] != 2)
            paginationHtml += '<li class="disabled"><a href="#">...</a></li>';
        else if( i < (pagesToDisplay.length - 1) ){
            if( pagesToDisplay[i+1] != (page + 1) ){
                paginationHtml += '<li class="disabled"><a href="#">...</a></li>';
            }
        }
    }
    if( curPage == pageCount ){
        paginationHtml += '<li class="disabled"><a href="#" aria-label="Next"><span aria-hidden="true">&raquo;</span></a></li>';
    }else{
        var nextOffset = offset + max;
        paginationHtml += '<li><a href="javascript:'+callbackFunction+'('+nextOffset+')" aria-label="Next"><span aria-hidden="true">&raquo;</span></a></li>';
    }
    paginationHtml += '</ul>';
    paginationHtml += '</nav>\n';


    if( displayCounts ){
        html += '<div class="row">';
        html += '<div class="col-md-6 text-muted"><em>\n';
        if( offset + max > total ){
            html += ' Displaying at '+(offset+1)+'-'+total+" of " + total + " items.";
        }else{
            html += ' Displaying at '+(offset+1)+'-'+(offset+max)+" of " + total + " items.";
        }
        html += '</em></div>\n';
        html += '<div class="col-md-6" style="text-align: right;">\n';
        html += paginationHtml;
        html += '</div>\n';
        html += '</div>\n';
    }else{
        html += '<div class="row">';
        html += '<div class="col-md-12" style="text-align: right;">\n';
        html += paginationHtml;
        html += '</div>\n';
        html += '</div>\n';
    }


    return html;
}

function getCurrentPage(offset, max){
    return Math.floor(offset/max) + 1;
}
