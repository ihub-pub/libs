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

package pub.ihub.secure.auth.web;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pub.ihub.core.ObjectBuilder;
import pub.ihub.secure.core.GrantType;
import pub.ihub.secure.oauth2.jose.JoseHeader;
import pub.ihub.secure.oauth2.jwt.JwtClaimsSet;
import pub.ihub.secure.oauth2.jwt.JwtEncoder;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.RegisteredClientRepository;
import pub.ihub.secure.oauth2.server.TokenType;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.token.OAuth2AuthorizationCode;
import pub.ihub.secure.oauth2.server.token.OAuth2TokenMetadata;
import pub.ihub.secure.oauth2.server.token.OAuth2Tokens;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static cn.hutool.core.lang.Assert.isFalse;
import static cn.hutool.core.lang.Assert.isTrue;
import static cn.hutool.core.lang.Assert.notNull;
import static cn.hutool.core.text.CharSequenceUtil.blankToDefault;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static cn.hutool.core.text.CharSequenceUtil.split;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Base64.getUrlEncoder;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.security.core.context.SecurityContextHolder.clearContext;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_CLIENT;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_GRANT;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_SCOPE;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.UNAUTHORIZED_CLIENT;
import static pub.ihub.secure.oauth2.server.token.OAuth2RefreshToken2.issueRefreshToken;
import static org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse.withToken;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_ID;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CODE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR_DESCRIPTION;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR_URI;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.GRANT_TYPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REDIRECT_URI;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REFRESH_TOKEN;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.SCOPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.STATE;
import static org.springframework.security.oauth2.core.oidc.IdTokenClaimNames.AZP;
import static org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID;
import static org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.ID_TOKEN;
import static org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.NONCE;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.AUD;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.EXP;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.IAT;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.ISS;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.NBF;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.SUB;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;
import static pub.ihub.core.ObjectBuilder.builder;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.DEFAULT_AUTHORIZATION_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.DEFAULT_TOKEN_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.ISSUER_URI;
import static pub.ihub.secure.oauth2.jose.JoseHeader.withAlgorithm;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.ACCESS_TOKEN_ATTRIBUTES;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.AUTHORIZED_SCOPES;
import static pub.ihub.secure.oauth2.server.TokenType.AUTHORIZATION_CODE;
import static pub.ihub.secure.oauth2.server.token.OAuth2AuthorizationCode.generateAuthCode;
import static pub.ihub.secure.oauth2.server.web.filter.OAuth2AuthorizationEndpointFilter.isPrincipalAuthenticated;

/**
 * @author liheng
 */
@Controller
@AllArgsConstructor
public class OAuth2Controller {

	private static final String CONSENT_ACTION_PARAMETER_NAME = "consent_action";
	private static final String CONSENT_ACTION_APPROVE = "approve";
	private static final String CONSENT_ACTION_CANCEL = "cancel";
	private static final String GRANT_TYPE_AUTHORIZATION_CODE = GRANT_TYPE + "=authorization_code";
	private static final String GRANT_TYPE_REFRESH_TOKEN = GRANT_TYPE + "=refresh_token";
	private static final String GRANT_TYPE_CLIENT_CREDENTIALS = GRANT_TYPE + "=client_credentials";

	private final RegisteredClientRepository registeredClientRepository;
	private final OAuth2AuthorizationService authorizationService;
	private final StringKeyGenerator codeGenerator = new Base64StringKeyGenerator(getUrlEncoder().withoutPadding(), 96);
	private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	private final JwtEncoder jwtEncoder;
	/**
	 * 认证消息转换器
	 */
	private final HttpMessageConverter<OAuth2AccessTokenResponse> accessTokenHttpResponseConverter =
		new OAuth2AccessTokenResponseHttpMessageConverter();
	/**
	 * 异常转换器
	 */
	private final HttpMessageConverter<OAuth2Error> errorHttpResponseConverter =
		new OAuth2ErrorHttpMessageConverter();

