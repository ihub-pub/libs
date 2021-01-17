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

package pub.ihub.secure.crypto;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.map.MapUtil;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.oauth2.jwt.JwtException;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

import static cn.hutool.crypto.KeyUtil.generateKey;
import static cn.hutool.crypto.KeyUtil.generateKeyPair;
import static pub.ihub.secure.oauth2.jose.JoseHeader.MAC_HS256;
import static pub.ihub.secure.oauth2.jose.JoseHeader.RSA_KEY_TYPE;
import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 加密密钥
 * TODO 数据库持久化
 *
 * @param <K> Key
 * @author henry
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public abstract class CryptoKey<K extends Key> implements Serializable {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	private final K key;
	private final String id;
	private final Map<String, Object> metadata;

	/**
	 * 返回密钥算法
	 *
	 * @return 密钥算法
	 */
	public final String getAlgorithm() {
		return getKey().getAlgorithm();
	}

	/**
	 * 返回JWS签名器
	 *
	 * @return 签名器
	 */
	public abstract JWSSigner getJwsSigner();

	/**
	 * 创建对称密钥
	 *
	 * @return 对称密钥
	 */
	public static SymmetricKey symmetric() {
		return new SymmetricKey(generateKey(MAC_HS256));
	}

	/**
	 * 生成非对称密钥
	 *
	 * @return 非对称密钥
	 */
	public static AsymmetricKey asymmetric() {
		KeyPair rsaKeyPair = generateKeyPair(RSA_KEY_TYPE, 2048);
		return new AsymmetricKey(rsaKeyPair.getPrivate(), rsaKeyPair.getPublic());
	}

	/**
	 * 对称密钥
	 */
	@EqualsAndHashCode(callSuper = true)
	public static class SymmetricKey extends CryptoKey<SecretKey> {

		public SymmetricKey(SecretKey key) {
			super(key, UUID.randomUUID().toString(), MapUtil.empty());
		}

		@Override
		public JWSSigner getJwsSigner() {
			try {
				return new MACSigner(getKey());
			} catch (KeyLengthException ex) {
				ex.printStackTrace();
				throw new JwtException(ex.getMessage(), ex);
			}
		}

	}

	/**
	 * 非对称密钥
	 */
	@Getter
	@EqualsAndHashCode(callSuper = true)
	public static class AsymmetricKey extends CryptoKey<PrivateKey> {

		private final PublicKey publicKey;

		public AsymmetricKey(PrivateKey key, PublicKey publicKey) {
			super(key, UUID.randomUUID().toString(), MapUtil.empty());
			this.publicKey = publicKey;
		}

		@Override
		public JWSSigner getJwsSigner() {
			Assert.state(getAlgorithm().equals(RSA_KEY_TYPE), "不支持密钥类型 '" + getAlgorithm() + "'");
			return new RSASSASigner(getKey());
		}

	}

}
