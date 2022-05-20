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
package pub.ihub.demo.service3.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liheng
 */
@RestController
public class DemoResourceController {

	@RequestMapping("/demo")
	public String index() {
		return "resource demo " + StpUtil.isLogin();
	}

	@RequestMapping("/hello1")
	public Mono<Map<String, String>> hello1(@RequestParam("text") String text) {
		return Mono.just(new HashMap<>(1) {{
			put("text", "Hello " + text);
		}});
	}

	@RequestMapping("/hello2")
	public Map<String, String> hello2(@RequestParam("text") String text) {
		return new HashMap<>(1) {{
			put("text", "Hello " + text);
		}};
	}

}
