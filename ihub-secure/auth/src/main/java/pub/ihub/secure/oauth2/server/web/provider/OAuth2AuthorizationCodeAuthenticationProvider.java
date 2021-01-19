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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import pub.ihub.core.ObjectBuilder;
import pub.ihub.secure.oauth2.jwt.JwtEncoder;
import org.springframework.util.StringUtils;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.TokenType;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.token.OAuth2AuthorizationCode;
import pub.ihub.secure.oauth2.server.token.OAuth2TokenMetadata;
import pub.ihub.secure.oauth2.server.token.OAuth2Tokens;
import pub.ihub.secure.oauth2.server.web.token.OAuth2AccessTokenAuthenticationToken;
import pub.ihub.secure.oauth2.server.web.token.OAuth2AuthorizationCodeAuthenticationToken;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthenticationToken;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static pub.ihub.secure.oauth2.server.web.provider.OAuth2AuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient;


/**
 * OAuth 2.0授权代码授予的AuthenticationProvider实现。
 *
 * @author henry
 */
@RequiredArgsConstructor
public class OAuth2AuthorizationCodeAuthenticationProvider implements AuthenticationProvider {

	private final OAuth2AuthorizationService authorizationService;
	private final JwtEncoder jwtEncoder;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		OAuth2AuthorizationCodeAuthenticationToken authorizationCodeAuthentication =
			(OAuth2AuthorizationCodeAuthenticationToken) authentication;

		OAuth2ClientAuthenticationToken clientPrincipal =
			getAuthenticatedClientElseThrowInvalidClient(authorizationCodeAuthentication);
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		OAuth2Authorization authorization = this.authorizationService.findByToken(
			authorizationCodeAuthentication.getCode(), TokenType.AUTHORIZATION_CODE);
		if (authorization == null) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT));
		}
		OAuth2AuthorizationCode authorizationCode = authorization.getTokens().getToken(OAuth2AuthorizationCode.class);
		OAuth2TokenMetadata authorizationCodeMetadata = authorization.getTokens().getTokenMetadata(authorizationCode);

		OAuth2AuthorizationRequest authorizationRequest = authorization.getAttribute(
			OAuth2Authorization.AUTHORIZATION_REQUEST);

		if (!registeredClient.getClientId().equals(authorizationRequest.getClientId())) {
			if (!authorizationCodeMetadata.isInvalidated()) {
				// Invalidate the authorization code given that a different client is attempting to use it
				authorization = OAuth2AuthenticationProviderUtils.invalidate(authorization, authorizationCode);
				this.authorizationService.save(authorization);
			}
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT));
		}

		if (StringUtils.hasText(authorizationRequest.getRedirectUri()) &&
			!authorizationRequest.getRedirectUri().equals(authorizationCodeAuthentication.getRedirectUri())) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT));
		}

		if (authorizationCodeMetadata.isInvalidated()) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT));
		}

		Set<String> authorizedScopes = authorization.getAttribute(OAuth2Authorization.AUTHORIZED_SCOPES);
		Jwt jwt = OAuth2TokenIssuerUtil.issueJwtAccessToken(
			this.jwtEncoder, authorization.getPrincipalName(), registeredClient.getClientId(),
			authorizedScopes, registeredClient.getTokenSettings().accessTokenTimeToLive());
		OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
			jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), authorizedScopes);

		OAuth2Tokens tokens = ObjectBuilder.clone(authorization.getTokens())
			.set(OAuth2Tokens::accessToken,accessToken).build();

		OAuth2RefreshToken refreshToken = null;
		if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
			refreshToken = OAuth2TokenIssuerUtil.issueRefreshToken(registeredClient.getTokenSettings().refreshTokenTimeToLive());
			tokens.refreshToken(refreshToken);
		}

		OidcIdToken idToken = null;
		if (authorizationRequest.getScopes().contains(OidcScopes.OPENID)) {
			Jwt jwtIdToken = OAuth2TokenIssuerUtil.issueIdToken(
				this.jwtEncoder, authorization.getPrincipalName(), registeredClient.getClientId(),
				(String) authorizationRequest.getAdditionalParameters().get(OidcParameterNames.NONCE));
			idToken = new OidcIdToken(jwtIdToken.getTokenValue(), jwtIdToken.getIssuedAt(),
				jwtIdToken.getExpiresAt(), jwtIdToken.getClaims());
			tokens.token(idToken);
		}

		authorization = OAuth2Authorization.from(authorization)
			.tokens(tokens)
			.attribute(OAuth2Authorization.ACCESS_TOKEN_ATTRIBUTES, jwt)
			.build();

		// Invalidate the authorization code as it can only be used once
		authorization = OAuth2AuthenticationProviderUtils.invalidate(authorization, authorizationCode);

		this.authorizationService.save(authorization);

		Map<String, Object> additionalParameters = Collections.emptyMap();
		if (idToken != null) {
			additionalParameters = new HashMap<>();
			additionalParameters.put(OidcParameterNames.ID_TOKEN, idToken.getTokenValue());
		}

		return new OAuth2AccessTokenAuthenticationToken(
			registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuth2AuthorizationCodeAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
