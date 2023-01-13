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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebInputException;
import pub.ihub.cloud.exception.NotFoundException;
import pub.ihub.cloud.rest.Result;
import pub.ihub.cloud.rest.ResultCode;
import pub.ihub.core.BusinessException;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liheng
 */
@RestController
@RequestMapping("/servlet")
@Validated
public class ServletDemoController {

	@RequestMapping("/hello")
	public Map<String, String> hello() {
		return new HashMap<>(1) {{
			put("text", "Hello IHub");
		}};
	}

	@RequestMapping("/demo")
	public String demo() {
		return "servlet demo";
	}

	@RequestMapping("/404")
	public String notFound() {
		throw new NotFoundException("404");
	}

	@RequestMapping("/inputException")
	public String inputException() {
		throw new ServerWebInputException("inputException");
	}

	@RequestMapping("/validationException")
	public String validationException() {
		throw new ValidationException("validationException");
	}

	@RequestMapping("/client")
	public int client(@RequestBody @Valid ReqBody body) {
		return body.data;
	}

	@RequestMapping("/success")
	public Result<?> success() {
		return Result.success();
	}

	@RequestMapping("/{text}")
	public Result<String> success(@PathVariable("text") String text) {
		return Result.success(text);
	}

	@RequestMapping("/code/{code}/{message}")
	public Result<?> code(@PathVariable @Min(9000) int code, @PathVariable String message) {
		return Result.code(code, message).putMetadata("k", "v");
	}

	@RequestMapping("/data")
	public Result<String> data() {
		return Result.data(null);
	}

	@RequestMapping("/data/{data}")
	public Result<String> data(@PathVariable String data) {
		return Result.data(data);
	}

	@RequestMapping("/data/{data}/{message}")
	public Result<String> data(@PathVariable String data, @PathVariable String message) {
		return Result.data(data, message);
	}

	@RequestMapping("/data/{code}/{data}/{message}")
	public Result<String> data(@PathVariable int code, @PathVariable String data, @PathVariable String message) {
		return Result.data(code, data, message);
	}

	@RequestMapping("/fail")
	public Result<?> fail() {
		throw new BusinessException("failMsg");
	}

	@RequestMapping("/error")
	public Result<?> error() {
		throw new RuntimeException("errorMsg");
	}

	@RequestMapping("/authentication")
	public Result<?> authentication() {
		return Result.code(ResultCode.AUTHENTICATION_ERROR);
	}

	@RequestMapping("/authorization")
	public Result<?> authorization() {
		return Result.code(ResultCode.AUTHORIZATION_ERROR);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ReqBody {

		@NotNull
		@Positive
		private Integer data;

	}

}
