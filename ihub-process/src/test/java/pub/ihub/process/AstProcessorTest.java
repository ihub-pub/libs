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

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;

/**
 * @author henry
 */
@DisplayName("基础AST注解处理器测试")
class AstProcessorTest {

	@DisplayName("AST注解处理测试-成功")
	@Test
	void process() {
		Compilation compilation =
			Compiler.javac()
				.withProcessors(new AstDemoProcessor())
				.compile(JavaFileObjects.forResource("test/Demo.java"));
		assertThat(compilation).succeededWithoutWarnings();
		assertThat(compilation).hadNoteCount(1);
		assertThat(compilation).hadNoteContaining("此处 啥也没有");
	}

	@DisplayName("AST注解处理测试-模拟警告")
	@Test
	void warning() {
		Compilation compilation =
			Compiler.javac()
				.withProcessors(new AstWarnProcessor())
				.compile(JavaFileObjects.forResource("test/Demo.java"));
		assertThat(compilation).succeeded();
		assertThat(compilation).hadWarningCount(1);
		assertThat(compilation).hadWarningContaining("此处 啥也没有");
	}

	@DisplayName("AST注解处理测试-模拟异常")
	@Test
	void processException() {
		Compilation compilation =
			Compiler.javac()
				.withProcessors(new AstErrorProcessor())
				.compile(JavaFileObjects.forResource("test/Demo.java"));
		assertThat(compilation).failed();
		assertThat(compilation).hadErrorCount(1);
		assertThat(compilation).hadErrorContaining("[java.lang.Deprecated] process error: java.lang.RuntimeException: 此处有异常");
	}

}
