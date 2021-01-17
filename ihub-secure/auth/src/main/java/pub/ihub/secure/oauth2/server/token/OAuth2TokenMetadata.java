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
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * 保存与OAuth 2.0令牌关联的元数据。
 *
 * @author henry
 */
@Getter
@EqualsAndHashCode
public class OAuth2TokenMetadata implements Serializable {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	protected static final String TOKEN_METADATA_BASE = "metadata.token.";

	public static final String INVALIDATED = TOKEN_METADATA_BASE.concat("invalidated");

	private final Map<String, Object> metadata;

	protected OAuth2TokenMetadata(Map<String, Object> metadata) {
		this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
	}

	public boolean isInvalidated() {
		return getMetadata(INVALIDATED);
	}

	@SuppressWarnings("unchecked")
	public <T> T getMetadata(String name) {
		Assert.hasText(name, "name cannot be empty");
		return (T) this.metadata.get(name);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder implements Serializable {

		private static final long serialVersionUID = SERIAL_VERSION_UID;
		private final Map<String, Object> metadata = defaultMetadata();

		protected Builder() {
		}

		public Builder invalidated() {
			metadata(INVALIDATED, true);
			return this;
		}

		public Builder metadata(String name, Object value) {
			Assert.hasText(name, "name cannot be empty");
			Assert.notNull(value, "value cannot be null");
			this.metadata.put(name, value);
			return this;
		}

		public Builder metadata(Consumer<Map<String, Object>> metadataConsumer) {
			metadataConsumer.accept(this.metadata);
			return this;
		}

		public OAuth2TokenMetadata build() {
			return new OAuth2TokenMetadata(this.metadata);
		}

		protected static Map<String, Object> defaultMetadata() {
			Map<String, Object> metadata = new HashMap<>();
			metadata.put(INVALIDATED, false);
			return metadata;
		}
	}

}
