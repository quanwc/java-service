package com.quanwc.javase.collection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by quanwenchao
 * 2018/7/26 16:54:59
 */
public class HashSetDemo {
    public static void main(String[] args) {

        Object obj = new Object();
        System.out.println(obj.hashCode());

        String str = "";
        System.out.println(str.hashCode());

        List<Integer> list = new ArrayList<>();
        System.out.println(list.hashCode());




        HashSet<String> set1 = new HashSet();
        boolean flag1 = set1.add("111");
        boolean flag2 = set1.add("111");
        boolean flag3 = set1.add("111");
        System.out.println(set1.toString());
        set1.clear();

    }
}
