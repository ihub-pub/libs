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

import cn.hutool.core.net.url.UrlBuilder;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.TemplateEngine;
import pub.ihub.core.ObjectBuilder;
import pub.ihub.secure.auth.jwt.JwtEncoder;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.RegisteredClientRepository;
import pub.ihub.secure.oauth2.server.TokenType;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.token.OAuth2ClientAuthToken;
import pub.ihub.secure.oauth2.server.token.OAuth2Tokens;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import static cn.hutool.core.lang.Assert.isFalse;
import static cn.hutool.core.lang.Assert.isTrue;
import static cn.hutool.core.lang.Assert.notNull;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static cn.hutool.core.text.CharSequenceUtil.split;
import static cn.hutool.extra.template.engine.thymeleaf.ThymeleafTemplate.wrap;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.security.core.context.SecurityContextHolder.clearContext;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_CLIENT;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_GRANT;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REQUEST;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_SCOPE;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.UNAUTHORIZED_CLIENT;
import static org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest.authorizationCode;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_ID;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CODE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR_DESCRIPTION;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR_URI;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.GRANT_TYPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REDIRECT_URI;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REFRESH_TOKEN;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.RESPONSE_TYPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.SCOPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.STATE;
import static org.springframework.security.oauth2.core.endpoint.PkceParameterNames.CODE_CHALLENGE;
import static org.springframework.security.oauth2.core.endpoint.PkceParameterNames.CODE_CHALLENGE_METHOD;
import static org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID;
import static pub.ihub.core.ObjectBuilder.builder;
import static pub.ihub.secure.auth.web.filter.OAuth2ClientAuthenticationFilter.filterParameters;
import static pub.ihub.secure.core.Constant.DEFAULT_AUTHORIZATION_ENDPOINT_URI;
import static pub.ihub.secure.core.Constant.DEFAULT_TOKEN_ENDPOINT_URI;
import static pub.ihub.secure.core.Constant.DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.ACCESS_TOKEN_ATTRIBUTES;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.AUTHORIZATION_REQUEST;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.AUTHORIZED_SCOPES;

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
	private static final String SCOPE_OPENID = SCOPE + "=" + OPENID;

	private final RegisteredClientRepository registeredClientRepository;
	private final OAuth2AuthorizationService authorizationService;
	private final JwtEncoder jwtEncoder;
	private final TemplateEngine engine;
	/**
	 * 认证消息转换器
	 */
	private final HttpMessageConverter<OAuth2AccessTokenResponse> accessTokenResponseConverter =
		new OAuth2AccessTokenResponseHttpMessageConverter();
	/**
	 * 异常转换器
	 */
	private final HttpMessageConverter<OAuth2Error> errorHttpResponseConverter = new OAuth2ErrorHttpMessageConverter();

	@GetMapping(DEFAULT_AUTHORIZATION_ENDPOINT_URI)
	// TODO 确认POST场景
	@PostMapping(value = DEFAULT_AUTHORIZATION_ENDPOINT_URI, params = {"!" + CONSENT_ACTION_PARAMETER_NAME, SCOPE_OPENID})
	public void authorize(@RequestParam(CLIENT_ID) @NotBlank String clientId,
						  @RequestParam(value = REDIRECT_URI, required = false) String redirectUri,
						  @RequestParam(STATE) @NotBlank String state,
						  @RequestParam(value = SCOPE, required = false) String scope,
						  @RequestParam(value = CODE_CHALLENGE, required = false) String codeChallenge,
						  @RequestParam(value = CODE_CHALLENGE_METHOD, required = false) String codeChallengeMethod,
						  HttpServletRequest request,
						  HttpServletResponse response) throws IOException {
		Set<String> scopes = extractScopes(scope);
		RegisteredClient registeredClient = notNull(registeredClientRepository.findByClientId(clientId),
			invalidRequestException(CLIENT_ID));
		isTrue(registeredClient.supportedAuthorizationCode(), exceptionSupplier(UNAUTHORIZED_CLIENT, CLIENT_ID));
		// TODO 考虑是否保留多回调地址
		isTrue(isNotBlank(redirectUri) ? registeredClient.getRedirectUris().contains(redirectUri) :
				!scopes.contains(OPENID) || registeredClient.getRedirectUris().size() == 1,
			exceptionSupplier(REDIRECT_URI));
		redirectUri = isNotBlank(redirectUri) ? redirectUri : registeredClient.getRedirectUris().iterator().next();
		if (!scopes.isEmpty() && !registeredClient.getScopes().containsAll(scopes)) {
			redirectError(response, redirectUri, exceptionSupplier(INVALID_SCOPE).get(), state);
		}
		// (REQUIRED for public clients) - RFC 7636 (PKCE)
		if (isNotBlank(codeChallenge)) {
			if (isNotBlank(codeChallengeMethod) && !"S256".equals(codeChallengeMethod) && !"plain".equals(codeChallengeMethod)) {
				redirectError(response, redirectUri, invalidRequestException(CODE_CHALLENGE_METHOD).get(), state);
			}
		} else {
			if (registeredClient.isRequireProofKey()) {
				redirectError(response, redirectUri, invalidRequestException(CODE_CHALLENGE).get(), state);
			}
		}

		ObjectBuilder<OAuth2Authorization> builder = ObjectBuilder.builder(OAuth2Authorization::new)
			.set(OAuth2Authorization::setRegisteredClientId, registeredClient.getId())
			.set(OAuth2Authorization::setPrincipalName, getAuthentication().getName())
			.put(OAuth2Authorization::getAttributes, AUTHORIZATION_REQUEST, authorizationCode()
				.authorizationUri(request.getRequestURL().toString())
				.clientId(clientId)
				.redirectUri(redirectUri)
				.scopes(scopes)
				.state(state)
				.additionalParameters(filterParameters(request, RESPONSE_TYPE, CLIENT_ID, REDIRECT_URI, SCOPE, STATE))
				.build());

		if (registeredClient.isRequireUserConsent()) {
			OAuth2Authorization authorization = builder.build().generatorState();
			authorizationService.save(authorization);

			response.setContentType(new MediaType("text", "html", UTF_8).toString());
			wrap(engine, "consent", UTF_8).render(new HashMap<>(5) {{
				put("formPath", request.getRequestURI());
				put("clientId", registeredClient.getClientId());
				put("principalName", authorization.getPrincipalName());
				put("state", authorization.getState());
				put("scopes", scopes);
			}}, response.getWriter());
		} else {
			OAuth2Tokens tokens = new OAuth2Tokens().authorizationCode(registeredClient);
			authorizationService.save(builder
				.set(OAuth2Authorization::setTokens, tokens)
				.put(OAuth2Authorization::getAttributes, AUTHORIZED_SCOPES, scopes)
				.build());

//			TODO security checks for code parameter
//			The authorization code MUST expire shortly after it is issued to mitigate the risk of leaks.
//			A maximum authorization code lifetime of 10 minutes is RECOMMENDED.
//			The client MUST NOT use the authorization code more than once.
//			If an authorization code is used more than once, the authorization server MUST deny the request
//			and SHOULD revoke (when possible) all tokens previously issued based on that authorization code.
//			The authorization code is bound to the client identifier and redirection URI.

			response.sendRedirect(ObjectBuilder.builder(UrlBuilder.of(redirectUri, UTF_8))
				.identity(b -> b.addQuery(CODE, tokens.getAuthorizationCode().getTokenValue()))
				.identity(isNotBlank(state), b -> b.addQuery(STATE, state))
				.build().build());
		}
	}

	@PostMapping(value = DEFAULT_AUTHORIZATION_ENDPOINT_URI, params = CONSENT_ACTION_PARAMETER_NAME)
	public void authorize(@RequestParam(CLIENT_ID) @NotBlank String clientId,
						  @RequestParam(STATE) @NotBlank String state,
						  @RequestParam(SCOPE) @NotEmpty Set<String> scopes,
						  @RequestParam(CONSENT_ACTION_PARAMETER_NAME) @NotBlank String action,
						  HttpServletResponse response) throws IOException {
		OAuth2Authorization authorization = authorizationService.findByState(state);
		isTrue(getAuthentication().getName().equals(authorization.getPrincipalName()), invalidRequestException(STATE));
		RegisteredClient registeredClient = notNull(registeredClientRepository.findByClientId(clientId), invalidRequestException(CLIENT_ID));
		isTrue(registeredClient.getId().equals(authorization.getRegisteredClientId()), invalidRequestException(CLIENT_ID));
		OAuth2AuthorizationRequest authorizationRequest = authorization.getAuthorizationRequest();
		String redirectUri = isNotBlank(authorizationRequest.getRedirectUri()) ?
			authorizationRequest.getRedirectUri() : registeredClient.getRedirectUris().iterator().next();
		if (!authorizationRequest.getScopes().containsAll(scopes)) {
			redirectError(response, redirectUri, invalidRequestException(SCOPE).get(), state);
		}

		if (CONSENT_ACTION_APPROVE.equals(action)) {
			OAuth2Tokens tokens = new OAuth2Tokens().authorizationCode(registeredClient);
			authorizationService.save(OAuth2Authorization.from(authorization)
				.set(OAuth2Authorization::setTokens, tokens)
				.put(OAuth2Authorization::getAttributes, AUTHORIZED_SCOPES, scopes)
				.build());

			response.sendRedirect(ObjectBuilder.builder(UrlBuilder.of(redirectUri, UTF_8))
				.identity(b -> b.addQuery(CODE, tokens.getAuthorizationCode().getTokenValue()))
				.identity(isNotBlank(authorizationRequest.getState()), b -> b.addQuery(STATE, authorizationRequest.getState()))
				.build().build());
		} else if (CONSENT_ACTION_CANCEL.equals(action)) {
			// TODO Need to remove 'in-flight' authorization if consent step is not completed (e.g. approved or cancelled)
			redirectError(response, "redirect:/cancel", exceptionSupplier(INVALID_REQUEST, "授权取消！").get(), authorizationRequest.getState());
		} else {
			authorizationService.remove(authorization);
			redirectError(response, redirectUri, exceptionSupplier(INVALID_REQUEST, "授权失败！").get(), authorizationRequest.getState());
		}
	}

	@PostMapping(value = DEFAULT_TOKEN_ENDPOINT_URI, params = GRANT_TYPE_AUTHORIZATION_CODE)
	public void token(@RequestParam(CODE) @NotBlank String code,
					  @RequestParam(value = REDIRECT_URI, required = false) String redirectUri,
					  HttpServletResponse response) throws IOException {
		RegisteredClient registeredClient = getAuthenticatedClient().getRegisteredClient();
		OAuth2Authorization authorization = notNull(authorizationService.findByToken(code, TokenType.AUTHORIZATION_CODE),
			invalidGrantException());
		OAuth2AuthorizationRequest authorizationRequest = authorization.getAuthorizationRequest();

		if (!registeredClient.getClientId().equals(authorizationRequest.getClientId())) {
			// 如果已授权，移除授权
			if (!authorization.getTokens().isInvalidated(TokenType.AUTHORIZATION_CODE)) {
				authorization = authorization.invalidate(code);
				authorizationService.save(authorization);
			}
			throw invalidGrantException().get();
		}

		isTrue(isBlank(authorizationRequest.getRedirectUri()) ||
			authorizationRequest.getRedirectUri().equals(redirectUri), invalidGrantException());
		isFalse(authorization.getTokens().isInvalidated(TokenType.AUTHORIZATION_CODE), invalidGrantException());

		Set<String> authorizedScopes = authorization.getAuthorizedScopes();
		Jwt jwt = jwtEncoder.issueJwtAccessToken(authorization.getPrincipalName(), registeredClient, authorizedScopes);
		OAuth2Tokens tokens = new OAuth2Tokens(authorization.getTokens())
			.accessToken(jwt, authorizedScopes).refreshToken(registeredClient)
			.oidcIdToken(jwtEncoder, authorization, registeredClient);
		// 授权码只能使用一次，作废授权码
		authorizationService.save(OAuth2Authorization.from(authorization)
			.set(OAuth2Authorization::setTokens, tokens)
			.put(OAuth2Authorization::getAttributes, ACCESS_TOKEN_ATTRIBUTES, jwt)
			.build().invalidate(code));

		sendAccessTokenResponse(response, tokens.getAccessOidcTokenResponse());
	}

	@PostMapping(value = DEFAULT_TOKEN_ENDPOINT_URI, params = GRANT_TYPE_REFRESH_TOKEN)
	public void refreshToken(@RequestParam(REFRESH_TOKEN) @NotBlank String refreshToken,
							 @RequestParam(value = SCOPE, required = false) String scope,
							 HttpServletResponse response) throws IOException {
		Set<String> scopes = extractScopes(scope);
		RegisteredClient registeredClient = getAuthenticatedClient().getRegisteredClient();
		OAuth2Authorization authorization = notNull(authorizationService.findByToken(refreshToken, TokenType.REFRESH_TOKEN), invalidGrantException());
		isTrue(registeredClient.getId().equals(authorization.getRegisteredClientId()), invalidGrantException());
		isTrue(registeredClient.supportedRefreshToken(), exceptionSupplier(UNAUTHORIZED_CLIENT));
		isFalse(authorization.getTokens().getRefreshToken().getExpiresAt().isBefore(Instant.now()), invalidGrantException());

		Set<String> authorizedScopes = authorization.getAuthorizedScopes();
		isTrue(authorizedScopes.containsAll(scopes), exceptionSupplier(INVALID_SCOPE));
		if (scopes.isEmpty()) {
			scopes = authorizedScopes;
		}
		isFalse(authorization.getTokens().isInvalidated(TokenType.REFRESH_TOKEN), invalidGrantException());

		Jwt jwt = jwtEncoder.issueJwtAccessToken(authorization.getPrincipalName(), registeredClient, scopes);
		OAuth2Tokens tokens = new OAuth2Tokens(authorization.getTokens())
			.accessToken(jwt, scopes).refreshToken(registeredClient);
		authorizationService.save(OAuth2Authorization.from(authorization)
			.set(OAuth2Authorization::setTokens, tokens)
			.put(OAuth2Authorization::getAttributes, ACCESS_TOKEN_ATTRIBUTES, jwt)
			.build());

		sendAccessTokenResponse(response, tokens.getAccessTokenResponse());
	}

	@PostMapping(value = DEFAULT_TOKEN_ENDPOINT_URI, params = GRANT_TYPE_CLIENT_CREDENTIALS)
	public void token(@RequestParam(value = SCOPE, required = false) String scope,
					  HttpServletResponse response) throws IOException {
		Set<String> scopes = extractScopes(scope);
		OAuth2ClientAuthToken clientPrincipal = getAuthenticatedClient();
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		isTrue(registeredClient.supportedClientCredentials(), exceptionSupplier(UNAUTHORIZED_CLIENT));

		Set<String> clientScopes = registeredClient.getScopes();
		isTrue(clientScopes.containsAll(scopes), exceptionSupplier(INVALID_SCOPE));
		if (scopes.isEmpty()) {
			scopes = new LinkedHashSet<>(clientScopes);
		}

		Jwt jwt = jwtEncoder.issueJwtAccessToken(clientPrincipal.getName(), registeredClient, scopes);
		OAuth2Tokens tokens = new OAuth2Tokens().accessToken(jwt, scopes);

		this.authorizationService.save(ObjectBuilder.builder(OAuth2Authorization::new)
			.set(OAuth2Authorization::setRegisteredClientId, registeredClient.getId())
			.set(OAuth2Authorization::setPrincipalName, clientPrincipal.getName())
			.set(OAuth2Authorization::setTokens, tokens)
			.put(OAuth2Authorization::getAttributes, ACCESS_TOKEN_ATTRIBUTES, jwt)
			.build());

		sendAccessTokenResponse(response, tokens.getAccessOidcTokenResponse());
	}

	@PostMapping(DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI)
	public void revoke(@RequestParam @NotBlank String token, HttpServletResponse response) throws IOException {
		try {
			OAuth2Authorization authorization = authorizationService.findByToken(token);
			if (authorization == null) {
				return;
			}
			isTrue(getAuthenticatedClient().getRegisteredClient().getId().equals(authorization.getRegisteredClientId()),
				invalidClientException());
			authorizationService.save(authorization.invalidate(token));
		} catch (OAuth2AuthenticationException ex) {
			clearContext();
			sendErrorResponse(response, ex.getError());
		}
	}

	private void redirectError(HttpServletResponse response, String redirectUri,
							   OAuth2AuthenticationException exception, String state) throws IOException {
		OAuth2Error error = exception.getError();
		response.sendRedirect(ObjectBuilder.builder(UrlBuilder.of(redirectUri, UTF_8))
			.identity(b -> b.addQuery(ERROR, error.getErrorCode()))
			.identity(isNotBlank(error.getDescription()), b -> b.addQuery(ERROR_DESCRIPTION, error.getDescription()))
			.identity(isNotBlank(state), b -> b.addQuery(STATE, state))
			.identity(isNotBlank(error.getUri()), b -> b.addQuery(ERROR_URI, error.getUri()))
			.build().build());
	}

	private static Authentication getAuthentication() {
		return getContext().getAuthentication();
	}

	private static OAuth2ClientAuthToken getAuthenticatedClient() {
		Authentication clientPrincipal = getAuthentication();
		if (clientPrincipal != null && clientPrincipal.isAuthenticated() &&
			OAuth2ClientAuthToken.class.isAssignableFrom(clientPrincipal.getClass())) {
			return (OAuth2ClientAuthToken) clientPrincipal;
		}
		throw invalidClientException().get();
	}

	private void sendErrorResponse(HttpServletResponse response, OAuth2Error error) throws IOException {
		errorHttpResponseConverter.write(error, null, builder(ServletServerHttpResponse::new, response)
			.set(ServletServerHttpResponse::setStatusCode, BAD_REQUEST).build());
	}

	private void sendAccessTokenResponse(HttpServletResponse response,
										 OAuth2AccessTokenResponse accessTokenResponse) throws IOException {
		accessTokenResponseConverter.write(accessTokenResponse, null, new ServletServerHttpResponse(response));
	}

	private static Supplier<OAuth2AuthenticationException> exceptionSupplier(String errorCode, String description) {
		return () -> new OAuth2AuthenticationException(new OAuth2Error(errorCode, description, null));
	}

	private static Supplier<OAuth2AuthenticationException> exceptionSupplier(String errorCode) {
		return exceptionSupplier(errorCode, null);
	}

	public static Supplier<OAuth2AuthenticationException> invalidRequestException(String parameterName) {
		return exceptionSupplier(INVALID_REQUEST, "OAuth 2.0参数错误：" + parameterName);
	}

	private static Supplier<OAuth2AuthenticationException> invalidClientException() {
		return exceptionSupplier(INVALID_CLIENT);
	}

	private static Supplier<OAuth2AuthenticationException> invalidGrantException() {
		return exceptionSupplier(INVALID_GRANT);
	}

	// TODO 确认多值参数
	private static Set<String> extractScopes(String scope) {
		return isNotBlank(scope) ? Arrays.stream(split(scope, " ")).collect(toSet()) : Collections.emptySet();
	}

}
