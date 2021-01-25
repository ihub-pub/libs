/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.oauth2.core;

import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * TODO
 * This class is temporary and will be removed after upgrading to Spring Security 5.5.0 GA.
 *
 * @author Joe Grandja
 * @see <a target="_blank" href="https://github.com/spring-projects/spring-security/pull/9146">Issue gh-9146</a>
 * @since 0.0.3
 */
public class OAuth2RefreshToken2 extends OAuth2RefreshToken {

	private static final StringKeyGenerator TOKEN_GENERATOR = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);

	private final Instant expiresAt;

	public OAuth2RefreshToken2(String tokenValue, Instant issuedAt, Instant expiresAt) {
		super(tokenValue, issuedAt);
		this.expiresAt = expiresAt;
	}

	@Override
	public Instant getExpiresAt() {
		return this.expiresAt;
	}

	public static OAuth2RefreshToken issueRefreshToken(Duration tokenTimeToLive) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(tokenTimeToLive);
		return new OAuth2RefreshToken2(TOKEN_GENERATOR.generateKey(), issuedAt, expiresAt);
	}

}
