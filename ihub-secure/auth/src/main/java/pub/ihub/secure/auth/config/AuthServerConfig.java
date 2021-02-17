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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static cn.hutool.core.lang.UUID.randomUUID;
import static cn.hutool.crypto.SecureUtil.generateKeyPair;

/**
 * 授权服务配置
 *
 * @author liheng
 */
@EnableWebSecurity
@Import(OAuth2AuthorizationServerConfiguration.class)
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
		RegisteredClient registeredClient = RegisteredClient.withId(randomUUID().toString())
			.clientId("messaging-client")
			.clientSecret("secret")
			.clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
			.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
			.redirectUri("http://localhost:8080/login/oauth2/code/ihub-oidc")
			.redirectUri("http://localhost:8080/authorized")
			.scope(OidcScopes.OPENID)
			.scope("message.read")
			.scope("message.write")
			.scope("internal")
			.clientSettings(clientSettings -> clientSettings.requireUserConsent(true))
			.build();
		return new InMemoryRegisteredClientRepository(registeredClient);
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		KeyPair keyPair = generateKeyPair("RSA", 2048);
		JWKSet jwkSet = new JWKSet(new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
			.privateKey((RSAPrivateKey) keyPair.getPrivate())
			.keyID(randomUUID().toString())
			.build());
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	@Bean
	public ProviderSettings providerSettings() {
		return new ProviderSettings().issuer("http://auth-server:9527");
	}

	@Bean
	SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeRequests().antMatchers("/login").permitAll().anyRequest().authenticated()
			.and().formLogin().loginPage("/login");
		return http.build();
	}

	@Bean
	@ConditionalOnMissingBean(UserDetailsService.class)
	UserDetailsService users() {
		return new InMemoryUserDetailsManager(User.withDefaultPasswordEncoder()
			.username("admin")
			.password("admin")
			.roles("ADMIN")
			.build());
	}

}
