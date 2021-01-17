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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import lombok.Getter;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import pub.ihub.core.ObjectBuilder;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nimbusds.jwt.JWTClaimsSet.getRegisteredNames;

/**
 * JWT Claims Set是一个JSON对象，表示由JSON Web令牌传达的声明
 *
 * @author henry
 */
@Getter
public class JwtClaimsSet implements JwtClaimAccessor {

	private final Map<String, Object> claims;

	public JwtClaimsSet(Map<String, Object> claims) {
		this.claims = Collections.unmodifiableMap(new LinkedHashMap<>(claims));
	}

	public JWTClaimsSet buildJwtClaimsSet() {
		JWTClaimsSet.Builder builder = ObjectBuilder.builder(Builder::new)
			.set(Validator::isNotNull, Builder::issuer, getIssuer(), URL::toExternalForm)
			.set(StrUtil::isNotBlank, Builder::subject, getSubject())
			.set(CollUtil::isNotEmpty, Builder::audience, getAudience())
			.set(Validator::isNotNull, Builder::issueTime, getIssuedAt(), Date::from)
			.set(Validator::isNotNull, Builder::expirationTime, getExpiresAt(), Date::from)
			.set(Validator::isNotNull, Builder::notBeforeTime, getNotBefore(), Date::from)
			.set(StrUtil::isNotBlank, Builder::jwtID, getId())
			.build();
		Map<String, Object> customClaims = claims.entrySet().stream()
			.filter(claim -> !getRegisteredNames().contains(claim.getKey()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		if (MapUtil.isNotEmpty(customClaims)) {
			customClaims.forEach(builder::claim);
		}
		return builder.build();
	}

}
