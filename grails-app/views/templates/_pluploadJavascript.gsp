<%@ page import="org.apache.commons.lang.StringUtils; javax.servlet.ServletException" %>
<g:if test="${pluploadCounter == null}">
    <g:set var="pluploadCounter" value="1" />
</g:if>

<g:if test="${context == null}">
    <% throw new ServletException("To call the _pluploadJavascript.gsp file, you must pass a 'context' param, the description of the context of the pluploader."); %>
</g:if>

<script type="text/javascript">
    var PLUPLOAD${pluploadCounter};
    var LAST_BINARY_ID_${pluploadCounter} = -1;

    $(document).ready(function(){
        console.log("Calling document ready function for plupload init[${pluploadCounter}]...");
        createPlupload${pluploadCounter}();
    });

    function createPlupload${pluploadCounter}(){
        console.log('Initializing plupload [${pluploadCounter}]...')
        var uploader = new plupload.Uploader({
            browse_button: 'fileUploadButton${pluploadCounter}',
            multi_selection: false,
            chunk_size: '100kb',
            max_retries: 0,
            url: '${createLink(controller: 'binary', action: 'upload')}',
            // Flash settings
            flash_swf_url : '${asset.assetPath([src: '/plupload-2.2.0/js/Moxie.swf'])}',
            // Flash settings
            silverlight_xap_url : '${asset.assetPath([src: '/plupload-2.2.0/js/Moxie.xap'])}',
            multipart_params: {
                format: 'json',
                context: '${context}'
            }
        });

        PLUPLOAD${pluploadCounter} = uploader;
        uploader.init();
        uploader.bind('FilesAdded', handleFilesAdded${pluploadCounter});
        uploader.bind('UploadProgress', handleUploadProgress${pluploadCounter});
        uploader.bind('Error', handleUploadError${pluploadCounter});
        uploader.bind('FileUploaded', handleFileUploaded${pluploadCounter});
        uploader.bind('UploadComplete', handleUploadComplete${pluploadCounter});
        console.log('Successfully initialized plupload [${pluploadCounter}]')
    }

    function handleUploadComplete${pluploadCounter}(up) {
//        console.log("handleUploadComplete[${pluploadCounter}]: "+JSON.stringify(up, null, "   "));
        console.log("Upload complete, setting hidden field 'binaryId${pluploadCounter}' to "+LAST_BINARY_ID_${pluploadCounter})
        $('#binaryId${pluploadCounter}').val(LAST_BINARY_ID_${pluploadCounter});
        <g:if test="${uploadCompleteCallback != null && uploadCompleteCallback instanceof java.util.List}">
            <g:each in="${uploadCompleteCallback}" var="callbackFunction">
                try{ ${callbackFunction}(up); } catch( e ) { console.log("Error executing callback '${callbackFunction}': "+e); }
            </g:each>
        </g:if>
        <g:elseif test="${uploadCompleteCallback != null}">
            try{ ${uploadCompleteCallback.toString()}(up); } catch( e ) { console.log("Error executing callback '${uploadCompleteCallback.toString()}': "+e); }
        </g:elseif>
    }
    function handleFileUploaded${pluploadCounter}(up, file, response){
        var responseData = jQuery.parseJSON(response.response);
        console.log("handleFileUploaded${pluploadCounter}: "+JSON.stringify(file, null, "   ")+"\n\n RESPONSE: \n"+JSON.stringify(responseData, null, "   "));
        $('#fileUploadStatus${pluploadCounter}').html('');
        LAST_BINARY_ID_${pluploadCounter} = responseData.binaryId;
    }
    function handleFilesAdded${pluploadCounter}(up, files){
        console.log("handleFilesAdded${pluploadCounter}: "+JSON.stringify(files, null, "   "));
        $('#binaryId${pluploadCounter}').val(-1);
        $('#fileName${pluploadCounter}').html(files[0].name)
        $('#fileUploadStatus${pluploadCounter}').html(
                '<div class="progress" style="width: 400px; height: 5px;">'+
                '<div class="progress-bar" id="fileUploadProgressBar${pluploadCounter}" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">'+
                '<span class="sr-only" id="fileUploadProgressText${pluploadCounter}">0%</span></div></div>'
        )
        LAST_BINARY_ID_${pluploadCounter} = -1;

        <g:if test="${filesAddedCallback != null && filesAddedCallback instanceof java.util.List}">
            <g:each in="${filesAddedCallback}" var="callbackFunction">
                try{ ${callbackFunction}(up, files); } catch( e ) { console.log("Error executing callback '${callbackFunction}': "+e); }
            </g:each>
        </g:if>
        <g:elseif test="${filesAddedCallback != null}">
            try{ ${filesAddedCallback.toString()}(up, files); } catch( e ) { console.log("Error executing callback '${filesAddedCallback.toString()}': "+e); }
        </g:elseif>

        PLUPLOAD${pluploadCounter}.start();

    }
    function handleUploadProgress${pluploadCounter}(up, file){
//        console.log("handleUploadProgress${pluploadCounter}: "+JSON.stringify(file, null, "   "));
        console.log("PLUPLOAD${pluploadCounter} Setting progress bar percentage to: "+file.percent);
        $('#fileUploadProgressBar${pluploadCounter}').width(file.percent+'%');
        $('#fileUploadProgressBar${pluploadCounter}').attr('aria-valuenow', file.percent);
        $('#fileUploadProgressText${pluploadCounter}').html(file.percent+'%');
    }
    function handleUploadError${pluploadCounter}(up, err) {
        console.log("handleUploadError${pluploadCounter}: "+JSON.stringify(err, null, "   "));
        var msg = 'Error Uploading['+err.code+']: '+err.message;
        if( err.response ){
            try {
                var jsonText = err.response;
                console.log("Attemting to parse JSON: "+jsonText);

                // TODO: Problem here is that the JSON returned by grails is not valid JSON and will not parse.
//                var responseData =  $.parseJSON(jsonText);
//                if( responseData && responseData.code && responseData.exception.message ) {
//                    msg = 'Error Uploading[' + responseData.code + ']: ' + responseData.exception.message;
//                }

                msg = jsonText;
            }catch(e){
                console.log("Could not parse error response: " + e);
            }
        }

        $('#fileUploadStatus${pluploadCounter}').html('<div style="color: darkred;">'+msg+'</div>')
    }


</script>