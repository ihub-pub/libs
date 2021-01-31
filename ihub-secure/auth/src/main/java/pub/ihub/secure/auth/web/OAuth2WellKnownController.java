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

import com.nimbusds.jose.jwk.JWKSet;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.core.oidc.OidcProviderConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pub.ihub.secure.crypto.CryptoKey;
import pub.ihub.secure.crypto.CryptoKeySource;

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
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.DEFAULT_AUTHORIZATION_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.DEFAULT_JWK_SET_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.DEFAULT_OIDC_PROVIDER_CONFIGURATION_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.DEFAULT_TOKEN_ENDPOINT_URI;
import static pub.ihub.secure.auth.config.OAuth2AuthorizationServerConfigurer.ISSUER_URI;

/**
 * @author liheng
 */
@RestController
@AllArgsConstructor
public class OAuth2WellKnownController {

	private final CryptoKeySource keySource;

	@GetMapping(value = DEFAULT_OIDC_PROVIDER_CONFIGURATION_ENDPOINT_URI, produces = APPLICATION_JSON_VALUE)
	public Map<String, Object> wellKnown() {
		// TODO IODC可选
		// 重构
		return OidcProviderConfiguration.builder()
			.issuer(ISSUER_URI)
			.authorizationEndpoint(asUrl(ISSUER_URI, DEFAULT_AUTHORIZATION_ENDPOINT_URI))
			.tokenEndpoint(asUrl(ISSUER_URI, DEFAULT_TOKEN_ENDPOINT_URI))
			// TODO: Use ClientAuthenticationMethod.CLIENT_SECRET_BASIC in Spring Security 5.5.0
			.tokenEndpointAuthenticationMethod("client_secret_basic")
			// TODO: Use ClientAuthenticationMethod.CLIENT_SECRET_POST in Spring Security 5.5.0
			.tokenEndpointAuthenticationMethod("client_secret_post")
			.jwkSetUri(asUrl(ISSUER_URI, DEFAULT_JWK_SET_ENDPOINT_URI))
			.responseType(CODE.getValue())
			.grantType(AUTHORIZATION_CODE.getValue())
			.grantType(CLIENT_CREDENTIALS.getValue())
			.grantType(REFRESH_TOKEN.getValue())
			.subjectType("public")
			.idTokenSigningAlgorithm(RS256.getName())
			.scope(OPENID)
			.build().getClaims();
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
