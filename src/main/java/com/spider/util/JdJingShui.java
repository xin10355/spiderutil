package com.spider.util;

import com.gargoylesoftware.htmlunit.WebClient;
import com.google.common.collect.Maps;
import com.spider.util.domain.Supplier;
import org.apache.log4j.Logger;

import java.sql.Connection;
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

}
