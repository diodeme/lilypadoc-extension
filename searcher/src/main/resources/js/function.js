$(document).ready(function () {
    // 监听select元素的change事件
    $('#searchTypeSelect').change(function () {
        changePlaceHolder()
    })
})

function changePlaceHolder(){
    var selectedOption = $('#searchTypeSelect').find('option:selected').text()
    // 更新input的placeholder属性
    $(".input input[type='text']").attr('placeholder', '请输入' + selectedOption)
}

window.methodAfter['searcher'] = function() {
    changePlaceHolder()
}