package com.quanwc.javase.shell;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * 执行远程服务器上的shell脚本
 * https://www.cnblogs.com/zjrodger/p/5551385.html
 * https://www.shuzhiduo.com/A/GBJrNDbqJ0/
 * @author quanwenchao
 * @date 2020/4/13 15:03:37
 */
@Slf4j
@Data
public class RemoteExecuteCommandUtil {
    // 字符编码默认是utf-8
    private static String DEFAULTCHART = "UTF-8";
    private static Connection conn;
    private String ip;
    private String userName;
    private String userPwd;

    public RemoteExecuteCommandUtil(String ip, String userName, String userPwd) {
        this.ip = ip;
        this.userName = userName;
        this.userPwd = userPwd;
    }

    public RemoteExecuteCommandUtil() {

    }

    /**
     * 远程登录linux的主机
     * 
     * @author Ickes
     * @since V0.1
     * @return 登录成功返回true，否则返回false
     */
    public Boolean login() {
        boolean flg = false;
        try {
            conn = new Connection(ip);
            conn.connect();// 连接
            flg = conn.authenticateWithPassword(userName, userPwd);// 认证
            if (flg) {
                log.info("登录服务器认证成功！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flg;
    }

    /**
     * @author Ickes 远程执行shll脚本或者命令
     * @param cmd
     *            即将执行的命令
     * @return 命令执行成功后返回的结果值，如果命令执行失败，返回空字符串，不是null
     * @since V0.1
     */
    public String executeSuccess(String cmd) {
        String result = "";
        try {
            if (login()) {
                Session session = conn.openSession();// 打开一个会话
                session.execCommand(cmd);// 执行命令
//                result = processStdout(session.getStdout(), DEFAULTCHART);
                conn.close();
                session.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 解析脚本执行返回的结果集
     * 
     * @author Ickes
     * @param in
     *            输入流对象
     * @param charset
     *            编码
     * @since V0.1
     * @return 以纯文本的格式返回
     */
    public static String processStdout(InputStream in, String charset) {
        InputStream stdout = new StreamGobbler(in);
        StringBuffer buffer = new StringBuffer();;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout, charset));
            String line = null;
            while ((line = br.readLine()) != null) {
                buffer.append(line + "\n");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    /**
     * 执行day30热议关键词的脚本
     * @throws Exception
     */
    public static void executeDay30HotKeywordShell(){
        RemoteExecuteCommandUtil executor = new RemoteExecuteCommandUtil("127.0.0.1", "username", "pwd");
        executor.executeSuccess("/usr/local/media-scala/submit_RComputeCompanyHotKeywordDay30.sh");
    }

    public static void main(String[] args) {
        executeDay30HotKeywordShell();
    }

}
