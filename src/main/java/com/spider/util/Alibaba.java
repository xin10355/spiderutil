package com.spider.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.google.common.collect.Maps;
import com.spider.util.domain.Supplier;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.w3c.dom.NamedNodeMap;

import java.io.Console;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

/**
 * 1688 供应商
 */
public class Alibaba {

    static Logger log = Logger.getLogger(Alibaba.class);
    static String JDBC_DRIVER = "org.apache.hive.jdbc.HiveDriver";
    static String CONNECTION_URL = "jdbc:hive2://10.138.23.145:21050/;auth=noSasl";
    static Connection con = null;
    static WebClient wc = null;

    Map<String, Supplier> suppliers = Maps.newLinkedHashMap(); //供应商(链接-obj)
    //家用电器搜索供应商列表页
    static String search_url = "https://s.1688.com/company/company_search.htm?keywords=%BC%D2%D3%C3%B5%E7%C6%F7&sortType=pop&n=y&filt=y";
    static String contact_url = "/page/contactinfo.htm";
    static String bsr_url = "/event/app/winport_bsr/getBsrData.htm";
    int maxPageNum = 100; //总页数


    public static void main(String[] args) throws Exception {
        log.setLevel(Level.INFO);
        Scanner scanner = new Scanner(System.in);
        System.out.printf("账号:");
        String username = scanner.nextLine();
        System.out.printf("密码:");
        String password = scanner.nextLine();
        new Alibaba().run(username, password);
    }

