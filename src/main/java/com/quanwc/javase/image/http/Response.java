package com.quanwc.javase.image.http;

import lombok.Data;

@Data
public class Response {
    private int statusCode;
    private String body;
}
