package com.quanwc.javase.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * httpClient
 * 
 * @author quanwenchao
 * @date 2019/5/12 16:22:45
 */
public class HttpClient {

    private static final String UTF8 = "UTF-8";
    private static final String CONTENT_TYPE = "application/json";

    private org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();

    /**
     * get请求
     * 
     * @param url
     *            请求地址url
     * @param params
     *            参数
     * @return
     */
    public Response get(String url, NameValuePair[] params) {
        Response response = new Response();

        GetMethod method = new GetMethod(url);
        method.setQueryString(params);
        method.getParams().setContentCharset(UTF8);

        try {
            response.setStatusCode(httpClient.executeMethod(method));
            response.setBody(method.getResponseBodyAsString());
            return response;
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            method.releaseConnection();
        }
        return null;
    }

    public Response get1(String url, NameValuePair[] params) {
        Response response = new Response();

        GetMethod method = new GetMethod(url);
        method.setQueryString(params);
        method.getParams().setContentCharset(UTF8);

        try {
            response.setStatusCode(httpClient.executeMethod(method));
             response.setBody(method.getResponseBodyAsString());

            InputStream inputStream = method.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            String str = "";
            while ((str = br.readLine()) != null) {
                stringBuffer.append(str);
            }
            response.setBody(stringBuffer.toString());
            return response;
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            method.releaseConnection();
        }
        return null;
    }

    /**
     * post请求
     * 
     * @param url
     *            请求地址url
     * @param params
     *            参数
     * @param body
     *            body参数
     * @return
     */
    public Response post(String url, NameValuePair[] params, String body) {
        PostMethod method = new PostMethod(url);
        method.setQueryString(params);

        method.getParams().setContentCharset(UTF8);
        try {
            method.setRequestEntity(new StringRequestEntity(body, CONTENT_TYPE, UTF8));
            Response response = new Response();
            response.setStatusCode(httpClient.executeMethod(method));
            response.setBody(method.getResponseBodyAsString());
            return response;
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            method.releaseConnection();
        }
        return null;
    }
}
