package com.spider.util.domain;

/**
 * Created by xin on 2019/01/10.
 */
public class JdBrand {
    private String name; //品牌名
    private String shopName; //店铺名
    private String shopUrl;
    private String filterUrl; //搜索url
    private String value; //售后服务评分
    private String trend; //趋势


    public JdBrand(String name, String filterUrl) {
        this.name = name;
        this.filterUrl = filterUrl;
    }

    @Override
    public String toString() {
        return "'" + (name==null?"":name) +
                "', '" + (shopName==null?"":shopName) +
                "', '" + (shopUrl==null?"":shopUrl) +
                "', '" + (filterUrl==null?"":filterUrl) +
                "', '" + (value==null?"":value) +
                "', '" + (trend==null?"":trend) +
                "'";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public void setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
    }

    public String getFilterUrl() {
        return filterUrl;
    }

    public void setFilterUrl(String filterUrl) {
        this.filterUrl = filterUrl;
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
