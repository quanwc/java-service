package com.quanwc.javase.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BigDecimal加减乘除运算: add()、subtract()、 multiply()、divide
 * Created by quanwenchao
 * 2018/7/25 15:11:34
 */
public class BigDecimalCompute {


    /**
     * 加法：add()
     */
    public static void add() {
        BigDecimal bd1 = new BigDecimal("1.001");
        BigDecimal bd2 = new BigDecimal("2.0001");

        BigDecimal addBd = bd1.add(bd2); // this + augend
        System.out.println("addBd: " + addBd.doubleValue()); // addBd: 3.0011
    }


    /**
     * 减法：subtract()
     */
    public static void subtract() {
        BigDecimal bd1 = new BigDecimal("11.001");
        BigDecimal bd2 = new BigDecimal("2.0001");

        BigDecimal subBd = bd1.subtract(bd2); // this- subtrahend: bd1 - bd2
        System.out.println("subBd: " + subBd.doubleValue()); // subBd: 9.0009
    }


    /**
     * 乘法：multiply()
     */
    public static void multiply() {
        BigDecimal bd1 = BigDecimal.valueOf(11.001);
        BigDecimal bd2 = BigDecimal.valueOf(2.0001);

        BigDecimal mulBd = bd1.multiply(bd2);
        System.out.println(mulBd.doubleValue()); // 22.0031001
    }

    /**
     * 除法：divide()
     *
     * @throws Non-terminating decimal expansion; no exact representable decimal result.
     *
     */
    public static void divide() {
        BigDecimal bd1 = BigDecimal.valueOf(11.001);
        BigDecimal bd2 = BigDecimal.valueOf(2);

        BigDecimal divideBd = bd1.divide(bd2, 3, RoundingMode.HALF_UP); // 被除数、小数点后3位、四舍五入
        System.out.println(divideBd.doubleValue());
    }


    /**
     * 比较大小：BigDecimal类型变量比较大小时用compareTo方法，判断变量值是否为0，与BigDecimal.ZERO比较大小
     *  @return -1, 0, or 1 as this {@code BigDecimal} is numerically less than, equal to, or greater than {@code val}.
     *
     *  eg: int result = bd1.compareTo(bd2);
     *      bd1 > bd2   result = 1
     *      bd1 = bd2   result = 0
     *      bd1 < bd2   result = -1
     */
    public static void compareTo() {
        BigDecimal bd1 = new BigDecimal(11.0001);
        BigDecimal bd2 = new BigDecimal(2.001);

        int result = bd1.compareTo(bd2);
        System.out.println("result: " + result);

        if (bd1.compareTo(bd2) < 0) {
            System.out.println("bd1 < bd2");
        } else if (bd1.compareTo(bd2) > 0) {
            System.out.println("bd1 > bd2");
        } else {
            System.out.println("bd1 == bd2");
        }


        System.out.println(BigDecimal.ZERO.doubleValue()); // 0.0

        // 与BigDecimal.ZERO比较大小
        int bdZero = bd2.compareTo(BigDecimal.ZERO);
        System.out.println(bdZero); // 1
    }

    /**
     * 保留小数点后3位
     */
    public static void format() {
        BigDecimal decimal = new BigDecimal("1.12365");
        BigDecimal decimal2 = decimal.setScale(3, BigDecimal.ROUND_HALF_UP); // 每一步运算时，都会产生一个新的对象，原来的decimal对象不变
        System.out.println(decimal2.doubleValue());
    }

    public static void main(String[] args) {

        // https://www.cnblogs.com/LeoBoy/p/6056394.html

        //add();

        //subtract();

        //multiply();

        //divide();

        //compareTo();

        //format();

    }

}
