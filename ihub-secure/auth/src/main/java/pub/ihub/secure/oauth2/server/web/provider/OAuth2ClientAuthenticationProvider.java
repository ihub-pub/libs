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

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.RegisteredClientRepository;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.web.OAuth2AuthProvider;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthToken;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import static cn.hutool.core.lang.Assert.isTrue;
import static cn.hutool.core.lang.Assert.notNull;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CODE;
import static pub.ihub.secure.oauth2.server.TokenType.AUTHORIZATION_CODE;

/**
 * 用于验证OAuth 2.0客户端的AuthenticationProvider实现。
 *
 * @author henry
 */
@RequiredArgsConstructor
public class OAuth2ClientAuthenticationProvider extends OAuth2AuthProvider<OAuth2ClientAuthToken> {

	private final RegisteredClientRepository registeredClientRepository;
	private final OAuth2AuthorizationService authorizationService;

	@Override
	public Authentication auth(OAuth2ClientAuthToken authentication) throws AuthenticationException {
		String clientId = authentication.getPrincipal().toString();
		RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
		notNull(registeredClient, invalidClientException());

		isTrue(registeredClient.getClientAuthenticationMethods().contains(
			authentication.getClientAuthenticationMethod()), invalidClientException());

		boolean authenticatedCredentials = false;

		if (authentication.getCredentials() != null) {
			String clientSecret = authentication.getCredentials().toString();
			// TODO Use PasswordEncoder.matches()
			isTrue(registeredClient.getClientSecret().equals(clientSecret), invalidClientException());
			authenticatedCredentials = true;
		}

		authenticatedCredentials = authenticatedCredentials ||
			authenticatePkceIfAvailable(authentication, registeredClient);
		isTrue(authenticatedCredentials, invalidClientException());

		return new OAuth2ClientAuthToken(registeredClient);
	}

	private boolean authenticatePkceIfAvailable(OAuth2ClientAuthToken clientAuthentication,
												RegisteredClient registeredClient) {

		Map<String, Object> parameters = clientAuthentication.getAdditionalParameters();
		if (CollectionUtils.isEmpty(parameters) || !authorizationCodeGrant(parameters)) {
			return false;
		}

		OAuth2Authorization authorization = this.authorizationService.findByToken(
			(String) parameters.get(CODE), AUTHORIZATION_CODE);
		notNull(authorization, invalidClientException());

		OAuth2AuthorizationRequest authorizationRequest = authorization.getAttribute(
			OAuth2Authorization.AUTHORIZATION_REQUEST);

		String codeChallenge = (String) authorizationRequest.getAdditionalParameters()
			.get(PkceParameterNames.CODE_CHALLENGE);
		isTrue(isNotBlank(codeChallenge) || registeredClient.isRequireProofKey(), invalidClientException());

		String codeChallengeMethod = (String) authorizationRequest.getAdditionalParameters()
			.get(PkceParameterNames.CODE_CHALLENGE_METHOD);
		String codeVerifier = (String) parameters.get(PkceParameterNames.CODE_VERIFIER);
		isTrue(codeVerifierValid(codeVerifier, codeChallenge, codeChallengeMethod), invalidClientException());

		return true;
	}

	private static boolean authorizationCodeGrant(Map<String, Object> parameters) {
		return AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(
			parameters.get(OAuth2ParameterNames.GRANT_TYPE)) &&
			parameters.get(CODE) != null;
	}

	private static boolean codeVerifierValid(String codeVerifier, String codeChallenge, String codeChallengeMethod) {
		if (!StringUtils.hasText(codeVerifier)) {
			return false;
		} else if (!StringUtils.hasText(codeChallengeMethod) || "plain".equals(codeChallengeMethod)) {
			return codeVerifier.equals(codeChallenge);
		} else if ("S256".equals(codeChallengeMethod)) {
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
				String encodedVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
				return encodedVerifier.equals(codeChallenge);
			} catch (NoSuchAlgorithmException ex) {
				// It is unlikely that SHA-256 is not available on the server. If it is not available,
				// there will likely be bigger issues as well. We default to SERVER_ERROR.
			}
		}
		throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR));
	}

}
