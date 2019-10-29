package com.quanwc.javase.lambda.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.quanwc.javase.dingtalk.entity.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author quanwenchao
 * @date 2019/10/28 15:09:57
 */
@Slf4j
public class CompanyService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void computeBaseSafetyValue2() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
        String endDateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log.info("company result computeBaseSafetyValue begin");

        try{
            List<JSONObject> jsonObjectList = mongoTemplate.findAll(JSONObject.class, "company_table");
            for (JSONObject jsonObject : jsonObjectList) {
                String id = jsonObject.getString("_id"); // 股票代码(公司)

                JSONArray dayArray = sortJsonArray(jsonObject.getJSONArray("day")); // 公司的day下的数据
                List<JSONObject> dayArrayResult =
                        dayArray.stream().filter(JSONObject.class::isInstance).map(JSONObject.class::cast).filter(
                                s -> s.getString("date").compareTo(endDateStr) < 0).collect((Collectors.toList()));

                // 计算正向基期
                double basePSafetyValue = 500;
                List<Double> pIndexList = dayArrayResult.stream().map(s2 -> s2.getDouble("p_index")).map(pIndex -> {
                    pIndex = pIndex <= 100 ? 500 : pIndex;
                    return pIndex;
                }).collect(Collectors.toList());
                // 去除最大值
                pIndexList.remove(Collections.max(pIndexList));
                basePSafetyValue = pIndexList.stream().mapToDouble(Double::valueOf).sum() / 60;
                basePSafetyValue = basePSafetyValue < 500 ? 500 : basePSafetyValue;

                // 计算负向基期
                double baseNSafetyValue = 500;
                List<Double> nIndexList = dayArrayResult.stream().map(s2 -> s2.getDouble("n_index")).map(nIndex -> {
                    nIndex = nIndex <= 100 ? 500 : nIndex;
                    return nIndex;
                }).collect(Collectors.toList());
                // 去除最大值
                nIndexList.remove(Collections.max(nIndexList));
                baseNSafetyValue = nIndexList.stream().mapToDouble(Double::valueOf).sum() / 60;
                baseNSafetyValue = baseNSafetyValue < 500 ? 500 : baseNSafetyValue;

                // step1：更新到company_result表
                Query query1 = new Query(Criteria.where("_id").is(id).and("day.date").is(date));
                Update update1 = new Update().set("base_p_safety_value", basePSafetyValue)
                        .set("base_n_safety_value", baseNSafetyValue)
                        .set("day.$.base_p_safety_value", basePSafetyValue)
                        .set("day.$.base_n_safety_value", baseNSafetyValue);
                mongoTemplate.updateFirst(query1, update1, "company_table");

            }

            log.info("company result computeBaseSafetyValue daily end");
        } catch (Exception e) {
            log.error("computeBaseSafetyValue error: ", e);

        }
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


}
