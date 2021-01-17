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

import org.springframework.util.Assert;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 令牌配置
 *
 * @author henry
 */
public class TokenSettings extends Settings {

	private static final String TOKEN_SETTING_BASE = "setting.token.";
	public static final String ACCESS_TOKEN_TIME_TO_LIVE = TOKEN_SETTING_BASE.concat("access-token-time-to-live");
	public static final String REUSE_REFRESH_TOKENS = TOKEN_SETTING_BASE.concat("reuse-refresh-tokens");
	public static final String REFRESH_TOKEN_TIME_TO_LIVE = TOKEN_SETTING_BASE.concat("refresh-token-time-to-live");

	public TokenSettings() {
		this(defaultSettings());
	}

	public TokenSettings(Map<String, Object> settings) {
		super(settings);
	}

	public Duration accessTokenTimeToLive() {
		return setting(ACCESS_TOKEN_TIME_TO_LIVE);
	}

	public TokenSettings accessTokenTimeToLive(Duration accessTokenTimeToLive) {
		Assert.notNull(accessTokenTimeToLive, "accessTokenTimeToLive cannot be null");
		Assert.isTrue(accessTokenTimeToLive.getSeconds() > 0, "accessTokenTimeToLive must be greater than Duration.ZERO");
		setting(ACCESS_TOKEN_TIME_TO_LIVE, accessTokenTimeToLive);
		return this;
	}

	public boolean reuseRefreshTokens() {
		return setting(REUSE_REFRESH_TOKENS);
	}

	public TokenSettings reuseRefreshTokens(boolean reuseRefreshTokens) {
		setting(REUSE_REFRESH_TOKENS, reuseRefreshTokens);
		return this;
	}

	public Duration refreshTokenTimeToLive() {
		return setting(REFRESH_TOKEN_TIME_TO_LIVE);
	}

	public TokenSettings refreshTokenTimeToLive(Duration refreshTokenTimeToLive) {
		Assert.notNull(refreshTokenTimeToLive, "refreshTokenTimeToLive cannot be null");
		Assert.isTrue(refreshTokenTimeToLive.getSeconds() > 0, "refreshTokenTimeToLive must be greater than Duration.ZERO");
		setting(REFRESH_TOKEN_TIME_TO_LIVE, refreshTokenTimeToLive);
		return this;
	}

	protected static Map<String, Object> defaultSettings() {
		Map<String, Object> settings = new HashMap<>();
		settings.put(ACCESS_TOKEN_TIME_TO_LIVE, Duration.ofMinutes(5));
		settings.put(REUSE_REFRESH_TOKENS, true);
		settings.put(REFRESH_TOKEN_TIME_TO_LIVE, Duration.ofMinutes(60));
		return settings;
	}

}
