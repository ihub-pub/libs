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
package pub.ihub.doc;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author henry
 */
@DisplayName("文档模块组件测试")
class DocAutoConfigurationTest {

	@DisplayName("文档自动配置测试")
	@Test
	void doc() {
		DocAutoConfiguration config = new DocAutoConfiguration();
		assertNotNull(config);
	}

	@DisplayName("测试OpenAPI Bean创建")
	@Test
	void testOpenApiBean() {
		DocAutoConfiguration config = new DocAutoConfiguration();
		DocProperties properties = new DocProperties();
		properties.setTitle("Test API");
		properties.setDescription("Test Description");
		properties.setVersion("1.0.0");

		OpenAPI openAPI = config.openApi(properties);
		assertNotNull(openAPI);
		assertNotNull(openAPI.getInfo());
		assertNotNull(openAPI.getExternalDocs());
		assertEquals("Test API", openAPI.getInfo().getTitle());
		assertEquals("Test Description", openAPI.getInfo().getDescription());
		assertEquals("1.0.0", openAPI.getInfo().getVersion());
	}

	@DisplayName("测试DocProperties属性")
	@Test
	void testDocProperties() {
		DocProperties properties = new DocProperties();
		properties.setTitle("Test API");
		properties.setDescription("Test Description");
		properties.setVersion("1.0.0");
		properties.setGroupName("test-group");
		properties.setTermsOfService("https://example.com/terms");

		assertEquals("Test API", properties.getTitle());
		assertEquals("Test Description", properties.getDescription());
		assertEquals("1.0.0", properties.getVersion());
		assertEquals("test-group", properties.getGroupName());
		assertEquals("https://example.com/terms", properties.getTermsOfService());
	}

	@DisplayName("测试Contact配置")
	@Test
	void testContact() {
		DocProperties.Contact contact = new DocProperties.Contact();
		contact.setName("Test Contact");
		contact.setUrl("https://example.com");
		contact.setEmail("test@example.com");

		io.swagger.v3.oas.models.info.Contact swaggerContact = contact.getContact();
		assertNotNull(swaggerContact);
		assertEquals("Test Contact", swaggerContact.getName());
		assertEquals("https://example.com", swaggerContact.getUrl());
		assertEquals("test@example.com", swaggerContact.getEmail());
	}

	@DisplayName("测试License配置")
	@Test
	void testLicense() {
		DocProperties.License license = new DocProperties.License();
		license.setName("Apache 2.0");
		license.setUrl("https://www.apache.org/licenses/LICENSE-2.0");

		io.swagger.v3.oas.models.info.License swaggerLicense = license.getLicense();
		assertNotNull(swaggerLicense);
		assertEquals("Apache 2.0", swaggerLicense.getName());
		assertEquals("https://www.apache.org/licenses/LICENSE-2.0", swaggerLicense.getUrl());
	}

	@DisplayName("测试完整OpenAPI配置")
	@Test
	void testFullOpenApiConfiguration() {
		DocProperties properties = new DocProperties();
		properties.setTitle("Full API");
		properties.setDescription("Full Description");
		properties.setVersion("2.0.0");
		properties.setTermsOfService("https://example.com/terms");

		DocProperties.Contact contact = new DocProperties.Contact();
		contact.setName("Contact Name");
		contact.setUrl("https://contact.example.com");
		contact.setEmail("contact@example.com");
		properties.setContact(contact);

		DocProperties.License license = new DocProperties.License();
		license.setName("MIT License");
		license.setUrl("https://opensource.org/licenses/MIT");
		properties.setLicense(license);

		DocAutoConfiguration config = new DocAutoConfiguration();
		OpenAPI openAPI = config.openApi(properties);

		assertNotNull(openAPI);
		assertNotNull(openAPI.getInfo());
		assertEquals("Full API", openAPI.getInfo().getTitle());
		assertEquals("Full Description", openAPI.getInfo().getDescription());
		assertEquals("2.0.0", openAPI.getInfo().getVersion());
		assertEquals("https://example.com/terms", openAPI.getInfo().getTermsOfService());

		assertNotNull(openAPI.getInfo().getContact());
		assertEquals("Contact Name", openAPI.getInfo().getContact().getName());

		assertNotNull(openAPI.getInfo().getLicense());
		assertEquals("MIT License", openAPI.getInfo().getLicense().getName());
	}

}
