package pub.ihub.core.swagger;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static pub.ihub.core.Constant.BASE_PACKAGES;
import static pub.ihub.core.Constant.PROPERTIES_PREFIX;

/**
 * Swagger配置属性
 *
 * @author liheng
 */
@Data
@ConfigurationProperties(PROPERTIES_PREFIX + ".swagger")
public class SwaggerProperties {

    /**
     * swagger解析包路径
     **/
    private List<String> basePackages = singletonList(BASE_PACKAGES);
    /**
     * swagger解析的url规则
     **/
    private List<String> basePath = singletonList("/**");
    /**
     * 在basePath基础上需要排除的url规则
     **/
    private List<String> excludePath = asList("/error", "/actuator/**");
    /**
     * 标题
     **/
    private String title = "IHub 接口文档";
    /**
     * 描述
     **/
    private String description = "IHub 接口文档";
    /**
     * 版本
     **/
    private String version = "1.0.0";
    /**
     * 许可证
     **/
    private String license = "Powered By IHub";
    /**
     * 许可证URL
     **/
    private String licenseUrl = "https://ihub.pub";
    /**
     * 服务条款URL
     **/
    private String termsOfServiceUrl = "https://ihub.pub";

    /**
     * host信息
     **/
    private String host = "localhost";
    /**
     * 联系人信息
     */
    private Contact contact = new Contact();
    /**
     * 全局统一鉴权配置
     **/
    private Authorization authorization = new Authorization();
    /**
     * 分组名称
     **/
    private String groupName = "默认";

    @Data
    @NoArgsConstructor
    public static class Contact {

        /**
         * 联系人
         **/
        private String name = "henry";
        /**
         * 联系人url
         **/
        private String url = "https://github.com/henry-hub";
        /**
         * 联系人email
         **/
        private String email = "henry.box@outlook.com";

    }

    @Data
    @NoArgsConstructor
    public static class Authorization {

        /**
         * 鉴权策略ID，需要和SecurityReferences ID保持一致
         */
        private String name = "";

        /**
         * 需要开启鉴权URL的正则
         */
        private String authRegex = "^.*$";

        /**
         * 鉴权作用域列表
         */
        private List<AuthorizationScope> authorizationScopeList = new ArrayList<>();

        private List<String> tokenUrlList = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class AuthorizationScope {

        /**
         * 作用域名称
         */
        private String scope = "";

        /**
         * 作用域描述
         */
        private String description = "";

    }

}
