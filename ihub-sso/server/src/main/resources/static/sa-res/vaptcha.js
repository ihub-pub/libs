/*
 * Copyright (c) 2022 Henry 李恒 (henry.box@outlook.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
vaptcha({
    vid: '623c36f7e82e6539de8fafe9',
    mode: 'click',
    scene: 0,
    container: '#vaptchaContainer',
    area: 'auto',
}).then(function (vaptchaObj) {
    //将VAPTCHA验证实例保存到局部变量中
    obj = vaptchaObj;

    // 渲染验证组件
    vaptchaObj.render();

    // 验证成功进行后续操作
    vaptchaObj.listen('pass', function () {
        serverToken = vaptchaObj.getServerToken();
        var data = {
            server: serverToken.server,
            token: serverToken.token,
        }

        // 点击登录向服务器端接口提交数据，以下为伪代码，仅作参考
        $.post('/login', data, function (r) {
            if (r.code == 200) {
                console.log('登录成功')
            } else {
                console.log('登录失败')

                // 账号或密码错误等原因导致登录失败，重置人机验证
                vaptchaObj.reset()
            }
        })
    })
})
