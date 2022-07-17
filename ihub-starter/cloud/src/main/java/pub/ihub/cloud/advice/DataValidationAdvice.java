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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pub.ihub.cloud.rest.Result;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.UnexpectedTypeException;
import javax.validation.ValidationException;
import java.util.stream.Collectors;

import static cn.hutool.core.util.ObjectUtil.defaultIfBlank;
import static java.util.Locale.getDefault;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static pub.ihub.cloud.advice.DataValidationAdvice.ORDER;
import static pub.ihub.cloud.rest.ResultCode.CONSTRAINT_VIOLATION_ERROR;
import static pub.ihub.cloud.rest.ResultCode.DATA_VALIDATION_ERROR;

/**
 * 数据验证错误处理
 *
 * @author liheng
 */
@RequiredArgsConstructor
@Slf4j
@RestControllerAdvice
@Order(ORDER)
@ResponseBody
@ResponseStatus(UNPROCESSABLE_ENTITY)
public class DataValidationAdvice {

	/**
	 * 默认顺序
	 */
	public static final int ORDER = ClientAdvice.ORDER - 1;

	@Qualifier("validationMessageSource")
	private final MessageSource validationMessageSource;


	/**
	 * 自定义约束背反
	 *
	 * @param e 异常
	 * @return 标准返回值结构
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	Result<?> constraintViolationError(ConstraintViolationException e) {
		log.error("自定义约束背反", e);
		return Result.code(CONSTRAINT_VIOLATION_ERROR, e.getConstraintViolations().stream()
			.map(ConstraintViolation::getMessage).collect(Collectors.joining("|")));
	}

	/**
	 * 方法参数不合法
	 *
	 * @param e 异常
	 * @return 标准返回值结构
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	Result<?> invalidMethodArgument(MethodArgumentNotValidException e) {
		log.error("方法参数不合法", e);
		return Result.code(DATA_VALIDATION_ERROR, e.getBindingResult().getAllErrors().stream()
			.map(this::formatErrorMessage).collect(Collectors.joining("|")));
	}

	@ExceptionHandler(UnexpectedTypeException.class)
	Result<?> onUnexpectedTypeException(UnexpectedTypeException e) {
		log.error("意外类型异常", e);
		return Result.code(DATA_VALIDATION_ERROR);
	}

	/**
	 * 捕获泛化的JSR303异常
	 *
	 * @param e 异常
	 * @return 标准返回值结构
	 */
	@ExceptionHandler(ValidationException.class)
	Result<?> genericError(ValidationException e) {
		log.error("泛化数据校验失败", e);
		return Result.code(DATA_VALIDATION_ERROR);
	}

	private String formatErrorMessage(MessageSourceResolvable error) {
		return defaultIfBlank(validationMessageSource.getMessage(error, getDefault()), error.getDefaultMessage());
	}

}
