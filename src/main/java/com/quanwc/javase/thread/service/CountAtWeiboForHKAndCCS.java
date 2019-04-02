package com.quanwc.javase.thread.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.quanwc.javase.thread.bean.HKAndCCS;
import com.quanwc.javase.thread.repository.HKAndCCSRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 处理港股、中概股：
 *
 * 将mongo中的7千多万条数据，分别于excel中的内容匹配，匹配到的话，则插入到mysql
 * @author quanwenchao
 * @date 2019/3/21 16:13:38
 */
@Slf4j
@Component
public class CountAtWeiboForHKAndCCS implements ApplicationRunner {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HKAndCCSRepository hkAndCCSRepository;

    // mongo分页每次查询条数  1百万
    private static final Integer DEFAULT_COUNT = 1000000;

    // 存放excel中的股票code、股票name
    private static final List<Map<String, String>> stockExcelList = new ArrayList<>();

    @PostConstruct
    public void initExcel() {
        File file = new File("/usr/local/projects/media-task" + File.separator + "港股+中概股名称.xlsx");
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
        ExecutorService executorService = Executors.newFixedThreadPool(76);
        for (int i = 0; i <= 75351782; i = i + DEFAULT_COUNT) {
            int skip = i;
            executorService.execute(() -> deal(skip, DEFAULT_COUNT));
        }
        executorService.shutdown();
    }

    /**
     * 多个线程分批（分页skip、limit）方式查询：
     * 从第skip条记录开始，处理limit条个
     * @param skip
     * @param limit
     */
    public void deal(Integer skip, Integer limit) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(Thread.currentThread().getName() + ", 线程开始start: " + sdf.format(new Date()));

		int num = 10000;
		for (int i = skip; i <= skip + limit; i = i + num) {
            System.out.println("i: " + i);
            List<HKAndCCS> listData = Lists.newArrayList();
			Query query = new Query();
			query.skip(i);
			query.limit(num);
			List<JSONObject> listFromMongo = mongoTemplate.find(query, JSONObject.class, "weibo-20190125");

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
            // 入库
            if (listData.size() > 0) {
                hkAndCCSRepository.saveAll(listData);
            }
		}
        System.out.println("线程开始end: " + sdf.format(new Date()));
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        File file = new File("E:" + File.separator + "港股+中概股名称.xlsx");
//        excelImportWithStock(file);
//        System.out.println(stockExcelList.size());

        int num = 0;
        for (int i = 0; i < 75351782; i = i + DEFAULT_COUNT) {
            int skip = i;
            num ++;
        }
        System.out.println(num);

    }
}
