package com.quanwc.javase.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author quanwenchao
 * @date 2019/4/14 15:40:46
 */
public class WeiBoHKAndCCSUtil implements Serializable{

//    // 存放excel中的股票code、股票name
//    private static final List<Map<String, String>> stockExcelList = new ArrayList<>();

//    /**
//     * mongo数据与excel匹配，匹配到的存入到mysql
//     * @param dataRdd
//     */
//    public static void dealMongoDataWithExcel(JavaMongoRDD<Document> dataRdd) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println("deal mongo data begin: " + sdf.format(new Date()) );
//
//        File file = new File("/usr/local/media-scala" + File.separator + "港股+中概股名称.xlsx");
//        excelImportWithStock(file); // 会给stockExcelList添加元素
//
//        dataRdd.foreach(s1 -> {
//            String text = s1.getString("text");
//            stockExcelList.forEach(s2 -> {
//                String stockCode = s2.get("stockCode");// 可能为null
//                String companyName = s2.get("companyName");
//                boolean bo = checkIllegal(companyName, text);
//                if (!bo) {
//                    return;
//                }
//
//                String sql = "insert into hk_ccs_company_weibo_spark(send_name, stock_code, stock_name, send_datetime, content, weibo_id) " +
//                        "values(?, ?, ?, ?, ?, ?)";
//                PreparedStatement ps = null;
//                try {
//                    ps = getJdbcConnection().prepareStatement(sql);
//                    ps.setString(1, s1.getString("user.name"));
//                    ps.setString(2, stockCode);
//                    ps.setString(3, companyName);
//                    ps.setDate(4, new java.sql.Date(s1.getDate("created_at").getTime()));
//                    ps.setString(5, text);
//                    ps.setLong(6, s1.getLong("weibo_id"));
//                    ps.executeUpdate();
//                } catch (Exception e) {
//                    System.out.println("insert db error: " + e.getMessage());
//                    e.printStackTrace();
//                } finally {
//                    if (ps != null) {
//                        try {
//                            ps.close();
//                        } catch (SQLException e) {
//                            System.out.println("close connection: " + e.getMessage());
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//            });
//        });
//
//        System.out.println("deal mongo data end: " + sdf.format(new Date()) );
//    }


//    /**
//     * 导入并读取excel，将每一行数据塞到map中
//     * @param file
//     * @return
//     */
//    private static void excelImportWithStock(File file) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println(Thread.currentThread().getName() + " excel start : " + sdf.format(new Date()));
//
//        org.apache.poi.ss.usermodel.Workbook wb = null;
//        try {
//            try { // Excel 07
//                wb = new XSSFWorkbook(new FileInputStream(file));
//            } catch (Exception ex) { // Excel 03
//                wb = new HSSFWorkbook(new FileInputStream(file));
//            }
//
//            // 读取第一个sheet
//            Sheet hssfSheet = wb.getSheetAt(0);
//            // 循环读取每个sheet的每一行数据，getLastRowNum获取总行数
//            int sheetRowsNum = hssfSheet.getLastRowNum();
//            System.out.println("hssfSheet.getLastRowNum()行数：" + sheetRowsNum + 1);
//            if (sheetRowsNum != 0) {
//                int cells = hssfSheet.getRow(1).getPhysicalNumberOfCells();
//                int index = 0;
//                for (int i = 0; i <= sheetRowsNum; i++) { // 遍历行
//                    Map<String, String> map = new HashMap<>();
//                    index++;
//                    for (int j = 0; j < cells; j++) { // 遍历列
//                        Cell cell = hssfSheet.getRow(i).getCell(j);
//                        if (j == 1) {
//                            map.put("stockCode", null == cell ? null : cell.getStringCellValue());
//                        } else if (j == 2) {
//                            map.put("companyName", cell.getStringCellValue());
//                        }
//                    }
//                    stockExcelList.add(map);
//                }
//                System.out.println("stockExcelList: " + stockExcelList.size());
//            }
//            System.out.println(Thread.currentThread().getName() + " excel end : " + sdf.format(new Date()));
//        } catch (IOException e) {
//            System.out.println("read excel error: " + e.getMessage()) ;
//            e.printStackTrace();
//        }
//    }

//
//    /**
//     * 校验微博内容的合法性
//     * @param text
//     * @return
//     */
//    private static boolean checkIllegal(String companyName, String text) {
//        if (null == text) {
//            return false;
//        }
//        // 过滤微博
//        if ("微博".equals(companyName) && "转发微博".equals(text)) {
//            return false;
//        }
//        if ("微博".equals(companyName) && "轉發微博".equals(text)) {
//            return false;
//        }
//        return true;
//    }

//    /**
//     * 获取连接
//     * @return
//     * @throws Exception
//     */
//    private static Connection getJdbcConnection() throws Exception{
////        String jdbcUrl = "jdbc:mysql://rm-wz9ykd02hk1w8e064o.mysql.rds.aliyuncs.com/wefid?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&serverTimezone=GMT%2b8"; // 外网
//        String jdbcUrl = "jdbc:mysql://rm-wz9ykd02hk1w8e064rw.mysql.rds.aliyuncs.com/wefid?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&serverTimezone=GMT%2b8"; // 内网
//        //1.加载驱动程序
//        Class.forName("com.mysql.jdbc.Driver");
//        //2. 获得数据库连接
//        Connection conn = DriverManager.getConnection(jdbcUrl, "fid", "fid123");
//        return conn;
//    }



