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

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端设置
 *
 * @author henry
 */
public class ClientSettings extends Settings {

	private static final String CLIENT_SETTING_BASE = "setting.client.";
	public static final String REQUIRE_PROOF_KEY = CLIENT_SETTING_BASE.concat("require-proof-key");
	public static final String REQUIRE_USER_CONSENT = CLIENT_SETTING_BASE.concat("require-user-consent");

	public ClientSettings() {
		this(defaultSettings());
	}

	public ClientSettings(Map<String, Object> settings) {
		super(settings);
	}

	public boolean requireProofKey() {
		return setting(REQUIRE_PROOF_KEY);
	}

	public ClientSettings requireProofKey(boolean requireProofKey) {
		setting(REQUIRE_PROOF_KEY, requireProofKey);
		return this;
	}

	public boolean requireUserConsent() {
		return setting(REQUIRE_USER_CONSENT);
	}

	public ClientSettings requireUserConsent(boolean requireUserConsent) {
		setting(REQUIRE_USER_CONSENT, requireUserConsent);
		return this;
	}

	protected static Map<String, Object> defaultSettings() {
		Map<String, Object> settings = new HashMap<>();
		settings.put(REQUIRE_PROOF_KEY, false);
		settings.put(REQUIRE_USER_CONSENT, false);
		return settings;
	}

}
