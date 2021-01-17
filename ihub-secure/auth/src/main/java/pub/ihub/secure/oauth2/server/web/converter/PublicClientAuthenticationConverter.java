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

package pub.ihub.secure.oauth2.server.web.converter;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import pub.ihub.secure.oauth2.server.web.OAuth2EndpointUtils;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * 尝试使用用于代码交换的证明密钥（PKCE）从HttpServletRequest提取用于认证公共客户端的参数。
 *
 * @author henry
 */
public final class PublicClientAuthenticationConverter implements AuthenticationConverter {

	@Override
	public Authentication convert(HttpServletRequest request) {
		if (!OAuth2EndpointUtils.matchesPkceTokenRequest(request)) {
			return null;
		}

		MultiValueMap<String, String> parameters = OAuth2EndpointUtils.getParameters(request);

		// client_id (REQUIRED for public clients)
		String clientId = parameters.getFirst(OAuth2ParameterNames.CLIENT_ID);
		if (!StringUtils.hasText(clientId) ||
			parameters.get(OAuth2ParameterNames.CLIENT_ID).size() != 1) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST));
		}

		// code_verifier (REQUIRED)
		if (parameters.get(PkceParameterNames.CODE_VERIFIER).size() != 1) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST));
		}

		parameters.remove(OAuth2ParameterNames.CLIENT_ID);

		return new OAuth2ClientAuthenticationToken(
			clientId, new HashMap<>(parameters.toSingleValueMap()));
	}

}
