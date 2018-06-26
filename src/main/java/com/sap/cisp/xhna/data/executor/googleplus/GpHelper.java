package com.sap.cisp.xhna.data.executor.googleplus;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.sap.cisp.xhna.data.common.DateUtil;

public class GpHelper {

    /**
     * Parse a string to date. string example: "2014-12-15T09:27:30.609Z"
     * 
     * @param timeStr
     * @return
     */
    public static Date parseStrToDate(String timeStr) {
        timeStr = timeStr.replace("T", " ").replace("Z", "");
        return DateUtil.stringToDate(timeStr, "yyyy-MM-dd HH:mm:ss.SSS");
    }

    /**
     * Convert rich text of activity item to plain text.
     * 
     * @param jsonObject
     * @return
     */
    public static void plainizeRichText(JSONObject jsonObject) {
        try {
            JSONObject object = jsonObject.getJSONObject("object");
            object.put("content",
                    object.getString("content").replaceAll("<[^>]*>", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
