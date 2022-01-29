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

import cn.hutool.core.util.StrUtil;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;
import pub.ihub.core.BaseConfigEnvironmentPostProcessor;
import pub.ihub.process.BaseJavapoetProcessor;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import static javax.lang.model.SourceVersion.RELEASE_11;

/**
 * @author henry
 */
@AutoService(Processor.class)
@SupportedSourceVersion(RELEASE_11)
@SupportedAnnotationTypes("org.springframework.boot.context.properties.ConfigurationProperties")
public class ConfigEnvironmentProcessor extends BaseJavapoetProcessor {

	@SneakyThrows
	@Override
	protected void processElement(Element element) {
		String profile = element.getAnnotation(ConfigurationProperties.class).value().replaceAll("\\w+\\.", "");
		MethodSpec activeProfile = MethodSpec.methodBuilder("getActiveProfile")
			.addModifiers(Modifier.PROTECTED)
			.returns(String.class)
			.addStatement("return $S", profile)
			.addAnnotation(Override.class)
			.build();

		TypeSpec helloWorld = TypeSpec.classBuilder(StrUtil.upperFirst(profile) + "ConfigPostProcessor")
			.superclass(BaseConfigEnvironmentPostProcessor.class)
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
			.addMethod(activeProfile)
			.build();

		JavaFile javaFile = JavaFile.builder(element.getEnclosingElement().toString(), helloWorld)
			.build();

		javaFile.writeTo(mFiler);

		writeServiceFile("META-INF/spring.factories", "# Environment Post Processors",
			"org.springframework.boot.env.EnvironmentPostProcessor=\\",
			javaFile.packageName + "." + javaFile.typeSpec.name);
	}

}
