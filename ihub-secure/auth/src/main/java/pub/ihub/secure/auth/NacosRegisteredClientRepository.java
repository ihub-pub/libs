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
package pub.ihub.secure.auth;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import lombok.SneakyThrows;
import org.springframework.core.env.PropertySource;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;

import java.util.Map;

import static cn.hutool.core.lang.UUID.randomUUID;
import static com.alibaba.cloud.nacos.NacosConfigProperties.COMMAS;
import static com.alibaba.cloud.nacos.parser.NacosDataParserHandler.getInstance;
import static com.alibaba.nacos.api.config.ConfigFactory.createConfigService;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.BASIC;
import static org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID;
import static pub.ihub.secure.core.Constant.SECURE_CLIENT_PROPERTIES_DOMAIN;
import static pub.ihub.secure.core.Constant.SECURE_CLIENT_PROPERTIES_SCOPE;
import static pub.ihub.secure.core.Constant.SECURE_CLIENT_PROPERTIES_SECRET;

/**
 * Nacos注册客户端存储库
 *
 * @author liheng
 */
public class NacosRegisteredClientRepository implements RegisteredClientRepository {

	private final ConfigService configService;
	private final NacosConfigProperties properties;

	@SneakyThrows
	public NacosRegisteredClientRepository(NacosConfigProperties properties) {
		configService = createConfigService(properties.getServerAddr());
		this.properties = properties;
	}

	@Override
	public void save(RegisteredClient registeredClient) {
		throw new UnsupportedOperationException("不支持保存Nacos注册客户端！");
	}

	@SneakyThrows
	@Override
	public RegisteredClient findById(String id) {
		throw new UnsupportedOperationException("不支持使用ID查询Nacos注册客户端！");
	}

	@SneakyThrows
	@Override
	public RegisteredClient findByClientId(String clientId) {
		PropertySource<?> propertySource = getInstance().parseNacosData(clientId, configService
			.getConfig(clientId, properties.getGroup(), properties.getTimeout()), properties.getFileExtension())
			.stream().findFirst().orElse(null);
		if (null != propertySource) {
			Map<String, String> source = (Map<String, String>) propertySource.getSource();
			String domain = source.get(SECURE_CLIENT_PROPERTIES_DOMAIN);
			RegisteredClient.Builder builder = RegisteredClient.withId(randomUUID().toString())
				.clientId(propertySource.getName())
				.clientSecret(source.get(SECURE_CLIENT_PROPERTIES_SECRET))
				.clientAuthenticationMethod(BASIC)
				.authorizationGrantType(AUTHORIZATION_CODE)
				.authorizationGrantType(REFRESH_TOKEN)
				.authorizationGrantType(CLIENT_CREDENTIALS)
				.redirectUri(domain + "/login/oauth2/code/ihub-oidc")
				.redirectUri(domain + "/authorized")
				.scope(OPENID)
				.scope("internal")
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build());
			for (String scope : source.get(SECURE_CLIENT_PROPERTIES_SCOPE).split(COMMAS)) {
				builder.scope(scope);
			}
			return builder.build();
		}
		return null;
	}

}
