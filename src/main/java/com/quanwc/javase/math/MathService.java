package com.quanwc.javase.math;

import lombok.val;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

import static jdk.nashorn.internal.objects.Global.println;

/**
 * @author quanwenchao
 * @date 2019/7/24 11:34:59
 */
public class MathService {

    public void math() {
        val num1 = Math.pow(2, 3); // 2的3次方
        println(num1);

        val num2 = Math.log10(2); // 以10为第，2的对数  e: 2.71828182846
        println(num2);

        val num3 = Math.cbrt(0.6931471805599453); // 27的立方根 eg: 27的1/3
        println(num3);

    }

    /**
     * 生成中位数
     * @param paramList
     * @return
     */
    private Double generateMedian(List<Double> paramList) {
        if (CollectionUtils.isEmpty(paramList)) {
            return 0.0;
        }
        // 对list排序
        Collections.sort(paramList);
        // 生成中位数
        Double median;
        if (paramList.size() % 2 == 0) {
            median = (paramList.get(paramList.size() / 2 - 1) + paramList.get(paramList.size() / 2)) / 2;
        } else {
            median = paramList.get(paramList.size() / 2);
        }
        return median;
    }


}
