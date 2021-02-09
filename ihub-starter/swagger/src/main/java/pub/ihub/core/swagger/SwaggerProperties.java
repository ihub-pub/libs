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

import lombok.Data;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.AntPathMatcher;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.builders.TokenEndpointBuilder;
import springfox.documentation.builders.TokenRequestEndpointBuilder;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationCodeGrant;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.ClientCredentialsGrant;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.ImplicitGrant;
import springfox.documentation.service.LoginEndpoint;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.swagger.web.ApiKeyVehicle;

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
public final class SwaggerProperties {

	/**
	 * 分组名称
	 **/
	private String groupName = "default";
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
	 * 全局统一请求头
	 */
	private final List<Header> headers = new ArrayList<>();
	/**
	 * api key 认证
	 **/
	private final Authorization authorization = new Authorization();
	/**
	 * oauth2 认证
	 */
	private final Oauth2 oauth2 = new Oauth2();

	@Setter
	public static final class Contact {

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

		public springfox.documentation.service.Contact getContact() {
			return new springfox.documentation.service.Contact(name, url, email);
		}

	}

	@Setter
	public static final class Authorization {

		/**
		 * 开启Authorization，默认：false
		 */
		private Boolean enabled = false;
		/**
		 * 鉴权策略ID，对应 SecurityReferences ID，默认：Authorization
		 */
		private String name = "Authorization";
		/**
		 * 鉴权传递的Header参数，默认：TOKEN
		 */
		private String keyName = "TOKEN";
		/**
		 * 需要开启鉴权URL的正则，默认：/**
		 */
		private List<String> pathPatterns = singletonList("/**");

		public ApiKey getApiKey() {
			return new ApiKey(name, keyName, ApiKeyVehicle.HEADER.getValue());
		}

		public SecurityContext getSecurityContext() {
			return SecurityContext.builder()
				.securityReferences(singletonList(SecurityReference.builder()
					.reference(name)
					.scopes(new AuthorizationScope[]{new AuthorizationScope("global", "accessEverything")})
					.build()))
				.operationSelector(context -> pathPatterns.stream()
					.anyMatch(patterns -> new AntPathMatcher().match(patterns, context.requestMappingPattern())))
				.build();
		}

		public Boolean getEnabled() {
			return enabled;
		}

	}

	@Setter
	public static final class Oauth2 {

		/**
		 * 开启Oauth2，默认：false
		 */
		private Boolean enabled = false;
		/**
		 * oath2 名称，默认：oauth2
		 */
		private String name = "oauth2";
		/**
		 * clientId name
		 */
		private String clientIdName;
		/**
		 * clientSecret name
		 */
		private String clientSecretName;
		/**
		 * authorize url
		 */
		private String authorizeUrl;
		/**
		 * token url
		 */
		private String tokenUrl;
		/**
		 * token name，默认：access_token
		 */
		private String tokenName = "access_token";
		/**
		 * 授权类型
		 */
		private GrantTypes grantType = GrantTypes.AUTHORIZATION_CODE;
		/**
		 * oauth2 scope 列表
		 */
		private List<AuthorizationScope> scopes = new ArrayList<>();
		/**
		 * 需要开启鉴权URL的正则，默认：/**
		 */
		private List<String> pathPatterns = singletonList("/**");


		public OAuth getOauth() {
			return new OAuthBuilder()
				.name(name)
				.grantTypes(singletonList(grantType.getGrantType(this)))
				.build();
		}

		public SecurityContext getSecurityContext() {
			return SecurityContext.builder()
				.securityReferences(singletonList(new SecurityReference(name, scopes.stream()
					.map(AuthorizationScope::toAuthorizationScope)
					.toArray(springfox.documentation.service.AuthorizationScope[]::new))))
				.operationSelector(context -> pathPatterns.stream()
					.anyMatch(patterns -> new AntPathMatcher().match(patterns, context.requestMappingPattern())))
				.build();
		}

		public Boolean getEnabled() {
			return enabled;
		}

		/**
		 * oauth2 认证类型
		 */
		private enum GrantTypes {
			/**
			 * 授权码
			 */
			AUTHORIZATION_CODE,
			/**
			 * 客户端凭证
			 */
			CLIENT_CREDENTIALS,
			/**
			 * implicit
			 */
			IMPLICIT,
			/**
			 * 密码
			 */
			PASSWORD;

			public GrantType getGrantType(Oauth2 oauth2) {
				switch (this) {
					case AUTHORIZATION_CODE:
						return new AuthorizationCodeGrant(
							new TokenRequestEndpointBuilder()
								.url(oauth2.authorizeUrl)
								.clientIdName(oauth2.clientIdName)
								.clientSecretName(oauth2.clientSecretName)
								.build(),
							new TokenEndpointBuilder()
								.url(oauth2.tokenUrl)
								.tokenName(oauth2.tokenName)
								.build());
					case CLIENT_CREDENTIALS:
						return new ClientCredentialsGrant(oauth2.tokenUrl);
					case IMPLICIT:
						return new ImplicitGrant(new LoginEndpoint(oauth2.authorizeUrl), oauth2.tokenName);
					case PASSWORD:
						return new ResourceOwnerPasswordCredentialsGrant(oauth2.tokenUrl);
					default:
						return null;
				}
			}

		}

		@Setter
		private static class AuthorizationScope {

			/**
			 * 作用域名称
			 */
			private String scope = "";

			/**
			 * 作用域描述
			 */
			private String description = "";

			public springfox.documentation.service.AuthorizationScope toAuthorizationScope() {
				return new springfox.documentation.service.AuthorizationScope(scope, description);
			}

		}

	}

	@Setter
	public static final class Header {

		/**
		 * 请求头名
		 */
		private String name;
		/**
		 * 请求头描述
		 */
		private String description;
		/**
		 * 是否必须，默认：false
		 */
		private boolean required = false;

		public RequestParameter toRequestParameter() {
			return new RequestParameterBuilder()
				.in(ParameterType.HEADER).name(name).description(description).required(required).build();
		}

	}

}
