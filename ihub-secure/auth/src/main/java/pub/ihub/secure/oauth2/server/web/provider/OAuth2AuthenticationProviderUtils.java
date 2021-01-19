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

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import pub.ihub.core.ObjectBuilder;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.token.OAuth2AuthorizationCode;
import pub.ihub.secure.oauth2.server.token.OAuth2TokenMetadata;
import pub.ihub.secure.oauth2.server.token.OAuth2Tokens;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthenticationToken;

/**
 * OAuth 2.0 AuthenticationProvider的实用程序方法。
 *
 * @author henry
 */
final class OAuth2AuthenticationProviderUtils {

	static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
		OAuth2ClientAuthenticationToken clientPrincipal = null;
		if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
			clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
		}
		if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
			return clientPrincipal;
		}
		throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
	}

	static <T extends AbstractOAuth2Token> OAuth2Authorization invalidate(
		OAuth2Authorization authorization, T token) {

		OAuth2TokenMetadata metadata = ObjectBuilder.builder(OAuth2TokenMetadata::new)
			.put(OAuth2TokenMetadata::getMetadata, OAuth2TokenMetadata.INVALIDATED, true).build();
		OAuth2Tokens tokens = ObjectBuilder.clone(authorization.getTokens()).build();
		tokens.token(token, metadata);

		if (OAuth2RefreshToken.class.isAssignableFrom(token.getClass())) {
			tokens.token(
				authorization.getTokens().getAccessToken(), metadata);
			OAuth2AuthorizationCode authorizationCode =
				authorization.getTokens().getToken(OAuth2AuthorizationCode.class);
			if (authorizationCode != null &&
				!authorization.getTokens().getTokenMetadata(authorizationCode).isInvalidated()) {
				tokens.token(authorizationCode, metadata);
			}
		}

		return OAuth2Authorization.from(authorization)
			.tokens(tokens)
			.build();
	}

}
