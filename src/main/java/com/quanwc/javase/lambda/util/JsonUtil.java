package com.quanwc.javase.lambda.util;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author quanwenchao
 * @date 2018/12/19 22:21:20
 */
public class JsonUtil {

	private static ObjectMapper mapper;

	/**
	 * JSONString转Object
	 * @param jsonStr
	 * @param clz
	 * @throws IOException
	 */
	public static <T> T JsonStr2Obj(String jsonStr, Class<T> clz) throws IOException {
		if(StringUtils.isBlank(jsonStr)){
			return null;
		}
		return mapper.readValue(jsonStr, clz);
	}
}