    /***
     * start
     * @throws Exception
     */
    @Test
    public void run(String tb_user, String tb_pwd) throws Exception {
        System.out.println("=====开始执行=====");
        initWc(); //init
        login(tb_user, tb_pwd); //taobao登录
        //analysisPage(); //分页信息

        //获取所有分页下供应商链接
        log.info("获取供应商链接...");
        for (int i = 1; i <= maxPageNum; i++) {
            listSuppliers(search_url + "&pageSize=30&offset=3&beginPage=" + i);
        }
        //suppliers.put("https://phisinic.1688.com", new Supplier("中山市飞时电器有限公司","https://phisinic.1688.com"));  //test
        log.info("获取供应商链接共"+ suppliers.size() + "条!");

        //处理获取到的所有供应商
        int num = 1;
        String now = "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "',"; //当前时间
        String insert_sql = "INSERT into rrs.internet_alibaba_suppliers(created_date,name,contact,contact_extend,telephone,mobilephone,tax,address,zip,home_url,url,duration,region,desc_rate,desc_rate_trend,resp_rate,resp_rate_trend,ship_rate,ship_rate_trend,return_rate) VALUES (";
        PreparedStatement ps = null;
        ResultSet st = null;
        try {
            Class.forName(JDBC_DRIVER);
            con = DriverManager.getConnection(CONNECTION_URL, "shadmin", "shadmin");

            for(Map.Entry<String, Supplier> entry: suppliers.entrySet()) {
                log.info("第" + num + "个：");
                Supplier supp = null;
                try {
                    supp = getSuppliers(entry.getKey());
                }catch (Exception e){
                    supp = entry.getValue();
                }
                //insert
                ps = con.prepareStatement(insert_sql + now + supp.toString() + ")");
                ps.execute();
                num++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (st != null) {
                st.close();
            }
            if (con != null) {
                con.close();
            }
            if (wc != null) {
                wc.close();
            }
        }
    }

    //初始化webClient
    public static void initWc() throws InterruptedException {
        //browser设置
        BrowserVersion bv = BrowserVersion.CHROME;
        bv.setUserLanguage("zh-CN"); //设置语言，否则不知道传过来是什么编码
        bv.setSystemLanguage("zh-CN");
        bv.setBrowserLanguage("zh-CN");
        bv.setPlatform("Win32"); // 源码里是写死Win32的，不知道到生产环境（linux）会不会变，稳妥起见还是硬设
        //webClient设置
        wc = new WebClient(bv);
        wc.getOptions().setUseInsecureSSL(false); // 允许使用不安全的SSL连接。如果不打开，站点证书过期的https将无法访问
        wc.getOptions().setJavaScriptEnabled(false);
        wc.getOptions().setCssEnabled(false);
        wc.getCookieManager().setCookiesEnabled(true);//开启cookie管理
        wc.getOptions().setThrowExceptionOnScriptError(false);// 禁用一些异常抛出
        wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
        wc.getOptions().setDoNotTrackEnabled(false); // 随请求发送DoNotTrack
        wc.getOptions().setRedirectEnabled(true);
        wc.setAjaxController(new NicelyResynchronizingAjaxController()); // 设置ajax控制器
        wc.setJavaScriptTimeout(5000); // 设置JS超时
        wc.getOptions().setTimeout(100000); //设置连接超时时间
    }

    /**
     * taobao login
     */
    public void login(String tb_user, String tb_pwd){
        try {
            String login_url = "https://login.taobao.com/member/login.jhtml";
            HtmlPage page = wc.getPage(login_url);
            HtmlInput ln = page.getHtmlElementById("TPL_username_1");
            HtmlInput pwd = page.getHtmlElementById("TPL_password_1");
            ln.setAttribute("value", tb_user);
            pwd.setAttribute("value", tb_pwd);
            HtmlButton submit = (HtmlButton) page.getElementById("J_SubmitStatic");
            HtmlPage nextPage = submit.click();
            //wc.waitForBackgroundJavaScript(3000);
            String redirectPageText = nextPage.asXml().replace(" ", "").replace("\n","");
            if(redirectPageText.indexOf("页面跳转中")>0){
                String target = redirectPageText.substring(redirectPageText.indexOf("top.location.href=")+19, redirectPageText.indexOf("},500);window.callback=function(){};")-3);
                HtmlPage _tbpage = wc.getPage(target); //登陆后跳转(初始化taobao.com的cookie)
                HtmlPage _1688page = wc.getPage("https://114.1688.com/newbie/coe.htm"); //打开1688.com(初始化1688.com的cookie)
            }
            //Set<Cookie> cookies = wc.getCookieManager().getCookies();
        } catch (FailingHttpStatusCodeException failingHttpStatusCodeException) {
            failingHttpStatusCodeException.printStackTrace();
        } catch (MalformedURLException malformedURLException) {
            malformedURLException.printStackTrace();
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    /**
     * 获取最大页数、分页链接
     * @throws Exception
     */
    public void analysisPage() throws Exception {
        try {
            HtmlPage page = (HtmlPage) wc.getPage(search_url);
            String totalPageNum = page.querySelector("#sw_mod_pagination_form div span").asText(); //总页数
            if(StringUtils.isNotBlank(totalPageNum)){
                totalPageNum = totalPageNum.replace("共","").replace("页","");
                maxPageNum = Integer.parseInt(totalPageNum);
                log.info("最大分页数：" + maxPageNum);
            }
        } catch (FailingHttpStatusCodeException failingHttpStatusCodeException) {
            failingHttpStatusCodeException.printStackTrace();
        } catch (MalformedURLException malformedURLException) {
            malformedURLException.printStackTrace();
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    /**
     * 解析供应商列表页面dom
     * @param url
     * @throws Exception
     */
    public void listSuppliers(String url) throws Exception {
        try {
            HtmlPage page = wc.getPage(url);
            DomNodeList<DomNode> suppList = page.querySelectorAll("#sw_mod_searchlist ul li[class='company-list-item']");
            for (int i = 0; i < suppList.size(); i++) {
                DomNode suppDom = suppList.get(i);
                NamedNodeMap attributeMap = suppDom.querySelector(".list-item-left .wrap .list-item-title a").getAttributes();
                String suppHref = attributeMap.getNamedItem("href").getNodeValue(); //url
                String suppName = attributeMap.getNamedItem("title").getNodeValue(); //name
                suppHref = suppHref.substring(0, suppHref.indexOf("1688.com")+8); // https://*.1688.com
                Supplier supp = new Supplier(suppName, suppHref);
                suppliers.put(suppHref, supp);
            }
        } catch (FailingHttpStatusCodeException failingHttpStatusCodeException) {
            failingHttpStatusCodeException.printStackTrace();
        }
    }

    /**
     * 解析供应商页面dom
     * @param url
     * @throws Exception
     */
    public Supplier getSuppliers(String url) throws Exception {
        Supplier supplier = suppliers.get(url);
        log.info(supplier.getUrl() + " " + supplier.getName());

        /*
        //设置cookie
        wc.getCookieManager().clearCookies();
        Iterator<Cookie> ci = cookies.iterator();
        while (ci.hasNext()) {
            wc.getCookieManager().addCookie(ci.next());
        }*/

        try {
            HtmlPage page = (HtmlPage) wc.getPage(url + contact_url); //联系方式页面
            TextPage bsrpage = wc.getPage(url + bsr_url); //bsr
            //联系方式信息
            DomNode contactInfoDom = page.querySelector("#site_content .grid-main .main-wrap .mod .m-body .m-content .props-part .fd-clr");
            String contact = contactInfoDom.querySelector(".contact-info dl dd a").asText().trim();
            String contactInfo = contactInfoDom.querySelector(".contact-info dl dd").asText().replace("免费电话","").trim();
            supplier.setContact(contact);
            supplier.setContactExtend(contactInfo);
            DomNodeList descInfoDom = page.querySelectorAll("#site_content .grid-main .main-wrap .mod .m-body .m-content .props-part .fd-clr .fd-line .contcat-desc dl");
            for (int i = 0; i < descInfoDom.size(); i++) {
                DomNode dl = (DomNode) descInfoDom.get(i);
                String label = dl.querySelector("dt").asText().replace(" ","").trim();
                String value = dl.querySelector("dd").asText().trim();
                if(label.equals("电话：")){
                    supplier.setTelephone(value);
                }
                if(label.equals("移动电话：")){
                    supplier.setMobilephone(value);
                }
                if(label.equals("传真：")){
                    supplier.setTax(value);
                }
                if(label.equals("地址：")){
                    supplier.setAddress(value);
                }
                if(label.equals("邮编：")){
                    supplier.setZip(value);
                }
                if(label.indexOf("主页")>=0){
                    DomNodeList dds = dl.querySelectorAll("dd > div");
                    for (int j = 0; j < dds.size(); j++) {
                        DomNode urlDom = (DomNode)dds.get(j);
                        String _url = urlDom.querySelector("a").getAttributes().getNamedItem("href").getNodeValue();
                        if(_url.indexOf("1688.com")<0){
                            supplier.setHomeUrl(_url);
                        }
                    }
                }
            }

            //商家(店铺)信息
            DomNode shopInfoDom = page.querySelector("#site_content .grid-sub .mod .m-body .m-content div div .content");
            DomNode durationNode = shopInfoDom.querySelector(".abstract .certify-info .tp-info a span"); //开店时间
            if(durationNode==null){
                durationNode = shopInfoDom.querySelector(".abstract .certify-info .tp-info");
            }
            String _duration = "";
            if (durationNode!=null){
                _duration = durationNode.asText().trim();
            }
            supplier.setDuration(_duration);
            String region = shopInfoDom.querySelector(".detail .address span[class='disc']").asText().trim(); //所在地区
            if(StringUtils.isNotBlank(region)){
                supplier.setRegion(region);
            }

            //bsr信息
            String jsonText = bsrpage.getContent();
            JSONObject jsonData = JSONObject.parseObject(jsonText).getJSONObject("result");
            JSONArray bsrRate = jsonData.getJSONArray("bsrDataList");
            String returnRate = jsonData.get("backRateCompareLineRate").toString();
            for (int i = 0; i < bsrRate.size(); i++) {
                JSONObject object = (JSONObject)bsrRate.get(i);
                String tag = object.get("tag").toString();
                String value = object.get("compareLineRate").toString();
                String trend = object.get("compareTag").toString();
                if(value.equals("0")){
                    value = "=";
                    trend = "--";
                } else if(trend.equals("1")){
                    trend = "↑";
                } else {
                    trend = "↓";
                }

                if(tag.equals("hm")){  //货描率
                    supplier.setDescRate(value);
                    supplier.setDescRateTrend(trend);
                }
                if(tag.equals("xy")){  //响应率
                    supplier.setRespRate(value);
                    supplier.setRespRateTrend(trend);
                }
                if(tag.equals("fh")){  //发货率
                    supplier.setShipRate(value);
                    supplier.setShipRateTrend(trend);
                }
            }
            supplier.setReturnRate(returnRate); //回头率

            //在线沟通信息
            /*DomNodeList onlineContactDom = page.querySelectorAll("#oWebMsg_Slider_qyzx .qyzx3_sidebar .qyzx3_list_subaccount > div");
            List<Contact> contactList = Lists.newLinkedList();
            for (int i = 0; i < onlineContactDom.size(); i++) {
                Contact _contact = new Contact();
                DomNode dom = (DomNode) onlineContactDom.get(i);
                String _desc = dom.querySelector("a span").asText().trim();
                String _url = dom.querySelector("a").getAttributes().getNamedItem("href").getNodeValue();
                _contact.setDesc(_desc);
                _contact.setUrl(_url);
                contactList.add(_contact);
            }
            supplier.setContacts(contactList);*/
        } catch (FailingHttpStatusCodeException failingHttpStatusCodeException) {
            failingHttpStatusCodeException.printStackTrace();
        } catch (MalformedURLException malformedURLException) {
            malformedURLException.printStackTrace();
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }

        return supplier;
    }


}
