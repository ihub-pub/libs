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
package pub.ihub.cloud;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应编码接口
 *
 * @author liheng
 */
public interface IResultCode {

	/**
	 * 响应编码
	 *
	 * @return 编码
	 */
	int getCode();

	/**
	 * 响应信息
	 *
	 * @return 信息
	 */
	default String getMessage() {
		return null;
	}

	/**
	 * 获取响应编码系列
	 *
	 * @return 响应编码系列
	 */
	default Series getSeries() {
		return Series.resolve(getCode());
	}

	/**
	 * 是否成功
	 *
	 * @return 是否成功
	 */
	default boolean isSuccess() {
		return Series.SUCCESS_SERIES == getSeries();
	}

	/**
	 * 是否数据验证错误
	 *
	 * @return 是否数据验证错误
	 */
	default boolean isDataValidationError() {
		return Series.DATA_VALIDATION_SERIES == getSeries();
	}

	/**
	 * 是否认证错误
	 *
	 * @return 是否认证错误
	 */
	default boolean isAuthenticationError() {
		return Series.AUTHENTICATION_SERIES == getSeries();
	}

	/**
	 * 是否授权错误
	 *
	 * @return 是否授权错误
	 */
	default boolean isAuthorizationError() {
		return Series.AUTHORIZATION_SERIES == getSeries();
	}

	/**
	 * 是否客户端错误
	 *
	 * @return 是否客户端错误
	 */
	default boolean isClientError() {
		return Series.CLIENT_SERIES == getSeries();
	}

	/**
	 * 是否服务端错误
	 *
	 * @return 是否服务端错误
	 */
	default boolean isServerError() {
		return Series.SERVER_SERIES == getSeries();
	}

	/**
	 * 是否业务异常
	 *
	 * @return 是否业务异常
	 */
	default boolean isBusinessError() {
		return Series.BUSINESS_SERIES == getSeries();
	}

	/**
	 * 是否错误
	 *
	 * @return 是否错误
	 */
	default boolean isError() {
		return isDataValidationError() || isAuthenticationError() || isAuthorizationError() ||
			isClientError() || isServerError() || this.isBusinessError();
	}

	/**
	 * 响应编码系列枚举
	 */
	@Getter
	@AllArgsConstructor
	enum Series {

		/**
		 * 成功
		 */
		SUCCESS_SERIES(0),
		/**
		 * 数据验证
		 */
		DATA_VALIDATION_SERIES(1),
		/**
		 * 认证
		 */
		AUTHENTICATION_SERIES(2),
		/**
		 * 授权
		 */
		AUTHORIZATION_SERIES(3),
		/**
		 * 客户端
		 */
		CLIENT_SERIES(4),
		/**
		 * 服务端
		 */
		SERVER_SERIES(5),
		/**
		 * 业务
		 */
		BUSINESS_SERIES(6);

		private final int value;

		static Series resolve(int code) {
			int seriesCode = code / 1000;
			for (Series series : values()) {
				if (series.value == seriesCode) {
					return series;
				}
			}
			return null;
		}

	}

}
