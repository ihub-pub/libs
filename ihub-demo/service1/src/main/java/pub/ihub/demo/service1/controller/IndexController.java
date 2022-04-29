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
package pub.ihub.demo.service1.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pub.ihub.demo.service1.feign.DemoResourceClient;

/**
 * @author liheng
 */
@AllArgsConstructor
@RestController
public class IndexController {

	private final DemoResourceClient resourceClient;

	@RequestMapping("/")
	public String index() {
		return "<h2>Sa-Token SSO-Client 应用端</h2>" +
			"<p>当前会话是否登录：" + StpUtil.isLogin() + "</p>" +
			"<p>登录用户：" + JSONUtil.toJsonPrettyStr(StpUtil.getTokenInfo()) + "</p>" +
			"<p>请求资源服务：" + resourceClient.index() + "</p>" +
			"<p><a href=\"javascript:location.href='/sso-client/sso/login?back=' + encodeURIComponent(location.href);\">登录</a> " +
			"<a href='/sso-client/sso/logout?back=self'>注销</a></p>";
	}

}
