package com.quanwc.javase.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.quanwc.javase.lambda.model.entity.PhoneDO;

/**
 * @author quanwenchao
 * @date 2018/12/29 10:45:41
 */
public class ListDemo {

	public static void main(String[] args) {
		testAddAll();
		//testSubList();
	}

	/**
	 * 测试Optional.ofNullable()方法，避免空指针
	 */
	private static void testNull() {
		// 从db中查询数据
		//List<PhoneDO> dtbItems = directTxtBrdBusinessF.getByIds(liveIds);
		List<PhoneDO> dtbItems = new ArrayList<>();

		// 判断空指针，判断dtbItems是否为null，不为null返回dtbItems，否则orElse创建一个空的List
		dtbItems = Optional.ofNullable(dtbItems).orElse(new ArrayList<>(0));
	}

	/**
	 * 测试addAll()方法
	 */
	private static void testAddAll() {
		List<Integer> result = new ArrayList<>();

		// asList(): 数组转换成集合
		List<Integer> list1 = Arrays.asList(1, 2, 3, 4, 5);
		List<Integer> list2 = Arrays.asList(6, 7, 8, 9, 10);

		//list1.add(888);

		result.addAll(list1);
		result.addAll(list2);
		System.out.println(result);
	}

	/**
	 * 测试subList()方法
	 */
	private static void testSubList() {
		List<Integer> list1 = Arrays.asList(1, 2, 3, 4, 5);

		Integer limit = 5;
		list1 = list1.subList(0, limit);
		System.out.println(list1);
	}
}