    public static List<Map<String, String>> excelhaha() {
        // 存放excel中的股票code、股票name
        List<Map<String, String>> stockExcelList = new ArrayList<>();

//        File file = new File("E:/港股+中概股名称.xlsx");
//        File file = new File("D:/港股+中概股名称.xls");

        File file = new File("/usr/local/media-scala" + File.separator + "港股+中概股名称.xlsx");
//        File file = new File("/usr/local/media-scala" + File.separator + "港股+中概股名称.xls");


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(Thread.currentThread().getName() + " excel start : " + sdf.format(new Date()));

        org.apache.poi.ss.usermodel.Workbook wb = null;



        try {
//            try { // Excel 07  后缀.xlsx
//                wb = new XSSFWorkbook(new FileInputStream(file));
//            } catch (Exception ex) { // Excel 03 后缀.xls
//                wb = new HSSFWorkbook(new FileInputStream(file));
//            }

            boolean is2007 = file.getName().endsWith(".xlsx");
            if (is2007) {
                wb = new XSSFWorkbook(new FileInputStream(file));
            } else {
                wb = new HSSFWorkbook(new FileInputStream(file));
            }

//            boolean is2003 = file.getName().endsWith(".xls");
//            if (!is2003) {
//                wb = new XSSFWorkbook(new FileInputStream(file));
//            } else {
//                wb = new HSSFWorkbook(new FileInputStream(file));
//            }

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
            System.out.println("read excel error: " + e.getMessage()) ;
            e.printStackTrace();
        }

        return stockExcelList;
    }


    public static void  insertdb(String username, String stockCode, String companyName, java.sql.Date date, String text, Long weiboId) {
        String sql = "insert into hk_ccs_company_weibo_spark(send_name, stock_code, stock_name, send_datetime, content, weibo_id) " +
                "values(?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = getJdbcConnection().prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, stockCode);
            ps.setString(3, companyName);
            ps.setDate(4, date);
            ps.setString(5, text);
            ps.setLong(6, weiboId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("insert db error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    System.out.println("close connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 获取连接
     * @return
     * @throws Exception
     */
    private static Connection getJdbcConnection() throws Exception{
//        String jdbcUrl = "jdbc:mysql://rm-wz9ykd02hk1w8e064o.mysql.rds.aliyuncs.com/wefid?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&serverTimezone=GMT%2b8"; // 外网
        String jdbcUrl = "jdbc:mysql://rm-wz9ykd02hk1w8e064rw.mysql.rds.aliyuncs.com/wefid?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&serverTimezone=GMT%2b8"; // 内网
        //1.加载驱动程序
        Class.forName("com.mysql.jdbc.Driver");
        //2. 获得数据库连接
        Connection conn = DriverManager.getConnection(jdbcUrl, "fid", "fid123");
        return conn;
    }

    public static void main(String[] args) {
        excelhaha();
    }

}
