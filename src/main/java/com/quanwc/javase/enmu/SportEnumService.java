package com.quanwc.javase.enmu;

/**
 * Created by quanwenchao
 * 2018/7/26 10:23:15
 */
public class SportEnumService {


    /**
     * 枚举常用方法
     * toString()、name()、getDeclaringClass()、SportEnum.valueOf()
     */
    public static void basicMethod() {
        System.out.print("e.toString(): ");
        for (SportEnum e : SportEnum.values()) {
            System.out.print(e.toString() + " "); // 返回枚举常量的名称：Basketball Football Billiards
        }

        System.out.print("\ne.name(): ");
        for (SportEnum e : SportEnum.values()) {
            System.out.print(e.name() + " "); // 与上面的toString()方法类似，返回枚举常量的名称。因为：toString()源码中都是返回Enmu类的name属性值
        }

        System.out.print("\ne.numId: ");
        for (SportEnum e : SportEnum.values()) {
            System.out.print(e.getNumId() + " ");
        }

        System.out.println("\n" + SportEnum.Basketball.getDeclaringClass().getName()); // 该枚举常量的对应的Class对象

        System.out.println(SportEnum.Basketball.getNumId()); // 1


        SportEnum like = SportEnum.valueOf("Basketball"); // 返回带指定名称的指定枚举类型的枚举常量，找不到会抛异常：IllegalArgumentException
        System.out.println("basketball-numId: " + like.getNumId());

        System.out.println("0000000000000: " + SportEnum.Basketball.name());
    }



    /**
     * 比较枚举对象之间的顺序
     */
    public static void enumCompareTo() {

        SportEnum football = SportEnum.Football;
        SportEnum billiards = SportEnum.Billiards;

        System.out.println(football.compareTo(billiards)); // -1

        switch (football.compareTo(billiards)) { // 比较枚举对象之间的顺序：返回 -1、0、1
            case -1:
                System.out.println("before");
                break;
            case 1:
                System.out.println("after");
                break;
            default:
                System.out.println("equals");
                break;
        }

    }

    /**
     * 返回枚举常量的序数
     */
    public static void enumOrdinal() {

        System.out.println("Basketball: " + SportEnum.Basketball.ordinal()); // 0
        System.out.println("Football: " + SportEnum.Football.ordinal()); // 1
        System.out.println("Billiards: " + SportEnum.Billiards.ordinal()); // 2

    }

    public static void main(String[] args) {

        basicMethod();

        //enumCompareTo();

        //enumOrdinal();
    }
}
