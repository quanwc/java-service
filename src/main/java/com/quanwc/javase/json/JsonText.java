package com.quanwc.javase.json;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * @author quanwenchao
 * @date 2018/8/24 18:47:50
 */
public class JsonText {
    public static void main(String[] args) throws Exception {

        List<Account> list = new ArrayList<Account>(){{
            add(new Account("coin", "金币", 10));
            add(new Account("experience", "经验", 20));
            add(new Account("cash", "金现金", 30));
        }};
        System.out.println(list);

        for (Account account : list) {
            if ("coin".equals(account.getAccountKey())) {
                System.out.println("金币余额: " + account.getQuantity());
            }
        }

    }
}


/**
 * @author quanwenchao
 * @date 2018/8/24 18:49:30
 */
@Data
class Account {

    private String accountKey;
    private String accountName;
    private Integer quantity;

    public Account() {
    }

    public Account(String accountKey, String accountName, Integer quantity) {
        this.accountKey = accountKey;
        this.accountName = accountName;
        this.quantity = quantity;
    }
}
