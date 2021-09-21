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

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 对象DSL构建工具
 *
 * @param <T> 对象类型
 * @author henry
 */
@RequiredArgsConstructor
public final class ObjectBuilder<T> {

	private final T object;

	/**
	 * 获取对象构建器
	 *
	 * @param object 构建对象
	 * @param <T>    对象类型
	 * @return 对象构建器
	 */
	public static <T> ObjectBuilder<T> builder(T object) {
		return new ObjectBuilder<>(object);
	}

	/**
	 * 获取对象构建器
	 *
	 * @param clazz 构建对象类
	 * @param <T>   对象类型
	 * @return 对象构建器
	 */
	@SneakyThrows
	public static <T> ObjectBuilder<T> builder(Class<T> clazz) {
		return new ObjectBuilder<>(clazz.getDeclaredConstructor().newInstance());
	}

	/**
	 * 获取对象构建器
	 *
	 * @param supplier 生成对象方法
	 * @param <T>      对象类型
	 * @return 对象构建器
	 */
	public static <T> ObjectBuilder<T> builder(Supplier<T> supplier) {
		return new ObjectBuilder<>(supplier.get());
	}

	/**
	 * 获取对象构建器
	 *
	 * @param constructor 对象构造方法
	 * @param parameter   对象构造方法参数
	 * @param <T>         对象类型
	 * @param <P>         对象构造方法参数类型
	 * @return 对象构建器
	 */
	public static <T, P> ObjectBuilder<T> builder(Function<P, T> constructor, P parameter) {
		return new ObjectBuilder<>(constructor.apply(parameter));
	}

	/**
	 * 获取克隆对象构建器
	 *
	 * @param object 构建对象
	 * @param <T>    对象类型
	 * @return 对象构建器
	 */
	public static <T> ObjectBuilder<T> clone(T object) {
		return builder(ObjectUtil.clone(object));
	}

	//<editor-fold desc="给对象赋值">

	/**
	 * 给对象赋值
	 *
	 * @param setter 赋值方法
	 * @param value  值
	 * @param <V>    值类型
	 * @return 对象构建器
	 */
	public <V> ObjectBuilder<T> set(BiConsumer<T, V> setter, V value) {
		setter.accept(object, value);
		return this;
	}

	/**
	 * 给对象赋值（带值转换器）
	 *
	 * @param setter         赋值方法
	 * @param value          值
	 * @param valueConverter 值转换器
	 * @param <S>            原始值类型
	 * @param <V>            转换值类型
	 * @return 对象构建器
	 */
	public <S, V> ObjectBuilder<T> set(BiConsumer<T, V> setter, S value, Converter<S, V> valueConverter) {
		return set(setter, valueConverter.convert(value));
	}

	/**
	 * 给对象赋值（外部条件）
	 *
	 * @param condition 条件
	 * @param setter    赋值方法
	 * @param value     值
	 * @param <V>       值类型
	 * @return 对象构建器
	 */
	public <V> ObjectBuilder<T> set(boolean condition, BiConsumer<T, V> setter, V value) {
		if (condition) {
			return set(setter, value);
		}
		return this;
	}

	/**
	 * 给对象赋值（外部条件）
	 *
	 * @param condition      条件
	 * @param setter         赋值方法
	 * @param value          值
	 * @param valueConverter 值转换器
	 * @param <S>            原始值类型
	 * @param <V>            转换值类型
	 * @return 对象构建器
	 */
	public <S, V> ObjectBuilder<T> set(boolean condition, BiConsumer<T, V> setter,
									   S value, Converter<S, V> valueConverter) {
		if (condition) {
			return set(setter, value, valueConverter);
		}
		return this;
	}

	/**
	 * 给对象赋值（内部条件）
	 *
	 * @param predicate 判断条件
	 * @param setter    赋值方法
	 * @param value     值
	 * @param <V>       值类型
	 * @return 对象构建器
	 */
	public <V> ObjectBuilder<T> set(Predicate<V> predicate, BiConsumer<T, V> setter, V value) {
		return set(predicate.test(value), setter, value);
	}

	/**
	 * 给对象赋值（带值转换器）
	 *
	 * @param predicate      判断条件
	 * @param setter         赋值方法
	 * @param value          值
	 * @param valueConverter 值转换器
	 * @param <S>            原始值类型
	 * @param <V>            转换值类型
	 * @return 对象构建器
	 */
	public <S, V> ObjectBuilder<T> set(Predicate<S> predicate, BiConsumer<T, V> setter,
									   S value, Converter<S, V> valueConverter) {
		return set(predicate.test(value), setter, value, valueConverter);
	}

