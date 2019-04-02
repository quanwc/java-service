package com.quanwc.web.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

	@Test
	public void contextLoads() {
		int i = 0;
		boolean flag = false;
		flag = ((++i) + (i++) == 2) ? true : false;
		System.out.println("i=" + i);
		System.out.println("flag=" + flag);
	}

}
