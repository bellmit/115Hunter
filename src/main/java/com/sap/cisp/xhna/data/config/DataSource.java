package com.sap.cisp.xhna.data.config;

public class DataSource {
    private String id = "";
    private String url = "";
    private String driver = "";
    private String userName = "";
    private String password = "";

    public DataSource() {
    }

    public DataSource(String id, String driver, String url, String username,
            String password, String type, String method) {
        this.id = id;
        this.driver = driver;
        this.url = url;
        this.userName = username;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getDriver() {
        return driver;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "DataSource [id=" + id + ", url=" + url + ", driver=" + driver
                + ", userName=" + userName + ", password=" + password + "]";
    }
}
