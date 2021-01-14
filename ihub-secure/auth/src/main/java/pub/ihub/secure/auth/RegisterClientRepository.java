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

package pub.ihub.secure.auth;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 动态注册客户端存储库
 *
 * @author liheng
 */
public interface RegisterClientRepository extends RegisteredClientRepository {

	/**
	 * 注册注册客户端
	 *
	 * @param registeredClient 注册客户端
	 * @return 注册结果
	 */
	boolean register(@NotNull RegisteredClient registeredClient);

	/**
	 * 通过ID注销
	 *
	 * @param id ID
	 * @return 注销结果
	 */
	boolean logoutById(@NotBlank String id);

	/**
	 * 通过客户端ID注销
	 *
	 * @param clientId 客户端ID
	 * @return 注销结果
	 */
	boolean logoutByClientId(@NotBlank String clientId);

}
