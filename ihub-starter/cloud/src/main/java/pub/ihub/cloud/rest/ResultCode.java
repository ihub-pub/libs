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

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标准响应编码
 *
 * @author liheng
 */
@Getter
@AllArgsConstructor
public enum ResultCode implements IResultCode {

	//<editor-fold defaultstate="collapsed" desc="xxx Success">
	SUCCESS(0, "成功"),
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="1xxx Validation Error">
	DATA_VALIDATION_ERROR(1000, "数据验证异常"),
	CONSTRAINT_VIOLATION_ERROR(1010, "自定义约束背反"),
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="2xxx Authentication Error">
	AUTHENTICATION_ERROR(2000, "认证异常"),
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="3xxx Authorization Error">
	AUTHORIZATION_ERROR(3000, "授权异常"),
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="4xxx Client Error">
	CLIENT_ERROR(4000, "客户端异常"),
	NOT_FOUND_ERROR(4010, "无法找到相应资源"),
	INVALID_FORMAT_ERROR(4020, "无效格式"),
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="5xxx Server Error">
	SERVER_ERROR(5000, "服务端异常"),
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="6xxx Server External Error">
	BUSINESS_ERROR(6000, "业务异常");
	//</editor-fold>


	/**
	 * 响应编码
	 */
	private final int code;

	/**
	 * 响应信息
	 */
	private final String message;

}
