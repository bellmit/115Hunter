package com.sap.cisp.xhna.data.executor.stream.iot.watertreatment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;
import com.sap.cisp.xhna.data.executor.stream.iot.IModelInfo;

public class SluiceState implements IModelInfo {
    private String sluiceId;
    private String timeStamp;
    private String state;
    private final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    public SluiceState(String sluiceId, String timeStamp, String state) {
        this.sluiceId = sluiceId;
        this.timeStamp = timeStamp;
        this.state = state;
    }

    public SluiceState(String js) {
        JSONObject vo = JSON.parseObject(js);
        try {
            this.sluiceId = Util.EscapeString(vo.getString("sluice_id"));
        } catch (Exception e) {
            this.sluiceId = "";
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
            this.state = Util.EscapeString(vo.getString("state"));
        } catch (Exception e) {
            this.state = "";
        }
    }

    public String getSluiceId() {
        return sluiceId;
    }

    public void setSluiceId(String sluiceId) {
        this.sluiceId = sluiceId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%s", sluiceId, timeStamp, state);
    }

    public String[] getAttributeValueList() {
        return new String[] { sluiceId, timeStamp, state };
    }

}
