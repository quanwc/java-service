package com.quanwc.javase.task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

/**
 * weibo_data表的迁移相关定时任务
 *
 * @author quanwenchao
 * @date 2019/8/29 14:58:04
 */
@Slf4j
@Component
public class WeiboDataHistoryTask {

    final int LIMIT_COUNT = 30000; // 3万
    final int FOLLOWERS_COUNT = 100000;  // 10万

    private static final String COLLECTION_WEIBO_RESULT = "weibo_result";
    private static final String COLLECTION_WEIBO_DATA = "weibo_data";
    private static final String COLLECTION_WEIBO_DATA_HISTORY = "weibo_data_history";

    @Autowired
    private MongoTemplate mongoTemplate;

//    @Override public void run(ApplicationArguments args) throws Exception {
//        dealWeiboDataHistory();
//    }

    /**
     * 每天8点、20点开始执行一次
     */
    @Scheduled(cron = " 0 45 20,10 * * ?")
    public void dealWeiboDataHistoryTask() {
        log.info("weibo_data dealWeiboDataHistoryTask begin");
        dealWeiboDataHistory();
        log.info("weibo_data dealWeiboDataHistoryTask end");
    }

    /**
     * weibo_data表，超过两个月并且没有在weibo_result中的微博，迁移到weibo_data_history表
     * 使用removeAll方法求差集
     */
    public void dealWeiboDataHistory() {

        LocalDateTime beginDateStr = LocalDateTime.now().minusMonths(2).withHour(0).withMinute(0).withSecond(0);
        // step1：weibo_result表中的微博id
        Query query1 = new Query();
        query1.fields().include("_id");
        query1.addCriteria(Criteria.where("create_time").lte(beginDateStr));
        List<JSONObject> weiboResultList = mongoTemplate.find(query1, JSONObject.class, COLLECTION_WEIBO_RESULT);
        Set<Long> weiboResultIdList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toSet());
        log.info("weibo_result ids size: " + weiboResultIdList.size());

        // step2：在weibo_data中查询超过2个月的数据
        Long lastId = 4400266788551013L;
        int i = 0;
        while (true) {
            log.info("weibo_data处理第" + (i + 1) + "次begin");
            i++;
            Query query2 = new Query(Criteria.where("_id").gt(lastId).and("created_at").lte(beginDateStr));
            query2.with(Sort.by("_id").ascending());
            List<JSONObject> weiboDataList = mongoTemplate.find(query2.limit(LIMIT_COUNT), JSONObject.class, COLLECTION_WEIBO_DATA);
            if (CollectionUtils.isEmpty(weiboDataList)) {
                break;
            }
            Set<Long> weiboDataIdList = weiboDataList.stream().map(s -> s.getLong("_id")).collect(Collectors.toSet());
            log.info("weibo_data ids size: " + weiboDataIdList.size());
//            weiboDataIdList.removeAll(weiboResultIdList); // 求差集：weiboDataIdList移出weiboResultIdList中存在的id，weiboDataIdList就是需要处理的数据
            weiboDataIdList = weiboDataIdList.stream().filter(s -> !weiboResultIdList.contains(s)).collect(Collectors.toSet());
            log.info("weibo_data removeAll weibo_result后weibo_data ids size: " + weiboDataIdList.size());
            if (CollectionUtils.isEmpty(weiboDataIdList)) {
                lastId = weiboDataList.get(weiboDataList.size() - 1).getLong("_id");
                log.info("weibo_data处理第" + (i) + "次end, lastId: " + lastId);
                continue;
            }
            // 过滤掉在weibo_data_history表已经存在的weiboId
            Query query3 = new Query(Criteria.where("_id").in(weiboDataIdList));
            query3.fields().include("_id");
            List<JSONObject> weiboDataHistoryExistList =
                mongoTemplate.find(query3, JSONObject.class, COLLECTION_WEIBO_DATA_HISTORY);
            Set<Long> weiboDataHistoryExistIdList = weiboDataHistoryExistList.stream().map(s -> s.getLong("_id")).collect(Collectors.toSet());
            log.info("weibo_data_history ids size: " + weiboDataHistoryExistIdList.size());
//            weiboDataIdList.removeAll(weiboDataHistoryExistIdList); // 求差集
            weiboDataIdList = weiboDataIdList.stream().filter(s -> !weiboDataHistoryExistIdList.contains(s)).collect(Collectors.toSet());
            log.info("weibo_data removeAll weibo_data_history后weibo_data ids size: " + weiboDataIdList.size());


            // 将weiboResultIdList对应的微博数据新增到weibo_data_history表、并在weibo_data中删除
            Set<Long> weiboDataFinalIdList = weiboDataIdList;
            List<JSONObject> weiboDataHistoryList = weiboDataList.stream()
                    .filter(s -> weiboDataFinalIdList.contains(s.getLong("_id"))).collect(Collectors.toList());

            mongoTemplate.insert(weiboDataHistoryList, COLLECTION_WEIBO_DATA_HISTORY);
            mongoTemplate.remove(new Query(Criteria.where("_id").in(weiboDataIdList)), COLLECTION_WEIBO_DATA);

            lastId = weiboDataList.get(weiboDataList.size() - 1).getLong("_id");
            log.info("weibo_data处理第" + (i) + "次end, lastId: " + lastId);
        }

