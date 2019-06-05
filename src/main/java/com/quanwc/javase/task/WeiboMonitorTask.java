package com.quanwc.javase.task;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.quanwc.javase.dingtalk.config.DingTalkConfig;
import com.quanwc.javase.dingtalk.entity.At;
import com.quanwc.javase.dingtalk.entity.Text;
import com.quanwc.javase.dingtalk.entity.TextMessage;
import com.quanwc.javase.dingtalk.util.DingTalkUtil;
import com.quanwc.javase.http.Response;

import lombok.extern.slf4j.Slf4j;

/**
 * 监控微博数据、评论数据的订阅逻辑是否发生中断，5分钟检查一次，如果中断则报警
 *
 * 微博数据：[00:00]至[00:40]之间，监控上一天的表数据  // 微博数据延迟40分钟
 * 评论数据：[00:00]至[00:05]之间，监控上一天的表数据  // 评论数据延迟5分钟
 * @author quanwenchao
 * @date 2019/6/3 19:36:46
 */
@Slf4j
@Component
public class WeiboMonitorTask {

    private String weiboCollection = "";
    private String commentCollection = "";

    private long weiboMaxCount = 0L;
    private long commentMaxCount = 0L;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private DingTalkConfig dingTalkConfig;


    /**
     * 监控微博数据、评论数据的订阅状态
     */
    @Scheduled(cron = " * */5 * * * *") // 表示每5分钟执行一次
    public void checkMonitor() {
        // 检查微博
        checkWeiboData();
        // 检查评论
        checkCommentData();
    }


    @Scheduled(cron = " 0 40 0 * * ?") // 每天0点40分0秒，将weiboMaxCount赋值为0
    public void updateWeiboMaxCount() {
        System.out.println("updateWeiboMaxCount: " + LocalDateTime.now());
        weiboMaxCount = 0L;
    }

    @Scheduled(cron = " 0 5 0 * * *") // 每天0点5分0秒，将commentMaxCount赋值为0
    public void updateCommentMaxCount() {
        System.out.println("updateCommentMaxCount: " + LocalDateTime.now());
        commentMaxCount = 0L;
    }


    public void checkWeiboData() {
        boolean bo = checkCurrentDate4Weibo(); // 当前时间是否在[00:00]至[00:40]之间
        if (bo) {
            weiboCollection = "weibo-" + getDateTimeAsString(LocalDateTime.now().minusDays(1), "yyyyMMdd");
        } else {
            weiboCollection = "weibo-" + getDateTimeAsString(LocalDateTime.now(), "yyyyMMdd");
        }

        long count = mongoTemplate.count(new Query(), weiboCollection);
        if (count <= weiboMaxCount) {
            notifyDingTalk("微博数据发生中断，count没有递增");
        }
        weiboMaxCount = count;

        System.out.println("weiboMaxCount: " + weiboMaxCount);
    }

    /**
     * 监控评论数据是否发生中断
     */
    public void checkCommentData() {

        boolean bo = checkCurrentDate4Comment(); // 当前时间是否在[00:00]至[00:05]之间
        if (bo) {
            commentCollection = "comment-" + getDateTimeAsString(LocalDateTime.now().minusDays(1), "yyyyMMdd");
        } else {
            commentCollection = "comment-" + getDateTimeAsString(LocalDateTime.now(), "yyyyMMdd");
        }

        long count = mongoTemplate.count(new Query(), commentCollection);
        if (count <= commentMaxCount) {
            notifyDingTalk("评论数据发生中断，count没有递增");
        }
        commentMaxCount = count;

        System.out.println("commentMaxCount: " + commentMaxCount);
    }

    /**
     * 唤醒钉钉通知
     *
     * @param errorMessage
     */
    private void notifyDingTalk(String errorMessage) {

        LocalDateTime now = LocalDateTime.now();
        System.out.println("now: " + now + ", " + errorMessage);

        TextMessage textMessage = new TextMessage();
        textMessage.setMsgtype("text");

        Text text = new Text();
        text.setContent(errorMessage);
        textMessage.setText(text);

        At at = new At();
        at.setAtMobiles(dingTalkConfig.getAtMobiles());
        at.setAtAll(true);
        textMessage.setAt(at);

        Response response = DingTalkUtil
            .sendTextMessage(dingTalkConfig.getWebhookUrl(), dingTalkConfig.getAccessToken(), textMessage);
        log.error(response.getBody());
    }

    /**
     * 检查当前日期是否在[00:00]至[00:40]之间
     * @return
     */
    private boolean checkCurrentDate4Weibo() {
        SimpleDateFormat formatter = new SimpleDateFormat("HHmm");
        String date = formatter.format(new Date()); // 获取当前时间的小时分钟：19:54 -> 1954
        int time = Integer.parseInt(date);
        if (time >= 00 && time <= 40) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查当前日期是否在[00:00]至[00:05]之间
     * @return
     */
    private boolean checkCurrentDate4Comment() {
        SimpleDateFormat formatter = new SimpleDateFormat("HHmm");
        String date = formatter.format(new Date()); // 获取当前时间的小时分钟：19:54 -> 1954
        int time = Integer.parseInt(date);
        if (time >= 0 && time <= 5) {
            return true;
        } else {
            return false;
        }
    }


    public String getDateTimeAsString(LocalDateTime localDateTime, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(formatter);
    }

}
