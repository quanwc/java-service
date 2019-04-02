package com.quanwc.javase.image.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quanwc.javase.image.bean.OssArticleDO;
import com.quanwc.javase.image.mapper.OssArticleMapper;

/**
 * @author quanwenchao
 * @date 2018/12/27 16:01:21
 */
@Service
public class OssArticleService {

	@Autowired
	private OssArticleMapper ossArticleMapper;

	/**
	 * 批量新增
	 * @param ossPostList
	 * @return
	 */
	public int saveBatch(List<OssArticleDO> ossPostList) {
		if (null == ossPostList || ossPostList.isEmpty()) {
			return -1;
		}
		return ossArticleMapper.saveBatch(ossPostList);
	}
}
