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
import cn.dev33.satoken.sso.SaSsoHandle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import me.zhyd.oauth.AuthRequestBuilder;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liheng
 */
@AllArgsConstructor
@RestController
public class SsoServerController {

	private final static String REDIRECT = "redirect";
	private final SsoProperties ssoProperties;
	private final RedisTemplate<String, String> redisTemplate;
	private final AuthStateCache stateCache;

	@RequestMapping("/sso/*")
	public Object ssoRequest() {
		return SaSsoHandle.serverRequest();
	}

	/**
	 * 获取授权链接并跳转到第三方授权页面
	 *
	 * @param source 第三方渠道
	 */
	@SneakyThrows
	@RequestMapping("/oauth/render/{source}")
	public void renderAuth(@PathVariable String source) {
		String state = AuthStateUtils.createState();
		stateCache.cache(redirectKey(state), SaHolder.getRequest().getParam(REDIRECT));
		String authorizeUrl = getAuthRequest(source).authorize(state);
		SaHolder.getResponse().redirect(authorizeUrl);
	}

	/**
	 * 用户在确认第三方平台授权（登录）后， 第三方平台会重定向到该地址，并携带code、state等参数
	 *
	 * @param source   第三方渠道
	 * @param callback 第三方回调时的入参
	 */
	@RequestMapping("/oauth/callback/{source}")
	public void login(@PathVariable String source, AuthCallback callback) {
		getAuthRequest(source).login(callback);
		// TODO 账号绑定
		SaHolder.getResponse().redirect(stateCache.get(redirectKey(callback.getState())));
	}

	private AuthRequest getAuthRequest(String source) {
		return AuthRequestBuilder.builder().source(source).authConfig(ssoProperties.getAuthConfig(source))
			.authStateCache(stateCache).build();
	}

	private String redirectKey(String state) {
		return REDIRECT + ":" + state;
	}

}
