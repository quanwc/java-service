package com.quanwc.javase.regex;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具类
 *
 * @author quanwenchao
 * @date 2019/5/30 09:57:54
 */
public class RegexUtil {

    /**
     * 匹配String字符串中，#中间的文本
     * eg：#中俄建交七十周年#111,#带回你的家#，请把#你的微笑留下……
     * 结果：#中俄建交七十周年#、#带回你的家#
     */
    public static void test1() {
        String regex = "#([^#]+)#";

        String text = "#中俄建交七十周年#111,#带回你的家#，请把#你的微笑留下……";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
//            String group = matcher.group(); // #中俄建交七十周年#、#带回你的家#
            String group = matcher.group(1); // 中俄建交七十周年、带回你的家  (截取正则表达式中，第一个括号中间的内容)
            System.out.println(group);
        }
    }


    // 去除尾部的链接的正则
    private static final String REGEX_URL_CONST = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]$";
    // 去除多余@符号的正则：
    private static final String REGEX_AT_CONST = "(\\s*@[\\w\\u4e00-\\u9fa5-]+){3,}?$";
    /**
     * 微博内容正则匹配：去除多余@符号
     * http://39.108.244.181/zentaopms/www/story-view-604.html
     * @param text 微博内容
     * @return 去除多余@符号后的内容
     */
    public static String weiboTextRegexMatch(String text) {
        if (StringUtils.isEmpty(text)) {
            return "";
        }

        text = text.replaceAll(REGEX_URL_CONST, "");

        text = text.trim().replaceAll("\t", "");
        text = text.trim().replaceAll("\n", "");
        String result = text.replaceAll(REGEX_AT_CONST, "");
        return result;
    }


    public static void main(String[] args) {
        test1();
    }
}
