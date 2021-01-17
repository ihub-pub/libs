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
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import pub.ihub.secure.oauth2.server.web.OAuth2EndpointUtils;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 尝试从HttpServletRequest POST参数中提取客户端凭据
 *
 * @author henry
 */
public final class ClientSecretPostAuthenticationConverter implements AuthenticationConverter {

	@Override
	public Authentication convert(HttpServletRequest request) {
		MultiValueMap<String, String> parameters = OAuth2EndpointUtils.getParameters(request);

		// client_id (REQUIRED)
		String clientId = parameters.getFirst(OAuth2ParameterNames.CLIENT_ID);
		if (!StringUtils.hasText(clientId)) {
			return null;
		}

		if (parameters.get(OAuth2ParameterNames.CLIENT_ID).size() != 1) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST));
		}

		// client_secret (REQUIRED)
		String clientSecret = parameters.getFirst(OAuth2ParameterNames.CLIENT_SECRET);
		if (!StringUtils.hasText(clientSecret)) {
			return null;
		}

		if (parameters.get(OAuth2ParameterNames.CLIENT_SECRET).size() != 1) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST));
		}

		return new OAuth2ClientAuthenticationToken(clientId, clientSecret, ClientAuthenticationMethod.POST,
			extractAdditionalParameters(request));
	}

	private static Map<String, Object> extractAdditionalParameters(HttpServletRequest request) {
		Map<String, Object> additionalParameters = Collections.emptyMap();
		if (OAuth2EndpointUtils.matchesPkceTokenRequest(request)) {
			// Confidential clients can also leverage PKCE
			additionalParameters = new HashMap<>(OAuth2EndpointUtils.getParameters(request).toSingleValueMap());
			additionalParameters.remove(OAuth2ParameterNames.CLIENT_ID);
			additionalParameters.remove(OAuth2ParameterNames.CLIENT_SECRET);
		}
		return additionalParameters;
	}

}
