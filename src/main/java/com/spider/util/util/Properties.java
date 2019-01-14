package com.spider.util.util;

/**
 * Created by xin on 2019/01/14.
 */
public enum Properties {

    mysql_url(0, "jdbc.url.mysql"),
    mysql_driver(1, "jdbc.driver.mysql"),
    mysql_user(2, "jdbc.username.mysql"),
    mysql_pwd(3, "jdbc.password.mysql");


    int index;
    String value;

    Properties(int i, String s) {
        this.index = i;
        this.value = s;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
