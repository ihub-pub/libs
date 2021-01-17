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
 * 供应程序配置
 *
 * @author henry
 */
public class ProviderSettings extends Settings {

	private static final String PROVIDER_SETTING_BASE = "setting.provider.";
	public static final String ISSUER = PROVIDER_SETTING_BASE.concat("issuer");
	public static final String AUTHORIZATION_ENDPOINT = PROVIDER_SETTING_BASE.concat("authorization-endpoint");
	public static final String TOKEN_ENDPOINT = PROVIDER_SETTING_BASE.concat("token-endpoint");
	public static final String JWK_SET_ENDPOINT = PROVIDER_SETTING_BASE.concat("jwk-set-endpoint");
	public static final String TOKEN_REVOCATION_ENDPOINT = PROVIDER_SETTING_BASE.concat("token-revocation-endpoint");

	public ProviderSettings() {
		this(defaultSettings());
	}

	public ProviderSettings(Map<String, Object> settings) {
		super(settings);
	}

	public String issuer() {
		return setting(ISSUER);
	}

	public ProviderSettings issuer(String issuer) {
		return setting(ISSUER, issuer);
	}

	public String authorizationEndpoint() {
		return setting(AUTHORIZATION_ENDPOINT);
	}

	public ProviderSettings authorizationEndpoint(String authorizationEndpoint) {
		return setting(AUTHORIZATION_ENDPOINT, authorizationEndpoint);
	}

	public String tokenEndpoint() {
		return setting(TOKEN_ENDPOINT);
	}

	public ProviderSettings tokenEndpoint(String tokenEndpoint) {
		return setting(TOKEN_ENDPOINT, tokenEndpoint);
	}

	public String jwkSetEndpoint() {
		return setting(JWK_SET_ENDPOINT);
	}

	public ProviderSettings jwkSetEndpoint(String jwkSetEndpoint) {
		return setting(JWK_SET_ENDPOINT, jwkSetEndpoint);
	}

	public String tokenRevocationEndpoint() {
		return setting(TOKEN_REVOCATION_ENDPOINT);
	}

	public ProviderSettings tokenRevocationEndpoint(String tokenRevocationEndpoint) {
		return setting(TOKEN_REVOCATION_ENDPOINT, tokenRevocationEndpoint);
	}

	protected static Map<String, Object> defaultSettings() {
		Map<String, Object> settings = new HashMap<>();
		settings.put(AUTHORIZATION_ENDPOINT, "/oauth2/authorize");
		settings.put(TOKEN_ENDPOINT, "/oauth2/token");
		settings.put(JWK_SET_ENDPOINT, "/oauth2/jwks");
		settings.put(TOKEN_REVOCATION_ENDPOINT, "/oauth2/revoke");
		return settings;
	}

}
