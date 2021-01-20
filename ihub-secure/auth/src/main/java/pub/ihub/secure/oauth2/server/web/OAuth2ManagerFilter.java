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

package pub.ihub.secure.oauth2.server.web;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static cn.hutool.core.lang.Assert.notNull;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static pub.ihub.core.ObjectBuilder.builder;

/**
 * OAuth2管理过滤器
 *
 * @author liheng
 */
public abstract class OAuth2ManagerFilter extends OAuth2Filter {

	/**
	 * 认证管理器
	 */
	protected final AuthenticationManager authenticationManager;
	/**
	 * 请求匹配策略
	 */
	protected final RequestMatcher requestMatcher;
	/**
	 * 认证转换器
	 */
	protected final Converter<HttpServletRequest, Authentication> authenticationConverter;
	/**
	 * 异常转换器
	 */
	private final HttpMessageConverter<OAuth2Error> errorHttpResponseConverter =
		new OAuth2ErrorHttpMessageConverter();

	public OAuth2ManagerFilter(AuthenticationManager authenticationManager, RequestMatcher requestMatcher,
							   Converter<HttpServletRequest, Authentication> authenticationConverter) {
		this.authenticationManager = notNull(authenticationManager, "认证管理器不能为空！");
		this.requestMatcher = requestMatcher;
		this.authenticationConverter = authenticationConverter;
	}

	public OAuth2ManagerFilter(AuthenticationManager authenticationManager, String requestMatcherpattern,
							   Converter<HttpServletRequest, Authentication> authenticationConverter) {
		this(authenticationManager, new AntPathRequestMatcher(requestMatcherpattern, POST.name()), authenticationConverter);
	}

	protected void sendErrorResponse(HttpServletResponse response, OAuth2Error error) throws IOException {
		this.errorHttpResponseConverter.write(error, null, builder(ServletServerHttpResponse::new, response)
			.set(ServletServerHttpResponse::setStatusCode, getStatusCode(error)).build());
	}

	protected HttpStatus getStatusCode(OAuth2Error error) {
		return BAD_REQUEST;
	}

}
