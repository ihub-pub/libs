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

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import pub.ihub.secure.crypto.CryptoKeySource;
import pub.ihub.secure.oauth2.jose.jws.NimbusJwsEncoder;
import pub.ihub.secure.oauth2.jwt.JwtEncoder;
import pub.ihub.secure.oauth2.server.InMemoryOAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.RegisteredClientRepository;
import pub.ihub.secure.oauth2.server.oidc.web.OidcProviderConfigurationEndpointFilter;
import pub.ihub.secure.oauth2.server.web.filter.JwkSetEndpointFilter;
import pub.ihub.secure.oauth2.server.web.filter.OAuth2AuthorizationEndpointFilter;
import pub.ihub.secure.oauth2.server.web.filter.OAuth2ClientAuthenticationFilter;
import pub.ihub.secure.oauth2.server.web.filter.OAuth2TokenEndpointFilter;
import pub.ihub.secure.oauth2.server.web.filter.OAuth2TokenRevocationEndpointFilter;
import pub.ihub.secure.oauth2.server.web.provider.OAuth2AuthorizationCodeAuthenticationProvider;
import pub.ihub.secure.oauth2.server.web.provider.OAuth2ClientAuthenticationProvider;
import pub.ihub.secure.oauth2.server.web.provider.OAuth2ClientCredentialsAuthenticationProvider;
import pub.ihub.secure.oauth2.server.web.provider.OAuth2RefreshTokenAuthenticationProvider;
import pub.ihub.secure.oauth2.server.web.provider.OAuth2TokenRevocationAuthenticationProvider;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/**
 * @param <B>
 * @author henry
 */
