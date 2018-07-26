package com.quanwc.javase.uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 调用有道翻译的api接口：
 *      通过java.net.URL类，请求有道翻译的数据接口
 *
 *  有道翻译api官网：http://fanyi.youdao.com/openapi
 *
 * Created by quanwenchao
 * 2018/7/9 22:23:34
 */
public class YoudaoURL {
    public static void main(String[] args) {
        new ReadByGet().start();
    }
}

class ReadByGet extends Thread {

    private static final String FANYI_URI = "http://openapi.youdao.com/api";

    @Override
    public void run() {

        try {

            URL url = new URL(FANYI_URI);
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            InputStreamReader ins = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ins);

            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((br.readLine()) != null) {
                sb.append(line);
            }

            br.close();
            ins.close();
            is.close();

            System.out.println("result: " + sb.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
