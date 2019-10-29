package com.quanwc.javase.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * weibo-xxxx表(按天存储表)的数据处理
 * @author quanwenchao
 * @date 2019/10/10 17:33:56
 */
@Slf4j
@Component
public class WeiboCollectionAnalyzeTask implements ApplicationRunner {

    @Override public void run(ApplicationArguments args) throws Exception {
//        dealWeiboDailyCollection();
    }

    @Autowired
    private MongoTemplate mongoTemplate;
//    @Autowired
//    private KeyWordNewRepository keyWordNewRepository;

    final int FOLLOWERS_COUNT_LIMIT = 100000; // 10万
    private static final String COLLECTION_WEIBO_DATA1008 = "weibo-20191008";
    private static final String COLLECTION_WEIBO_ANALYZE_DATA1008 = "z_weibo_analyze_20191008";
    private List<String> financeKeywordList = null; // 财经词List
    private List<String> specifiedCompanyList = new ArrayList<>(); // 只筛选特定公司的微博

    @PostConstruct
    public void init() {
//        financeKeywordList = keyWordNewRepository.listFinanceKeyword();
        financeKeywordList = new ArrayList<>();
        specifiedCompanyList.add("格力电器");
        specifiedCompanyList.add("中兴通讯");
        specifiedCompanyList.add("比亚迪");
        specifiedCompanyList.add("万科A");
        specifiedCompanyList.add("涪陵榨菜");
        specifiedCompanyList.add("平安银行");
        specifiedCompanyList.add("中国平安");
        specifiedCompanyList.add("恒生电子");
        specifiedCompanyList.add("万科");
        specifiedCompanyList.add("格力");
    }


    /**
     * 每天凌晨0:10分，分析昨天weibo-xxxx表的全量数据
     */
//    @Scheduled(cron = " 0 55 9 * * ?") // 暂时可以不用跑了
    public void dealWeiboDailyCollection() {
        log.info("WeiboCollectionAnalyzeTask dealWeiboDailyCollection begin");
        dealWeiboCollection();
        log.info("WeiboCollectionAnalyzeTask dealWeiboDailyCollection end");
    }

    public void dealWeiboCollection() {
        long time1 = System.currentTimeMillis() / 1000;
        String dateSuffix = this.getDateTimeAsString(LocalDateTime.now().minusDays(1), "yyyyMMdd");
        ExecutorService executorService = new ThreadPoolExecutor(20, 30,
            10L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
            new ThreadFactoryBuilder().setNameFormat("weibo-"+ dateSuffix + "-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy()); // 主线程执行

        for (int i = 0; i <= 100; ++i) {
            log.info("WeiboCollectionAnalyzeTask dealWeiboCollection remainder with " + i + " begin");
            deal(executorService, dateSuffix, i);
            log.info("WeiboCollectionAnalyzeTask dealWeiboCollection remainder with " + i + " end");
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
    }

    /**
     * 每次处理id除以100余remainder的所有微博
     * //表weibo-20191008特定微博筛选：mid尾号99 、字数大于20个字、10万粉以下、text包含财经关键词和特定公司
     * @param executorService 线程池
     * @param dateSuffix 待处理的collection后缀
     * @param remainder 余数
     */
    public void deal(ExecutorService executorService,String dateSuffix, int remainder) {
        Query query = new Query();
        query.fields().include("_id");
        query.addCriteria(Criteria.where("_id").mod(100, remainder)); // id除以100余remainder
        List<JSONObject> weiboDataList = mongoTemplate.find(query, JSONObject.class, "weibo-" + dateSuffix);
        List<Long> weiboIdList = weiboDataList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
        log.info("weiboIdList.size: " + weiboIdList.size());

        List<List<Long>> partitionList = Lists.partition(weiboIdList, 10000); // 每次处理1万条
        for (int i = 0; i < partitionList.size(); ++i) {
            log.info("partition处理第" + i + "次");
            List<Long> ids = partitionList.get(i);

            Query query3 = new Query();
            query3.addCriteria(Criteria.where("_id").in(ids));
            query3.fields().include("_id").include("text").include("retweeted_status.mid")
                .include("retweeted_status.text").include("user.id").include("user.screen_name")
                .include("user.verified").include("user.followers_count");
            List<JSONObject> weiboDataParationList = mongoTemplate.find(query3, JSONObject.class, "weibo-" + dateSuffix);
            log.info("weiboDataParationList.size: " + weiboDataParationList.size());

            for (int j = 0; j < weiboDataParationList.size(); ++j) {
                JSONObject jsonObject = weiboDataParationList.get(j);
                int finalJ = j;
                executorService.execute(() -> dealSingleWeibo(jsonObject, finalJ, dateSuffix));
            }
        }
    }

    /**
     * 处理单条微博
     * @param s weibo
     */
    public void dealSingleWeibo(JSONObject s, int i, String dateSuffix) {
//        if (i % 10000 == 0) {
//            log.info("dealSingleWeibo 正在处理第" + i + "个");
//        }
        try {
            String text = s.getString("text");
            Long followers_count = s.getJSONObject("user").getLong("followers_count");
            // followers_count = followers_count == null ? 0 : followers_count; // 欧洲用户会返回不会返回粉丝数等属性
            if (followers_count == null) {
                return;
            }

            // 只处理特定公司
            boolean bo1 = specifiedCompanyList.stream().filter(w -> text.contains(w)).count() > 0;
            if (!bo1) {
                return;
            }

            // text在财经库中存在
            //            boolean bo2 = financeKeywordList.stream().filter(w -> text.contains(w)).count() > 0;
            if (text.length() > 20 && followers_count < FOLLOWERS_COUNT_LIMIT) {
                mongoTemplate.save( s, "z_weibo_analyze_" + dateSuffix);
            }
        } catch (Exception e) {
            log.error("发生error: webiId=" + s.getLong("_id"), e );
        }
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

}
