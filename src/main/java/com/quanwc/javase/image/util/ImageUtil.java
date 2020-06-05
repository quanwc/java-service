package com.quanwc.javase.image.util;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by quanwenchao
 * 2018/7/19 20:56:09
 */
@Slf4j
public class ImageUtil {

    /**
     * 解析帖子内容中的图片:
     *  step1：解析<img>标签
     *  step2：找到<src>属性的位置
     *  step3：街区src属性的内容，即就是图片
     * @param content
     * @return 帖子内容中的图片；如果有多个图片，以";"分割
     */
    public static List<String> parsePostContentImage(String content) {

        if (null == content || content.isEmpty()) {
            return null;
        }

        List<String> resultList = new ArrayList<>();

        String[] split = content.split("<img");
        if (null == split) {
            return null;
        }
        for (int i = 0; i < split.length; ++i) {
            String str = split[i];

            Integer index = str.indexOf("src = ");
            if (-1 == index) {
                continue;
            }

            // 判断是否为http协议
//            if (!isHttpProtocol(str)) {
//                continue;
//            }

            // 从index位置开始，找到src的结尾引号
            Integer index2 = str.indexOf("\"", index + 5);
            if (-1 == index2) {
                continue;
            }

            // http://www.gelonghui.com/forum.php?mod=image&amp%3Baid=6014&amp%3Bsize=300x300&amp%3Bkey=76ffe9b83f9f23b5&amp%3Bnocache=yes&amp%3Btype=fixnone
            try {
                String img = str.substring(index + 5, index2);
                String resultImg = img.replaceAll("!wm", ""); // 去掉水印
                resultList.add(splitUriParm(resultImg));
            } catch (Exception e) {
                log.error("occur an error during parse content, split[i]: " + split[i]);
                //log.error(e.getMessage());
            }
        }

        return resultList;
    }


    /**
     * 判断是否是http、https请求
     * @param uri 请求uri  eg：src="ttp:image/png;base64,iVBORw0KGgoAAAAN"
     *            src="data/attachment/portal/201408/07/172635buuwk6yk00dck0ee.jpg"
     * @return
     */
    private static boolean isHttpProtocol(String uri) {

        // 必要判断
        if (null == uri) {
            return false;
        }
        if (!uri.contains("http")) {
            return false;
        }
        if (!uri.contains("src=")) {
            return false;
        }

        Integer index1 = uri.indexOf("src=");
        if (-1 == index1) {
            return false;
        }
        Integer index2 = uri.indexOf(":", index1 + 5); // 从"src="开始，截取第一个冒号
        if (-1 == index2) {
            return false;
        }

        String id = uri.substring(index1 + 5, index2);

        if ("http".equals(id) || "https".equals(id)) {
            return true;
        }

        return false;
    }

    /**
     * 截取uri问号后面的内容
     * @param uri 原uri  eg：src="http://ifanr-cdn.b0.upaiyun.com/wp-content/uploads/2017/07/biange.jpg?|imageMogr2/strip/interlace/1/quality/85/format/jpg
     * @return 截取"?"后的uri
     */
    private static String splitUriParm(String uri) {
        if (null == uri || !uri.contains("?")) {
            return uri;
        }
        return uri.substring(0, uri.indexOf("?"));
    }

    public static void main(String[] args) {
        //String content = "<img src=\"http://ifanr-cdn.b0.upaiyun.com/wp-content/uploads/2017/07/biange.jpg?|imageMogr2/st" +
        //        "rip/interlace/1/quality/85/format/jpg\">" +
        //        "</p><p>这意味着，增资完成后，雷鸟科技将不再是 FFalcon 的控股子";
        //splitUri(content);


        String content = "别人也喊我峰哥<img class=\"emoji\" src = \"//img.t.sinajs.cn/t4/appstyle/expression/ext/normal/4a/2018new_xiaoku_thumb.png\"></img>";

        List<String> strings = parsePostContentImage(content);
        for (String str : strings) {
            System.out.println("str: " + str);
        }
    }

}
