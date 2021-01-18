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

package pub.ihub.secure.oauth2.server.token;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.lang.Assert.notBlank;
import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 保存与OAuth 2.0令牌关联的元数据。
 *
 * @author henry
 */
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class OAuth2TokenMetadata implements Serializable {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	protected static final String TOKEN_METADATA_BASE = "metadata.token.";
	public static final String INVALIDATED = TOKEN_METADATA_BASE.concat("invalidated");

	private final Map<String, Object> metadata = new HashMap<>() {
		{
			put(INVALIDATED, false);
		}
	};

	public boolean isInvalidated() {
		return getMetadata(INVALIDATED);
	}

	@SuppressWarnings("unchecked")
	public <T> T getMetadata(String name) {
		return (T) this.metadata.get(notBlank(name));
	}

}
