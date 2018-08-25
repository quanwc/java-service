package com.quanwc.javase.json;

import lombok.Data;

/**
 * @author quanwenchao
 * @date 2018/8/24 22:06:44
 */
public class JsonText2 {
    public static void main(String[] args) {
        Account2 account2 = new Account2(10d, 20d, 30d);
        System.out.println("金币余额: " + account2.getCoin());
    }
}


@Data
class Account2 {
    private Double coin;
    private Double experience;
    private Double cash;

    public Account2(Double coin, Double experience, Double cash) {
        this.coin = coin;
        this.experience = experience;
        this.cash = cash;
    }
}
