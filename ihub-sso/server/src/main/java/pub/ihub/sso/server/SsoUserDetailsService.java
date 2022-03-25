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

/**
 * 加载用户核心信息接口
 *
 * @param <T> ID类型
 * @author liheng
 * @see org.springframework.security.core.userdetails.UserDetailsService
 */
public interface SsoUserDetailsService<T> {


	/**
	 * 通过用户名加载用户信息
	 *
	 * @param username 用户名
	 * @return 用户信息
	 */
	SsoUserDetails<T> loadUserByUsername(String username);

	/**
	 * 加密密码
	 *
	 * @param password 密码
	 * @return 加密密码
	 */
	default String encryptPassword(String password) {
		return password;
	}

}
