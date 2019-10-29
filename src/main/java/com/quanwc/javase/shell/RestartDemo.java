package com.quanwc.javase.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author quanwenchao
 * @date 2019/6/21 12:43:25
 */
public class RestartDemo {
    public static void main(String[] args) {

        Process  process = null;
        BufferedReader br= null;
        try {
            String shellPath= "/home/tomcat_ws/bin/start_tomcatws.sh";
            process = Runtime.getRuntime().exec(shellPath.toString());
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String result = sb.toString();



        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
