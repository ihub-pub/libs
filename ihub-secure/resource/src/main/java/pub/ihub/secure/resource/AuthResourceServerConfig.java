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

import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.Writer;
import java.util.HashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.boot.autoconfigure.security.SecurityProperties.IGNORED_ORDER;
import static pub.ihub.secure.core.Constant.RESOURCE_APIS_ENDPOINT_URI;
import static pub.ihub.secure.core.Constant.RESOURCE_INTERNAL_URI;
import static pub.ihub.secure.core.Constant.RESOURCE_SCOPES_ENDPOINT_URI;

/**
 * 资源服务配置
 *
 * @author liheng
 */
@EnableWebSecurity
@EnableConfigurationProperties(AuthResourceProperties.class)
public class AuthResourceServerConfig {

	@Bean
	@Order(IGNORED_ORDER)
	SecurityFilterChain securityResourceFilterChain(HttpSecurity http, AuthResourceProperties properties,
													RequestMappingHandlerMapping handlerMapping) throws Exception {
		http
			.authorizeRequests(registry -> registry
				.mvcMatchers(RESOURCE_INTERNAL_URI + "/**").hasAnyAuthority(appendScopePrefix("internal")))
			.authorizeRequests(registry -> properties.getScopeResources().forEach((scope, resources) -> registry
				.mvcMatchers(resources).hasAnyAuthority(appendScopePrefix(scope))))
			.authorizeRequests().anyRequest().authenticated();

		http.oauth2ResourceServer().jwt();

		http.addFilterAfter(doFilter(RESOURCE_SCOPES_ENDPOINT_URI, properties::getScopeResources),
			FilterSecurityInterceptor.class);
		http.addFilterAfter(doFilter(RESOURCE_APIS_ENDPOINT_URI, () -> handlerMapping.getHandlerMethods().keySet()
			.stream().map(info -> new HashMap<String, Object>(2) {{
				put("methods", info.getMethodsCondition().getMethods());
				put("patterns", info.getPatternValues());
			}}).collect(Collectors.toSet())), FilterSecurityInterceptor.class);

		return http.build();
	}

	@SneakyThrows
	private Filter doFilter(String matcherUri, Supplier<?> supplier) {
		RequestMatcher requestMatcher = new AntPathRequestMatcher(matcherUri, HttpMethod.GET.name());
		return (ServletRequest request, ServletResponse response, FilterChain chain) -> {
			if (!requestMatcher.matches((HttpServletRequest) request)) {
				chain.doFilter(request, response);
				return;
			}
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			try (Writer writer = response.getWriter()) {
				writer.write(JSONUtil.toJsonStr(supplier.get()));
			}
		};
	}

	private static String appendScopePrefix(String scope) {
		return "SCOPE_" + scope;
	}

}
