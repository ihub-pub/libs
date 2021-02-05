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
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cn.hutool.core.lang.Assert.notBlank;
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
	public OAuth2Authorization findByToken(String token) {
		notBlank(token, "令牌不能为空！");
		return authorizations.values().stream()
			.filter(authorization -> authorization.matchesToken(token))
			.findFirst().orElse(null);
	}

	@Override
	public OAuth2Authorization findByToken(String token, TokenType tokenType) {
		notBlank(token, "令牌不能为空！");
		return authorizations.values().stream()
			.filter(authorization -> authorization.matchesToken(token, tokenType))
			.findFirst().orElse(null);
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

	@RequiredArgsConstructor
	@EqualsAndHashCode
	private static class OAuth2AuthorizationId implements Serializable {

		private static final long serialVersionUID = SERIAL_VERSION_UID;
		private final String registeredClientId;
		private final String principalName;

	}

}
