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
package pub.ihub.sso.client;

import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author liheng
 */
@AutoConfiguration(after = SsoClientAutoConfiguration.class)
@EnableConfigurationProperties(SsoClientProperties.class)
@ConditionalOnClass(RequestInterceptor.class)
public class SsoFeignAutoConfiguration {

	/**
	 * 服务内部调用鉴权拦截器
	 *
	 * @return 鉴权拦截器
	 */
	@Bean
	@ConditionalOnProperty(value = "ihub.sso.id-token", matchIfMissing = true)
	public RequestInterceptor idTokenInterceptor() {
		return (RequestTemplate requestTemplate) -> {
			// 传递登录状态
			requestTemplate.header(StpUtil.getTokenName(), StpUtil.getTokenValue());
			// 内部调用隔离token
			requestTemplate.header(SaSameUtil.SAME_TOKEN, SaSameUtil.getToken());
		};
	}

}
