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
package pub.ihub.sso.server;

import cn.dev33.satoken.context.SaHolder;
import cn.hutool.captcha.ICaptcha;
import jakarta.servlet.http.HttpServletRequest;
import me.zhyd.oauth.cache.AuthCacheConfig;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import pub.ihub.cloud.CloudAutoConfiguration;

import javax.security.auth.login.FailedLoginException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author liheng
 */
@AutoConfiguration(after = CloudAutoConfiguration.class)
@EnableConfigurationProperties({SsoServerProperties.class, SsoCaptchaProperties.class})
@ComponentScan("pub.ihub.sso.server")
public class SsoServerAutoConfiguration {

	/**
	 * 用户信息接口默认实现，实际环境需要重新实现
	 *
	 * @return 默认实现
	 */
	@Bean
	@ConditionalOnMissingBean
	SsoUserDetailsService<Long> defaultUserDetailsService() {
		return username -> Stream.of(defaultUser()).filter(details -> details.getUsername().equals(username))
			.findFirst().orElse(null);
	}

	/**
	 * 用户信息接口默认实现，实际环境需要重新实现
	 *
	 * @return 默认实现
	 */
	@Bean
	@ConditionalOnMissingBean
	SsoSocialUserService<Long> socialUserService() {
		return new SsoSocialUserService<>() {
			@Override
			public SsoUserDetails<Long> findUserByUuid(String source, String uuid) {
				return defaultUser();
			}

			@Override
			public SsoUserDetails<Long> createUserByAuth(String source, AuthUser authUser) {
				return defaultUser();
			}

			@Override
			public void bingUserAndAuth(String source, Long loginId, AuthUser authUser) {
			}
		};
	}

	/**
	 * 授权信息缓存服务
	 *
	 * @param stringRedisTemplate redisTemplate
	 * @return 缓存服务
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(StringRedisTemplate.class)
	AuthStateCache authStateRedisCache(StringRedisTemplate stringRedisTemplate) {
		ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
		return new AuthStateCache() {
			@Override
			public void cache(String key, String value) {
				valueOperations.set(prefixKey(key), value, AuthCacheConfig.timeout, TimeUnit.MILLISECONDS);
			}

			@Override
			public void cache(String key, String value, long timeout) {
				valueOperations.set(prefixKey(key), value, timeout, TimeUnit.MILLISECONDS);
			}

			@Override
			public String get(String key) {
				return valueOperations.get(prefixKey(key));
			}

			@Override
			public boolean containsKey(String key) {
				return Boolean.TRUE.equals(stringRedisTemplate.hasKey(prefixKey(key)));
			}

			private String prefixKey(String key) {
				return "auth-state:" + key;
			}
		};
	}

	/**
	 * 登录验证码检查处理器
	 *
	 * @return 检查处理器
	 */
	@Bean
	@ConditionalOnProperty(value = "ihub.sso.captcha.enabled", matchIfMissing = true)
	SsoLoginTicketHandle captchaVerificationHandle() {
		return () -> {
			var session = ((HttpServletRequest) SaHolder.getRequest().getSource()).getSession();
			var captcha = session.getAttribute("captcha");
			var captchaCode = SaHolder.getRequest().getParam("captcha");
			session.removeAttribute("captcha");
			if (null == captcha || !((ICaptcha) captcha).verify(captchaCode)) {
				throw new FailedLoginException("验证码错误！");
			}
		};
	}

	/**
	 * 默认用户
	 *
	 * @return 用户
	 */
	private SsoUserDetails<Long> defaultUser() {
		return new SsoUserDetails<>() {
			@Override
			public Long getLoginId() {
				return 10001L;
			}

			@Override
			public String getUsername() {
				return "admin";
			}

			@Override
			public String getPassword() {
				return "123456";
			}
		};
	}

}
