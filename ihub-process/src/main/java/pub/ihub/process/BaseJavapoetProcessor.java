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

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 基础Javapoet注解处理器
 *
 * @author henry
 */
public abstract class BaseJavapoetProcessor extends BaseProcessor {

	protected void writeResource(JavaFileManager.Location location, String resourcePkg, List<String> lines) throws IOException {
		FileObject resource = mFiler.createResource(location, "", resourcePkg);
		try (OutputStream out = resource.openOutputStream()) {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, UTF_8));
			for (String line : lines) {
				writer.write(line);
				writer.newLine();
			}
			writer.flush();
		}
	}

}
