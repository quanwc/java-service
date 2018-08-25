package com.quanwc.javase.fundament;

import java.util.Random;

/**
 * Created by quanwenchao
 * 2018/8/16 10:12:31
 */
public class RandomDemo {

    /**
     * 生成[1, max]之间的随机数
     */
    public static Integer getRandomNumber(Integer max) {
        Random rd = new Random();
        return rd.nextInt(max) + 1;
    }


    /**
     * 生成[x, y]之间的随机数
     * @return [x, y]之间的随机数
     */
    public static Integer getRandomNumber2() {
        Integer min = 200;
        Integer max = 500;

        Random random = new  Random();

        /**
         * random.nextInt(max) % (max-min+1)  ->  [0, 499] % 301 == [0, 300]
         * [0, 300] + 200 = [200, 500]
         */
        int result = random.nextInt(max) % (max-min+1) + min;
        return result;
    }

    public static void main(String[] args) {

        // [1, max]的随机数
        /*for (int i = 0; i < 10; ++i) {
            System.out.println(getRandomNumber(5));
        }*/

        for (int i=0; i<10; ++i) {
            System.out.println(getRandomNumber2());
        }
    }
}
