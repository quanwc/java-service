package com.quanwc.javase.dingtalk.util;

import org.apache.commons.httpclient.NameValuePair;

import com.alibaba.fastjson.JSON;
import com.quanwc.javase.dingtalk.entity.TextMessage;
import com.quanwc.javase.http.HttpClient;
import com.quanwc.javase.http.Response;

/**
 * 钉钉util
 * @author quanwenchao
 * @date 2019/5/13 11:42:43
 */
public class DingTalkUtil {
    private static HttpClient httpClient = new HttpClient();

    /**
     * 发送文本消息到钉钉
     *
     * @param webhookUrl
     * @param accessToken
     * @param textMessage
     *            文本消息对象
     * @return
     */
    public static Response sendTextMessage(String webhookUrl, String accessToken, TextMessage textMessage) {
        try {
            Response response = httpClient.post(webhookUrl,
                new NameValuePair[] {new NameValuePair("access_token", accessToken)}, JSON.toJSONString(textMessage));
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
