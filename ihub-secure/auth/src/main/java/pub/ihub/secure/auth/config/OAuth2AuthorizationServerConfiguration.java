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

package pub.ihub.secure.auth.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import pub.ihub.secure.auth.web.filter.OAuth2ClientAuthenticationFilter;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.RegisteredClientRepository;

import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;

/**
 * @author henry
 */
@Configuration
public class OAuth2AuthorizationServerConfiguration {

	// TODO 整理配置文件
	public static final String ISSUER_URI = "http://auth-server:9527";
	public static final String DEFAULT_OIDC_PROVIDER_CONFIGURATION_ENDPOINT_URI = "/.well-known/openid-configuration";
	public static final String DEFAULT_JWK_SET_ENDPOINT_URI = "/oauth2/jwks";
	public static final String DEFAULT_AUTHORIZATION_ENDPOINT_URI = "/oauth2/authorize";
	public static final String DEFAULT_TOKEN_ENDPOINT_URI = "/oauth2/token";
	public static final String DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI = "/oauth2/revoke";

	private final RequestMatcher authorizationEndpointMatcher = new OrRequestMatcher(
		new AntPathRequestMatcher(DEFAULT_AUTHORIZATION_ENDPOINT_URI, GET.name()),
		new AntPathRequestMatcher(DEFAULT_AUTHORIZATION_ENDPOINT_URI, POST.name()));

	private final RequestMatcher tokenEndpointMatcher = new AntPathRequestMatcher(
		DEFAULT_TOKEN_ENDPOINT_URI, POST.name());

	private final RequestMatcher tokenRevocationEndpointMatcher = new AntPathRequestMatcher(
		DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI, POST.name());

	private final RequestMatcher jwkSetEndpointMatcher = new AntPathRequestMatcher(
		DEFAULT_JWK_SET_ENDPOINT_URI, GET.name());

	private final RequestMatcher oidcProviderConfigurationEndpointMatcher = new AntPathRequestMatcher(
		DEFAULT_OIDC_PROVIDER_CONFIGURATION_ENDPOINT_URI, GET.name());

	private final RequestMatcher clientAuthenticationMatcher =
		new OrRequestMatcher(tokenEndpointMatcher, tokenRevocationEndpointMatcher);

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		RequestMatcher endpointsMatcher = (request) -> authorizationEndpointMatcher.matches(request) ||
			tokenEndpointMatcher.matches(request) ||
			tokenRevocationEndpointMatcher.matches(request) ||
			jwkSetEndpointMatcher.matches(request) || oidcProviderConfigurationEndpointMatcher.matches(request);

		http
			.requestMatcher(endpointsMatcher)
			.authorizeRequests(authorizeRequests -> {
				authorizeRequests.requestMatchers(oidcProviderConfigurationEndpointMatcher,
					jwkSetEndpointMatcher).permitAll();
				authorizeRequests.anyRequest().authenticated();
			})
			.csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
			.apply(new ClientAuthenticationConfigurer<>());

		return http.build();
	}

	private final class ClientAuthenticationConfigurer<B extends HttpSecurityBuilder<B>>
		extends AbstractHttpConfigurer<ClientAuthenticationConfigurer<B>, B> {

		@Override
		public void init(B builder) {
			ExceptionHandlingConfigurer<B> exceptionHandling = builder.getConfigurer(ExceptionHandlingConfigurer.class);
			if (exceptionHandling != null) {
				DelegatingAuthenticationEntryPoint authenticationEntryPoint =
					new DelegatingAuthenticationEntryPoint(new LinkedHashMap<>() {{
						put(clientAuthenticationMatcher, new HttpStatusEntryPoint(UNAUTHORIZED));
					}});
				authenticationEntryPoint.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint(DEFAULT_LOGIN_PAGE_URL));
				exceptionHandling.authenticationEntryPoint(authenticationEntryPoint);
			}
		}

		@Override
		public void configure(B builder) {
			// OAuth2.0客户端请求提取身份认证凭证过滤器
			builder.addFilterAfter(postProcess(new OAuth2ClientAuthenticationFilter(
				getBean(builder, RegisteredClientRepository.class),
				getBean(builder, OAuth2AuthorizationService.class),
				clientAuthenticationMatcher)
			), AbstractPreAuthenticatedProcessingFilter.class);
		}

		private <T> T getBean(B builder, Class<T> type) {
			return builder.getSharedObject(ApplicationContext.class).getBean(type);
		}

	}

}
