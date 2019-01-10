package com.spider.util.domain;

/**
 * Created by xin on 2019/01/10.
 */
public class JdShop {
    private String name; //品牌名
    private String shopName; //店铺名
    private String url;
    private String value; //售后服务评分
    private String trend; //趋势

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }
}
