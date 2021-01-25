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

package pub.ihub.secure.oauth2.server.web;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthToken;

import static java.util.Collections.emptyList;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_CLIENT;

/**
 * 授权令牌
 *
 * @author liheng
 */
public abstract class OAuth2AuthToken extends AbstractAuthenticationToken {

	/**
	 * 授权客户端
	 */
	private final Authentication clientPrincipal;

	public OAuth2AuthToken(Authentication clientPrincipal) {
		super(emptyList());
		this.clientPrincipal = clientPrincipal;
	}

	public OAuth2AuthToken() {
		super(emptyList());
		this.clientPrincipal = null;
	}

	@Override
	public Object getPrincipal() {
		return clientPrincipal;
	}

	@Override
	public Object getCredentials() {
		return "";
	}

	public OAuth2ClientAuthToken getAuthenticatedClient() {
		if (clientPrincipal != null && clientPrincipal.isAuthenticated() &&
			OAuth2ClientAuthToken.class.isAssignableFrom(this.clientPrincipal.getClass())) {
			return (OAuth2ClientAuthToken) clientPrincipal;
		}
		throw new OAuth2AuthenticationException(new OAuth2Error(INVALID_CLIENT));
	}


}
