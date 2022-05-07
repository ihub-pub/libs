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

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import pub.ihub.cloud.Result;
import pub.ihub.core.BusinessException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

/**
 * 全局异常处理
 *
 * @author liheng
 */
@Slf4j
@ControllerAdvice
@Order
@ResponseBody
public class GlobalExceptionAdvice {

	@ExceptionHandler(BusinessException.class)
	@ResponseStatus(OK)
	public Result<?> handlerException(BusinessException e) {
		log.error("业务异常", e);
		return Result.fail(e.getMessage());
	}

	@ExceptionHandler(Throwable.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	public Result<?> handlerException(Throwable e) {
		log.error("系统错误", e);
		return Result.error(e.getMessage());
	}

}
