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
package pub.ihub.process.boot;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;

/**
 * @author liheng
 */
@DisplayName("启动环境处理器测试")
class BootProcessorTest {

	@DisplayName("配置环境处理器测试-成功")
	@Test
	void configEnvironment() {
		Compilation compilation =
			Compiler.javac()
				.withProcessors(new ConfigEnvironmentProcessor())
				.compile(JavaFileObjects.forResource("test/DemoProperties.java"),
					JavaFileObjects.forResource("test/DemoAutoConfiguration.java"));
		assertThat(compilation).succeededWithoutWarnings();
		assertThat(compilation)
			.generatedSourceFile("test/DemoConfigPostProcessor")
			.contentsAsUtf8String()
			.contains("public final class DemoConfigPostProcessor extends BaseConfigEnvironmentPostProcessor");
	}

	@DisplayName("自动配置注解处理器测试-成功")
	@Test
	void configuration() {
		Compilation compilation =
			Compiler.javac()
				.withProcessors(new ConfigurationProcessor())
				.compile(JavaFileObjects.forResource("test/DemoProperties.java"),
					JavaFileObjects.forResource("test/DemoAutoConfiguration.java"),
					JavaFileObjects.forResource("test/OtherAutoConfiguration.java"));
		assertThat(compilation).succeededWithoutWarnings();
	}

}
