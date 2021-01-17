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

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 甲Converter ，其选择（和代表）到内部的一个Map的Converter的使用OAuth2ParameterNames.GRANT_TYPE请求参数
 *
 * @author henry
 */
public final class DelegatingAuthorizationGrantAuthenticationConverter implements Converter<HttpServletRequest, Authentication> {

	private final Map<AuthorizationGrantType, Converter<HttpServletRequest, Authentication>> converters;

	public DelegatingAuthorizationGrantAuthenticationConverter(
		Map<AuthorizationGrantType, Converter<HttpServletRequest, Authentication>> converters) {
		Assert.notEmpty(converters, "converters cannot be empty");
		this.converters = Collections.unmodifiableMap(new HashMap<>(converters));
	}

	@Nullable
	@Override
	public Authentication convert(HttpServletRequest request) {
		Assert.notNull(request, "request cannot be null");

		String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
		if (StringUtils.isEmpty(grantType)) {
			return null;
		}

		Converter<HttpServletRequest, Authentication> converter =
			this.converters.get(new AuthorizationGrantType(grantType));
		if (converter == null) {
			return null;
		}

		return converter.convert(request);
	}

}
