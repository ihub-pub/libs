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

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import me.zhyd.oauth.cache.AuthCacheConfig;
import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author liheng
 */
@Configuration
@EnableConfigurationProperties(SsoProperties.class)
public class SsoServerAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(SsoUserDetailsService.class)
	SsoUserDetailsService<Long> defaultUserDetailsService() {
		// 默认实现，实际环境需要重新实现
		return username -> new SsoUserDetails<>() {
			@Override
			public Long getLoginId() {
				return 10001L;
			}

			@Override
			public String getUsername() {
				return username;
			}

			@Override
			public String getPassword() {
				return "123456";
			}
		};
	}

	@Autowired
	private void configSso(SaTokenConfig cfg, @Autowired(required = false) List<SsoLoginTicketHandle> ticketHandles,
						   SsoUserDetailsService<?> userService) {
		cfg.sso.setNotLoginView(() -> new ModelAndView("login.html"));

		cfg.sso.setDoLoginHandle((name, pwd) -> {
			// 前置检查用于一些额外认证
			assert ticketHandles.isEmpty() || ticketHandles.stream()
				.anyMatch(h -> h.handle(StpUtil.getSession().getId()));

			SsoUserDetails<?> user = userService.loadUserByUsername(name);
			// TODO 其他失败判断
			if (Objects.isNull(user)) {
				return SaResult.error("登录失败！");
			} else if (user.getPassword().equals(userService.encryptPassword(pwd))) {
				StpUtil.login(user.getLoginId());
				return SaResult.ok("登录成功！").setData(StpUtil.getTokenValue());
			}
			return SaResult.error("登录失败！");
		});
	}

	@Bean
	@ConditionalOnMissingBean(AuthStateCache.class)
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

}
