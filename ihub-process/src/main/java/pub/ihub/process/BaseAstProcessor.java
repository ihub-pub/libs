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

import javax.annotation.processing.ProcessingEnvironment;

/**
 * 基础AST注解处理器
 *
 * @author henry
 */
@SuppressWarnings("unused")
public abstract class BaseAstProcessor extends BaseProcessor {

//	protected JavacTrees javacTrees;
//	protected TreeMaker treeMaker;
//	protected Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
//		javacTrees = JavacTrees.instance(processingEnvironment);
//		Context context = ((JavacProcessingEnvironment) processingEnvironment).getContext();
//		treeMaker = TreeMaker.instance(context);
//		names = Names.instance(context);
    }

}
