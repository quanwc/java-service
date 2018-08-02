package com.quanwc.javase.oo.jc;

/**
 * Created by quanwenchao
 * 2018/7/31 16:14:21
 */
public class Test {
    public static void main(String[] args) {

        //IComp base = new IComp();
        //String say = base.say();
        //System.out.println(say); // hello base

        Base objA = new DeriveB();
        String say = objA.say();
        System.out.println(say);

    }
}
