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

/**
 * 密钥资源
 *
 * @author henry
 */
public interface CryptoKeySource {

	/**
	 * 获取密钥
	 *
	 * @param algorithm 密钥算法
	 * @return 密钥
	 */
	CryptoKey<?> get(String algorithm);

	/**
	 * 获取所有非对称密钥
	 *
	 * @return 非对称密钥
	 */
	List<AsymmetricKey> getAsymmetricKeys();

}
