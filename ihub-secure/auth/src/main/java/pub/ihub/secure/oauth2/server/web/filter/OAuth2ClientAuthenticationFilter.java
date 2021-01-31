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

package pub.ihub.secure.oauth2.server.web.filter;

import lombok.Setter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.RegisteredClientRepository;
import pub.ihub.secure.oauth2.server.TokenType;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.web.OAuth2Filter;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthToken;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.lang.Assert.isTrue;
import static cn.hutool.core.lang.Assert.notNull;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.core.context.SecurityContextHolder.clearContext;
import static org.springframework.security.core.context.SecurityContextHolder.createEmptyContext;
import static org.springframework.security.core.context.SecurityContextHolder.setContext;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.BASIC;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.POST;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_CLIENT;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REQUEST;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_ID;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_SECRET;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CODE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.GRANT_TYPE;
import static org.springframework.security.oauth2.core.endpoint.PkceParameterNames.CODE_VERIFIER;
import static org.springframework.security.web.authentication.www.BasicAuthenticationConverter.AUTHENTICATION_SCHEME_BASIC;
import static pub.ihub.core.ObjectBuilder.builder;

/**
 * OAuth2.0客户端授权令牌认证过滤器
 *
 * @author henry
 */
@Setter
public class OAuth2ClientAuthenticationFilter extends OAuth2Filter {

	private final RegisteredClientRepository registeredClientRepository;
	private final OAuth2AuthorizationService authorizationService;
	/**
	 * 请求匹配策略
	 */
	private final RequestMatcher requestMatcher;
	/**
	 * 认证转换器
	 */
	private final Converter<HttpServletRequest, OAuth2ClientAuthToken> authenticationConverter;
	/**
	 * 异常转换器
	 */
	private final HttpMessageConverter<OAuth2Error> errorHttpResponseConverter =
		new OAuth2ErrorHttpMessageConverter();
	/**
	 * 认证凭证转换器
	 */
	private static List<AuthenticationConverter> converters = Arrays.asList(
		OAuth2ClientAuthenticationFilter::clientSecretBasicConvert,
		OAuth2ClientAuthenticationFilter::clientSecretPostConvert,
		OAuth2ClientAuthenticationFilter::publicClientConvert);
	/**
	 * 认证成功处理器
	 */
	private AuthenticationSuccessHandler authenticationSuccessHandler;
	/**
	 * 认证失败处理
	 */
	private AuthenticationFailureHandler authenticationFailureHandler;

