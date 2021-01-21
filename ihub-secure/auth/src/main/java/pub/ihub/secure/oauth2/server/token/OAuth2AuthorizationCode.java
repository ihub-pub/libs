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

package pub.ihub.secure.oauth2.server.token;

import org.springframework.security.oauth2.core.AbstractOAuth2Token;

import java.time.Instant;
import java.time.temporal.TemporalAmount;

/**
 * OAuth 2.0授权码
 *
 * @author henry
 */
public class OAuth2AuthorizationCode extends AbstractOAuth2Token {

	public OAuth2AuthorizationCode(String tokenValue, Instant issuedAt, Instant expiresAt) {
		super(tokenValue, issuedAt, expiresAt);
	}

	public static OAuth2AuthorizationCode generateAuthCode(String tokenValue, TemporalAmount amountToAdd) {
		Instant issuedAt = Instant.now();
		return new OAuth2AuthorizationCode(tokenValue, issuedAt, issuedAt.plus(amountToAdd));
	}

}
