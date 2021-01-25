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
import org.springframework.util.Assert;
import pub.ihub.secure.oauth2.server.web.OAuth2AuthToken;

import java.util.Map;

import static cn.hutool.core.lang.Assert.notBlank;
import static cn.hutool.core.lang.Assert.notNull;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 授权码授权
 *
 * @author henry
 */
@Getter
public class OAuth2AuthCodeToken extends OAuth2AuthToken {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	/**
	 * 授权码
	 */
	private final String code;
	/**
	 * 重定向uri
	 */
	private final String redirectUri;
	/**
	 * 附加参数
	 */
	private final Map<String, Object> additionalParameters;

	public OAuth2AuthCodeToken(String code, Authentication clientPrincipal,
							   @Nullable String redirectUri,
							   @Nullable Map<String, Object> additionalParameters) {
		super(notNull(clientPrincipal, "授权主体不能为空！"));
		this.code = notBlank(code, "授权码不能为空！");
		this.redirectUri = redirectUri;
		this.additionalParameters = unmodifiableMap(additionalParameters != null ? additionalParameters : emptyMap());
	}

}
