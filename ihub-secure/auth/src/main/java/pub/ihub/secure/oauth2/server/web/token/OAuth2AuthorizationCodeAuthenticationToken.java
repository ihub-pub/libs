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
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 用于OAuth 2.0授权代码授予的Authentication实现。
 *
 * @author henry
 */
@Getter
public class OAuth2AuthorizationCodeAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	private final String code;
	private final Authentication clientPrincipal;
	private final String redirectUri;
	private final Map<String, Object> additionalParameters;

	public OAuth2AuthorizationCodeAuthenticationToken(String code, Authentication clientPrincipal,
													  @Nullable String redirectUri, @Nullable Map<String, Object> additionalParameters) {
		super(Collections.emptyList());
		Assert.hasText(code, "code cannot be empty");
		Assert.notNull(clientPrincipal, "clientPrincipal cannot be null");
		this.code = code;
		this.clientPrincipal = clientPrincipal;
		this.redirectUri = redirectUri;
		this.additionalParameters = Collections.unmodifiableMap(
			additionalParameters != null ?
				additionalParameters :
				Collections.emptyMap());
	}

	@Override
	public Object getPrincipal() {
		return this.clientPrincipal;
	}

	@Override
	public Object getCredentials() {
		return "";
	}

}
