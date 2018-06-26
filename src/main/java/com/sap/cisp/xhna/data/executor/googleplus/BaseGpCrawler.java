package com.sap.cisp.xhna.data.executor.googleplus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseGpCrawler {
    private static Logger logger = LoggerFactory.getLogger(BaseGpCrawler.class);

    /**
     * Crawling with return value is a string.
     * 
     * @param urlString
     */
    public static String httpGetString(String urlString) throws Exception {
        StringBuffer result = new StringBuffer("");
        BufferedReader reader = null;

        int times = 0;
        do {
            if (times > 0) {
                logger.debug("retry(" + times + ") sending request to "
                        + urlString);
                try {
                    Thread.sleep(500 * times);
                } catch (InterruptedException e) {
                    throw e;
                }
            }
            times++;

            try {
                URL url = new URL(urlString);
                URLConnection connection;
                connection = url.openConnection();

                reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                logger.error("Exception : ", e);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Exception : ", e);
                }
            }

        } while (result.length() == 0 && times < 3);

        if (result.length() == 0) {
            logger.error("failed getting response from " + urlString);
        } else {
            logger.debug("OK getting response from " + urlString);
        }

        return result.toString();
    }

    /**
     * Crawling with return value is a JSON object.
     * 
     * @param urlString
     * @return
     * @throws Exception
     */
    public static JSONObject httpGetJson(String urlString) throws Exception {
        JSONObject jsonObject = null;
        String result = httpGetString(urlString);

        try {
            if (!result.isEmpty())
                jsonObject = new JSONObject(result);
            logger.debug("OK parsing response of " + urlString);
        } catch (JSONException e) {
            e.printStackTrace();
            logger.error("exception occurs parsing response of " + urlString);
            throw e;
        }

        return jsonObject;
    }

    /**
     * Crawl activities with given keyword and next page token.
     * 
     * @param keyword
     * @param nextToken
     * @return
     */
    public static JSONObject crawlActivitiesByKeyword(String token,
            String keyword, String language, String nextToken) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("query", keyword);
        params.put("language", language);
        if (nextToken != null) {
            params.put("pageToken", nextToken);
        }

        JSONObject jsonObject = null;
        params.put("key", token);
        String urlString = GpService.buildActivitiesSearch(params);
        jsonObject = httpGetJson(urlString);
        return jsonObject;
    }

    /**
     * Crawl activities with given userId and next page token.
     * 
     * @param userId
     * @param nextToken
     * @return
     * @throws Exception
     */
    public static JSONObject crawlActivitiesByUser(String token, String userId,
            String nextToken) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        if (nextToken != null) {
            params.put("pageToken", nextToken);
        }

        JSONObject jsonObject = null;
        params.put("key", token);
        String urlString = GpService.buildActivitiesList(userId, "public",
                params);
        jsonObject = httpGetJson(urlString);
        return jsonObject;
    }

    /**
     * Crawl user profile with given userId.
     * 
     * @param userId
     * @return
     */
    // public static JSONObject crawlProfileByUser(String userId) {
    // String urlString = GpService.buildPeopleGet(userId, null);
    // return httpGetJson(urlString);
    // }

    /**
     * 2015-07-09 shangqian Crawl user profile by given user name,such as 'BBC
     * News'.
     * 
     * @param: user name
     * @return: JSONObject
     * @throws Exception
     */
    public static JSONObject crawlProfileByUserName(String token,
            String userName) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("query", userName);
        params.put("maxResults", "1");

        JSONObject jsonObject = null;
        params.put("key", token);
        String urlString = GpService.buildPeopleSearch(params);
        jsonObject = httpGetJson(urlString);
        return jsonObject;
    }
    
    /**
     * 2015-10-08 shangqian Crawl user profile by given user id,such as '107045876535773972576'
     * 
     * @param: user id
     * @return: JSONObject
     * @throws Exception
     */
    public static JSONObject crawlProfileByUserId(String token,
            String userId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("query", userId);
        params.put("maxResults", "1");

        JSONObject jsonObject = null;
        params.put("key", token);
        String urlString = GpService.buildPeopleGet(userId, params);
        jsonObject = httpGetJson(urlString);

        return jsonObject;
    }
}
