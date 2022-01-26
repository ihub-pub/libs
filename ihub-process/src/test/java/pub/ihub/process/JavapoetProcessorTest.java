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
package pub.ihub.process;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Element;

/**
 * @author henry
 */
@DisplayName("基础Javapoet注解处理器测试")
class JavapoetProcessorTest {

	@DisplayName("Javapoet注解处理测试-成功")
	@Test
	void process() {
		new DemoJavapoetProcessor().processElement(null);
	}

	static class DemoJavapoetProcessor extends BaseJavapoetProcessor {

		@Override
		protected void processElement(Element element) {
		}

	}

}
