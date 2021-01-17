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
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.util.Assert;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.token.OAuth2Tokens;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * OAuth 2.0授权的表示形式，其中包含与resource owner授予client的授权相关的状态。
 *
 * @author henry
 */
@Getter
@EqualsAndHashCode
public class OAuth2Authorization implements Serializable {

	public static String STATE = OAuth2Authorization.class.getName().concat(".STATE");
	@Deprecated
	public static String CODE = OAuth2Authorization.class.getName().concat(".CODE");
	public static String AUTHORIZATION_REQUEST = OAuth2Authorization.class.getName().concat(".AUTHORIZATION_REQUEST");
	public static String AUTHORIZED_SCOPES = OAuth2Authorization.class.getName().concat(".AUTHORIZED_SCOPES");
	public static String ACCESS_TOKEN_ATTRIBUTES = OAuth2Authorization.class.getName().concat(".ACCESS_TOKEN_ATTRIBUTES");

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	private String registeredClientId;
	private String principalName;
	private OAuth2Tokens tokens;

	@Deprecated
	private OAuth2AccessToken accessToken;

	private Map<String, Object> attributes;

	protected OAuth2Authorization() {
	}

	@Deprecated
	public OAuth2AccessToken getAccessToken() {
		return getTokens().getAccessToken();
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		Assert.hasText(name, "name cannot be empty");
		return (T) this.attributes.get(name);
	}

	public static Builder withRegisteredClient(RegisteredClient registeredClient) {
		Assert.notNull(registeredClient, "registeredClient cannot be null");
		return new Builder(registeredClient.getId());
	}

	public static Builder from(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		return new Builder(authorization.getRegisteredClientId())
			.principalName(authorization.getPrincipalName())
			.tokens(OAuth2Tokens.from(authorization.getTokens()).build())
			.attributes(attrs -> attrs.putAll(authorization.getAttributes()));
	}

	public static class Builder implements Serializable {

		private static final long serialVersionUID = SERIAL_VERSION_UID;
		private String registeredClientId;
		private String principalName;
		private OAuth2Tokens tokens;

		@Deprecated
		private OAuth2AccessToken accessToken;

		private Map<String, Object> attributes = new HashMap<>();

		protected Builder(String registeredClientId) {
			this.registeredClientId = registeredClientId;
		}

		public Builder principalName(String principalName) {
			this.principalName = principalName;
			return this;
		}

		public Builder tokens(OAuth2Tokens tokens) {
			this.tokens = tokens;
			return this;
		}

		@Deprecated
		public Builder accessToken(OAuth2AccessToken accessToken) {
			this.accessToken = accessToken;
			return this;
		}

		public Builder attribute(String name, Object value) {
			Assert.hasText(name, "name cannot be empty");
			Assert.notNull(value, "value cannot be null");
			this.attributes.put(name, value);
			return this;
		}

		public Builder attributes(Consumer<Map<String, Object>> attributesConsumer) {
			attributesConsumer.accept(this.attributes);
			return this;
		}

		public OAuth2Authorization build() {
			Assert.hasText(this.principalName, "principalName cannot be empty");

			OAuth2Authorization authorization = new OAuth2Authorization();
			authorization.registeredClientId = this.registeredClientId;
			authorization.principalName = this.principalName;
			if (this.tokens == null) {
				OAuth2Tokens.Builder builder = OAuth2Tokens.builder();
				if (this.accessToken != null) {
					builder.accessToken(this.accessToken);
				}
				this.tokens = builder.build();
			}
			authorization.tokens = this.tokens;
			authorization.attributes = Collections.unmodifiableMap(this.attributes);
			return authorization;
		}
	}

}
