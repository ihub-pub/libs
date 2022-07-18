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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pub.ihub.cloud.rest.IResultCode;
import pub.ihub.cloud.rest.ResultCode;

/**
 * @author henry
 */
@DisplayName("标准响应编码测试")
class ResultCodeTest {

	@DisplayName("单元测试")
	@Test
	void demo() {
		Assertions.assertTrue(ResultCode.SUCCESS.isSuccess());
		Assertions.assertFalse(ResultCode.BUSINESS_ERROR.isSuccess());
		Assertions.assertTrue(ResultCode.DATA_VALIDATION_ERROR.isError());
		Assertions.assertTrue(ResultCode.AUTHENTICATION_ERROR.isError());
		Assertions.assertTrue(ResultCode.AUTHORIZATION_ERROR.isError());
		Assertions.assertTrue(ResultCode.CLIENT_ERROR.isError());
		Assertions.assertTrue(ResultCode.SERVER_ERROR.isError());
		Assertions.assertTrue(ResultCode.BUSINESS_ERROR.isError());
		Assertions.assertFalse(ResultCode.SUCCESS.isError());

		IResultCode otherCode = () -> 9527;
		Assertions.assertNull(otherCode.getSeries());
		Assertions.assertNull(otherCode.getMessage());
	}

}
