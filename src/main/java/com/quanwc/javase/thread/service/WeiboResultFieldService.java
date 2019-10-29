package com.quanwc.javase.thread.service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import lombok.extern.slf4j.Slf4j;

/**
 * weibo_result表的特定属性重新跑数据
 * 
 * @author quanwenchao
 * @date 2019/7/23 10:33:52
 */
@Slf4j
@Service
public class WeiboResultFieldService {

    private static final String COLLECTION_WEIBO_RESULT = "weibo_result";
    private static final String COLLECTION_WEIBO_DATA = "weibo_data";

    @Autowired
    private MongoTemplate mongoTemplate;

    private List<Map<String, Object>> subjectAllList = null; // 所有话题列表
    Map<Integer, Map<String, Object>> subjectMap = null; // 话题列表转化为map

//    @PostConstruct
//    public void init() {
//        subjectAllList = subjectRepository.listSubject(); // 初始化话题的List
//        subjectMap = subjectAllList.stream().collect(Collectors.toMap(sub -> (Integer)sub.get("id"), Function.identity()));
//    }


    /**
     * 重新跑利多利空接口，设置profit_lm、state属性值
     * @param executorService
     * @param beginDate
     * @param endDate
     */
    public void dealProfitLm(ExecutorService executorService, Date beginDate, Date endDate) {
        // 在weibo_result查询
        Query query = new Query();
        query.addCriteria(Criteria.where("create_time").gte(beginDate).lte(endDate)); // 可以分多个时间范围，多次处理
        query.fields().include("_id");
        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, COLLECTION_WEIBO_RESULT);
        log.info("weiboResultList: " + weiboResultList.size());

