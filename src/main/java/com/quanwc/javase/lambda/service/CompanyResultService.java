package com.quanwc.javase.lambda.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bson.Document;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.extern.slf4j.Slf4j;


/**
 * 情绪值（单小时微博的情绪值） == 单小时增量
 * 情绪指数 == 过去24小时的增量值 -> 求和 -> 再归一化
 */
@Service
@Slf4j
public class CompanyResultService {

    // 新增公司发送到ES
    private static final String ES_COMPANY_URL = "http://120.24.69.36:8817/es/incrementStocks?windCodes=";

    // 公司下30天的热议关键词，拆分的中间表r_company_hot_keyword
    private static final String R_COMPANY_HOT_KEYWORD = "r_company_hot_keyword";
    // 公司下30天的热议人物，拆分的中间表r_company_hot_person
    private static final String R_COMPANY_HOT_PERSON = "r_company_hot_person";
    // r_weibo_kol
    private static final String R_WEIBO_KOL = "r_weibo_kol";

    private final MongoTemplate mongoTemplate;
    private final RedisTemplateCms<String, String> redisTemplateCms;
    private RestTemplate restTemplate;

    public CompanyResultService(MongoTemplate mongoTemplate,
                                RedisTemplateCms redisTemplateCms, RestTemplate restTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplateCms = redisTemplateCms;
        this.restTemplate = restTemplate;
    }


    public void initCompany() {
        try {
            List<Map<String, Object>> companyList = null;
            // 查询最大id
            Optional<String> maxId = redisTemplateCms.opsForValue().get("MEDIA:COMPANY:MAXID").blockOptional();
            if (maxId.isPresent()) {
//                companyList = stocksRepository.listHqCompany(Long.parseLong(maxId.get()));
            } else {
//                companyList = stocksRepository.listHqCompany();
            }
            if (CollectionUtils.isEmpty(companyList)) {
                return;
            }

            // 初始化company_result
//            initCompanyResult(companyList);
            // 初始化company_result_news
//            initCompanyResultNews(companyList);
            // 初始化company_result_xueqiu
//            initCompanyResultXueqiu(companyList);

            // 将新增的公司初始化到company_result_day_hour表
            initCompanyResultDayHour(companyList);

            // 将新增的公司初始化到company_result_daily_data表
            initCompanyResultDailyData(companyList);

            // 将maxId放到redis
            boolean upResult = redisTemplateCms.opsForValue().set("MEDIA:COMPANY:MAXID",
                String.valueOf(companyList.get(companyList.size() - 1).get("id"))).block();
            if (upResult) {
                log.info("InitCompanyResultServiceImpl initModel()，更新redis成功");
            }

            // 发送公司信息到ES
            sendCompanyList2ES(companyList);
        } catch (DataAccessException e) {
            log.error("定时任务出现异常:\nInitCompanyResultServiceImpl initCompany()", e);
        }
    }

