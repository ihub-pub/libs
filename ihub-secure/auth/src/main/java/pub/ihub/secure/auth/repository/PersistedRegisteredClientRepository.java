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

package pub.ihub.secure.auth.repository;

import org.springframework.validation.annotation.Validated;
import pub.ihub.secure.auth.RegisterClientRepository;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;

/**
 * 动态注册客户端存储库（持久化）
 * TODO 是否需要缓存？
 *
 * @author liheng
 */
@Validated
public class PersistedRegisteredClientRepository implements RegisterClientRepository {

	@Override
	public RegisteredClient findById(String id) {
		return null;
	}

	@Override
	public RegisteredClient findByClientId(String clientId) {
		return null;
	}

	@Override
	public boolean register(RegisteredClient registeredClient) {
		return false;
	}

	@Override
	public boolean logoutById(String id) {
		return false;
	}

	@Override
	public boolean logoutByClientId(String clientId) {
		return false;
	}

}
