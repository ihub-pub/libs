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

package pub.ihub.secure.oauth2.server.web.token;

import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.util.Assert;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;

import java.util.Collections;
import java.util.Map;

import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 颁发OAuth 2.0访问令牌和（可选）刷新令牌时使用的Authentication实现。
 *
 * @author henry
 */
@Getter
public class OAuth2AccessTokenAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	private final RegisteredClient registeredClient;
	private final Authentication clientPrincipal;
	private final OAuth2AccessToken accessToken;
	@Nullable
	private final OAuth2RefreshToken refreshToken;
	private final Map<String, Object> additionalParameters;

	public OAuth2AccessTokenAuthenticationToken(RegisteredClient registeredClient,
												Authentication clientPrincipal, OAuth2AccessToken accessToken) {
		this(registeredClient, clientPrincipal, accessToken, null);
	}

	public OAuth2AccessTokenAuthenticationToken(RegisteredClient registeredClient, Authentication clientPrincipal,
												OAuth2AccessToken accessToken, @Nullable OAuth2RefreshToken refreshToken) {
		this(registeredClient, clientPrincipal, accessToken, refreshToken, Collections.emptyMap());
	}

	public OAuth2AccessTokenAuthenticationToken(RegisteredClient registeredClient, Authentication clientPrincipal,
												OAuth2AccessToken accessToken, @Nullable OAuth2RefreshToken refreshToken, Map<String, Object> additionalParameters) {
		super(Collections.emptyList());
		Assert.notNull(registeredClient, "registeredClient cannot be null");
		Assert.notNull(clientPrincipal, "clientPrincipal cannot be null");
		Assert.notNull(accessToken, "accessToken cannot be null");
		Assert.notNull(additionalParameters, "additionalParameters cannot be null");
		this.registeredClient = registeredClient;
		this.clientPrincipal = clientPrincipal;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.additionalParameters = additionalParameters;
	}

	@Override
	public Object getPrincipal() {
		return clientPrincipal;
	}

	@Override
	public Object getCredentials() {
		return "";
	}

}
