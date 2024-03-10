let ticking = false
let isAnimating = false
$(window).scroll(function () {
    if (!ticking) {
        window.requestAnimationFrame(function () {
            if (!isAnimating) {
                changeToc()
            }
            ticking = false
        })
        ticking = true
    }
})

function changeToc() {
    var closest = null
    var closestDistance = Infinity // 初始值为无限大, 后面任何距离都会小于它
    var outClosestObj = null;
    var outClosestDistance = -Infinity;

    $('#doc h1, #doc h2, #doc h3, #doc h4, #doc h5, #doc h6').each(function () {
        var fromTop = $(this).offset().top - $(window).scrollTop()

        // 如果元素在可视范围内
        if (fromTop > 0 && fromTop < $(window).height()) {
            if (closest == null || fromTop < closestDistance) {
                closest = $(this)
                closestDistance = fromTop
            }
        }

        if (fromTop < 0) {
            if (outClosestObj == null || fromTop > outClosestDistance) {
                outClosestObj = $(this);
                outClosestDistance = fromTop
            }
        }
    })

    // 如果找到了最近的目标元素的话
    if (closest != null) {
        var id = closest.attr('id')
        $('#toc a').removeClass('toc-active') // 移除其他元素的激活状态
        $('#toc a[href="#' + id + '"]').addClass('toc-active') // 激活最近的目标元素
    } else if (outClosestObj != null) {
        var id = outClosestObj.attr('id')
        $('#toc a').removeClass('toc-active') // 移除其他元素的激活状态
        $('#toc a[href="#' + id + '"]').addClass('toc-active') // 激活最近的目标元素
    }
}

function adjustTocHeight() {
    var toc = $('#toc')
    var windowHeight = $(window).height()
    var bodyTop = $('#body').offset().top
    var footerTop = $('#footer').innerHeight()

    toc.css('height', windowHeight - bodyTop - footerTop - 1)
}

// 当窗口大小发生变化的时候调整高度
$(window).resize(adjustTocHeight)

window.methodAfter['toc'] = function () {
    // 调整目录的高度
    adjustTocHeight()
}

$(document).ready(function () {
    $('#toc a').click(function () {
        isAnimating = true // 标记动画状态
        var hash = decodeURIComponent(this.hash)
        var targetId = hash.substring(1) // 移除 '#' 获取真实的ID
        var target = document.getElementById(targetId) // 依据真实的ID获取元素

        if (target) {
            // 滚动到对应的目标元素
            $('html, body').animate(
                {
                    scrollTop: $(target).offset().top
                },
                450,
                function () {
                    isAnimating = false // 动画结束后, 标记动画结束
                    window.location.hash = hash // 修改, 更新URL的hash (导航标记)
                }
            )
        } else {
            console.log('Target not found: ', targetId)
        }
        $('#toc a').removeClass('toc-active') // 移除其他元素的激活状态
        $(this).addClass('toc-active') // 激活当前的目标元素
    })
})