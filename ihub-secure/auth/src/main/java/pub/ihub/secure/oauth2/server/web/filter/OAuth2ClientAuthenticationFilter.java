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

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import pub.ihub.secure.oauth2.server.web.converter.ClientSecretBasicAuthenticationConverter;
import pub.ihub.secure.oauth2.server.web.converter.ClientSecretPostAuthenticationConverter;
import pub.ihub.secure.oauth2.server.web.converter.DelegatingAuthenticationConverter;
import pub.ihub.secure.oauth2.server.web.converter.PublicClientAuthenticationConverter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * 用于处理OAuth 2.0客户端的身份验证请求
 * TODO 整理页面
 *
 * @author henry
 */
public class OAuth2ClientAuthenticationFilter extends OncePerRequestFilter {

	private final AuthenticationManager authenticationManager;
	private final RequestMatcher requestMatcher;
	private final HttpMessageConverter<OAuth2Error> errorHttpResponseConverter = new OAuth2ErrorHttpMessageConverter();
	private AuthenticationConverter authenticationConverter;
	private AuthenticationSuccessHandler authenticationSuccessHandler;
	private AuthenticationFailureHandler authenticationFailureHandler;

	public OAuth2ClientAuthenticationFilter(AuthenticationManager authenticationManager,
											RequestMatcher requestMatcher) {
		Assert.notNull(authenticationManager, "authenticationManager cannot be null");
		Assert.notNull(requestMatcher, "requestMatcher cannot be null");
		this.authenticationManager = authenticationManager;
		this.requestMatcher = requestMatcher;
		this.authenticationConverter = new DelegatingAuthenticationConverter(
			Arrays.asList(
				new ClientSecretBasicAuthenticationConverter(),
				new ClientSecretPostAuthenticationConverter(),
				new PublicClientAuthenticationConverter()));
		this.authenticationSuccessHandler = this::onAuthenticationSuccess;
		this.authenticationFailureHandler = this::onAuthenticationFailure;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		if (this.requestMatcher.matches(request)) {
			try {
				Authentication authenticationRequest = this.authenticationConverter.convert(request);
				if (authenticationRequest != null) {
					Authentication authenticationResult = this.authenticationManager.authenticate(authenticationRequest);
					this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, authenticationResult);
				}
			} catch (OAuth2AuthenticationException failed) {
				this.authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	public final void setAuthenticationConverter(AuthenticationConverter authenticationConverter) {
		Assert.notNull(authenticationConverter, "authenticationConverter cannot be null");
		this.authenticationConverter = authenticationConverter;
	}

	public final void setAuthenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
		Assert.notNull(authenticationSuccessHandler, "authenticationSuccessHandler cannot be null");
		this.authenticationSuccessHandler = authenticationSuccessHandler;
	}

	public final void setAuthenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
		Assert.notNull(authenticationFailureHandler, "authenticationFailureHandler cannot be null");
		this.authenticationFailureHandler = authenticationFailureHandler;
	}

	private void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										 Authentication authentication) {

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}

	private void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
										 AuthenticationException failed) throws IOException {

		SecurityContextHolder.clearContext();

		// TODO
		// The authorization server MAY return an HTTP 401 (Unauthorized) status code
		// to indicate which HTTP authentication schemes are supported.
		// If the client attempted to authenticate via the "Authorization" request header field,
		// the authorization server MUST respond with an HTTP 401 (Unauthorized) status code and
		// include the "WWW-Authenticate" response header field
		// matching the authentication scheme used by the client.

		OAuth2Error error = ((OAuth2AuthenticationException) failed).getError();
		ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
		if (OAuth2ErrorCodes.INVALID_CLIENT.equals(error.getErrorCode())) {
			httpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
		} else {
			httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);
		}
		this.errorHttpResponseConverter.write(error, null, httpResponse);
	}

}
