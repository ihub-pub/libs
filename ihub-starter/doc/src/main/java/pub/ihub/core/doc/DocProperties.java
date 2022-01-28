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
package pub.ihub.core.doc;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.Data;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static pub.ihub.core.Constant.PROPERTIES_PREFIX;

/**
 * Swagger配置属性
 *
 * @author liheng
 */
@Data
@ConfigurationProperties(PROPERTIES_PREFIX + ".doc")
public final class DocProperties {

	/**
	 * 分组名称
	 **/
	private String groupName;
	/**
	 * 标题
	 **/
	@Value("${spring.application.name:doc}-接口文档")
	private String title;
	/**
	 * 描述
	 **/
	private String description;
	/**
	 * 版本
	 **/
	@Value("${application.version:1.0.0}")
	private String version;
	/**
	 * 服务条款URL
	 **/
	private String termsOfService;
	/**
	 * 联系人信息
	 */
	private Contact contact = new Contact();
	/**
	 * 许可证
	 **/
	private License license = new License();

	OpenAPI getOpenApi() {
		return new OpenAPI()
			.info(new Info().title(title).description(description).termsOfService(termsOfService).version(version)
				.contact(contact.getContact()).license(license.getLicense()))
			.externalDocs(new ExternalDocumentation().description("More about SpringDoc").url("https://springdoc.org"));
	}

	@Setter
	static final class Contact {

		/**
		 * 联系人
		 **/
		private String name = "IHub";
		/**
		 * 联系人url
		 **/
		private String url = "https://github.com/ihub-pub";
		/**
		 * 联系人email
		 **/
		private String email = "henry.box@outlook.com";

		io.swagger.v3.oas.models.info.Contact getContact() {
			return new io.swagger.v3.oas.models.info.Contact().name(name).url(url).email(email);
		}

	}

	@Setter
	static final class License {

		/**
		 * 许可证名称
		 **/
		private String name;
		/**
		 * 许可证URL
		 **/
		private String url;

		io.swagger.v3.oas.models.info.License getLicense() {
			return new io.swagger.v3.oas.models.info.License().name(name).url(url);
		}

	}

}
