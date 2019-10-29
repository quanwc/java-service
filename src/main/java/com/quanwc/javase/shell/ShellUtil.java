package com.quanwc.javase.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author quanwenchao
 * @date 2019/6/21 19:19:36
 */
public class ShellUtil {

    /**
     *
     */
    public  void notifyShell() {
        Process process = null;
        BufferedReader br = null;
        try {
            String shellPath = "/usr/local/weixin-service/restart.sh";
            process = Runtime.getRuntime().exec(shellPath.toString());
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String result = sb.toString();
            System.out.println("result: " + result);

            if (process != null) {
                int extValue = process.waitFor(); //返回码 0 表示正常退出 1表示异常退出
                if (0 == extValue) {
                    System.out.println("=============启动脚本-执行完毕！");
                } else {
                    System.out.println("=============启动脚本-执行异常！");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 钉钉通知

        } finally {
            if (process != null) {
                process.destroy();
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