        log.info("weibo_data处理完毕");
    }




    /**
     * 与dealWeiboDataHistory相比，使用removeAllWithHashSet方法求差集
     */
    public void dealWeiboDataHistory2() {

        // step1：weibo_result表中的微博id
        Query query1 = new Query();
        query1.fields().include("_id");
        List<JSONObject> weiboResultList = mongoTemplate.find(query1, JSONObject.class, COLLECTION_WEIBO_RESULT);
        List<Long> weiboResultIdList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
        log.info("weibo_result ids size: " + weiboResultIdList.size());

        Date beginDate;
        String dateFmt = "yyyy-MM-dd 00:00:00";
        String beginDateStr = LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ofPattern(dateFmt));
        try {
            beginDate = new SimpleDateFormat(dateFmt).parse(beginDateStr);
        } catch (ParseException e) {
            log.error("date parse error: ", e);
            throw new RuntimeException("date parse error");
        }

        // step2：在weibo_data中查询超过2个月的数据
        Long lastId = 4294883511997748L;
        for (int i = 0; i < 300; ++i) {
            log.info("weibo_data处理第" + (i + 1) + "次begin");
            Query query2 = new Query(Criteria.where("_id").gt(lastId).and("created_at").lte(beginDate));
            query2.with(Sort.by("_id").ascending());
            query2.limit(LIMIT_COUNT);
            List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, COLLECTION_WEIBO_DATA);
            List<Long> weiboDataIdList = weiboDataList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
            log.info("weibo_data ids size: " + weiboDataIdList.size());
            // 求差集：
            List<Long> resultIdList = removeAllWithHashSet(weiboDataIdList, weiboResultIdList);

            // 将weiboResultIdList对应的微博数据新增到weibo_data_history表、并在weibo_data中删除
            List<JSONObject> weiboDataHistoryList = weiboDataList.stream()
                .filter(s -> resultIdList.contains(s.getLong("_id"))).collect(Collectors.toList());
            mongoTemplate.insert(weiboDataHistoryList, COLLECTION_WEIBO_DATA_HISTORY);
            mongoTemplate.remove(new Query(Criteria.where("_id").in(resultIdList)), COLLECTION_WEIBO_DATA);

            if (weiboDataList.size() < LIMIT_COUNT) { // 在weibo_data中查询的数量小于limit的个数，则是最后一次查询了
                break;
            }

            lastId = weiboDataList.get(weiboDataList.size() - 1).getLong("_id");
            log.info("weibo_data处理第" + (i + 1) + "次end");
        }

        log.info("weibo_data处理完毕");
    }




    /**
     * weibo_data表，两个月范围内，粉丝数低于10万，并且没有在weibo_result中的微博，迁移到weibo_data_history表
     * 只运行一次
     */
    public void dealWeiboDataWith10Followers() {
        // step1：weibo_result表中的微博id
        Query query1 = new Query();
        query1.fields().include("_id");
        List<JSONObject> weiboResultList = mongoTemplate.find(query1, JSONObject.class, COLLECTION_WEIBO_RESULT);
        List<Long> weiboResultIdList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
        log.info("weibo_result ids size: " + weiboResultIdList.size());

        Date beginDate;
        String dateFmt = "yyyy-MM-dd 00:00:00";
        String beginDateStr = LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ofPattern(dateFmt));
        try {
            beginDate = new SimpleDateFormat(dateFmt).parse(beginDateStr);
        } catch (ParseException e) {
            log.error("date parse error: ", e);
            throw new RuntimeException("date parse error");
        }

        // step2：在weibo_data中查询2个月范围内，粉丝数小于10万的数据
        Long lastId = 4391569077794786L;
        for (int i = 0; i < 700; ++i) {
            log.info("weibo_data处理第" + (i + 1) + "次begin");
            Query query2 = new Query(Criteria.where("_id").gt(lastId).and("created_at").gte(beginDate));
            query2.with(Sort.by("_id").ascending());
            query2.limit(LIMIT_COUNT);
            List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, COLLECTION_WEIBO_DATA);
            log.info("weibo_data size: " + weiboDataList.size());

            List<JSONObject> collect = weiboDataList.stream().filter(s -> s.getJSONObject("user") != null)
                .filter(s -> s.getJSONObject("user").getInteger("followers_count") != null)
                .collect(Collectors.toList());
            log.info("collect size: " + collect.size());

            List<Long> weiboDataIdList = collect.stream().filter(s -> s.getJSONObject("user").getInteger("followers_count") < FOLLOWERS_COUNT)
                .map(s -> s.getLong("_id")).collect(Collectors.toList());
            log.info("weibo_data followers < 10万 size: " + weiboDataIdList.size());

            weiboDataIdList.removeAll(weiboResultIdList); // 求差集

            // 将weiboResultIdList对应的微博数据新增到weibo_data_history表、并在weibo_data中删除
            List<JSONObject> weiboDataHistoryList = weiboDataList.stream()
                .filter(s -> weiboDataIdList.contains(s.getLong("_id"))).collect(Collectors.toList());
            mongoTemplate.insert(weiboDataHistoryList, COLLECTION_WEIBO_DATA_HISTORY);
            mongoTemplate.remove(new Query(Criteria.where("_id").in(weiboDataIdList)), COLLECTION_WEIBO_DATA);

            if (weiboDataList.size() < LIMIT_COUNT) {
                break;
            }

            lastId = weiboDataList.get(weiboDataList.size() - 1).getLong("_id");
            log.info("weibo_data处理第" + (i + 1) + "次end");
        }

        log.info("weibo_data处理完毕");
    }



    public static void main(String[] args) {

        List<Long> weiboDataIdList = new ArrayList<>();
        weiboDataIdList.add(1L);
        weiboDataIdList.add(2L);

        List<Long> weiboResultIdList = new ArrayList<>();
        weiboResultIdList.add(1L);
        weiboResultIdList.add(2L);
        weiboResultIdList.add(3L);

        System.out.println(weiboDataIdList);
        System.out.println(weiboResultIdList);

//        weiboDataIdList.removeAll(weiboResultIdList);

        List<Long> resultIdList = removeAllWithHashSet(weiboDataIdList, weiboResultIdList);
        System.out.println(resultIdList);
    }

    /**
     * source与destination求差集
     * @param source [1, 2, 3, 4, 5]
     * @param destination [1, 2]
     * @return [3, 4, 5]
     */
    public static List<Long> removeAllWithHashSet(List<Long> source, List<Long> destination) {
        List<Long> result = new LinkedList<Long>();
        HashSet<Long> destinationSet = new HashSet<>(destination);
        for(Long t : source) {
            if (!destinationSet.contains(t)) {
                result.add(t);
            }
        }
        return result;
    }
}
