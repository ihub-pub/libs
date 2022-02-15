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
package pub.ihub.core;

import cn.hutool.core.map.MapUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

import static pub.ihub.core.IHubLibsVersion.getVersion;

/**
 * 自定义文件集配置处理器
 *
 * @author liheng
 */
public abstract class BaseConfigEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	/**
	 * 默认顺序
	 */
	public static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER;
	/**
	 * 自定义属性源名称
	 */
	public static final String CUSTOMIZE_PROPERTY_SOURCE_NAME = "customize";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String profile = getActiveProfile();
		if (null != profile) {
			environment.addActiveProfile(profile);
		}
		Map<String, Object> properties = getCustomizeProperties();
		if (null != properties) {
			environment.getPropertySources().addLast(new MapPropertySource(CUSTOMIZE_PROPERTY_SOURCE_NAME, properties));
		}
	}

	@Override
	public int getOrder() {
		return ORDER;
	}

	/**
	 * 配置文件集
	 *
	 * @return 文件集名称
	 */
	protected String getActiveProfile() {
		return null;
	}

	/**
	 * 获取自定义属性
	 *
	 * @return 自定义属性
	 */
	protected Map<String, Object> getCustomizeProperties() {
		return MapUtil.<String, Object>builder("ihub-libs.version", getVersion()).build();
	}

}
