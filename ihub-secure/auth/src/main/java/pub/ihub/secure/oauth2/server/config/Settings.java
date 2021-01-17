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

package pub.ihub.secure.oauth2.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 配置工具类
 *
 * @author henry
 */
@RequiredArgsConstructor
public class Settings implements Serializable {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	private final Map<String, Object> settings;

	@SuppressWarnings("unchecked")
	public <T> T setting(String name) {
		Assert.hasText(name, "name cannot be empty");
		return (T) this.settings.get(name);
	}

	@SuppressWarnings("unchecked")
	public <T extends Settings> T setting(String name, Object value) {
		Assert.hasText(name, "name cannot be empty");
		Assert.notNull(value, "value cannot be null");
		this.settings.put(name, value);
		return (T) this;
	}

	public Map<String, Object> settings() {
		return settings;
	}

	@SuppressWarnings("unchecked")
	public <T extends Settings> T settings(Consumer<Map<String, Object>> settingsConsumer) {
		settingsConsumer.accept(settings);
		return (T) this;
	}

}
