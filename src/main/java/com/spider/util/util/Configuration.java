package com.spider.util.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by xin on 2019/01/14.
 */
public class Configuration {

    private String connection_url;
    private String connection_driver;
    private String connection_username;
    private String connection_password;
    private String connectionUrl;
    private String connectionDriver;
    private String username;
    private String password;

    public Configuration(String connection_url, String connection_driver, String connection_username, String connection_password) throws IOException {
        this.connection_url = connection_url;
        this.connection_driver = connection_driver;
        this.connection_username = connection_username;
        this.connection_password = connection_password;
        loadConfiguration();
    }

    /**
     * 加载配置
     *
     * @throws IOException
     */
    public void loadConfiguration() throws IOException {
        InputStream inputConf = null;
        InputStream inputSql = null;
        try {
            //读取配置
            String confFile = "jdbc.conf";
            inputConf = Configuration.class.getClassLoader().getResourceAsStream(confFile);
            Properties prop = new Properties();
            prop.load(inputConf);
            connectionUrl = prop.getProperty(this.connection_url);
            connectionDriver = prop.getProperty(this.connection_driver);
            username = prop.getProperty(this.connection_username);
            password = prop.getProperty(this.connection_password);
        } finally {
            try {
                if (inputConf != null)
                    inputConf.close();
                if (inputSql != null)
                    inputSql.close();
            } catch (IOException e) {
                // nothing to do
            }
        }
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getConnectionDriver() {
        return connectionDriver;
    }

    public void setConnectionDriver(String connectionDriver) {
        this.connectionDriver = connectionDriver;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
