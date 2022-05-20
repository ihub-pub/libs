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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import pub.ihub.cloud.rest.Result;
import pub.ihub.cloud.exception.NotFoundException;

import java.util.NoSuchElementException;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static pub.ihub.cloud.rest.ResultCode.CLIENT_ERROR;
import static pub.ihub.cloud.rest.ResultCode.INVALID_FORMAT_ERROR;
import static pub.ihub.cloud.rest.ResultCode.NOT_FOUND_ERROR;

/**
 * 客户端错误处理
 *
 * @author liheng
 */
@Slf4j
@RestControllerAdvice
@Order(HIGHEST_PRECEDENCE)
@ResponseBody
public class ClientAdvice {

	/**
	 * 捕获无相应资源异常
	 *
	 * @param e 异常
	 * @return 标准返回值结构
	 */
	@ExceptionHandler({NotFoundException.class, NoSuchElementException.class})
	@ResponseStatus(NOT_FOUND)
	Result<?> notFound(Throwable e) {
		log.error("指定资源不存在", e);
		return Result.code(NOT_FOUND_ERROR, e.getMessage());
	}

	/**
	 * 无效格式异常
	 *
	 * @param ex 异常
	 * @return 标准返回值结构
	 */
	@ExceptionHandler({HttpMessageNotReadableException.class, InvalidFormatException.class})
	@ResponseStatus(BAD_REQUEST)
	Result<?> invalidFormat(Throwable ex) {
		log.error("无效格式", ex);
		return Result.code(INVALID_FORMAT_ERROR);
	}

	/**
	 * 客户端异常
	 *
	 * @param ex 异常
	 * @return 标准返回值结构
	 */
	@ExceptionHandler(ServerWebInputException.class)
	@ResponseStatus(BAD_REQUEST)
	Result<?> webInput(ServerWebInputException ex) {
		log.error("客户端异常", ex);
		return Result.code(CLIENT_ERROR);
	}

}
