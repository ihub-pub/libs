/*
 * Copyright (c) 2021 Henry 李恒 (henry.box@outlook.com).
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import pub.ihub.test.IHubSTConfig;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author henry
 */
@DisplayName("SpringCloud测试")
@IHubSTConfig
@ComponentScan("pub.ihub.cloud")
class CloudAutoConfigurationTest {

	@Autowired
	MockMvc mockMvc;

	@DisplayName("Demo测试")
	@Test
	void demo() throws Exception {
		mockMvc.perform(get("/servlet/hello")).andDo(MockMvcResultHandlers.print())
			.andExpectAll(status().isOk(), content().string("{\"code\":0,\"data\":{\"text\":\"Hello IHub\"}}"));
		mockMvc.perform(get("/servlet/demo")).andDo(MockMvcResultHandlers.print())
			.andExpectAll(status().isOk(), content().string("servlet demo"));
		mockMvc.perform(get("/reactor/demo")).andDo(MockMvcResultHandlers.print())
			.andExpectAll(status().isOk(), content().string("reactor demo"));
	}

	@DisplayName("异常响应测试")
	@Test
	void exception() throws Exception {
		mockMvc.perform(get("/servlet/404")).andExpectAll(status().isNotFound(),
			content().json("{\"code\":4010,\"message\":\"404\"}"));
		mockMvc.perform(post("/servlet/client")).andExpectAll(status().is4xxClientError(),
			content().json("{\"code\":4020,\"message\":\"无效格式\"}"));
		mockMvc.perform(get("/servlet/inputException")).andExpectAll(status().is4xxClientError(),
			content().json("{\"code\":4000,\"message\":\"客户端异常\"}"));
		mockMvc.perform(get("/servlet/code/8888/msg")).andExpectAll(status().isUnprocessableEntity(),
			content().json("{\"code\":1010,\"message\":\"must be greater than or equal to 9000\"}"));
		mockMvc.perform(post("/servlet/client").contentType(APPLICATION_JSON).content("{\"data\":0}"))
			.andExpectAll(status().isUnprocessableEntity(), content().json("{\"code\":1000,\"message\":\"must be greater than 0\"}"));
		mockMvc.perform(post("/reactor/client").contentType(APPLICATION_JSON).content("{\"data\":0}"))
			.andExpectAll(status().isUnprocessableEntity(), content().json("{\"code\":1000,\"message\":\"数据验证异常\"}"));
		mockMvc.perform(get("/servlet/validationException")).andExpectAll(status().is4xxClientError(),
			content().json("{\"code\":1000,\"message\":\"数据验证异常\"}"));
		mockMvc.perform(get("/servlet/authentication")).andExpectAll(status().isUnauthorized(),
			content().json("{\"code\":2000,\"message\":\"认证异常\"}"));
		mockMvc.perform(get("/servlet/authorization")).andExpectAll(status().isForbidden(),
			content().json("{\"code\":3000,\"message\":\"授权异常\"}"));
	}

	@DisplayName("成功响应测试")
	@Test
	void success() throws Exception {
		mockMvc.perform(get("/servlet/success")).andExpectAll(status().isOk(),
			content().json("{\"code\":0,\"message\":\"成功\"}"));
		mockMvc.perform(get("/servlet/msg")).andExpectAll(status().isOk(),
			content().json("{\"code\":0,\"message\":\"msg\"}"));
		mockMvc.perform(get("/servlet/code/9527/msg")).andExpectAll(status().isOk(),
			content().json("{\"code\":9527,\"message\":\"msg\",\"metadata\":{\"k\":\"v\"}}"));
	}

	@DisplayName("响应数据测试")
	@Test
	void data() throws Exception {
		mockMvc.perform(get("/servlet/data")).andExpectAll(status().isOk(),
			content().json("{\"code\":0,\"message\":\"响应数据为空\"}"));
		mockMvc.perform(get("/servlet/data/text")).andExpectAll(status().isOk(),
			content().json("{\"code\":0,\"data\":\"text\"}"));
		mockMvc.perform(get("/servlet/data/text/msg")).andExpectAll(status().isOk(),
			content().json("{\"code\":0,\"message\":\"msg\"}"));
		mockMvc.perform(get("/servlet/data/9527/text/msg")).andExpectAll(status().isOk(),
			content().json("{\"code\":9527,\"message\":\"msg\"}"));
	}

	@DisplayName("业务异常测试")
	@Test
	void fail() throws Exception {
		mockMvc.perform(get("/servlet/fail")).andExpectAll(status().isOk(),
			content().json("{\"code\":6000,\"message\":\"failMsg\"}"));
	}

	@DisplayName("服务端异常测试")
	@Test
	void error() throws Exception {
		mockMvc.perform(get("/servlet/error")).andExpectAll(status().is5xxServerError(),
			content().json("{\"code\":5000,\"message\":\"errorMsg\"}"));
	}

}
