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

package pub.ihub.secure.resource.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.autoconfigure.security.SecurityProperties.IGNORED_ORDER;

/**
 * 资源服务配置
 *
 * @author liheng
 */
@EnableWebSecurity
public class AuthResourceServerConfig {

	@Bean
	@Order(IGNORED_ORDER)
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// TODO 可配资源范围
		http.mvcMatcher("/messages/**")
			.authorizeRequests()
			.mvcMatchers("/messages/**").hasAnyAuthority("SCOPE_message.read");
		http.oauth2ResourceServer()
			.jwt();
		return http.build();
	}

}
