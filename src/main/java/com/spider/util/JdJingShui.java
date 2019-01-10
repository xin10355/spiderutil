package com.spider.util;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.collect.Maps;
import com.spider.util.domain.Supplier;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
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

    Map<String, Supplier> suppliers = Maps.newLinkedHashMap(); //供应商(链接-obj)


    /**
     * 爬取京东综合评分,与行业相比,将数据封装到dsr对象.
     * @param wc
     * @param data
     */
    public void getJDMydAndThyInfo(WebClient wc,JdtmData data){
        try {
            HtmlPage jdympage = (HtmlPage) wc.getPage(data.getJd_url());
            /*DomNodeList jdymList1 = jdympage.querySelectorAll(".jGrade em");
            data.setMyd_jd(((DomNode) jdymList1.get(0)).asText());
            DomNodeList jdymList2 = jdympage.querySelectorAll(".jRatingContent .jIconLow em");
            DomNodeList jdymList3 = jdympage.querySelectorAll(".jRatingContent .jIconHigh em");


            //判断颜色样式
            if (jdymList2.getLength() == 1) {
                if(StringUtil.isNotEmpty(((DomNode) jdymList2.get(0)).asText())&&!((DomNode) jdymList2.get(0)).asText().equals("%")){
                    data.setThy_jd("-" + ((DomNode) jdymList2.get(0)).asText());
                }

            }else {
                data.setThy_jd( ((DomNode) jdymList3.get(0)).asText());
            }*/


            //20190110 改动 xin
            String appId = jdympage.querySelector("#pageInstance_appId").getAttributes().getNamedItem("value").getNodeValue();
            // https://mall.jd.com/view/getJshopHeader.html?callback=jQuery&appId=
            HtmlPage dsrpage = wc.getPage("https://mall.jd.com/view/getJshopHeader.html?callback=jQuery&appId=" + appId + "&_=" + new Date().getTime()); //dsr header dom
            String jsonText = dsrpage.getBody().asXml();
            String html = jsonText.replace("  ","").replace("\r\n","").replace("\\&quot;","");
            Document dom = Jsoup.parse(html);
            Elements spans = dom.getElementById("jRatingTotal_table").child(2).child(1).getElementsByTag("span");
            if(spans.size()==2){ //与同行业相比有差别
                String myd_jd = spans.get(0).html().trim();
                data.setMyd_jd(myd_jd);
                if(spans.get(0).attributes().get("class").equals("level-text-green")){
                    data.setThy_jd("-"); //低
                }else{
                    data.setThy_jd(""); //高
                }
            } else { //与同行业相比无差别
                data.setMyd_jd(null);
                data.setThy_jd("--"); //抓取不到百分比了
            }

        } catch (FailingHttpStatusCodeException failingHttpStatusCodeException) {
            failingHttpStatusCodeException.printStackTrace();
        } catch (MalformedURLException malformedURLException) {
            malformedURLException.printStackTrace();
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

}