	/**
	 * 给对象赋值（带自定义异常）
	 *
	 * @param predicate      判断条件
	 * @param setter         赋值方法
	 * @param value          值
	 * @param valueConverter 值转换器
	 * @param error          异常
	 * @param <S>            原始值类型
	 * @param <V>            转换值类型
	 * @param <X>            异常类型
	 * @return 对象构建器
	 * @throws X 赋值异常
	 */
	public <S, V, X extends RuntimeException> ObjectBuilder<T> set(Predicate<S> predicate, BiConsumer<T, V> setter,
																   S value, Converter<S, V> valueConverter,
																   Supplier<X> error) throws X {
		try {
			return set(predicate, setter, value, valueConverter);
		} catch (Exception e) {
			e.printStackTrace();
			throw error.get();
		}
	}

	/**
	 * 给子对象赋值（内部条件）
	 *
	 * @param getter 获取属性方法
	 * @param map    赋值映射
	 * @param <S>    属性类型
	 * @param <V>    值类型
	 * @return 对象构建器
	 */
	public <S, V> ObjectBuilder<T> setSub(Function<T, S> getter, Map<BiConsumer<S, V>, V> map) {
		map.forEach((BiConsumer<S, V> setter, V value) -> setter.accept(getter.apply(object), value));
		return this;
	}

	/**
	 * 给子对象赋值（内部条件）
	 *
	 * @param getter 获取属性方法
	 * @param setter 赋值方法
	 * @param value  值
	 * @param <S>    属性类型
	 * @param <V>    值类型
	 * @return 对象构建器
	 */
	public <S, V> ObjectBuilder<T> setSub(Function<T, S> getter, BiConsumer<S, V> setter, V value) {
		return setSub(getter, MapUtil.of(setter, value));
	}

	/**
	 * 给子对象赋值（内部条件）
	 *
	 * @param condition 条件
	 * @param getter    获取属性方法
	 * @param setter    赋值方法
	 * @param value     值
	 * @param <S>       属性类型
	 * @param <V>       值类型
	 * @return 对象构建器
	 */
	public <S, V> ObjectBuilder<T> setSub(boolean condition, Function<T, S> getter, BiConsumer<S, V> setter, V value) {
		if (condition) {
			return setSub(getter, setter, value);
		} else {
			return this;
		}
	}

	/**
	 * 给子对象赋值（内部条件）
	 *
	 * @param predicate 判断条件
	 * @param getter    获取属性方法
	 * @param setter    赋值方法
	 * @param value     值
	 * @param <S>       属性类型
	 * @param <V>       值类型
	 * @return 对象构建器
	 */
	public <S, V> ObjectBuilder<T> setSub(Predicate<V> predicate, Function<T, S> getter, BiConsumer<S, V> setter, V value) {
		return setSub(predicate.test(value), getter, setter, value);
	}

	/**
	 * 子对象操作
	 *
	 * @param getter  获取属性方法
	 * @param setters 操作方法
	 * @param <S>     属性类型
	 * @return 对象构建器
	 */
	@SafeVarargs
	public final <S> ObjectBuilder<T> setSub(Function<T, S> getter, Consumer<S>... setters) {
		Arrays.stream(setters).forEach((setter) -> setter.accept(getter.apply(object)));
		return this;
	}

	//</editor-fold>

	//<editor-fold desc="集合对象添加值">

	/**
	 * 集合对象赋值
	 *
	 * @param setter      赋值方法
	 * @param supplier    原始集合
	 * @param accumulator 累加方法
	 * @param value       值
	 * @param <I>         集合类型
	 * @param <V>         值类型
	 * @return 对象构建器
	 */
	@SafeVarargs
	public final <I extends Iterable<V>, V> ObjectBuilder<T> set(BiConsumer<T, I> setter, Supplier<I> supplier,
																 BiConsumer<I, V> accumulator, V... value) {
		I collection = supplier.get();
		Arrays.stream(value).forEach((v) -> accumulator.accept(collection, v));
		setter.accept(object, collection);
		return this;
	}

	/**
	 * 集合对象赋值
	 *
	 * @param condition   条件
	 * @param setter      赋值方法
	 * @param supplier    原始集合
	 * @param accumulator 累加方法
	 * @param value       值
	 * @param <I>         集合类型
	 * @param <V>         值类型
	 * @return 对象构建器
	 */
	public <I extends Iterable<V>, V> ObjectBuilder<T> set(boolean condition, BiConsumer<T, I> setter,
														   Supplier<I> supplier, BiConsumer<I, V> accumulator, V value) {
		if (condition) {
			return set(setter, supplier, accumulator, value);
		} else {
			return this;
		}
	}

