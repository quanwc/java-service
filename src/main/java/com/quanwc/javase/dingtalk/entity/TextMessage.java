package com.quanwc.javase.dingtalk.entity;

import java.util.HashMap;

import lombok.Data;

/**
 * @author quanwenchao
 * @date 2019/5/12 16:09:29
 */
@Data
public class TextMessage {

    /**
     * 消息类型
     */
    private String msgtype;

    /**
     * Text字段
     */
    private Text text;

    /**
     * At字段
     */
    private At at;



    public static void main(String[] args) {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("msgtype", "text");

        HashMap<String, String> textItems = new HashMap<>();
        textItems.put("content", "aaaaa");
        resultMap.put("text", textItems);

        HashMap<String, Object> atItems = new HashMap<>();
        atItems.put("atMobiles", null);
        atItems.put("isAtAll", false);
        resultMap.put("at", atItems);

        System.out.println(resultMap.toString());
    }
}
