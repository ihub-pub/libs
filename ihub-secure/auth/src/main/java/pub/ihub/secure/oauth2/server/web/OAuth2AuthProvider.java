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

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.function.Supplier;

import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_CLIENT;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_GRANT;

/**
 * 认证处理器
 *
 * @author liheng
 */
public abstract class OAuth2AuthProvider<T extends Authentication> extends TypeReference<T>
	implements AuthenticationProvider {

	@SuppressWarnings("unchecked")
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		return auth((T) authentication);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return TypeUtil.getClass(getType()).isAssignableFrom(authentication);
	}

	protected static Supplier<OAuth2AuthenticationException> exceptionSupplier(String errorCode) {
		return () -> new OAuth2AuthenticationException(new OAuth2Error(errorCode));
	}

	protected static Supplier<OAuth2AuthenticationException> invalidClientException() {
		return exceptionSupplier(INVALID_CLIENT);
	}

	protected static Supplier<OAuth2AuthenticationException> invalidGrantException() {
		return exceptionSupplier(INVALID_GRANT);
	}

	/**
	 * 执行认证方法
	 *
	 * @param authentication 授权令牌
	 * @return 令牌
	 * @throws AuthenticationException 授权失败异常
	 */
	public abstract Authentication auth(T authentication) throws AuthenticationException;

}
