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
package pub.ihub.cloud.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.springframework.http.HttpStatus.OK;

/**
 * 统一响应结果
 *
 * @author liheng
 */
@Data
public class Result<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	// <editor-fold defaultstate="collapsed" desc="核心属性">

	/**
	 * 响应编码
	 */
	private final int code;

	/**
	 * 响应信息
	 */
	@JsonInclude(NON_NULL)
	private String message;

	/**
	 * 响应数据
	 */
	@JsonInclude(NON_NULL)
	private T data;

	/**
	 * 响应元信息
	 */
	@JsonInclude(NON_EMPTY)
	private Map<String, Object> metadata = new HashMap<>();

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="构造方法">

	public Result(int code, T data, String message) {
		this.code = code;
		this.data = data;
		this.message = message;
	}

	public Result(IResultCode code, T data, String message) {
		this(code.getCode(), data, message);
	}

	public Result(IResultCode code, T data) {
		this(code, data, null == data ? "响应数据为空" : null);
	}

	public Result(IResultCode code, String message) {
		this(code, null, message);
	}

	// </editor-fold>

	/**
	 * 添加元数据
	 *
	 * @param key   键
	 * @param value 值
	 * @param <V>   值泛型
	 * @return Result
	 */
	public <V> Result<T> putMetadata(String key, V value) {
		metadata.put(key, value);
		return this;
	}

	public HttpStatus httpStatus() {
		// TODO 映射状态码
		return OK;
	}

	// <editor-fold defaultstate="collapsed" desc="构建方法">

	public static <T> Result<T> success() {
		return code(ResultCode.SUCCESS);
	}

	public static <T> Result<T> success(String message) {
		return code(ResultCode.SUCCESS, message);
	}

	public static <T> Result<T> code(IResultCode code) {
		return code(code, code.getMessage());
	}

	public static <T> Result<T> code(IResultCode code, String message) {
		return code(code.getCode(), message);
	}

	public static <T> Result<T> code(int code, String message) {
		return new Result<>(code, null, message);
	}

	public static <T> Result<T> data(T data) {
		return new Result<>(ResultCode.SUCCESS, data);
	}

	public static <T> Result<T> data(T data, String message) {
		return new Result<>(ResultCode.SUCCESS, data, message);
	}

	public static <T> Result<T> data(int code, T data, String message) {
		return new Result<>(code, data, message);
	}

	public static <T> Result<T> fail(String message) {
		return code(ResultCode.BUSINESS_ERROR, message);
	}

	public static <T> Result<T> error(String message) {
		return code(ResultCode.SERVER_ERROR, message);
	}

	// </editor-fold>

}
