/*
 * Copyright (c) 2021 Henry 李恒 (henry.box@outlook.com).
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

package pub.ihub.secure.resource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.autoconfigure.security.SecurityProperties.IGNORED_ORDER;
import static org.springframework.http.HttpMethod.GET;
import static pub.ihub.secure.core.Constant.RESOURCE_SCOPES_ENDPOINT_URI;

/**
 * 资源服务配置
 *
 * @author liheng
 */
@EnableWebSecurity
@EnableConfigurationProperties(AuthResourceProperties.class)
@ComponentScan("pub.ihub.secure.resource")
public class AuthResourceServerConfig {

	@Bean
	@Order(IGNORED_ORDER)
	SecurityFilterChain securityFilterChain(HttpSecurity http, AuthResourceProperties properties) throws Exception {
		http
			.authorizeRequests(registry -> registry
				.mvcMatchers(GET, RESOURCE_SCOPES_ENDPOINT_URI).hasAnyAuthority(appendScopePrefix("internal")))
			.authorizeRequests(registry -> properties.getAccessResources().forEach((accessResources) -> registry
				.mvcMatchers(accessResources.getResources()).access(accessResources.getAccess())))
			.authorizeRequests(registry -> properties.getScopeMethodResources().forEach((scope, matchers) ->
				matchers.forEach((method, resources) -> registry
					.mvcMatchers(method, resources).hasAnyAuthority(appendScopePrefix(scope)))))
			.authorizeRequests(registry -> properties.getScopeResources().forEach((scope, resources) -> registry
				.mvcMatchers(resources).hasAnyAuthority(appendScopePrefix(scope))))
			.authorizeRequests().anyRequest().authenticated();
		http.oauth2ResourceServer()
			.jwt();
		return http.build();
	}

	private static String appendScopePrefix(String scope) {
		return "SCOPE_" + scope;
	}

}
