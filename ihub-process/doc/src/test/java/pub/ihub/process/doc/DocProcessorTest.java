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
package pub.ihub.process.doc;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.ArrayList;

/**
 * @author henry
 */
@DisplayName("基础文档注解处理器测试")
class DocProcessorTest {

	@DisplayName("Controller文档注解处理器测试-成功")
	@Test
	void controller() {
		ControllerDocProcessor processor = new ControllerDocProcessor();
		processor.init(new MockProcessingEnvironment());
		processor.process(new MockElement());
	}

	@DisplayName("实体文档注解处理器测试-成功")
	@Test
	void entity() {
		EntityDocProcessor processor = new EntityDocProcessor();
		processor.init(new MockProcessingEnvironment());
		processor.process(new MockElement());
	}

	//<editor-fold desc="Mock Element">

	static class MockJavacTrees extends JavacTrees {

		protected MockJavacTrees(Context context) {
			super(context);
		}

		@Override
		public JCTree getTree(Element element) {
			switch (element.getKind()) {
				case CLASS:
					return new MockJcClassDecl();
				case METHOD:
					return new MockJcMethodDecl(((MockMethodSymbol) element).annotation, ((MockMethodSymbol) element).kind);
				case FIELD:
					return new MockJcVariableDecl();
				default:
					return null;
			}
		}
	}

	static class MockProcessingEnvironment extends JavacProcessingEnvironment {

		protected MockProcessingEnvironment() {
			super(new Context());
			getContext().put(JavacTrees.class, (JavacTrees) null);
			getContext().put(JavacTrees.class, new MockJavacTrees(getContext()));
		}

		@Override
		public JavacElements getElementUtils() {
			return new MockElements(new Context());
		}

	}

	static class MockElement extends Symbol.ClassSymbol {

		public MockElement() {
			super(0, null, new Type.ClassType(null, null, null), null);
		}

		@Override
		public java.util.List<Symbol> getEnclosedElements() {
			return java.util.List.of(
				new MockMethodSymbol(this, "@RequestMapping", Tree.Kind.IDENTIFIER),
				new MockMethodSymbol(this, "@RequestMapping", Tree.Kind.ANNOTATED_TYPE),
				new MockMethodSymbol(this, "@Demo", Tree.Kind.ANNOTATED_TYPE),
				new MockVarSymbol(this),
				new MockClassSymbol(this, "MockClassType")
			);
		}

	}

	static class MockMethodSymbol extends Symbol.MethodSymbol {

		final String annotation;
		final Tree.Kind kind;

		public MockMethodSymbol(Symbol owner, String annotation, Tree.Kind kind) {
			super(0, null, null, owner);
			this.annotation = annotation;
			this.kind = kind;
		}

		@Override
		public ElementKind getKind() {
			return ElementKind.METHOD;
		}

	}

	static class MockVarSymbol extends Symbol.VarSymbol {

		public MockVarSymbol(Symbol owner) {
			super(0, null, null, owner);
		}

		@Override
		public ElementKind getKind() {
			return ElementKind.FIELD;
		}

	}

	static class MockClassSymbol extends Symbol.ClassSymbol {

		private final CharSequence name;

		public MockClassSymbol(Symbol owner, CharSequence name) {
			super(0, null, null, owner);
			this.name = name;
		}

		@Override
		public ElementKind getKind() {
			return "MockClassType".contentEquals(name) ? ElementKind.CLASS : ElementKind.OTHER;
		}

		@Override
		public java.util.List<Symbol> getEnclosedElements() {
			return new ArrayList<>();
		}

		@Override
		public String toString() {
			return "MockClassSymbol";
		}
	}

	static class MockJcClassDecl extends JCTree.JCClassDecl {

		protected MockJcClassDecl() {
			super(new MockJcModifiers(), null, List.nil(), null, null, null, null);
		}

	}

	static class MockJcMethodDecl extends JCTree.JCMethodDecl {

		protected MockJcMethodDecl(String annotation, Kind kind) {
			super(new MockJcModifiers(annotation), null, new MockReturnType(kind), null, null,
				List.of(new MockJcVariableDecl(), new MockJcVariableDecl(Kind.ANNOTATION_TYPE)),
				null, null, null, null);
		}
	}

	static class MockJcModifiers extends JCTree.JCModifiers {

		protected MockJcModifiers(String annotation) {
			super(0, List.nil());
			annotations = annotations.append(new MockJcAnnotation(annotation));
		}

		protected MockJcModifiers() {
			this("@Other");
		}
	}

	static class MockJcVariableDecl extends JCTree.JCVariableDecl {

		protected MockJcVariableDecl(Kind kind) {
			super(new MockJcModifiers(), new MockNameImpl(), null, null, null);
			vartype = new ParamType(kind);
		}

		protected MockJcVariableDecl() {
			this(Kind.IDENTIFIER);
		}
	}

	static class MockNameImpl extends Name {

		protected MockNameImpl() {
			super(null);
		}

		@Override
		public int getIndex() {
			return 0;
		}

		@Override
		public int getByteLength() {
			return 0;
		}

		@Override
		public byte getByteAt(int i) {
			return 0;
		}

		@Override
		public byte[] getByteArray() {
			return new byte[0];
		}

		@Override
		public int getByteOffset() {
			return 0;
		}
	}

	static class MockJcAnnotation extends JCTree.JCAnnotation {

		private final String text;

		protected MockJcAnnotation(String text) {
			super(null, null, null);
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	static class ParamType extends JCTree.JCIdent {

		private final Kind kind;

		protected ParamType(Kind kind) {
			super(null, null);
			type = new MockArrayType();
			this.kind = kind;
		}

		@Override
		public Kind getKind() {
			return kind;
		}
	}

	static class MockReturnType extends JCTree.JCIdent {

		final Kind kind;

		protected MockReturnType(Kind kind) {
			super(null, null);
			type = new MockClassType();
			this.kind = kind;
		}

		@Override
		public Kind getKind() {
			return kind;
		}
	}

	static class MockClassType extends Type.ClassType {

		public MockClassType() {
			super(null, null, null);
		}

		@Override
		public String toString() {
			return "MockClassType";
		}
	}

	static class MockArrayType extends Type.ArrayType {

		public MockArrayType() {
			super(null, null);
		}

		@Override
		public String toString() {
			return "MockTypeVar";
		}
	}

	static class MockElements extends JavacElements {

		protected MockElements(Context context) {
			super(context);
		}

		@Override
		public String getDocComment(Element e) {
			return " 获取用户\n" +
				" @param a 参数a\n" +
				" @param b 参数b\n" +
				" @return 用户";
		}

		@Override
		public Symbol.ClassSymbol getTypeElement(CharSequence name) {
			return new MockClassSymbol(null, name);
		}

	}

	//</editor-fold>

}
