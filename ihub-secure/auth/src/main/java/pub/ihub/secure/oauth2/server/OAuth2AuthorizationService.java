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

import org.springframework.lang.Nullable;

/**
 * OAuth2授权服务
 *
 * @author henry
 */
public interface OAuth2AuthorizationService {

	/**
	 * 保存授权
	 *
	 * @param authorization 授权
	 */
	void save(OAuth2Authorization authorization);

	/**
	 * 移除授权
	 *
	 * @param authorization 授权
	 */
	void remove(OAuth2Authorization authorization);

	/**
	 * 查找授权
	 *
	 * @param token     令牌
	 * @param tokenType 令牌类型
	 * @return 授权
	 */
	OAuth2Authorization findByToken(String token, @Nullable TokenType tokenType);

}
