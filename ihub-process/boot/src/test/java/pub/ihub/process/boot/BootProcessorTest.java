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
import org.junit.jupiter.api.Test;

import javax.tools.StandardLocation;

import static com.google.testing.compile.CompilationSubject.assertThat;

/**
 * @author liheng
 */
class BootProcessorTest {

	@Test
	void configEnvironment() {
		Compilation compilation =
			Compiler.javac()
				// 选择注解处理器
				.withProcessors(new ConfigEnvironmentProcessor())
				// 选择需要处理的代码
				.compile(JavaFileObjects.forResource("test/DemoProperties.java"));
		// 断言是否成功
		assertThat(compilation).succeededWithoutWarnings();
		// 断言生成的内容， compile-testing 默认是生成到了内存中
//		assertThat(compilation)
//			// 选择生成的配置文件，如果是生成 java 文件可以使用 generatedSourceFile 方法
//			.generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/spring.factories")
//			// 转换成 utf-8 字符串然后进行比较
//			.contentsAsUtf8String()
//			// 由于生成的文件尾部有个 writer.newLine() 在各个平台不一样，所以采用 startsWith
//			.contains("test.MyProcessor");
	}

}
