package com.quanwc.javase.lambda.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.quanwc.javase.lambda.model.dto.PhoneDTO;
import com.quanwc.javase.lambda.util.PhoneUtil;

/**
 * @author quanwenchao
 * @date 2018/12/19 22:27:15
 */
public class PhoneService {

	public static void main(String[] args) {

		//String subjectRelatedContentKey = GRedisKeys.getSubjectRelatedContent(subjectId);
		//Set<String> set1 = defaultRedisTemplate.opsForZSet().reverseRangeByScore(
		//		subjectRelatedContentKey, 0, createTimestamp, 0, limit);

		Set<String> set2 = new HashSet<>();

		// 主题下的内容
		List<PhoneDTO> phoneDTOList = PhoneUtil.listSet2PhoneDTOList(set2);
	}

	/**
	 * 排序
	 */
	public static List<PhoneDTO> test1() {

		List<PhoneDTO> phoneDTOList = PhoneUtil.listSet2PhoneDTOList(null);

		// 按照排序号
		List<PhoneDTO> phoneDTOS = phoneDTOList.stream()
				.filter(PhoneDTO::getIsTop)
				.sorted(Comparator.comparing(PhoneDTO::getOrderNum))
				.collect(Collectors.toList());
		return phoneDTOS;
	}

	/**
	 * 排序
	 * @return
	 */
	public static List<PhoneDTO> test2() {

		List<PhoneDTO> phoneDTOList = PhoneUtil.listSet2PhoneDTOList(null);

		// 按照时间戳由新到旧排序
		phoneDTOList
				.sort(Comparator.comparing(phoneDTO -> (-1) * phoneDTO.getCreateTimestamp()));

		return phoneDTOList;
	}

}
