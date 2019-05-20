package com.quanwc.javase.test;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author quanwenchao
 * @date 2019/4/13 17:29:01
 */
public class Main {
		// Scanner scan= new Scanner(System.in);
		// while (scan.hasNextLine()) {
		// System.out.println("please input num:");
		// String str2 = scan.nextLine();
		// }
		// scan.close();

//		Scanner scan = new Scanner(System.in);
//		String line;
//		String strings = "";
//		ArrayList<String> array = new ArrayList<String>();
//		while (!"end".equals(line = scan.nextLine())) {
//			array.add(line);
//			strings += line + " ";
//		}
//		for (String str : array) {
//			System.out.println("str: " + str);
//		}

    public static void selectionSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n; i++) {
            int k = i;
            // 找出最小值的下标
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[k]) {
                    k = j;
                }
            }
            // 将最小值放到未排序记录的第一个位置
            if (k > i) {
                int tmp = a[i];
                a[i] = a[k];
                a[k] = tmp;
            }

            if (i==2) {
                for (int j : a)
                    System.out.println(j + " ");
            }

        }
    }

    public static void main(String[] args) {
        int[] b = { 900, 512, 613, 700, 810};
        selectionSort(b);
        for (int i : b)
            System.out.print(i + " ");
    }
}