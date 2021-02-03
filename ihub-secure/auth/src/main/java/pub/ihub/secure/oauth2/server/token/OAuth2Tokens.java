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

import cn.hutool.core.util.ObjectUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.jwt.Jwt;
import pub.ihub.secure.oauth2.jwt.JwtEncoder;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.lang.Assert.notBlank;
import static cn.hutool.core.lang.Assert.notNull;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Base64.getUrlEncoder;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;
import static org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse.withToken;
import static org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID;
import static org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.ID_TOKEN;
import static org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.NONCE;
import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;
import static pub.ihub.core.ObjectBuilder.builder;

/**
 * OAuth2令牌的容器
 *
 * @author henry
 */
@EqualsAndHashCode
public class OAuth2Tokens implements Serializable {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	private static final StringKeyGenerator TOKEN_GENERATOR =
		new Base64StringKeyGenerator(getUrlEncoder().withoutPadding(), 96);
	private final Map<Class<? extends AbstractOAuth2Token>, OAuth2TokenHolder> tokens;

	public OAuth2Tokens() {
		this.tokens = new HashMap<>();
	}

	public OAuth2Tokens(OAuth2Tokens tokens) {
		this.tokens = new HashMap<>(tokens.tokens);
	}

	public OAuth2AuthorizationCode getAuthorizationCode() {
		return getToken(OAuth2AuthorizationCode.class);
	}

	@Nullable
	public OAuth2AccessToken getAccessToken() {
		return getToken(OAuth2AccessToken.class);
	}

	@Nullable
	public OAuth2RefreshToken getRefreshToken() {
		OAuth2RefreshToken refreshToken = getToken(OAuth2RefreshToken.class);
		return refreshToken != null ? refreshToken : getToken(OAuth2RefreshToken2.class);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends AbstractOAuth2Token> T getToken(Class<T> tokenType) {
		OAuth2TokenHolder tokenHolder = tokens.get(notNull(tokenType, "token类型不能为空！"));
		return tokenHolder != null ? (T) tokenHolder.getToken() : null;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends AbstractOAuth2Token> T getToken(String token) {
		OAuth2TokenHolder tokenHolder = tokens.values().stream()
			.filter(holder -> holder.getToken().getTokenValue().equals(notBlank(token, "token不能为空！")))
			.findFirst().orElse(null);
		return tokenHolder != null ? (T) tokenHolder.getToken() : null;
	}

	@Nullable
	public <T extends AbstractOAuth2Token> OAuth2TokenMetadata getTokenMetadata(Class<T> tokenType) {
		OAuth2TokenHolder tokenHolder = tokens.get(notNull(tokenType, "token类型不能为空！"));
		return tokenHolder != null ? tokenHolder.tokenMetadata : null;
	}

	public <T extends AbstractOAuth2Token> boolean isInvalidated(Class<T> tokenType) {
		return getTokenMetadata(tokenType).isInvalidated();
	}

	private void addToken(AbstractOAuth2Token token, OAuth2TokenMetadata tokenMetadata) {
		notNull(token, "token不能为空！");
		if (tokenMetadata == null) {
			tokenMetadata = builder(OAuth2TokenMetadata::new).build();
		}
		tokens.put(token.getClass(), new OAuth2TokenHolder(token, tokenMetadata));
	}

	public OAuth2Tokens authorizationCode(RegisteredClient registeredClient) {
		Instant issuedAt = Instant.now();
		addToken(new OAuth2AuthorizationCode(TOKEN_GENERATOR.generateKey(), issuedAt,
			issuedAt.plus(registeredClient.getAccessTokenTimeToLive())), null);
		return this;
	}

	public OAuth2Tokens accessToken(Jwt jwt, Set<String> scopes) {
		addToken(new OAuth2AccessToken(BEARER, jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), scopes),
			null);
		return this;
	}

	public OAuth2Tokens refreshToken(RegisteredClient registeredClient) {
		boolean needRefresh = !tokens.containsKey(OAuth2RefreshToken.class) || !registeredClient.isReuseRefreshTokens();
		if (registeredClient.supportedRefreshToken() && needRefresh) {
			Instant issuedAt = Instant.now();
			addToken(new OAuth2RefreshToken2(TOKEN_GENERATOR.generateKey(), issuedAt,
				issuedAt.plus(registeredClient.getRefreshTokenTimeToLive())), null);
		}
		return this;
	}

	public OAuth2Tokens oidcIdToken(JwtEncoder jwtEncoder, OAuth2Authorization authorization, RegisteredClient client) {
		OAuth2AuthorizationRequest authorizationRequest = authorization.getAuthorizationRequest();
		if (authorizationRequest.getScopes().contains(OPENID)) {
			Jwt jwtIdToken = jwtEncoder.issueIdToken(authorization.getPrincipalName(), client,
				(String) authorizationRequest.getAdditionalParameters().get(NONCE));
			addToken(new OidcIdToken(jwtIdToken.getTokenValue(), jwtIdToken.getIssuedAt(),
				jwtIdToken.getExpiresAt(), jwtIdToken.getClaims()), null);
		}
		return this;
	}

	public <T extends AbstractOAuth2Token> void token(T token, OAuth2TokenMetadata tokenMetadata) {
		addToken(token, tokenMetadata);
	}

	private OAuth2AccessTokenResponse.Builder getBuilder() {
		OAuth2AccessToken accessToken = getAccessToken();
		return builder(withToken(accessToken.getTokenValue())
			.tokenType(accessToken.getTokenType())
			.scopes(accessToken.getScopes()))
			.set(token -> token.getIssuedAt() != null && token.getExpiresAt() != null,
				OAuth2AccessTokenResponse.Builder::expiresIn, accessToken,
				token -> SECONDS.between(token.getIssuedAt(), token.getExpiresAt()))
			.set(ObjectUtil::isNotNull, OAuth2AccessTokenResponse.Builder::refreshToken,
				getRefreshToken(), AbstractOAuth2Token::getTokenValue)
			.build();
	}

	public OAuth2AccessTokenResponse getAccessTokenResponse() {
		return getBuilder().additionalParameters(Collections.emptyMap()).build();
	}

	public OAuth2AccessTokenResponse getAccessOidcTokenResponse() {
		OidcIdToken idToken = getToken(OidcIdToken.class);
		return getBuilder().additionalParameters(new HashMap<>(1) {{
			if (idToken != null) {
				put(ID_TOKEN, idToken.getTokenValue());
			}
		}}).build();
	}

	@RequiredArgsConstructor
	@Getter
	@EqualsAndHashCode
	private static class OAuth2TokenHolder implements Serializable {

		private static final long serialVersionUID = SERIAL_VERSION_UID;
		private final AbstractOAuth2Token token;
		private final OAuth2TokenMetadata tokenMetadata;

	}

}
