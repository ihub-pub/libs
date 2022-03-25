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
import lombok.Data;
import me.zhyd.oauth.config.AuthConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

import static pub.ihub.core.Constant.PROPERTIES_PREFIX;

/**
 * SSO服务配置属性
 *
 * @author liheng
 */
@Data
@ConfigurationProperties(PROPERTIES_PREFIX + ".sso")
public class SsoProperties {

	/**
	 * 第三方授权配置
	 */
	private Map<String, AuthConfig> socialAuthConfig = new HashMap<>();

	AuthConfig getAuthConfig(String source) {
		AuthConfig authConfig = socialAuthConfig.get(source);
		assert null != authConfig;
		if (CharSequenceUtil.isBlank(authConfig.getRedirectUri())) {
			SaRequest request = SaHolder.getRequest();
			authConfig.setRedirectUri(request.getUrl().replace(request.getRequestPath(), "") + "/oauth/callback/" + source);
		}
		return authConfig;
	}

}
