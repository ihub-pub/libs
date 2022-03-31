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
package pub.ihub.sso.client;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.id.SaIdUtil;
import cn.dev33.satoken.stp.StpUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * @author liheng
 */
@Component
public class FeignInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate requestTemplate) {
		// 传递登录状态
		requestTemplate.header(SaManager.config.getTokenName(), StpUtil.getTokenValue());
		// 内部调用隔离token
		requestTemplate.header(SaIdUtil.ID_TOKEN, SaIdUtil.getToken());
	}

}
