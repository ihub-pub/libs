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
package pub.ihub.sso.resource;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liheng
 */
@Configuration
@EnableConfigurationProperties(SsoResourceProperties.class)
@ConditionalOnReactiveDiscoveryEnabled
@ConditionalOnClass(SaReactorFilter.class)
public class SsoResourceReactorAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SaReactorFilter getSaReactorFilter() {
		return new SaReactorFilter()
			// 拦截地址
			.addInclude("/**")
			// 开放地址
			.addExclude("/favicon.ico", "/sso-client")
			// 鉴权方法：每次访问进入
			.setAuth(obj -> {
				// 登录校验 -- 拦截所有路由，并排除/sso/login 用于开放登录
				SaRouter.match("/**", "/*/sso/login", r -> StpUtil.checkLogin());

				// 权限认证 -- 不同模块, 校验不同权限
				SaRouter.match("/user/**", r -> StpUtil.checkPermission("user"));
				SaRouter.match("/admin/**", r -> StpUtil.checkPermission("admin"));
				SaRouter.match("/goods/**", r -> StpUtil.checkPermission("goods"));
				SaRouter.match("/orders/**", r -> StpUtil.checkPermission("orders"));
			})
			.setError(e -> SaResult.error(e.getMessage()));
	}

}
