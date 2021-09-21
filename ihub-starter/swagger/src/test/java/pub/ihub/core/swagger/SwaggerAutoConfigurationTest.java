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
package pub.ihub.core.swagger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pub.ihub.test.IHubFTConfig;

/**
 * @author henry
 */
@DisplayName("文档模块组件测试")
@IHubFTConfig
class SwaggerAutoConfigurationTest {

	@Test
	void swagger() {
	}

	@Nested()
	@DisplayName("关闭Swagger授权测试")
	@IHubFTConfig
	@SpringBootTest("ihub.swagger.authorization.enabled=false")
	class AuthorizationEnabled {

		@Test
		void swagger() {
		}

	}

	@Nested()
	@DisplayName("授权码认证类型测试")
	@IHubFTConfig
	@SpringBootTest({"ihub.swagger.oauth2.enabled=true", "ihub.swagger.oauth2.grantType=AUTHORIZATION_CODE"})
	class AuthorizationCode {

		@Test
		void grantType() {
		}

	}

	@Nested()
	@DisplayName("客户端凭证认证类型测试")
	@IHubFTConfig
	@SpringBootTest({"ihub.swagger.oauth2.enabled=true", "ihub.swagger.oauth2.grantType=CLIENT_CREDENTIALS"})
	class ClientCredentials {

		@Test
		void grantType() {
		}

	}

	@Nested()
	@DisplayName("隐式认证类型测试")
	@IHubFTConfig
	@SpringBootTest({"ihub.swagger.oauth2.enabled=true", "ihub.swagger.oauth2.grantType=IMPLICIT"})
	class Implicit {

		@Test
		void grantType() {
		}

	}

	@Nested()
	@DisplayName("密码认证类型测试")
	@IHubFTConfig
	@SpringBootTest({"ihub.swagger.oauth2.enabled=true", "ihub.swagger.oauth2.grantType=PASSWORD"})
	class Password {

		@Test
		void grantType() {
		}

	}

	@Nested()
	@DisplayName("作用域测试")
	@IHubFTConfig
	@SpringBootTest({
		"ihub.swagger.oauth2.enabled=true",
		"ihub.swagger.oauth2.scopes[0].scope=scope",
		"ihub.swagger.oauth2.scopes[0].description=description"
	})
	class AuthorizationScope {

		@Test
		void scope() {
		}

	}

	@Nested()
	@DisplayName("请求头测试")
	@IHubFTConfig
	@SpringBootTest({
		"ihub.swagger.headers[0].name=name",
		"ihub.swagger.headers[0].description=description"
	})
	class Header {

		@Test
		void header() {
		}

	}

}
