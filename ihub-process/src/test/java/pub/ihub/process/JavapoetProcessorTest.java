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

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static javax.lang.model.SourceVersion.RELEASE_11;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

/**
 * @author henry
 */
@DisplayName("基础Javapoet注解处理器测试")
class JavapoetProcessorTest {

	private static final String LINES_RESOURCE = "META-INF/resource.lines";

	@DisplayName("Javapoet注解处理测试-成功")
	@Test
	void process() {
		Compilation compilation =
			Compiler.javac()
				.withProcessors(new DemoJavapoetProcessor())
				.compile(JavaFileObjects.forResource("test/Demo.java"));
		assertThat(compilation).succeededWithoutWarnings();
		assertThat(compilation)
			.generatedFile(CLASS_OUTPUT, LINES_RESOURCE)
			.contentsAsUtf8String()
			.containsMatch("line1[\\S\\s]*line2[\\S\\s]*line3");
	}

	@SupportedSourceVersion(RELEASE_11)
	@SupportedAnnotationTypes("java.lang.Deprecated")
	static class DemoJavapoetProcessor extends BaseJavapoetProcessor {

		@Override
		protected void processElement(Element element) throws IOException {
			List<String> lines = new ArrayList<>();
			lines.add("line1");
			lines.add("line2");
			lines.add("line3");
			writeResource(CLASS_OUTPUT, LINES_RESOURCE, lines);
		}

	}

}
