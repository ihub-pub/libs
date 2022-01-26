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

import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * @author henry
 */
@DisplayName("基础AST注解处理器测试")
class AstProcessorTest {

	@DisplayName("AST注解处理测试-成功")
	@Test
	void process() {
		DemoAstProcessor processor = new DemoAstProcessor(" 获取用户\n" +
			" @param a 参数a\n" +
			" @param b 参数b\n" +
			" @return 用户");
		processor.init(JavacProcessingEnvironment.instance(new Context()));
		processor.process(Set.of(new MockElement()), new MockRoundEnvironment());
		// 模拟多次添加注解
		processor.process(Set.of(new MockElement()), new MockRoundEnvironment());
	}

	@DisplayName("AST注解处理测试-模拟没有注释")
	@Test
	void processMissDocComment() {
		DemoAstProcessor processor = new DemoAstProcessor(null);
		processor.init(JavacProcessingEnvironment.instance(new Context()));
		processor.process(Set.of(new MockElement()), new MockRoundEnvironment());
	}

	@DisplayName("AST注解处理测试-模拟异常")
	@Test
	void processException() {
		DemoAstProcessor processor = new DemoAstProcessor("doc");
		processor.init(JavacProcessingEnvironment.instance(new Context()));
		processor.process(null, null);
	}

	//<editor-fold desc="Mock Element">

	static class DemoAstProcessor extends BaseAstProcessor {

		private String docComment;
		private JCTree.JCModifiers mods;

		public DemoAstProcessor(String docComment) {
			this.docComment = docComment;
		}

		@Override
		public synchronized void init(ProcessingEnvironment processingEnvironment) {
			super.init(processingEnvironment);
			elementUtils = new MockElements(docComment);
			mods = treeMaker.Modifiers(0);
		}

		@Override
		protected void process(Element element) {
			note("test %s", "mock");
			appendAnnotation(element, mods, DisplayName.class,
				(doc, tags) -> com.sun.tools.javac.util.List.of(makeArg("value", doc)));
		}

	}

	static class MockElement implements TypeElement {

		@Override
		public TypeMirror asType() {
			return null;
		}

		@Override
		public ElementKind getKind() {
			return null;
		}

		@Override
		public Set<Modifier> getModifiers() {
			return null;
		}

		@Override
		public Name getSimpleName() {
			return null;
		}

		@Override
		public TypeMirror getSuperclass() {
			return null;
		}

		@Override
		public List<? extends TypeMirror> getInterfaces() {
			return null;
		}

		@Override
		public List<? extends TypeParameterElement> getTypeParameters() {
			return null;
		}

		@Override
		public Element getEnclosingElement() {
			return null;
		}

		@Override
		public List<? extends Element> getEnclosedElements() {
			return null;
		}

		@Override
		public NestingKind getNestingKind() {
			return null;
		}

		@Override
		public Name getQualifiedName() {
			return null;
		}

		@Override
		public List<? extends AnnotationMirror> getAnnotationMirrors() {
			return null;
		}

		@Override
		public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
			return null;
		}

		@Override
		public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
			return null;
		}

		@Override
		public <R, P> R accept(ElementVisitor<R, P> v, P p) {
			return null;
		}

	}

	static class MockRoundEnvironment implements RoundEnvironment {

		@Override
		public boolean processingOver() {
			return false;
		}

		@Override
		public boolean errorRaised() {
			return false;
		}

		@Override
		public Set<? extends Element> getRootElements() {
			return null;
		}

		@Override
		public Set<? extends Element> getElementsAnnotatedWith(TypeElement a) {
			return Set.of(new MockElement());
		}

		@Override
		public Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a) {
			return null;
		}
	}

	static class MockElements extends JavacElements {

		private final String docComment;

		protected MockElements(String docComment) {
			super(new Context());
			this.docComment = docComment;
		}

		@Override
		public String getDocComment(Element e) {
			return docComment;
		}

	}

	//</editor-fold>

}
