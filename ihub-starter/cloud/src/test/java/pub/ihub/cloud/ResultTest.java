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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import pub.ihub.cloud.rest.Result;
import pub.ihub.cloud.rest.ResultCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Result单元测试
 *
 * @author henry
 */
@DisplayName("Result单元测试")
class ResultTest {

	@DisplayName("测试success方法")
	@Test
	void testSuccess() {
		Result<Void> result = Result.success();
		assertNotNull(result);
		assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
		assertEquals(ResultCode.SUCCESS.getMessage(), result.getMessage());
	}

	@DisplayName("测试success带message方法")
	@Test
	void testSuccessWithMessage() {
		Result<Void> result = Result.success("操作成功");
		assertNotNull(result);
		assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
		assertEquals("操作成功", result.getMessage());
	}

	@DisplayName("测试code方法")
	@Test
	void testCode() {
		Result<Void> result = Result.code(ResultCode.SERVER_ERROR);
		assertNotNull(result);
		assertEquals(ResultCode.SERVER_ERROR.getCode(), result.getCode());
		assertEquals(ResultCode.SERVER_ERROR.getMessage(), result.getMessage());
	}

	@DisplayName("测试code带message方法")
	@Test
	void testCodeWithMessage() {
		Result<Void> result = Result.code(ResultCode.CLIENT_ERROR, "自定义消息");
		assertNotNull(result);
		assertEquals(ResultCode.CLIENT_ERROR.getCode(), result.getCode());
		assertEquals("自定义消息", result.getMessage());
	}

	@DisplayName("测试code带int和message方法")
	@Test
	void testCodeWithIntAndMessage() {
		Result<Void> result = Result.code(9999, "测试code");
		assertNotNull(result);
		assertEquals(9999, result.getCode());
		assertEquals("测试code", result.getMessage());
	}

	@DisplayName("测试data方法")
	@Test
	void testData() {
		Result<String> result = Result.data("test data");
		assertNotNull(result);
		assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
		assertEquals("test data", result.getData());
		assertNull(result.getMessage());
	}

	@DisplayName("测试data为null方法")
	@Test
	void testDataNull() {
		Result<String> result = Result.data(null);
		assertNotNull(result);
		assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
		assertEquals("响应数据为空", result.getMessage());
	}

	@DisplayName("测试data带message方法")
	@Test
	void testDataWithMessage() {
		Result<String> result = Result.data("test data", "数据获取成功");
		assertNotNull(result);
		assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
		assertEquals("test data", result.getData());
		assertEquals("数据获取成功", result.getMessage());
	}

	@DisplayName("测试data带code、data和message方法")
	@Test
	void testDataWithCodeDataAndMessage() {
		Result<String> result = Result.data(1001, "test data", "自定义数据");
		assertNotNull(result);
		assertEquals(1001, result.getCode());
		assertEquals("test data", result.getData());
		assertEquals("自定义数据", result.getMessage());
	}

	@DisplayName("测试fail方法")
	@Test
	void testFail() {
		Result<Void> result = Result.fail("业务失败");
		assertNotNull(result);
		assertEquals(ResultCode.BUSINESS_ERROR.getCode(), result.getCode());
		assertEquals("业务失败", result.getMessage());
	}

	@DisplayName("测试error方法")
	@Test
	void testError() {
		Result<Void> result = Result.error("系统错误");
		assertNotNull(result);
		assertEquals(ResultCode.SERVER_ERROR.getCode(), result.getCode());
		assertEquals("系统错误", result.getMessage());
	}

	@DisplayName("测试putMetadata方法")
	@Test
	void testPutMetadata() {
		Result<Void> result = Result.success();
		result.putMetadata("key1", "value1");
		result.putMetadata("key2", 123);
		assertNotNull(result.getMetadata());
		assertEquals("value1", result.getMetadata().get("key1"));
		assertEquals(123, result.getMetadata().get("key2"));
	}

