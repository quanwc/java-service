package com.quanwc.javase.lambda.model.dto;

import lombok.Data;

/**
 * @author quanwenchao
 * @date 2018/12/19 22:20:18
 */
@Data
public class PhoneDTO {

	/** 主键id */
	private Integer id;

	/** 主题id */
	private Integer subjectId;

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
