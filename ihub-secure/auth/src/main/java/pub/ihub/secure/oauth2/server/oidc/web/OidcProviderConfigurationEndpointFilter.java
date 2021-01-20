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

package pub.ihub.secure.oauth2.server.oidc.web;

import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.oauth2.core.oidc.OidcProviderConfiguration;
import org.springframework.security.oauth2.core.oidc.http.converter.OidcProviderConfigurationHttpMessageConverter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN;
import static org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType.CODE;
import static org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.DEFAULT_AUTHORIZATION_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.DEFAULT_JWK_SET_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.DEFAULT_OIDC_PROVIDER_CONFIGURATION_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.DEFAULT_TOKEN_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.ISSUER_URI;

/**
 * 处理OpenID提供程序配置请求的过滤器
 * TODO
 *
 * @author henry
 */
public class OidcProviderConfigurationEndpointFilter extends OncePerRequestFilter {

	private final RequestMatcher requestMatcher;
	private final OidcProviderConfigurationHttpMessageConverter providerConfigurationHttpMessageConverter =
		new OidcProviderConfigurationHttpMessageConverter();

	public OidcProviderConfigurationEndpointFilter() {
		this.requestMatcher = new AntPathRequestMatcher(DEFAULT_OIDC_PROVIDER_CONFIGURATION_ENDPOINT_URI, GET.name());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		if (!this.requestMatcher.matches(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		OidcProviderConfiguration providerConfiguration = OidcProviderConfiguration.builder()
			.issuer(ISSUER_URI)
			.authorizationEndpoint(asUrl(ISSUER_URI, DEFAULT_AUTHORIZATION_ENDPOINT_URI))
			.tokenEndpoint(asUrl(ISSUER_URI, DEFAULT_TOKEN_ENDPOINT_URI))
			// TODO: Use ClientAuthenticationMethod.CLIENT_SECRET_BASIC in Spring Security 5.5.0
			.tokenEndpointAuthenticationMethod("client_secret_basic")
			// TODO: Use ClientAuthenticationMethod.CLIENT_SECRET_POST in Spring Security 5.5.0
			.tokenEndpointAuthenticationMethod("client_secret_post")
			.jwkSetUri(asUrl(ISSUER_URI, DEFAULT_JWK_SET_ENDPOINT_URI))
			.responseType(CODE.getValue())
			.grantType(AUTHORIZATION_CODE.getValue())
			.grantType(CLIENT_CREDENTIALS.getValue())
			.grantType(REFRESH_TOKEN.getValue())
			.subjectType("public")
			.idTokenSigningAlgorithm(RS256.getName())
			.scope(OPENID)
			.build();

		ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
		this.providerConfigurationHttpMessageConverter.write(providerConfiguration, APPLICATION_JSON, httpResponse);
	}

	private static String asUrl(String issuer, String endpoint) {
		return fromUriString(issuer).path(endpoint).build().toUriString();
	}

}
