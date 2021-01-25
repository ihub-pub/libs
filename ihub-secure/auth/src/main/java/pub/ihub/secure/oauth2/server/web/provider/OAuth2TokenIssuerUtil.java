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

package pub.ihub.secure.oauth2.server.web.provider;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import pub.ihub.secure.oauth2.jose.JoseHeader;
import pub.ihub.secure.oauth2.jwt.JwtClaimsSet;
import pub.ihub.secure.oauth2.jwt.JwtEncoder;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.SCOPE;
import static org.springframework.security.oauth2.core.oidc.IdTokenClaimNames.AZP;
import static org.springframework.security.oauth2.core.oidc.IdTokenClaimNames.NONCE;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.AUD;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.EXP;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.IAT;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.ISS;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.NBF;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.SUB;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.ISSUER_URI;
import static pub.ihub.secure.oauth2.jose.JoseHeader.withAlgorithm;

/**
 * @author henry
 */
final class OAuth2TokenIssuerUtil {

	static Jwt issueJwtAccessToken(JwtEncoder jwtEncoder, String subject, String audience, Set<String> scopes, Duration tokenTimeToLive) {
		JoseHeader joseHeader = withAlgorithm(RS256);

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(tokenTimeToLive);

		return jwtEncoder.encode(joseHeader, new JwtClaimsSet(new HashMap<>(7) {
			{
				put(ISS, ISSUER_URI);
				put(SUB, subject);
				put(AUD, singletonList(audience));
				put(IAT, issuedAt);
				put(EXP, expiresAt);
				put(NBF, issuedAt);
				put(SCOPE, scopes);
			}
		}));
	}

	static Jwt issueIdToken(JwtEncoder jwtEncoder, String subject, String audience, String nonce) {
		JoseHeader joseHeader = withAlgorithm(RS256);

		Instant issuedAt = Instant.now();
		// TODO Allow configuration for id token time-to-live
		Instant expiresAt = issuedAt.plus(30, ChronoUnit.MINUTES);

		Map<String, Object> claims = new HashMap<>(7) {
			{
				put(ISS, ISSUER_URI);
				put(SUB, subject);
				put(AUD, singletonList(audience));
				put(IAT, issuedAt);
				put(EXP, expiresAt);
				put(AZP, audience);
			}
		};
		if (StringUtils.hasText(nonce)) {
			claims.put(NONCE, nonce);
		}

		// TODO Add 'auth_time' claim

		return jwtEncoder.encode(joseHeader, new JwtClaimsSet(claims));
	}

}
