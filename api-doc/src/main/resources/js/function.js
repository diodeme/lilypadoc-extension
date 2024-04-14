$(document).ready(function () {
    $('.details-open').click(function () {
        $(this).closest('tr').closest('tbody').next('tbody.details').toggle();
    });
});