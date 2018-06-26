package com.sap.cisp.xhna.data.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.common.Util;

public class ConfigStorage {
    public static String DATABASE_TYPE;
    public static String HDFS_URL = "";
    public static String HIVE_URL = "";
    public static String IQ_URL = "";
    public static String IQ_USERNAME = "";
    public static String IQ_PASSWORD = "";
    public static DataSource DS_INFO = null;
    private static Logger log = LoggerFactory.getLogger(ConfigStorage.class);
    static {
        loadConfig();
    }

    private static void loadConfig() {
        InputStream inputStream = null;
        Properties properties = new Properties();
        try {
            inputStream = new FileInputStream(
                    "configurations/storage.properties");
            properties.load(inputStream);
            DATABASE_TYPE = properties.getProperty("storage.database.type");
            if ("hive".equalsIgnoreCase(DATABASE_TYPE)) {
                HDFS_URL = properties.getProperty("storage.hdfs.url");
                HIVE_URL = properties.getProperty("storage.hive.jdbc.url");
            } else if ("iq".equalsIgnoreCase(DATABASE_TYPE)) {
                IQ_URL = properties.getProperty("storage.iq.jdbc.url");
                IQ_USERNAME = properties.getProperty("storage.iq.username");
                IQ_PASSWORD = properties.getProperty("storage.iq.password");
            } else if ("oltpdatabase".equalsIgnoreCase(DATABASE_TYPE)) {
                DS_INFO = Util.getDBSByName("configurations/DataSource.xml",
                        properties.getProperty("storage.oltp.database"));
            } else {
                log.error("Parsing storage popperties error: database type defines error!");
                return;
            }

        } catch (Exception e) {
            log.error("Parsing storage popperties error: get parameters error!");
            return;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
