package test;

import lombok.Data;

/**
 * 测试
 */
@Data
public class Demo {

	/**
	 * ID
	 */
	private DemoId id;

	/**
	 * Demo ID
	 */
	@Data
	public class DemoId {

		/**
		 * ID
		 */
		private Long id;

	}

}
