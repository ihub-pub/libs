/*
 * Copyright (c) 2021 Henry 李恒 (henry.box@outlook.com).
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

package pub.ihub.secure.resource;

import cn.hutool.core.map.MapUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

import static pub.ihub.core.Constant.PROPERTIES_PREFIX;

/**
 * 资源服务配置属性
 *
 * @author liheng
 */
@Data
@ConfigurationProperties(PROPERTIES_PREFIX + ".resource")
public class AuthResourceProperties {

	/**
	 * 授权服务
	 */
	private String providerIssuer;

	/**
	 * 拥有资源作用域（匹配优先级高）
	 */
	private Map<String, String> resourceScope = MapUtil.empty();

	/**
	 * 作用域拥有资源（匹配优先级高）
	 */
	private Map<String, String> scopeResource = MapUtil.empty();

	/**
	 * 拥有资源作用域
	 */
	private Map<String, List<String>> resourceScopes = MapUtil.empty();

	/**
	 * 作用域拥有资源
	 */
	private Map<String, List<String>> scopeResources = MapUtil.empty();

	/**
	 * 复杂条件资源
	 */
	private Map<String, List<String>> accessResources = MapUtil.empty();

}
