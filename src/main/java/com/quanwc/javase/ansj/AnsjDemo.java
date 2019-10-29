package com.quanwc.javase.ansj;

import org.ansj.library.DicLibrary;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;

/**
 * ansj分词：http://nlpchina.github.io/ansj_seg/
 * @author quanwenchao
 * @date 2019/6/6 23:39:01
 */
public class AnsjDemo {

    /**
     * parse()
     */
    public static void test() {
        String str = "让战士们过一个欢乐祥和的新春佳节。" ;
        System.out.println(ToAnalysis.parse(str));
        //        让/v,战士/n,们/k,过/ug,一个/m,欢乐/a,祥和/a,的/uj,新春/t,佳节/n,。/w
    }

    /**
     * parse(text).toStringWithOutNature("/")  以','分割
     */
    public static void test2() {
        String text = "让战士们过一个欢乐祥和的新春佳节。" ;
        String result = ToAnalysis.parse(text).toStringWithOutNature(",");
        System.out.println(result);
        //      让,战士,们,过,一个,欢乐,祥和,的,新春,佳节,。
    }


    /**
     * 单个添加词典：DicLibrary.insert()，UserDefineLibrary.insertWord()方法已废弃
     */
    public static void test3() {
        String text = "苏州高新区在盖楼，北京东方举报格力电器000651.SZ";
        String str1 = ToAnalysis.parse(text).toString("/");
        System.out.println("str1: " + str1);

        DicLibrary.insert(DicLibrary.DEFAULT, "苏州高新");
        String str2 = ToAnalysis.parse(text).toString("/");
        System.out.println("str2: "   + str2);

        DicLibrary.insert(DicLibrary.DEFAULT, "京东方"); //   如果相邻的词黏结后正好为自定义词典中的词，则可以被分词——实现的。换句话说，如果自定义的词未能完全覆盖相邻词，则不能被分词
        String str3 = ToAnalysis.parse(text).toString("/");
        System.out.println("str3: "   + str3);
    }

    public static void test4() {
        StopRecognition stopRecognition = new StopRecognition();
        String text = "";
        ToAnalysis.parse(text).recognition(stopRecognition);

        DicLibrary.insert("", "苏州高新");

    }


    public static void main(String[] args) {
        test3();
    }
}
