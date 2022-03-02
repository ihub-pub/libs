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
package pub.ihub.process.boot;

import com.google.auto.service.AutoService;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;

import static javax.lang.model.SourceVersion.RELEASE_11;

/**
 * 自动配置注解处理器
 *
 * @author henry
 */
@AutoService(Processor.class)
@SupportedSourceVersion(RELEASE_11)
@SupportedAnnotationTypes("org.springframework.context.annotation.Configuration")
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
public class ConfigurationProcessor extends BaseSpringFactoriesProcessor {

	@Override
	protected void processElement(Element element) {
		writeSpringFactoriesFile("org.springframework.boot.autoconfigure.EnableAutoConfiguration",
			element.getEnclosingElement().toString() + "." + element.getSimpleName());
	}

}
