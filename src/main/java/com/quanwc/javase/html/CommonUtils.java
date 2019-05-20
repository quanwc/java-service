package com.quanwc.javase.html;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

//import net.sourceforge.pinyin4j.PinyinHelper;
//import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
//import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
//import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
//import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 项目通用的工具类
 *
 * @author hebo2
 *
 */
@Slf4j
public class CommonUtils {

	private static final String regExHtml = "<[^>]+>"; // HTML标签的正则表达式

	private static final String regExHtmlScript = "<script(.*?)>(.*?)</script>|<script(.*?)>"; // HTML的script标签的正则表达式

	private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]+$");

	private static final Pattern EMAIL_PATTERN = Pattern
			.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");

	/**
	 * 取len位随机数
	 * @param len 要返回随机数位数 ，支持1-10位，小于1或大于10返回null
	 * @return
	 */
	public static Long getRandom(int len) {

		if (len < 1 || len > 10) {
			return null;
		}

		long base = 1;
		for (int i = 0; i < len; i++) {
			base *= 10;
		}

		long random = Math.round(Math.random() * base);
		if (len == 1) {
			return random;
		}

		while (random < base / 10 || random > base - 1) // 保险，小概率
		{
			random = Math.round(Math.random() * base);
		}

		return random;
	}

	public static int getRandom(int min, int max) {
		Random random = new Random();

		return random.nextInt(max) % (max - min + 1) + min;
	}

	/**
	 * @deprecated 改为TimeUtil中的dateFormat 日期格式化
	 *
	 * @param sFormat (如果传入null，使用默认格式：yyyy-MM-dd HH:mm:ss)
	 * @return
	 */
	public static String dateFormat(Date date, String sFormat) {

		if (sFormat == null) {
			sFormat = "yyyy-MM-dd HH:mm:ss";
		}

		java.text.DateFormat format = new SimpleDateFormat(sFormat);
		return format.format(date);
	}

	// wujieming add begin
	/**
	 * 字符串日期转化为日期并指定格式
	 *
	 * @param sFormat (如果传入null，使用默认格式：yyyy-MM-dd HH:mm:ss)
	 * @return
	 * @throws ParseException
	 */
	public static Date dateStr2Date(String dateStr, String sFormat)
			throws ParseException {

		if (sFormat == null) {
			sFormat = "yyyy-MM-dd HH:mm:ss";
		}

		java.text.DateFormat format = new SimpleDateFormat(sFormat);

		return format.parse(dateStr);
	}
	// wujieming add end

	/**
	 * @deprecated 改为TimeUtil中getStartTimestamp 得到指定日期当天的开始时间戳 如果转化有异常返回-1 例:传入"2015-5-20"
	 * ,得到1432051200
	 * @param dateStr
	 * @return
	 */
	public static long getStartTimestamp(String dateStr) {
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		try {
			date = s.parse(dateStr);
		}
		catch (ParseException e) {
			e.printStackTrace();
			return -1;
		}
		long startTime = date.getTime() / 1000;
		return startTime;
	}

	/**
	 * @deprecated 改为TimeUtil中getStartTimestamp 获得当天开始的时间戳
	 * @return
	 */
	public static long getStartTimestamp() {
		Date date = new Date();
		String dateStr = dateFormat(date, "yyyy-MM-dd");
		long timestamp = getStartTimestamp(dateStr);
		return timestamp;
	}

	/**
	 * 获得指定日期当天的结束的时间戳 例如:传入"2015-5-20",得到1432137600
	 * @param dateStr
	 * @return
	 */
	public static long getEndTimestamp(String dateStr) {
		long startTime = getStartTimestamp(dateStr);
		return startTime + (60 * 60 * 24);
	}

	/**
	 * 获得指定日期当天的结束的时间戳
	 * @param timestamp
	 * @return
	 */
	public static long getEndTimestamp(long timestamp) {

		String dateStr = formatTimeSecs(timestamp, "yyyy-MM-dd");
		long endTimesatmp = getEndTimestamp(dateStr);
		return endTimesatmp;
	}

	/**
	 * 格式化时间秒
	 *
	 * @param secs 秒数
	 * @param format (如果传入null，使用默认格式：yyyy-MM-dd HH:mm:ss)
	 * @return
	 */
	public final static String formatTimeSecs(long secs, String format) {

		Date d = timeSecs2Date(secs);

		return dateFormat(d, format);
	}

	/**
	 * time秒转Date对象
	 *
	 * @param timeSecs（注意：单位秒）
	 * @return
	 */
	public static Date timeSecs2Date(long timeSecs) {
		return new Date(timeSecs * 1000);
	}

	/**
	 * 过滤html标签
	 *
	 * @param html string
	 * @return plain string
	 */
	public static String delHtmlTag(String html) {

		if (StringUtils.isBlank(html)) {
			return html;
		}
		Pattern p_html = Pattern.compile(regExHtml, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(html);

		return m_html.replaceAll(""); // 过滤html标签
	}

	public static String delHtmlTagWithoutTags(String html,String... tags) {

		if (StringUtils.isBlank(html)) {
			return null;
		}
		if (ArrayUtils.isEmpty(tags)) {
			throw new IllegalArgumentException("Illegal tags");
		}
		for (String tag : tags) {
			// 把<>替换成[]避免被删掉
			String tagWithoutBrace = tag.replaceAll("<", "").replaceAll(">", "");
			html = html.replaceAll(tag, "[" + tagWithoutBrace + "]");
		}
		Pattern pattern = Pattern.compile(regExHtml, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(html);
		html = matcher.replaceAll("");

		for (String tag : tags) {
			// 把[]替换回<>
			String tagWithoutBrace = tag.replaceAll("<", "").replaceAll(">", "");
			html = html.replaceAll("\\[" + tagWithoutBrace + "]", tag);
		}
		return html;
	}

	/**
	 * 取文件后缀名
	 *
	 * @param filename
	 * @return
	 */
	public static String getFilenamePostfix(String filename) {
		if (StringUtils.isBlank(filename)) {
			return "";
		}

		int index = filename.lastIndexOf(".");
		if (index < 0) {
			return "";
		}
		return filename.substring(index + 1);
	}

	/**
	 * 从HttpServletRequest中获取用户IP地址
	 *
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ipAddress = null;
		// ipAddress = this.getRequest().getRemoteAddr();
		ipAddress = request.getHeader("x-forwarded-for");
		if (ipAddress == null || ipAddress.length() == 0
				|| "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			if (ipAddress.equals("127.0.0.1")) {
				// 根据网卡取本机配置的IP
				InetAddress inet = null;
				try {
					inet = InetAddress.getLocalHost();
				}
				catch (UnknownHostException e) {
					e.printStackTrace();
				}
				ipAddress = inet.getHostAddress();
			}

		}

		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
															// = 15
			if (ipAddress.indexOf(",") > 0) {
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
			}
		}
		return ipAddress;
	}

	/**
	 * String分割为List<String>
	 *
	 * @param str
	 * @param split --注意，是正则表达式， .分隔时传 \\.
	 * @return
	 */
	public static List<String> splitString2List(String str, String split) {
		List<String> strList = new Vector<String>();

		if (str == null) {
			return strList;
		}

		String[] strTmp;

		if (split != null) {
			strTmp = str.split(split);
		}
		else {
			strTmp = str.split(",");
		}

		for (String s : strTmp) {
			if (s.trim().length() > 0)// 忽略空值
			{
				strList.add(s);
			}
		}

		return strList;
	}

//	/**
//	 * 取中文字符串拼音首字母
//	 *
//	 * @param chinese 中文+英文字符串
//	 * @return 拼音
//	 */
//	public static String toPYSpell(String chinese) throws BusinessException {
//		StringBuffer pyStr = new StringBuffer();
//		char[] arr = chinese.toCharArray();
//
//		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
//		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
//		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//
//		for (char curchar : arr) {
//			if (curchar > 128) {
//				try {
//					String[] temp = PinyinHelper.toHanyuPinyinStringArray(curchar,
//							defaultFormat);
//					if (temp != null) {
//						pyStr.append(temp[0].charAt(0));
//					}
//				}
//				catch (BadHanyuPinyinOutputFormatCombination e) {
//					e.printStackTrace();
//					throw new BusinessException(
//							"Get " + chinese + " pinyin string exception.");
//				}
//			}
//			else {
//				pyStr.append(curchar);
//			}
//		}
//
//		return pyStr.toString().replaceAll("\\W", "").trim().toLowerCase();
//	}

	/**
	 * 大数显示转换
	 * @param num
	 * @return
	 */
	public static String getBigNumDesc(String num) {
		try {
			DecimalFormat df0 = new DecimalFormat("######0");
			DecimalFormat df1 = new DecimalFormat("######0.0");
			DecimalFormat df2 = new DecimalFormat("######0.00");
			Double dnum = Double.parseDouble(num);

			if (dnum > 10000000000d) {
				dnum = dnum / 100000000d;
				return df0.format(dnum) + "亿";
			}
			else if (dnum > 100000000d) {
				dnum = dnum / 100000000d;
				return df2.format(dnum) + "亿";
			}
			else if (dnum > 1000000d) {
				dnum = dnum / 10000d;
				return df0.format(dnum) + "万";
			}
			else if (dnum > 10000d) {
				dnum = dnum / 10000d;
				return df2.format(dnum) + "万";
			}
			else {
				return num;
			}
		}
		catch (Exception e) {
			// 有异常原文返回
			return num;
		}
	}

	public static int compareVer(String v1, String v2) {
		String[] v1L = v1.split("\\.");
		String[] v2L = v2.split("\\.");

		// minSize为较短的列表长度
		int minSize = v1L.length;
		if (v1L.length > v2L.length) {
			minSize = v2L.length;
		}

		for (int i = 0; i < minSize; i++) {
			if (Integer.parseInt(v1L[i]) != Integer.parseInt(v2L[i])) {
				return Integer.parseInt(v1L[i]) - Integer.parseInt(v2L[i]);
			}
		}

		return v1L.length - v2L.length;
	}

	public static String md5Encode(String plainText) {
		if(StringUtils.isBlank(plainText)){
			return "";
		}
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();

			int i;
			StringBuffer buf = new StringBuffer("");
			for (byte aB : b) {
				i = aB;
				if (i < 0) {
					i += 256;
				}
				if (i < 16) {
					buf.append("0");
				}
				buf.append(Integer.toHexString(i));
			}
			return buf.toString();
		}
		catch (NoSuchAlgorithmException e) {
			log.error("mdf加密失败",e);
			return "";
		}
	}

	/**
	 * 判断注册来源
	 * @author Charles.xiao
	 * @param agent
	 * @return
	 */
	public static String checkFrom(String agent) {
		String value = "other";
		List<String> web = Arrays.asList("Chrome", "Firefox", "Safari", "IE", "QQBrowser",
				"Maxthon", "GreenBrowser", "360SE", "Opera");
		List<String> ios = Arrays.asList("iphone", "ipad", "iOS", "ios", "iPad");
		List<String> android = Arrays.asList("android", "Android", "ANDROID");
		for (String string : android) {
			if (regex(string, agent)) {
				return "ANDROID";
			}
		}
		for (String string : ios) {
			if (regex(string, agent)) {
				return "IOS";
			}
		}
		for (String string : web) {
			if (regex(string, agent)) {
				return "WEB";
			}
		}
		return value;
	}

	/**
	 * 用户端来源查找
	 * @author Charles.xiao
	 * @param regex
	 * @param str
	 * @return
	 */
	private static boolean regex(String regex, String str) {
		Pattern p = Pattern.compile(regex, Pattern.MULTILINE);
		Matcher m = p.matcher(str);
		return m.find();
	}

	/**
	 * 替换html中的特殊字符
	 * @param str
	 * @return
	 */
	public static String htmlEncode(String str) {
		str = str.replaceAll(">", "&gt;");
		str = str.replaceAll("<", "&lt;");
		str = str.replaceAll(" ", "&nbsp;");
		str = str.replaceAll(" ", "&nbsp;");
		str = str.replaceAll("\"", "&quot;");
		str = str.replaceAll("\'", "'");
		str = str.replaceAll("\n", "<br/> ");

		return str;
	}

	/**
	 * 恢复html中的特殊字符
	 * @param str
	 * @return
	 */
	public static String htmlDecode(String str) {
		str = str.replaceAll("&gt;", ">");
		str = str.replaceAll("&lt;", "<");
		str = str.replaceAll("&nbsp;", " ");
		str = str.replaceAll("&quot;", "\"");
		str = str.replaceAll("'", "\'");
		str = str.replaceAll("<br/> ", "\n");
		str = str.replace("&amp;", "&");

		return str;
	}

	/**
	 * 将一个大的列表分割成指定个数的列表
	 * @param <T>
	 * @param <T>
	 * @param limit 一个列表的大小
	 * @param list 将要分割的大列表
	 * @return 列表数组
	 */
	public static <T> List<List<T>> subList(int limit, List<T> list) {
		if (list == null || list.size() == 0) {
			return null;
		}

		int count = -1;
		int model = list.size() % limit;
		List<List<T>> result = new ArrayList<List<T>>();

		if (model == 0) {
			count = list.size() / limit;

			for (int i = 0; i < count; i++) {
				List<T> subList = list.subList(i * limit, limit * (i + 1));
				result.add(subList);
			}

		}
		else {
			count = list.size() / limit + 1;

			for (int i = 0; i < count; i++) {

				if (i == count - 1) {
					List<T> subList = list.subList(i * limit, limit * i + model);
					result.add(subList);
				}
				else {
					List<T> subList = list.subList(i * limit, limit * (i + 1));
					result.add(subList);
				}
			}
		}

		return result;
	}

	/**
	 * 取摘要，去html标签
	 * @param content
	 * @param length
	 * @return
	 */
	public static String getContentSummary(String content, Integer length) {

		if (content == null) {
			return "";
		}

		String summary = CommonUtils.delHtmlTag(content);

		// 截取帖子摘要
		if (summary.length() >= length) {
			return summary.substring(0, length);
		}
		else {
			return summary;
		}
	}

	/**
	 * 打乱列表
	 * @param sourceList
	 * @return
	 */
	public static <V> List<V> randomList(List<V> sourceList) {

		if (sourceList == null || sourceList.size() == 0) {
			return sourceList;
		}

		ArrayList randomList = new ArrayList<V>(sourceList.size());
		do {
			int index = Math.abs(new Random().nextInt(sourceList.size()));
			randomList.add(sourceList.remove(index));
		}
		while (sourceList.size() > 0);

		return randomList;
	}

	// wujieming add begin
	/**
	 * 检验是否都是数字
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {

		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);

		return isNum.matches();
	}

	/**
	 * 检测是否含有中文
	 * @param str
	 * @return
	 */
	public static boolean isChinese(String str) {
		boolean temp = false;
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			temp = true;
		}
		return temp;
	}
	// wujieming add end

	/**
	 * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
	 * @author linhongbin
	 * @param encryptText 被签名的字符串
	 * @param encryptKey 密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey)
			throws Exception {
		String MAC_NAME = "HmacSHA1";
		byte[] data = encryptKey.getBytes();
		// 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
		SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
		// 生成一个指定 Mac 算法 的 Mac 对象
		Mac mac = Mac.getInstance(MAC_NAME);
		// 用给定密钥初始化 Mac 对象
		mac.init(secretKey);

		byte[] text = encryptText.getBytes();
		// 完成 Mac 操作
		return mac.doFinal(text);
	}

	public static Date dateTrim(Date date) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		date = calendar.getTime();

		return date;
	}

	/**
	 * 取出非utf-8的字符
	 * @param text
	 * @return
	 * @throws UnsupportedEncodingException
	 */

	public static String filterOffUtf8Mb4(String text)
			throws UnsupportedEncodingException {
		byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		int i = 0;
		while (i < bytes.length) {
			short b = bytes[i];
			if (b > 0) {
				buffer.put(bytes[i++]);
				continue;
			}
			b += 256;
			if ((b ^ 0xC0) >> 4 == 0) {
				buffer.put(bytes, i, 2);
				i += 2;
			}
			else if ((b ^ 0xE0) >> 4 == 0) {
				buffer.put(bytes, i, 3);
				i += 3;
			}
			else if ((b ^ 0xF0) >> 4 == 0) {
				i += 4;
			}
			// 添加处理如b的指为-48等情况出现的死循环
			else {
				buffer.put(bytes[i++]);
			}
		}
		buffer.flip();
		String temp = new String(buffer.array(), "utf-8");

		// 自定义消除非法字符
		temp = temp.replace("❤", "");
		temp = temp.replace("⋯⋯", "。");
		temp = temp.replace("，", ",");
		temp = temp.replace("、", ",");
		temp = temp.replace("“", "\"");
		temp = temp.replace("”", "\"");
		temp = temp.replace("‘", "'");
		temp = temp.replace("’", "'");
		temp = temp.replace("；", ";");
		temp = temp.replace("（", "(");
		temp = temp.replace("）", ")");
		temp = temp.replace(" ާ", "");

		return temp;
	}

	public static <T> String list2String(List<T> list) {

		if (list == null) {
			return "null";
		}

		if (list.size() == 0) {
			return "";
		}

		String res = "";
		boolean firstItem = true;

		Iterator<T> it = list.iterator();
		while (it.hasNext()) {

			if (firstItem) {
				res = res + it.next();
				firstItem = false;
			}
			else {
				res = res + "," + it.next();
			}
		}

		return res;
	}

	/**
	 * 替换视频中的缩头标签
	 * @author kingsley0
	 * @param content
	 * @return
	 */
	public static String replaceVideoTagsInContent(String content,
			HttpServletRequest request) {
		if (content == null) {
			content = "";
		}
		String comeFrom = CommonUtils.checkFrom(request.getHeader("user-agent"));
//		String fromApp = request.getHeader(ServiceConstants.HTTP_HEADER_FROMAPP);
		String fromApp = request.getHeader("ios");
		String SupportVideo = request.getHeader("SupportVideo");
		Pattern pattern = Pattern.compile("\\[vod\\](.*?)\\[\\/vod\\]");
		Matcher matcher = pattern.matcher(content);
		List<String> result = new ArrayList<String>();
		while (matcher.find()) {
			result.add(matcher.group(1));
		}
		if ("ANDROID".equals(comeFrom) && fromApp != null && SupportVideo == null) {
			for (String s1 : result) {
				if (s1.contains("youku")) {
					content = content.replace("[vod]" + s1 + "[/vod]",
							"<a href='" + s1 + "'>点击播放视频</a>");
				}
				else {
					content = content.replace("[vod]" + s1 + "[/vod]",
							"<a href='https://v.qq.com/iframe/player.html?vid=" + s1
									+ "&tiny=0&auto=0'>点击播放视频</a>");
				}
			}
		}
		else {
			for (String s1 : result) {
				if (s1.contains("youku")) {
					content = content.replace("[vod]" + s1 + "[/vod]",
							"<div class='glh-video'><iframe frameborder='0' controls='controls' class='glh-video' src='"
									+ s1
									+ "' frameborder=0 allowfullscreen></iframe></div>");
				}
				else {
					content = content.replace("[vod]" + s1 + "[/vod]",
							"<div class='glh-video'><iframe frameborder='0' controls='controls' class='glh-video' src='https://v.qq.com/iframe/player.html?vid="
									+ s1 + "&tiny=0&auto=0'></iframe></div>");
				}
			}

		}

		return content;
	}

	/**
	 * 检查是该字符串是否由数字或者字母组成
	 * @param str
	 * @return
	 */
	public static Boolean isLetterOrNumber(String str) {

		Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
		Matcher matcher = pattern.matcher(str);

		return matcher.matches();
	}

	/**
	 * 检查手机号是否合法
	 * @param phone 手机号
	 * @return 合法返回true，否则返回false
	 */
	public static boolean isLegalPhone(String phone) {
		return !StringUtils.isBlank(phone) && PHONE_PATTERN.matcher(phone).matches();
	}

	/**
	 * 检查电子邮件是否合法
	 * @param email 电子邮件地址
	 * @return 合法返回true，否则返回false
	 */
	public static boolean isLegalEmail(String email) {
		return !StringUtils.isBlank(email) && EMAIL_PATTERN.matcher(email).matches();
	}

	/**
	 * 检查链接是否是http超链接
	 * @param link
	 * @return
	 */
	public static boolean isHttp(String link) {
		// 暂时用检查字符串头部验证超链接,以后有更严格的需求可以替换正则表达式
		if (link == null || link.isEmpty()) {
			return false;
		}
		else if (link.startsWith("http://") || link.startsWith("https://")) {
			return true;
		}
		return false;
	}

	/**
	 * 过滤html的script标签 超过2才匹配得到返回null
	 *
	 * @param html string
	 * @return plain string
	 */
	public static String delHtmlScriptTag(String html) {

		if (html == null) {
			return null;
		}
		Pattern p_html = Pattern.compile(regExHtmlScript, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(html);
		if (m_html.find()) {
			html = m_html.replaceAll("");
		}
		else {
			return html;
		}

		Matcher m_html_1 = p_html.matcher(html);
		if (m_html_1.find()) {
			html = m_html_1.replaceAll("");
		}
		else {
			return html;
		}

		Matcher m_html_2 = p_html.matcher(html);
		if (m_html_2.find()) {
			return null;
		}

		return html;
	}

	/**
	 * 比较app版本号大小
	 * @param currentVersion 当前版本
	 * @param givenVersion 指定版本
	 * @return currentVersion > givenVersion返回1，相等返回0，小于返回-1
	 */
	public static int compareVersion(String currentVersion, String givenVersion) throws IllegalArgumentException{
		if (StringUtils.isBlank(currentVersion) || StringUtils.isBlank(givenVersion)) {
			throw new IllegalArgumentException("compareVersion error: illegal params.");
		}
		// 注意此处为正则匹配，不能用.；
		String[] currentVersionArray = currentVersion.split("\\.");
		String[] givenVersionArray = givenVersion.split("\\.");
		int index = 0;
		// 取最小长度值
		int minLength = Math.min(currentVersionArray.length, givenVersionArray.length);
		int diff = 0;
		// 先比较长度 ,再比较字符
		while (index < minLength
				&& (diff = currentVersionArray[index].length() - givenVersionArray[index].length()) == 0
				&& (diff = currentVersionArray[index].compareTo(givenVersionArray[index])) == 0) {
			++index;
		}
		// 如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
		diff = (diff != 0) ? diff : currentVersionArray.length - givenVersionArray.length;
		return diff;
	}

}
