package com.quanwc.javase.http;

import lombok.Data;

/**
 * response
 * @author quanwenchao
 * @date 2019/5/12 16:22:51
 */
@Data
public class Response {
    /**
     * 响应码
     */
    private Integer statusCode;

    /**
     * 响应结果
     */
    private String body;
}
