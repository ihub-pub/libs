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

package pub.ihub.secure.oauth2.server.web.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.jwt.Jwt;
import pub.ihub.core.ObjectBuilder;
import pub.ihub.secure.oauth2.jwt.JwtEncoder;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.token.OAuth2AuthorizationCode;
import pub.ihub.secure.oauth2.server.token.OAuth2TokenMetadata;
import pub.ihub.secure.oauth2.server.token.OAuth2Tokens;
import pub.ihub.secure.oauth2.server.web.OAuth2AuthProvider;
import pub.ihub.secure.oauth2.server.web.token.OAuth2AccessAuthToken;
import pub.ihub.secure.oauth2.server.web.token.OAuth2AuthCodeToken;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthToken;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.lang.Assert.isFalse;
import static cn.hutool.core.lang.Assert.isTrue;
import static cn.hutool.core.lang.Assert.notNull;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;
import static org.springframework.security.oauth2.core.OAuth2RefreshToken2.issueRefreshToken;
import static org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID;
import static org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.ID_TOKEN;
import static org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.NONCE;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.ACCESS_TOKEN_ATTRIBUTES;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.AUTHORIZATION_REQUEST;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.AUTHORIZED_SCOPES;
import static pub.ihub.secure.oauth2.server.TokenType.AUTHORIZATION_CODE;
import static pub.ihub.secure.oauth2.server.web.provider.OAuth2TokenIssuerUtil.issueIdToken;
import static pub.ihub.secure.oauth2.server.web.provider.OAuth2TokenIssuerUtil.issueJwtAccessToken;


/**
 * OAuth 2.0授权代码授予的AuthenticationProvider实现。
 *
 * @author henry
 */
@RequiredArgsConstructor
public class OAuth2AuthorizationCodeAuthenticationProvider extends OAuth2AuthProvider<OAuth2AuthCodeToken> {

	private final OAuth2AuthorizationService authorizationService;
	private final JwtEncoder jwtEncoder;

	@Override
	public Authentication auth(OAuth2AuthCodeToken authentication) throws AuthenticationException {
		OAuth2ClientAuthToken clientPrincipal = authentication.getAuthenticatedClient();
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		OAuth2Authorization authorization = this.authorizationService.findByToken(
			authentication.getCode(), AUTHORIZATION_CODE);
		notNull(authorization, invalidGrantException());
		OAuth2AuthorizationCode authorizationCode = authorization.getTokens().getToken(OAuth2AuthorizationCode.class);
		OAuth2TokenMetadata authorizationCodeMetadata = authorization.getTokens().getTokenMetadata(authorizationCode);

		OAuth2AuthorizationRequest authorizationRequest = authorization.getAttribute(AUTHORIZATION_REQUEST);

		if (!registeredClient.getClientId().equals(authorizationRequest.getClientId())) {
			if (!authorizationCodeMetadata.isInvalidated()) {
				// Invalidate the authorization code given that a different client is attempting to use it
				authorization = authorization.invalidate(authorizationCode);
				this.authorizationService.save(authorization);
			}
			throw invalidGrantException().get();
		}

		isTrue(isBlank(authorizationRequest.getRedirectUri()) ||
			authorizationRequest.getRedirectUri().equals(authentication.getRedirectUri()), invalidGrantException());

		isFalse(authorizationCodeMetadata.isInvalidated(), invalidGrantException());

		Set<String> authorizedScopes = authorization.getAttribute(AUTHORIZED_SCOPES);
		Jwt jwt = issueJwtAccessToken(
			this.jwtEncoder, authorization.getPrincipalName(), registeredClient.getClientId(),
			authorizedScopes, registeredClient.getAccessTokenTimeToLive());
		OAuth2AccessToken accessToken = new OAuth2AccessToken(BEARER,
			jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), authorizedScopes);

		OAuth2Tokens tokens = ObjectBuilder.clone(authorization.getTokens())
			.set(OAuth2Tokens::accessToken, accessToken).build();

		OAuth2RefreshToken refreshToken = null;
		if (registeredClient.getAuthorizationGrantTypes().contains(REFRESH_TOKEN)) {
			refreshToken = issueRefreshToken(registeredClient.getRefreshTokenTimeToLive());
			tokens.refreshToken(refreshToken);
		}

		OidcIdToken idToken = null;
		if (authorizationRequest.getScopes().contains(OPENID)) {
			Jwt jwtIdToken = issueIdToken(
				this.jwtEncoder, authorization.getPrincipalName(), registeredClient.getClientId(),
				(String) authorizationRequest.getAdditionalParameters().get(NONCE));
			idToken = new OidcIdToken(jwtIdToken.getTokenValue(), jwtIdToken.getIssuedAt(),
				jwtIdToken.getExpiresAt(), jwtIdToken.getClaims());
			tokens.token(idToken);
		}

		authorization = OAuth2Authorization.from(authorization)
			.set(OAuth2Authorization::setTokens, tokens)
			.put(OAuth2Authorization::getAttributes, ACCESS_TOKEN_ATTRIBUTES, jwt)
			.build();

		// Invalidate the authorization code as it can only be used once
		authorization = authorization.invalidate(authorizationCode);

		this.authorizationService.save(authorization);

		Map<String, Object> additionalParameters = Collections.emptyMap();
		if (idToken != null) {
			additionalParameters = new HashMap<>();
			additionalParameters.put(ID_TOKEN, idToken.getTokenValue());
		}

		return new OAuth2AccessAuthToken(
			registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
	}

}
