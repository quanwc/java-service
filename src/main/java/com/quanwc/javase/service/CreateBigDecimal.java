package com.quanwc.javase.service;

import java.math.BigDecimal;

/**
 * 创建BigDecimal对象
 * Created by quanwenchao
 * 2018/7/23 18:58:59
 */
public class CreateBigDecimal {

    /**
     * 将Double转为BigDecimal
     *      i>:   new BigDecimal(Double param)，有精度损失，不建议使用
     *      ii>:  new BigDecimal(String param)， 建议优先使用String
     *      iii>: BigDecimal.valueOf(double param)
     */
    public static void createBigDecimalObj() {
        Double num1 = 1.23;
        BigDecimal numBd1 = new BigDecimal(num1);
        System.out.println(numBd1); // 1.229999999999999982236431605997495353221893310546875


        Double num2 = 1.23;
        BigDecimal numBd2 = new BigDecimal(num2.toString());
        System.out.println(numBd2); // 1.23


        BigDecimal numBd3 = BigDecimal.valueOf(1.23);
        System.out.println(numBd3); // 1.23

        Double doubleBd = numBd3.doubleValue(); // 将BigDecimal转为Double
        System.out.println("doubleBd: " + doubleBd);
    }


    public static void main(String[] args) {
        createBigDecimalObj();
    }

}
