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

import static pub.ihub.core.Constant.PROPERTIES_PREFIX;

/**
 * 通用常量
 *
 * @author liheng
 */
public final class Constant {

	public static final String SECURE_PROPERTIES_PREFIX = PROPERTIES_PREFIX + ".secure";

	public static final String CLIENT_ID_OIDC = "ihub-oidc";
	public static final String CLIENT_ID_AUTHORIZATION_CODE = "ihub-authorization-code";
	public static final String CLIENT_ID_CLIENT_CREDENTIALS = "ihub-client-credentials";
	public static final String CLIENT_ID_INTERNAL = "ihub-internal";

	public static final String RESOURCE_INTERNAL_URI = "/internal";
	public static final String RESOURCE_SCOPES_ENDPOINT_URI = RESOURCE_INTERNAL_URI + "/scopes";
	public static final String RESOURCE_APIS_ENDPOINT_URI = RESOURCE_INTERNAL_URI + "/apis";

	public static final String SECURE_CLIENT_PROPERTIES_PREFIX = SECURE_PROPERTIES_PREFIX + ".client";
	public static final String SECURE_CLIENT_PROPERTIES_DOMAIN = SECURE_CLIENT_PROPERTIES_PREFIX + ".domain";
	public static final String SECURE_CLIENT_PROPERTIES_SECRET = SECURE_CLIENT_PROPERTIES_PREFIX + ".secret";
	public static final String SECURE_CLIENT_PROPERTIES_SCOPE = SECURE_CLIENT_PROPERTIES_PREFIX + ".scope";

}
