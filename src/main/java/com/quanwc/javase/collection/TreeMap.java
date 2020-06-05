package com.quanwc.javase.collection;

import java.time.LocalDateTime;

/**
 * @author quanwenchao
 * @date 2019/12/27 11:38:21
 */
public class TreeMap {
    public static void main(String[] args) {

        java.util.TreeMap<String, Double> nIndexMap = new java.util.TreeMap<>();
        nIndexMap.put("2019-12-01 14:00:00", 14.0);
        nIndexMap.put("2019-12-01 12:00:00", 12.0);
        nIndexMap.put("2019-12-01 15:00:00", 15.0);
        nIndexMap.put("2019-12-01 10:00:00", 10.0);
        System.out.println(nIndexMap);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime = now.minusDays(1);
        System.out.println(localDateTime);
        System.out.println(now);
    }
}
