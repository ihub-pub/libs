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

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.PathUtil;
import lombok.SneakyThrows;

import javax.tools.FileObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.tools.StandardLocation.SOURCE_OUTPUT;

/**
 * 基础Javapoet注解处理器
 *
 * @author henry
 */
public abstract class BaseJavapoetProcessor extends BaseProcessor {

	@SneakyThrows
	protected void writeServiceFile(String resourceFile, String... resourceLine) {
		List<String> resourceLines = new ArrayList<>();
		FileObject existingFile = mFiler.getResource(SOURCE_OUTPUT, "", resourceFile);
		String sourcePath = existingFile.getName().replaceAll("build.*java", "src")
			.replace("main", "main" + File.separator + "resources");
		if (PathUtil.exists(Paths.get(sourcePath), false)) {
			List<String> oldServices = new FileReader(sourcePath).readLines();
			resourceLines.addAll(oldServices);
		}

		resourceLines.addAll(List.of(resourceLine));

		FileObject fileObject = mFiler.createResource(SOURCE_OUTPUT, "", resourceFile);
		try (OutputStream out = fileObject.openOutputStream()) {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, UTF_8));
			for (String service : resourceLines) {
				writer.write(service);
				writer.newLine();
			}
			writer.flush();
		}
	}

}
