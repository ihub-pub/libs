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

import cn.hutool.core.text.CharSequenceUtil;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * 基础AST注解处理器
 *
 * @author henry
 */
public abstract class BaseAstProcessor extends BaseProcessor {

	protected JavacTrees javacTrees;
	protected TreeMaker treeMaker;
	protected Names names;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		javacTrees = JavacTrees.instance(processingEnvironment);
		Context context = ((JavacProcessingEnvironment) processingEnvironment).getContext();
		treeMaker = TreeMaker.instance(context);
		names = Names.instance(context);
	}

	protected void appendAnnotation(Element element, JCModifiers mods, Class<?> annotation,
									BiFunction<String, Map<String, java.util.List<String>>, List<JCExpression>> argsGetter) {
		appendAnnotation(element, mods, annotation, doc -> argsGetter.apply(doc, getTags(doc)));
	}

	protected void appendAnnotation(Element element, JCModifiers mods, Class<?> annotation,
									Function<String, List<JCExpression>> argsGetter) {
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

	protected JCExpression makeArg(String key, String value) {
		return treeMaker.Assign(treeMaker.Ident(names.fromString(key)), treeMaker.Literal(value));
	}

	protected JCTree.JCAnnotation makeAnnotation(Class<?> annotationClass, List<JCExpression> args) {
		JCExpression expression = chainDots(annotationClass.getCanonicalName().split("\\."));
		return treeMaker.Annotation(expression, args);
	}

	private JCExpression chainDots(String... elems) {
		JCExpression e = null;
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
