package com.quanwc.javase.oo.objectMethod;

/**
 * Created by quanwenchao
 * 2018/7/31 10:38:44
 */
public class HashCode {
    public static void main(String[] args) {

        Object obj1 = new Object();
        System.out.println(obj1.hashCode()); // 1325547227

        Object obj2 = new Object();
        System.out.println(obj2.hashCode()); // 980546781

    }
}
