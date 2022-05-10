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

import cn.dev33.satoken.config.SaSsoConfig;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.sso.SaSsoHandle;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.AuthRequestBuilder;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author liheng
 */
@AllArgsConstructor
@RestController
@Slf4j
public class SsoServerController {

	private final SsoServerProperties ssoProperties;
	private final AuthStateCache stateCache;
	private final SsoSocialUserService socialUserService;

	/**
	 * sso授权相关接口
	 *
	 * @return 响应
	 */
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
	public void renderAuth(@PathVariable String source, @RequestParam String redirect) {
		String state = AuthStateUtils.createState();
		stateCache.cache(redirectKey(state), redirect);
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
		var response = getAuthRequest(source).login(callback);
		if (response.ok()) {
			AuthUser authUser = (AuthUser) response.getData();
			if (StpUtil.isLogin()) {
				// 用户已登录：绑定第三方账号
				socialUserService.bingUserAndAuth(source, StpUtil.getLoginId(), authUser);
			} else {
				SsoUserDetails<?> user = socialUserService.findUserByUuid(source, authUser.getUuid());
				// 用户不存在：使用第三方信息自动创建用户
				if (Objects.isNull(user)) {
					user = socialUserService.createUserByAuth(source, authUser);
				}
				// 用户登录
				StpUtil.login(user.getLoginId());
			}
		}
		SaHolder.getResponse().redirect(stateCache.get(redirectKey(callback.getState())));
	}

	@Autowired
	private void configSso(SaSsoConfig cfg, @Autowired(required = false) List<SsoLoginTicketHandle> ticketHandles,
						   SsoUserDetailsService<?> userService) {
		cfg.setNotLoginView(() -> new ModelAndView("login.html", new HashMap<>(4) {{
			put("title", ssoProperties.getTitle());
			put("copyright", ssoProperties.getCopyright());
			put("icon", ssoProperties.getIcon());
			put("socialAuths", ssoProperties.getAuthSource());
		}}));

		cfg.setDoLoginHandle((name, pwd) -> {
			// 前置检查用于一些额外认证，如：验证码
			if (!ticketHandles.isEmpty()) {
				try {
					for (SsoLoginTicketHandle handle : ticketHandles) {
						handle.handle();
					}
				} catch (LoginException e) {
					return SaResult.error(e.getMessage());
				}
			}

			SsoUserDetails<?> user = userService.loadUserByUsername(name);
			if (Objects.isNull(user)) {
				log.debug("账号错误！");
				return SaResult.error("账号或者密码错误！");
			} else if (user.isAccountNonExpired()) {
				log.debug("账号已过期！");
			} else if (user.isAccountNonLocked()) {
				log.debug("账号已锁定！");
				return SaResult.error("账号已锁定！");
			} else if (!user.isEnabled()) {
				log.debug("账号已禁用！");
			} else if (user.getPassword().equals(userService.encryptPassword(pwd))) {
				StpUtil.login(user.getLoginId());
				log.debug("登录成功！");
				return SaResult.ok("登录成功！").setData(StpUtil.getTokenValue());
			} else {
				log.debug("密码错误！");
				return SaResult.error("账号或者密码错误！");
			}
			return SaResult.error("登录失败！");
		});
	}

	private AuthRequest getAuthRequest(String source) {
		return AuthRequestBuilder.builder().source(source).authConfig(ssoProperties.getAuthConfig(source))
			.authStateCache(stateCache).build();
	}

	private String redirectKey(String state) {
		return "redirect:" + state;
	}

}
