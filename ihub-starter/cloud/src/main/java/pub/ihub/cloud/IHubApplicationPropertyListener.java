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

package pub.ihub.cloud;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.springframework.boot.WebApplicationType.NONE;
import static org.springframework.boot.context.config.ConfigFileApplicationListener.CONFIG_NAME_PROPERTY;
import static org.springframework.boot.context.config.ConfigFileApplicationListener.DEFAULT_ORDER;
import static org.springframework.boot.context.properties.bind.Binder.get;
import static pub.ihub.core.Constant.SPRING_CONFIG_NAMES;
import static pub.ihub.core.IHubLibsVersion.getVersion;

/**
 * @author liheng
 */
public class IHubApplicationPropertyListener implements
	ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		if (NONE == event.getSpringApplication().getWebApplicationType()) {
			return;
		}
		ConfigurableEnvironment environment = event.getEnvironment();
		setProperty(environment, CONFIG_NAME_PROPERTY, SPRING_CONFIG_NAMES);
		setProperty(environment, "ihub-libs.version", getVersion());
	}

	private void setProperty(ConfigurableEnvironment environment, String key, String value) {
		if (!get(environment).bind(key, String.class).isBound()) {
			System.getProperties().putIfAbsent(key, value);
		}
	}

	@Override
	public int getOrder() {
		return DEFAULT_ORDER - 10;
	}

}
