package com.quanwc.javase.thread.bean;

import java.sql.Timestamp;
import java.util.Objects;

import javax.persistence.*;

/**
 * @author quanwenchao
 * @date 2019/3/21 21:39:40
 */
@Entity
@Table(name = "hk_ccs_company_weibo_1_25", schema = "wefid", catalog = "")
public class HKAndCCS {
    private long id;
    private String sendName;
    private String stockCode;
    private String stockName;
    private Timestamp sendDatetime;
    private String content;
    private long weiboId;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "send_name")
    public String getSendName() {
        return sendName;
    }

    public void setSendName(String sendName) {
        this.sendName = sendName;
    }

    @Basic
    @Column(name = "stock_code")
    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    @Basic
    @Column(name = "stock_name")
    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    @Basic
    @Column(name = "send_datetime")
    public Timestamp getSendDatetime() {
        return sendDatetime;
    }

    public void setSendDatetime(Timestamp sendDatetime) {
        this.sendDatetime = sendDatetime;
    }

    @Basic
    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Basic
    @Column(name = "weibo_id")
    public long getWeiboId() {
        return weiboId;
    }

    public void setWeiboId(long weiboId) {
        this.weiboId = weiboId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HKAndCCS that = (HKAndCCS) o;
        return id == that.id &&
                weiboId == that.weiboId &&
                Objects.equals(sendName, that.sendName) &&
                Objects.equals(stockCode, that.stockCode) &&
                Objects.equals(stockName, that.stockName) &&
                Objects.equals(sendDatetime, that.sendDatetime) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, sendName, stockCode, stockName, sendDatetime, content, weiboId);
    }
}