	@PostMapping(value = DEFAULT_AUTHORIZATION_ENDPOINT_URI, params = CONSENT_ACTION_PARAMETER_NAME)
	public void authorize(@RequestParam(CLIENT_ID) @NotBlank String clientId,
						  @RequestParam(STATE) @NotBlank String state,
						  @RequestParam(SCOPE) @NotEmpty Set<String> scopes,
						  @RequestParam(CONSENT_ACTION_PARAMETER_NAME) @NotBlank String action,
						  HttpServletRequest request,
						  HttpServletResponse response) throws IOException {
		OAuth2Authorization authorization = getAuthorization(state);
		RegisteredClient registeredClient = getRegisteredClient(clientId, authorization);
		OAuth2AuthorizationRequest authorizationRequest = authorization.getAuthorizationRequest();
		String redirectUri = isNotBlank(authorizationRequest.getRedirectUri()) ?
			authorizationRequest.getRedirectUri() : registeredClient.getRedirectUris().iterator().next();
		if (!authorizationRequest.getScopes().containsAll(scopes)) {
//			response.sendRedirect(redirectError(redirectUri, exceptionSupplier(SCOPE).get(), state));
			sendErrorResponse(request, response, redirectUri, exceptionSupplier(SCOPE).get(), state);
		}

		if (CONSENT_ACTION_APPROVE.equals(action)) {
			OAuth2AuthorizationCode authorizationCode = generateAuthCode(codeGenerator.generateKey(),
				registeredClient.getAccessTokenTimeToLive());
			authorizationService.save(OAuth2Authorization.from(authorization)
				.set(OAuth2Authorization::setTokens,
					ObjectBuilder.builder(OAuth2Tokens::new).set(OAuth2Tokens::token, authorizationCode).build())
				.setSub(OAuth2Authorization::getAttributes, attributes -> {
					attributes.remove(OAuth2Authorization.STATE);
					attributes.put(AUTHORIZED_SCOPES, scopes);
				})
				.build());

			sendAuthorizationResponse(request, response, redirectUri, authorizationCode, state);
//			response.sendRedirect(ObjectBuilder.builder(UrlBuilder.of(redirectUri, UTF_8))
//				.identity(b -> b.addQuery(CODE, authorizationCode.getTokenValue()))
//				.identity(isNotBlank(state), b -> b.addQuery(STATE, state))
//				.build().build());
		} else if (CONSENT_ACTION_CANCEL.equals(action)) {
			// TODO Need to remove 'in-flight' authorization if consent step is not completed (e.g. approved or cancelled)
//			response.sendRedirect(redirectError("redirect:/cancel", exceptionSupplier("授权取消！").get(), state));
			sendErrorResponse(request, response, "redirect:/cancel", exceptionSupplier("授权取消！").get(), state);
		} else {
			authorizationService.remove(authorization);
//			response.sendRedirect(redirectError(redirectUri, exceptionSupplier("授权失败！").get(), state));
			sendErrorResponse(request, response, redirectUri, exceptionSupplier("授权失败！").get(), state);
		}
	}

	@PostMapping(value = DEFAULT_TOKEN_ENDPOINT_URI, params = GRANT_TYPE_AUTHORIZATION_CODE)
	public void token(@RequestParam(CODE) @NotBlank String code,
					  @RequestParam(value = REDIRECT_URI, required = false) String redirectUri,
					  HttpServletResponse response) throws IOException {
		RegisteredClient registeredClient = getAuthenticatedClient().getRegisteredClient();
		OAuth2Authorization authorization = notNull(authorizationService.findByToken(code, AUTHORIZATION_CODE), invalidGrantException());
		OAuth2AuthorizationCode authorizationCode = authorization.getTokens().getToken(OAuth2AuthorizationCode.class);
		OAuth2TokenMetadata authorizationCodeMetadata = authorization.getTokens().getTokenMetadata(authorizationCode);

		OAuth2AuthorizationRequest authorizationRequest = authorization.getAuthorizationRequest();

		if (!registeredClient.getClientId().equals(authorizationRequest.getClientId())) {
			// 如果已授权，移除授权
			if (!authorizationCodeMetadata.isInvalidated()) {
				authorization = authorization.invalidate(authorizationCode);
				authorizationService.save(authorization);
			}
			throw invalidGrantException().get();
		}

		isTrue(isBlank(authorizationRequest.getRedirectUri()) ||
			authorizationRequest.getRedirectUri().equals(redirectUri), invalidGrantException());

		isFalse(authorizationCodeMetadata.isInvalidated(), invalidGrantException());

		Set<String> authorizedScopes = authorization.getAuthorizedScopes();
		Jwt jwt = issueJwtAccessToken(jwtEncoder, authorization.getPrincipalName(), registeredClient.getClientId(),
			authorizedScopes, registeredClient.getAccessTokenTimeToLive());

		OAuth2Tokens tokens = ObjectBuilder.clone(authorization.getTokens())
			.set(OAuth2Tokens::accessToken, new OAuth2AccessToken(BEARER,
				jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), authorizedScopes))
			.set(registeredClient.getGrantTypes().contains(GrantType.REFRESH_TOKEN),
				OAuth2Tokens::refreshToken, issueRefreshToken(registeredClient.getRefreshTokenTimeToLive()))
			.set(authorizationRequest.getScopes().contains(OPENID), OAuth2Tokens::token, issueIdToken(
				this.jwtEncoder, authorization.getPrincipalName(), registeredClient.getClientId(),
				(String) authorizationRequest.getAdditionalParameters().get(NONCE)), jwtIdToken ->
				new OidcIdToken(jwtIdToken.getTokenValue(), jwtIdToken.getIssuedAt(),
					jwtIdToken.getExpiresAt(), jwtIdToken.getClaims()))
			.build();

