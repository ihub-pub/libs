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
import cn.dev33.satoken.context.model.SaRequest;
import cn.hutool.core.text.CharSequenceUtil;
import me.zhyd.oauth.config.AuthConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pub.ihub.core.Constant.PROPERTIES_PREFIX;

/**
 * SSO授权服务配置属性
 *
 * @author liheng
 */
@ConfigurationProperties(PROPERTIES_PREFIX + ".sso")
public final class SsoServerProperties {

	/**
	 * 标题
	 */
	private String title = "IHub SSO 认证中心";

	/**
	 * 版权
	 */
	private String copyright = "Copyright © " + LocalDate.now().getYear() + " IHub. All Rights Reserved.";

	/**
	 * 图标
	 */
	private String icon = "https://cdn.jsdelivr.net/gh/ihub-pub/ihub-pub.github.io/favicon.ico";

	/**
	 * 第三方授权配置
	 */
	private Map<String, AuthConfig> socialAuthConfig = new HashMap<>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Map<String, AuthConfig> getSocialAuthConfig() {
		return socialAuthConfig;
	}

	public void setSocialAuthConfig(Map<String, AuthConfig> socialAuthConfig) {
		this.socialAuthConfig = socialAuthConfig;
	}

	AuthConfig getAuthConfig(String source) {
		AuthConfig authConfig = socialAuthConfig.get(source);
		assert null != authConfig;
		if (CharSequenceUtil.isBlank(authConfig.getRedirectUri())) {
			SaRequest request = SaHolder.getRequest();
			authConfig.setRedirectUri(request.getUrl().replace(request.getRequestPath(), "") + "/oauth/callback/" + source);
		}
		return authConfig;
	}

	List<Map<String, String>> getAuthSource() {
		return socialAuthConfig.keySet().stream().map(source -> new HashMap<String, String>(1) {{
			put("icon", source.replace("_", "-").toLowerCase());
			put("source", source);
		}}).collect(Collectors.toList());
	}

}
