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

package pub.ihub.secure.core;

import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * 授权方式
 *
 * @author liheng
 */
@AllArgsConstructor
public enum GrantType {

	/**
	 * 授权码
	 */
	AUTHORIZATION_CODE(AuthorizationGrantType.AUTHORIZATION_CODE),
	/**
	 * 密码
	 */
	PASSWORD(AuthorizationGrantType.PASSWORD),
	/**
	 * 刷新令牌
	 */
	REFRESH_TOKEN(AuthorizationGrantType.REFRESH_TOKEN),
	/**
	 * 客户端凭证
	 */
	CLIENT_CREDENTIALS(AuthorizationGrantType.CLIENT_CREDENTIALS),
	/**
	 * 隐式
	 */
	IMPLICIT(AuthorizationGrantType.IMPLICIT);

	/**
	 * 授权授予类型
	 */
	private final AuthorizationGrantType grantType;

	public String getValue() {
		return grantType.getValue();
	}

}
