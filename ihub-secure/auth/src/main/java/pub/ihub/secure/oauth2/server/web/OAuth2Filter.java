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

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.collection.CollUtil.isNotEmpty;
import static cn.hutool.core.lang.Assert.isTrue;
import static cn.hutool.core.lang.Assert.notBlank;
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

	protected static String getParameterValue(HttpServletRequest request, String key, boolean canNull) {
		String[] values = request.getParameterValues(key);
		if (canNull && ArrayUtil.isEmpty(values)) {
			return null;
		} else if (ArrayUtil.isNotEmpty(values) && values.length == 1) {
			return values[0];
		} else {
			throw exceptionSupplier(key).get();
		}
	}

	protected static String getParameterValue(HttpServletRequest request, String key) {
		return getParameterValue(request, key, false);
	}

	// TODO 确认多值参数
	protected static Set<String> extractScopes(MultiValueMap<String, String> parameters) {
		String scope = getParameterValue(parameters, SCOPE);
		return isNotBlank(scope) ? Arrays.stream(split(scope, " ")).collect(toSet()) : Collections.emptySet();
	}

	protected static String getParameterValue(MultiValueMap<String, String> parameters, String key, boolean canNull) {
		List<String> values = parameters.get(key);
		if (canNull && isEmpty(values)) {
			return null;
		} else if (isNotEmpty(values) && values.size() == 1) {
			return notBlank(values.get(0), exceptionSupplier(key));
		} else {
			throw exceptionSupplier(key).get();
		}
	}

	protected static String getParameterValue(MultiValueMap<String, String> parameters, String key) {
		return getParameterValue(parameters, key, false);
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
			isTrue(isEmpty(values) || values.size() == 1, exceptionSupplier(key));
		}
		return parameters;
	}

	protected static Map<String, Object> filterParameters(MultiValueMap<String, String> parameters, String... exceptKeys) {
		return isEmpty(parameters) ? empty() : parameters.entrySet().stream()
			.filter(e -> Arrays.stream(exceptKeys).noneMatch(k -> k.equals(e.getKey())))
			.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
	}

	protected static Supplier<OAuth2AuthenticationException> exceptionSupplier(String errorCode, String parameterName,
																			   String redirectUri) {
		return () -> new OAuth2AuthenticationException(
			new OAuth2Error(errorCode, "OAuth 2.0参数错误：" + parameterName, redirectUri)
		);
	}

	protected static Supplier<OAuth2AuthenticationException> exceptionSupplier(String errorCode, String parameterName) {
		return exceptionSupplier(errorCode, parameterName, null);
	}

	protected static Supplier<OAuth2AuthenticationException> exceptionSupplier(String parameterName) {
		return exceptionSupplier(INVALID_REQUEST, parameterName);
	}

}
