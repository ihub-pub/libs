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

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import pub.ihub.secure.auth.RegisterClientRepository;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 动态注册客户端存储库（内存中）
 *
 * @author liheng
 */
@Slf4j
@Validated
public class DynamicRegisteredClientRepository implements RegisterClientRepository {

	private final Map<String, RegisteredClient> idRegistrationMap = new HashMap<>();
	private final Map<String, RegisteredClient> clientIdRegistrationMap = new HashMap<>();

	@Override
	public RegisteredClient findById(String id) {
		return this.idRegistrationMap.get(id);
	}

	@Override
	public RegisteredClient findByClientId(String clientId) {
		return this.clientIdRegistrationMap.get(clientId);
	}

	@Override
	public boolean register(RegisteredClient registeredClient) {
		String id = registeredClient.getId();
		if (idRegistrationMap.containsKey(id)) {
			log.error("Registered client must be unique. Found duplicate identifier: {}", id);
			return false;
		}
		String clientId = registeredClient.getClientId();
		if (idRegistrationMap.containsKey(id)) {
			log.error("Registered client must be unique. Found duplicate client identifier: {}", clientId);
			return false;
		}
		idRegistrationMap.put(id, registeredClient);
		clientIdRegistrationMap.put(clientId, registeredClient);
		return true;
	}

	@Override
	public boolean logoutById(String id) {
		return null != idRegistrationMap.remove(id);
	}

	@Override
	public boolean logoutByClientId(String clientId) {
		return null != clientIdRegistrationMap.remove(clientId);
	}

}
