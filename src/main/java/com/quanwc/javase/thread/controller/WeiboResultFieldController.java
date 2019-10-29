package com.quanwc.javase.thread.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.quanwc.javase.thread.service.WeiboResultFieldService;

import lombok.extern.slf4j.Slf4j;

/**
 * weibo_result表的特定属性重新跑数据、以及脏数据的处理
 * 
 * @author quanwenchao
 * @date 2019/7/23 10:34:45
 */
@Slf4j
//@Component
public class WeiboResultFieldController {

    @Autowired
    private WeiboResultFieldService weiboResultFieldService;

//    @Override
    public void run(ApplicationArguments args) throws Exception {
//         dealProfitLm();
//        dealIsFinance();
//        dealSubjectWith325();
//        dealMatchOthers();

//        dealMatchLabels();
    }





    /**
     * weibo_result表利多利空属性重新设置
     */
    public void dealProfitLm() {
        ExecutorService executorService = new ThreadPoolExecutor(10, 15, 10L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100), new ThreadFactoryBuilder().setNameFormat("weibo-result-profitLm-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy()); // 主线程执行

        String dateFmt = "yyyy-MM-dd HH:mm:ss";
        // 开始处理的时间范围，总量通过for循环次数控制
        String beginDateStr = "2019-10-15 00:00:00";
        String endDateStr = "2019-10-16 12:00:00";
        LocalDateTime startTime = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
        LocalDateTime endTime = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));



        long time1 = System.currentTimeMillis() / 1000;
        log.info("WeiboResultRestoreController dealProfitLm begin: " + LocalDateTime.now());
        for (int i = 1; i <= 20; i++) {
            Date from = Date.from(startTime.toInstant(ZoneOffset.ofHours(8)));
            Date to = Date.from(endTime.toInstant(ZoneOffset.ofHours(8)));

            weiboResultFieldService.dealProfitLm(executorService, from, to);
            System.out.println(from + " - " + to);

            startTime = startTime.plusDays(2);
            endTime = endTime.plusDays(2);
        }

        long time2 = System.currentTimeMillis() / 1000;
        try {
            executorService.shutdown();
            boolean b = executorService.awaitTermination(300, TimeUnit.SECONDS);
            if (b) {
                log.info("dealProfitLm所有任务执行完毕，共耗时" + (time2 - time1) + "秒");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("error: " + e.getMessage());
        }
        log.info("WeiboResultRestoreController dealProfitLm end: " + LocalDateTime.now());
    }







