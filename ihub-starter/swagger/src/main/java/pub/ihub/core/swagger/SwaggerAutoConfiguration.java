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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pub.ihub.core.swagger.SwaggerProperties.Header;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static springfox.documentation.builders.PathSelectors.ant;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

/**
 * Swagger自动配置
 *
 * @author liheng
 */
@Configuration
@EnableSwagger2
@ConditionalOnClass(Docket.class)
@EnableConfigurationProperties(SwaggerProperties.class)
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerAutoConfiguration {

	@Bean
	public Docket api(SwaggerProperties properties) {
		ApiSelectorBuilder builder = new Docket(SWAGGER_2)
			.host(properties.getHost())
			.useDefaultResponseMessages(false)
			.globalRequestParameters(properties.getHeaders().stream().map(Header::toRequestParameter).collect(toList()))
			.apiInfo(apiInfo(properties))
			.groupName(properties.getGroupName())
			.select();

		properties.getBasePackages().forEach(basePackage -> builder.apis(basePackage(basePackage)));
		properties.getBasePath().forEach(path -> builder.paths(ant(path)));
		properties.getExcludePath().forEach(p -> builder.paths(ant(p).negate()));

		Docket docket = builder.build();

		if (properties.getAuthorization().getEnabled()) {
			docket.securitySchemes(singletonList(properties.getAuthorization().getApiKey()));
			docket.securityContexts(singletonList(properties.getAuthorization().getSecurityContext()));
		}

		if (properties.getOauth2().getEnabled()) {
			docket.securitySchemes(singletonList(properties.getOauth2().getOauth()));
			docket.securityContexts(singletonList(properties.getOauth2().getSecurityContext()));
		}

		return docket;
	}

	private ApiInfo apiInfo(SwaggerProperties properties) {
		return new ApiInfoBuilder()
			.title(properties.getTitle())
			.description(properties.getDescription())
			.license(properties.getLicense())
			.licenseUrl(properties.getLicenseUrl())
			.termsOfServiceUrl(properties.getTermsOfServiceUrl())
			.contact(properties.getContact().getContact())
			.version(properties.getVersion())
			.build();
	}

}
