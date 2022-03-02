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
package pub.ihub.process.doc;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;

/**
 * @author henry
 */
@DisplayName("基础文档注解处理器测试")
class DocProcessorTest {

	@DisplayName("实体文档注解处理器测试-成功")
	@Test
	void entity() {
		Compilation compilation =
			Compiler.javac()
				.withProcessors(new EntityDocProcessor())
				.compile(JavaFileObjects.forResource("test/Demo.java"));
		assertThat(compilation).succeededWithoutWarnings();
	}

	@DisplayName("Controller文档注解处理器测试-成功")
	@Test
	void controller() {
		Compilation compilation =
			Compiler.javac()
				.withProcessors(new ControllerDocProcessor())
				.compile(JavaFileObjects.forResource("test/Demo.java"),
					JavaFileObjects.forResource("test/DemoController.java"));
		assertThat(compilation).succeededWithoutWarnings();
	}

}
