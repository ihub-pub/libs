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
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.CollectionUtils;
import pub.ihub.core.ObjectBuilder;
import pub.ihub.secure.oauth2.jwt.JwtEncoder;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.token.OAuth2Tokens;
import pub.ihub.secure.oauth2.server.web.OAuth2AuthProvider;
import pub.ihub.secure.oauth2.server.web.token.OAuth2AccessAuthToken;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthToken;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientCredentialsToken;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static pub.ihub.secure.oauth2.server.OAuth2Authorization.ACCESS_TOKEN_ATTRIBUTES;
import static pub.ihub.secure.oauth2.server.web.provider.OAuth2TokenIssuerUtil.issueJwtAccessToken;

/**
 * OAuth 2.0客户端凭据授予的AuthenticationProvider实现。
 *
 * @author henry
 */
@RequiredArgsConstructor
public class OAuth2ClientCredentialsAuthenticationProvider extends OAuth2AuthProvider<OAuth2ClientCredentialsToken> {

	private final OAuth2AuthorizationService authorizationService;
	private final JwtEncoder jwtEncoder;

	@Override
	public Authentication auth(OAuth2ClientCredentialsToken authentication) throws AuthenticationException {
		OAuth2ClientAuthToken clientPrincipal = authentication.getAuthenticatedClient();
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.CLIENT_CREDENTIALS)) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT));
		}

		// Default to configured scopes
		Set<String> scopes = registeredClient.getScopes();
		if (!CollectionUtils.isEmpty(authentication.getScopes())) {
			Set<String> unauthorizedScopes = authentication.getScopes().stream()
				.filter(requestedScope -> !registeredClient.getScopes().contains(requestedScope))
				.collect(Collectors.toSet());
			if (!CollectionUtils.isEmpty(unauthorizedScopes)) {
				throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE));
			}
			scopes = new LinkedHashSet<>(authentication.getScopes());
		}

		Jwt jwt = issueJwtAccessToken(this.jwtEncoder, clientPrincipal.getName(), registeredClient.getClientId(), scopes, registeredClient.getAccessTokenTimeToLive());
		OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
			jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), scopes);

		OAuth2Authorization authorization = ObjectBuilder.builder(OAuth2Authorization::new)
			.set(OAuth2Authorization::setRegisteredClientId, registeredClient.getId())
			.set(OAuth2Authorization::setPrincipalName, clientPrincipal.getName())
			.set(OAuth2Authorization::setTokens,
				ObjectBuilder.builder(OAuth2Tokens::new).set(OAuth2Tokens::accessToken, accessToken).build())
			.set(OAuth2Authorization::setAttributes, new HashMap<>(1) {
				{
					put(ACCESS_TOKEN_ATTRIBUTES, jwt);
				}
			})
			.build();
		this.authorizationService.save(authorization);

		return new OAuth2AccessAuthToken(registeredClient, clientPrincipal, accessToken);
	}

}
