package com.sap.cisp.xhna.data.executor.googleplus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GpService {
    public static Logger logger = LoggerFactory.getLogger(GpService.class);
    private static List<String> api_keys = new ArrayList<String>();
    private static int key_index = 0;
    static {
        api_keys.add("AIzaSyD4V3WxWUv4XvGZd8oSVFv45uvD-OkkyIw");
        api_keys.add("AIzaSyCAume9d2DtikYdTE2jhpTtrjYE_rdqFm4");
        api_keys.add("AIzaSyD-v-U0-Eq1CiG3XNNpG8_EFwOEqKCa8pI");
        api_keys.add("AIzaSyDIm1oVCsiteXkZrVXQs3ICGkP7wkorkLQ");
        api_keys.add("AIzaSyADeNPfiJB55FVOr6qSq5fmIW69PCEwR4k");
        api_keys.add("AIzaSyBanFzxZDmJI1Ix20EhVm5aMUMfgCDuCZA");
        api_keys.add("AIzaSyCzFDBfKbFF6qa5c-5x51_Alg7uzTE3zkI");
        api_keys.add("AIzaSyAypQABLxb5oWvJj5t3XPFcUyZd68Oud9g");
        api_keys.add("AIzaSyCh_bE6zswo0ky11iQDB0GRk8StWzRLRnA");
        api_keys.add("AIzaSyDLSLG8ATae0b0azVPa5JuOLZQpZkmUiIM");
    }
    private static final boolean PRETTY_PRINT = false;

    // in case of other API_KEYs are out of limit
    public static final String SECURE_KEY = "AIzaSyAmx8zhVoRazSGSdXQ3eQJRTIaTNqOuwaE";

    // official google+ API
    private static final String PEOPLE_GET = "https://www.googleapis.com/plus/v1/people/{userId}?{query}";
    private static final String PEOPLE_SEARCH = "https://www.googleapis.com/plus/v1/people?{query}";
    private static final String PEOPLE_LIST_BY_ACTIVITY = "https://www.googleapis.com/plus/v1/activities/{activityId}/people/{collection}?{query}";

    private static final String ACTIVITIES_LIST = "https://www.googleapis.com/plus/v1/people/{userId}/activities/{collection}?{query}";
    private static final String ACTIVITIES_GET = "https://www.googleapis.com/plus/v1/activities/{activityId}?{query}";
    private static final String ACTIVITIES_SEARCH = "https://www.googleapis.com/plus/v1/activities?{query}";

    private static final String COMMENTS_LIST = "https://www.googleapis.com/plus/v1/activities/{activityId}/comments?{query}";
    private static final String COMMENTS_GET = "https://www.googleapis.com/plus/v1/comments/{commentId}?{query}";

    // self google+ API
    private static final String CIRCLES_GET = "https://plus.google.com/u/0/_/socialgraph/lookup/visible/?{query}";

    // build urls
    public static String buildPeopleGet(String userId,
            Map<String, String> params) {
        Map<String, String> pairs = new HashMap<String, String>();
        pairs.put("userId", userId);
        pairs.put("query", buildQueryString(params));
        return preparedURL(PEOPLE_GET, pairs);
    }

    public static String buildPeopleSearch(Map<String, String> params) {
        Map<String, String> pairs = new HashMap<String, String>();
        pairs.put("query", buildQueryString(params));
        return preparedURL(PEOPLE_SEARCH, pairs);
    }

    public static String buildPeopleListByActivity(String activityId,
            String collection, Map<String, String> params) {
        Map<String, String> pairs = new HashMap<String, String>();
        pairs.put("activityId", activityId);
        pairs.put("collection", collection);
        pairs.put("query", buildQueryString(params));
        return preparedURL(PEOPLE_LIST_BY_ACTIVITY, pairs);
    }

    public static String buildActivitiesList(String userId, String collection,
            Map<String, String> params) {
        Map<String, String> pairs = new HashMap<String, String>();
        pairs.put("userId", userId);
        pairs.put("collection", collection);
        pairs.put("query", buildQueryString(params));
        return preparedURL(ACTIVITIES_LIST, pairs);
    }

    public static String buildActivitiesGet(String activityId,
            Map<String, String> params) {
        Map<String, String> pairs = new HashMap<String, String>();
        pairs.put("activityId", activityId);
        pairs.put("query", buildQueryString(params));
        return preparedURL(ACTIVITIES_GET, pairs);
    }

    public static String buildActivitiesSearch(Map<String, String> params) {
        params.put("maxResults", "20");
        Map<String, String> pairs = new HashMap<String, String>();
        pairs.put("query", buildQueryString(params));
        return preparedURL(ACTIVITIES_SEARCH, pairs);
    }

    public static String buildCommentsList(String activityId,
            Map<String, String> params) {
        Map<String, String> pairs = new HashMap<String, String>();
        pairs.put("activityId", activityId);
        pairs.put("query", buildQueryString(params));
        return preparedURL(COMMENTS_LIST, pairs);
    }

    public static String buildCommentGet(String commentId,
            Map<String, String> params) {
        Map<String, String> pairs = new HashMap<String, String>();
        pairs.put("commentId", commentId);
        pairs.put("query", buildQueryString(params));
        return preparedURL(COMMENTS_GET, pairs);
    }

    public static String circlesGet(Map<String, String> params) {
        Map<String, String> pairs = new HashMap<String, String>();
        pairs.put("query", buildQueryString(params));
        return preparedURL(CIRCLES_GET, pairs);
    }

    /**
     * Build a query string with given map. Consider to build partial response,
     * i.e : https://www.googleapis.com/plus/v1/activities/
     * z12gtjhq3qn2xxl2o224exwiqruvtda0i
     * ?fields=url,object(content,attachments/url)&key=YOUR-API-KEY
     * 
     * @param params
     * @return
     */
    private static String buildQueryString(Map<String, String> params) {
        StringBuffer query = new StringBuffer("");
        if (params != null && params.size() > 0) {
            String[] keys = params.keySet().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                String encodeString = "";
                try {
                    encodeString = URLEncoder.encode(params.get(keys[i]),
                            "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                query.append(keys[i] + "=" + encodeString + "&");
            }
        }

        logger.info("Query String --> {}", query);
        if (params != null && params.get("key") != null) {
            query.deleteCharAt(query.length() - 1);
        } else {
            query.append("key=" + nextAPIKey());
        }

        query.append("&prettyPrint=" + PRETTY_PRINT);

        return query.toString();
    }

    /**
     * Prepare url.
     * 
     * @param url
     * @param pairs
     * @return
     */
    private static String preparedURL(String url, Map<String, String> pairs) {
        String[] keys = pairs.keySet().toArray(new String[0]);
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            url = url.replace("{" + key + "}", pairs.get(key));
        }
        logger.info("GooglePlus prepared URL ---> {}", url);
        return url;
    }

    private static synchronized String nextAPIKey() {
        int current = key_index;
        key_index = (key_index + 1) % (api_keys.size());
        return api_keys.get(current);
    }

}
