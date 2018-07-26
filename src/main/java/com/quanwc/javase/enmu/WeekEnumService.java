package com.quanwc.javase.enmu;

/**
 * Created by quanwenchao
 * 2018/7/26 10:11:13
 */
public class WeekEnumService {


    /**
     * 计算日期
     */
    public static void computeDate() {
        for (WeekEnum e : WeekEnum.values()) {
            System.out.println(e.name());
        }
        System.out.println("=================");


        WeekEnum wednesday = WeekEnum.WEDNESDAY;
        switch (wednesday) {
            case MONDAY:
                System.out.println("one");
                break;
            case TUESDAY:
                System.out.println("two");
                break;
            case WEDNESDAY:
                System.out.println("three");
                break;
            default:
                System.out.println("other");
        }
    }

    public static void main(String[] args) {
        computeDate();
    }
}
