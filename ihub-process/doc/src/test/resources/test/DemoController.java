package test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试Controller
 */
@RestController
public class DemoController {

	/**
	 * 获取demo
	 *
	 * @return demo对象
	 */
	@GetMapping
	public Demo demo() {
		return null;
	}

	/**
	 * 保存demo
	 *
	 * @param demo demo对象
	 * @return 结果
	 */
	@PostMapping
	public String demo(@RequestBody Demo demo) {
		return "ok";
	}

	/**
	 * 修改demo
	 *
	 * @param id   ID
	 * @param demo demo对象
	 * @return 结果
	 */
	@GetMapping("{id}")
	public int demo(@PathVariable long id, @RequestBody Demo demo) {
		return 0;
	}

	private void otherMethod() {
	}

}