        // 在weibo_data中查询
        List<Long> idList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
        Query query2 = new Query();
        query2.addCriteria(Criteria.where("created_at").gte(beginDate).lt(endDate));
        query2.addCriteria(Criteria.where("_id").in(idList));
        query2.fields().include("_id").include("text");
        List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, COLLECTION_WEIBO_DATA);
        log.info("weiboDataList: " + weiboDataList.size());
        if (CollectionUtils.isEmpty(weiboDataList)) {
            return;
        }

        for (int i=0; i<weiboDataList.size(); ++i) {
            JSONObject s = weiboDataList.get(i);
            int finalI = i;
            executorService.execute(() -> dealWeiboProfitLm(s, finalI, beginDate, endDate));
        }
    }
    /**
     * 处理单条微博
     * @param s
     * @param i
     */
    private static final String PROFIT_RESULT = "非利空";
    private void dealWeiboProfitLm(JSONObject s, int i, Date beginDate, Date endDate) {
        Long id = s.getLong("_id");
        String text = s.getString("text");

        // 重新匹配资讯分类接口
//        Map<String, Object> profitLmMap = consumerService.getProfitLm(text);
        Map<String, Object> profitLmMap = new HashMap<>();
        int profitLmResult = (Integer)profitLmMap.get("profitLm");
//        String title = (String)profitLmMap.get("title");

        // 入库
        Query query3 = new Query(Criteria.where("_id").is(id));
        Update update3 = new Update().set("profit_lm", profitLmResult).set("state", profitLmResult);
        UpdateResult updateResult = mongoTemplate.updateFirst(query3, update3, COLLECTION_WEIBO_RESULT);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (i % 1000 == 0) {
            log.info(sdf.format(beginDate) + "到" + sdf.format(endDate) + "处理完毕第" + i + "个，weiboId=" + id);
        }
    }







    /**
     * 重新跑财经非财经接口，设置is_finance属性值
     * @param executorService
     * @param beginDate
     * @param endDate
     */
    public void dealIsFinance(ExecutorService executorService, Date beginDate, Date endDate) {
        // 在weibo_result查询
        Query query = new Query();
        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate)); // 可以分多个时间范围，多次处理
        query.fields().include("_id");
        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, COLLECTION_WEIBO_RESULT);
        log.info("weiboResultList: " + weiboResultList.size());

        // 在weibo_data中查询
        List<Long> idList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
        Query query2 = new Query();
        query2.addCriteria(Criteria.where("created_at").gte(beginDate).lt(endDate));
        query2.addCriteria(Criteria.where("_id").in(idList));
        query2.fields().include("_id").include("text");
        List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, COLLECTION_WEIBO_DATA);
        log.info("weiboDataList: " + weiboDataList.size());

        for (int i=0; i<weiboDataList.size(); ++i) {
            JSONObject s = weiboDataList.get(i);
            int finalI = i;
            executorService.execute(() -> dealIsFinance(s, finalI, beginDate, endDate));
        }
    }
    private static final String IS_FINANCE = "财经";
    private void dealIsFinance(JSONObject s, int i, Date beginDate, Date endDate) {
        Long id = s.getLong("_id");
        String text = s.getString("text");

        // 重新匹配资讯分类接口
        Integer isFinanceType = 0;
//        JSONObject financeObject = httpMessageUtil.financeJudge(text).block();
        JSONObject financeObject = new JSONObject();
        if (null != financeObject && IS_FINANCE.equals(financeObject.getString("result"))) {
            isFinanceType = 1;
        }

        // 入库
        Query query3 = new Query(Criteria.where("_id").is(id));
        Update update3 = new Update().set("is_finance", isFinanceType);
        mongoTemplate.updateFirst(query3, update3, COLLECTION_WEIBO_RESULT);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (i % 1000 == 0) {
            log.info(sdf.format(beginDate) + "到" + sdf.format(endDate) + "处理完毕第" + i + "个，weiboId=" + s.getLong("_id"));
        }
    }






    /**
     * weibo_result表，id=325话题的脏数据处理
     * @param executorService 线程池对象
     * @param dealSubjectId 待处理话题id
     * @param beginDate 开始时间
     * @param endDate 结束时间
     */
    public void dealSubjectWith325(ExecutorService executorService, int dealSubjectId, Date beginDate, Date endDate) {

        // 在weibo_result查询
        Query query = new Query();
        query.addCriteria(Criteria.where("create_time").gte(beginDate).lte(endDate).and("match_labels.match.subject")
            .in(dealSubjectId));
        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, "weibo_result");
        log.info("weiboResultList: " + weiboResultList.size());

        for (int i=0; i<weiboResultList.size(); ++i) {
            JSONObject s = weiboResultList.get(i);
            int finalI = i;
            executorService.execute(() -> dealSubject325(dealSubjectId,s, finalI));
        }

    }
    /**
     * 处理单条微博
     * @param dealSubjectId 待处理的subjectId
     * @param s 微博
     * @param i 下标i
     */
    private void dealSubject325(int dealSubjectId, JSONObject s, int i) {
        Long id = s.getLong("_id");
        JSONObject match = s.getJSONObject("match_labels").getJSONObject("match");
        if (match == null) {
            return;
        }

        JSONArray windCodeArray = match.getJSONArray("windCode");
        JSONArray subjectArray = match.getJSONArray("subject");
        if (CollectionUtils.isEmpty(subjectArray)) {
            return;
        }

        // 只匹配到了325这个话题
        Integer subjectId = subjectArray.getInteger(0);
        if (subjectArray.size() == 1 && subjectId == dealSubjectId && CollectionUtils.isEmpty(windCodeArray)) {
            // 删掉整个元素
            Query query1 = new Query(Criteria.where("_id").is(id));
            DeleteResult de1 = mongoTemplate.remove(query1, "weibo_result");
            DeleteResult de2 = mongoTemplate.remove(query1, "weibo_operation_data_time");
            DeleteResult de3 = mongoTemplate.remove(query1, "weibo_repost_data");
            DeleteResult de4 = mongoTemplate.remove(query1, "weibo_comment_data");
        } else {
            // 删掉325这个元素
            for (int j=0; j<subjectArray.size(); ++j) {
                if (subjectArray.getInteger(j) == dealSubjectId) {
                    subjectArray.remove(j);
                    break;
                }
            }
            Query query2 = new Query(Criteria.where("_id").is(id));
            Update update2 = new Update().set("match_labels.match.subject", subjectArray);
            UpdateResult updateResult = mongoTemplate.updateFirst(query2, update2, "weibo_result");
        }

        if (i % 1000 == 0) {
            log.info("处理完毕第" + i + "个，weiboId=" + s.getLong("_id"));
        }
    }






    /**
     * match_others标签设置，以及微博title属性
     * @param executorService
     * @param beginDate
     * @param endDate
     */
    public void dealMatchOthers(ExecutorService executorService, Date beginDate, Date endDate) {
        // 在weibo_result查询
        Query query = new Query();
        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate)); // 可以分多个时间范围，多次处理
        query.fields().include("_id").include("match_labels.match.windCode").include("match_labels.relate");
        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, COLLECTION_WEIBO_RESULT);
        log.info("weiboResultList: " + weiboResultList.size());

        // weiboId，windCode
        Map<Long, JSONArray> weiboIdWindCodeMap = weiboResultList.stream()
            .filter(s -> s.getJSONObject("match_labels") != null)
            .filter(s -> s.getJSONObject("match_labels").getJSONObject("match") != null)
            .filter(s -> s.getJSONObject("match_labels").getJSONObject("match").getJSONArray("windCode") != null)
            .collect(Collectors.toMap(s -> s.getLong("_id"), s -> s.getJSONObject("match_labels").getJSONObject("match").getJSONArray("windCode")));
        // weiboId，relate
        Map<Long, JSONArray> weiboIdRelateMap = weiboResultList.stream()
            .filter(s -> s.getJSONObject("match_labels") != null)
            .filter(s -> s.getJSONObject("match_labels").getJSONArray("relate") != null)
            .collect(Collectors.toMap(s -> s.getLong("_id"), s -> s.getJSONObject("match_labels").getJSONArray("relate")));

        // 在weibo_data中查询
        List<Long> idList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
        Query query2 = new Query();
        query2.addCriteria(Criteria.where("created_at").gte(beginDate).lt(endDate));
        query2.addCriteria(Criteria.where("_id").in(idList));
        query2.fields().include("_id").include("text");
        List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, COLLECTION_WEIBO_DATA);
        log.info("weiboDataList: " + weiboDataList.size());

        for (int i=0; i<weiboDataList.size(); ++i) {
            int finalI = i;

            JSONObject s = weiboDataList.get(i);
            Long id = s.getLong("_id");
            String text = s.getString("text");
//            String title = consumerService.getProfitLm(text).getString("title");
            String title = getTitle(text);

            JSONArray windCodeArray = weiboIdWindCodeMap.get(id);
            JSONArray relate = weiboIdRelateMap.get(id);

            executorService.execute(() -> dealSingleWeiboMatchOthers(id, title, text,relate, windCodeArray, finalI, beginDate, endDate));
//            dealSingleWeiboMatchOthers(id, title, text,relate, windCodeArray, finalI, beginDate, endDate);
        }
    }
    private void dealSingleWeiboMatchOthers(Long id, String title, String text, JSONArray relate, JSONArray windCodeArray, int i, Date beginDate, Date endDate) {

        // 获取公司的关联度
        JSONObject matchOthers = new JSONObject();

        // 重新匹配资讯分类接口
        windCodeArray = windCodeArray == null ? new JSONArray() : windCodeArray;
        relate = relate == null ? new JSONArray() : relate;
        if (!CollectionUtils.isEmpty(windCodeArray)) {
            List<String> windCodeList = windCodeArray.stream().map(String.class::cast).collect(Collectors.toList());
//            Map<String, Integer> companyRelatedScoreMap = consumerService.getCompanyRelatedScore(title, text,
//                windCodeList.toArray(new String[windCodeList.size()]), relate);
            Map<String, Integer> companyRelatedScoreMap = new HashMap<>();

            Map<String, Integer> resultMap = new HashMap<>();
            Iterator<Map.Entry<String, Integer>> iterator = companyRelatedScoreMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> next = iterator.next();
                String key = next.getKey();
                Integer value = next.getValue();
                resultMap.put(key.replace(".", "_"), value);
            }

            matchOthers.put("linked_stock_score",  resultMap);
        }

        // 入库
        Query query3 = new Query(Criteria.where("_id").is(id));
        Update update3 = new Update().set("match_others", matchOthers).set("title", title);
        mongoTemplate.updateFirst(query3, update3, COLLECTION_WEIBO_RESULT);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (i % 1000 == 0) {
            log.info(sdf.format(beginDate) + "到" + sdf.format(endDate) + "处理完毕第" + i + "个，weiboId=" + id);
        }
    }









    /**
     * weibo_result表，match_classifies、match_labels、match_others属性
     * @param executorService
     * @param beginDate
     * @param endDate
     */
    public void dealMatchLabels(ExecutorService executorService, Date beginDate, Date endDate) {
        // 在weibo_result查询
        Query query = new Query();
        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate));
        query.fields().include("_id").include("title");
        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, COLLECTION_WEIBO_RESULT);
        log.info("weiboResultList: " + weiboResultList.size());

        // weiboIdTitleMap：weiboId、title
        Map<Long, String> weiboIdTitleMap =
            weiboResultList.stream().collect(Collectors.toMap(s -> s.getLong("_id"), s -> s.getString("title") == null ? "" : s.getString("title") ));

        List<Long> idList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
        Query query2 = new Query();
        query2.addCriteria(Criteria.where("created_at").gte(beginDate).lt(endDate));
        query2.addCriteria(Criteria.where("_id").in(idList));
        query2.fields().include("_id").include("text").include("user").include("created_at");
        List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, COLLECTION_WEIBO_DATA);
        log.info("weiboDataList: " + weiboDataList.size());

        for (int i=0; i<weiboDataList.size(); ++i) {
            JSONObject s = weiboDataList.get(i);
            int finalI = i;
//            executorService.execute(() -> dealWeiboCategory(s, weiboIdTitleMap.get(s.getLong("_id")), finalI));
            dealWeiboCategory(s, weiboIdTitleMap.get(s.getLong("_id")), finalI);
        }
    }

    /**
     * 处理单条微博
     * @param s
     */
    public void dealWeiboCategory(JSONObject s, String title, int i) {
        Long id = s.getLong("_id");
        String text = s.getString("text");
        JSONObject user = s.getJSONObject("user");

        // 重新匹配资讯分类接口
        JSONObject object = new JSONObject();
        object.put("text", text);
        object.put("user", user);
        List<Integer> listTypes = new ArrayList<>();
        JSONObject matchData = new JSONObject();

        // step3：匹配话题接口
//        List<Integer> subjectIds = consumerService.matchSubject(text);
        List<Integer> subjectIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(subjectIds)) {
            List<Integer> validSubjectIds = subjectIds.stream().map(subjectId -> subjectMap.get(subjectId))
                .filter(m -> ((Date)m.get("start_time")).compareTo(s.getDate("created_at")) <= 0)
                .map(m -> ((Integer)m.get("id"))).collect(Collectors.toList());
            matchData.getJSONObject("match").put("subject", validSubjectIds);
        }


        JSONArray windCodeArray = matchData.getJSONObject("match").getJSONArray("windCode");

        // 获取公司的关联度
        JSONObject matchOthers = new JSONObject();
        if (!CollectionUtils.isEmpty(windCodeArray)) {
            List<String> windCodeList = windCodeArray.stream().map(String.class::cast).collect(Collectors.toList());
//            Map<String, Integer> companyRelatedScoreMap = consumerService.getCompanyRelatedScore(title, text,
//                windCodeList.toArray(new String[windCodeList.size()]), matchData.getJSONArray("relate"));
            Map<String, Integer> companyRelatedScoreMap = new HashMap<>();

            Map<String, Integer> resultMap = new HashMap<>();
            Iterator<Map.Entry<String, Integer>> iterator = companyRelatedScoreMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> next = iterator.next();
                String key = next.getKey();
                Integer value = next.getValue();
                resultMap.put(key.replace(".", "_"), value);
            }

            matchOthers.put("linked_stock_score",  resultMap);
        }



        // 入库
        Query query3 = new Query(Criteria.where("_id").is(id));
        Update update3 = new Update().set("match_classifies", listTypes).set("match_labels", matchData).set("match_others", matchOthers);
        mongoTemplate.updateFirst(query3, update3, COLLECTION_WEIBO_RESULT);

        if (i % 100 == 0) {
            log.info("处理完毕第" + i + "个，weiboId=" + s.getLong("_id"));
        }

    }






    private static String getTitle(String text) {
        String regex = "【()】";
        Pattern pattern = Pattern.compile("【(.+?)】");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String group = matcher.group(1);
            return group;
        }
        return null;
    }

    public static void main(String[] args) {
        String text = "【北向资金连续第八日净流入 格力电器、中国平安净买入额居前】北向资【金】今日净流入27.85亿元，为连续第八日净流入。盘后数据显示，格力电器、中国平安、招商银行净买入居前，分别获净买入4.66亿元、3.56亿元、2.98亿元。此外，近期回调的猪肉股温氏股份、"
            + "牧原股份分别获净买入1.6亿元、1.21亿元。贵州茅台、恒瑞医药、中国软件净卖出额居前，净卖出金额分别2.08亿元、1.87亿元、0.91亿元。";

        String title = getTitle(text);

    }


}
