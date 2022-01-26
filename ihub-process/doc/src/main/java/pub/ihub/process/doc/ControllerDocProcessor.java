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
import com.sun.tools.javac.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.Arrays;

import static com.sun.source.tree.Tree.Kind.IDENTIFIER;
import static javax.lang.model.SourceVersion.RELEASE_11;

/**
 * Controller文档注解处理器
 *
 * @author henry
 */
@AutoService(Processor.class)
@SupportedSourceVersion(RELEASE_11)
@SupportedAnnotationTypes("org.springframework.web.bind.annotation.RestController")
public class ControllerDocProcessor extends BaseDocProcessor {

	private static final String[] MAPPING_ANNOTATIONS = new String[]{
		"@RequestMapping", "@GetMapping", "@PostMapping", "@PutMapping", "@PatchMapping"
	};

	@Override
	protected void processElement(Element element) {
		javacTrees.getTree(element).accept(new TreeTranslator() {
			@Override
			public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
				super.visitClassDef(jcClassDecl);
				appendAnnotation(element, jcClassDecl.mods, Tag.class, doc -> List.of(
					makeArg("name", doc.replaceAll("\\n.*", "").trim()),
					makeArg("description", doc.replaceAll("\\s|<.*>|@.*", ""))
				));
				element.getEnclosedElements().stream().filter(e -> ElementKind.METHOD == e.getKind()).forEach(e -> {
					JCTree.JCMethodDecl tree = (JCTree.JCMethodDecl) javacTrees.getTree(e);
					if (Arrays.stream(MAPPING_ANNOTATIONS)
						.anyMatch(name -> tree.mods.annotations.stream().anyMatch(a -> a.toString().startsWith(name)))) {
						appendAnnotation(e, tree.mods, Operation.class, doc -> List.of(
							makeArg("summary", doc.replaceAll("\\n.*", "").trim()),
							makeArg("description", doc.replaceAll("@.*", ""))
						));
						appendAnnotation(e, tree.mods, ApiResponse.class, (doc, tags) -> List.of(
							makeArg("responseCode", "200"), makeArg("description", String.join("", tags.get("return")))
						));
						tree.params.forEach(param -> {
							appendAnnotation(e, param.mods, Parameter.class, (doc, tags) -> List.of(
								makeArg("name", param.name.toString()), makeArg("description", tags.get("param").stream()
									.filter(p -> p.startsWith(param.name.toString() + " "))
									.findFirst().orElse("").replaceAll(".* ", ""))
							));
							if (IDENTIFIER == param.vartype.getKind()) {
								processEntity(param.vartype);
							}
						});
						if (IDENTIFIER == tree.getReturnType().getKind()) {
							processEntity(tree.getReturnType());
						}
					}
				});
			}
		});
	}

}
