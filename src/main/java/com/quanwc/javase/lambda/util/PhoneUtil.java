package com.quanwc.javase.lambda.util;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import com.quanwc.javase.lambda.constant.PhoneConstant;
import com.quanwc.javase.lambda.model.dto.PhoneDTO;
import com.quanwc.javase.lambda.model.entity.PhoneDO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author quanwenchao
 * @date 2018/12/19 22:20:51
 */
@Slf4j
public class PhoneUtil {

	/**
	 * String 转为 PhoneDTO
	 * @param str
	 * @return
	 */
	public static PhoneDTO string2PhoneDTO(String str) {
		try {
			PhoneDTO phoneDTO = JsonUtil.JsonStr2Obj(str, PhoneDTO.class);
			return phoneDTO;
		} catch (IOException e) {
			log.error("str convert json error: " + e);
		}
		return null;
	}

	/**
	 * Set<String> 转为 List<PhoneDTO>
	 * @param subjectReferenceKeySet
	 * @return
	 */
	public static List<PhoneDTO> listSet2PhoneDTOList(
			Set<String> subjectReferenceKeySet) {
		return subjectReferenceKeySet.stream().map(str -> {
			try {
				return JsonUtil.JsonStr2Obj(str, PhoneDTO.class);
			}
			catch (IOException e) {
				log.error("PhoneDTO Get Failed", e);
				return null;
			}
			// 满足条件的才保留
		}).filter(obj -> !Objects.isNull(obj)).collect(Collectors.toList());
	}

	/**
	 * List<PhoneDO> 转为 List<PhoneDTO>
	 * @param paramList
	 * @return
	 */
	public static List<PhoneDO> phoneDTOList2PhoneDOList1(List<PhoneDTO> paramList) {
		if (CollectionUtils.isEmpty(paramList)) {
			return null;
		}
		List<PhoneDO> phoneDOList = paramList.stream().map(PhoneUtil::phoneDTO2PhoneDO)
				.filter(obj -> !Objects.isNull(obj)).collect(Collectors.toList());
		return phoneDOList;
	}

	/**
	 * List<PhoneDO> 转为 List<PhoneDTO>
	 * @param paramList
	 * @return
	 */
	public static List<PhoneDO> phoneDTOList2PhoneDOList2(List<PhoneDTO> paramList) {
		if (CollectionUtils.isEmpty(paramList)) {
			return null;
		}
		List<PhoneDO> resultList = new ArrayList<>(paramList.size());
		paramList.forEach(param -> {
			PhoneDO result = phoneDTO2PhoneDO(param);
			if (null == result) {
				// continue;
				return;
			}
			resultList.add(result);
		});

		return resultList;
	}

	/**
	 * List<PhoneDO> 转为 List<PhoneDTO>
	 * @param paramList
	 * @return
	 */
	public static List<PhoneDO> phoneDTOList2PhoneDOList3(List<PhoneDTO> paramList) {
		if (CollectionUtils.isEmpty(paramList)) {
			return null;
		}

		List<PhoneDO> phoneDOList = paramList.stream().map((phone) -> {
			return phoneDTO2PhoneDO(phone);
		}).filter(obj -> !Objects.isNull(obj)).collect(Collectors.toList());

		return phoneDOList;
	}

	/**
	 * PhoneDTO 转为 PhoneDO
	 * @param param
	 * @return
	 */
	public static PhoneDO phoneDTO2PhoneDO(PhoneDTO param) {
		if (null == param) {
			return null;
		}

		PhoneDO result = new PhoneDO();
		BeanUtils.copyProperties(param, result);
		// result.setId(param.getId());
		// result.setName(param.getName());
		// result.setIsHot(param.getIsHot());
		// result.setIsTop(param.getIsTop());
		// result.setCreateTimestamp(param.getCreateTimestamp());
		// result.setNick(param.getNick());
		// result.setFollowNum(param.getFollowNum());
		// result.setSubjectReferenceNum(param.getSubjectReferenceNum());
		// result.setSubjectReferenceChoiceNum(param.getSubjectReferenceChoiceNum());
		return result;
	}

	public void test() {

		List<PhoneDO> phoneDOList = new ArrayList<>(0);

		// key:用户ID，value:是否置顶
		Map<Integer, Boolean> isTopMap = new HashMap<>();

		if (!CollectionUtils.isEmpty(phoneDOList)) {
			List<Integer> userSubjectIds = phoneDOList.stream()
					.map(PhoneDO::getUserId).collect(Collectors.toList());

			phoneDOList.forEach(obj -> isTopMap.put(obj.getUserId(),
					obj.getIsTop().equals(PhoneConstant.PHONE_IS_TOP)));
		}
	}

}
