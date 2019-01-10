package com.spider.util.domain;

import java.util.List;

/**
 * Created by xin on 2018/12/24.
 */
public class Supplier {

    private String name; //供应商名称
    private String contact; //联系人姓名
    private String contactExtend; //联系人扩展
    private String telephone; //固话
    private String mobilephone; //移动电话
    private String tax; //传真
    private String address; //地址
    private String zip; //邮编
    private String homeUrl; //公司主页
    private String url; //1688链接地址
    private String duration; //开店时间
    private String region; //所在地区
    private String descRate; //货描率
    private String descRateTrend;
    private String respRate; //响应率
    private String respRateTrend;
    private String shipRate; //发货率
    private String shipRateTrend;
    private String returnRate; //回头率
    private List<Contact> contacts;


    public Supplier(String name, String url){
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        String data = "'" +(name==null?"":name) +
                "','" + (contact==null?"":contact) +
                "','" + (contactExtend==null?"":contactExtend) +
                "','" + (telephone==null?"":telephone) +
                "','" + (mobilephone==null?"":mobilephone) +
                "','" + (tax==null?"":tax) +
                "','" + (address==null?"":address) +
                "','" + (zip==null?"":zip) +
                "','" + (homeUrl==null?"":homeUrl) +
                "','" + (url==null?"":url) +
                "','" + (duration==null?"":duration) +
                "','" + (region==null?"":region) +
                "','" + (descRate==null?"":descRate) +
                "','" + (descRateTrend==null?"":descRateTrend) +
                "','" + (respRate==null?"":respRate) +
                "','" + (respRateTrend==null?"":respRateTrend) +
                "','" + (shipRate==null?"":shipRate) +
                "','" + (shipRateTrend==null?"":shipRateTrend) +
                "','" + (returnRate==null?"":returnRate) + "'";
        return data;
    }

    /////////////////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContactExtend() {
        return contactExtend;
    }

    public void setContactExtend(String contactExtend) {
        this.contactExtend = contactExtend;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMobilephone() {
        return mobilephone;
    }

    public void setMobilephone(String mobilephone) {
        this.mobilephone = mobilephone;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getHomeUrl() {
        return homeUrl;
    }

    public void setHomeUrl(String homeUrl) {
        this.homeUrl = homeUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDescRate() {
        return descRate;
    }

    public void setDescRate(String descRate) {
        this.descRate = descRate;
    }

    public String getRespRate() {
        return respRate;
    }

    public void setRespRate(String respRate) {
        this.respRate = respRate;
    }

    public String getShipRate() {
        return shipRate;
    }

    public void setShipRate(String shipRate) {
        this.shipRate = shipRate;
    }

    public String getReturnRate() {
        return returnRate;
    }

    public void setReturnRate(String returnRate) {
        this.returnRate = returnRate;
    }

    public String getDescRateTrend() {
        return descRateTrend;
    }

    public void setDescRateTrend(String descRateTrend) {
        this.descRateTrend = descRateTrend;
    }

    public String getRespRateTrend() {
        return respRateTrend;
    }

    public void setRespRateTrend(String respRateTrend) {
        this.respRateTrend = respRateTrend;
    }

    public String getShipRateTrend() {
        return shipRateTrend;
    }

    public void setShipRateTrend(String shipRateTrend) {
        this.shipRateTrend = shipRateTrend;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}