	public OAuth2ClientAuthenticationFilter(RegisteredClientRepository registeredClientRepository,
											OAuth2AuthorizationService authorizationService,
											RequestMatcher requestMatcher) {
		this.registeredClientRepository = registeredClientRepository;
		this.authorizationService = authorizationService;
		this.requestMatcher = requestMatcher;
		this.authenticationConverter = OAuth2ClientAuthenticationFilter::convert;
		authenticationSuccessHandler = this::onAuthenticationSuccess;
		authenticationFailureHandler = this::onAuthenticationFailure;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		if (this.requestMatcher.matches(request)) {
			try {
				OAuth2ClientAuthToken authentication = authenticationConverter.convert(request);
				if (authentication != null) {
					RegisteredClient registeredClient = notNull(registeredClientRepository
						.findByClientId(authentication.getPrincipal().toString()), invalidClientException());
					isTrue(registeredClient.getClientAuthenticationMethods().contains(
						authentication.getClientAuthenticationMethod()), invalidClientException());

					boolean authenticatedCredentials = false;
					if (authentication.getCredentials() != null) {
						// TODO Use PasswordEncoder.matches()
						isTrue(registeredClient.getClientSecret().equals(authentication.getCredentials().toString()),
							invalidClientException());
						authenticatedCredentials = true;
					}
					isTrue(authenticatedCredentials ||
						authenticatePkceIfAvailable(authentication, registeredClient), invalidClientException());

					authenticationSuccessHandler
						.onAuthenticationSuccess(request, response, new OAuth2ClientAuthToken(registeredClient));
				}
			} catch (OAuth2AuthenticationException failed) {
				authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	private static OAuth2ClientAuthToken convert(HttpServletRequest request) {
		return (OAuth2ClientAuthToken) converters.stream().map(converter -> converter.convert(request))
			.filter(Objects::nonNull).findFirst().orElse(null);
	}

	/**
	 * 尝试从HttpServletRequest提取HTTP Basic凭证
	 *
	 * @param request 请求
	 * @return 认证凭证
	 */
	private static OAuth2ClientAuthToken clientSecretBasicConvert(HttpServletRequest request) {
		String header = request.getHeader(AUTHORIZATION);
		if (header == null) {
			return null;
		}
		try {
			String[] parts = header.split("\\s");
			if (!parts[0].equalsIgnoreCase(AUTHENTICATION_SCHEME_BASIC)) {
				return null;
			}
			byte[] decodedCredentials = Base64.getDecoder().decode(parts[1].getBytes(UTF_8));
			String[] credentials = new String(decodedCredentials, UTF_8).split(":", 2);
			String clientId = URLDecoder.decode(credentials[0], UTF_8.name());
			String clientSecret = URLDecoder.decode(credentials[1], UTF_8.name());
			return new OAuth2ClientAuthToken(clientId, clientSecret, BASIC,
				filterParameters(getParametersWithPkce(request)));
		} catch (Exception ex) {
			throw new OAuth2AuthenticationException(new OAuth2Error(INVALID_REQUEST), ex);
		}
	}

	/**
	 * 尝试从HttpServletRequest POST参数中提取客户端凭据
	 *
	 * @param request 请求
	 * @return 认证凭证
	 */
	private static OAuth2ClientAuthToken clientSecretPostConvert(HttpServletRequest request) {
		MultiValueMap<String, String> parameters = getParametersWithPkce(request, CLIENT_ID, CLIENT_SECRET);
		if (isEmpty(parameters)) {
			return null;
		}
		String clientId = getParameterValue(parameters, CLIENT_ID, true);
		if (isBlank(clientId)) {
			return null;
		}
		String clientSecret = getParameterValue(parameters, CLIENT_SECRET, true);
		if (isBlank(clientSecret)) {
			return null;
		}
		return new OAuth2ClientAuthToken(clientId, clientSecret, POST,
			filterParameters(parameters, CLIENT_ID, CLIENT_SECRET));
	}

	/**
	 * 尝试从HttpServletRequest提取用于认证公共客户端的参数（PKCE）
	 *
	 * @param request 请求
	 * @return 认证凭证
	 */
	private static OAuth2ClientAuthToken publicClientConvert(HttpServletRequest request) {
		MultiValueMap<String, String> parameters = getParametersWithPkce(request, CODE_VERIFIER);
		if (isEmpty(parameters)) {
			return null;
		}
		return new OAuth2ClientAuthToken(getParameterValue(parameters, CLIENT_ID),
			filterParameters(parameters, CLIENT_ID));
	}

	private void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										 Authentication authentication) {
		setContext(builder(createEmptyContext()).set(SecurityContext::setAuthentication, authentication).build());
	}

	private void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
										 AuthenticationException failed) throws IOException {
		clearContext();
		sendErrorResponse(response, ((OAuth2AuthenticationException) failed).getError());
	}

	private static MultiValueMap<String, String> getParametersWithPkce(HttpServletRequest request, String... checkKeys) {
		if (AUTHORIZATION_CODE.getValue().equals(request.getParameter(GRANT_TYPE)) &&
			request.getParameter(CODE) != null && request.getParameter(CODE_VERIFIER) != null) {
			return getParameters(request, checkKeys);
		}
		return null;
	}

	private void sendErrorResponse(HttpServletResponse response, OAuth2Error error) throws IOException {
		this.errorHttpResponseConverter.write(error, null, builder(ServletServerHttpResponse::new, response)
			.set(ServletServerHttpResponse::setStatusCode,
				INVALID_CLIENT.equals(error.getErrorCode()) ? UNAUTHORIZED : BAD_REQUEST).build());
	}

	private boolean authenticatePkceIfAvailable(OAuth2ClientAuthToken clientAuthentication,
												RegisteredClient registeredClient) {

		Map<String, Object> parameters = clientAuthentication.getAdditionalParameters();
		if (CollectionUtils.isEmpty(parameters) || !authorizationCodeGrant(parameters)) {
			return false;
		}

		OAuth2Authorization authorization = this.authorizationService.findByToken(
			(String) parameters.get(CODE), TokenType.AUTHORIZATION_CODE);
		notNull(authorization, invalidClientException());

		OAuth2AuthorizationRequest authorizationRequest = authorization.getAuthorizationRequest();

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

	private static Supplier<OAuth2AuthenticationException> invalidClientException() {
		return exceptionSupplier(INVALID_CLIENT);
	}

}
