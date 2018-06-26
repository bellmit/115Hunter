package com.sap.cisp.xhna.data.executor.stream.iot.watertreatment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;
import com.sap.cisp.xhna.data.executor.stream.iot.IModelInfo;

public class SensorData implements IModelInfo {
    private String channelCode;
    private String timeStamp;
    private String value;
    private final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    public SensorData(String channelCode, String timeStamp, String value) {
        this.channelCode = channelCode;
        this.timeStamp = timeStamp;
        this.value = value;
    }

    public SensorData(String js) {
        JSONObject vo = JSON.parseObject(js);
        try {
            this.channelCode = Util.EscapeString(vo.getString("channelCode"));
        } catch (Exception e) {
            this.channelCode = "";
        }
        try {
            this.timeStamp = Util.EscapeString(vo.getString("timeStamp"));
            if(timeStamp.isEmpty()) {
                timeStamp = sdf.format(Calendar.getInstance().getTime());
            }
        } catch (Exception e) {
            this.timeStamp = sdf.format(Calendar.getInstance().getTime());
        }
        try {
            this.value = Util.EscapeString(vo.getString("value"));
        } catch (Exception e) {
            this.value = "";
        }
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%s", channelCode, timeStamp, value);
    }

    public String[] getAttributeValueList() {
        return new String[] { channelCode, timeStamp, value };
    }
}
