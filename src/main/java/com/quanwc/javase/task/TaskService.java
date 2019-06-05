package com.quanwc.javase.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author quanwenchao
 * @date 2019/6/5 16:40:56
 */
//@Component
public class TaskService {

    // @Scheduled注解使用：
    //    https://www.cnblogs.com/softidea/p/5833248.html

    @Scheduled(cron = " * */5 * * * *") // 表示每5分钟执行一次(能被5整除，也就是每5分钟执行一次)
    @Scheduled(cron = " 0 40 0 * * ?") // 每天0点40分0秒，执行一次

    @Scheduled(cron = " 0 40 0 * * ?") // 每天0点40分0秒,执行一次
    @Scheduled(cron = " 0 40 0 * * *")// 每天0点40分0秒，执行一次。eg：如果是六位的话，*和?是一样的
    @Scheduled(cron = " 0 15 10 ? * *") // 每天上午10:15触发
    @Scheduled(cron = " 0 15 10 * * ?") // 每天上午10:15触发
    @Scheduled(cron = " 0 15 10 * * ? *") // 每天上午10:15触发

    @Scheduled(cron = "0 0 10,14,16 * * ? ")  // 每天上午10点，下午2点，4点
    public void test() {

    }


}
