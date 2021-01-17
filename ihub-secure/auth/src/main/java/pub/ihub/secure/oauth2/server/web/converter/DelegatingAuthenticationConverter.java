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

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 一个AuthenticationConverter ，仅将其委派给其内部AuthenticationConverter List 。
 * 每个AuthenticationConverter都有机会访问AuthenticationConverter.convert(HttpServletRequest) ，并返回第一个non-null Authentication 。
 *
 * @author henry
 */
public final class DelegatingAuthenticationConverter implements AuthenticationConverter {

	private final List<AuthenticationConverter> converters;

	public DelegatingAuthenticationConverter(List<AuthenticationConverter> converters) {
		Assert.notEmpty(converters, "converters cannot be empty");
		this.converters = Collections.unmodifiableList(new LinkedList<>(converters));
	}

	@Nullable
	@Override
	public Authentication convert(HttpServletRequest request) {
		Assert.notNull(request, "request cannot be null");
		// @formatter:off
		return this.converters.stream()
			.map(converter -> converter.convert(request))
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
		// @formatter:on
	}

}
