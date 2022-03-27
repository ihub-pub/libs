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
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.sso.SaSsoHandle;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import me.zhyd.oauth.AuthRequestBuilder;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

	@Autowired
	private void configSso(SaTokenConfig cfg, @Autowired(required = false) List<SsoLoginTicketHandle> ticketHandles,
						   SsoUserDetailsService<?> userService) {
		cfg.sso.setNotLoginView(() -> new ModelAndView("login.html", new HashMap<>(3) {{
			put("title", "IHub SSO 认证中心");
			put("background", getBingImage());
			put("copyright", "Copyright © " + LocalDate.now().getYear() + " IHub. All Rights Reserved.");
			put("socialAuths", ssoProperties.getAuthSource());
		}}));

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

	private AuthRequest getAuthRequest(String source) {
		return AuthRequestBuilder.builder().source(source).authConfig(ssoProperties.getAuthConfig(source))
			.authStateCache(stateCache).build();
	}

	private String redirectKey(String state) {
		return REDIRECT + ":" + state;
	}

	private String getBingImage() {
		String image = redisTemplate.opsForValue().get(getImageKey());
		if (CharSequenceUtil.isBlank(image)) {
			String body = HttpUtil.get("https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=zh-CN");
			var images = JSONUtil.parseObj(body).getBeanList("images", Map.class).stream().findFirst();
			image = images.map(map -> "https://cn.bing.com" + map.get("url").toString())
				.orElse("https://api.sunweihu.com/api/bing1/api.php");
			redisTemplate.opsForValue().set(getImageKey(), image, 2, TimeUnit.DAYS);
		}
		return image;
	}

	private String getImageKey() {
		return "bing:" + LocalDate.now();
	}

}
