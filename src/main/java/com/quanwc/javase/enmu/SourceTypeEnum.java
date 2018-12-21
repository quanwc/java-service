package com.quanwc.javase.enmu;

import java.util.Arrays;

/**
 * 分组来源（source）
 */
public enum SourceTypeEnum {
	/**
	 * 帖子
	 */
	POST(3, "帖子"),
	/**
	 * 直播
	 */
	LIVE(4, "直播"),

	/**
	 * 要闻/事件
	 */
	NEWS(5, "事件"),

	TOPIC(7, "话题");

	/**
	 * 分组来源类型
	 */
	private int type;

	private String typeName;

	SourceTypeEnum(int type, String typeName) {
		this.type = type;
		this.typeName = typeName;
	}

	public int getType() {
		return type;
	}

	public String getTypeName() {
		return typeName;
	}

	/**
	 * 根据type，获取指定的枚举类型
	 * @param type
	 * @return
	 */
	public static SourceTypeEnum getSourceType(int type) {
		return Arrays.stream(SourceTypeEnum.values()).filter(e -> e.getType() == type)
				.findFirst().orElse(null);
	}
}
