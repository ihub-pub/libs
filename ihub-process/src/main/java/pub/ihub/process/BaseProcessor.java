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

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * 基础注解处理器
 *
 * @author henry
 */
public abstract class BaseProcessor extends AbstractProcessor {

	protected Filer mFiler;
	protected Messager messager;
	protected Elements elementUtils;

	/**
	 * 注解元素处理方法
	 *
	 * @param element 元素
	 */
	protected abstract void process(Element element);

	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		mFiler = processingEnvironment.getFiler();
		messager = processingEnvironment.getMessager();
		elementUtils = processingEnvironment.getElementUtils();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			annotations.forEach(typeElement -> roundEnv.getElementsAnnotatedWith(typeElement).forEach(this::process));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			error("%s process error: %s", annotations, e);
			return false;
		}
	}

	protected void note(String msg) {
		messager.printMessage(NOTE, msg);
	}

	protected void note(String format, Object... args) {
		note(String.format(format, args));
	}

	protected void warning(String msg) {
		messager.printMessage(WARNING, msg);
	}

	protected void warning(String format, Object... args) {
		warning(String.format(format, args));
	}

	protected void error(String msg) {
		messager.printMessage(ERROR, msg);
	}

	protected void error(String format, Object... args) {
		error(String.format(format, args));
	}

}