    /**
     * 新增公司新增到company_result_day_hour表，并初始化当天、第二天的模板
     * @param companyList
     */
    private void initCompanyResultDayHour(List<Map<String, Object>> companyList) {
        if (CollectionUtils.isEmpty(companyList)) {
            return;
        }

        LocalDateTime todayDate = LocalDateTime.now();
        LocalDateTime tomorrowDate = LocalDateTime.now().plusDays(1);
        String todayDateStr = todayDate.format(DateTimeFormatter.ISO_DATE);
        String tomorrowDateStr = tomorrowDate.format(DateTimeFormatter.ISO_DATE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");

        LocalDateTime todayZero = LocalDateTime.of(todayDate.toLocalDate(), LocalTime.of(0, 0, 0));
        List<Map<String, Object>> todayHourResultList = IntStream.rangeClosed(0, 23).boxed().collect(ArrayList::new, (list, item) -> {
            Map<String, Object> map = new HashMap<>();
            LocalDateTime withHour = todayZero.withHour(item);
            map.put("date", formatter.format(withHour));
            map.put("p_index", 0.0);
            map.put("n_index", 0.0);
            list.add(map);
        }, List::addAll);

        LocalDateTime tomorrowZero = LocalDateTime.of(tomorrowDate.toLocalDate(), LocalTime.of(0, 0, 0));
        List<Map<String, Object>> tomorrowHourResultList = IntStream.rangeClosed(0, 23).boxed().collect(ArrayList::new, (list, item) -> {
            Map<String, Object> map = new HashMap<>();
            LocalDateTime withHour = tomorrowZero.withHour(item);
            map.put("date", formatter.format(withHour));
            map.put("p_index", 0.0);
            map.put("n_index", 0.0);
            list.add(map);
        }, List::addAll);

        List<Document> stringList = companyList.stream().map(s -> {
            Document document = new Document("_id", (String) s.get("wind_code"))
                .append(todayDateStr, todayHourResultList).append(tomorrowDateStr, tomorrowHourResultList);
            return document;
        }).collect(Collectors.toList());
        mongoTemplate.insert(stringList, "company_table_day_hour");
    }

    /**
     * 新增公司新增到company_result_daily_data表，并初始化当天、第二天的模板
     * @param companyList
     */
    private void initCompanyResultDailyData(List<Map<String, Object>> companyList) {
        if (CollectionUtils.isEmpty(companyList)) {
            return;
        }

        LocalDateTime todayDate = LocalDateTime.now();
        LocalDateTime tomorrowDate = LocalDateTime.now().plusDays(1);
        String todayDateStr = todayDate.format(DateTimeFormatter.ISO_DATE);
        String tomorrowDateStr = tomorrowDate.format(DateTimeFormatter.ISO_DATE);

        List<Document> stringList = companyList.stream().map(s -> {
            Document document = new Document("_id", (String) s.get("wind_code"))
                .append(todayDateStr, new Document()).append(tomorrowDateStr, new Document());
            return document;
        }).collect(Collectors.toList());
        mongoTemplate.insert(stringList, "company_table_daily_data");
    }


    /**
     * 新增公司发送到ES
     * @param companyList 公司列表
     */
    public void sendCompanyList2ES(List<Map<String, Object>> companyList){
        if (CollectionUtils.isEmpty(companyList)) {
            return;
        }

        // Collectors.joining(",")
        String windCodeStr = companyList.stream().map(m -> (String)m.get("wind_code")).collect(Collectors.joining(","));
        String esCompanyUrl = ES_COMPANY_URL + windCodeStr;
        JSONObject jsonObject = restTemplate.getForObject(esCompanyUrl, JSONObject.class);
        if (0 != jsonObject.getInteger("code")){
        }
    }





    public void updateDayAndClearOld() {
        try {
            log.info("company result updateDayAndClearOld begin");
            // 更新company_result日模板以及清理老数据
            updateCompanyResultDayAndClearOld();
            // 更新company_result_news日模板以及清理老数据
//            updateCompanyResultNewsDayAndClearOld();
            // 更新company_result_xueqiu日模板以及清理老数据
//            updateCompanyResultXueqiuDayAndClearOld();
            log.info("company result updateDayAndClearOld end");
        } catch (Exception e) {
            log.error("定时任务出现异常:\nInitCompanyResultServiceImpl updateDayAndClearOld()", e);
        }
    }

    /**
     * 更新公司(company_result)日模板以及清理day老数据
     */
    public void updateCompanyResultDayAndClearOld() {
        LocalDateTime now = LocalDateTime.now().plusDays(1); // 初始化第二天的日模板数据
        String dayDate = now.format(DateTimeFormatter.ISO_DATE);
        List<JSONObject> companyResultList = mongoTemplate.findAll(JSONObject.class, "company_table");
        for (JSONObject s : companyResultList) {
            JSONArray days = s.getJSONArray("day");
            JSONObject dayModel = getHourModelJson(dayDate);
            days.add(dayModel);

            // 按照day倒排序、将JSONArray类型的days赋值给List<Object>类型的dayList
            // 注意：不能写成List<Object> dayList = sortJsonArray(days)，这是一个bug
            days = sortJsonArray(days);
            List<Object> dayList = days;
            if (dayList.size() > 61) {
                dayList = dayList.subList(0, 61);
            }

            // 更新入库
            Query query = new Query(Criteria.where("_id").is(s.getString("_id")));
            Update update = Update.update("day", dayList);
            mongoTemplate.updateFirst(query, update, "company_table");
        }

        // 数据同步到company_result_test表，之后要删掉
        List<JSONObject> companyResultTestList = mongoTemplate.findAll(JSONObject.class, "company_result_test");
        for (JSONObject s : companyResultTestList) {
            JSONArray days = s.getJSONArray("day");
            JSONObject dayModel = getHourModelJson(dayDate);
            days.add(dayModel);

            // 按照day倒排序、将JSONArray类型的days赋值给List<Object>类型的dayList
            // 注意：不能写成List<Object> dayList = sortJsonArray(days)，这是一个bug
            days = sortJsonArray(days);
            List<Object> dayList = days;
            if (dayList.size() > 61) {
                dayList = dayList.subList(0, 61);
            }

            // 更新入库
            Query query = new Query(Criteria.where("_id").is(s.getString("_id")));
            Update update = Update.update("day", dayList);
            mongoTemplate.updateFirst(query, update, "company_result_test");
        }
    }


    /**
     * 更新公司每天每小时情绪指数表(company_result_day_hour)的第二天小时模板
     */
    public void addDayHourHourModel() {
        try {
            log.info("addDayHourHourModel addDayHourHourModel begin");
            // 更新company_result_day_hour表第二天模板
            addCompanyResultDayHourHourModel();
            // 更新company_result_news_day_hour表第二天模板
//            addCompanyResultNewsDayHourHourModel();
            // 更新company_result_xueqiu_day_hour表第二天模板
//            addCompanyResultXueqiuDayHourHourModel();
            log.info("addDayHourHourModel end");
        } catch (Exception e) {
            log.error("定时任务出现异常:\nInitCompanyResultServiceImpl addDayHourHourModel()", e);
        }
    }

    /**
     * 更新company_result_day_hour表，第二天的小时维度模板
     */
    private void addCompanyResultDayHourHourModel() {
        LocalDateTime tomorrowDate = LocalDateTime.now().plusDays(1);
        String tomorrowDayDate = tomorrowDate.format(DateTimeFormatter.ISO_DATE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");

        LocalDateTime tomorrowZero = LocalDateTime.of(tomorrowDate.toLocalDate(), LocalTime.of(0, 0, 0));
        List<Map<String, Object>> hourResultList = IntStream.rangeClosed(0, 23).boxed()
            .collect(ArrayList::new, (list, item) -> {
                Map<String, Object> map = new HashMap<>();
                LocalDateTime withHour = tomorrowZero.withHour(item);
                map.put("date", formatter.format(withHour));
                map.put("p_index", 0.0);
                map.put("n_index", 0.0);
                list.add(map);
            }, List::addAll);

        Query query = new Query();
        query.fields().include("_id");
        List<JSONObject> companyResultList = mongoTemplate
            .find(query, JSONObject.class, "company_table_day_hour");

        companyResultList.parallelStream()
            .map(result -> result.getString("_id"))
            .forEach(id -> {
                Update update = Update.update(tomorrowDayDate, hourResultList);
                mongoTemplate.upsert(Query
                    .query(Criteria.where("_id").is(id)), update, "company_table_day_hour");
            });
    }




    /**
     * 初始化company_result_daily_data表的第二天模板
     */
    public void dealCompanyResultDailyData() {
        LocalDateTime tomorrowDate = LocalDateTime.now().plusDays(1);
        String tomorrowDayDate = tomorrowDate.format(DateTimeFormatter.ISO_DATE);
        Query query = new Query();
        query.fields().include("_id");
        List<JSONObject> companyResultList = mongoTemplate
            .find(query, JSONObject.class, "company_table_daily_data");
        companyResultList.parallelStream()
            .map(result -> result.getString("_id"))
            .forEach(id -> {
                Update update = Update.update(tomorrowDayDate, new Document());
                mongoTemplate.upsert(Query
                    .query(Criteria.where("_id").is(id)), update, "company_table_daily_data");
            });
    }

    /**
     * 动态计算公司的基期安全值
     * 禅道：http://39.108.244.181/zentaopms/www/story-view-622.html
     */
    public void computeBaseSafetyValue2() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
        String endDateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log.info("company result computeBaseSafetyValue daily begin");

        try{
            List<JSONObject> jsonObjectList = mongoTemplate.findAll(JSONObject.class, "company_table");
            for (JSONObject jsonObject : jsonObjectList) {
                String id = jsonObject.getString("_id"); // 股票代码(公司)

                JSONArray dayArray = sortJsonArray(jsonObject.getJSONArray("day")); // 公司的day下的数据
                List<JSONObject> dayArrayResult =
                    dayArray.stream().filter(JSONObject.class::isInstance).map(JSONObject.class::cast).filter(
                        s -> s.getString("date").compareTo(endDateStr) < 0).collect((Collectors.toList()));

                // 计算正向基期
                double basePSafetyValue = 500;
                List<Double> pIndexList = dayArrayResult.stream().map(s2 -> s2.getDouble("p_index")).map(pIndex -> {
                    pIndex = pIndex <= 100 ? 500 : pIndex;
                    return pIndex;
                }).collect(Collectors.toList());
                // 去除最大值
                pIndexList.remove(Collections.max(pIndexList));
                basePSafetyValue = pIndexList.stream().mapToDouble(Double::valueOf).sum() / 60;
                basePSafetyValue = basePSafetyValue < 500 ? 500 : basePSafetyValue;

                // 计算负向基期
                double baseNSafetyValue = 500;
                List<Double> nIndexList = dayArrayResult.stream().map(s2 -> s2.getDouble("n_index")).map(nIndex -> {
                    nIndex = nIndex <= 100 ? 500 : nIndex;
                    return nIndex;
                }).collect(Collectors.toList());
                // 去除最大值
                nIndexList.remove(Collections.max(nIndexList));
                baseNSafetyValue = nIndexList.stream().mapToDouble(Double::valueOf).sum() / 60;
                baseNSafetyValue = baseNSafetyValue < 500 ? 500 : baseNSafetyValue;

                // step1：更新到company_result表
                Query query1 = new Query(Criteria.where("_id").is(id).and("day.date").is(date));
                Update update1 = new Update().set("base_p_safety_value", basePSafetyValue)
                    .set("base_n_safety_value", baseNSafetyValue)
                    .set("day.$.base_p_safety_value", basePSafetyValue)
                    .set("day.$.base_n_safety_value", baseNSafetyValue);
                mongoTemplate.updateFirst(query1, update1, "company_table");

                // step2：存储基期到company_result_daily_data表
                Query query2 = new Query(Criteria.where("_id").is(id));
                Update update2 = new Update().set(date + ".base_value.base_p_safety_value", basePSafetyValue)
                    .set(date + ".base_value.base_n_safety_value", baseNSafetyValue);
                mongoTemplate.updateFirst(query2, update2, "company_table_daily_data");
            }

            log.info("company result computeBaseSafetyValue daily end");
        } catch (Exception e) {
            log.error("computeBaseSafetyValue error: ", e);
        }
    }

    /**
     * 生成中位数
     * @param paramList
     * @return
     */
    private Double generateMedian(List<Double> paramList) {
        if (CollectionUtils.isEmpty(paramList)) {
            return 0.0;
        }
        // 对list排序
        Collections.sort(paramList);
        // 生成中位数
        Double median;
        if (paramList.size() % 2 == 0) {
            median = (paramList.get(paramList.size() / 2 - 1) + paramList.get(paramList.size() / 2)) / 2;
        } else {
            median = paramList.get(paramList.size() / 2);
        }
        return median;
    }

    /**
     * 处理公司下的热议关键词day30的临时表数据，submit_RComputeCompanyHotKeywordDay30
     */
    public void dealDay30RelationHotKeyword() {

        // 先清空R_COMPANY_HOT_KEYWORD表
        mongoTemplate.dropCollection(R_COMPANY_HOT_KEYWORD);

        Date beginDate;
        Date endDate;
        String dateFmt = "yyyy-MM-dd 00:00:00";
        String beginDateStr = LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ofPattern(dateFmt));
        String endDateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateFmt));

        try {
            beginDate = new SimpleDateFormat(dateFmt).parse(beginDateStr);
            endDate = new SimpleDateFormat(dateFmt).parse(endDateStr);
        } catch (ParseException e) {
            log.error("date parse error: ", e);
            throw new RuntimeException("date parse error");
        }

        ExecutorService executorService = new ThreadPoolExecutor(10, 50,
            10L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
            new ThreadFactoryBuilder().setNameFormat("company_hot_keyword-pool-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy());

        // step1：处理weibo_result
        Query query = new Query();
        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate)
                .and("match_labels.match.windCode").exists(true).ne(null).not().size(0)
                .and("match_labels.hotKey").exists(true).ne(null).not().size(0)
        );
        // 只查询特定属性
        query.fields().include("_id").include("match_labels.match.windCode").include("match_labels.hotKey")
            .include("last_n_index").include("last_p_index");
        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, "weibo_result");
        if (CollectionUtils.isEmpty(weiboResultList)) {
            return;
        }

        List<JSONObject> weiboLists = weiboResultList.stream().flatMap(s -> flatByCode(s).stream())
                .collect(Collectors.toList());
        log.info("weiboLists.size(): " + weiboLists.size());

        // 按照windCode分组
        Map<String, List<JSONObject>> weiboListMap =
            weiboLists.stream().collect(Collectors.groupingBy(s -> s.getString("windCode")));
        int weiboListMapSize = weiboListMap.size();
        log.info("weiboListMap size: {}", weiboListMapSize);

        int i = 1;
        for (Map.Entry<String, List<JSONObject>> entry : weiboListMap.entrySet()) {
            String windCode = entry.getKey();
            String percent = String.format("%.2f", i * 100 / (weiboListMapSize * 1.0)); // 百分比
            log.info("{} : 已处理: {}%, weibo windCode begin：{}", i++, percent, windCode);
            // windCode对应的所有微博
            List<JSONObject> value = entry.getValue();
            executorService.execute(() -> dealCompanyWeiboHotKeyword(windCode, value));
            log.info("weibo windCode end：" + windCode);
        }


        // step2：处理xueqiu_result
        Query query2 = new Query();
        query2.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate)
            .and("match_labels.match.windCode").exists(true).ne(null).not().size(0)
            .and("match_labels.hotKey").exists(true).ne(null).not().size(0)
        );
        // 只查询特定属性
        query2.fields().include("_id").include("match_labels.match.windCode").include("match_labels.hotKey")
            .include("negative_index").include("positive_index");
        List<JSONObject> xueqiuResultList = mongoTemplate.find(query2, JSONObject.class, "xueqiu_result");
        if (CollectionUtils.isEmpty(xueqiuResultList)) {
            return;
        }

        List<JSONObject> xueqiuLists = xueqiuResultList.stream().flatMap(s -> flatByCode(s).stream())
            .collect(Collectors.toList());
        log.info("xueqiuLists.size(): " + xueqiuLists.size());

        // 按照windCode分组
        Map<String, List<JSONObject>> xueqiuListsMap =
            xueqiuLists.stream().collect(Collectors.groupingBy(s -> s.getString("windCode")));
        int xueqiuListMapSize = xueqiuListsMap.size();
        log.info("xueqiuListMapSize size: {}", xueqiuListMapSize);

        int j = 1;
        for (Map.Entry<String, List<JSONObject>> entry : xueqiuListsMap.entrySet()) {
            String windCode = entry.getKey();
            String percent = String.format("%.2f", j * 100 / (xueqiuListMapSize * 1.0)); // 百分比
            log.info("{} : 已处理: {}%, xueqiu windCode begin：{}",j++, percent, windCode);
            // windCode对应的所有雪球
            List<JSONObject> value = entry.getValue();
            executorService.execute(() -> dealCompanyXueqiuHotKeyword(windCode, value));
            log.info("xueqiu windCode end：" + windCode);
        }

        executorService.shutdown();
    }

    /**
     * 处理微博
     * @param windCode
     * @param value
     */
    private void dealCompanyWeiboHotKeyword(String windCode, List<JSONObject> value) {
        List<JSONObject> objList = value.stream()
                .flatMap(json -> {
                            Long weiboId = json.getLong("_id");
                            Double totalHeatValue = json.getDouble("last_n_index") + json.getDouble("last_p_index");
                            JSONArray jsonArray = json.getJSONObject("match_labels").getJSONArray("hotKey");
                            return jsonArray.stream().map(hotKey -> {
                                JSONObject object = new JSONObject();
                                object.put("wind_code", windCode);
                                object.put("wind_code_hotKey", windCode + "-" + hotKey);
                                object.put("ref_id", "weibo-" + weiboId);
                                object.put("total_heat_value", totalHeatValue);
                                return object;
                            });
                        }
                ).collect(Collectors.toList());
        log.info("windCode: {}, related info size: {}", windCode, objList.size());
        try {
            mongoTemplate.insert(objList, R_COMPANY_HOT_KEYWORD);
        } catch (Exception e) {
            log.error("save info exception, windCode: " + windCode, e);
        }
    }

    /**
     * 处理雪球
     * @param windCode
     * @param value
     */
    private void dealCompanyXueqiuHotKeyword(String windCode, List<JSONObject> value) {
        List<JSONObject> objList = value.stream()
            .flatMap(json -> {
                    Long xueqiuId = json.getLong("_id");
                    Double negative_index = json.getDouble("negative_index");
                    Double positive_index = json.getDouble("positive_index");
                    negative_index = null == negative_index ? 0.0 : negative_index;
                    positive_index = null == positive_index ? 0.0 : positive_index;
                    Double totalHeatValue = negative_index + positive_index;

                    JSONArray jsonArray = json.getJSONObject("match_labels").getJSONArray("hotKey");
                    return jsonArray.stream().map(hotKey -> {
                        JSONObject object = new JSONObject();
                        object.put("wind_code", windCode);
                        object.put("wind_code_hotKey", windCode + "-" + hotKey);
                        object.put("ref_id", "xueqiu-" + xueqiuId);
                        object.put("total_heat_value", totalHeatValue);
                        return object;
                    });
                }
            ).collect(Collectors.toList());
        try {
            mongoTemplate.insert(objList, R_COMPANY_HOT_KEYWORD);
        } catch (Exception e) {
            System.out.println("save info exception, windCode: " + windCode + e);
        }
    }

    /**
     * weibo_result的每条微博，根据windCode拆分成多条微博记录
     * @param object
     * @return
     */
    private List<JSONObject> flatByCode(JSONObject object)  {
        List<JSONObject> collect =
            object.getJSONObject("match_labels").getJSONObject("match").getJSONArray("windCode").stream()
                .map(windCode -> {
                    JSONObject s =(JSONObject)object.clone();
                    s.put("windCode", windCode);
                    return s;
                }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 处理公司下的热议任务day30的临时表数据，submit_RComputeCompanyHotPersonDay30
     */
    public void dealDay30RelationHotPerson() {

        // 先清空R_COMPANY_HOT_KEYWORD表
        mongoTemplate.dropCollection(R_COMPANY_HOT_PERSON);

        Date beginDate;
        Date endDate;
        String dateFmt = "yyyy-MM-dd 00:00:00";
        String beginDateStr = LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ofPattern(dateFmt));
        String endDateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateFmt));

        try {
            beginDate = new SimpleDateFormat(dateFmt).parse(beginDateStr);
            endDate = new SimpleDateFormat(dateFmt).parse(endDateStr);
        } catch (ParseException e) {
            log.error("date parse error: ", e);
            throw new RuntimeException("date parse error");
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate)
            .and("match_labels.match.windCode").exists(true).ne(null).not().size(0)
            .and("match_labels.match.person").exists(true).ne(null).not().size(0)
        );
        // 只查询特定属性
        query.fields().include("_id").include("match_labels.match.windCode").include("match_labels.match.person")
            .include("last_n_index").include("last_p_index");
        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, "weibo_result");
        if (CollectionUtils.isEmpty(weiboResultList)) {
            return;
        }

        List<JSONObject> lists = weiboResultList.stream().flatMap(s -> flatByCode(s).stream())
            .collect(Collectors.toList());
        log.info("lists.size(): " + lists.size());

        // 按照windCode分组
        Map<String, List<JSONObject>> listMap =
            lists.stream().collect(Collectors.groupingBy(s -> s.getString("windCode")));


        ExecutorService executorService = new ThreadPoolExecutor(10, 50,
            10L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
            new ThreadFactoryBuilder().setNameFormat("company_hot_person-pool-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy());
        int i = 1;
        int size = listMap.size();
        log.info("map size: {}", size);
        for (Map.Entry<String, List<JSONObject>> entry : listMap.entrySet()) {
            String windCode = entry.getKey();
            String percent = String.format("%.2f", i * 100 / (size * 1.0)); // 百分比
            log.info("{} : 已处理: {}%, windCode begin：{}", i++, percent, windCode);
            // windCode对应的微博
            List<JSONObject> value = entry.getValue();
            executorService.execute(() -> dealCompanyHotPerson(windCode, value));
            log.info("windCode end：" + windCode);
        }
        executorService.shutdown();
    }

    private void dealCompanyHotPerson(String windCode, List<JSONObject> value) {
        List<JSONObject> objList = value.stream()
            .flatMap(json -> {
                    Long weiboId = json.getLong("_id");
                    Double totalHeatValue = json.getDouble("last_n_index") + json.getDouble("last_p_index");
                    JSONArray jsonArray = json.getJSONObject("match_labels").getJSONObject("match").getJSONArray("person");
                    return jsonArray.stream().map(person -> {
                        JSONObject object = new JSONObject();
                        object.put("wind_code", windCode);
                        object.put("wind_code_person", windCode + "-" + person);
                        object.put("weibo_id", weiboId);
                        object.put("total_heat_value", totalHeatValue);
                        return object;
                    });
                }
            ).collect(Collectors.toList());
        log.info("windCode: {}, related info size: {}", windCode, objList.size());
        try {
            mongoTemplate.insert(objList, R_COMPANY_HOT_PERSON);
        } catch (Exception e) {
            log.error("save info exception, windCode: " + windCode, e);
        }
    }

    /**
     * 处理kol的中间表：
     * 从weibo_result表查询满足条件的微博id，然后批量去weibo_data中查找微博的用户、点赞、转发、评论，再塞到r_weibo_kol
     */
    public void dealKOLReleation() {
        log.info("InitCompanyResultServiceImpl dealKOLReleation begin");

        // 先清空R_COMPANY_HOT_KEYWORD表
        mongoTemplate.dropCollection(R_WEIBO_KOL);

        Date beginDate;
        Date endDate;
        String dateFmt = "yyyy-MM-dd 00:00:00";
        String beginDateStr = LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ofPattern(dateFmt));
        String endDateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateFmt));

        try {
            beginDate = new SimpleDateFormat(dateFmt).parse(beginDateStr);
            endDate = new SimpleDateFormat(dateFmt).parse(endDateStr);
        } catch (ParseException e) {
            throw new RuntimeException("date parse error");
        }

        Query query1 = new Query();
        query1.addCriteria(Criteria.where("create_time").gte(beginDate).lte(endDate));
        // 只查询特定属性
        query1.fields().include("_id").include("match_labels.match.windCode");
        List<JSONObject> weiboResultList = mongoTemplate.find(query1, JSONObject.class, "weibo_result"); // weibo_result
        log.info("weiboResultList.size(): " + weiboResultList.size());
        if (CollectionUtils.isEmpty(weiboResultList)) {
            return;
        }

        List<Map<String, Object>> weiboIdList = weiboResultList.stream().map(s ->{
            Map<String, Object> map = Maps.newHashMap();
            map.put("_id",s.getLong("_id"));
            map.put("windCodes",s.getJSONObject("match_labels").getJSONObject("match").get("windCode"));
            return  map;
        }).collect(Collectors.toList());
        List<List<Map<String, Object>>> partitionList = Lists.partition(weiboIdList, 10000); // 每次处理1万条
        for (int i = 0; i < partitionList.size(); i++) {
            List<Map<String, Object>> maps = partitionList.get(i);
            List<Object> ids = maps
                    .stream()
                    .map(s -> s.get("_id"))
                    .collect(Collectors.toList());

            Query query2 = new Query();
            query2.addCriteria(Criteria.where("_id").in(ids));

            // 只查询特定属性
            query2.fields().include("user.id").include("_id").include("attitudes_count").include("reposts_count")
                .include("comments_count");
            List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, "weibo_data");

            List<JSONObject> objList = weiboDataList.stream().map(s -> {
                JSONObject object = new JSONObject();
                object.put("uid", s.getJSONObject("user").getLong("id"));
                object.put("weibo_id", s.getLong("_id"));
                object.put("attitudes_count", s.getLong("attitudes_count"));
                object.put("reposts_count", s.getLong("reposts_count"));
                object.put("comments_count", s.getLong("comments_count"));
                maps.forEach(a->{
                    if(a.get("_id").toString().equals(s.get("_id").toString())){
                        object.put("wind_codes",a.get("windCodes"));
                    }
                });
                return object;
            }).collect(Collectors.toList());

            mongoTemplate.insert(objList, R_WEIBO_KOL);
        }

        log.info("InitCompanyResultServiceImpl dealKOLReleation end");
    }

    /**
     * 获取json格式的IndexResult，方便updateDayAndClearOld()、addHourModelData()方法，避免覆盖单一情绪指数、情绪密度的问题
     * @param date
     * @return
     */
    private JSONObject getHourModelJson(String date) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("date", date);
        jsonObject.put("n_index", 0.0);
        jsonObject.put("p_index", 0.0);
        return jsonObject;
    }

    /**
     * JSONArray中的数据，按照日期倒排列
     * @param jsonArray
     * @return
     */
    private JSONArray sortJsonArray(JSONArray jsonArray) {
        if ((null == jsonArray || jsonArray.size() == 0)) {
            return new JSONArray();
        }

        JSONArray resultJsonArray = new JSONArray();

        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonList.add(jsonArray.getJSONObject(i));
        }

        Collections.sort(jsonList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                //                return o1.getInteger("n_index").compareTo(o2.getInteger("n_index")); // string比较大小
                return o2.getString("date").compareTo(o1.getString("date")); // string比较大小
            }
        });
        for (int i = 0; i < jsonList.size(); i++) {
            resultJsonArray.add(jsonList.get(i));
        }
        return resultJsonArray;
    }

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        String dayDate = now.format(DateTimeFormatter.ISO_DATE);
        System.out.println(dayDate);

        LocalDateTime tomorrowDate = LocalDateTime.now().plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");
        tomorrowDate.format(formatter);

        LocalDateTime tomorrowZero = LocalDateTime.of(tomorrowDate.toLocalDate(), LocalTime.of(0, 0, 0));
        List<Map<String, Object>> resultList = IntStream.rangeClosed(0, 23).boxed().collect(ArrayList::new, (list, item) -> {
            Map<String, Object> map = new HashMap<>();
            LocalDateTime withHour = tomorrowZero.withHour(item);
            map.put("date", formatter.format(withHour));
            map.put("p_index", 0.0D);
            map.put("n_index", 0.0D);
            list.add(map);
        }, List::addAll);
        System.out.println(resultList);


    }
}
