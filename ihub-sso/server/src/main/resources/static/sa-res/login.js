// sa
var sa = {};

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

function login(captchaVerification){
	sa.loading("正在登录...");
	// 开始登录
	setTimeout(function() {
		$.ajax({
			url: "sso/doLogin",
			type: "post",
			data: {
				name: $('[name=name]').val(),
				pwd: $('[name=pwd]').val(),
                captchaVerification: captchaVerification
			},
			dataType: 'json',
			success: function(res){
				sa.hideLoading();
				if(res.code == 200) {
					layer.msg('登录成功', {anim: 0, icon: 6 });
					setTimeout(function() {
						location.reload();
					}, 800)
				} else {
					layer.msg(res.msg, {anim: 6, icon: 2 });
				}
			},
			error: function(xhr, type, errorThrown){
				sa.hideLoading();
				if(xhr.status == 0){
					return layer.alert('无法连接到服务器，请检查网络');
				}
				return layer.alert("异常：" + JSON.stringify(xhr));
			}
		});
	}, 400);
}

$('#btn').slideVerify({
	baseUrl:'',
	mode:'pop',     //展示模式
	containerId:'btn',//pop模式 必填 被点击之后出现行为验证码的元素id
	imgSize : {       //图片的大小对象,有默认值{ width: '310px',height: '155px'},可省略
		width: '400px',
		height: '200px',
	},
	barSize:{          //下方滑块的大小对象,有默认值{ width: '310px',height: '50px'},可省略
		width: '400px',
		height: '40px',
	},
	beforeCheck:function(){  //检验参数合法性的函数  mode ="pop"有效
		if ($('[name=name]').val() === '') {
			layer.msg("账号不能为空", {anim: 6, icon: 2 });
			return false
		}
		if ($('[name=pwd]').val() === '') {
			layer.msg("密码不能为空", {anim: 6, icon: 2 });
			return false
		}
		return true
	},
	ready : function() {},  //加载完毕的回调
	success : function(params) { //成功的回调
		login(params.captchaVerification)
	},
	error : function() {}        //失败的回调
});

// 绑定回车事件
$('[name=name],[name=pwd]').bind('keypress', function(event){
	if(event.keyCode == "13") {
		$('.login-btn').click();
	}
});

// 输入框获取焦点
$("[name=name]").focus();

// 打印信息
var str = "This page is provided by IHub, Please refer to: " + "https://ihub.pub/";
console.log(str);
