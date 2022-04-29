/*
 * Copyright (c) 2022 Henry 李恒 (henry.box@outlook.com).
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
package pub.ihub.sso.client;

import cn.dev33.satoken.sso.SaSsoHandle;
import cn.hutool.core.builder.GenericBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author liheng
 */
@Configuration
public class SsoClientAutoConfiguration {

	@Bean
	public Filter ssoFilter() {
		return (ServletRequest request, ServletResponse response, FilterChain chain) -> {
			Object object = SaSsoHandle.clientRequest();
			if (object instanceof String) {
				response.getWriter().write(object.toString());
			}
		};
	}

	@Bean
	public FilterRegistrationBean<?> ssoFilterRegistration() {
		return GenericBuilder.of(FilterRegistrationBean::new)
			.with(FilterRegistrationBean::setName, "SsoFilter")
			.with(FilterRegistrationBean::addUrlPatterns, "/sso/*")
			.with(FilterRegistrationBean::setFilter, ssoFilter())
			.build();
	}

}
