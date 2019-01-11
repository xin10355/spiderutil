package com.spider.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import com.spider.util.domain.JdBrand;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 京东 净水器、饮水机 DSR爬取
 * Created by xin on 2019/01/10.
 */
public class JdJingShui {

    static Logger log = Logger.getLogger(Alibaba.class);
    static String JDBC_DRIVER = "org.apache.hive.jdbc.HiveDriver";
    static String CONNECTION_URL = "jdbc:hive2://10.138.23.145:21050/;auth=noSasl";
    static Connection con = null;
    static WebClient wc = null;
    static String _p = "&sort=sort_rank_asc&trans=1&md=1&my=list_brand";
    static String yinshui_url = "https://list.jd.com/list.html?cat=737,738,898"; //净水器
    static String jingshui_url = "https://list.jd.com/list.html?cat=737,738,750"; //饮水机
    static String dsr_url = "https://mall.jd.com/view/getJshopHeader.html?callback=jQuery&appId="; //dsr
    Map<String, JdBrand> brands = Maps.newLinkedHashMap(); //品牌(名称-obj)


    //初始化webClient
    public static void initWc() throws InterruptedException {
        log.info("init webclient");
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

    @Test
    public void run() throws Exception {
        log.info("start...");
        initWc();
        getBrandNames(jingshui_url); //获取品牌名称
        getBrandNames(yinshui_url);
        save();
    }

    /**
     * save
     * @throws Exception
     */
    public void save() throws Exception {
        log.info("save...");
        String now = "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "',"; //当前时间
        String insert_sql = "INSERT into rrs.jddsr(created_date,name,shop_name,shop_url,filter_url,value,trend) VALUES (";
        PreparedStatement ps = null;
        ResultSet st = null;
        try {
            Class.forName(JDBC_DRIVER);
            con = DriverManager.getConnection(CONNECTION_URL, "shadmin", "shadmin");
            int num = 1;
            for(Map.Entry<String, JdBrand> entry: brands.entrySet()) {
                JdBrand brand = entry.getValue();
                log.info("第" + num + "个：");
                brand = filterBrand(entry.getValue());
                num++;
                try {
                    //insert
                    ps = con.prepareStatement(insert_sql + now + brand.toString() + ")");
                    ps.execute();
                }catch (Exception e){}
            }
            log.info("save success ：" + brands.size());
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


    /**
     * 获取当前页面的品牌名称
     * @param url
     */
    public void getBrandNames(String url) throws IOException {
        log.info("处理品牌名称");
        HtmlPage page = wc.getPage(url + _p); //品牌列表json
        String jsonText = page.asText();
        JSONArray jsonData = JSONObject.parseObject(jsonText).getJSONArray("brands");
        for (int i = 0; i < jsonData.size(); i++) {
            JSONObject json = jsonData.getJSONObject(i);
            String brandName = json.getString("name");
            int id = json.getIntValue("id");
            //eg: https://list.jd.com/list.html?cat=737,738,750&ev=exbrand_18374&sort=sort_rank_asc&trans=1&JL=3_品牌_小米（MI）
            String filterUrl = url + "&ev=exbrand_" + id + "&sort=sort_rank_asc&trans=1&JL=3_品牌_" + brandName; //拼接搜索url
            JdBrand jdBrand = new JdBrand(brandName, filterUrl);
            brands.put(brandName, jdBrand);
        }
    }

    /**
     * 搜索并过滤出当前品牌的旗舰店，不含 官方店、自营店、专营店、专卖店
     * 品牌名+类别+旗舰店
     * @param jdBrand
     */
    public JdBrand filterBrand(JdBrand jdBrand) throws Exception {
        String brandName = jdBrand.getName();
        String aliasName = brandName; //别称
        if(brandName.indexOf("（")>0){ //是否包含别称
            aliasName = brandName.substring(brandName.indexOf("（")+1, brandName.indexOf("）"));
            brandName = brandName.substring(0, brandName.indexOf("（"));
        }
        HtmlPage page = wc.getPage(jdBrand.getFilterUrl());
        DomNodeList<DomNode> items = page.querySelectorAll(".gl-i-wrap");
        for (int i = 0; i < items.size(); i++) {
            HtmlDivision div = (HtmlDivision) items.get(i);
            String venderid = div.getAttribute("venderid");
            String shopid = div.getAttribute("jdzy_shop_id");
            //获取店铺名称
            // https://rms.shop.jd.com/json/pop/shopInfo.action?callback=jQuery&ids=xxxxx&_=
            String shopInfoUrl = "https://rms.shop.jd.com/json/pop/shopInfo.action";
            HttpRequest request = HttpRequest.get(shopInfoUrl, true, "ids", venderid, "callback", "jQuery", "_", new Date().getTime());
            String body = request.body();
            String jsonText = body.replace("jQuery([","").replace("])","");
            JSONObject jsonObject = null;
            try {
                jsonObject = JSONObject.parseObject(jsonText);
            }catch (Exception e){
                continue;
            }
            String shopName = jsonObject.getString("name"); //店铺名称
            if(shopName==null){
                continue;
            }else{
                shopName = shopName.trim();
            }
            String shopUrl = "https://mall.jd.com/index-" + shopid + ".html";
            if(venderid.indexOf("10000")>=0 || shopName.contains("自营") || shopName.contains("专卖") || shopName.contains("专营") || shopName.contains("官方")){
                continue; //跳过官方店、自营店、专营店、专卖店
            } else if((shopName.contains(brandName) || shopName.contains(aliasName))
                    && (shopName.contains("净水") || shopName.contains("饮水"))
                    && shopName.contains("旗舰店")) {
                //'品牌名'+ 类别 +'旗舰店'
                jdBrand.setShopName(shopName);
                jdBrand.setShopUrl(shopUrl);
                log.info("success：" + brandName);
                break;
            }
        }
        jdBrand = getBrandInfo(jdBrand); //getdata
        return jdBrand;
    }

    /**
     * 抓取dsr信息
     * @param jdBrand
     */
    public JdBrand getBrandInfo(JdBrand jdBrand) throws Exception {
        if(jdBrand.getShopUrl()==null){
            return jdBrand;
        }
        String url = jdBrand.getShopUrl().replace("index", "shopLevel"); //替换链接
        HtmlPage jdympage = (HtmlPage) wc.getPage(url);
        String appId = jdympage.querySelector("#pageInstance_appId").getAttributes().getNamedItem("value").getNodeValue();
        //dsr header dom
        HtmlPage dsrpage = wc.getPage(dsr_url + appId + "&_=" + new Date().getTime());
        String jsonText = dsrpage.getBody().asXml();
        String html = jsonText.replace("  ","").replace("\r\n","").replace("\\&quot;","");
        Document dom = Jsoup.parse(html);
        Elements spans = dom.getElementById("jRatingTotal_table").child(2).child(1).getElementsByTag("span");
        if(spans.size()==2){ //与同行业相比有差别
            String dsr = spans.get(0).html().trim();
            jdBrand.setValue(dsr);
            if(spans.get(0).attributes().get("class").equals("level-text-green")){
                jdBrand.setTrend("低"); //低
            }else{
                jdBrand.setTrend("高"); //高
            }
        } else { //与同行业相比无差别
            jdBrand.setValue(null);
            jdBrand.setTrend("--"); //抓取不到分数了
        }
        return jdBrand;
    }

}
