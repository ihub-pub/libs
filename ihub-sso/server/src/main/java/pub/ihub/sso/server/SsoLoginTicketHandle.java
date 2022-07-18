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

import javax.security.auth.login.LoginException;

/**
 * 登录验证处理接口
 *
 * @author liheng
 */
public interface SsoLoginTicketHandle {

	/**
	 * 处理方法
	 *
	 * @throws LoginException 登录异常
	 */
	void handle() throws LoginException;

}
