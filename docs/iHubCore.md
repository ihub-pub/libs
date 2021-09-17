> `ihub-core`组件主要包含`ihub`组件集所需的通用属性以及通用工具。

## 组件安装

```groovy
implementation "pub.ihub.lib:ihub-core:${version}"
```

## 使用示例

### ObjectBuilder

> 自定义文件集配置处理器，用于自定义组件配置文件

```java
class IHubConfig extends BaseConfigEnvironmentPostProcessor {

	/**
	 * 配置文件集
	 */
	@Override
	protected String getActiveProfile() {
		return "ihub";
	}

	/**
	 * 自定义属性
	 */
	@Override
	protected Map<String, Object> getCustomizeProperties() {
		return MapUtil.<String, Object>builder("ihub-libs.version", "1.0.0").build();
	}

}
```