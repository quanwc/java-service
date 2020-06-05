//package com.quanwc.javase.data;
//
//import java.text.SimpleDateFormat;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.ZoneOffset;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//import javax.annotation.PostConstruct;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.StringUtils;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.google.common.util.concurrent.ThreadFactoryBuilder;
//import com.media.entity.mediaMonitor.*;
//import com.media.entity.weibo.InfluenceParam;
//import com.media.repository.fundData.WebNewsRepository;
//import com.media.repository.mediaMonitor.*;
//import com.media.service.ConsumerService;
//import com.media.service.NewsRelatedDegreeRule;
//import com.media.util.HttpMessageUtil;
//import com.media.util.influnce.WeiboBasicUtil;
//
//import lombok.extern.slf4j.Slf4j;
//
///**
// * weibo_result表，相关属性导出
// *
// * @author quanwenchao
// * @date 2019/10/12 10:16:12
// */
//@Slf4j
//// @Component
//public class WeiboResultDataExport implements ApplicationRunner {
//
//    private static final String SOURCE_WEIBO = "微博";
//    private static final String TITLE_TYPE_GENERATE = "生成标题";
//
//    private int INSERT_DB_COUNT = 0; // 入库次数变量
//
//    @Autowired
//    private MongoTemplate mongoTemplate;
//    @Autowired
//    private ConsumerService consumerService;
//    @Autowired
//    private EWeiboPneumoniaRepository eWeiboPneumoniaRepository;
//    @Autowired
//    private EPneumonia0207Repository ePneumonia0207Repository;
//    @Autowired
//    private EPneumoniaCount0207Repository ePneumoniaCount0207Repository;
//    @Autowired
//    private StocksRepo stocksRepo;
//    @Autowired
//    private HttpMessageUtil httpMessageUtil;
//    @Autowired
//    private KeywordNewRepository keywordNewRepository;
//    @Autowired
//    private EPneumoniaWindcodeRepository ePneumoniaWindcodeRepository;
//    @Autowired
//    private EPositiveNegativeRepository ePositiveNegativeRepository;
//    @Autowired
//    private NewsRelatedDegreeRule newsRelatedDegreeRule;
//    @Autowired
//    private ESingleRepository eSingleRepository;
//    @Autowired
//    private WebNewsRepository webNewsRepository;
//    @Autowired
//    private EHealthRepository eHealthRepository;
//
//    private Map<String, String> windCodeNameMap = new HashMap<>();
//    private List<String> positiveKeywordList = null; // 正面词List
//    private List<String> negativeKeywordList = null; // 负面词List
//
//    private Map<String, Integer> keywordTimeMap = new HashMap<>(); // 词频
//
//    @PostConstruct
//    private void init() {
//        List<StocksEntity> stocksEntityList = stocksRepo.findAll();
//        windCodeNameMap =
//            stocksEntityList.stream().collect(Collectors.toMap(s -> s.getWindCode(), s -> s.getStockCnName()));
//        positiveKeywordList = keywordNewRepository.listPositiveKeyword(); // 初始化正面词的List
//        negativeKeywordList = keywordNewRepository.listNegativeKeyword(); // 初始化负面词的List
//    }
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//
//        // weiboResultImport7();
//        System.out.println("end");
//    }
//
//    /**
//     * 肺炎病毒相关微博导出（正负向、新关联度等）
//     */
//    public void weiboResultImport1() {
//        ExecutorService executorService = new ThreadPoolExecutor(10, 15, 10L, TimeUnit.SECONDS,
//            new ArrayBlockingQueue<>(100), new ThreadFactoryBuilder().setNameFormat("weibo-result-import-%d").build(),
//            new ThreadPoolExecutor.CallerRunsPolicy()); // 主线程执行
//
//        String dateFmt = "yyyy-MM-dd HH:mm:ss";
//        String beginDateStr = "2020-01-04 00:00:00";
//        String endDateStr = "2020-02-04 00:00:00";
//        LocalDateTime startTime = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
//        LocalDateTime endTime = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));
//
//        long time1 = System.currentTimeMillis() / 1000;
//        log.info("WeiboResultDataExport begin: " + LocalDateTime.now());
//
//        Date from = Date.from(startTime.toInstant(ZoneOffset.ofHours(8)));
//        Date to = Date.from(endTime.toInstant(ZoneOffset.ofHours(8)));
//
//        this.dealWeiboResultImport1(executorService, from, to);
//
//        long time2 = System.currentTimeMillis() / 1000;
//        try {
//            executorService.shutdown();
//            boolean b = executorService.awaitTermination(200, TimeUnit.SECONDS);
//            if (b) {
//                log.info("所有任务执行完毕，共耗时" + (time2 - time1) + "秒");
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            log.info("error: " + e.getMessage());
//        }
//        log.info("WeiboResultDataExport end: " + LocalDateTime.now());
//    }
//
//    /**
//     * 肺炎相关的微博数据导出到mysql
//     *
//     * @param executorService
//     * @param beginDate
//     * @param endDate
//     */
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    private void dealWeiboResultImport1(ExecutorService executorService, Date beginDate, Date endDate) {
//
//        // 在weibo_result查询
//        Query query = new Query();
//        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate).and("match_labels.match.windCode")
//            .exists(true).ne(null).not().size(0));
//        query.addCriteria(Criteria.where("_id").mod(5, 0)); // 除以5余0
//        query.fields().include("_id").include("create_time").include("title")
//            .include("match_others.linked_stock_score");
//        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, "weibo_result");
//        log.info("weiboResultList: " + weiboResultList.size());
//        List<Long> idList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//
//        // 在weibo_data中查询
//        Query query2 = new Query();
//        query2.addCriteria(Criteria.where("created_at").gte(beginDate).lt(endDate));
//        query2.addCriteria(Criteria.where("_id").in(idList));
//        query2.fields().include("_id").include("text");
//        List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, "weibo_data");
//        weiboDataList =
//            weiboDataList.stream().filter(s -> s.getString("text").length() <= 150).collect(Collectors.toList());
//        log.info("weiboDataList: " + weiboDataList.size());
//
//        Map<Long, String> weiboDataIdTextMap =
//            weiboDataList.stream().collect(Collectors.toMap(s -> s.getLong("_id"), s -> s.getString("text")));
//        Set<Long> weiboDataIdSet = weiboDataIdTextMap.keySet();
//
//        weiboResultList = weiboResultList.stream().filter(s -> weiboDataIdSet.contains(s.getLong("_id")))
//            .collect(Collectors.toList()); // 最终筛选的1w条微博
//
//        for (int i = 0; i < weiboResultList.size(); ++i) {
//
//            if (INSERT_DB_COUNT > 10000) {
//                break;
//            }
//
//            JSONObject s = weiboResultList.get(i);
//            Long id = s.getLong("_id");
//            LocalDateTime localDateTime = date2LocalDateTime(s.getDate("create_time"));
//            String createTime = localDateTime.format(formatter);
//            String title = s.getString("title");
//            String text = weiboDataIdTextMap.get(id);
//
//            String initCompanyName = "";
//            Integer initDegreeScore = null;
//            String linkedStockScoreStr = "";
//            JSONObject relatedObject = httpMessageUtil.newsRelated(title, text).block();
//            JSONArray jsonArray = relatedObject.getJSONArray("obj");
//            if (!CollectionUtils.isEmpty(jsonArray)) {
//                List<JSONObject> relatedCompanyList = jsonArray.stream().filter(JSONObject.class::isInstance)
//                    .map(JSONObject.class::cast).filter(w -> w.getInteger("type") == 1).collect(Collectors.toList());
//                if (!CollectionUtils.isEmpty(relatedCompanyList)) {
//                    JSONObject initObject = relatedCompanyList.get(0);
//                    initCompanyName = initObject.getString("companyName");
//                    initDegreeScore = initObject.getInteger("degreeScore");
//                    for (int j = 0; j < relatedCompanyList.size(); ++j) {
//                        JSONObject jsonObject = relatedCompanyList.get(j);
//                        String companyName = jsonObject.getString("companyName");
//                        Integer degreeScore = jsonObject.getInteger("degreeScore");
//                        linkedStockScoreStr += (companyName + "=" + degreeScore + ", ");
//                        if (degreeScore > initDegreeScore) {
//                            initCompanyName = companyName;
//                            initDegreeScore = degreeScore;
//                        }
//                    }
//                }
//            }
//            if (!StringUtils.isEmpty(linkedStockScoreStr)) {
//                linkedStockScoreStr = linkedStockScoreStr.trim();
//                linkedStockScoreStr = linkedStockScoreStr.trim().substring(0, linkedStockScoreStr.length() - 1);
//            }
//
//            if (null == initDegreeScore || initDegreeScore <= 60) {
//                continue; // 只处理关联度大于60的数据
//            }
//
//            JSONObject profitLmObject = consumerService.getProfitLmWithKeword(text).getJSONObject("search");
//            String profitLm = profitLmObject.getString("result");
//            String positiveAlignment = profitLmObject.getString("positiveAlignment");
//            String postiveWord = profitLmObject.getString("postiveWord");
//            String negativeWord = profitLmObject.getString("negativeWord");
//            String allSegmentation = profitLmObject.getString("allSegmentation");
//            String[] split = allSegmentation.split("/");
//            for (int i1 = 0; i1 < split.length; i1++) {
//                String s1 = split[i1];
//                s1 = s1.replaceAll("[\\pP\\pS\\pZ]", "");
//                if (StringUtils.isEmpty(s1)) {
//                    continue;
//                }
//                if (keywordTimeMap.containsKey(s1)) {
//                    keywordTimeMap.put(s1, keywordTimeMap.get(s1) + 1);
//                } else {
//                    keywordTimeMap.put(s1, 1);
//                }
//            }
//
//            int finalI = i;
//            String finalLinkedStockScoreStr = linkedStockScoreStr;
//            String finalInitKey = initCompanyName;
//            Integer finalInitValue = initDegreeScore;
//
//            dealWeiboResultImport1(finalI, beginDate, endDate, id, createTime, title, text, positiveAlignment, profitLm,
//                postiveWord, negativeWord, finalLinkedStockScoreStr, finalInitKey, finalInitValue);
//        }
//    }
//
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//    private void dealWeiboResultImport1(int i, Date beginDate, Date endDate, Long mid, String createTime, String title,
//        String text, String positiveAlignment, String profitLm, String postiveWord, String negativeWord,
//        String linkedStockScoreStr, String linkedStockScoreMaxCompany, Integer linkedStockScoreMaxValue) {
//
//        title = title == null ? "" : title;
//
//        EPneumonia0207 obj = new EPneumonia0207();
//        obj.setRefId(mid);
//        obj.setSource(SOURCE_WEIBO);
//        obj.setCreateTime(createTime);
//        obj.setTitle(title);
//        if (!StringUtils.isEmpty(title)) {
//            obj.setTitleType(TITLE_TYPE_GENERATE);
//        }
//        obj.setText(text);
//        obj.setTextLength(text.length());
//        obj.setProfitLm(profitLm);
//        obj.setPositiveAlignment(positiveAlignment);
//        obj.setProfitLmPositiveNegativeCount((StringUtils.isEmpty(postiveWord) ? 0 : postiveWord.split(",").length)
//            + (StringUtils.isEmpty(negativeWord) ? 0 : negativeWord.split(",").length));
//        obj.setProfitLmPositiveCount(StringUtils.isEmpty(postiveWord) ? 0 : postiveWord.split(",").length);
//        obj.setProfitLmPositiveKeyword(postiveWord);
//        obj.setProfitLmNegativeCount(StringUtils.isEmpty(negativeWord) ? 0 : negativeWord.split(",").length);
//        obj.setProfitLmNeagtiveKeyword(negativeWord);
//        obj.setLinkedStockScore(linkedStockScoreStr);
//        obj.setLinkedStockScoreMaxCompany(linkedStockScoreMaxCompany);
//        obj.setLinkedStockScoreMaxValue(linkedStockScoreMaxValue);
//
//        if (INSERT_DB_COUNT <= 10000) {
//            ePneumonia0207Repository.save(obj);
//            if (INSERT_DB_COUNT % 100 == 0) {
//                log.info("INSERT_DB_COUNT： " + INSERT_DB_COUNT);
//            }
//            INSERT_DB_COUNT++;
//        }
//
//        if (i % 100 == 0) {
//            log.info(sdf.format(beginDate) + "到" + sdf.format(endDate) + "处理完毕第" + i + "个，weiboId=" + mid);
//        }
//    }
//
//    private void dealPneumoniaKeyword() {
//        Iterator<Map.Entry<String, Integer>> iterator = keywordTimeMap.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, Integer> next = iterator.next();
//            String key = next.getKey();
//            Integer value = next.getValue();
//
//            EPneumoniaCount0207 obj = new EPneumoniaCount0207();
//            obj.setKeyword(key);
//            obj.setCount(value);
//
//            boolean negativeBo = negativeKeywordList.contains(key);
//            boolean positiveBo = positiveKeywordList.contains(key);
//            String profitStr = "未收录";
//            if (negativeBo) {
//                profitStr = "负向";
//            } else if (positiveBo) {
//                profitStr = "正向";
//            }
//
//            obj.setProfitLm(profitStr);
//            if (!StringUtils.isEmpty(key) || !"null".equals(key) || !"\n".equals(key)) {
//                ePneumoniaCount0207Repository.save(obj);
//            }
//        }
//    }
//
//    /**
//     * Date -> LocalDateTime
//     *
//     * @param date
//     * @return
//     */
//    public static LocalDateTime date2LocalDateTime(Date date) {
//        Instant instant = date.toInstant();
//        ZoneId zoneId = ZoneId.systemDefault();
//        return instant.atZone(zoneId).toLocalDateTime();
//    }
//
//    /**
//     * weibo_reslt表，匹配A股公司的冠状病毒特定微博导出
//     */
//    public void weiboResultImport2() {
//        String dateFmt = "yyyy-MM-dd HH:mm:ss";
//        String beginDateStr = "2020-01-20 00:00:00";
//        LocalDateTime startTime = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
//        Date beginDate = Date.from(startTime.toInstant(ZoneOffset.ofHours(8)));
//
//        List<String> windCodeList = new ArrayList<>();
//        windCodeList.add("688166.SH");
//        windCodeList.add("000651.SZ");
//        windCodeList.add("600438.SH");
//        windCodeList.add("002460.SZ");
//        windCodeList.add("002024.SZ");
//        windCodeList.add("600717.SH");
//        windCodeList.add("603086.SH");
//        windCodeList.add("688158.SH");
//
//        // 在weibo_result查询
//        Query query = new Query();
//        query.addCriteria(
//            Criteria.where("create_time").gte(beginDate).and("match_labels.match.windCode").in(windCodeList));
//        query.fields().include("_id").include("create_time").include("title").include("profit_lm")
//            .include("match_labels.match.windCode").include("match_others.linked_stock_score");
//        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, "weibo_result");
//        log.info("weiboResultList: " + weiboResultList.size());
//
//        List<JSONObject> resultWeiboResultList = weiboResultList.stream().filter(s -> {
//            JSONArray windCodeArray = s.getJSONObject("match_labels").getJSONObject("match").getJSONArray("windCode");
//            String windCodes = windCodeArray.stream().map(r -> r.toString()).collect(Collectors.joining(","));
//            if (windCodes.contains("SZ") || windCodes.contains("SH")) {
//                return true;
//            }
//            return false;
//        }).collect(Collectors.toList());
//        log.info("resultWeiboResultList.size: " + resultWeiboResultList.size());
//        List<Long> idList = resultWeiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//
//        // 在weibo_data中查询
//        Query query2 = new Query();
//        // query2.addCriteria(Criteria.where("created_at").gte(beginDate));
//        query2.addCriteria(Criteria.where("_id").in(idList));
//        query2.fields().include("_id").include("text").include("user.screen_name").include("reposts_count")
//            .include("comments_count");
//        List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, "weibo_data");
//        Map<Long, JSONObject> weiboIdMap =
//            weiboDataList.stream().collect(Collectors.toMap(s -> s.getLong("_id"), s -> s));
//        log.info("weiboDataList: " + weiboDataList.size());
//
//        List<String> pneumoniaList = new ArrayList<>();
//        pneumoniaList.add("冠状病毒");
//        pneumoniaList.add("肺炎");
//        pneumoniaList.add("疫情");
//
//        List<JSONObject> list = weiboDataList.stream().filter(s -> {
//            String text = s.getString("text");
//            boolean b = pneumoniaList.stream().anyMatch(w -> text.contains(w));
//            return b;
//        }).collect(Collectors.toList());
//        System.out.println(list.size());
//
//        List<Long> longList = list.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//
//        List<JSONObject> collectList = resultWeiboResultList.stream().filter(s -> longList.contains(s.getLong("_id")))
//            .collect(Collectors.toList());
//
//        collectList.stream().forEach(s -> {
//            Long id = s.getLong("_id");
//            LocalDateTime localDateTime = date2LocalDateTime(s.getDate("create_time"));
//            String createTime = localDateTime.format(formatter);
//            JSONObject weiboData = weiboIdMap.get(id);
//            String text = weiboData.getString("text");
//            Integer repostsCount = weiboData.getInteger("reposts_count");
//            Integer commentsCount = weiboData.getInteger("comments_count");
//            String screenName = weiboData.getJSONObject("user").getString("screen_name");
//            String title = s.getString("title");
//            Integer profitLm = s.getInteger("profit_lm");
//            String profitLmString = "中性";
//            if (0 == profitLm) {
//                profitLmString = "负向";
//            } else if (1 == profitLm) {
//                profitLmString = "正向";
//            }
//            final String profitLmStringFinal = profitLmString;
//
//            HashMap<String, Integer> linkedScoreMap = new HashMap<>();
//            Set<Map.Entry<String, Object>> entrySet =
//                s.getJSONObject("match_others").getJSONObject("linked_stock_score").entrySet();
//            Iterator<Map.Entry<String, Object>> iterator = entrySet.iterator();
//            while (iterator.hasNext()) {
//                Map.Entry<String, Object> next = iterator.next();
//                String key = next.getKey();
//                key = key.replace("_", ".");
//                Integer value = (Integer)next.getValue();
//                linkedScoreMap.put(key, value);
//            }
//
//            JSONArray windCodeArray = s.getJSONObject("match_labels").getJSONObject("match").getJSONArray("windCode");
//            windCodeArray.stream().forEach(w -> {
//
//                String windCode = (String)w;
//
//                boolean contains = windCodeList.contains(windCode);
//                if (!contains) {
//                    return;
//                }
//
//                EPneumoniaWindcode obj = new EPneumoniaWindcode();
//                obj.setMid(id);
//                obj.setWindCode(windCode);
//                obj.setWindCodeName(windCodeNameMap.get(windCode));
//                obj.setCreateTime(createTime);
//                obj.setTitle(title);
//                obj.setText(text);
//                obj.setScreenName(screenName);
//                obj.setProfitLm(profitLmStringFinal);
//                obj.setRepostsCount(repostsCount);
//                obj.setCommentsCount(commentsCount);
//                obj.setLinkedScore(linkedScoreMap.get(windCode));
//
//                ePneumoniaWindcodeRepository.save(obj);
//            });
//        });
//    }
//
//    /**
//     * 统计weibo_result表，每天资讯的长度区间数量
//     */
//    public void weiboResultImport3() {
//
//        String dateFmt = "yyyy-MM-dd HH:mm:ss";
//        String beginDateStr = "2020-02-01 00:00:00";
//        String endDateStr = "2020-02-05 00:00:00";
//        LocalDateTime beginDate = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
//        LocalDateTime endDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));
//
//        // 在weibo_result查询
//        Query query = new Query();
//        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate));
//        query.fields().include("_id");
//        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, "weibo_result");
//        log.info("weiboResultList: " + weiboResultList.size());
//
//        List<Long> idList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//
//        // 在weibo_data中查询
//        Query query2 = new Query();
//        query2.addCriteria(Criteria.where("_id").in(idList));
//        query2.fields().include("_id").include("created_at").include("text");
//        List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, "weibo_data");
//        log.info("weiboDataList: " + weiboDataList.size());
//
//        Map<String, List<JSONObject>> dailyWeiboDataMap =
//            weiboDataList.stream().collect(Collectors.groupingBy(s -> date2String(s.getDate("created_at"))));
//
//        Iterator<Map.Entry<String, List<JSONObject>>> iterator = dailyWeiboDataMap.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, List<JSONObject>> next = iterator.next();
//            String key = next.getKey();
//            List<JSONObject> value = next.getValue();
//            long count1 = value.stream().map(s -> s.getString("text")).filter(s -> s.length() < 50).count();
//            long count2 =
//                value.stream().map(s -> s.getString("text")).filter(s -> s.length() >= 50 && s.length() < 100).count();
//            long count3 = value.stream().map(s -> s.getString("text")).filter(s -> s.length() >= 100).count();
//            System.out.println(key + "\t" + count1 + "\t\t\t" + count2 + "\t\t\t" + count3);
//        }
//        System.out.println("weibResultImport3 end");
//    }
//
//    /**
//     * 微博内容、标题的正负面加权测试
//     */
//    private void weiboResultImport4() {
//        String dateFmt = "yyyy-MM-dd HH:mm:ss";
//        String beginDateStr = "2020-01-20 00:00:00";
//        String endDateStr = "2020-02-20 00:00:00";
//        LocalDateTime startTime = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
//        LocalDateTime endTime = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));
//
//        Date beginDate = Date.from(startTime.toInstant(ZoneOffset.ofHours(8)));
//        Date endDate = Date.from(endTime.toInstant(ZoneOffset.ofHours(8)));
//
//        // 在weibo_result查询
//        Query query = new Query();
//        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate).and("title").exists(true).ne(null));
//        query.addCriteria(Criteria.where("_id").mod(5, 0)); // 除以5余0
//        query.fields().include("_id").include("create_time");
//        query.limit(6000);
//        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, "weibo_result");
//        log.info("weiboResultList: " + weiboResultList.size());
//        List<Long> idList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//
//        // 在weibo_data中查询
//        Query query2 = new Query();
//        query2.addCriteria(Criteria.where("_id").in(idList));
//        query2.fields().include("_id").include("text");
//        List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, "weibo_data");
//        log.info("weiboDataList: " + weiboDataList.size());
//
//        Map<Long, String> weiboIdTextMap =
//            weiboDataList.stream().collect(Collectors.toMap(s -> s.getLong("_id"), s -> s.getString("text")));
//
//        for (int i = 0; i < weiboResultList.size(); i++) {
//            JSONObject s = weiboResultList.get(i);
//            Long id = s.getLong("_id");
//            Date createTime = s.getDate("create_time");
//            String text = weiboIdTextMap.get(id);
//            String title = "";
//            try {
//                title = getNewsTitle(text);
//            } catch (Exception e) {
//                log.error(id + " " + text);
//            }
//            if (StringUtils.isEmpty(title)) {
//                continue;
//            }
//
//            JSONObject textProfitLmObject = consumerService.getProfitLmWithKeword2(title, text).getJSONObject("search");
//            String textProfitLm = textProfitLmObject.getString("result");
//            String textPositiveAlignment = textProfitLmObject.getString("positiveAlignment");
//            String textPostiveWord = textProfitLmObject.getString("postiveWord");
//            String textNegativeWord = textProfitLmObject.getString("negativeWord");
//            String textAllSegmentation = textProfitLmObject.getString("allSegmentation");
//
//            JSONObject titleProfitLmObject = consumerService.getProfitLmWithKeword2(title, "").getJSONObject("search");
//            String titleProfitLm = titleProfitLmObject.getString("result");
//            String titlePositiveAlignment = titleProfitLmObject.getString("positiveAlignment");
//            String titlePostiveWord = titleProfitLmObject.getString("postiveWord");
//            String titleNegativeWord = titleProfitLmObject.getString("negativeWord");
//            String titleAllSegmentation = titleProfitLmObject.getString("allSegmentation");
//
//            EPositiveNegative obj = new EPositiveNegative();
//            obj.setRefId(id);
//            obj.setSource("微博");
//            LocalDateTime localDateTime = date2LocalDateTime(s.getDate("create_time"));
//            obj.setCreateTime(localDateTime.format(formatter));
//            obj.setTitle(title);
//            obj.setTitleType(TITLE_TYPE_GENERATE);
//            obj.setText(text);
//            obj.setTextProfitLm(textProfitLm);
//            obj.setTextPositiveAlignment(textPositiveAlignment);
//            obj.setTextProfitLmPositiveCount(
//                StringUtils.isEmpty(textPostiveWord) ? 0 : textPostiveWord.split(",").length);
//            obj.setTextProfitLmNegativeCount(
//                StringUtils.isEmpty(textNegativeWord) ? 0 : textNegativeWord.split(",").length);
//
//            obj.setTitleProfitLm(titleProfitLm);
//            obj.setTitleProfitLmPositiveCount(
//                StringUtils.isEmpty(titlePostiveWord) ? 0 : titlePostiveWord.split(",").length);
//            obj.setTitleProfitLmNegativeCount(
//                StringUtils.isEmpty(titleNegativeWord) ? 0 : titleNegativeWord.split(",").length);
//            obj.setTitleProfitLmPositiveKeyword(titlePostiveWord);
//            obj.setTitleProfitLmNeagtiveKeyword(titleNegativeWord);
//
//            ePositiveNegativeRepository.save(obj);
//        }
//
//    }
//
//    private static final String FORMAT_PATTERN = "yyyy-MM-dd";
//    public static String date2String(Date time) {
//        return new SimpleDateFormat(FORMAT_PATTERN).format(time);
//    }
//
//    private static final String FORMAT_PATTERN2 = "yyyy-MM-dd HH:mm:ss";;
//    public static String date2String2(Date time) {
//        return new SimpleDateFormat(FORMAT_PATTERN2).format(time);
//    }
//
//    // 第一个【】 内的字符数 >= 10个 拿出来当中标题 否则不生成
//    public String getNewsTitle(String content) {
//        String createTitle = "";
//        if (content.contains("【") && content.contains("】")) {
//            String exgStr = content.substring(content.indexOf("【") + 1, content.indexOf("】"));
//            if (exgStr.length() >= 10) {
//                createTitle = exgStr;
//            }
//        }
//        return createTitle;
//    }
//
//    /**
//     * 特定公司，综合情绪指数和特定微博数据导出
//     */
//    public void weiboResultImport5() {
//        String dateFmt = "yyyy-MM-dd HH:mm:ss";
//        String beginDateStr = "2020-03-03 00:00:00";
//        String endDateStr = "2020-03-10 00:00:00";
//        LocalDateTime startTime = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
//        LocalDateTime endTime = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));
//
//        Date beginDate = Date.from(startTime.toInstant(ZoneOffset.ofHours(8)));
//        Date endDate = Date.from(endTime.toInstant(ZoneOffset.ofHours(8)));
//
//        List<String> windCodeList = new ArrayList<>();
//        windCodeList.add("601318.SH");
//        windCodeList.add("000651.SZ");
//        windCodeList.add("000063.SZ");
//        windCodeList.add("600999.SH");
//        windCodeList.add("600036.SH");
//
//        Map<String, String> windCodeNameMap = new HashMap<>();
//        windCodeNameMap.put("601318.SH", "中国平安");
//        windCodeNameMap.put("000651.SZ", "格力电器");
//        windCodeNameMap.put("000063.SZ", "中兴通讯");
//        windCodeNameMap.put("600999.SH", "招商证券");
//        windCodeNameMap.put("600036.SH", "招商银行");
//
//        // 在weibo_result查询
//        Query query = new Query();
//        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate));
//        query.addCriteria(
//            Criteria.where("create_time").gte(beginDate).and("match_labels.match.windCode").in(windCodeList));
//        query.fields().include("_id").include("create_time").include("title").include("profit_lm")
//            .include("match_others.linked_stock_score").include("match_labels.match.windCode");
//        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, "weibo_result");
//        log.info("weiboResultList: " + weiboResultList.size());
//        List<Long> idList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//
//        List<JSONObject> weiboLists =
//            weiboResultList.stream().flatMap(s -> flatByCode(s).stream()).collect(Collectors.toList());
//        log.info("weiboLists.size(): " + weiboLists.size());
//
//        // 在weibo_data中查询
//        Query query2 = new Query();
//        query2.addCriteria(Criteria.where("_id").in(idList));
//        query2.fields().include("_id").include("text");
//        List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, "weibo_data");
//        log.info("weiboDataList: " + weiboDataList.size());
//
//        Map<Long, String> weiboIdTextMap =
//            weiboDataList.stream().collect(Collectors.toMap(s -> s.getLong("_id"), s -> s.getString("text")));
//
//        // 按照windCode分组
//        Map<String, List<JSONObject>> weiboListMap =
//            weiboLists.stream().collect(Collectors.groupingBy(s -> s.getString("windCode")));
//        int weiboListMapSize = weiboListMap.size();
//        log.info("weiboListMap size: {}", weiboListMapSize);
//
//        Iterator<Map.Entry<String, List<JSONObject>>> iterator = weiboListMap.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, List<JSONObject>> next = iterator.next();
//            String windCode = next.getKey();
//            List<JSONObject> value = next.getValue();
//            if (!windCodeList.contains(windCode)) {
//                continue;
//            }
//
//            Map<String, List<JSONObject>> dailyWeiboResultListMap =
//                value.stream().collect(Collectors.groupingBy(s -> date2String(s.getDate("create_time"))));
//
//            Iterator<Map.Entry<String, List<JSONObject>>> iterator1 = dailyWeiboResultListMap.entrySet().iterator();
//            while (iterator1.hasNext()) {
//                Map.Entry<String, List<JSONObject>> next1 = iterator1.next();
//                String date = next1.getKey();
//                List<JSONObject> weiboList = next1.getValue();
//
//                weiboList.forEach(s -> {
//                    Long id = s.getLong("_id");
//                    Integer profitLm = s.getInteger("profit_lm");
//                    String profitLmString = "中性";
//                    if (0 == profitLm) {
//                        profitLmString = "负向";
//                    } else if (1 == profitLm) {
//                        profitLmString = "正向";
//                    }
//
//                    int degreeScore = 0;
//                    Set<Map.Entry<String, Object>> entrySet2 =
//                        s.getJSONObject("match_others").getJSONObject("linked_stock_score").entrySet();
//                    Iterator<Map.Entry<String, Object>> iterator2 = entrySet2.iterator();
//                    while (iterator2.hasNext()) {
//                        Map.Entry<String, Object> next2 = iterator2.next();
//                        String key2 = next2.getKey();
//                        key2 = key2.replace("_", ".");
//                        Integer value2 = (Integer)next2.getValue();
//                        if (key2.equals(windCode)) {
//                            degreeScore = value2;
//                        }
//                    }
//
//                    String text = weiboIdTextMap.get(id);
//                    String title = newsRelatedDegreeRule.getNewsTitle(text);
//
//                    Query query1 = new Query(Criteria.where("_id").is(windCode));
//                    JSONObject companyResult = mongoTemplate.find(query1, JSONObject.class, "company_result").get(0);
//                    JSONObject object = companyResult.getJSONArray("day").stream().filter(JSONObject.class::isInstance)
//                        .map(JSONObject.class::cast).filter(w -> w.getString("date").compareTo(date) == 0).findFirst()
//                        .get();
//                    Double singleIndex = object.getDouble("single_index");
//
//                    System.out.println(windCode + "  " + date + "  " + profitLmString + "  " + title + degreeScore);
//
//                    ESingle obj = new ESingle();
//                    obj.setRefId(id);
//                    obj.setWindCode(windCodeNameMap.get(windCode));
//                    obj.setDailyDate(date);
//                    obj.setTitle(title);
//                    obj.setText(text);
//                    obj.setProfitLm(profitLmString);
//                    obj.setDegreeScore(degreeScore);
//                    obj.setSingleIndex(singleIndex);
//                    eSingleRepository.save(obj);
//                });
//            }
//        }
//    }
//
//    /**
//     * weibo_result的每条微博，根据windCode拆分成多条微博记录
//     *
//     * @param object
//     * @return
//     */
//    private List<JSONObject> flatByCode(JSONObject object) {
//        return object.getJSONObject("match_labels").getJSONObject("match").getJSONArray("windCode").stream()
//            .map(windCode -> {
//                JSONObject s = (JSONObject)object.clone();
//                s.put("windCode", windCode);
//                return s;
//            }).collect(Collectors.toList());
//    }
//
//    /**
//     * 归因数据导出 http://39.108.244.181/zentaopms/www/story-view-777.html
//     */
//    private void weiboResultImport6() {
//
//        String dateFmt = "yyyy-MM-dd HH:mm:ss";
//        String beginDateStr = "2020-03-04 00:00:00";
//        String endDateStr = "2020-03-12 00:00:00";
//        LocalDateTime beginDate = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
//        LocalDateTime endDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));
//
//        // 在weibo_result查询
//        Query query = new Query();
//        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate).and("match_labels.match.windCode")
//            .exists(true).ne(null).not().size(0));
//        query.fields().include("_id").include("create_time");
//        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, "news_result");
//        log.info("weiboResultList: " + weiboResultList.size());
//
//        // 按天分组
//        Map<String, List<JSONObject>> dailyWeiboResultMap =
//            weiboResultList.stream().collect(Collectors.groupingBy(s -> date2String(s.getDate("create_time"))));
//
//        // map按照key排序
//        LinkedHashMap<String, List<JSONObject>> linkedHashMap =
//            dailyWeiboResultMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
//
//        Iterator<Map.Entry<String, List<JSONObject>>> iterator = linkedHashMap.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, List<JSONObject>> next = iterator.next();
//            String key = next.getKey();
//            List<JSONObject> value = next.getValue();
//            List<Long> idList = value.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//            List<com.media.entity.fundData.WebNews> webNewsList = webNewsRepository.findByIdIn(idList);
//            long count = webNewsList.stream().filter(s -> s.getSource().contains("新浪")).count();
//
//            System.out.println(key + "\t" + count);
//        }
//    }
//
//    /**
//     * 健康值测试数据导出 http://39.108.244.181/zentaopms/www/story-view-780.html
//     */
//    public void weiboResultImport7() {
//        String dateFmt = "yyyy-MM-dd HH:mm:ss";
//        String beginDateStr = "2020-02-24 00:00:00";
//        String endDateStr = "2020-03-09 00:00:00";
//        LocalDateTime startTime = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
//        LocalDateTime endTime = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));
//
//        Date beginDate = Date.from(startTime.toInstant(ZoneOffset.ofHours(8)));
//        Date endDate = Date.from(endTime.toInstant(ZoneOffset.ofHours(8)));
//
//        List<String> windCodeList = new ArrayList<>();
//        windCodeList.add("601318.SH");
//        windCodeList.add("02318.HK");
//        windCodeList.add("000001.SZ");
//        windCodeList.add("600036.SH");
//        windCodeList.add("03968.HK");
//        windCodeList.add("00700.HK");
//        windCodeList.add("600745.SH");
//
//        Map<String, String> windCodeNameMap = new HashMap<>();
//        windCodeNameMap.put("601318.SH", "中国平安");
//        windCodeNameMap.put("02318.HK", "中国平安");
//        windCodeNameMap.put("000001.SZ", "中国平安");
//        windCodeNameMap.put("600036.SH", "招商银行");
//        windCodeNameMap.put("03968.HK", "招商银行");
//        windCodeNameMap.put("00700.HK", "腾讯控股");
//        windCodeNameMap.put("600745.SH", "闻泰科技");
//
//        // 在weibo_result查询
//        Query query = new Query();
//        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate));
//        query.addCriteria(Criteria.where("match_labels.match.windCode").in(windCodeList));
//        query.fields().include("_id").include("create_time").include("profit_lm")
//            .include("match_others.linked_stock_score").include("match_labels.match.windCode");
//        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, "weibo_result");
//        log.info("weiboResultList: " + weiboResultList.size());
//        List<Long> idList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//
//        // 在weibo_data中查询
//        Query query2 = new Query();
//        query2.addCriteria(Criteria.where("_id").in(idList));
//        query2.fields().include("_id").include("text").include("user");
//        List<JSONObject> weiboDataList = mongoTemplate.find(query2, JSONObject.class, "weibo_data");
//        log.info("weiboDataList: " + weiboDataList.size());
//
//        Map<Long, String> weiboIdTextMap =
//            weiboDataList.stream().collect(Collectors.toMap(s -> s.getLong("_id"), s -> s.getString("text")));
//        Map<Long, JSONObject> weiboIdUserMap =
//            weiboDataList.stream().collect(Collectors.toMap(s -> s.getLong("_id"), s -> s.getJSONObject("user")));
//
//        // 遍历处理每一条微博
//        weiboResultList.stream().forEach(s -> {
//            Long id = s.getLong("_id");
//            Integer profitLm = s.getInteger("profit_lm");
//            String profitLmString = "中性";
//            if (0 == profitLm) {
//                profitLmString = "负向";
//            } else if (1 == profitLm) {
//                profitLmString = "正向";
//            }
//            String createTime = date2String2(s.getDate("create_time"));
//            String text = weiboIdTextMap.get(id);
//            String title = newsRelatedDegreeRule.getNewsTitle(text);
//            JSONObject profitLmObject = consumerService.getProfitLmWithKeword(text).getJSONObject("search");
//            String positiveAlignment = profitLmObject.getString("positiveAlignment");
//            // 获取用户权重
//            InfluenceParam param = consumerService.getInfluenceParam(weiboIdUserMap.get(id));
//            double mediaWeight = WeiboBasicUtil.getUserInfluence(param);
//
//            Set<Map.Entry<String, Object>> entrySet2 =
//                s.getJSONObject("match_others").getJSONObject("linked_stock_score").entrySet();
//            Iterator<Map.Entry<String, Object>> iterator2 = entrySet2.iterator();
//            while (iterator2.hasNext()) {
//                Map.Entry<String, Object> next2 = iterator2.next();
//                String key2 = next2.getKey();
//                String windCode = key2.replace("_", ".");
//                String windCodeName = windCodeNameMap.get(windCode);
//                Integer degreeScore = (Integer)next2.getValue();
//                if (windCodeList.contains(windCode) && degreeScore >= 70) {
//                    // 获取该微博的每小时转发、评论改变的小时，并入库
//                    Query weiboOperationQuery = new Query(Criteria.where("_id").is(id));
//                    weiboOperationQuery.fields().include("operations");
//                    JSONObject weiboOperationDataTime =
//                        mongoTemplate.findOne(weiboOperationQuery, JSONObject.class, "weibo_operation_data_time");
//                    JSONArray operationArray = weiboOperationDataTime.getJSONArray("operations");
//                    if (operationArray.size() == 1) {
//                        JSONObject first = (JSONObject)operationArray.get(0);
//                        String date = first.getString("date");
//                        int tempComments = 0;
//                        int tempReposts = 0;
//
//                        EHealth obj = new EHealth();
//                        obj.setSource("微博");
//                        obj.setRefId(id + "");
//                        obj.setTitle(title);
//                        obj.setText(text);
//                        obj.setCreateTime(createTime);
//                        obj.setProfitLm(profitLmString);
//                        obj.setPositiveAlignment(positiveAlignment);
//                        obj.setWindCode(windCode);
//                        obj.setWindCodeName(windCodeName);
//                        obj.setDegreeScore(degreeScore);
//                        obj.setMediaWeight(mediaWeight);
//                        obj.setHourCompare(date);
//                        obj.setHourRepostsCount(tempReposts);
//                        obj.setHourCommentsCount(tempComments);
//                        eHealthRepository.save(obj);
//                    } else {
//                        JSONObject first = (JSONObject)operationArray.get(0);
//                        Integer initComments = first.getInteger("comments");
//                        Integer initRePosts = first.getInteger("rePosts");
//                        for (int i = 1; i < operationArray.size(); i++) {
//                            JSONObject operation = (JSONObject)operationArray.get(i);
//                            String date = operation.getString("date");
//                            Integer comments = operation.getInteger("comments");
//                            Integer rePosts = operation.getInteger("rePosts");
//
//                            int tempComments = comments - initComments;
//                            int tempReposts = rePosts - initRePosts;
//                            if (tempComments != 0 || tempReposts != 0) {
//                                // db
//                                initComments = comments;
//                                initRePosts = rePosts;
//
//                                EHealth obj = new EHealth();
//                                obj.setSource("微博");
//                                obj.setRefId(id + "");
//                                obj.setTitle(title);
//                                obj.setText(text);
//                                obj.setCreateTime(createTime);
//                                obj.setProfitLm(profitLmString);
//                                obj.setPositiveAlignment(positiveAlignment);
//                                obj.setWindCode(windCode);
//                                obj.setWindCodeName(windCodeName);
//                                obj.setDegreeScore(degreeScore);
//                                obj.setMediaWeight(mediaWeight);
//                                obj.setHourCompare(date);
//                                obj.setHourRepostsCount(tempReposts);
//                                obj.setHourCommentsCount(tempComments);
//                                eHealthRepository.save(obj);
//                            }
//                        }
//                    }
//                }
//            }
//        });
//    }
//
//    public static void main(String[] args) {
//        String str = "300598.SZ=-1, 300526.SZ=-1, 300552.SZ=-1, ".trim();
//        String substring = str.substring(0, str.length() - 1);
//        System.out.println(substring);
//
//        List<String> pneumoniaList = new ArrayList<>();
//        pneumoniaList.add("疫情");
//        pneumoniaList.add("肺炎");
//        pneumoniaList.add("冠状病毒");
//        pneumoniaList.add("新冠");
//        String text = "是新冠";
//        boolean b = pneumoniaList.stream().anyMatch(w -> text.contains(w));
//        System.out.println(b);
//
//        String str11 = "123/null.$n../。。";
//        String regex = "[\\pP\\pS\\pZ]";
//        System.out.println(str11.replaceAll(regex, ""));
//    }
//}
