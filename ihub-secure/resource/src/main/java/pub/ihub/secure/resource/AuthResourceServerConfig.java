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

import cn.hutool.core.util.ArrayUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.util.stream.Collectors;

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
		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests =
			http.authorizeRequests();
		requests.mvcMatchers(GET, RESOURCE_SCOPES_ENDPOINT_URI).hasAnyAuthority("SCOPE_resource");

		properties.getResourceScope().forEach((resource, scope) -> requests
			.mvcMatchers(resource).hasAnyAuthority(appendScopePrefix(scope)));
		properties.getScopeResource().forEach((scope, resource) -> requests
			.mvcMatchers(resource).hasAnyAuthority(appendScopePrefix(scope)));
		properties.getResourceScopes().forEach((resource, scopes) -> requests
			.mvcMatchers(resource).hasAnyAuthority(ArrayUtil.toArray(scopes.stream()
				.map(AuthResourceServerConfig::appendScopePrefix).collect(Collectors.toList()), String.class)));
		properties.getScopeResources().forEach((scope, resources) -> requests
			.mvcMatchers(ArrayUtil.toArray(resources, String.class)).hasAnyAuthority(appendScopePrefix(scope)));
		properties.getAccessResources().forEach((access, resources) -> requests
			.mvcMatchers(ArrayUtil.toArray(resources, String.class)).access(access));
		// TODO 支持带method配置

		requests.anyRequest().authenticated();

		http.oauth2ResourceServer()
			.jwt();
		return http.build();
	}

	private static String appendScopePrefix(String scope) {
		return "SCOPE_" + scope;
	}

}
