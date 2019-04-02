package com.quanwc.javase.image.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.quanwc.javase.image.bean.Image;
import com.quanwc.javase.image.bean.OssArticleDO;
import com.quanwc.javase.image.constant.ImageConstant;
import com.quanwc.javase.image.http.HttpClient;
import com.quanwc.javase.image.http.Response;
import com.quanwc.javase.image.mapper.ArticleMapper;
import com.quanwc.javase.image.util.ImageUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 帖子（含专栏）的service
 * @author quanwenchao
 * @date 2018/12/27 15:32:15
 */
@Slf4j
@Service
public class ArticleService {

	@Autowired
	private ArticleMapper articleMapper;
	@Autowired
	private OssArticleService ossArticleService;
	@Autowired
	private HttpClient httpClient;

	private static final List<String> illegalList = new ArrayList<>(); // oss响应为null的图片

	public Integer count() {
		Integer totalCount = articleMapper.count();
		log.info("统计帖子数量: totalCount={}", totalCount);
		return totalCount;
	}

	/**
	 * 处理帖子有关的图片
	 */
	public void dealArticleFakePic() {
		Integer maxValue = articleMapper.count();

		for (int step = 0; step < maxValue; step += ImageConstant.LIMIT_COUNT) {
			List<Map> articleListFromDB = this.listArticle(step,
					ImageConstant.LIMIT_COUNT);
			List<OssArticleDO> ossArticleDOList = new ArrayList<>(); // 批量处理
			for (Map map : articleListFromDB) {

				Integer articleId = (Integer) map.get(ImageConstant.ARTICLE_ID);
				String content = (String) map.get(ImageConstant.ARTICLE_CONTENT);

				// 过滤假图片
				List<OssArticleDO> fakeImageList = doContentFakeImage(articleId, content);
				if (CollectionUtils.isEmpty(fakeImageList)) {
					continue;
				}
				//for (OssArticleDO obj : ossPostDOList) {
				//	ossArticleDOList.add(obj);
				//}
				ossArticleDOList.addAll(fakeImageList);
			}

			// 批量新增假图片
			ossArticleService.saveBatch(ossArticleDOList);
		}

	}

	/**
	 * 分页查询帖子列表
	 * @return
	 */
	public List<Map> listArticle(Integer start, Integer count) {
		if (null == start || null == count) {
			return null;
		}
		return articleMapper.listArticle(start, count);
	}

	/**
	 * 处理帖子的内容图片
	 * @param articleId 文章ID
	 * @param content 帖子内容
	 * @return 帖子内容中图片，对应的OssArticleDO对象集合
	 */
	public List<OssArticleDO> doContentFakeImage(Integer articleId, String content) {
		if (null == content || null == articleId) {
			return null;
		}

		List<String> images = ImageUtil.parsePostContentImage(content); // 解析内容中的图片
		if (CollectionUtils.isEmpty(images)) {
			return null;
		}

		List<OssArticleDO> resultList = new ArrayList<>();

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY));

		for (String imgUrl : images) {
			if (imgUrl.contains("oss") || imgUrl.contains("gelonghui.com")) { // 忽略第三方图片

				Response response = null;
				try {
					response = httpClient.doOssGet(imgUrl);
				}
				catch (Exception e) {
					log.error("1============articleId: " + articleId);
					log.error("2============content occur error: ");
					log.error("3============imgUrl: " + imgUrl);
				}

				// 必要过滤
				if (null == response || null == response.getBody()) {
					illegalList.add("article content oss response null: articleId="
							+ articleId + ", imgUrl=" + imgUrl);
					continue;
				}
				
				int statusCode = response.getStatusCode();
				if (404 == statusCode) {
					log.error("article oss response 404, imgUrl: " + imgUrl);
					continue;
				}
				if (statusCode != 200) {
					OssArticleDO ossPostDO = initOssArticleDO(articleId, imgUrl, statusCode);
					if (null != ossPostDO) {
						resultList.add(ossPostDO);
					}
					log.error("article oss response " + statusCode + ", imgUrl: " + imgUrl);
					continue;
				}
				
				try {
					Image image = objectMapper.readValue(response.getBody(), Image.class);
					Integer width = Integer.valueOf(image.getImageWidth().getValue());
					//boolean isImageFormat = "jpg".equals(format) || "png".equals(format)
					//		|| "jpeg".equals(format) || "bmp".equals(format) || "gif".equals(format) || "webp".equals(format);

					if (null == width || width <= 0) {
						// 不是图片格式的收集处理
						OssArticleDO ossPostDO = initOssArticleDO(articleId, imgUrl, statusCode);
						if (null != ossPostDO) {
							resultList.add(ossPostDO);
						}
					}
				}
				catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
		return resultList;
	}


	public void singleFakePic(String imgUrl) {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY));

		List<OssArticleDO> resultList = new ArrayList<>();

		Integer articleId = 1001;

		if (imgUrl.contains("oss") || imgUrl.contains("gelonghui.com")) { // 忽略第三方图片

			Response response = null;
			try {
				response = httpClient.doOssGet(imgUrl);
				System.out.println("response: " + response);
			}
			catch (Exception e) {
				log.error("1============articleId: " + articleId);
				log.error("3============imgUrl: " + imgUrl);
			}

			// 必要过滤
			if (null == response || null == response.getBody()) {
				illegalList.add("article content oss response null: articleId="
						+ articleId + ", imgUrl=" + imgUrl);
				return;
			}
			if (response.getBody().contains("NoSuchKey")) {
				log.error("article oss response NoSuchKey, imgUrl: " + imgUrl);
				return;
			}

			int statusCode = response.getStatusCode();
			if (200 == statusCode) {
				try {
					Image image = objectMapper.readValue(response.getBody(), Image.class);
					String format = image.getFormat().getValue();
					boolean isImageFormat = "jpg".equals(format) || "png".equals(format)
							|| "jpeg".equals(format) || "gif".equals(format);
					if (!isImageFormat) {
						// 不是图片格式的收集处理
						OssArticleDO ossPostDO = initOssArticleDO(articleId, imgUrl, statusCode);
						if (null != ossPostDO) {
							resultList.add(ossPostDO);
						}
					}
				}
				catch (IOException e) {
					log.error(e.getMessage());
				}
			}

		}
	}


	/**
	 * 初始化OssArticleDO对象
	 * @param articleId 图片所属的帖子id
	 * @param fakeImgUrl 图片的url
	 * @return OssArticleDO对象
	 */
	public OssArticleDO initOssArticleDO(Integer articleId, String fakeImgUrl, Integer statusCode) {
		if (null == articleId || null == fakeImgUrl) {
			return null;
		}
		OssArticleDO articleDO = new OssArticleDO();
		articleDO.setArticleId(articleId);
		articleDO.setFakeImageUrl(fakeImgUrl);
		articleDO.setStatusCode(statusCode);
		return articleDO;
	}
}
