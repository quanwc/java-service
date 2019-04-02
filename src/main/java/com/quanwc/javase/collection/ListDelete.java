package com.quanwc.javase.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author quanwenchao
 * @date 2019/1/15 16:19:45
 */
public class ListDelete {
	public static void main(String[] args) {

		//test1();

		test2();
	}

	static void test1() {

		List<String> list = new ArrayList<String>();
		list.add("1");
		list.add("2");

		Iterator<String> iterator = list.iterator();
		while (iterator.hasNext()) {
			String item = iterator.next();
			if ("1".equals(item)) {
				iterator.remove();
			}
		}

		System.out.println(list);
	}


	static void test2() {

		List<String> list = new ArrayList<String>();
		list.add("1");
		list.add("2");
		for (String item : list) {
			if ("2".equals(item)) {
				list.remove(item);
			}
		}

		System.out.println(list);
	}
}
