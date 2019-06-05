package com.quanwc.javase.dingtalk.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * DingTalk配置类
 * @author quanwenchao
 * @date 2019/5/13 09:44:09
 */
@Data
@Component
@ConfigurationProperties(prefix = "dingtalk")
public class DingTalkConfig {
    private String webhookUrl;
    private String accessToken;
    private List<String> atMobiles;
}
