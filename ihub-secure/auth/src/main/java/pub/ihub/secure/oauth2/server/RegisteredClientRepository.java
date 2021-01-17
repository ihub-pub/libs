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


import pub.ihub.secure.oauth2.server.client.RegisteredClient;

/**
 * OAuth 2.0 RegisteredClient存储库
 *
 * @author henry
 */
public interface RegisteredClientRepository {

	/**
	 * 获取注册客户端
	 *
	 * @param id ID
	 * @return 客户端
	 */
	RegisteredClient findById(String id);

	/**
	 * 获取注册客户端
	 *
	 * @param clientId 客户端ID
	 * @return 客户端
	 */
	RegisteredClient findByClientId(String clientId);

}