    /**
     * weibo_result表，财经非财经属性重新设置
     */
    public void dealIsFinance() {
        ExecutorService executorService =
            new ThreadPoolExecutor(10, 15, 10L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
                new ThreadFactoryBuilder().setNameFormat("weibo-result-isFinance-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()); // 主线程执行

        String dateFmt = "yyyy-MM-dd HH:mm:ss";
        // 开始处理的时间范围，总量通过for循环次数控制
        String beginDateStr = "2019-06-01 00:00:00";
        String endDateStr = "2019-06-03 00:00:00";
        LocalDateTime startTime = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
        LocalDateTime endTime = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));

        long time1 = System.currentTimeMillis() / 1000;

        // 每次处理两天数据
        // 时间总跨度：2019-06-01 00:00:00 至 2019-07-23 00:00:00
        log.info("WeiboResultFieldController begin: " + LocalDateTime.now());
        for (int i = 1; i <= 26; i++) {
            Date from = Date.from(startTime.toInstant(ZoneOffset.ofHours(8)));
            Date to = Date.from(endTime.toInstant(ZoneOffset.ofHours(8)));

            weiboResultFieldService.dealIsFinance(executorService, from, to);
            System.out.println(from + " - " + to);

            startTime = startTime.plusDays(2);
            endTime = endTime.plusDays(2);
        }

        long time2 = System.currentTimeMillis() / 1000;
        try {
            executorService.shutdown();
            boolean b = executorService.awaitTermination(200, TimeUnit.SECONDS);
            if (b) {
                log.info("所有任务执行完毕，共耗时" + (time2 - time1) + "秒");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("error: " + e.getMessage());
        }
        log.info("WeiboResultFieldController end: " + LocalDateTime.now());
    }

    
    
    


    
    /**
     * weibo_result表，id=325话题的脏数据处理
     */
    public void dealSubjectWith325() {
        ExecutorService executorService =
            new ThreadPoolExecutor(10, 15, 10L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
                new ThreadFactoryBuilder().setNameFormat("weibo-result-dealSubjectWith325-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()); // 主线程执行

        // 待处理的subjectId
        int dealSubjectId = 472;

        Date beginDate;
        Date endDate;
//        String beginDateStr = "2019-08-01 00:00:00";
//        String endDateStr = "2019-08-03 00:00:00";
        String beginDateStr = "2019-08-20 00:00:00";
        String endDateStr = "2019-08-23 20:00:00";
        String dateFmt = "yyyy-MM-dd HH:mm:ss";

        try {
            beginDate = new SimpleDateFormat(dateFmt).parse(beginDateStr);
            endDate = new SimpleDateFormat(dateFmt).parse(endDateStr);
        } catch (ParseException e) {
            log.error("date parse error: ", e.getMessage());
            throw new RuntimeException("date parse error");
        }

        long time1 = System.currentTimeMillis() / 1000;
        log.info("WeiboResultFieldController dealSubjectWith325 begin: " + LocalDateTime.now());

        weiboResultFieldService.dealSubjectWith325(executorService, dealSubjectId, beginDate, endDate);

        long time2 = System.currentTimeMillis() / 1000;
        try {
            executorService.shutdown();
            boolean b = executorService.awaitTermination(300, TimeUnit.SECONDS);
            if (b) {
                log.info("dealSubjectWith325所有任务执行完毕，共耗时" + (time2 - time1) + "秒");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("error: " + e.getMessage());
        }
        log.info("WeiboResultFieldController dealSubjectWith325 end: " + LocalDateTime.now());
    }

    /**
     * weibo_result表，match_others、title属性重新设置
     */
    public void dealMatchOthers() {
        ExecutorService executorService = new ThreadPoolExecutor(10, 15, 10L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100), new ThreadFactoryBuilder().setNameFormat("weibo-result-matchOthers-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy()); // 主线程执行

        String dateFmt = "yyyy-MM-dd HH:mm:ss";
        // 开始处理的时间范围，总量通过for循环次数控制
        String beginDateStr = "2019-09-01 00:00:00";
        String endDateStr = "2019-09-09 00:00:00";
        LocalDateTime startTime = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
        LocalDateTime endTime = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));

        long time1 = System.currentTimeMillis() / 1000;
        // 每次处理两天数据
        // 时间总跨度：2019-06-01 00:00:00 至 2019-09-09 00:00:00
        log.info("WeiboResultRestoreController dealMatchOthers begin: " + LocalDateTime.now());
        for (int i = 1; i <= 1; i++) {
            Date from = Date.from(startTime.toInstant(ZoneOffset.ofHours(8)));
            Date to = Date.from(endTime.toInstant(ZoneOffset.ofHours(8)));

            weiboResultFieldService.dealMatchOthers(executorService, from, to);
            System.out.println(from + " - " + to);

            startTime = startTime.plusDays(2);
            endTime = endTime.plusDays(2);
        }

        long time2 = System.currentTimeMillis() / 1000;
        try {
            executorService.shutdown();
            boolean b = executorService.awaitTermination(300, TimeUnit.SECONDS);
            if (b) {
                log.info("dealMatchOthers所有任务执行完毕，共耗时" + (time2 - time1) + "秒");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("error: " + e.getMessage());
        }
        log.info("WeiboResultRestoreController dealMatchOthers end: " + LocalDateTime.now());
    }







    /**
     * weibo_result表数据，重新匹配match_classifies、match_labels、match_others标签
     */
    public void dealMatchLabels() {
        ExecutorService executorService = new ThreadPoolExecutor(10, 15,
            10L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
            new ThreadFactoryBuilder().setNameFormat("weibo-result-matchLabels-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy()); // 主线程执行


        String dateFmt = "yyyy-MM-dd HH:mm:ss";
        // 开始处理的时间范围，总量通过for循环次数控制
        String beginDateStr = "2019-09-23 00:00:00";
        String endDateStr = "2019-09-25 00:00:00";
        LocalDateTime startTime = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
        LocalDateTime endTime = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));

        long time1 = System.currentTimeMillis() / 1000;
        // 每次处理两天数据
        log.info("WeiboResultRestoreController match_labels begin: " + LocalDateTime.now());
        for (int i = 1; i <= 2; i++) {
            Date from = Date.from(startTime.toInstant(ZoneOffset.ofHours(8)));
            Date to = Date.from(endTime.toInstant(ZoneOffset.ofHours(8)));

            weiboResultFieldService.dealMatchLabels(executorService, from, to);
            System.out.println(from + " - " + to);

            startTime = startTime.plusDays(2);
            endTime = endTime.plusDays(2);
        }

        long time2 = System.currentTimeMillis() / 1000;
        try {
            executorService.shutdown();
            boolean b = executorService.awaitTermination(300, TimeUnit.SECONDS);
            if (b) {
                log.info("dealMatchLabels所有任务执行完毕，共耗时" + (time2 - time1) + "秒");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("error: " + e.getMessage());
        }
        log.info("WeiboResultRestoreController match_labels end: " + LocalDateTime.now());

    }




    public static void main(String[] args) {
//        try {
//            long time1 = System.currentTimeMillis();
//            Thread.sleep(5000);
//            long time2 = System.currentTimeMillis();
//            System.out.println(time1);
//            System.out.println(time2 - time1);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        dealProfitLm();
    }

}
