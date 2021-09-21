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
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
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
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * 授权服务配置
 *
 * @author liheng
 */
@EnableWebSecurity
public class AuthServerConfig implements WebMvcConfigurer {

	@Bean
	WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().antMatchers("/webjars/**");
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/login").setViewName("login");
	}

	@SneakyThrows
	@Bean
	public RegisteredClientRepository registeredClientRepository(NacosConfigProperties properties) {
		return new NacosRegisteredClientRepository(properties);
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
	public ProviderSettings providerSettings(@Value("${ihub.application.auth-server-addr}") String authServerAddr) {
		return ProviderSettings.builder().issuer(authServerAddr).build();
	}

	@Bean
	public OAuth2AuthorizationConsentService authorizationConsentService() {
		return new InMemoryOAuth2AuthorizationConsentService();
	}

	@Bean
	@Order(HIGHEST_PRECEDENCE)
	SecurityFilterChain securityServerFilterChain(HttpSecurity http) throws Exception {
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
