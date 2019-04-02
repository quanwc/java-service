package com.quanwc.javase.image.constant;

/**
 * Created by quanwenchao
 * 2018/7/19 9:22:21
 */
public class ImageConstant {

    // 每次分页查询数量
    public static final Integer LIMIT_COUNT = 6000;
    // 筛选图片fileSize大于LIMIT_SIZE的图片
    //public static final Integer LIMIT_SIZE = 180000;
    public static final Integer LIMIT_SIZE = 800000;
    // 一个excel存放多少条数据
    public static final Integer EXCEL_MAX_COUNT = 10000;

    // g_cloumne表：ID字段
    public static final String COLUMN_ID = "ID";
    // g_cloumn表：COVER_IMAGE_URL字段
    public static final String COLUMN_COVER_IMAGE_URL = "COVER_IMAGE_URL";

    // g_news表：ID字段
    public static final String NEWS_ID = "ID";
    // g_news表：CONTENT字段
    public static final String NEWS_CONTENT = "CONTENT";


    // 普通文章
    public static final Integer POST_TYPE_1 = 1;
    // 专栏文章
    public static final Integer POST_TYPE_2 = 2;

    // 状态 0:逻辑删除 ; 1:正常;2审阅
    public static final Integer POST_STATUS = 0;


    // article_clone库
    // article表：ID字段
    public static final String ARTICLE_ID = "article_id";
    // article表：content字段
    public static final String ARTICLE_CONTENT = "content";

}
