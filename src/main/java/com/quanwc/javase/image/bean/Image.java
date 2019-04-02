package com.quanwc.javase.image.bean;

import lombok.Data;

/**
 * Created by quanwenchao
 * 2018/7/17 21:17:05
 */
@Data
public class Image {

    @Data
    public class ImageValue {
        public String value;
    }

    private ImageValue FileSize;
    private ImageValue Format;
    private ImageValue ImageHeight;
    private ImageValue ImageWidth;
    private ImageValue ResolutionUnit;
    private ImageValue XResolution;
    private ImageValue YResolution;

    //private Map<String, String> fileSize;
    //private Map<String, String> format;
    //private Map<String, String> imageHeight;
    //private Map<String, String> imageWidth;
    //private Map<String, String> resolutionUnit;
    //private Map<String, String> xresolution;
    //private Map<String, String> yresolution;
}
