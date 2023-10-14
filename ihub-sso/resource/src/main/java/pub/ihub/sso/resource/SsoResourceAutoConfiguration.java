/*
 * Copyright (c) 2022-2023 the original author or authors.
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
package pub.ihub.sso.resource;

import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.same.SaSameUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import pub.ihub.cloud.CloudAutoConfiguration;
import pub.ihub.cloud.rest.Result;

/**
 * @author liheng
 */
@AutoConfiguration(after = CloudAutoConfiguration.class)
@EnableConfigurationProperties(SsoResourceProperties.class)
@ConditionalOnDiscoveryEnabled
@ConditionalOnClass(SaServletFilter.class)
public class SsoResourceAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SaServletFilter getSaServletFilter() {
		return new SaServletFilter().addInclude("/**")
			.setAuth(obj -> SaSameUtil.checkCurrentRequestToken())
			.setError(e -> Result.error(e.getMessage()));
	}

}
