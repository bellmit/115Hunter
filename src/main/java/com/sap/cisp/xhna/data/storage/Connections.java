package com.sap.cisp.xhna.data.storage;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connections {
    private String driverClassName;
    private String url;
    private String username;
    private String password;

    private static Logger log = LoggerFactory.getLogger(Connections.class);

    public Connections(String driverClassName, String url, String username,
            String password) {
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() {
        try {
            Class.forName(this.driverClassName);
        } catch (ClassNotFoundException e) {
            log.error("No JDBC Driver found!");
            return null;
        }
        Connection conn = null;
        try {
            /*
             * Begin workaround to fix the driver load issue
             * java.lang.NullPointerException 
             * at java.util.Hashtable.put(Hashtable.java:459)
             * at java.util.Properties.setProperty(Properties.java:166)
             * at org.postgresql.Driver.connect(Driver.java:244) ~[gearman-server-0.8.11-20150731.182506-1.jar:0.4] 
             * at java.sql.DriverManager.getConnection(DriverManager.java:664) ~[?:1.8.0_60] 
             * at java.sql.DriverManager.getConnection(DriverManager.java:247) ~[?:1.8.0_60] 
             * at com.sap.cisp.xhna.data.storage.Connections.getConnection (Connections.java:35)
             */
            Enumeration<Driver> driverEnum = DriverManager.getDrivers();
            while (driverEnum.hasMoreElements()) {
                Driver driver = driverEnum.nextElement();
                log.info("*** Get the DB driver -> {}", driver.getClass()
                        .getName());
                if (driver.getClass().getName()
                        .equalsIgnoreCase("org.postgresql.Driver")) {
                    log.info("*** Deregister the postgresql driver.");
                    DriverManager.deregisterDriver(driver);
                }
            }
            /* End of workaround */

            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            log.error("Could not Connect to url: [{}]!", url);
            return null;
        }
        return conn;
    }
}
