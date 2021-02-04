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

package pub.ihub.secure.oauth2.jwt;

import cn.hutool.core.lang.UUID;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.produce.JWSSignerFactory;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static cn.hutool.core.lang.Assert.isTrue;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static com.nimbusds.jose.JOSEObjectType.JWT;
import static java.util.Collections.singletonList;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.SCOPE;
import static org.springframework.security.oauth2.core.oidc.IdTokenClaimNames.AZP;
import static org.springframework.security.oauth2.core.oidc.IdTokenClaimNames.NONCE;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.AUD;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.EXP;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.IAT;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.ISS;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.JTI;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.NBF;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.SUB;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfiguration.ISSUER_URI;
import static pub.ihub.secure.oauth2.jwt.JoseHeader.KID;
import static pub.ihub.secure.oauth2.jwt.JoseHeader.TYP;
import static pub.ihub.secure.oauth2.jwt.JoseHeader.withAlgorithm;

/**
 * Jws编码器
 * 该实现使用JSON Web签名（JWS）紧凑序列化格式对JSON Web令牌（JWT）进行编码
 *
 * @author henry
 */
@RequiredArgsConstructor
public class NimbusJwsEncoder implements JwtEncoder {

	private static final JWSSignerFactory JWS_SIGNER_FACTORY = new DefaultJWSSignerFactory();
	private final JWKSource<SecurityContext> jwkSource;
	private final Map<JWK, JWSSigner> jwsSigners = new ConcurrentHashMap<>();

	@Override
	public Jwt encode(JoseHeader headers, JwtClaimsSet claims) throws JwtException {
		final AtomicReference<JWSAlgorithm> jwsAlgorithm = new AtomicReference<>();
		jwsAlgorithm.set(JWSAlgorithm.parse(headers.getJwsAlgorithm().getName()));
		JWSHeader jwsHeader = new JWSHeader(jwsAlgorithm.get());
		JWKSelector jwkSelector = new JWKSelector(JWKMatcher.forJWSHeader(jwsHeader));

		List<JWK> jwks;
		try {
			jwks = this.jwkSource.get(jwkSelector, null);
		} catch (KeySourceException ex) {
			throw new JwtException(String.format("不支持密钥算法：%s", ex.getMessage()), ex);
		}
		isTrue(!jwks.isEmpty(), "不支持密钥算法'{}'", jwsAlgorithm.get().getName());
		isTrue(jwks.size() == 1, "匹配到多个'{}'密钥算法", jwsAlgorithm.get().getName());
		JWK jwk = jwks.get(0);

		SignedJWT signedJwt = new SignedJWT(
			headers.header(TYP, JWT.getType()).header(KID, jwk.getKeyID()).buildJwsHeader(),
			claims.claim(JTI, UUID.randomUUID().toString()).buildJwtClaimsSet());
		try {
			signedJwt.sign(jwsSigners.computeIfAbsent(jwk, this::createJwsSigner));
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

	private JWSSigner createJwsSigner(JWK key) {
		try {
			return JWS_SIGNER_FACTORY.createJWSSigner(key);
		} catch (JOSEException ex) {
			throw new JwtException(String.format("创建JWS签名器失败：%s" + ex.getMessage()), ex);
		}
	}

}
