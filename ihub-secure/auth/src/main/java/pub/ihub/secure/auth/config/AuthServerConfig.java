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

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.data.repository.Repository;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pub.ihub.core.ObjectBuilder;
import pub.ihub.secure.auth.repository.DynamicRegisteredClientRepository;
import pub.ihub.secure.auth.repository.PersistedRegisteredClientRepository;
import pub.ihub.secure.oauth2.jwt.JwtEncoder;
import pub.ihub.secure.oauth2.jwt.NimbusJwsEncoder;
import pub.ihub.secure.oauth2.server.InMemoryOAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.InMemoryRegisteredClientRepository;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.RegisteredClientRepository;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static cn.hutool.core.collection.CollUtil.newHashSet;
import static cn.hutool.core.lang.UUID.randomUUID;
import static cn.hutool.crypto.KeyUtil.generateKeyPair;
import static org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.BASIC;
import static org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID;
import static pub.ihub.secure.core.GrantType.AUTHORIZATION_CODE;
import static pub.ihub.secure.core.GrantType.CLIENT_CREDENTIALS;
import static pub.ihub.secure.core.GrantType.REFRESH_TOKEN;
import static pub.ihub.secure.oauth2.jwt.JoseHeader.RSA_KEY_TYPE;

/**
 * 授权服务配置
 *
 * @author liheng
 */
@EnableWebSecurity
@Import(OAuth2AuthorizationServerConfiguration.class)
@ComponentScan("pub.ihub.secure.auth.web")
public class AuthServerConfig implements WebMvcConfigurer {

	@Bean
	WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().antMatchers("/webjars/**");
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/login").setViewName("login");
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient registeredClient = ObjectBuilder.builder(RegisteredClient::new)
			.set(RegisteredClient::setId, randomUUID().toString())
			.set(RegisteredClient::setClientId, "messaging-client")
			.set(RegisteredClient::setClientSecret, "secret")
			.set(RegisteredClient::setClientAuthenticationMethods, newHashSet(BASIC))
			.set(RegisteredClient::setGrantTypes, newHashSet(AUTHORIZATION_CODE, REFRESH_TOKEN, CLIENT_CREDENTIALS))
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
	public OAuth2AuthorizationService auth2AuthorizationService() {
		return new InMemoryOAuth2AuthorizationService();
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		KeyPair keyPair = generateKeyPair(RSA_KEY_TYPE, 2048);
		JWKSet jwkSet = new JWKSet(new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
			.privateKey((RSAPrivateKey) keyPair.getPrivate())
			.keyID(randomUUID().toString())
			.build());
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	@Bean
	public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
		return new NimbusJwsEncoder(jwkSource);
	}

	@Bean
	@Order(BASIC_AUTH_ORDER - 5)
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeRequests().antMatchers("/login*").permitAll().anyRequest().authenticated()
			.and().formLogin().loginPage("/login");
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
