/*
 * Copyright (c) 2022 Henry 李恒 (henry.box@outlook.com).
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
package pub.ihub.sso.server;

import me.zhyd.oauth.config.AuthDefaultSource;

/**
 * 社会化用户
 *
 * @author liheng
 */
public class SocialUser {

	private int id;//	主键
	private String uuid;//	第三方系统的唯一ID
	private AuthDefaultSource source;//	第三方用户来源
	private String access_token;//	用户的授权令牌
	private int expire_in;//	第三方用户的授权令牌的有效期
	private String refresh_token;//	刷新令牌
	private String open_id;//	第三方用户的 open id
	private String uid;//	第三方用户的 ID
	private String access_code;//	个别平台的授权信息
	private String union_id;//	第三方用户的 union id
	private String scope;//	第三方用户授予的权限
	private String token_type;//	个别平台的授权信息
	private String id_token;//	id token
	private String mac_algorithm;//	小米平台用户的附带属性
	private String mac_key;//	小米平台用户的附带属性
	private String code;//	用户的授权code
	private String oauth_token;//	Twitter平台用户的附带属性
	private String oauth_token_secret;//	Twitter平台用户的附带属性

}
