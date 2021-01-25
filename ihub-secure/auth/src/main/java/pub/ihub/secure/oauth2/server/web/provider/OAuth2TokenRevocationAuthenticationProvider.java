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
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.util.StringUtils;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.TokenType;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.web.OAuth2AuthProvider;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthToken;
import pub.ihub.secure.oauth2.server.web.token.OAuth2RevocationToken;

/**
 * OAuth 2.0令牌吊销的AuthenticationProvider实现。
 *
 * @author henry
 */
@RequiredArgsConstructor
public class OAuth2TokenRevocationAuthenticationProvider extends OAuth2AuthProvider<OAuth2RevocationToken> {

	private final OAuth2AuthorizationService authorizationService;

	@Override
	public Authentication auth(OAuth2RevocationToken authentication) throws AuthenticationException {
		OAuth2ClientAuthToken clientPrincipal = authentication.getAuthenticatedClient();
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		TokenType tokenType = null;
		String tokenTypeHint = authentication.getTokenTypeHint();
		if (StringUtils.hasText(tokenTypeHint)) {
			if (TokenType.REFRESH_TOKEN.getValue().equals(tokenTypeHint)) {
				tokenType = TokenType.REFRESH_TOKEN;
			} else if (TokenType.ACCESS_TOKEN.getValue().equals(tokenTypeHint)) {
				tokenType = TokenType.ACCESS_TOKEN;
			}
		}

		OAuth2Authorization authorization = this.authorizationService.findByToken(
			authentication.getToken(), tokenType);
		if (authorization == null) {
			// Return the authentication request when token not found
			return authentication;
		}

		if (!registeredClient.getId().equals(authorization.getRegisteredClientId())) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
		}

		AbstractOAuth2Token token = authorization.getTokens().getToken(authentication.getToken());
		authorization = authorization.invalidate(token);
		this.authorizationService.save(authorization);

		return new OAuth2RevocationToken(token, clientPrincipal);
	}

}