	@DisplayName("测试httpStatus方法 - SUCCESS")
	@Test
	void testHttpStatusSuccess() {
		Result<Void> result = Result.success();
		assertEquals(HttpStatus.OK, result.httpStatus());
	}

	@DisplayName("测试httpStatus方法 - BUSINESS_ERROR")
	@Test
	void testHttpStatusBusinessError() {
		Result<Void> result = Result.fail("业务错误");
		assertEquals(HttpStatus.OK, result.httpStatus());
	}

	@DisplayName("测试httpStatus方法 - DATA_VALIDATION_ERROR")
	@Test
	void testHttpStatusDataValidationError() {
		Result<Void> result = Result.code(ResultCode.DATA_VALIDATION_ERROR);
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.httpStatus());
	}

	@DisplayName("测试httpStatus方法 - AUTHENTICATION_ERROR")
	@Test
	void testHttpStatusAuthenticationError() {
		Result<Void> result = Result.code(ResultCode.AUTHENTICATION_ERROR);
		assertEquals(HttpStatus.UNAUTHORIZED, result.httpStatus());
	}

	@DisplayName("测试httpStatus方法 - AUTHORIZATION_ERROR")
	@Test
	void testHttpStatusAuthorizationError() {
		Result<Void> result = Result.code(ResultCode.AUTHORIZATION_ERROR);
		assertEquals(HttpStatus.FORBIDDEN, result.httpStatus());
	}

	@DisplayName("测试httpStatus方法 - CLIENT_ERROR")
	@Test
	void testHttpStatusClientError() {
		Result<Void> result = Result.code(ResultCode.CLIENT_ERROR);
		assertEquals(HttpStatus.BAD_REQUEST, result.httpStatus());
	}

	@DisplayName("测试httpStatus方法 - SERVER_ERROR")
	@Test
	void testHttpStatusServerError() {
		Result<Void> result = Result.error("服务器错误");
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.httpStatus());
	}

	@DisplayName("测试httpStatus方法 - NOT_FOUND_ERROR")
	@Test
	void testHttpStatusNotFoundError() {
		Result<Void> result = Result.code(ResultCode.NOT_FOUND_ERROR);
		assertEquals(HttpStatus.NOT_FOUND, result.httpStatus());
	}

	@DisplayName("测试构造方法 - IResultCode")
	@Test
	void testConstructorWithIResultCode() {
		Result<String> result = new Result<>(ResultCode.SUCCESS, "data", "message");
		assertNotNull(result);
		assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
		assertEquals("data", result.getData());
		assertEquals("message", result.getMessage());
	}

	@DisplayName("测试构造方法 - IResultCode不带message")
	@Test
	void testConstructorWithIResultCodeAndData() {
		Result<String> result = new Result<>(ResultCode.SUCCESS, "data", null);
		assertNotNull(result);
		assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
		assertEquals("data", result.getData());
		assertNull(result.getMessage());
	}

	@DisplayName("测试构造方法 - IResultCode带message")
	@Test
	void testConstructorWithIResultCodeAndMessage() {
		Result<String> result = new Result<>(ResultCode.SUCCESS, null, "message");
		assertNotNull(result);
		assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
		assertEquals("message", result.getMessage());
	}

	@DisplayName("测试构造方法 - data为null时设置默认消息")
	@Test
	void testConstructorWithNullDataSetsDefaultMessage() {
		Result<Object> result = new Result<>(ResultCode.SUCCESS, null, null);
		assertNotNull(result);
		assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
		assertNull(result.getData());
		assertNull(result.getMessage());
	}

	@DisplayName("测试构造方法 - data不为null时message为null")
	@Test
	void testConstructorWithNonNullDataSetsNullMessage() {
		Result<Integer> result = new Result<>(ResultCode.SUCCESS, 123, null);
		assertNotNull(result);
		assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
		assertEquals(123, result.getData());
		assertNull(result.getMessage());
	}

}
