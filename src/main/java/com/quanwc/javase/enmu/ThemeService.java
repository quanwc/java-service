package com.quanwc.javase.enmu;

import lombok.extern.slf4j.Slf4j;

/**
 * 将变量转为枚举类型
 * @author quanwenchao
 * @date 2018/12/10 17:11:20
 */
@Slf4j
public class ThemeService {



	public static void main(String[] args) {

		// 将name，获取指定的枚举类型
		String source1 = "POST";
		SourceTypeEnum sourceTypeEnum1 = SourceTypeEnum.valueOf(source1);
		System.out.println(sourceTypeEnum1);

		// 根据type，获取指定的枚举类型
		Integer source2 = 3;
		SourceTypeEnum sourceTypeEnum2 = SourceTypeEnum.getSourceType(source2);
		System.out.println(sourceTypeEnum2);


		Integer source = 3;
		SourceTypeEnum sourceTypeEnum = SourceTypeEnum.getSourceType(source);
		switch (sourceTypeEnum) {
			case POST:
				log.info("post deal");
				//postIds.add(refId);
				break;
			case NEWS:
				log.info("news deal");
				//postIds.add(refId);
				break;
			case LIVE:
				log.info("live deal");
				//liveIds.add(refId);
		}
	}

}
