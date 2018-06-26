package com.sap.cisp.xhna.data.executor.stream.iot.watertreatment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;
import com.sap.cisp.xhna.data.executor.stream.iot.IModelInfo;

/*
 * CREATE COLUMN TABLE "IOT_WATERTREATMENT_REAL"."ALARM" ("STATION_ID" INTEGER CS_INT NOT NULL ,
 "SENSOR_ID" INTEGER CS_INT NOT NULL ,
 "STATE" VARCHAR(32),
 "DESCRIPTION" VARCHAR(50),
 "SEVERITY" VARCHAR(32),
 "TYPE" VARCHAR(32),
 "TIMESTAMP" LONGDATE CS_LONGDATE,
 "VALUE" VARCHAR(50)) UNLOAD PRIORITY 5 AUTO MERGE 
 */
public class Alarm implements IModelInfo {
    private String stationId;
    private String sensorId;
    private String state;
    private String description;
    private String severity;
    private String type;
    private String timeStamp;
    private String value;
    private final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    private static Logger logger = LoggerFactory.getLogger(Alarm.class);

    
    public Alarm() {
    }

    public Alarm(String stationId, String sensorId, String state,
            String description, String severity, String type, String timeStamp,
            String value) {
        super();
        this.stationId = stationId;
        this.sensorId = sensorId;
        this.state = state;
        this.description = description;
        this.severity = severity;
        this.type = type;
        this.timeStamp = timeStamp;
        this.value = value;
    }

    public Alarm(String js) {
        logger.info("Covert alarm json : {}", js);
        JSONObject vo = JSON.parseObject(js);
        try {
            this.stationId = Util.EscapeString(vo.getString("station_id"));
        } catch (Exception e) {
            this.stationId = "";
        }
        try {
            this.sensorId = Util.EscapeString(vo.getString("sensor_id"));
        } catch (Exception e) {
            this.sensorId = "";
        }
        try {
            this.state = Util.EscapeString(vo.getString("state"));
        } catch (Exception e) {
            this.state = "";
        }
        try {
            this.description = Util.EscapeString(vo.getString("description"));
        } catch (Exception e) {
            this.description = "";
        }
        try {
            this.severity = Util.EscapeString(vo.getString("severity"));
        } catch (Exception e) {
            this.severity = "";
        }
        try {
            this.type = Util.EscapeString(vo.getString("type"));
        } catch (Exception e) {
            this.type = "";
        }
        try {
            this.timeStamp = Util.EscapeString(vo.getString("timestamp"));
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

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
        return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", stationId,
                sensorId, state, description, severity, type, timeStamp, value);
    }

    public String[] getAttributeValueList() {
        return new String[] {stationId, sensorId, state, description,
                severity, type, timeStamp, value };
    }
}
