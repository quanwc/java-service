package com.quanwc.javase.image.http;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.springframework.stereotype.Component;

/**
 */
//@Slf4j
@Component
public class HttpClient {

    private final int MAX_CONNETION_PER_HOST = 150;
    private final int CONNETION_TIMEOUT = 30000;
    private final int SOCKET_TIMEOUT = 30000;
    private org.apache.commons.httpclient.HttpClient httpClient;

    @PostConstruct
    private void init() {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = connectionManager.getParams();
        params.setDefaultMaxConnectionsPerHost(MAX_CONNETION_PER_HOST);
        params.setConnectionTimeout(CONNETION_TIMEOUT);
        params.setSoTimeout(SOCKET_TIMEOUT);

        HttpClientParams clientParams = new HttpClientParams();
        clientParams.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        httpClient = new org.apache.commons.httpclient.HttpClient(clientParams, connectionManager);
    }


    /**
     * 请求oss，获取图片属性：
     *      http://image-demo.oss-cn-hangzhou.aliyuncs.com/example.jpg?x-oss-process=image/info
     * @param imageUrl 图片url
     * @return
     */
    public Response doOssGet(String imageUrl) {
        Response response = this.get(imageUrl,
                new NameValuePair[] {
                        new NameValuePair("x-oss-process", "image/info")});
        return response;
    }


    public Response head(String url, NameValuePair[] params) {
        HeadMethod method = new HeadMethod(url);
        method.setQueryString(params);
        method.getParams().setContentCharset("UTF-8"); // 编码格式

        try {
            Response response = new Response();
            response.setStatusCode(httpClient.executeMethod(method));
            response.setBody(method.getResponseBodyAsString());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        return null;

    }


    public Response get(String url, NameValuePair[] params) {
        GetMethod method = new GetMethod(url);
        method.setQueryString(params);
        method.getParams().setContentCharset("UTF-8"); // 编码格式

        try {
            Response response = new Response();
            response.setStatusCode(httpClient.executeMethod(method));
            response.setBody(method.getResponseBodyAsString());
            return response;
        } catch (Exception e) {
            //log.error(toLogString(url, params));
            //log.error(e.getMessage());
        } finally {
            method.releaseConnection();
        }
        return null;
    }

    public Response post(String url, NameValuePair[] params, String body) {
        PostMethod method = new PostMethod(url);
        method.setQueryString(params);
        try {
            method.setRequestEntity(new StringRequestEntity(body, "application/json", "UTF-8"));
            method.getParams().setContentCharset("UTF-8");
            Response response = new Response();
            response.setStatusCode(httpClient.executeMethod(method));
//            method.getParams().setContentCharset("UTF-8");
            response.setBody(method.getResponseBodyAsString());
            return response;
        } catch (Exception e) {
            //log.error(toLogString(url, params));
            //log.error(e.getMessage());
        } finally {
            method.releaseConnection();
        }
        return null;
    }

    private String toLogString(String url, NameValuePair[] params) {
        StringBuffer sb = new StringBuffer();
        sb.append("Weixin HTTP GET URL: ");
        sb.append(url);
        sb.append(" Paramters: ");
        for (NameValuePair nameValuePair : params) {
            sb.append(nameValuePair.toString());
        }
        return sb.toString();
    }
}
