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

package pub.ihub.secure.auth.web;

import cn.hutool.core.collection.ListUtil;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pub.ihub.secure.crypto.CryptoKey;
import pub.ihub.secure.crypto.CryptoKeySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN;
import static org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType.CODE;
import static org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfiguration.DEFAULT_AUTHORIZATION_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfiguration.DEFAULT_JWK_SET_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfiguration.DEFAULT_OIDC_PROVIDER_CONFIGURATION_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfiguration.DEFAULT_TOKEN_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfiguration.ISSUER_URI;

/**
 * @author liheng
 */
@RestController
@AllArgsConstructor
public class OAuth2WellKnownController {

	//<editor-fold desc="OpenID wellKnown相关配置">

	/**
	 * OpenID Provider
	 */
	private static final String ISSUER = "issuer";

	/**
	 * OAuth2授权端点的URL
	 */
	private static final String AUTHORIZATION_ENDPOINT = "authorization_endpoint";

	/**
	 * OAuth2令牌端点的URL
	 */
	private static final String TOKEN_ENDPOINT = "token_endpoint";

	/**
	 * OAuth2令牌端点支持的客户端身份验证方法
	 */
	private static final String TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED = "token_endpoint_auth_methods_supported";

	/**
	 * JSON Web Key Set URL
	 */
	private static final String JWKS_URI = "jwks_uri";

	/**
	 * 支持的OAuth2响应类型
	 */
	private static final String RESPONSE_TYPES_SUPPORTED = "response_types_supported";

	/**
	 * 支持的OAuth2授权方式
	 */
	private static final String GRANT_TYPES_SUPPORTED = "grant_types_supported";

	/**
	 * 支持的主题标识符类型
	 */
	private static final String SUBJECT_TYPES_SUPPORTED = "subject_types_supported";

	/**
	 * 支持的OAuth2作用域
	 */
	private static final String SCOPES_SUPPORTED = "scopes_supported";

	/**
	 * JWS支持的ID Token签名算法
	 */
	private static final String ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED = "id_token_signing_alg_values_supported";

	//</editor-fold>

	private final CryptoKeySource keySource;

	@GetMapping(value = DEFAULT_OIDC_PROVIDER_CONFIGURATION_ENDPOINT_URI, produces = APPLICATION_JSON_VALUE)
	public Map<String, Object> wellKnown() {
		return new HashMap<>(10) {{
			put(ISSUER, ISSUER_URI);
			put(AUTHORIZATION_ENDPOINT, asUrl(ISSUER_URI, DEFAULT_AUTHORIZATION_ENDPOINT_URI));
			put(TOKEN_ENDPOINT, asUrl(ISSUER_URI, DEFAULT_TOKEN_ENDPOINT_URI));
			// TODO: Use ClientAuthenticationMethod.CLIENT_SECRET_BASIC CLIENT_SECRET_POST in Spring Security 5.5.0
			put(TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED, ListUtil.list(true, "client_secret_basic", "client_secret_post"));
			put(JWKS_URI, asUrl(ISSUER_URI, DEFAULT_JWK_SET_ENDPOINT_URI));
			put(RESPONSE_TYPES_SUPPORTED, ListUtil.list(true, CODE.getValue()));
			put(GRANT_TYPES_SUPPORTED, ListUtil.list(true,
				AUTHORIZATION_CODE.getValue(), CLIENT_CREDENTIALS.getValue(), REFRESH_TOKEN.getValue()));
			put(SUBJECT_TYPES_SUPPORTED, ListUtil.list(true, "public"));
			put(ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED, ListUtil.list(true, RS256.getName()));
			put(SCOPES_SUPPORTED, ListUtil.list(true, OPENID));
		}};
	}

	@GetMapping(value = DEFAULT_JWK_SET_ENDPOINT_URI, produces = APPLICATION_JSON_VALUE)
	public String jwkSet() {
		// TODO 重构密钥资源库
		return new JWKSet(
			keySource.getAsymmetricKeys().stream()
				.map(CryptoKey.AsymmetricKey::toJwk)
				.filter(Objects::nonNull)
				.collect(Collectors.toList())
		).toString();
	}

	private static String asUrl(String issuer, String endpoint) {
		return fromUriString(issuer).path(endpoint).build().toUriString();
	}

}
