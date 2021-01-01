package pub.ihub.core.swagger;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
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
@EnableConfigurationProperties(SwaggerProperties.class)
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerAutoConfiguration {

	@Bean
	public Docket api(SwaggerProperties swaggerProperties) {
		ApiSelectorBuilder builder = new Docket(SWAGGER_2)
			.host(swaggerProperties.getHost())
			.apiInfo(apiInfo(swaggerProperties))
			.groupName(swaggerProperties.getGroupName())
			.select();

		swaggerProperties.getBasePackages().forEach(basePackage -> builder.apis(basePackage(basePackage)));
		swaggerProperties.getBasePath().forEach(path -> builder.paths(ant(path)));
		swaggerProperties.getExcludePath().forEach(p -> builder.paths(ant(p).negate()));

		SwaggerProperties.Authorization authorization = swaggerProperties.getAuthorization();
		return builder.build()
			.securitySchemes(singletonList(securitySchema(authorization)))
			.securityContexts(singletonList(securityContext(authorization)))
			.securityContexts(newArrayList(securityContext(authorization)))
			.securitySchemes(singletonList(securitySchema(authorization)))
			.pathMapping("/");
	}

	private SecurityContext securityContext(SwaggerProperties.Authorization authorization) {
		return SecurityContext.builder()
			.securityReferences(defaultAuth(authorization))
			.operationSelector(context -> context.requestMappingPattern().matches(authorization.getAuthRegex()))
			.build();
	}

	private List<SecurityReference> defaultAuth(SwaggerProperties.Authorization authorization) {
		List<AuthorizationScope> authorizationScopeList = newScopes(authorization);
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[authorizationScopeList.size()];
		return singletonList(SecurityReference.builder()
			.reference(authorization.getName())
			.scopes(authorizationScopeList.toArray(authorizationScopes))
			.build());
	}

	private OAuth securitySchema(SwaggerProperties.Authorization authorization) {
		return new OAuth(authorization.getName(), newScopes(authorization), authorization.getTokenUrlList()
			.stream().map(ResourceOwnerPasswordCredentialsGrant::new).collect(toList()));
	}

	private ApiInfo apiInfo(SwaggerProperties swaggerProperties) {
		return new ApiInfoBuilder()
			.title(swaggerProperties.getTitle())
			.description(swaggerProperties.getDescription())
			.license(swaggerProperties.getLicense())
			.licenseUrl(swaggerProperties.getLicenseUrl())
			.termsOfServiceUrl(swaggerProperties.getTermsOfServiceUrl())
			.contact(new Contact(
				swaggerProperties.getContact().getName(),
				swaggerProperties.getContact().getUrl(),
				swaggerProperties.getContact().getEmail()))
			.version(swaggerProperties.getVersion())
			.build();
	}

	private static List<AuthorizationScope> newScopes(SwaggerProperties.Authorization authorization) {
		return authorization.getAuthorizationScopeList().stream()
			.map(scope -> new AuthorizationScope(scope.getScope(), scope.getDescription())).collect(toList());
	}

}
