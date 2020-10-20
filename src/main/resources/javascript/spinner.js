$(document).ready(function() {
    $("#publicationReport").click(function () {

        var isValid = document.querySelector('#form1');
        if (isValid.checkValidity()) {

            $(".loading-spinner").show();

        }
    });
});
