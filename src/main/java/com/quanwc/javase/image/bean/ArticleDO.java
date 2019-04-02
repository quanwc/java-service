package com.quanwc.javase.image.bean;

import lombok.Data;

/**
 * 帖子（含专栏）的do
 * @author quanwenchao
 * @date 2018/12/27 15:15:05
 */
@Data
public class ArticleDO {

	/**
	 * 主键id
	 */
	private Integer id;

	/**
	 * 帖子id
	 */
	private Integer articleId;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 内容
	 */
	private String content;

	/**
	 * 创建时间
	 */
	private Long createdTimestamp;

}
