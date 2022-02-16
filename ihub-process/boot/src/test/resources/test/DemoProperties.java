package test;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static pub.ihub.core.Constant.PROPERTIES_PREFIX;

@ConfigurationProperties(PROPERTIES_PREFIX + ".demo")
public final class DemoProperties {

}
