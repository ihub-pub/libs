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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static pub.ihub.core.Constant.PROPERTIES_PREFIX;

/**
 * 验证码配置属性
 *
 * @author liheng
 */
@Data
@ConfigurationProperties(PROPERTIES_PREFIX + ".sso.captcha")
public class SsoCaptchaProperties {

	/**
	 * 启用验证码
	 */
	private boolean enabled = true;

	/**
	 * 验证码类型
	 */
	private CaptchaType type = CaptchaType.GIF;

	/**
	 * 验证码位数
	 */
	private int codeCount = 5;

	/**
	 * 干扰线验证码-干扰线条数
	 */
	private int lineCount = 150;

	/**
	 * 扭曲干扰验证码-干扰元素个数
	 */
	private int thickness = 4;

	/**
	 * 圆圈干扰验证码-干扰圆圈条数
	 */
	private int circleCount = 15;

	enum CaptchaType {
		/**
		 * 圆圈干扰验证码
		 */
		CIRCLE,
		/**
		 * 干扰线验证码
		 */
		LINE,
		/**
		 * 扭曲干扰验证码
		 */
		SHEAR,
		/**
		 * 动态验证码
		 */
		GIF
	}

}
