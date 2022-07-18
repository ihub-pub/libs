// sa
const sa = {};

// 打开loading
sa.loading = function(msg) {
	layer.closeAll();	// 开始前先把所有弹窗关了
	return layer.msg(msg, {icon: 16, shade: 0.3, time: 1000 * 20, skin: 'ajax-layer-load' });
};

// 隐藏loading
sa.hideLoading = function() {
	layer.closeAll();
};


// ----------------------------------- 登录事件 -----------------------------------

$('.login-btn').click(function(){
	if ($('[name=name]').val() === '') {
		layer.msg("账号不能为空", {anim: 6, icon: 2 });
		return false
	}
	if ($('[name=pwd]').val() === '') {
		layer.msg("密码不能为空", {anim: 6, icon: 2 });
		return false
	}
    const captcha = $('[name=captcha]')
	if (captcha.length > 0 && captcha.val() === '') {
		layer.msg("验证码不能为空", {anim: 6, icon: 2 });
		return false
	}
	sa.loading("正在登录...");
	// 开始登录
	setTimeout(function() {
		$.ajax({
			url: "sso/doLogin",
			type: "post",
			data: {
				name: $('[name=name]').val(),
				pwd: $('[name=pwd]').val(),
				captcha: $('[name=captcha]').val()
			},
			dataType: 'json',
			success: function(res){
				sa.hideLoading();
				if(res.code === 0) {
					layer.msg('登录成功', {anim: 0, icon: 6 });
					setTimeout(function() {
						location.reload();
					}, 800)
				} else {
					layer.msg(res.message, {anim: 6, icon: 2 });
					$('.s-captcha').click();
				}
			},
			error: function(xhr, type, errorThrown){
				sa.hideLoading();
				$('.s-captcha').click();
				if(xhr.status === 0){
					return layer.alert('无法连接到服务器，请检查网络');
				}
				return layer.alert("异常：" + JSON.stringify(xhr));
			}
		});
	}, 400);
})

// 绑定回车事件
$('[name=name],[name=pwd],[name=captcha]').bind('keypress', function(event){
	if(event.keyCode === "13") {
		$('.login-btn').click();
	}
});

$('.s-captcha').click(function(){
	this.src = "/captcha?" + new Date().getTime()
})

// 输入框获取焦点
$("[name=name]").focus();

// 打印信息
console.log("This page is provided by IHub, Please refer to: " + "https://ihub.pub/");
