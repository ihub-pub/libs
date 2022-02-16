package test;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger自动配置
 *
 * @author liheng
 */
@Configuration
@ConditionalOnClass(OpenAPI.class)
@EnableConfigurationProperties(DocProperties.class)
public class DocAutoConfiguration {

	@Bean
	public OpenAPI openApi(DocProperties properties) {
		return properties.getOpenApi();
	}

}
