package com.quanwc.javase.json;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * JsonArray 和 List互转
 *
 * @author quanwenchao
 * @date 2019/7/23 11:11:12
 */
public class JsonArrayListService {

    /**
     * JSONArray 转 List
     */
    public static void testJSONArray2List() {

        String str = "{\n" + "\t\"types\": [\n" + "\t\t1,\n" + "\t\t2,\n" + "\t\t3,\n" + "\t\t5\n" + "\t],\n"
                + "\t\"key\": {\n" + "\t\t\"relate\": [\n" + "\t\t\t{\n" + "\t\t\t\t\"windCode\": \"01211.HK\",\n"
                + "\t\t\t\t\"keyword\": {\n" + "\t\t\t\t\t\"product\": [\n" + "\t\t\t\t\t\t\"元\"\n" + "\t\t\t\t\t]\n"
                + "\t\t\t\t}\n" + "\t\t\t},\n" + "\t\t\t{\n" + "\t\t\t\t\"windCode\": \"windCode\",\n"
                + "\t\t\t\t\"keyword\": {\n" + "\t\t\t\t\t\"superevise\": [\n" + "\t\t\t\t\t\t\"发布*计划\"\n"
                + "\t\t\t\t\t]\n" + "\t\t\t\t}\n" + "\t\t\t}\n" + "\t\t],\n" + "\t\t\"match\": {\n"
                + "\t\t\t\"person\": [\n" + "\t\t\t\t\"王兴\"\n" + "\t\t\t],\n" + "\t\t\t\"product\": [\n"
                + "\t\t\t\t\"元\",\n" + "\t\t\t\t\"美团\"\n" + "\t\t\t],\n" + "\t\t\t\"superevise\": [\n"
                + "\t\t\t\t\"发布*计划\"\n" + "\t\t\t],\n" + "\t\t\t\"windCode\": [\n" + "\t\t\t\t\"03690.HK\"\n" + "\t\t\t]\n"
                + "\t\t}\n" + "\t},\n" + "\t\"hotKey\": [\n" + "\t\t\"连续亏损\",\n" + "\t\t\"亏损\",\n" + "\t\t\"短视频\"\n"
                + "\t]\n" + "}";
        JSONObject data = (JSONObject)JSONObject.parse(str); // String -> JSONObject
        List<Integer> listTypes = data.getJSONArray("types").toJavaList(Integer.class);


        JSONArray windCodeArray = new JSONArray();
        List<String> list2 = JSONObject.parseArray(windCodeArray.toJSONString(), String.class); // JSONArray -> List

        List<JSONObject> list = new ArrayList<>();
        String string = JSON.toJSONString(list); // List -> String

//        String str = " [\"000725.SZ\",\n" + "                \"600736.SH\",\n" + "                \"000651.SZ\"]";
        JSONArray array1 = JSONArray.parseArray(string); // String -> JSONArray
    }


    /**
     * Collectors.joining(",")方法，元素以","拼接
     */
    public static void testJoining() {
        List<Map<String, Object>> companyList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("wind_code", "0000011.SZ");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("wind_code", "000002.SZ");
        companyList.add(map1);
        companyList.add(map2);

        String windCodeStr = companyList.stream().map(m -> (String)m.get("wind_code")).collect(Collectors.joining(","));
        System.out.println(windCodeStr); // 000001.SZ,000002.SZ

//        companyList.sort(Comparator.comparing(s -> (String) s.get("wind_code")));
//        companyList.sort(Collections.reverseOrder());
//        System.out.println(companyList.toString());

    }


    /**
     * 两个list求交集
     */
    public static void testRetainAll() {
        List<String> list1 = new ArrayList<String>();
        List<String> list2 = new ArrayList<String>();
        list1.add("1");
        list1.add("2");
        list1.add("3");
        list1.add("4");

        list2.add("1");
        list2.add("3");
        list2.add("5");

        // 求交集: 使用retainAll
        list2.retainAll(list1); // list2与list1求交集，结果返回给list2，list1的值没有改变，类似于list2.retainAll(list1)
        for(String s : list2){
            System.out.println(s);
        }

        // 求交集: 用filter也可以实现
        List<String> resultList = list2.stream().filter(s -> list1.contains(s)).collect(Collectors.toList());
        resultList.stream().forEach(s -> System.out.println(s));
    }

    public static void testSort() {
        JSONArray days = new JSONArray();

        JSONObject object1 = new JSONObject();
        object1.put("date", "2019-07-23");
        object1.put("p_index", 10105.2);
        object1.put("n_index", 1503.1);
        days.add(object1);// 添加元素

        JSONObject object2 = new JSONObject();
        object2.put("date", "2019-07-25");
        object2.put("p_index", 12.6);
        object2.put("n_index", 156.1);
        days.add(object2);// 添加元素

        // 按照day倒排序、将JSONArray类型的days赋值给List<Object>类型的dayList
        // 注意：不能写成List<Object> dayList = sortJsonArray(days)，这是一个bug
        days = sortJsonArray(days);
        List<Object> dayList = days;
        if (dayList.size() > 61) {
            dayList = days.subList(0, 61);
        }
        System.out.println(dayList.toString());
    }

    /**
     * JSONArray中的数据，按照日期倒排列
     * @param jsonArray
     * @return 排序后的数据
     */
    private static JSONArray sortJsonArray(JSONArray jsonArray) {
        if ((null == jsonArray || jsonArray.size() == 0)) {
            return new JSONArray();
        }

        JSONArray resultJsonArray = new JSONArray();

        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonList.add(jsonArray.getJSONObject(i));
        }

        Collections.sort(jsonList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                //                return o1.getInteger("n_index").compareTo(o2.getInteger("n_index")); // string比较大小
                return o2.getString("date").compareTo(o1.getString("date")); // string比较大小
            }
        });
        for (int i = 0; i < jsonList.size(); i++) {
            resultJsonArray.add(jsonList.get(i));
        }
        return resultJsonArray;
    }

    public static void main(String[] args) {
        testJoining();
    }
}
