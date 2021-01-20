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

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.util.MultiValueMap;
import pub.ihub.secure.oauth2.server.web.OAuth2ManagerFilter;
import pub.ihub.secure.oauth2.server.web.token.OAuth2TokenRevocationAuthenticationToken;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static cn.hutool.core.lang.Assert.notBlank;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.core.context.SecurityContextHolder.clearContext;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REQUEST;

/**
 * OAuth2.0令牌撤销过滤器
 * TOKEN、TOKEN_TYPE_HINT为临时常量，在升级到Spring Security 5.5.0 GA之后可以使用OAuth2ParameterNames
 *
 * @author henry
 */
public class OAuth2TokenRevocationEndpointFilter extends OAuth2ManagerFilter {

	private static final String TOKEN = "token";
	private static final String TOKEN_TYPE_HINT = "token_type_hint";

	public OAuth2TokenRevocationEndpointFilter(AuthenticationManager authenticationManager,
											   String tokenRevocationEndpointUri) {
		super(authenticationManager, tokenRevocationEndpointUri, OAuth2TokenRevocationEndpointFilter::convert);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		if (!requestMatcher.matches(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			authenticationManager.authenticate(authenticationConverter.convert(request));
			response.setStatus(OK.value());
		} catch (OAuth2AuthenticationException ex) {
			clearContext();
			sendErrorResponse(response, ex.getError());
		}
	}

	private static Authentication convert(HttpServletRequest request) {
		MultiValueMap<String, String> parameters = getParameters(request, TOKEN, TOKEN_TYPE_HINT);
		String token = notBlank(parameters.getFirst(TOKEN),
			() -> new OAuth2AuthenticationException(new OAuth2Error(INVALID_REQUEST)));
		return new OAuth2TokenRevocationAuthenticationToken(token, getContext().getAuthentication(),
			parameters.getFirst(TOKEN_TYPE_HINT));
	}

}
