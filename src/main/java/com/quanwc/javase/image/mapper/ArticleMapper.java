package com.quanwc.javase.image.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 帖子（含专栏）的mapper
 * @author quanwenchao
 * @date 2018/12/27 15:28:11
 */
@Mapper
public interface ArticleMapper {

	/**
	 * 统计个数
	 * @return
	 */
	@Select("select count(*) from article")
	Integer count();

	/**
	 *
	 * @param start
	 * @param count
	 * @return
	 */
	List<Map> listArticle(@Param("start") Integer start, @Param("count") Integer count);

}
