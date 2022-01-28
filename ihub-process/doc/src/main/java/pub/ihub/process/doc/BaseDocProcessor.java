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

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import pub.ihub.process.BaseAstProcessor;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 * 基础文档注解处理器
 *
 * @author henry
 */
public abstract class BaseDocProcessor extends BaseAstProcessor {

	protected void processEntity(JCTree ident) {
		note(ident.type.toString());
		TypeElement element = elementUtils.getTypeElement(ident.type.toString());
		note("%s", element);
		JCTree.JCClassDecl tree = javacTrees.getTree(element);
		if (null != tree) {
			appendSchema(element, tree);
		}
	}

	protected void appendSchema(TypeElement element, JCTree.JCClassDecl tree) {
		appendAnnotation(element, tree.mods, Schema.class,
			doc -> List.of(makeArg("description", doc.replaceAll("@.*", "").trim())));
		element.getEnclosedElements().stream().filter(e -> ElementKind.FIELD == e.getKind()).forEach(e ->
			appendAnnotation(e, ((JCTree.JCVariableDecl) javacTrees.getTree(e)).mods, Schema.class,
				doc -> List.of(makeArg("description", doc.trim()))));
		element.getEnclosedElements().stream().filter(e -> ElementKind.CLASS == e.getKind())
			.forEach(e -> appendSchema((TypeElement) e, (JCTree.JCClassDecl) javacTrees.getTree(e)));
	}

}
