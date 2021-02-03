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

package pub.ihub.secure.oauth2.jwt;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import pub.ihub.secure.oauth2.jose.JoseHeader;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;

import java.util.Set;

/**
 * Jws编码器
 *
 * @author henry
 */
public interface JwtEncoder {

	/**
	 * 将JWT编码为紧凑的格式
	 *
	 * @param headers JOSE报头
	 * @param claims  JWT Claims Set
	 * @return Jwt
	 * @throws JwtException 异常类型
	 */
	Jwt encode(JoseHeader headers, JwtClaimsSet claims) throws JwtException;

	/**
	 * 发放jwt访问令牌
	 *
	 * @param subject 令牌服务主体
	 * @param client  注册客户端
	 * @param scopes  令牌作用域
	 * @return 令牌
	 * @throws JwtException jwt异常
	 */
	Jwt issueJwtAccessToken(String subject, RegisteredClient client, Set<String> scopes) throws JwtException;

	/**
	 * 发放ID令牌
	 *
	 * @param subject 令牌服务主体
	 * @param client  注册客户端
	 * @param nonce   随机数，用于客户端验证
	 * @return 令牌
	 * @throws JwtException jwt异常
	 */
	Jwt issueIdToken(String subject, RegisteredClient client, String nonce) throws JwtException;

}
