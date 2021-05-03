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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static pub.ihub.core.Constant.PROPERTIES_PREFIX;

/**
 * 服务配置属性
 *
 * @author liheng
 */
@Data
@ConfigurationProperties(PROPERTIES_PREFIX + ".application")
public class IHubApplicationProperties {

}
