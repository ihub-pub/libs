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

import cn.hutool.core.lang.Assert;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import pub.ihub.secure.oauth2.jose.JoseHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import pub.ihub.secure.oauth2.jwt.JwtClaimsSet;
import pub.ihub.secure.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import pub.ihub.secure.crypto.CryptoKey;
import pub.ihub.secure.crypto.CryptoKeySource;

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
		String keyAlgorithm = Assert.notBlank(headers.getJwsAlgorithmValue(), "密钥算法不能为空！");
		CryptoKey<?> cryptoKey = Assert.notNull(keySource.get(keyAlgorithm), "不支持密钥算法'%s'",
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

}
