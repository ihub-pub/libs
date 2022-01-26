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

import com.google.auto.service.AutoService;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static javax.lang.model.SourceVersion.RELEASE_11;

/**
 * 实体文档注解处理器
 *
 * @author henry
 */
@AutoService(Processor.class)
@SupportedSourceVersion(RELEASE_11)
@SupportedAnnotationTypes({"lombok.Data", "lombok.Getter", "lombok.Setter"})
public class EntityDocProcessor extends BaseDocProcessor {

	@Override
	protected void processElement(Element element) {
		javacTrees.getTree(element).accept(new TreeTranslator() {
			@Override
			public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
				super.visitClassDef(jcClassDecl);
				appendSchema((TypeElement) element, jcClassDecl);
			}
		});
	}

}
