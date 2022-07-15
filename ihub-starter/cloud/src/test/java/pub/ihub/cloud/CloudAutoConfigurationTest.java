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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

	@DisplayName("Cloud测试")
	@Test
	void cloud() throws Exception {
		mockMvc.perform(get("/servlet/demo")).andDo(MockMvcResultHandlers.print())
			.andExpect(status().isOk());
		mockMvc.perform(get("/reactor/demo")).andDo(MockMvcResultHandlers.print())
			.andExpect(status().isOk());
	}

}
