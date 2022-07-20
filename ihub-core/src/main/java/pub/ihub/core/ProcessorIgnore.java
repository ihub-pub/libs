package pub.ihub.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * 处理器忽略处理注解
 *
 * @author liheng
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface ProcessorIgnore {

}
