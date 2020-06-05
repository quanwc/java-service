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
//import com.media.entity.cms.BasicXueqiuStocks;
//import com.media.entity.fundData.WebNews;
//import com.media.entity.fundData.WebNewsTxt;
//import com.media.entity.hkus.NewsEntity;
//import com.media.entity.hkus.NewsMgEntity;
//import com.media.entity.mediaMonitor.*;
//import com.media.repository.cms.BasicXueqiuStocksRepository;
//import com.media.repository.fundData.WebNewsRepository;
//import com.media.repository.fundData.WebNewsTxtRepository;
//import com.media.repository.hkus.HkusNewsMgRepository;
//import com.media.repository.hkus.HkusNewsRepository;
//import com.media.repository.mediaMonitor.*;
//import com.media.service.ConsumerService;
//import com.media.service.NewsRelatedDegreeRule;
//import com.media.util.CommonUtil;
//import com.media.util.HttpMessageUtil;
//
//import lombok.extern.slf4j.Slf4j;
//
///**
// * news_result表相关数据导出
// *
// * @author quanwenchao
// * @date 2020/2/11 09:56:56
// */
//@Slf4j
//// @Component
//public class NewResultDataExport implements ApplicationRunner {
//
//    private static final String SOURCE_WEIBO = "微博";
//    private static final String SOURCE_NEWS = "新闻";
//    private static final String TITLE_TYPE_GENERATE = "生成标题";
//    private static final String TITLE_TYPE_SELF = "自带标题";
//
//    private int INSERT_DB_COUNT = 0; // 入库次数变量
//
//    private static List<Long> list = new ArrayList<>();
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
//    private WebNewsTxtRepository webNewsTxtRepository;
//    @Autowired
//    private WebNewsRepository webNewsRepository;
//    @Autowired
//    private EPositiveNegativeRepository ePositiveNegativeRepository;
//    @Autowired
//    private NewsRelatedDegreeRule newsRelatedDegreeRule;
//    @Autowired
//    private ESingleRepository eSingleRepository;
//    @Autowired
//    private EHealthRepository eHealthRepository;
//    @Autowired
//    private HkusNewsRepository hkusNewsRepository;
//    @Autowired
//    private HkusNewsMgRepository hkusNewsMgRepository;
//    @Autowired
//    private BasicXueqiuStocksRepository basicXueqiuStocksRepository;
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
////        newsResultImport7();
//        System.out.println("end");
//    }
//
//    public void weiboResultImport1() {
//        ExecutorService executorService = new ThreadPoolExecutor(10, 15, 10L, TimeUnit.SECONDS,
//            new ArrayBlockingQueue<>(100), new ThreadFactoryBuilder().setNameFormat("news-result-export-%d").build(),
//            new ThreadPoolExecutor.CallerRunsPolicy()); // 主线程执行
//
//        String dateFmt = "yyyy-MM-dd HH:mm:ss";
//        // 开始处理的时间范围，总量通过for循环次数控制0
//        String beginDateStr = "2020-01-04 00:00:00";
//        String endDateStr = "2020-02-04 00:00:00";
//        LocalDateTime startTime = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
//        LocalDateTime endTime = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));
//
//        long time1 = System.currentTimeMillis() / 1000;
//        log.info("NewsResultDataExport begin: " + LocalDateTime.now());
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
//
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    private void dealWeiboResultImport1(ExecutorService executorService, Date beginDate, Date endDate) {
//
//        // 在news_result查询
//        Query query = new Query();
//        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate).and("match_labels.match.windCode")
//            .exists(true).ne(null).not().size(0));
//        // query.addCriteria(Criteria.where("_id").mod(5, 0)); // 除以5余0
//        query.fields().include("_id").include("create_time");
//        List<JSONObject> newsResultList = mongoTemplate.find(query, JSONObject.class, "news_result");
//        log.info("newsResultList: " + newsResultList.size());
//        List<Long> idList = newsResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//
//        // 查询标题
//        List<WebNews> webNewsList = webNewsRepository.findByIdIn(idList);
//        Map<Long, String> webNewsIdTitleMap =
//            webNewsList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getTitle()));
//
//        // 查询text
//        List<WebNewsTxt> webNewsTextList = webNewsTxtRepository.findByNewsIdIn(idList);
//        webNewsTextList =
//            webNewsTextList.stream().filter(s -> s.getNewsTxt().length() <= 150).collect(Collectors.toList()); // 10000
//        log.info("webNewsTextList: " + webNewsTextList.size());
//
//        Map<Long, String> webNewsIdTextMap =
//            webNewsTextList.stream().collect(Collectors.toMap(s -> s.getNewsId(), s -> s.getNewsTxt()));
//        Set<Long> weiboDataIdSet = webNewsIdTextMap.keySet();
//
//        newsResultList =
//            newsResultList.stream().filter(s -> weiboDataIdSet.contains(s.getLong("_id"))).collect(Collectors.toList()); // 最终筛选的1w条微博
//
//        for (int i = 0; i < newsResultList.size(); ++i) {
//
//            if (INSERT_DB_COUNT > 10000) {
//                break;
//            }
//
//            JSONObject s = newsResultList.get(i);
//            Long id = s.getLong("_id");
//            LocalDateTime localDateTime = date2LocalDateTime(s.getDate("create_time"));
//            String createTime = localDateTime.format(formatter);
//            String title = webNewsIdTitleMap.get(id);
//            String text = webNewsIdTextMap.get(id);
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
//            dealNewsResultImport1(finalI, beginDate, endDate, id, createTime, title, text, positiveAlignment, profitLm,
//                postiveWord, negativeWord, finalLinkedStockScoreStr, finalInitKey, finalInitValue);
//        }
//    }
//
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//    private void dealNewsResultImport1(int i, Date beginDate, Date endDate, Long mid, String createTime, String title,
//        String text, String positiveAlignment, String profitLm, String postiveWord, String negativeWord,
//        String linkedStockScoreStr, String linkedStockScoreMaxCompany, Integer linkedStockScoreMaxValue) {
//
//        title = title == null ? "" : title;
//
//        EPneumonia0207 obj = new EPneumonia0207();
//        obj.setRefId(mid);
//        obj.setSource(SOURCE_NEWS);
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
//     * 统计news_result表，每天资讯的长度区间数量
//     */
//    public void newsResultImport3() {
//
//        String dateFmt = "yyyy-MM-dd HH:mm:ss";
//        String beginDateStr = "2020-02-01 00:00:00";
//        String endDateStr = "2020-02-05 00:00:00";
//        LocalDateTime beginDate = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
//        LocalDateTime endDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));
//
//        // 在news_result查询
//        Query query = new Query();
//        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate));
//        query.fields().include("_id").include("create_time");
//        List<JSONObject> newsResultList = mongoTemplate.find(query, JSONObject.class, "news_result");
//        log.info("newsResultList: " + newsResultList.size());
//
//        Map<String, List<JSONObject>> dailyNewsResultMap =
//            newsResultList.stream().collect(Collectors.groupingBy(s -> date2String(s.getDate("create_time"))));
//
//        Iterator<Map.Entry<String, List<JSONObject>>> iterator = dailyNewsResultMap.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, List<JSONObject>> next = iterator.next();
//            String key = next.getKey();
//            List<JSONObject> value = next.getValue();
//            List<Long> idList = value.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//
//            List<String> webNewsTextList = webNewsTxtRepository.findByNewsIdIn(idList).stream().map(s -> s.getNewsTxt())
//                .collect(Collectors.toList());
//
//            long count1 = webNewsTextList.stream().filter(s -> s.length() < 50).count();
//            long count2 = webNewsTextList.stream().filter(s -> s.length() >= 50 && s.length() < 100).count();
//            long count3 = webNewsTextList.stream().filter(s -> s.length() >= 100).count();
//            System.out.println(key + "\t" + count1 + "\t\t\t" + count2 + "\t\t\t" + count3);
//        }
//
//        System.out.println("newsResultImport3 end");
//    }
//
//    /**
//     * 微博内容、标题的正负面加权测试
//     */
//    private void newsResultImport4() {
//        String dateFmt = "yyyy-MM-dd HH:mm:ss";
//        String beginDateStr = "2020-01-20 00:00:00";
//        String endDateStr = "2020-02-20 00:00:00";
//        LocalDateTime startTime = LocalDateTime.parse(beginDateStr, DateTimeFormatter.ofPattern(dateFmt));
//        LocalDateTime endTime = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern(dateFmt));
//        Date beginDate = Date.from(startTime.toInstant(ZoneOffset.ofHours(8)));
//        Date endDate = Date.from(endTime.toInstant(ZoneOffset.ofHours(8)));
//
//        // 在news_result查询
//        Query query = new Query();
//        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate));
//        query.addCriteria(Criteria.where("_id").mod(5, 0)); // 除以5余0
//        query.fields().include("_id").include("create_time");
//        query.limit(6000);
//        List<JSONObject> weiboResultList = mongoTemplate.find(query, JSONObject.class, "news_result");
//        log.info("weiboResultList: " + weiboResultList.size());
//        List<Long> idList = weiboResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//
//        // 查询标题
//        List<WebNews> webNewsList = webNewsRepository.findByIdIn(idList);
//        Map<Long, String> webNewsIdTitleMap =
//            webNewsList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getTitle()));
//
//        // 查询内容
//        Map<Long, String> newsIdTextMap = webNewsTxtRepository.findByNewsIdIn(idList).stream()
//            .collect(Collectors.toMap(s -> s.getNewsId(), s -> s.getNewsTxt()));
//        log.info("newsIdTextMap: " + newsIdTextMap.size());
//
//        for (int i = 0; i < weiboResultList.size(); i++) {
//            JSONObject s = weiboResultList.get(i);
//            Long id = s.getLong("_id");
//            Date createTime = s.getDate("create_time");
//            String title = webNewsIdTitleMap.get(id);
//            String text = newsIdTextMap.get(id);
//
//            String titleType = TITLE_TYPE_SELF;
//            if (StringUtils.isEmpty(title)) {
//                try {
//                    title = getNewsTitle(text);
//                    titleType = TITLE_TYPE_GENERATE;
//                } catch (Exception e) {
//                    log.error(id + " " + text);
//                }
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
//            obj.setSource("新闻");
//            LocalDateTime localDateTime = date2LocalDateTime(s.getDate("create_time"));
//            obj.setCreateTime(localDateTime.format(formatter));
//            obj.setTitle(title);
//            obj.setTitleType(titleType);
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
//
//        }
//    }
//
//    /**
//     * 特定公司，综合情绪指数和特定新闻数据导出
//     */
//    public void newsResultImport5() {
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
//        // 在news_result查询
//        Query query = new Query();
//        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate));
//        query.addCriteria(
//            Criteria.where("create_time").gte(beginDate).and("match_labels.match.windCode").in(windCodeList));
//        query.fields().include("_id").include("create_time").include("title").include("profitLm")
//            .include("match_others.linked_stock_score").include("match_labels.match.windCode");
//        List<JSONObject> newsResultList = mongoTemplate.find(query, JSONObject.class, "news_result");
//        log.info("newsResultList: " + newsResultList.size());
//        List<Long> idList = newsResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//
//        List<JSONObject> newsLists =
//            newsResultList.stream().flatMap(s -> flatByCode(s).stream()).collect(Collectors.toList());
//        log.info("newsLists.size(): " + newsLists.size());
//
//        // 查询标题
//        List<WebNews> webNewsList = webNewsRepository.findByIdIn(idList);
//        Map<Long, String> webNewsIdTitleMap =
//            webNewsList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getTitle()));
//
//        // 查询内容
//        Map<Long, String> newsIdTextMap = webNewsTxtRepository.findByNewsIdIn(idList).stream()
//            .collect(Collectors.toMap(s -> s.getNewsId(), s -> s.getNewsTxt()));
//        log.info("newsIdTextMap: " + newsIdTextMap.size());
//
//        // 按照windCode分组
//        Map<String, List<JSONObject>> newsListMap =
//            newsLists.stream().collect(Collectors.groupingBy(s -> s.getString("windCode")));
//        int newsListMapSize = newsListMap.size();
//        log.info("newsListMap size: {}", newsListMapSize);
//
//        Iterator<Map.Entry<String, List<JSONObject>>> iterator = newsListMap.entrySet().iterator();
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
//                    Integer profitLm = s.getInteger("profitLm");
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
//                    String text = newsIdTextMap.get(id);
//                    String title = webNewsIdTitleMap.get(id);
//                    if (StringUtils.isEmpty(title)) {
//                        title = newsRelatedDegreeRule.getNewsTitle(text);
//                    }
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
//     * 健康值测试数据导出 http://39.108.244.181/zentaopms/www/story-view-780.html
//     */
//    public void newsResultImport7() {
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
//        String dealSource = "雪球";
//        String dealCollection = "xueqiu_result";
//
//        // 在news_result查询
//        Query query = new Query();
//        query.addCriteria(Criteria.where("create_time").gte(beginDate).lt(endDate));
//        query.addCriteria(Criteria.where("match_labels.match.windCode").in(windCodeList));
//        query.fields().include("_id").include("create_time").include("title").include("content").include("weigth")
//            .include("profitLm").include("match_others.linked_stock_score").include("match_labels.match.windCode");
//        List<JSONObject> newsResultList = mongoTemplate.find(query, JSONObject.class, dealCollection);
//        log.info("newsResultList: " + newsResultList.size());
//        List<Long> idList = newsResultList.stream().map(s -> s.getLong("_id")).collect(Collectors.toList());
//
//        // 查询内容、标题
//        List<BasicXueqiuStocks> basicXueqiuStocksList = basicXueqiuStocksRepository.findAllByIdIn(idList);
//        Map<Long, String> newsIdTextMap =
//            basicXueqiuStocksList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getDescription()));
//        Map<Long, String> newsIdTitleMap =
//            basicXueqiuStocksList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getTitle()));
//
//        newsResultList.stream().forEach(s -> {
//            Long id = s.getLong("_id");
//            Integer profitLm = s.getInteger("profitLm");
//            String profitLmString = "中性";
//            if (0 == profitLm) {
//                profitLmString = "负向";
//            } else if (1 == profitLm) {
//                profitLmString = "正向";
//            }
//            String createTime = date2String2(s.getDate("create_time"));
//            String text = newsIdTextMap.get(id);
//            String title = newsIdTitleMap.get(id);
//            if (StringUtils.isEmpty(title)) {
//                title = newsRelatedDegreeRule.getNewsTitle(text);
//            }
//            JSONObject profitLmObject = consumerService.getProfitLmWithKeword(text).getJSONObject("search");
//            String positiveAlignment = profitLmObject.getString("positiveAlignment");
//            double mediaWeight = s.getDouble("weigth");
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
//                    EHealth obj = new EHealth();
//                    obj.setSource(dealSource);
//                    obj.setRefId(id + "");
//                    obj.setTitle(title);
//                    obj.setText(text);
//                    obj.setCreateTime(createTime);
//                    obj.setProfitLm(profitLmString);
//                    obj.setPositiveAlignment(positiveAlignment);
//                    obj.setWindCode(windCode);
//                    obj.setWindCodeName(windCodeName);
//                    obj.setDegreeScore(degreeScore);
//                    obj.setMediaWeight(mediaWeight);
//                    // obj.setHourCompare("");
//                    // obj.setHourRepostsCount(0);
//                    // obj.setHourCommentsCount(0);
//                    eHealthRepository.save(obj);
//                }
//            }
//        });
//    }
//
//    /**
//     * 非微博数据源资讯的标题、内容查询
//     * 新闻(news_result)、雪球(xueqiu_result)、飞笛(fidnews_result)、港美股(hkusnews_result)、美股(hkusnews_mg_result)
//     *
//     * weibo_result: mongo数据库  weibo_data
//     * news_result：cms主库  fund_data  web_news、web_news_txt
//     * xueqiu_result：cms主库  cms  basic_xueqiu_stocks
//     * fidnews_result：在mongo中有存，content字段；  cms主库  cms  cms_content
//     * hkusnews_result：cms主库  hkus_stock_data  news
//     * hkusnews_mg_result：cms主库  hkus_stock_data  news_mg
//     */
//    private void notWeiboTextQuery() {
//
//        List<Long> idList = new ArrayList<>();
//        // idList.add(1L);
//        Long id = idList.get(0);
//
//        // 港美股：hkusnews_result
//        List<NewsEntity> hkusNewsList = hkusNewsRepository.findAllByIdIn(idList);
//        Map<Long, String> newsIdTextMap =
//            hkusNewsList.stream().collect(Collectors.toMap(s -> s.getId(), s -> CommonUtil.delHtmlTag(s.getContent())));
//        Map<Long, String> newsIdTitleMap =
//            hkusNewsList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getTitle()));
//        String text = newsIdTextMap.get(id);
//        String title = newsIdTitleMap.get(id);
//
//        // 美股: hkusnews_mg_result
//        List<NewsMgEntity> hkusMgNewsList = hkusNewsMgRepository.findAllByIdIn(idList);
//        Map<Long, String> newsMgIdTextMap =
//            hkusNewsList.stream().collect(Collectors.toMap(s -> s.getId(), s -> CommonUtil.delHtmlTag(s.getContent())));
//        Map<Long, String> newsMgIdTitleMap =
//            hkusNewsList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getTitle()));
//
//        // 雪球: xueqiu_result
//        List<BasicXueqiuStocks> basicXueqiuStocksList = basicXueqiuStocksRepository.findAllByIdIn(idList);
//        Map<Long, String> xueqiuIdTextMap =
//            basicXueqiuStocksList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getDescription()));
//        Map<Long, String> xueqiuIdTitleMap =
//            basicXueqiuStocksList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getTitle()));
//
//        // 新闻: news_result
//        List<WebNews> webNewsList = webNewsRepository.findByIdIn(idList);
//        Map<Long, String> webNewsIdTextMap = webNewsTxtRepository.findByNewsIdIn(idList).stream()
//            .collect(Collectors.toMap(s -> s.getNewsId(), s -> s.getNewsTxt()));
//        Map<Long, String> webNewsIdTitleMap =
//            webNewsList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getTitle()));
//
//        // 飞笛: fidnews_result
//        Query query = new Query();
//        query.addCriteria(Criteria.where("match_labels.match.windCode").in(idList));
//        query.fields().include("_id").include("create_time").include("title").include("profitLm").include("content")
//            .include("match_labels.match.windCode");
//        List<JSONObject> fidnewsResultList = mongoTemplate.find(query, JSONObject.class, "fidnews_result");
//
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
//    private static final String FORMAT_PATTERN2 = "yyyy-MM-dd HH:mm:ss";;
//    public static String date2String2(Date time) {
//        return new SimpleDateFormat(FORMAT_PATTERN2).format(time);
//    }
//
//    private static final String FORMAT_PATTERN = "yyyy-MM-dd";
//    public static String date2String(Date time) {
//        return new SimpleDateFormat(FORMAT_PATTERN).format(time);
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
