/*
 * Copyright (c) 2022-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pub.ihub.sso.client;

import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.sso.processor.SaSsoClientProcessor;
import cn.hutool.core.builder.GenericBuilder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import pub.ihub.cloud.CloudAutoConfiguration;

/**
 * @author liheng
 */
@AutoConfiguration(after = CloudAutoConfiguration.class)
@EnableConfigurationProperties(SsoClientProperties.class)
@ConditionalOnDiscoveryEnabled
@ConditionalOnClass(SaServletFilter.class)
public class SsoClientAutoConfiguration {

	/**
	 * 客户端单点登录过滤器
	 *
	 * @return 过滤器
	 */
	@Bean
	public Filter ssoFilter() {
		return (ServletRequest request, ServletResponse response, FilterChain chain) -> {
			Object object = SaSsoClientProcessor.instance.dister();
			if (object instanceof String) {
				response.getWriter().write(object.toString());
			}
		};
	}

	/**
	 * 注册单点登录过滤器
	 *
	 * @return 过滤器
	 */
	@Bean
	public FilterRegistrationBean<?> ssoFilterRegistration() {
		return GenericBuilder.of(FilterRegistrationBean::new)
			.with(FilterRegistrationBean::setName, "SsoFilter")
			.with(FilterRegistrationBean::addUrlPatterns, "/sso/*")
			.with(FilterRegistrationBean::setFilter, ssoFilter())
			.build();
	}

}
