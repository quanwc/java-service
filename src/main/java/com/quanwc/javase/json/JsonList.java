package com.quanwc.javase.json;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * json -> list 的两种方式
 * @author quanwenchao
 * @date 2018/8/25 9:01:53
 */
public class JsonList {

    /**
     * json -> list
     * 方式一: objectMapper.getTypeFactory().constructParametricType(List.class, Account.class)
     * @throws Exception
     */
    public static void method1() throws Exception {
        String jsonStr = "[{\n" +
                "            \"accountKey\": \"coin\",\n" +
                "            \"accountName\": \"金币\",\n" +
                "            \"quantity\": 10\n" +
                "        },\n" +
                "        {\n" +
                "            \"accountKey\": \"experience\",\n" +
                "            \"accountName\": \"经验\",\n" +
                "            \"quantity\": 20\n" +
                "        },\n" +
                "        {\n" +
                "            \"accountKey\": \"cash\",\n" +
                "            \"accountName\": \"现金\",\n" +
                "            \"quantity\": 30\n" +
                "        }]\n";

        System.out.println(jsonStr);

        ObjectMapper objectMapper = new ObjectMapper();
        // 需要先反序列化复杂类型 为泛型的List Type
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, Account.class);
        List<Account> accountList = objectMapper.readValue(jsonStr, javaType);
        System.out.println(accountList);
    }


    /**
     * json -> list
     * 方式二: TypeReference<List<Account>>() {}
     * @throws Exception
     */
    public static void method2() throws Exception {
        String jsonStr = "[{\n" +
                "            \"accountKey\": \"coin\",\n" +
                "            \"accountName\": \"金币\",\n" +
                "            \"quantity\": 10\n" +
                "        },\n" +
                "        {\n" +
                "            \"accountKey\": \"experience\",\n" +
                "            \"accountName\": \"经验\",\n" +
                "            \"quantity\": 20\n" +
                "        },\n" +
                "        {\n" +
                "            \"accountKey\": \"cash\",\n" +
                "            \"accountName\": \"现金\",\n" +
                "            \"quantity\": 30\n" +
                "        }]\n";

        System.out.println(jsonStr);

        ObjectMapper objectMapper = new ObjectMapper();
        List<Account> accountList = objectMapper.readValue(jsonStr, new TypeReference<List<Account>>() {});
        System.out.println(accountList);
    }


    public static void main(String[] args) throws Exception {

        //method1();

        method2();
    }
}
