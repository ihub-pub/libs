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
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;

import java.util.Collections;
import java.util.Map;

import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 用于OAuth 2.0客户端身份验证的Authentication实现。
 *
 * @author henry
 */
@Getter
public class OAuth2ClientAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	private String clientId;
	private String clientSecret;
	private ClientAuthenticationMethod clientAuthenticationMethod;
	private Map<String, Object> additionalParameters;
	private RegisteredClient registeredClient;

	public OAuth2ClientAuthenticationToken(String clientId, String clientSecret,
										   ClientAuthenticationMethod clientAuthenticationMethod,
										   @Nullable Map<String, Object> additionalParameters) {
		this(clientId, additionalParameters);
		Assert.hasText(clientSecret, "clientSecret cannot be empty");
		Assert.notNull(clientAuthenticationMethod, "clientAuthenticationMethod cannot be null");
		this.clientSecret = clientSecret;
		this.clientAuthenticationMethod = clientAuthenticationMethod;
	}

	public OAuth2ClientAuthenticationToken(String clientId,
										   @Nullable Map<String, Object> additionalParameters) {
		super(Collections.emptyList());
		Assert.hasText(clientId, "clientId cannot be empty");
		this.clientId = clientId;
		this.additionalParameters = additionalParameters != null ?
			Collections.unmodifiableMap(additionalParameters) : null;
		this.clientAuthenticationMethod = ClientAuthenticationMethod.NONE;
	}

	public OAuth2ClientAuthenticationToken(RegisteredClient registeredClient) {
		super(Collections.emptyList());
		Assert.notNull(registeredClient, "registeredClient cannot be null");
		this.registeredClient = registeredClient;
		setAuthenticated(true);
	}

	@Override
	public Object getPrincipal() {
		return registeredClient != null ? registeredClient.getClientId() : clientId;
	}

	@Override
	public Object getCredentials() {
		return clientSecret;
	}

}
