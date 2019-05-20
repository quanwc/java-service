package com.quanwc.javase.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * @author quanwenchao
 * @date 2019/4/13 19:58:42
 */
public class Main2 {

	public static Integer getZeroLength(List<Integer> paramList) {
		Integer result = 0;
		for (Integer x : paramList) {
			if (x == 0) {
				result++;
			}
		}
		return result;
	}

	public static void print(List<Integer> numList) {

		int result = 0;
		int[] arr = new int[] {};

		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == 0) {
				for (int j = i; j < arr.length; j++) {
					if (arr[j] == 0) {
						result++;
					}
				}
			}

		}
	}



	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		String line;
		ArrayList<String> array = new ArrayList<String>();
		while (!"end".equals(line = scan.nextLine())) {
			array.add(line);
		}

		Integer num = Integer.parseInt(array.get(0)); // 3
		System.out.println("num=" + num);

		String string = array.get(1);
		String[] strings = string.split(" ");
		List<Integer> numList = new ArrayList(); // 2 0 1
		for (String str : strings) {
			numList.add(Integer.parseInt(str));
		}
	}
}
