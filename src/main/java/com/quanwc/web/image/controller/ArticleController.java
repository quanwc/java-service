package com.quanwc.web.image.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quanwc.javase.image.service.ArticleService;

/**
 * 帖子（含专栏）的controller
 * @author quanwenchao
 * @date 2018/12/27 15:30:13
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

	@Autowired
	private ArticleService articleService;

	/**
	 * 统计总个数
	 * @return
	 */
	@GetMapping(value = "/count")
	public Integer postClone() {
		return articleService.count();
	}

	@GetMapping(value = "singleFakePic")
	public String singleFakePic() {

		//String imgUrl = "http://img3.gelonghui.com/forum/201408/28/134647frgerkf2r2d3drvg.jpg"; // NoSuchKey 404

		//String imgUrl = "https://glhcdn.oss-cn-hangzhou.aliyuncs.com/abc/20181228.png"; // txt转为image上传到阿里云， 400

		String imgUrl = "http://img3.gelonghui.com/201807/p20180729164328517.png"; // 400
		//String imgUrl = "http://www.guuzhang.com/data/attachment/forum/201408/01/094821fkk9to2sor2n4oss.jpg"; // 403
		articleService.singleFakePic(imgUrl);
		return "success";
	}


	/**
	 * 处理帖子的假图片
	 * @return
	 */
	@GetMapping(value = "/dealArticleFakePic")
	public String dealArticleFakePic() {
		System.out.println("begin: " + System.currentTimeMillis() / 1000);
		articleService.dealArticleFakePic();
		System.out.println("end: " + System.currentTimeMillis() / 1000);
		return "dealArticleFakePic success";
	}
}
