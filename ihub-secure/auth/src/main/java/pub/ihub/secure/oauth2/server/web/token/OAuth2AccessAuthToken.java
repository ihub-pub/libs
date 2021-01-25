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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.web.OAuth2AuthToken;

import java.util.Collections;
import java.util.Map;

import static cn.hutool.core.lang.Assert.notNull;
import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 访问和刷新（可选）授权
 *
 * @author henry
 */
@Getter
public class OAuth2AccessAuthToken extends OAuth2AuthToken {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	/**
	 * 注册客户端
	 */
	private final RegisteredClient registeredClient;
	/**
	 * 访问令牌
	 */
	private final OAuth2AccessToken accessToken;
	/**
	 * 刷新令牌
	 */
	private final OAuth2RefreshToken refreshToken;
	/**
	 * 附加参数
	 */
	private final Map<String, Object> additionalParameters;

	public OAuth2AccessAuthToken(RegisteredClient registeredClient,
								 Authentication clientPrincipal, OAuth2AccessToken accessToken) {
		this(registeredClient, clientPrincipal, accessToken, null);
	}

	public OAuth2AccessAuthToken(RegisteredClient registeredClient, Authentication clientPrincipal,
								 OAuth2AccessToken accessToken, @Nullable OAuth2RefreshToken refreshToken) {
		this(registeredClient, clientPrincipal, accessToken, refreshToken, Collections.emptyMap());
	}

	public OAuth2AccessAuthToken(RegisteredClient registeredClient, Authentication clientPrincipal,
								 OAuth2AccessToken accessToken, @Nullable OAuth2RefreshToken refreshToken,
								 Map<String, Object> additionalParameters) {
		super(notNull(clientPrincipal, "授权主体不能为空！"));
		this.registeredClient = notNull(registeredClient, "注册客户端不能为空！");
		this.accessToken = notNull(accessToken, "访问令牌不能为空！");
		this.refreshToken = refreshToken;
		this.additionalParameters = notNull(additionalParameters, "附加参数不能为空！");
	}

}
