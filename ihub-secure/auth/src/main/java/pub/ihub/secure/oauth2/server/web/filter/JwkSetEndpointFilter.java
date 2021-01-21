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

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.security.web.util.matcher.RequestMatcher;
import pub.ihub.secure.crypto.CryptoKey.AsymmetricKey;
import pub.ihub.secure.crypto.CryptoKeySource;
import pub.ihub.secure.oauth2.server.web.OAuth2Filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * JWK过滤器
 *
 * @author henry
 */
public class JwkSetEndpointFilter extends OAuth2Filter {

	private final CryptoKeySource keySource;
	private final RequestMatcher requestMatcher;

	public JwkSetEndpointFilter(CryptoKeySource keySource, String jwkSetEndpointUri) {
		this.keySource = keySource;
		this.requestMatcher = requestMatcher(jwkSetEndpointUri, GET);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		if (!this.requestMatcher.matches(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		response.setContentType(APPLICATION_JSON_VALUE);

		try (Writer writer = response.getWriter()) {
			writer.write(new JWKSet(
				this.keySource.getAsymmetricKeys().stream()
					.map(AsymmetricKey::toJwk)
					.filter(Objects::nonNull)
					.collect(Collectors.toList())
			).toString());
		}
	}

}