public final class OAuth2AuthorizationServerConfigurer<B extends HttpSecurityBuilder<B>>
	extends AbstractHttpConfigurer<OAuth2AuthorizationServerConfigurer<B>, B> {

	// TODO 整理配置文件
	public static final String ISSUER_URI = "http://auth-server:9000";
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

	/**
	 * Sets the repository of registered clients.
	 *
	 * @param registeredClientRepository the repository of registered clients
	 * @return the {@link OAuth2AuthorizationServerConfigurer} for further configuration
	 */
	public OAuth2AuthorizationServerConfigurer<B> registeredClientRepository(RegisteredClientRepository registeredClientRepository) {
		Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
		this.getBuilder().setSharedObject(RegisteredClientRepository.class, registeredClientRepository);
		return this;
	}

	/**
	 * Sets the authorization service.
	 *
	 * @param authorizationService the authorization service
	 * @return the {@link OAuth2AuthorizationServerConfigurer} for further configuration
	 */
	public OAuth2AuthorizationServerConfigurer<B> authorizationService(OAuth2AuthorizationService authorizationService) {
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		this.getBuilder().setSharedObject(OAuth2AuthorizationService.class, authorizationService);
		return this;
	}

	/**
	 * Sets the source for cryptographic keys.
	 *
	 * @param keySource the source for cryptographic keys
	 * @return the {@link OAuth2AuthorizationServerConfigurer} for further configuration
	 */
	public OAuth2AuthorizationServerConfigurer<B> keySource(CryptoKeySource keySource) {
		Assert.notNull(keySource, "keySource cannot be null");
		this.getBuilder().setSharedObject(CryptoKeySource.class, keySource);
		return this;
	}

	/**
	 * Returns a {@code List} of {@link RequestMatcher}'s for the authorization server endpoints.
	 *
	 * @return a {@code List} of {@link RequestMatcher}'s for the authorization server endpoints
	 */
	public List<RequestMatcher> getEndpointMatchers() {
		// TODO Initialize matchers using URI's from ProviderSettings
		return Arrays.asList(this.authorizationEndpointMatcher, this.tokenEndpointMatcher,
			this.tokenRevocationEndpointMatcher, this.jwkSetEndpointMatcher,
			this.oidcProviderConfigurationEndpointMatcher);
	}

	@Override
	public void init(B builder) {
		OAuth2ClientAuthenticationProvider clientAuthenticationProvider =
			new OAuth2ClientAuthenticationProvider(
				getRegisteredClientRepository(builder),
				getAuthorizationService(builder));
		builder.authenticationProvider(postProcess(clientAuthenticationProvider));

		JwtEncoder jwtEncoder = getJwtEncoder(builder);

		OAuth2AuthorizationCodeAuthenticationProvider authorizationCodeAuthenticationProvider =
			new OAuth2AuthorizationCodeAuthenticationProvider(
				getAuthorizationService(builder),
				jwtEncoder);
		builder.authenticationProvider(postProcess(authorizationCodeAuthenticationProvider));

		OAuth2RefreshTokenAuthenticationProvider refreshTokenAuthenticationProvider =
			new OAuth2RefreshTokenAuthenticationProvider(
				getAuthorizationService(builder),
				jwtEncoder);
		builder.authenticationProvider(postProcess(refreshTokenAuthenticationProvider));

		OAuth2ClientCredentialsAuthenticationProvider clientCredentialsAuthenticationProvider =
			new OAuth2ClientCredentialsAuthenticationProvider(
				getAuthorizationService(builder),
				jwtEncoder);
		builder.authenticationProvider(postProcess(clientCredentialsAuthenticationProvider));

		OAuth2TokenRevocationAuthenticationProvider tokenRevocationAuthenticationProvider =
			new OAuth2TokenRevocationAuthenticationProvider(
				getAuthorizationService(builder));
		builder.authenticationProvider(postProcess(tokenRevocationAuthenticationProvider));

		ExceptionHandlingConfigurer<B> exceptionHandling = builder.getConfigurer(ExceptionHandlingConfigurer.class);
		if (exceptionHandling != null) {
			LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
			entryPoints.put(
				new OrRequestMatcher(this.tokenEndpointMatcher, this.tokenRevocationEndpointMatcher),
				new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
			DelegatingAuthenticationEntryPoint authenticationEntryPoint =
				new DelegatingAuthenticationEntryPoint(entryPoints);

			// TODO This needs to change as the login page could be customized with a different URL
			authenticationEntryPoint.setDefaultEntryPoint(
				new LoginUrlAuthenticationEntryPoint(
					DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL));

			exceptionHandling.authenticationEntryPoint(authenticationEntryPoint);
		}
	}

	@Override
	public void configure(B builder) {
		// TODO IODC可选
		OidcProviderConfigurationEndpointFilter oidcProviderConfigurationEndpointFilter =
			new OidcProviderConfigurationEndpointFilter();
		builder.addFilterBefore(postProcess(oidcProviderConfigurationEndpointFilter), AbstractPreAuthenticatedProcessingFilter.class);

		JwkSetEndpointFilter jwkSetEndpointFilter = new JwkSetEndpointFilter(getKeySource(builder), DEFAULT_JWK_SET_ENDPOINT_URI);
		builder.addFilterBefore(postProcess(jwkSetEndpointFilter), AbstractPreAuthenticatedProcessingFilter.class);

		// OAuth2.0授权令牌认证过滤器
		OAuth2AuthorizationEndpointFilter authorizationEndpointFilter =
			new OAuth2AuthorizationEndpointFilter(
				getRegisteredClientRepository(builder),
				getAuthorizationService(builder),
				DEFAULT_AUTHORIZATION_ENDPOINT_URI);
		builder.addFilterBefore(postProcess(authorizationEndpointFilter), AbstractPreAuthenticatedProcessingFilter.class);

		AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);

		// OAuth2.0客户端请求提取身份认证凭证过滤器
		builder.addFilterAfter(postProcess(new OAuth2ClientAuthenticationFilter(authenticationManager,
			new OrRequestMatcher(tokenEndpointMatcher, tokenRevocationEndpointMatcher))
		), AbstractPreAuthenticatedProcessingFilter.class);

		// OAuth2.0令牌授予过滤器
		builder.addFilterAfter(postProcess(new OAuth2TokenEndpointFilter(authenticationManager,
			DEFAULT_TOKEN_ENDPOINT_URI)), FilterSecurityInterceptor.class);

		// OAuth2.0令牌撤销过滤器
		builder.addFilterAfter(postProcess(new OAuth2TokenRevocationEndpointFilter(authenticationManager,
			DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI)), OAuth2TokenEndpointFilter.class);
	}

	private static <B extends HttpSecurityBuilder<B>> RegisteredClientRepository getRegisteredClientRepository(B builder) {
		RegisteredClientRepository registeredClientRepository = builder.getSharedObject(RegisteredClientRepository.class);
		if (registeredClientRepository == null) {
			registeredClientRepository = getBean(builder, RegisteredClientRepository.class);
			builder.setSharedObject(RegisteredClientRepository.class, registeredClientRepository);
		}
		return registeredClientRepository;
	}

	private static <B extends HttpSecurityBuilder<B>> OAuth2AuthorizationService getAuthorizationService(B builder) {
		OAuth2AuthorizationService authorizationService = builder.getSharedObject(OAuth2AuthorizationService.class);
		if (authorizationService == null) {
			authorizationService = getOptionalBean(builder, OAuth2AuthorizationService.class);
			if (authorizationService == null) {
				authorizationService = new InMemoryOAuth2AuthorizationService();
			}
			builder.setSharedObject(OAuth2AuthorizationService.class, authorizationService);
		}
		return authorizationService;
	}

	private static <B extends HttpSecurityBuilder<B>> JwtEncoder getJwtEncoder(B builder) {
		JwtEncoder jwtEncoder = getOptionalBean(builder, JwtEncoder.class);
		if (jwtEncoder == null) {
			CryptoKeySource keySource = getKeySource(builder);
			jwtEncoder = new NimbusJwsEncoder(keySource);
		}
		return jwtEncoder;
	}

	private static <B extends HttpSecurityBuilder<B>> CryptoKeySource getKeySource(B builder) {
		CryptoKeySource keySource = builder.getSharedObject(CryptoKeySource.class);
		if (keySource == null) {
			keySource = getBean(builder, CryptoKeySource.class);
			builder.setSharedObject(CryptoKeySource.class, keySource);
		}
		return keySource;
	}

	private static <B extends HttpSecurityBuilder<B>, T> T getBean(B builder, Class<T> type) {
		return builder.getSharedObject(ApplicationContext.class).getBean(type);
	}

	private static <B extends HttpSecurityBuilder<B>, T> T getOptionalBean(B builder, Class<T> type) {
		Map<String, T> beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(
			builder.getSharedObject(ApplicationContext.class), type);
		if (beansMap.size() > 1) {
			throw new NoUniqueBeanDefinitionException(type, beansMap.size(),
				"Expected single matching bean of type '" + type.getName() + "' but found " +
					beansMap.size() + ": " + StringUtils.collectionToCommaDelimitedString(beansMap.keySet()));
		}
		return (!beansMap.isEmpty() ? beansMap.values().iterator().next() : null);
	}
}
