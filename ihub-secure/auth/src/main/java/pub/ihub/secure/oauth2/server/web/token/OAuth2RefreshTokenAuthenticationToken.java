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
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Set;

import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 用于OAuth 2.0刷新令牌授予的Authentication实现。
 *
 * @author henry
 */
@Getter
public class OAuth2RefreshTokenAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	private final String refreshToken;
	private final Authentication clientPrincipal;
	private final Set<String> scopes;

	public OAuth2RefreshTokenAuthenticationToken(String refreshToken, Authentication clientPrincipal,
												 Set<String> scopes) {
		super(Collections.emptySet());
		Assert.hasText(refreshToken, "refreshToken cannot be empty");
		Assert.notNull(clientPrincipal, "clientPrincipal cannot be null");
		Assert.notNull(scopes, "scopes cannot be null");
		this.refreshToken = refreshToken;
		this.clientPrincipal = clientPrincipal;
		this.scopes = scopes;
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