		// 授权码只能使用一次，作废授权码
		authorizationService.save(OAuth2Authorization.from(authorization)
			.set(OAuth2Authorization::setTokens, tokens)
			.put(OAuth2Authorization::getAttributes, ACCESS_TOKEN_ATTRIBUTES, jwt)
			.build().invalidate(authorizationCode));

		OidcIdToken idToken = tokens.getToken(OidcIdToken.class);
		sendAccessTokenResponse(response, tokens.getAccessToken(), tokens.getRefreshToken(), idToken != null ?
			new HashMap<>(1) {{
				put(ID_TOKEN, idToken.getTokenValue());
			}} : Collections.emptyMap());
	}

	@PostMapping(value = DEFAULT_TOKEN_ENDPOINT_URI, params = GRANT_TYPE_REFRESH_TOKEN)
	public void refreshToken(@RequestParam(REFRESH_TOKEN) @NotBlank String refreshToken,
							 @RequestParam(value = SCOPE, required = false) String scope,
							 HttpServletResponse response) throws IOException {
		Set<String> scopes = extractScopes(scope);
		RegisteredClient registeredClient = getAuthenticatedClient().getRegisteredClient();
		OAuth2Authorization authorization = notNull(authorizationService.findByToken(refreshToken, TokenType.REFRESH_TOKEN), invalidGrantException());
		isTrue(registeredClient.getId().equals(authorization.getRegisteredClientId()), invalidGrantException());
		isTrue(registeredClient.getGrantTypes().contains(GrantType.REFRESH_TOKEN), exceptionSupplier(UNAUTHORIZED_CLIENT));
		isFalse(authorization.getTokens().getRefreshToken().getExpiresAt().isBefore(Instant.now()), invalidGrantException());

		Set<String> authorizedScopes = authorization.getAuthorizedScopes();
		isTrue(authorizedScopes.containsAll(scopes), exceptionSupplier(INVALID_SCOPE));
		if (scopes.isEmpty()) {
			scopes = authorizedScopes;
		}

		isFalse(authorization.getTokens().getTokenMetadata(authorization.getTokens().getRefreshToken()).isInvalidated(),
			invalidGrantException());

		Jwt jwt = issueJwtAccessToken(this.jwtEncoder, authorization.getPrincipalName(), registeredClient.getClientId(),
			scopes, registeredClient.getAccessTokenTimeToLive());

		OAuth2Tokens tokens = ObjectBuilder.clone(authorization.getTokens())
			.set(OAuth2Tokens::accessToken, new OAuth2AccessToken(BEARER,
				jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), scopes))
			.set(OAuth2Tokens::refreshToken, registeredClient.isReuseRefreshTokens() ?
				authorization.getTokens().getRefreshToken() : issueRefreshToken(registeredClient.getRefreshTokenTimeToLive()))
			.build();

		authorizationService.save(OAuth2Authorization.from(authorization)
			.set(OAuth2Authorization::setTokens, tokens)
			.put(OAuth2Authorization::getAttributes, ACCESS_TOKEN_ATTRIBUTES, jwt)
			.build());

		sendAccessTokenResponse(response, tokens.getAccessToken(), tokens.getRefreshToken(), Collections.emptyMap());
	}

	@PostMapping(value = DEFAULT_TOKEN_ENDPOINT_URI, params = GRANT_TYPE_CLIENT_CREDENTIALS)
	public void token(@RequestParam(value = SCOPE, required = false) String scope,
					  HttpServletResponse response) throws IOException {
		Set<String> scopes = extractScopes(scope);
		OAuth2ClientAuthToken clientPrincipal = getAuthenticatedClient();
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		isTrue(registeredClient.getGrantTypes().contains(GrantType.CLIENT_CREDENTIALS), exceptionSupplier(UNAUTHORIZED_CLIENT));

		Set<String> clientScopes = registeredClient.getScopes();
		isTrue(clientScopes.containsAll(scopes), exceptionSupplier(INVALID_SCOPE));
		if (scopes.isEmpty()) {
			scopes = new LinkedHashSet<>(clientScopes);
		}

		Jwt jwt = issueJwtAccessToken(this.jwtEncoder, clientPrincipal.getName(), registeredClient.getClientId(),
			scopes, registeredClient.getAccessTokenTimeToLive());
		OAuth2AccessToken accessToken = new OAuth2AccessToken(BEARER,
			jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), scopes);

		this.authorizationService.save(ObjectBuilder.builder(OAuth2Authorization::new)
			.set(OAuth2Authorization::setRegisteredClientId, registeredClient.getId())
			.set(OAuth2Authorization::setPrincipalName, clientPrincipal.getName())
			.set(OAuth2Authorization::setTokens, ObjectBuilder.builder(OAuth2Tokens::new)
				.set(OAuth2Tokens::accessToken, accessToken).build())
			.set(OAuth2Authorization::setAttributes, new HashMap<>(1) {
				{
					put(ACCESS_TOKEN_ATTRIBUTES, jwt);
				}
			})
			.build());

		sendAccessTokenResponse(response, accessToken, null, Collections.emptyMap());
	}

	@PostMapping(DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI)
	public void revoke(@RequestParam("token") @NotBlank String token,
					   @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint,
					   HttpServletResponse response) throws IOException {
		try {
			OAuth2Authorization authorization = authorizationService.findByToken(token, TokenType.of(tokenTypeHint));
			if (authorization == null) {
				return;
			}
			isTrue(getAuthenticatedClient().getRegisteredClient().getId().equals(authorization.getRegisteredClientId()),
				invalidClientException());
			this.authorizationService.save(authorization.invalidate(authorization.getTokens().getToken(token)));
		} catch (OAuth2AuthenticationException ex) {
			clearContext();
			sendErrorResponse(response, ex.getError());
		}
	}

	private OAuth2Authorization getAuthorization(String state) {
		OAuth2Authorization authorization = authorizationService.findByToken(state, new TokenType(OAuth2Authorization.STATE));
		Authentication principal = SecurityContextHolder.getContext().getAuthentication();
		isTrue(isPrincipalAuthenticated(principal) &&
			principal.getName().equals(authorization.getPrincipalName()), exceptionSupplier(STATE));
		return authorization;
	}

	private RegisteredClient getRegisteredClient(String clientId, OAuth2Authorization authorization) {
		RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
		isTrue(registeredClient.getId().equals(authorization.getRegisteredClientId()), exceptionSupplier(CLIENT_ID));
		return registeredClient;
	}

	private String redirectError(String redirectUri, OAuth2AuthenticationException exception, String state) {
		OAuth2Error error = exception.getError();
		return "redirect:" + ObjectBuilder.builder(UrlBuilder.of(redirectUri, UTF_8))
			.identity(b -> b.addQuery(ERROR, error.getErrorCode()))
			.identity(isNotBlank(error.getDescription()), b -> b.addQuery(ERROR_DESCRIPTION, error.getDescription()))
			.identity(isNotBlank(state), b -> b.addQuery(STATE, state))
			.identity(isNotBlank(error.getUri()), b -> b.addQuery(ERROR_URI, error.getUri()))
			.build().build();
	}

	public OAuth2ClientAuthToken getAuthenticatedClient() {
		Authentication clientPrincipal = getContext().getAuthentication();
		if (clientPrincipal != null && clientPrincipal.isAuthenticated() &&
			OAuth2ClientAuthToken.class.isAssignableFrom(clientPrincipal.getClass())) {
			return (OAuth2ClientAuthToken) clientPrincipal;
		}
		throw new OAuth2AuthenticationException(new OAuth2Error(INVALID_CLIENT));
	}

	private void sendAuthorizationResponse(HttpServletRequest request, HttpServletResponse response, String redirectUri,
										   OAuth2AuthorizationCode authorizationCode, String state) throws IOException {
		redirectStrategy.sendRedirect(request, response, fromUriString(redirectUri)
			.queryParam(CODE, authorizationCode.getTokenValue())
			.queryParamIfPresent(STATE, Optional.of(blankToDefault(state, null))).toUriString());
	}

	private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, String redirectUri,
								   OAuth2AuthenticationException exception, String state) throws IOException {
		OAuth2Error error = exception.getError();
		redirectStrategy.sendRedirect(request, response, fromUriString(redirectUri)
			.queryParam(ERROR, error.getErrorCode())
			.queryParamIfPresent(ERROR_DESCRIPTION, Optional.of(blankToDefault(error.getDescription(), null)))
			.queryParamIfPresent(ERROR_URI, Optional.of(blankToDefault(error.getUri(), null)))
			.queryParamIfPresent(STATE, Optional.of(blankToDefault(state, null))).toUriString());
	}

	private void sendErrorResponse(HttpServletResponse response, OAuth2Error error) throws IOException {
		this.errorHttpResponseConverter.write(error, null, builder(ServletServerHttpResponse::new, response)
			.set(ServletServerHttpResponse::setStatusCode, getStatusCode(error)).build());
	}

	private void sendAccessTokenResponse(HttpServletResponse response, OAuth2AccessToken accessToken,
										 org.springframework.security.oauth2.core.OAuth2RefreshToken refreshToken,
										 Map<String, Object> additionalParameters) throws IOException {
		accessTokenHttpResponseConverter.write(
			builder(withToken(accessToken.getTokenValue())
				.tokenType(accessToken.getTokenType())
				.scopes(accessToken.getScopes()))
				.set(token -> token.getIssuedAt() != null && token.getExpiresAt() != null,
					OAuth2AccessTokenResponse.Builder::expiresIn, accessToken,
					token -> SECONDS.between(token.getIssuedAt(), token.getExpiresAt()))
				.set(ObjectUtil::isNotNull, OAuth2AccessTokenResponse.Builder::refreshToken,
					refreshToken, AbstractOAuth2Token::getTokenValue)
				.set(CollUtil::isNotEmpty, OAuth2AccessTokenResponse.Builder::additionalParameters, additionalParameters)
				.build().build(),
			null,
			new ServletServerHttpResponse(response)
		);
	}

	private HttpStatus getStatusCode(OAuth2Error error) {
		return BAD_REQUEST;
	}

	private static Supplier<OAuth2AuthenticationException> exceptionSupplier(String errorCode) {
		return () -> new OAuth2AuthenticationException(new OAuth2Error(errorCode));
	}

	private static Supplier<OAuth2AuthenticationException> invalidClientException() {
		return exceptionSupplier(INVALID_CLIENT);
	}

	private static Supplier<OAuth2AuthenticationException> invalidGrantException() {
		return exceptionSupplier(INVALID_GRANT);
	}

	private static Jwt issueJwtAccessToken(JwtEncoder jwtEncoder, String subject, String audience, Set<String> scopes, Duration tokenTimeToLive) {
		JoseHeader joseHeader = withAlgorithm(RS256);

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(tokenTimeToLive);

		return jwtEncoder.encode(joseHeader, new JwtClaimsSet(new HashMap<>(7) {
			{
				put(ISS, ISSUER_URI);
				put(SUB, subject);
				put(AUD, singletonList(audience));
				put(IAT, issuedAt);
				put(EXP, expiresAt);
				put(NBF, issuedAt);
				put(SCOPE, scopes);
			}
		}));
	}

	private static Jwt issueIdToken(JwtEncoder jwtEncoder, String subject, String audience, String nonce) {
		JoseHeader joseHeader = withAlgorithm(RS256);

		Instant issuedAt = Instant.now();
		// TODO Allow configuration for id token time-to-live
		Instant expiresAt = issuedAt.plus(30, ChronoUnit.MINUTES);

		Map<String, Object> claims = new HashMap<>(7) {
			{
				put(ISS, ISSUER_URI);
				put(SUB, subject);
				put(AUD, singletonList(audience));
				put(IAT, issuedAt);
				put(EXP, expiresAt);
				put(AZP, audience);
			}
		};
		if (StringUtils.hasText(nonce)) {
			claims.put(IdTokenClaimNames.NONCE, nonce);
		}

		// TODO Add 'auth_time' claim

		return jwtEncoder.encode(joseHeader, new JwtClaimsSet(claims));
	}

	// TODO 确认多值参数
	public static Set<String> extractScopes(String scope) {
		return isNotBlank(scope) ? Arrays.stream(split(scope, " ")).collect(toSet()) : Collections.emptySet();
	}

}
