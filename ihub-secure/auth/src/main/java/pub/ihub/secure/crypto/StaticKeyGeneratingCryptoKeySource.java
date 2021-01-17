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

package pub.ihub.secure.crypto;

import pub.ihub.secure.crypto.CryptoKey.AsymmetricKey;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pub.ihub.secure.crypto.CryptoKey.asymmetric;
import static pub.ihub.secure.crypto.CryptoKey.symmetric;

/**
 * 静态密钥源
 * TODO 暂用
 *
 * @author henry
 */
public class StaticKeyGeneratingCryptoKeySource implements CryptoKeySource {

	private final List<CryptoKey<?>> keys;

	public StaticKeyGeneratingCryptoKeySource() {
		this.keys = Stream.of(asymmetric(), symmetric()).collect(Collectors.toList());
	}

	@Override
	public CryptoKey<?> get(String algorithm) {
		return keys.stream()
			.filter(key -> key.getAlgorithm().equals(algorithm))
			.findFirst()
			.orElse(null);
	}

	@Override
	public List<AsymmetricKey> getAsymmetricKeys() {
		return keys.stream()
			.filter(key -> AsymmetricKey.class.isAssignableFrom(key.getClass()))
			.map(AsymmetricKey.class::cast)
			.collect(Collectors.toList());
	}

}
