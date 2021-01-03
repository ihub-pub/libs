package pub.ihub.core.swagger;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pub.ihub.core.swagger.SwaggerProperties.Header;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;

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
@ConditionalOnClass(Docket.class)
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnMissingClass("org.springframework.cloud.gateway.config.GatewayAutoConfiguration")
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
