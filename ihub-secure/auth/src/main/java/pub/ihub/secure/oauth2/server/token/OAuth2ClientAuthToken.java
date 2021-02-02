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

package pub.ihub.secure.oauth2.server.token;

import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;

import java.util.Map;

import static cn.hutool.core.lang.Assert.notBlank;
import static cn.hutool.core.lang.Assert.notNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.NONE;
import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 客户端授权令牌
 *
 * @author henry
 */
@Getter
public class OAuth2ClientAuthToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	/**
	 * 客户端ID
	 */
	private String clientId;
	/**
	 * 客户端密钥
	 */
	private String clientSecret;
	/**
	 * 客户端授权方法
	 */
	private ClientAuthenticationMethod clientAuthenticationMethod;
	/**
	 * 附加参数
	 */
	private Map<String, Object> additionalParameters;
	/**
	 * 注册客户端
	 */
	private RegisteredClient registeredClient;

	public OAuth2ClientAuthToken(String clientId, String clientSecret,
								 ClientAuthenticationMethod clientAuthenticationMethod,
								 @Nullable Map<String, Object> additionalParameters) {
		this(clientId, additionalParameters);
		this.clientSecret = notNull(clientSecret, "客户端密钥不能为空！");
		this.clientAuthenticationMethod = notNull(clientAuthenticationMethod, "客户端授权方法不能为空！");
	}

	public OAuth2ClientAuthToken(String clientId,
								 @Nullable Map<String, Object> additionalParameters) {
		super(emptyList());
		this.clientId = notBlank(clientId, "客户端ID不能为空！");
		this.additionalParameters = additionalParameters != null ? unmodifiableMap(additionalParameters) : null;
		this.clientAuthenticationMethod = NONE;
	}

	public OAuth2ClientAuthToken(RegisteredClient registeredClient) {
		super(emptyList());
		this.registeredClient = notNull(registeredClient, "注册客户端不能为空！");
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
