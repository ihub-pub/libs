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
package pub.ihub.cloud.advice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ServerWebInputException;
import pub.ihub.cloud.exception.NotFoundException;
import pub.ihub.cloud.rest.Result;
import pub.ihub.core.BusinessException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import jakarta.validation.ValidationException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Advice单元测试
 *
 * @author henry
 */
@DisplayName("Advice单元测试")
class AdviceTest {

	@DisplayName("测试GlobalExceptionAdvice - BusinessException")
	@Test
	void testGlobalExceptionAdviceBusinessException() {
		GlobalExceptionAdvice advice = new GlobalExceptionAdvice();
		BusinessException exception = new BusinessException("业务异常");
		Result<?> result = advice.handlerException(exception);
		assertNotNull(result);
		assertEquals("业务异常", result.getMessage());
	}

	@DisplayName("测试GlobalExceptionAdvice - Throwable")
	@Test
	void testGlobalExceptionAdviceThrowable() {
		GlobalExceptionAdvice advice = new GlobalExceptionAdvice();
		RuntimeException exception = new RuntimeException("系统错误");
		Result<?> result = advice.handlerException(exception);
		assertNotNull(result);
		assertEquals("系统错误", result.getMessage());
	}

	@DisplayName("测试ClientAdvice - NotFoundException")
	@Test
	void testClientAdviceNotFoundException() {
		ClientAdvice advice = new ClientAdvice();
		NotFoundException exception = new NotFoundException("资源不存在");
		Result<?> result = advice.notFound(exception);
		assertNotNull(result);
		assertEquals("资源不存在", result.getMessage());
	}

	@DisplayName("测试ClientAdvice - NoSuchElementException")
	@Test
	void testClientAdviceNoSuchElementException() {
		ClientAdvice advice = new ClientAdvice();
		NoSuchElementException exception = new NoSuchElementException("元素不存在");
		Result<?> result = advice.notFound(exception);
		assertNotNull(result);
		assertEquals("元素不存在", result.getMessage());
	}

	@DisplayName("测试ClientAdvice - HttpMessageNotReadableException")
	@Test
	void testClientAdviceInvalidFormatHttpMessage() {
		ClientAdvice advice = new ClientAdvice();
		HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
		Result<?> result = advice.invalidFormat(exception);
		assertNotNull(result);
	}

	@DisplayName("测试ClientAdvice - InvalidFormatException")
	@Test
	void testClientAdviceInvalidFormatException() {
		ClientAdvice advice = new ClientAdvice();
		InvalidFormatException exception = mock(InvalidFormatException.class);
		Result<?> result = advice.invalidFormat(exception);
		assertNotNull(result);
	}

	@DisplayName("测试ClientAdvice - ServerWebInputException")
	@Test
	void testClientAdviceWebInput() {
		ClientAdvice advice = new ClientAdvice();
		ServerWebInputException exception = new ServerWebInputException("输入错误");
		Result<?> result = advice.webInput(exception);
		assertNotNull(result);
	}

	@DisplayName("测试DataValidationAdvice - ConstraintViolationException")
	@Test
	void testDataValidationAdviceConstraintViolation() {
		MessageSource messageSource = new StaticMessageSource();
		DataValidationAdvice advice = new DataValidationAdvice(messageSource);

		Set<ConstraintViolation<?>> violations = new HashSet<>();
		ConstraintViolation<?> violation = mock(ConstraintViolation.class);
		when(violation.getMessage()).thenReturn("约束违反");
		violations.add(violation);

		ConstraintViolationException exception = new ConstraintViolationException(violations);
		Result<?> result = advice.constraintViolationError(exception);
		assertNotNull(result);
	}

	@DisplayName("测试DataValidationAdvice - MethodArgumentNotValidException")
	@Test
	void testDataValidationAdviceMethodArgumentNotValid() {
		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage("error.code", Locale.getDefault(), "错误消息");
		DataValidationAdvice advice = new DataValidationAdvice(messageSource);

		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
		FieldError fieldError = new FieldError("object", "field", "默认消息");
		fieldError = new FieldError("object", "field", "默认消息");
		when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
		when(exception.getBindingResult()).thenReturn(bindingResult);

		Result<?> result = advice.invalidMethodArgument(exception);
		assertNotNull(result);
	}

	@DisplayName("测试DataValidationAdvice - UnexpectedTypeException")
	@Test
	void testDataValidationAdviceUnexpectedType() {
		MessageSource messageSource = new StaticMessageSource();
		DataValidationAdvice advice = new DataValidationAdvice(messageSource);

		UnexpectedTypeException exception = new UnexpectedTypeException("意外类型");
		Result<?> result = advice.onUnexpectedTypeException(exception);
		assertNotNull(result);
	}

	@DisplayName("测试DataValidationAdvice - ValidationException")
	@Test
	void testDataValidationAdviceGenericError() {
		MessageSource messageSource = new StaticMessageSource();
		DataValidationAdvice advice = new DataValidationAdvice(messageSource);

		ValidationException exception = new ValidationException("验证错误");
		Result<?> result = advice.genericError(exception);
		assertNotNull(result);
	}

	@DisplayName("测试RestfulResponseBodyAdvice - Result类型")
	@Test
	void testRestfulResponseBodyAdviceWithResult() {
		RestfulResponseBodyAdvice advice = new RestfulResponseBodyAdvice();

		Result<String> resultBody = Result.success("test");
		MappingJacksonValue bodyContainer = new MappingJacksonValue(resultBody);
		MediaType contentType = MediaType.APPLICATION_JSON;
		MethodParameter returnType = mock(MethodParameter.class);
		ServerHttpRequest request = mock(ServerHttpRequest.class);
		ServerHttpResponse response = mock(ServerHttpResponse.class);

		advice.beforeBodyWriteInternal(bodyContainer, contentType, returnType, request, response);

		assertNotNull(bodyContainer.getValue());
		assertTrue(bodyContainer.getValue() instanceof Result);
	}

	@DisplayName("测试RestfulResponseBodyAdvice - 非Result类型")
	@Test
	void testRestfulResponseBodyAdviceWithNonResult() {
		RestfulResponseBodyAdvice advice = new RestfulResponseBodyAdvice();

		String nonResultBody = "test string";
		MappingJacksonValue bodyContainer = new MappingJacksonValue(nonResultBody);
		MediaType contentType = MediaType.APPLICATION_JSON;
		MethodParameter returnType = mock(MethodParameter.class);
		ServerHttpRequest request = mock(ServerHttpRequest.class);
		ServerHttpResponse response = mock(ServerHttpResponse.class);

		advice.beforeBodyWriteInternal(bodyContainer, contentType, returnType, request, response);

		assertNotNull(bodyContainer.getValue());
		assertTrue(bodyContainer.getValue() instanceof Result);
		assertEquals("test string", ((Result<?>) bodyContainer.getValue()).getData());
	}

}
