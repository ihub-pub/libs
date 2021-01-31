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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * @author henry
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public final class TokenType implements Serializable {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	public static final TokenType ACCESS_TOKEN = new TokenType("access_token");
	public static final TokenType REFRESH_TOKEN = new TokenType("refresh_token");
	public static final TokenType AUTHORIZATION_CODE = new TokenType("authorization_code");

	private final String value;

	public static TokenType of(String tokenTypeHint) {
		if (isBlank(tokenTypeHint)) {
			return null;
		}
		if (REFRESH_TOKEN.getValue().equals(tokenTypeHint)) {
			return REFRESH_TOKEN;
		} else if (ACCESS_TOKEN.getValue().equals(tokenTypeHint)) {
			return ACCESS_TOKEN;
		}
		return null;
	}

}
