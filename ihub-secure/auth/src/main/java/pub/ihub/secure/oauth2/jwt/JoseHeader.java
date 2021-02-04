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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSHeader.Builder;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.Base64URL;
import lombok.Getter;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import pub.ihub.core.ObjectBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nimbusds.jose.JWSHeader.getRegisteredParameterNames;
import static org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256;
import static org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS384;
import static org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS512;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.ES256;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.ES384;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.ES512;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS384;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS512;

/**
 * JOSE报头
 * JOSE报头是一个JSON对象，内容为JSON Web令牌的报头参数
 * 在JWT、JWS、JWE中用于JWT的加密操作以及JWT的其他属
 *
 * @author henry
 */
@Getter
public class JoseHeader {

	//<editor-fold desc="JWS算法常量">

	public static final String MAC_HS256 = "HmacSHA256";
	public static final String MAC_HS384 = "HmacSHA384";
	public static final String MAC_HS512 = "HmacSHA512";
	public static final String RSA_KEY_TYPE = "RSA";
	public static final String EC_KEY_TYPE = "EC";
	private static final Map<JwsAlgorithm, String> JCA_KEY_ALGORITHM_MAPPINGS = new HashMap<>() {
		{
			put(HS256, MAC_HS256);
			put(HS384, MAC_HS384);
			put(HS512, MAC_HS512);
			put(RS256, RSA_KEY_TYPE);
			put(RS384, RSA_KEY_TYPE);
			put(RS512, RSA_KEY_TYPE);
			put(ES256, EC_KEY_TYPE);
			put(ES384, EC_KEY_TYPE);
			put(ES512, EC_KEY_TYPE);
		}
	};

	//</editor-fold>

	//<editor-fold desc="JWS报头key">

	/**
	 * JWS算法报头标识
	 */
	public static final String ALG = "alg";
	/**
	 * JWK Set URL
	 */
	public static final String JKU = "jku";
	/**
	 * JSON Web密钥标头是公共密钥，与用于对JWS进行数字签名或加密JWE的密钥相对应
	 */
	public static final String JWK = "jwk";
	/**
	 * 密钥ID头是指示哪个键被用来确保JWS或JWE一个提示
	 */
	public static final String KID = "kid";
	/**
	 * X.509 URL头是URI，它引用X.509公钥证书或证书链的资源，该资源与用于对JWS进行数字签名或加密JWE的密钥相对应
	 */
	public static final String X5U = "x5u";
	/**
	 * X.509证书链标头，包含X.509公共密钥证书或与用于对JWS进行数字签名或加，JWE的密钥相对应的证书链
	 */
	public static final String X5C = "x5c";
	/**
	 * X.509证书SHA-1指纹报头
	 */
	public static final String X5T = "x5t";
	/**
	 * X.509证书SHA-256指纹报头
	 */
	public static final String X5T_S256 = "x5t#S256";
	/**
	 * JOSE对象类型报头
	 */
	public static final String TYP = "typ";
	/**
	 * 内容类型报头
	 */
	public static final String CTY = "cty";
	/**
	 * 关键报头
	 */
	public static final String CRIT = "crit";

	//</editor-fold>

	private final Map<String, Object> headers;

	private JoseHeader(Map<String, Object> headers) {
		this.headers = new LinkedHashMap<>(headers);
	}

	/**
	 * 返回用于对JWS进行数字签名的JWS算法
	 *
	 * @return JWS算法
	 */
	public JwsAlgorithm getJwsAlgorithm() {
		return getHeader(ALG);
	}

	public String getJwsAlgorithmValue() {
		return JCA_KEY_ALGORITHM_MAPPINGS.get(getJwsAlgorithm());
	}

	@SuppressWarnings("unchecked")
	public <T> T getHeader(String name) {
		return (T) headers.get(Assert.notBlank(name, "name不能为空！"));
	}

	public static JoseHeader withAlgorithm(JwsAlgorithm jwsAlgorithm) {
		return new JoseHeader(new HashMap<>(1) {{
			put(ALG, jwsAlgorithm);
		}});
	}

	public JoseHeader header(String key, Object value) {
		headers.put(key, value);
		return this;
	}

	public JWSHeader buildJwsHeader() {
		return ObjectBuilder.builder(new Builder(JWSAlgorithm.parse(getJwsAlgorithm().getName())))
			.set(CollUtil::isNotEmpty, Builder::criticalParams, getHeader(CRIT))
			.set(StrUtil::isNotBlank, Builder::contentType, getHeader(CTY))
			.set(Validator::isNotNull, Builder::jwkURL, converterUri(getHeader(JKU), JKU))
			.set(Validator::isNotNull, Builder::jwk, converterJwk(getHeader(JWK), JWK))
			.set(StrUtil::isNotBlank, Builder::keyID, getHeader(KID))
			.set(StrUtil::isNotBlank, Builder::type, getHeader(TYP), JOSEObjectType::new)
			.set(ArrayUtil::isNotEmpty, Builder::x509CertChain, getHeader(X5C),
				(List<String> x5c) -> x5c.stream().map(Base64::new).collect(Collectors.toList()))
			.set(StrUtil::isNotBlank, Builder::x509CertThumbprint, getHeader(X5T), Base64URL::new)
			.set(StrUtil::isNotBlank, Builder::x509CertSHA256Thumbprint, getHeader(X5T_S256), Base64URL::new)
			.set(Validator::isNotNull, Builder::x509CertURL, converterUri(getHeader(X5U), X5U))
			.set(MapUtil::isNotEmpty, Builder::customParams, headers.entrySet().stream()
				.filter(header -> !getRegisteredParameterNames().contains(header.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
			.build().build();
	}

	private URI converterUri(String uri, String headerKey) {
		try {
			return StrUtil.isNotBlank(uri) ? new URI(uri) : null;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException(StrUtil.format("Jwt编码错误：JOSE头'%s'转换失败", headerKey));
		}
	}

	private com.nimbusds.jose.jwk.JWK converterJwk(Map<String, Object> jwk, String headerKey) {
		try {
			return MapUtil.isNotEmpty(jwk) ? com.nimbusds.jose.jwk.JWK.parse(jwk) : null;
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(StrUtil.format("Jwt编码错误：JOSE头'%s'转换失败", headerKey));
		}
	}

}
