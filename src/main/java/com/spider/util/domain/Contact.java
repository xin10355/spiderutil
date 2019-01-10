package com.spider.util.domain;

/**
 * Created by xin on 2018/12/24.
 */
public class Contact{
    String desc;
    String url;

    @Override
    public String toString() {
        return "'" + (desc==null?"":desc) + "'," +
                "'" + (url==null?"":url) + "'" ;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
