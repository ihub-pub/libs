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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.MultiValueMap;
import pub.ihub.secure.oauth2.server.web.OAuth2ManagerFilter;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthenticationToken;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;
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
public class OAuth2ClientAuthenticationFilter extends OAuth2ManagerFilter {

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

	public OAuth2ClientAuthenticationFilter(AuthenticationManager authenticationManager,
											RequestMatcher requestMatcher) {
		super(authenticationManager, requestMatcher, OAuth2ClientAuthenticationFilter::convert);
		authenticationSuccessHandler = this::onAuthenticationSuccess;
		authenticationFailureHandler = this::onAuthenticationFailure;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		if (this.requestMatcher.matches(request)) {
			try {
				// 获取客户端授权令牌
				Authentication authenticationRequest = authenticationConverter.convert(request);
				if (authenticationRequest != null) {
					Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);
					authenticationSuccessHandler.onAuthenticationSuccess(request, response, authenticationResult);
				}
			} catch (OAuth2AuthenticationException failed) {
				authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	private static Authentication convert(HttpServletRequest request) {
		return converters.stream().map(converter -> converter.convert(request))
			.filter(Objects::nonNull).findFirst().orElse(null);
	}

	/**
	 * 尝试从HttpServletRequest提取HTTP Basic凭证
	 *
	 * @param request 请求
	 * @return 认证凭证
	 */
	private static Authentication clientSecretBasicConvert(HttpServletRequest request) {
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
			return new OAuth2ClientAuthenticationToken(clientId, clientSecret, BASIC,
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
	private static Authentication clientSecretPostConvert(HttpServletRequest request) {
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
		return new OAuth2ClientAuthenticationToken(clientId, clientSecret, POST,
			filterParameters(parameters, CLIENT_ID, CLIENT_SECRET));
	}

	/**
	 * 尝试从HttpServletRequest提取用于认证公共客户端的参数（PKCE）
	 *
	 * @param request 请求
	 * @return 认证凭证
	 */
	private static Authentication publicClientConvert(HttpServletRequest request) {
		MultiValueMap<String, String> parameters = getParametersWithPkce(request, CODE_VERIFIER);
		if (isEmpty(parameters)) {
			return null;
		}
		return new OAuth2ClientAuthenticationToken(getParameterValue(parameters, CLIENT_ID),
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

	@Override
	protected HttpStatus getStatusCode(OAuth2Error error) {
		return INVALID_CLIENT.equals(error.getErrorCode()) ? UNAUTHORIZED : BAD_REQUEST;
	}

	private static MultiValueMap<String, String> getParametersWithPkce(HttpServletRequest request, String... checkKeys) {
		if (AUTHORIZATION_CODE.getValue().equals(request.getParameter(GRANT_TYPE)) &&
			request.getParameter(CODE) != null && request.getParameter(CODE_VERIFIER) != null) {
			return getParameters(request, checkKeys);
		}
		return null;
	}

}
