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


function isEmtpy(str) {
    return (!str || str.length === 0);
}

let setStatusMessage = function(target, data) {
    let html = "";
    //html += "<div id='status-message' class='alert alert-success' class='glyphicon glyphicon-ok-circle'>Copied</div>";

    html = html +
    "<div id='status-message' class='alert alert-success' role='alert' class='glyphicon glyphicon-ok-circle' style='border-radius: 1px; margin-top: 2em;'>"+
    "<button type='button' class='close' data-dismiss='alert' aria-label='Close'><span aria-hidden='True' style='color:#155724; float: right;'>&times;</span></button>"+
    //"<div class='text-center'>" +
    //"<svg width='3em' height='3em' viewBox='0 0 16 16' class='m-1 bi bi-shield-fill-check' fill='currentColor' xmlns='http://www.w3.org/2000/svg'><path fill-rule='evenodd' d='M8 .5c-.662 0-1.77.249-2.813.525a61.11 61.11 0 0 0-2.772.815 1.454 1.454 0 0 0-1.003 1.184c-.573 4.197.756 7.307 2.368 9.365a11.192 11.192 0 0 0 2.417 2.3c.371.256.715.451 1.007.586.27.124.558.225.796.225s.527-.101.796-.225c.292-.135.636-.33 1.007-.586a11.191 11.191 0 0 0 2.418-2.3c1.611-2.058 2.94-5.168 2.367-9.365a1.454 1.454 0 0 0-1.003-1.184 61.09 61.09 0 0 0-2.772-.815C9.77.749 8.663.5 8 .5zm2.854 6.354a.5.5 0 0 0-.708-.708L7.5 8.793 6.354 7.646a.5.5 0 1 0-.708.708l1.5 1.5a.5.5 0 0 0 .708 0l3-3z'/></svg>" +
    //"</div>" +
    "<p style='font-size:80%;' class='mb-0 font-weight-light'><b class='mr-1'>Copied:</b> "+ data +"</p>"+
    "</div>";

    if (!isEmtpy(html)) {
        $('#'+target).html(html);

        $('#'+target).fadeToggle(2000,"swing", function(){ if ($('#'+target).html != "") $('#'+target).html("");});

        //$('#'+target).fadeTo(200, 1);
        //$('#'+target).delay(2000).fadeTo(300, 0);

    }
}

function copyFunction(hrefLinkId) {
  navigator.clipboard.writeText(hrefLinkId);
  setStatusMessage('copyURLtoClipboard-status', hrefLinkId);
}