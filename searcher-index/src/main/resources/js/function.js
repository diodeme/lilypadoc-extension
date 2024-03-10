$(document).ready(function () {
    $("#searcher-input").keydown(function (event) {
        if (event.keyCode === 13) {
            const input = $("#searcher-input").val();
            console.log(input);
            const categoryLevel = $("#searchTypeSelect").find('option:selected').attr('value');
            // 输出值
            console.log(categoryLevel); // 输出: 123
            const response = getTargetPath(input, categoryLevel);
            if (response.errorCode.code === "0") {
                const targetPath = response.result.targetPath;
                const notFoundTitle = response.result.notFoundTitle;
                const notFoundTip = response.result.notFoundTip;
                console.log(targetPath);
                if (isStringEmpty(targetPath)) {
                    alert(notFoundTitle, notFoundTip)
                } else {
                    goToTarget(targetPath);
                }
            } else {
                alert("请求后端失败: " + response.errorCode.message);
            }
        }
    });
});

function getTargetPath(target, Level) {
    let res = '';
    const customPath = '/plugin'; // 自定义的路径名称
    $.ajax({
        url: getCurDestination() + customPath, // 拼接成完整的URL
        type: 'POST', // 请求方式
        data: JSON.stringify({
            name: 'SearcherIndex',
            target: target,
            level: Level
        }),
        async: false,
        contentType: 'application/json',
        success: function (response) {
            // 处理请求成功
            console.log(response);
            res = response;
        },
        error: function (jqXHR, textStatus, errorThrown) {
            // 处理请求失败
            console.error('AJAX error: ' + textStatus + ' - ' + errorThrown);
            return '';
        }
    });
    return res;
}

function isStringEmpty(str) {
    return $.trim(str) === '';
}

function goToTarget(targetPath) {
    // 跳转页面到目标地址
    window.location.href = getCurDestination() + targetPath + (hasSuffix(targetPath) ? "" : "/" + "index.html");
}

function hasSuffix(str) {
    return str.includes('.') && str.lastIndexOf('.') !== str.length - 1;
}

function getCurDestination() {
    const currentURL = window.location.href;
    const urlObject = new URL(currentURL);
    // 获取当前页面的 URL
    let protocolAndHost = urlObject.protocol + '//' + urlObject.hostname;
    if (urlObject.port) {
        protocolAndHost += ':' + urlObject.port;
    }
    return protocolAndHost;
}

function alert(notFoundTitle, notFoundTip) {
    var outer = document.createElement('div');
    outer.classList.add('alert-outer');
    outer.id = 'search-alert';

    // 创建div元素
    var alertBox = document.createElement('div');
    alertBox.setAttribute('role', 'alert');
    alertBox.classList.add('alert', 'alert-box');

    // 设置div的HTML内容
    alertBox.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>';
    var alertInfo = document.createElement('div');
    alertInfo.innerHTML = '<h3 class="font-bold">' + notFoundTitle + '</h3><div class="text-xs">' + notFoundTip + '</div>';
    alertBox.appendChild(alertInfo);
    outer.appendChild(alertBox);
    // 将div添加到body的开头
    document.body.insertBefore(outer, document.body.firstChild);

    // 设置显示和隐藏, 添加动画效果的延迟
    setTimeout(function () {
        outer.classList.add('show');

        setTimeout(function () {
            outer.classList.remove('show');
            outer.classList.add('fade');

            // 这里的代码应该与CSS中的transition属性相匹配
        }, 2000); // 这里的3000表示动画显示持续3000毫秒后开始隐藏的延迟
    }, 10); // 这里的延迟是为了确保动画效果

    const searchAlert = document.getElementById('search-alert');

// 鼠标悬停时, 暂停动画播放
    searchAlert.addEventListener('mouseover', function () {
        this.style.animationPlayState = 'paused';
    });

// 鼠标移开时, 恢复动画播放
    searchAlert.addEventListener('mouseout', function () {
        this.style.animationPlayState = 'running';
    });

// 监听动画结束事件来设置display:none;
    searchAlert.addEventListener('animationend', function () {
        if (this.classList.contains('fade')) {
            this.style.display = 'none';
            document.body.removeChild(outer);
        }
    });
}