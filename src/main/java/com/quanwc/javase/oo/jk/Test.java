package com.quanwc.javase.oo.jk;

/**
 * Created by quanwenchao
 * 2018/7/31 16:47:42
 */
public class Test {
    public static void main(String[] args) {

        IComp compA = new CompImplA();
        String handleA = compA.handle();
        System.out.println(handleA); // handle CompImplA


        IComp compB = new CompImplB();
        String handleB = compB.handle();
        System.out.println(handleB); // handle CompImplB
    }
}
