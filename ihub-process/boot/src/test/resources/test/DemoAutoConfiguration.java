package test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger自动配置
 *
 * @author liheng
 */
@Configuration
@EnableConfigurationProperties(DemoProperties.class)
public class DemoAutoConfiguration {

}
