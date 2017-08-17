/**
 * Created by remi on 17/01/15.
 */
(function () {

    var uploadfiles = document.querySelector('#uploadfiles');
    uploadfiles.addEventListener('change', function () {
        var files = this.files;
        for(var i=0; i<files.length; i++){
            uploadFile(this.files[i]);
        }

    }, false);


    /**
     * Upload a file
     * @param file
     */
    function uploadFile(file){
        var url = "/UploadFileServlet";
        var xhr = new XMLHttpRequest();
        var fd = new FormData();
        xhr.open("POST", url, true);
        xhr.onreadystatechange = function() {
            if (xhr.readyState == 4 && xhr.status == 200) {
                // Every thing ok, file uploaded
            	alert("tODO OK" + xhr.responseText);
                console.log(xhr.responseText); // handle response.
                window.location.href = "http://firma-isisconsultores.rhcloud.com/pdfjs/web/viewer.html?file=documentos/transferencia.pdf";
            }
        };
        fd.append('uploaded_file', file);
        xhr.send(fd);
    }
}());