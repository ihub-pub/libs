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
import org.springframework.security.oauth2.jwt.Jwt;
import pub.ihub.secure.oauth2.jwt.JwtEncoder;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.TokenType;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.config.TokenSettings;
import pub.ihub.secure.oauth2.server.token.OAuth2TokenMetadata;
import pub.ihub.secure.oauth2.server.token.OAuth2Tokens;
import pub.ihub.secure.oauth2.server.web.token.OAuth2AccessTokenAuthenticationToken;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthenticationToken;
import pub.ihub.secure.oauth2.server.web.token.OAuth2RefreshTokenAuthenticationToken;

import java.time.Instant;
import java.util.Set;

import static pub.ihub.secure.oauth2.server.web.provider.OAuth2AuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient;


/**
 * OAuth 2.0刷新令牌授予的AuthenticationProvider实现。
 *
 * @author henry
 */
@RequiredArgsConstructor
public class OAuth2RefreshTokenAuthenticationProvider implements AuthenticationProvider {

	private final OAuth2AuthorizationService authorizationService;
	private final JwtEncoder jwtEncoder;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		OAuth2RefreshTokenAuthenticationToken refreshTokenAuthentication =
			(OAuth2RefreshTokenAuthenticationToken) authentication;

		OAuth2ClientAuthenticationToken clientPrincipal =
			getAuthenticatedClientElseThrowInvalidClient(refreshTokenAuthentication);
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		OAuth2Authorization authorization = this.authorizationService.findByToken(
			refreshTokenAuthentication.getRefreshToken(), TokenType.REFRESH_TOKEN);
		if (authorization == null) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT));
		}

		if (!registeredClient.getId().equals(authorization.getRegisteredClientId())) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
		}

		if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT));
		}

		Instant refreshTokenExpiresAt = authorization.getTokens().getRefreshToken().getExpiresAt();
		if (refreshTokenExpiresAt.isBefore(Instant.now())) {
			// As per https://tools.ietf.org/html/rfc6749#section-5.2
			// invalid_grant: The provided authorization grant (e.g., authorization code,
			// resource owner credentials) or refresh token is invalid, expired, revoked [...].
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT));
		}

		// As per https://tools.ietf.org/html/rfc6749#section-6
		// The requested scope MUST NOT include any scope not originally granted by the resource owner,
		// and if omitted is treated as equal to the scope originally granted by the resource owner.
		Set<String> scopes = refreshTokenAuthentication.getScopes();
		Set<String> authorizedScopes = authorization.getAttribute(OAuth2Authorization.AUTHORIZED_SCOPES);
		if (!authorizedScopes.containsAll(scopes)) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE));
		}
		if (scopes.isEmpty()) {
			scopes = authorizedScopes;
		}

		OAuth2RefreshToken refreshToken = authorization.getTokens().getRefreshToken();
		OAuth2TokenMetadata refreshTokenMetadata = authorization.getTokens().getTokenMetadata(refreshToken);

		if (refreshTokenMetadata.isInvalidated()) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT));
		}

		Jwt jwt = OAuth2TokenIssuerUtil
			.issueJwtAccessToken(this.jwtEncoder, authorization.getPrincipalName(), registeredClient.getClientId(), scopes, registeredClient.getTokenSettings().accessTokenTimeToLive());
		OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
			jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), scopes);

		TokenSettings tokenSettings = registeredClient.getTokenSettings();

		if (!tokenSettings.reuseRefreshTokens()) {
			refreshToken = OAuth2TokenIssuerUtil.issueRefreshToken(tokenSettings.refreshTokenTimeToLive());
		}

		authorization = OAuth2Authorization.from(authorization)
			.tokens(OAuth2Tokens.from(authorization.getTokens()).accessToken(accessToken).refreshToken(refreshToken).build())
			.attribute(OAuth2Authorization.ACCESS_TOKEN_ATTRIBUTES, jwt)
			.build();
		this.authorizationService.save(authorization);

		return new OAuth2AccessTokenAuthenticationToken(
			registeredClient, clientPrincipal, accessToken, refreshToken);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuth2RefreshTokenAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
