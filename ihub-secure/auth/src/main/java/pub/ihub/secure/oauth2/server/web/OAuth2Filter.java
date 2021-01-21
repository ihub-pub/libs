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

import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.lang.Assert.isTrue;
import static cn.hutool.core.map.MapUtil.empty;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static cn.hutool.core.text.CharSequenceUtil.split;
import static java.util.stream.Collectors.toSet;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REQUEST;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CODE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.GRANT_TYPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.SCOPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.STATE;
import static org.springframework.security.oauth2.core.endpoint.PkceParameterNames.CODE_VERIFIER;

/**
 * OAuth2过滤器
 *
 * @author liheng
 */
public abstract class OAuth2Filter extends OncePerRequestFilter {

	protected AntPathRequestMatcher requestMatcher(String pattern, HttpMethod httpMethod) {
		return new AntPathRequestMatcher(pattern, httpMethod.name());
	}

	protected static String getParameterValue(HttpServletRequest request, String key) {
		String[] values = request.getParameterValues(key);
		if (values != null && values.length == 1) {
			return values[0];
		} else {
			throw new OAuth2AuthenticationException(new OAuth2Error(INVALID_REQUEST));
		}
	}

	protected static Set<String> extractScopes(MultiValueMap<String, String> parameters) {
		String scope = parameters.getFirst(SCOPE);
		return isNotBlank(scope) ? Arrays.stream(split(scope, " ")).collect(toSet()) : Collections.emptySet();
	}

	protected static MultiValueMap<String, String> getParameters(HttpServletRequest request, String... checkKeys) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());
		parameterMap.forEach((key, values) -> {
			if (values.length > 0) {
				for (String value : values) {
					parameters.add(key, value);
				}
			}
		});
		for (String key : checkKeys) {
			List<String> values = parameters.get(key);
			isTrue(isEmpty(values) || values.size() == 1, () ->
				new OAuth2AuthenticationException(new OAuth2Error(INVALID_REQUEST)));
		}
		return parameters;
	}

	protected static Map<String, Object> getParametersWithPkce(HttpServletRequest request, String... checkKeys) {
		if (AUTHORIZATION_CODE.getValue().equals(request.getParameter(GRANT_TYPE)) &&
			request.getParameter(CODE) != null && request.getParameter(CODE_VERIFIER) != null) {
			return new HashMap<>(getParameters(request, checkKeys).toSingleValueMap());
		}
		return empty();
	}

	protected static Map<String, Object> filterParameters(MultiValueMap<String, String> parameters, String... exceptKeys) {
		return parameters.entrySet().stream()
			.filter(e -> Arrays.stream(exceptKeys).noneMatch(k -> k.equals(e.getKey())))
			.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
	}

}
