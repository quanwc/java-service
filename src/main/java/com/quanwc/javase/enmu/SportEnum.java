package com.quanwc.javase.enmu;


/**
 * 运动的枚举类
 * Created by quanwenchao
 * 2018/7/26 10:05:55
 */
public enum SportEnum {

    Basketball(1), Football(2), Billiards(3);

    private Integer numId; // 运动编号

    SportEnum(Integer numId) {
        this.numId = numId;
    }

    public Integer getNumId() {
        return numId;
    }

    public void setNumId(Integer numId) {
        this.numId = numId;
    }
}