	/**
	 * 集合对象赋值
	 *
	 * @param predicate   判断条件
	 * @param setter      赋值方法
	 * @param supplier    原始集合
	 * @param accumulator 累加方法
	 * @param value       值
	 * @param <I>         集合类型
	 * @param <V>         值类型
	 * @return 对象构建器
	 */
	public <I extends Iterable<V>, V> ObjectBuilder<T> set(Predicate<V> predicate, BiConsumer<T, I> setter,
														   Supplier<I> supplier, BiConsumer<I, V> accumulator,
														   V value) {
		return set(predicate.test(value), setter, supplier, accumulator, value);
	}

	/**
	 * 集合对象添加值
	 *
	 * @param getter     原始集合
	 * @param collection 集合
	 * @param <C>        集合类型
	 * @param <V>        值类型
	 * @return 对象构建器
	 */
	public final <C extends Collection<V>, V> ObjectBuilder<T> add(Function<T, C> getter, C collection) {
		getter.apply(object).addAll(collection);
		return this;
	}

	/**
	 * 集合对象添加值
	 *
	 * @param getter 原始集合
	 * @param value  值
	 * @param <C>    集合类型
	 * @param <V>    值类型
	 * @return 对象构建器
	 */
	@SafeVarargs
	public final <C extends Collection<V>, V> ObjectBuilder<T> add(Function<T, C> getter, V... value) {
		getter.apply(object).addAll(Arrays.asList(value));
		return this;
	}

	/**
	 * 集合对象添加值
	 *
	 * @param condition 条件
	 * @param getter    原始集合
	 * @param value     值
	 * @param <C>       集合类型
	 * @param <V>       值类型
	 * @return 对象构建器
	 */
	public <C extends Collection<V>, V> ObjectBuilder<T> add(boolean condition, Function<T, C> getter, V value) {
		if (condition) {
			return add(getter, value);
		} else {
			return this;
		}
	}

	/**
	 * 集合对象添加值
	 *
	 * @param predicate 判断条件
	 * @param getter    原始集合
	 * @param value     值
	 * @param <C>       集合类型
	 * @param <V>       值类型
	 * @return 对象构建器
	 */
	public <C extends Collection<V>, V> ObjectBuilder<T> add(Predicate<V> predicate, Function<T, C> getter, V value) {
		return add(predicate.test(value), getter, value);
	}

	//</editor-fold>

	//<editor-fold desc="map对象添加值">

	/**
	 * 集合对象添加值
	 *
	 * @param getter 原始集合
	 * @param key    键
	 * @param value  值
	 * @param <K>    键值类型
	 * @param <V>    值类型
	 * @return 对象构建器
	 */
	public <K, V> ObjectBuilder<T> put(Function<T, Map<K, V>> getter, K key, V value) {
		getter.apply(object).put(key, value);
		return this;
	}

	/**
	 * 集合对象添加值
	 *
	 * @param condition 条件
	 * @param getter    原始集合
	 * @param key       键
	 * @param value     值
	 * @param <K>       键值类型
	 * @param <V>       值类型
	 * @return 对象构建器
	 */
	public <K, V> ObjectBuilder<T> put(boolean condition, Function<T, Map<K, V>> getter, K key, V value) {
		if (condition) {
			put(getter, key, value);
		}
		return this;
	}

	/**
	 * 集合对象添加值
	 *
	 * @param predicate 判断条件
	 * @param getter    原始集合
	 * @param key       键
	 * @param value     值
	 * @param <K>       键值类型
	 * @param <V>       值类型
	 * @return 对象构建器
	 */
	public <K, V> ObjectBuilder<T> put(BiPredicate<K, V> predicate, Function<T, Map<K, V>> getter, K key, V value) {
		return put(predicate.test(key, value), getter, key, value);
	}

	/**
	 * 集合对象添加值
	 *
	 * @param getter 原始集合
	 * @param map    集合
	 * @param <K>    键值类型
	 * @param <V>    值类型
	 * @return 对象构建器
	 */
	public <K, V> ObjectBuilder<T> putAll(Function<T, Map<K, V>> getter, Map<K, V> map) {
		getter.apply(object).putAll(map);
		return this;
	}

	//</editor-fold>

	/**
	 * 返回构建对象
	 *
	 * @return 对象
	 */
	public T build() {
		return object;
	}

}
