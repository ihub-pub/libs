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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

import static pub.ihub.secure.core.Constant.SECURE_PROPERTIES_PREFIX;

/**
 * 资源服务配置属性
 *
 * @author liheng
 */
@Data
@ConfigurationProperties(SECURE_PROPERTIES_PREFIX + ".resource")
public final class AuthResourceProperties {

	/**
	 * 自定义条件资源映射
	 */
	private List<AccessResources> accessResources = CollUtil.empty(List.class);

	/**
	 * 作用域方法资源映射
	 */
	private Map<String, Map<HttpMethod, String[]>> scopeMethodResources = MapUtil.empty();

	/**
	 * 作用域资源映射
	 */
	private Map<String, String[]> scopeResources = MapUtil.empty();

	@Data
	public static final class AccessResources {

		private String access;
		private String[] resources;

	}

}
