/*
 * Copyright (c) 2021 Henry 李恒 (henry.box@outlook.com).
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
package pub.ihub.core;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomLong;
import static cn.hutool.core.util.RandomUtil.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author henry
 */
@DisplayName("对象构建测试")
class ObjectBuilderTest {

	@DisplayName("获取对象构建器测试")
	@Test
	void builder() {
		assertNotNull(ObjectBuilder.builder("str"));
		assertNotNull(ObjectBuilder.builder(String.class));
		assertNotNull(ObjectBuilder.builder(String::new));
		assertNotNull(ObjectBuilder.builder(String::new, "str"));
		assertNotNull(ObjectBuilder.clone("str"));
		assertThrows(NoSuchMethodException.class, () -> ObjectBuilder.builder(ObjectBuilder.class));
	}

	@DisplayName("对象赋值测试")
	@Test
	void set() {
		var str = randomString(10);
		var demo = ObjectBuilder.builder(Demo.class).set(StrUtil::isNotBlank, Demo::setStr, str)
			.set(StrUtil::isNotBlank, Demo::setStr, " ")
			.set(false, Demo::setStr, 0L, StrUtil::toString).build();
		assertEquals(str, demo.getStr());

		str = StrUtil.toString(randomLong(10));
		demo = ObjectBuilder.builder(Demo.class)
			.set(StrUtil::isNotBlank, Demo::setNum, str, NumberUtil::parseLong, RuntimeException::new).build();
		assertEquals(NumberUtil.parseLong(str), demo.getNum());
		assertThrows(RuntimeException.class, () -> ObjectBuilder.builder(Demo.class)
			.set(StrUtil::isNotBlank, Demo::setNum, "error", NumberUtil::parseLong, RuntimeException::new).build());
	}

	@DisplayName("子对象赋值测试")
	@Test
	void subObj() {
		var str = randomString(10);
		var num = randomLong(10);
		var demo = ObjectBuilder.builder(Demo.class).set(Demo::setSub, new Demo())
			.setSub(StrUtil::isNotBlank, Demo::getSub, Demo::setStr, str)
			.setSub(StrUtil::isNotBlank, Demo::getSub, Demo::setStr, " ")
			.setSub(Demo::getSub, (d) -> d.setNum(num)).build();
		assertEquals(str, demo.getSub().getStr());
		assertEquals(num, demo.getSub().getNum());
	}

	@DisplayName("集合对象赋值测试")
	@Test
	void iterable() {
		var demo = ObjectBuilder.builder(Demo.class)
			.set(StrUtil::isNotBlank, Demo::setList, ArrayList::new, List::add, randomString(10))
			.set(StrUtil::isNotBlank, Demo::setList, ArrayList::new, List::add, " ")
			.add(StrUtil::isNotBlank, Demo::getList, randomString(10))
			.add(StrUtil::isNotBlank, Demo::getList, " ")
			.add(Demo::getList, new ArrayList<>()).build();
		assertEquals(2, demo.getList().size());
	}

	@DisplayName("map对象添加值测试")
	@Test
	void map() {
		var str = randomString(10);
		var demo = ObjectBuilder.builder(Demo.class).set(Demo::setMap, new HashMap<>(1))
			.put((k, v) -> StrUtil.isNotBlank(v), Demo::getMap, "str", str)
			.put((k, v) -> StrUtil.isNotBlank(v), Demo::getMap, "str", " ")
			.putAll(Demo::getMap, new HashMap<>(0)).build();
		assertEquals(str, demo.getMap().get("str"));
	}

}
