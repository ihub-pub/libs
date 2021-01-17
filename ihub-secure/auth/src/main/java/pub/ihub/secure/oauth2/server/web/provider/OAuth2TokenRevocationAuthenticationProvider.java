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
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.util.StringUtils;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.TokenType;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthenticationToken;
import pub.ihub.secure.oauth2.server.web.token.OAuth2TokenRevocationAuthenticationToken;

import static pub.ihub.secure.oauth2.server.web.provider.OAuth2AuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient;

/**
 * OAuth 2.0令牌吊销的AuthenticationProvider实现。
 *
 * @author henry
 */
@RequiredArgsConstructor
public class OAuth2TokenRevocationAuthenticationProvider implements AuthenticationProvider {

	private final OAuth2AuthorizationService authorizationService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		OAuth2TokenRevocationAuthenticationToken tokenRevocationAuthentication =
			(OAuth2TokenRevocationAuthenticationToken) authentication;

		OAuth2ClientAuthenticationToken clientPrincipal =
			getAuthenticatedClientElseThrowInvalidClient(tokenRevocationAuthentication);
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		TokenType tokenType = null;
		String tokenTypeHint = tokenRevocationAuthentication.getTokenTypeHint();
		if (StringUtils.hasText(tokenTypeHint)) {
			if (TokenType.REFRESH_TOKEN.getValue().equals(tokenTypeHint)) {
				tokenType = TokenType.REFRESH_TOKEN;
			} else if (TokenType.ACCESS_TOKEN.getValue().equals(tokenTypeHint)) {
				tokenType = TokenType.ACCESS_TOKEN;
			}
		}

		OAuth2Authorization authorization = this.authorizationService.findByToken(
			tokenRevocationAuthentication.getToken(), tokenType);
		if (authorization == null) {
			// Return the authentication request when token not found
			return tokenRevocationAuthentication;
		}

		if (!registeredClient.getId().equals(authorization.getRegisteredClientId())) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
		}

		AbstractOAuth2Token token = authorization.getTokens().getToken(tokenRevocationAuthentication.getToken());
		authorization = OAuth2AuthenticationProviderUtils.invalidate(authorization, token);
		this.authorizationService.save(authorization);

		return new OAuth2TokenRevocationAuthenticationToken(token, clientPrincipal);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuth2TokenRevocationAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
