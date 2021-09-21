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
package pub.ihub.secure.core;

import static pub.ihub.secure.core.GrantType.AUTHORIZATION_CODE;
import static pub.ihub.secure.core.GrantType.CLIENT_CREDENTIALS;
import static pub.ihub.secure.core.GrantType.PASSWORD;
import static pub.ihub.secure.core.GrantType.REFRESH_TOKEN;

/**
 * 授权使用场景
 *
 * @author liheng
 */
public enum AuthUseScene {

	/**
	 * 浏览器
	 */
	WEB(AUTHORIZATION_CODE, REFRESH_TOKEN),
	/**
	 * 移动设备(手机)
	 */
	CELL_PHONE(PASSWORD, REFRESH_TOKEN),
	/**
	 * 其他设备
	 */
	DEVICE_CLIENT(PASSWORD, REFRESH_TOKEN),
	/**
	 * 服务端
	 */
	SERVER(CLIENT_CREDENTIALS);

	/**
	 * 授权方式
	 */
	private final GrantType[] grantTypes;

	AuthUseScene(GrantType... grantTypes) {
		this.grantTypes = grantTypes;
	}

	public GrantType[] getGrantTypes() {
		return grantTypes;
	}

}
