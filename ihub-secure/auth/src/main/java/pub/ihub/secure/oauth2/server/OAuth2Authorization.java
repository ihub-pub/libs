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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import pub.ihub.core.ObjectBuilder;
import pub.ihub.secure.oauth2.server.token.OAuth2AuthorizationCode;
import pub.ihub.secure.oauth2.server.token.OAuth2TokenMetadata;
import pub.ihub.secure.oauth2.server.token.OAuth2Tokens;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.lang.Assert.notBlank;
import static cn.hutool.core.lang.Assert.notNull;
import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;
import static pub.ihub.secure.oauth2.server.token.OAuth2TokenMetadata.INVALIDATED;

/**
 * OAuth 2.0授权的表示形式，其中包含与resource owner授予client的授权相关的状态。
 *
 * @author henry
 */
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class OAuth2Authorization implements Serializable {

	public static String STATE = OAuth2Authorization.class.getName().concat(".STATE");
	public static String AUTHORIZATION_REQUEST = OAuth2Authorization.class.getName().concat(".AUTHORIZATION_REQUEST");
	public static String AUTHORIZED_SCOPES = OAuth2Authorization.class.getName().concat(".AUTHORIZED_SCOPES");
	public static String ACCESS_TOKEN_ATTRIBUTES = OAuth2Authorization.class.getName().concat(".ACCESS_TOKEN_ATTRIBUTES");

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	private String registeredClientId;
	private String principalName;
	private OAuth2Tokens tokens;

	private Map<String, Object> attributes;

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T) this.attributes.get(notBlank(name, "name不能为空！"));
	}

	public static ObjectBuilder<OAuth2Authorization> from(OAuth2Authorization authorization) {
		notNull(authorization);
		return ObjectBuilder.builder(OAuth2Authorization::new)
			.set(OAuth2Authorization::setRegisteredClientId, authorization.registeredClientId)
			.set(OAuth2Authorization::setPrincipalName, authorization.principalName)
			.set(OAuth2Authorization::setTokens, ObjectBuilder.clone(authorization.getTokens()).build())
			.set(OAuth2Authorization::setAttributes, new HashMap<>(authorization.getAttributes()));
	}

	public <T extends AbstractOAuth2Token> OAuth2Authorization invalidate(T token) {
		OAuth2TokenMetadata metadata = ObjectBuilder.builder(OAuth2TokenMetadata::new)
			.put(OAuth2TokenMetadata::getMetadata, INVALIDATED, true).build();
		OAuth2Tokens tokens = ObjectBuilder.clone(this.getTokens()).build();
		tokens.token(token, metadata);

		if (OAuth2RefreshToken.class.isAssignableFrom(token.getClass())) {
			tokens.token(this.getTokens().getAccessToken(), metadata);
			OAuth2AuthorizationCode authorizationCode = this.getTokens().getToken(OAuth2AuthorizationCode.class);
			if (authorizationCode != null && !this.getTokens().getTokenMetadata(authorizationCode).isInvalidated()) {
				tokens.token(authorizationCode, metadata);
			}
		}

		return from(this).set(OAuth2Authorization::setTokens, tokens).build();
	}

}
