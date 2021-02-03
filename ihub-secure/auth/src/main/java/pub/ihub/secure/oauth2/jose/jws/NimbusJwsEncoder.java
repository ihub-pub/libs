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

package pub.ihub.secure.oauth2.jose.jws;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import pub.ihub.secure.crypto.CryptoKey;
import pub.ihub.secure.crypto.CryptoKeySource;
import pub.ihub.secure.oauth2.jose.JoseHeader;
import pub.ihub.secure.oauth2.jwt.JwtClaimsSet;
import pub.ihub.secure.oauth2.jwt.JwtEncoder;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.lang.Assert.notBlank;
import static cn.hutool.core.lang.Assert.notNull;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
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
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfiguration.ISSUER_URI;
import static pub.ihub.secure.oauth2.jose.JoseHeader.withAlgorithm;

/**
 * Jws编码器
 * 该实现使用JSON Web签名（JWS）紧凑序列化格式对JSON Web令牌（JWT）进行编码
 *
 * @author henry
 */
@RequiredArgsConstructor
public class NimbusJwsEncoder implements JwtEncoder {

	private final CryptoKeySource keySource;

	@Override
	public Jwt encode(JoseHeader headers, JwtClaimsSet claims) throws JwtException {
		String keyAlgorithm = notBlank(headers.getJwsAlgorithmValue(), "密钥算法不能为空！");
		CryptoKey<?> cryptoKey = notNull(keySource.get(keyAlgorithm), "不支持密钥算法'%s'",
			headers.getJwsAlgorithm().getName());

		SignedJWT signedJwt = new SignedJWT(headers.buildJwsHeader(), claims.buildJwtClaimsSet());
		try {
			signedJwt.sign(cryptoKey.getJwsSigner());
		} catch (JOSEException ex) {
			throw new JwtException(String.format("Jwt编码错误：%s", ex.getMessage()), ex);
		}

		return new Jwt(signedJwt.serialize(), claims.getIssuedAt(), claims.getExpiresAt(),
			headers.getHeaders(), claims.getClaims());
	}

	@Override
	public Jwt issueJwtAccessToken(String subject, RegisteredClient client, Set<String> scopes) throws JwtException {
		Instant issuedAt = Instant.now();
		return encode(withAlgorithm(RS256), new JwtClaimsSet(new HashMap<>(7) {
			{
				put(ISS, ISSUER_URI);
				put(SUB, subject);
				put(AUD, singletonList(client.getClientId()));
				put(IAT, issuedAt);
				put(EXP, issuedAt.plus(client.getAccessTokenTimeToLive()));
				put(NBF, issuedAt);
				put(SCOPE, scopes);
			}
		}));
	}

	@Override
	public Jwt issueIdToken(String subject, RegisteredClient client, String nonce) throws JwtException {
		Instant issuedAt = Instant.now();
		Map<String, Object> claims = new HashMap<>(7) {
			{
				put(ISS, ISSUER_URI);
				put(SUB, subject);
				put(AUD, singletonList(client.getClientId()));
				put(IAT, issuedAt);
				put(EXP, issuedAt.plus(client.getIdTokenTimeToLive()));
				put(AZP, client.getClientId());
			}
		};
		if (isNotBlank(nonce)) {
			claims.put(NONCE, nonce);
		}
		// TODO Add 'auth_time' claim
		return encode(withAlgorithm(RS256), new JwtClaimsSet(claims));
	}

}
