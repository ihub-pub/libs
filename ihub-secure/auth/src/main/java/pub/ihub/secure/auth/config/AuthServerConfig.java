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

package pub.ihub.secure.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.data.repository.Repository;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.key.CryptoKeySource;
import org.springframework.security.crypto.key.StaticKeyGeneratingCryptoKeySource;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import pub.ihub.secure.auth.repository.DynamicRegisteredClientRepository;
import pub.ihub.secure.auth.repository.PersistedRegisteredClientRepository;

import java.util.UUID;

import static org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER;
import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 授权服务配置
 *
 * @author liheng
 */
@EnableWebSecurity
@Import(OAuth2AuthorizationServerConfiguration.class)
@ComponentScan("pub.ihub.secure.auth.web")
public class AuthServerConfig {

	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
			.clientId("messaging-client")
			.clientSecret("secret")
			.clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
			.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
			.redirectUri("http://localhost:8080/login/oauth2/code/messaging-client-oidc")
			.redirectUri("http://localhost:8080/authorized")
			.scope(OidcScopes.OPENID)
			.scope("message.read")
			.scope("message.write")
			.clientSettings(clientSettings -> clientSettings.requireUserConsent(true))
			.build();
		return new InMemoryRegisteredClientRepository(registeredClient);
	}

	@Bean
	@ConditionalOnClass(Repository.class)
	@ConditionalOnMissingBean(RegisteredClientRepository.class)
	public RegisteredClientRepository persistedRegisteredClientRepository() {
		// TODO 接入数据库
		return new PersistedRegisteredClientRepository();
	}

	@Bean
	@ConditionalOnMissingBean(RegisteredClientRepository.class)
	public RegisteredClientRepository dynamicRegisteredClientRepository() {
		return new DynamicRegisteredClientRepository();
	}

	@Bean
	public CryptoKeySource keySource() {
		return new StaticKeyGeneratingCryptoKeySource();
	}

	@Bean
	public ProviderSettings providerSettings() {
		return new ProviderSettings().issuer("http://auth-server:9000");
	}

	@Bean
	@Order(BASIC_AUTH_ORDER - 5)
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeRequests().antMatchers("/login*").permitAll().anyRequest().authenticated()
			.and()
//			.formLogin().loginPage("/login").loginProcessingUrl("/signin");
			.formLogin(withDefaults());
		return http.build();
	}

	@Bean
	UserDetailsService users() {
		// TODO 接入数据库
		UserDetails user = User.withDefaultPasswordEncoder()
			.username("user1")
			.password("password")
			.roles("USER")
			.build();
		return new InMemoryUserDetailsManager(user);
	}

}
