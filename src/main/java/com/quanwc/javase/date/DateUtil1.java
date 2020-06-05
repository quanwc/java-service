package com.quanwc.javase.date;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

/**
 * 日期工具类
 * @author quanwenchao
 * @date 2019/5/20 10:21:47
 */
public class DateUtil1 {

    /**
     * LocalDateTime -> java.util.Date
     * @param localDateTime
     * @return
     */
    public static Date localDateTime2Date(LocalDateTime localDateTime){
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        return Date.from(zdt.toInstant());
    }

    /**
     * java.util.Date -> LocalDateTime
     * @param date
     * @return
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }

    /**
     * 将long类型的timestamp转为LocalDateTime
     * @param timestamp 单位是毫秒
     * @return LocalDateTime
     */
    public static LocalDateTime long2LocalDateTime(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     *将LocalDateTime转为long类型的timestamp
     * @param localDateTime
     * @return 毫秒数
     */
    public static long localDateTime2Long(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return instant.toEpochMilli();
    }


    /**
     * 两个日期相差的值
     * @return
     */
    public void betweenDateValue(){
        LocalDateTime dateTime = LocalDateTime.now().plusHours(1);
        LocalDateTime now = LocalDateTime.now();

        // 相差的小时值：
        Duration.between(now, dateTime).toHours();

        // 相差的分钟数
        Duration.between(now, dateTime).toMinutes();

        // 相差的秒数
        Duration.between(now, dateTime).getSeconds();
    }





    /**
     * 遍历时间范围【2019-07-01，2019-12-18】号之间的日期
     */
    public static void betweenDateForeach() {

        String dateFmt = "yyyy-MM-dd HH:mm:ss";
        // 开始处理的时间范围，总量通过for循环次数控制
        String beginDateStr = "2019-07-01 00:00:00";
        String endDateStr = "2019-12-18 00:00:00";
        LocalDateTime beginDate = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
        LocalDateTime endDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));


        while (!beginDate.isAfter(endDate)) { // beginDate <= endDate
            String dayDateStr = beginDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            System.out.println(dayDateStr);

            // beginDate++
            beginDate = beginDate.plusDays(1);
        }
    }


    /**
     * java.sql.Timestamp转为String
     * @param time java.sql.Timestamp类型
     * @return String（yyyy-MM-dd）这种格式)
     */
    private static final String FORMAT_PATTERN_0 = "yyyy-MM-dd";
    public static String timestamp2String(java.sql.Timestamp time) {
        return new SimpleDateFormat(FORMAT_PATTERN_0).format(time);
    }

    /**
     * Wed Apr 16 17:47:17 +0800 2014 --> LocalDateTime
     * @param time String类型的日期  ->  LocalDateTime
     * @return
     */
    public static LocalDateTime dateString2LocalDateTime(String time) {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK));
    }

    /**
     * java.util.Date转为String
     * @param time java.util.Date类型
     * @return String（yyyy-MM-dd）这种格式)
     */
    private static final String FORMAT_PATTERN_1 = "yyyy-MM-dd";
    public static String date2String(Date time) {
        return new SimpleDateFormat(FORMAT_PATTERN_1).format(time);
    }

    /**
     * java.util.Date转为String
     * @param time java.util.Date类型
     * @return String（yyyy-MM-dd HH:mm:ss）这种格式)
     */
    private static final String FORMAT_PATTERN_2 = "yyyy-MM-dd HH:mm:ss";;
    public static String date2String2(Date time) {
        return new SimpleDateFormat(FORMAT_PATTERN_2).format(time);
    }

    /**
     * 日期函数parse、format的使用
     * @return
     */
    public static void parseAndFormat(){

        String str = "2014-04-16T17:47:17"; // LocalDateTime类型的字符串，则可以直接解析，参数和LocalDateTime的字符串结构一致
        LocalDateTime localDateTime = LocalDateTime.parse(str);
        System.out.println(localDateTime);


        String str2 = "2019-07-01 00:00:00"; // 非LocalDateTime类型的字符串，则指定dateTmt，参数和dateFmt的结构一致
        String dateFmt = "yyyy-MM-dd HH:mm:ss";
        LocalDateTime localDateTime1 = LocalDateTime.parse(str2, DateTimeFormatter.ofPattern(dateFmt));
        System.out.println(localDateTime1);


        // DateTimeFormatter.ISO_DATE 和 DateTimeFormatter.ofPattern("yyyy-MM-dd")  这两种方式等效
        String date1 = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
        String date2 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    }


    /**
     * 一天的24个小时
     */
    public static void dateWith24Hour() {

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");

        LocalDateTime dateTimeZero = LocalDateTime.of(dateTime.toLocalDate(), LocalTime.of(0, 0, 0));
        List<Map<String, Object>> todayHourResultList = IntStream.rangeClosed(0, 23).boxed().collect(ArrayList::new, (list, item) -> {
            Map<String, Object> map = new HashMap<>();
            LocalDateTime withHour = dateTimeZero.withHour(item);
            map.put("date", formatter.format(withHour));
            map.put("p_index", 0.0);
            map.put("n_index", 0.0);
            list.add(map);
        }, List::addAll);

        System.out.println(todayHourResultList);

    }



    public static void main(String[] args) {

//        Date date = localDateTime2Date(LocalDateTime.now().minusDays(1));
//        System.out.println(date);
//
//        LocalDateTime dateTime = date2LocalDateTime(date);
//        System.out.println(dateTime);
//
//        LocalDateTime dateTime1 = long2LocalDateTime(date.getTime());
//        System.out.println(dateTime1);
//
//        long l = localDateTime2Long(LocalDateTime.now());
//        System.out.println(l);
//
//        betweenDateForeach();

//        LocalDateTime dateTime = dateString2LocalDateTime("Wed Apr 16 17:47:17 +0800 2014");
//        System.out.println(dateTime);
//
//        dateWith24Hour();

//        parseAndFormat();

    }

}
