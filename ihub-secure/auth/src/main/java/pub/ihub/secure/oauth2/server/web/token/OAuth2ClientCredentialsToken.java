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
import org.springframework.security.core.Authentication;
import pub.ihub.secure.oauth2.server.web.OAuth2AuthToken;

import java.util.LinkedHashSet;
import java.util.Set;

import static cn.hutool.core.lang.Assert.notNull;
import static java.util.Collections.unmodifiableSet;
import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 客户端作用域授权
 *
 * @author henry
 */
@Getter
public class OAuth2ClientCredentialsToken extends OAuth2AuthToken {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	/**
	 * 作用域
	 */
	private final Set<String> scopes;

	public OAuth2ClientCredentialsToken(Authentication clientPrincipal, Set<String> scopes) {
		super(notNull(clientPrincipal, "授权主体不能为空！"));
		this.scopes = unmodifiableSet(new LinkedHashSet<>(notNull(scopes, "作用域不能为空！")));
	}

}
