package com.sap.cisp.xhna.data.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class SendHttp {

    private static Logger logger = LoggerFactory.getLogger(SendHttp.class);

    /**
     * send http request
     * 
     * @throws Exception
     */
    public static JSONObject sendGet(URL url) throws Exception {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        JSONObject jsonObject = null;
        try {
            HttpURLConnection oConn = null;
            oConn = (HttpURLConnection) url.openConnection();
            oConn.setConnectTimeout(5000);
            oConn.connect();
            if (oConn.getResponseCode() == 200) {
                in = new BufferedReader(new InputStreamReader(
                        oConn.getInputStream(), "UTF-8"));
                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
            } else {
                logger.error("HTTP response code:" + oConn.getResponseCode()
                        + "\nHTTP response message:"
                        + oConn.getResponseMessage() + "\nResult:" + result);
                throw new DataCrawlException(oConn.getResponseMessage(),
                        new Exception(
                                "HTTP response error. With Response Code "
                                        + oConn.getResponseCode()),
                        oConn.getResponseCode());
            }

            jsonObject = JSONObject.parseObject(result.toString());
        } catch (Exception e) {
            System.err.println(url.toString() + "---get fail!");
            e.printStackTrace();
            jsonObject = null;
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                throw e2;
            }
        }
        return jsonObject;
    }

}
