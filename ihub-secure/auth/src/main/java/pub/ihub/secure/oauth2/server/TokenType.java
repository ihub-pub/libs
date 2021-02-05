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

package pub.ihub.secure.oauth2.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import pub.ihub.secure.oauth2.server.token.OAuth2AuthorizationCode;
import pub.ihub.secure.oauth2.server.token.OAuth2RefreshToken2;

import java.util.Arrays;

/**
 * 令牌类型
 *
 * @author liheng
 */
@AllArgsConstructor
@Getter
public enum TokenType {

	/**
	 * 访问令牌
	 */
	ACCESS_TOKEN("access_token", OAuth2AccessToken.class),
	/**
	 * 刷新令牌
	 */
	REFRESH_TOKEN("refresh_token", OAuth2RefreshToken.class),
	REFRESH_TOKEN_2("refresh_token", OAuth2RefreshToken2.class),
	/**
	 * 授权码
	 */
	AUTHORIZATION_CODE("authorization_code", OAuth2AuthorizationCode.class),
	/**
	 * OpenID令牌
	 */
	OIDC_ID_TOKEN("oidc_id_token", OidcIdToken.class);

	private final String value;
	private final Class<? extends AbstractOAuth2Token> tokenType;

	public static <T extends AbstractOAuth2Token> TokenType of(T token) {
		return Arrays.stream(values())
			.filter(t -> t.tokenType.isAssignableFrom(token.getClass())).findFirst().orElse(null);
	}

}
