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

package pub.ihub.secure.core;

/**
 * 通用常量
 *
 * @author liheng
 */
public final class Constant {

	public static final String PROPERTIES_PREFIX = pub.ihub.core.Constant.PROPERTIES_PREFIX + ".secure";

	public static final String CLIENT_ID_OIDC = "ihub-oidc";
	public static final String CLIENT_ID_AUTHORIZATION_CODE = "ihub-authorization-code";
	public static final String CLIENT_ID_CLIENT_CREDENTIALS = "ihub-client-credentials";
	public static final String CLIENT_ID_INTERNAL = "ihub-internal";

	public static final String DEFAULT_OIDC_PROVIDER_CONFIGURATION_ENDPOINT_URI = "/.well-known/openid-configuration";
	public static final String DEFAULT_JWK_SET_ENDPOINT_URI = "/oauth2/jwks";
	public static final String DEFAULT_AUTHORIZATION_ENDPOINT_URI = "/oauth2/authorize";
	public static final String DEFAULT_TOKEN_ENDPOINT_URI = "/oauth2/token";
	public static final String DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI = "/oauth2/revoke";

	public static final String RESOURCE_SCOPES_ENDPOINT_URI = "/oauth2/scopes";

}
