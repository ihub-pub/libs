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
package pub.ihub.sso.server;

import cn.dev33.satoken.context.SaHolder;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.CaptchaService;
import me.zhyd.oauth.cache.AuthCacheConfig;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.security.auth.login.FailedLoginException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author liheng
 */
@Configuration
@EnableConfigurationProperties(SsoProperties.class)
public class SsoServerAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	SsoUserDetailsService<Long> defaultUserDetailsService() {
		// 用户信息接口默认实现，实际环境需要重新实现
		return username -> Stream.of(defaultUser()).filter(details -> details.getUsername().equals(username))
			.findFirst().orElse(null);
	}

	@Bean
	@ConditionalOnMissingBean
	SsoSocialUserService<Long> socialUserService() {
		// 用户信息接口默认实现，实际环境需要重新实现
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
			public boolean bingUserAndAuth(String source, Long loginId, AuthUser authUser) {
				return true;
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(RedisTemplate.class)
	AuthStateCache authStateRedisCache(RedisTemplate<String, String> redisTemplate) {
		ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
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
				return Boolean.TRUE.equals(redisTemplate.hasKey(prefixKey(key)));
			}

			private String prefixKey(String key) {
				return "auth-state:" + key;
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(StringRedisTemplate.class)
	CaptchaCacheService captchaCacheService(StringRedisTemplate stringRedisTemplate) {
		return new CaptchaCacheService() {
			@Override
			public void set(String key, String value, long expiresInSeconds) {
				stringRedisTemplate.opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
			}

			@Override
			public boolean exists(String key) {
				return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
			}

			@Override
			public void delete(String key) {
				stringRedisTemplate.delete(key);
			}

			@Override
			public String get(String key) {
				return stringRedisTemplate.opsForValue().get(key);
			}

			@Override
			public Long increment(String key, long val) {
				return stringRedisTemplate.opsForValue().increment(key, val);
			}

			@Override
			public String type() {
				return "redis";
			}
		};
	}

	@Bean
	@ConditionalOnBean(CaptchaService.class)
	SsoLoginTicketHandle captchaVerificationHandle(CaptchaService captchaService) {
		return () -> {
			CaptchaVO captchaVO = new CaptchaVO();
			captchaVO.setCaptchaVerification(SaHolder.getRequest().getParam("captchaVerification"));
			ResponseModel response = captchaService.verification(captchaVO);
			if (!response.isSuccess()) {
				throw new FailedLoginException(response.getRepMsg());
			}
		};
	}

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
