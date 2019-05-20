package com.quanwc.javase.date;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public enum TIMESTAMPTYPE{
        SECOND,MILLS;
    }

    /**
     * 获取当前时间戳
     * @param type
     * @return
     */
    public static long getTimestamp(TIMESTAMPTYPE type){
        if (type.equals(TIMESTAMPTYPE.SECOND))
            return LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
        else
            return LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    /**
     * 将LocalDateTime转为自定义的时间格式的字符串
     * @param localDateTime
     * @param format
     * @return
     */
    public static String getDateTimeAsString(LocalDateTime localDateTime, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(formatter);
    }


    /**
     * 将long类型的timestamp转为LocalDateTime
     * @param timestamp
     * @return
     */
    public static LocalDateTime getDateTimeOfTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     *将LocalDateTime转为long类型的timestamp
     * @param localDateTime
     * @return
     */
    public static long getTimestampOfDateTime(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return instant.toEpochMilli();
    }


    /**
     *将某时间字符串转为自定义时间格式的LocalDateTime
     * @param time
     * @param format
     * @return
     */
    public static LocalDateTime parseStringToDateTime(String time, String format) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(time, df);
    }

    //Wed Apr 16 17:47:17 +0800 2014 --> LocalDateTime
    public static LocalDateTime parseStringToDateTime(String time) {
        return LocalDateTime.parse(time,DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK));
    }

    /**
     * 获取年月日字符串
     * @param time
     * @return
     */
    public static String getDateStr(LocalDateTime time){
       return time.toLocalDate().format(DateTimeFormatter.ISO_DATE);
    }

    /**
     * 获取相差指定天数得日期
     * @param time
     * @param days
     * @return
     */
    public static LocalDateTime getDateLimitDays(LocalDateTime time,int days){
        return time.plusDays(days);
    }

    /**
     * 获取日期 精确到小时
     * @param time
     * @return
     */
    public static LocalDateTime getHourDate(LocalDateTime time){
        return LocalDateTime.parse(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH")),DateTimeFormatter.ofPattern("yyyy-MM-dd HH"));
    }

    /**
     * 判断两个日期相差得小时数
     * @param start
     * @param end
     * @return
     */
    public static long judgeHour(LocalDateTime start,LocalDateTime end){
        Duration duration =  Duration.between(start,end);
        return duration.toHours();
    }

    public static Date changeToDate(LocalDateTime localDateTime){
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        return Date.from(zdt.toInstant());
    }

    public static LocalDateTime changeToDateTime(Date date){
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }

    public static Date fromLocalDateToDate(LocalDate ldt){
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = ldt.atStartOfDay(zoneId);
        return Date.from(zdt.toInstant());
    }

    /**
     * 将yyyy-MM-dd => LocalDate
     * @param date
     * @return
     */
    public static LocalDate parseLocalDate(String date){
        return LocalDate.parse(date,DateTimeFormatter.ISO_DATE);
    }



    public static void main(String[] args) {
        try {
         String date="2018-09-09";

            System.out.println(parseLocalDate(date).plusDays(-1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
