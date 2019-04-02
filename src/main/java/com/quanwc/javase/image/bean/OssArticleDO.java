package com.quanwc.javase.image.bean;

import lombok.Data;

/**
 * @author quanwenchao
 * @date 2018/12/27 15:58:12
 */
@Data
public class OssArticleDO {
	private Integer id;
	private Integer articleId;
	private String fakeImageUrl;
	private Integer statusCode;
}
