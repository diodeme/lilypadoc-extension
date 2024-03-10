$(window).scroll(function () {
    let sidebar = $('#sidebar')
    sidebar.css({ position: 'sticky', top: '0' })
})

function adjustSidebarHeight () {
    var windowHeight = $(window).height()
    var documentHeight = $('#body').offset().top + $('#body').innerHeight()
    var headerIconHeight = $('.sidebar_header_icon').innerHeight();
    var sidebar = $('#sidebar_menu')

    if (documentHeight > windowHeight) {
        // 如果文档的总高度大于窗口的高度则设置为窗口高度
        sidebar.css('height', windowHeight-headerIconHeight)
    } else {
        // 如果文档的总高度小于或等于窗口的高度则设置为文档的总高度
        sidebar.css('height', documentHeight-headerIconHeight)
    }
}

window.methodAfter['sidebar'] = function () {
    $('.sidebar_header_icon').html($('.header_icon').html());

    // 在文档加载完成后调整侧边栏高度
    adjustSidebarHeight()
};