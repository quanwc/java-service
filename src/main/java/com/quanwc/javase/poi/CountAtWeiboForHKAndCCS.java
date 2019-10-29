package com.quanwc.javase.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * 处理港股、中概股
 * @author quanwenchao
 * @date 2019/3/21 16:13:38
 */
@Slf4j
//@Component
public class CountAtWeiboForHKAndCCS implements ApplicationRunner {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HKAndCCSRepository hkAndCCSRepository;

    // mongo分页每次查询条数  1百万
    private static final Integer DEFAULT_COUNT = 1000000;

    // 存放excel中的股票code、股票name
    private static final List<Map<String, String>> stockExcelList = new ArrayList<>();

    //@PostConstruct
    public void initExcel() {
        File file = new File("/usr/local/projects/media-subscribe" + File.separator + "港股+中概股名称.xlsx");
//        File file = new File("E:" + File.separator + "港股+中概股名称.xlsx");
        excelImportWithStock(file);
    }

    /**
     * 容器启动的时候执行该方法
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
//        ExecutorService executorService = Executors.newFixedThreadPool(76);
//        for (int i = 0; i <= 75351782; i = i + DEFAULT_COUNT) {
//            int skip = i;
//            executorService.execute(() -> deal(skip, DEFAULT_COUNT));
//        }

        // 线程数量设置为cpu的核心数量
//        ExecutorService executorService = Executors.newFixedThreadPool(2);
//        executorService.execute(() -> deal(0, 100000));
//        executorService.execute(() -> deal(100000, 100000));
//        executorService.shutdown();

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> deal(0, 500000)); // 0-50万条记录);
        executorService.execute(() -> deal(500000, 1000000));// 50-100万条记录

    }

    /**
     * 多个线程分批（分页skip、limit）方式查询：
     * 从第skip条记录开始，处理limit条个
     * @param skip
     * @param limit
     */
    public void deal(Integer skip, Integer limit) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(Thread.currentThread().getName() + ", 线程start: " + sdf.format(new Date()));

		int num = 50000; // 每个线程，每次处理5万条数据
		for (int i = skip; i < skip + limit; i = i + num) {
            System.out.println(Thread.currentThread().getName() + ", i: " + i);
            List<HKAndCCS> listData = Lists.newArrayList();
			Query query = new Query();
			query.skip(i);
			query.limit(num);
//            query.with(new Sort(Sort.Direction.DESC, "_id")).limit(1);
            System.out.println(Thread.currentThread().getName() + "， 查询mongo start，" + sdf.format(new Date()));
			List<JSONObject> listFromMongo = mongoTemplate.find(query, JSONObject.class, "weibo-20190125");
            System.out.println(Thread.currentThread().getName() + "， 查询mongo end，" + sdf.format(new Date()));

            System.out.println(Thread.currentThread().getName() + "， 匹配excel内容 start，" + sdf.format(new Date()));
            for (JSONObject jsonObject : listFromMongo) {
                String text = jsonObject.getString("text"); // 微博内容
                for (Map<String, String> str: stockExcelList) {
                    String stockCode = str.get("stockCode");// 可能为null
                    String companyName = str.get("companyName");

                    // 检查合法性
                    boolean illegal = checkIllegal(companyName, text);
                    if (!illegal) {
                        continue;
                    }

                    boolean containsStockCode = (null == stockCode ? false : text.contains(stockCode));
                    boolean containsCompany = text.contains(companyName);
                    if (containsStockCode || containsCompany) {
                        HKAndCCS hkAndCCS = new HKAndCCS();
//                    hkAndCCS.setId(1); // 自增主键
                        hkAndCCS.setSendName(jsonObject.getJSONObject("user").getString("name"));
                        hkAndCCS.setStockCode(stockCode);
                        hkAndCCS.setStockName(companyName);
                        hkAndCCS.setSendDatetime(jsonObject.getTimestamp("created_at"));
                        hkAndCCS.setContent(text);
                        hkAndCCS.setWeiboId(jsonObject.getLong("id"));
                        listData.add(hkAndCCS);
                    }
                }
            }
            System.out.println(Thread.currentThread().getName() + "， 匹配excel内容 end，" + sdf.format(new Date()));
            // 入库
            if (listData.size() > 0) {
                System.out.println(Thread.currentThread().getName() + "， 插入mysql start，" + sdf.format(new Date()) + ", size: " + listData.size());
                hkAndCCSRepository.saveAll(listData);
                System.out.println(Thread.currentThread().getName() + "， 插入mysql end，" + sdf.format(new Date()) + ", size: " + listData.size());
            }
		}
        System.out.println(Thread.currentThread().getName() + ", 线程end: " + sdf.format(new Date()));
    }

    /**
     * 校验微博内容的合法性
     * @param text
     * @return
     */
    private boolean checkIllegal(String companyName, String text) {
        if (null == text) {
            return false;
        }
        // 过滤微博
        if ("微博".equals(companyName) && "转发微博".equals(text)) {
            return false;
        }
        return true;
    }

    /**
     * 导入并读取excel，将每一行数据塞到map中
     * @param file
     * @return
     */
    private static void excelImportWithStock(File file) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(Thread.currentThread().getName() + " excel start : " + sdf.format(new Date()));

        org.apache.poi.ss.usermodel.Workbook wb = null;
        try {
            try { // Excel 07
                wb = new XSSFWorkbook(new FileInputStream(file));
            } catch (Exception ex) { // Excel 03
                wb = new HSSFWorkbook(new FileInputStream(file));
            }

            // 读取第一个sheet
            Sheet hssfSheet = wb.getSheetAt(0);
            // 循环读取每个sheet的每一行数据，getLastRowNum获取总行数
            int sheetRowsNum = hssfSheet.getLastRowNum();
            System.out.println("hssfSheet.getLastRowNum()行数：" + sheetRowsNum + 1);
            if (sheetRowsNum != 0) {
                int cells = hssfSheet.getRow(1).getPhysicalNumberOfCells();
                int index = 0;
                for (int i = 0; i <= sheetRowsNum; i++) { // 遍历行
                    Map<String, String> map = new HashMap<>();
                    index++;
                    for (int j = 0; j < cells; j++) { // 遍历列
                        Cell cell = hssfSheet.getRow(i).getCell(j);
                        if (j == 1) {
                            map.put("stockCode", null == cell ? null : cell.getStringCellValue());
                        } else if (j == 2) {
                            map.put("companyName", cell.getStringCellValue());
                        }
                    }
                    stockExcelList.add(map);
                }
                System.out.println("stockExcelList: " + stockExcelList.size());
            }
            System.out.println(Thread.currentThread().getName() + " excel end : " + sdf.format(new Date()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        File file = new File("E:" + File.separator + "港股+中概股名称.xlsx");
//        excelImportWithStock(file);
//        System.out.println(stockExcelList.size());

//        int num = 0;
//        for (int i = 0; i < 75351782; i = i + DEFAULT_COUNT) {
//            int skip = i;
//            num ++;
//        }
//        System.out.println(num);

    }
}
