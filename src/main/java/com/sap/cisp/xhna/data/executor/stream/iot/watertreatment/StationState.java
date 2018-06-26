package com.sap.cisp.xhna.data.executor.stream.iot.watertreatment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;
import com.sap.cisp.xhna.data.executor.stream.iot.IModelInfo;

public class StationState implements IModelInfo {
    private String stationId;
    private String timeStamp;
    private String state;
    private final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    public StationState(String stationId, String timeStamp, String state) {
        this.stationId = stationId;
        this.timeStamp = timeStamp;
        this.state = state;
    }

    public StationState(String js) {
        JSONObject vo = JSON.parseObject(js);
        try {
            this.stationId = Util.EscapeString(vo.getString("station_id"));
        } catch (Exception e) {
            this.stationId = "";
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

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
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
        return String.format("%s\t%s\t%s", stationId, timeStamp, state);
    }

    public String[] getAttributeValueList() {
        return new String[] { stationId, timeStamp, state };
    }

}
