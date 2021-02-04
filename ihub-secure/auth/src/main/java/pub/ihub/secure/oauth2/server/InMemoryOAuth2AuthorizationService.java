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

package pub.ihub.secure.oauth2.server;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import pub.ihub.secure.oauth2.server.token.OAuth2AuthorizationCode;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.STATE;

/**
 * OAuth2授权服务（内存中）TODO
 *
 * @author henry
 */
public class InMemoryOAuth2AuthorizationService implements OAuth2AuthorizationService {

	private final Map<OAuth2AuthorizationId, OAuth2Authorization> authorizations = new ConcurrentHashMap<>();

	@Override
	public void save(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		OAuth2AuthorizationId authorizationId = new OAuth2AuthorizationId(
			authorization.getRegisteredClientId(), authorization.getPrincipalName());
		authorizations.put(authorizationId, authorization);
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		OAuth2AuthorizationId authorizationId = new OAuth2AuthorizationId(
			authorization.getRegisteredClientId(), authorization.getPrincipalName());
		authorizations.remove(authorizationId, authorization);
	}

	@Override
	public OAuth2Authorization findByToken(String token, @Nullable TokenType tokenType) {
		Assert.hasText(token, "token cannot be empty");
		return authorizations.values().stream()
			.filter(authorization -> hasToken(authorization, token, tokenType))
			.findFirst()
			.orElse(null);
	}

	@Override
	public OAuth2Authorization findByState(String state) {
		OAuth2Authorization authorization = authorizations.values().stream()
			.filter(auth -> state.equals(auth.getState()))
			.findFirst()
			.orElse(null);
		assert authorization != null;
		// TODO 是否需要移除
		authorization.getAttributes().remove(STATE);
		return authorization;
	}

	private static boolean hasToken(OAuth2Authorization authorization, String token, @Nullable TokenType tokenType) {
		if (tokenType == null) {
			return matchesAuthorizationCode(authorization, token) ||
				matchesAccessToken(authorization, token) ||
				matchesRefreshToken(authorization, token);
		} else if (TokenType.AUTHORIZATION_CODE.equals(tokenType)) {
			return matchesAuthorizationCode(authorization, token);
		} else if (TokenType.ACCESS_TOKEN.equals(tokenType)) {
			return matchesAccessToken(authorization, token);
		} else if (TokenType.REFRESH_TOKEN.equals(tokenType)) {
			return matchesRefreshToken(authorization, token);
		}
		return false;
	}

	private static boolean matchesAuthorizationCode(OAuth2Authorization authorization, String token) {
		OAuth2AuthorizationCode authorizationCode = authorization.getTokens().getAuthorizationCode();
		return authorizationCode != null && authorizationCode.getTokenValue().equals(token);
	}

	private static boolean matchesAccessToken(OAuth2Authorization authorization, String token) {
		return authorization.getTokens().getAccessToken() != null &&
			authorization.getTokens().getAccessToken().getTokenValue().equals(token);
	}

	private static boolean matchesRefreshToken(OAuth2Authorization authorization, String token) {
		return authorization.getTokens().getRefreshToken() != null &&
			authorization.getTokens().getRefreshToken().getTokenValue().equals(token);
	}

	@RequiredArgsConstructor
	@EqualsAndHashCode
	private static class OAuth2AuthorizationId implements Serializable {

		private static final long serialVersionUID = SERIAL_VERSION_UID;
		private final String registeredClientId;
		private final String principalName;

	}

}
