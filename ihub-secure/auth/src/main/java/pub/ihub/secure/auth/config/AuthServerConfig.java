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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import pub.ihub.core.ObjectBuilder;
import pub.ihub.secure.auth.repository.DynamicRegisteredClientRepository;
import pub.ihub.secure.auth.repository.PersistedRegisteredClientRepository;
import pub.ihub.secure.crypto.CryptoKeySource;
import pub.ihub.secure.crypto.StaticKeyGeneratingCryptoKeySource;
import pub.ihub.secure.oauth2.server.InMemoryRegisteredClientRepository;
import pub.ihub.secure.oauth2.server.RegisteredClientRepository;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;

import static cn.hutool.core.collection.CollUtil.newHashSet;
import static cn.hutool.core.lang.UUID.randomUUID;
import static org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.BASIC;
import static org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID;

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
		RegisteredClient registeredClient = ObjectBuilder.builder(RegisteredClient::new)
			.set(RegisteredClient::setId, randomUUID().toString())
			.set(RegisteredClient::setClientId, "messaging-client")
			.set(RegisteredClient::setClientSecret, "secret")
			.set(RegisteredClient::setClientAuthenticationMethods, newHashSet(BASIC))
			.set(RegisteredClient::setAuthorizationGrantTypes,
				newHashSet(AUTHORIZATION_CODE, REFRESH_TOKEN, CLIENT_CREDENTIALS))
			.set(RegisteredClient::setRedirectUris, newHashSet(
				"http://localhost:8080/login/oauth2/code/messaging-client-oidc",
				"http://localhost:8080/authorized"))
			.set(RegisteredClient::setScopes, newHashSet(OPENID, "message.read", "message.write"))
			.set(RegisteredClient::setRequireUserConsent, true)
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
