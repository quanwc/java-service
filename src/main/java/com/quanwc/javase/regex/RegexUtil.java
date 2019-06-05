package com.quanwc.javase.regex;

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

    public static void main(String[] args) {
        test1();
    }
}
