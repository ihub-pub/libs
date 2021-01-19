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
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.util.Assert;

import java.util.Collections;

import static cn.hutool.core.lang.Assert.notBlank;
import static cn.hutool.core.lang.Assert.notNull;
import static java.util.Collections.emptyList;
import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 撤销令牌
 *
 * @author henry
 */
@Getter
public class OAuth2TokenRevocationAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	private final String token;
	private final Authentication clientPrincipal;
	private final String tokenTypeHint;

	public OAuth2TokenRevocationAuthenticationToken(String token,
													Authentication clientPrincipal,
													@Nullable String tokenTypeHint) {
		super(emptyList());
		this.token = notBlank(token, "令牌不能为空！");
		this.clientPrincipal = notNull(clientPrincipal, "授权主体不能为空！");
		this.tokenTypeHint = tokenTypeHint;
	}

	public OAuth2TokenRevocationAuthenticationToken(AbstractOAuth2Token revokedToken,
													Authentication clientPrincipal) {
		super(emptyList());
		this.token = notNull(revokedToken, "令牌不能为空！").getTokenValue();
		this.clientPrincipal = notNull(clientPrincipal, "授权主体不能为空！");
		this.tokenTypeHint = null;
		setAuthenticated(true);
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
