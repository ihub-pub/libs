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

import cn.hutool.core.text.CharSequenceUtil;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import pub.ihub.process.BaseAstProcessor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * 基础文档注解处理器
 *
 * @author henry
 */
public abstract class BaseDocProcessor extends BaseAstProcessor {

	protected void processEntity(JCTree ident) {
		TypeElement element = elementUtils.getTypeElement(ident.type.toString());
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

	protected void appendAnnotation(Element element, JCTree.JCModifiers mods, Class<?> annotation,
									BiFunction<String, Map<String, java.util.List<String>>, List<JCTree.JCExpression>> argsGetter) {
		appendAnnotation(element, mods, annotation, doc -> argsGetter.apply(doc, getTags(doc)));
	}

	protected void appendAnnotation(Element element, JCTree.JCModifiers mods, Class<?> annotation,
									Function<String, List<JCTree.JCExpression>> argsGetter) {
		String doc = elementUtils.getDocComment(element);
		if (CharSequenceUtil.isNotBlank(doc)) {
			if (mods.annotations.stream().noneMatch(a -> a.toString().contains(annotation.getSimpleName()))) {
				mods.annotations = mods.annotations.append(makeAnnotation(annotation, argsGetter.apply(doc)));
			} else {
				note("%s %s %s annotation is exist!", element.getKind(), element, annotation.getCanonicalName());
			}
		} else {
			warning("%s %s doc comment is miss!", element.getKind(), element);
		}
	}

	protected JCTree.JCExpression makeArg(String key, String value) {
		return treeMaker.Assign(treeMaker.Ident(names.fromString(key)), treeMaker.Literal(value));
	}

	protected JCTree.JCAnnotation makeAnnotation(Class<?> annotationClass, List<JCTree.JCExpression> args) {
		JCTree.JCExpression expression = chainDots(annotationClass.getCanonicalName().split("\\."));
		return treeMaker.Annotation(expression, args);
	}

	private JCTree.JCExpression chainDots(String... elems) {
		JCTree.JCExpression e = null;
		for (String elem : elems) {
			e = e == null ? treeMaker.Ident(names.fromString(elem)) : treeMaker.Select(e, names.fromString(elem));
		}
		return e;
	}

	private Map<String, java.util.List<String>> getTags(String doc) {
		Map<String, java.util.List<String>> tags = new HashMap<>(0);
		Pattern pattern = compile("@(\\w+) (.*)");
		Matcher matcher = pattern.matcher(doc);
		matcher.results().forEach(matchResult -> {
			var values = tags.getOrDefault(matchResult.group(1), new ArrayList<>());
			values.add(matchResult.group(2));
			tags.putIfAbsent(matchResult.group(1), values);
		});
		return tags;
	}

}
