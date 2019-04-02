package com.quanwc.javase.image.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.quanwc.javase.image.bean.OssArticleDO;

/**
 * @author quanwenchao
 * @date 2018/12/27 16:02:13
 */
@Mapper
public interface OssArticleMapper {

	Integer saveBatch(@Param(value = "ossArticleList") List<OssArticleDO> ossArticleList);
}
