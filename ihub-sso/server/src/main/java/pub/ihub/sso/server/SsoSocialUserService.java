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

import me.zhyd.oauth.model.AuthUser;

/**
 * 第三方登录接口
 *
 * @param <T> ID类型
 * @author liheng
 */
public interface SsoSocialUserService<T> {

	/**
	 * 通过第三方UUID查找用户信息
	 *
	 * @param source 第三方渠道
	 * @param uuid   UUID
	 * @return 用户信息
	 */
	SsoUserDetails<T> findUserByUuid(String source, String uuid);

	/**
	 * 通过第三方授权信息创建用户
	 *
	 * @param source   第三方渠道
	 * @param authUser 授权信息
	 * @return 创建用户
	 */
	SsoUserDetails<T> createUserByAuth(String source, AuthUser authUser);

	/**
	 * 绑定第三方用户
	 *
	 * @param source   第三方渠道
	 * @param loginId  登录ID
	 * @param authUser 授权信息
	 */
	void bingUserAndAuth(String source, T loginId, AuthUser authUser);

}
