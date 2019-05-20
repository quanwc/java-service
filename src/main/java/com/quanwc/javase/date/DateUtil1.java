package com.quanwc.javase.date;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author quanwenchao
 * @date 2019/5/20 10:21:47
 */
public class DateUtil1 {
    /**
     * Wed Apr 16 17:47:17 +0800 2014 --> LocalDateTime
     * @param time String类型的日期  ->  LocalDateTime
     * @return
     */
    public static LocalDateTime parseStringToDateTime(String time) {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK));
    }

    /**
     * 2017-03-29T15:59:21  ->  2017-03-29
     * 获取年月日字符串
     * @param time
     * @return
     */
    public static String getDateStr(LocalDateTime time){
        return time.toLocalDate().format(DateTimeFormatter.ISO_DATE);
    }


    public static void main(String[] args) {
        String dateStr = "Wed Mar 29 15:59:21 +0800 2017";
        LocalDateTime localDateTime = parseStringToDateTime(dateStr);
        System.out.println(localDateTime);

        String dateStr1 = getDateStr(localDateTime);
        System.out.println(dateStr1);
    }
}
