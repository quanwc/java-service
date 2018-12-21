package com.quanwc.javase.lambda.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author quanwenchao
 * @date 2018/7/10 8:55:29
 */
public class LambdaService {
	public static void main(String[] args) {

		test1();
		test2();

	}


	public static void test1() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("runnable");
			}
		}).start();

		new Thread(() -> System.out.println("lambda")).start();
	}

	public static void test2() {
		List<Integer> list = Arrays.asList(4, 5, 6, 1, 2, 3, 7, 8, 8, 9, 10);

		List<Integer> evenList = list.stream().filter(i -> i % 2 == 0)
				.collect(Collectors.toList()); // 过滤出偶数列表 [4,6,8,8,10]<br>
		System.out.println(evenList);

		List<Integer> sortList = list.stream().sorted(
				Comparator.comparing(integer -> integer.hashCode())
		).limit(5)
				.collect(Collectors.toList());// 排序并且提取出前5个元素 [1,2,3,4,5]
		System.out.println(sortList);

	}
}
