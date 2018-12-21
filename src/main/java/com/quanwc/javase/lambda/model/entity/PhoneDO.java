package com.quanwc.javase.lambda.model.entity;

import lombok.Data;

/**
 * @author quanwenchao
 * @date 2018/12/19 22:19:17
 */
@Data
public class PhoneDO {

	/** 主键id */
	private Integer id;

	/** 用户id */
	private Integer userId;

	/** 来源 */
	private Integer source;

	/** 关联ID */
	private Integer refId;

	/** 是否置顶 */
	private Boolean isTop;

	/** 排序号 */
	private Integer orderNum;

	/** 创建时间 */
	private Long createTimestamp;
}
