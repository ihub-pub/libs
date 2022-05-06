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
package pub.ihub.sso.resource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;

import static pub.ihub.core.Constant.PROPERTIES_PREFIX;

/**
 * SSO资源服务配置属性
 *
 * @author liheng
 */
@Data
@ConfigurationProperties(PROPERTIES_PREFIX + ".sso")
public class SsoResourceProperties {

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

}
