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

import cn.hutool.core.io.file.FileWriter;
import lombok.SneakyThrows;

import java.util.List;

import static javax.tools.StandardLocation.SOURCE_OUTPUT;

/**
 * 基础Javapoet注解处理器
 *
 * @author henry
 */
public abstract class BaseJavapoetProcessor extends BaseProcessor {

	@SneakyThrows
	protected void writeServiceFile(String resourceFile, String... resourceLines) {
		String sourcePath = mFiler.getResource(SOURCE_OUTPUT, "", resourceFile).getName();
		new FileWriter(sourcePath).writeLines(List.of(resourceLines), true);
	}

}
