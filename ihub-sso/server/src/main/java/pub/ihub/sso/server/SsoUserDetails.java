/*
 * Copyright (c) 2022 Henry 李恒 (henry.box@outlook.com).
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
package pub.ihub.sso.server;

import java.io.Serializable;

/**
 * 用户核心信息
 *
 * @param <T> ID类型
 * @author liheng
 * @see org.springframework.security.core.userdetails.UserDetails
 */
public interface SsoUserDetails<T> extends Serializable {

	/**
	 * 用户登录ID
	 *
	 * @return 登录ID
	 */
	T getLoginId();

	/**
	 * 用户名/账号
	 *
	 * @return 用户名
	 */
	String getUsername();

	/**
	 * 用户密码
	 *
	 * @return 密码
	 */
	String getPassword();

	/**
	 * 账号是否过期
	 *
	 * @return 账号是否过期
	 */
	default boolean isAccountNonExpired() {
		return false;
	}

	/**
	 * 账号是否锁定
	 *
	 * @return 账号是否锁定
	 */
	default boolean isAccountNonLocked() {
		return false;
	}

	/**
	 * 用户凭证是否过期
	 *
	 * @return 用户凭证是否过期
	 */
	default boolean isCredentialsNonExpired() {
		return false;
	}

	/**
	 * 用户是否启用
	 *
	 * @return 用户是否启用
	 */
	default boolean isEnabled() {
		return true;
	}

}
