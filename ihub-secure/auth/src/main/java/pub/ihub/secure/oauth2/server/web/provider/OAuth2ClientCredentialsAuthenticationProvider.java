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
import org.springframework.security.oauth2.jwt.Jwt;
import pub.ihub.core.ObjectBuilder;
import pub.ihub.secure.oauth2.jwt.JwtEncoder;
import org.springframework.util.CollectionUtils;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.token.OAuth2Tokens;
import pub.ihub.secure.oauth2.server.web.token.OAuth2AccessTokenAuthenticationToken;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthenticationToken;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientCredentialsAuthenticationToken;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static pub.ihub.secure.oauth2.server.OAuth2Authorization.ACCESS_TOKEN_ATTRIBUTES;
import static pub.ihub.secure.oauth2.server.web.provider.OAuth2AuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient;
import static pub.ihub.secure.oauth2.server.web.provider.OAuth2TokenIssuerUtil.issueJwtAccessToken;

/**
 * OAuth 2.0客户端凭据授予的AuthenticationProvider实现。
 *
 * @author henry
 */
@RequiredArgsConstructor
public class OAuth2ClientCredentialsAuthenticationProvider implements AuthenticationProvider {

	private final OAuth2AuthorizationService authorizationService;
	private final JwtEncoder jwtEncoder;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		OAuth2ClientCredentialsAuthenticationToken clientCredentialsAuthentication =
			(OAuth2ClientCredentialsAuthenticationToken) authentication;

		OAuth2ClientAuthenticationToken clientPrincipal =
			getAuthenticatedClientElseThrowInvalidClient(clientCredentialsAuthentication);
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.CLIENT_CREDENTIALS)) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT));
		}

		Set<String> scopes = registeredClient.getScopes();        // Default to configured scopes
		if (!CollectionUtils.isEmpty(clientCredentialsAuthentication.getScopes())) {
			Set<String> unauthorizedScopes = clientCredentialsAuthentication.getScopes().stream()
				.filter(requestedScope -> !registeredClient.getScopes().contains(requestedScope))
				.collect(Collectors.toSet());
			if (!CollectionUtils.isEmpty(unauthorizedScopes)) {
				throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE));
			}
			scopes = new LinkedHashSet<>(clientCredentialsAuthentication.getScopes());
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

		return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuth2ClientCredentialsAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
