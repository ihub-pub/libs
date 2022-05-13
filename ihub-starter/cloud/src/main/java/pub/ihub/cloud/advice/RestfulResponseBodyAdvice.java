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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;
import pub.ihub.cloud.Result;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * restful响应处理器
 *
 * @author liheng
 */
@RequiredArgsConstructor
@RestControllerAdvice
@Order(HIGHEST_PRECEDENCE)
@ConditionalOnProperty(value = "ihub.cloud.restful-body", havingValue = "true", matchIfMissing = true)
public class RestfulResponseBodyAdvice extends AbstractMappingJacksonResponseBodyAdvice {

	@Override
	protected void beforeBodyWriteInternal(MappingJacksonValue bodyContainer,
										   MediaType contentType,
										   MethodParameter returnType,
										   ServerHttpRequest request,
										   ServerHttpResponse response) {
		var body = bodyContainer.getValue();
		if (body instanceof Result) {
			response.setStatusCode(((Result<?>) body).httpStatus());
		} else if (body instanceof Object) {
			// TODO 定义ABaseDTO
			bodyContainer.setValue(Result.data(body));
		}
	}

}
