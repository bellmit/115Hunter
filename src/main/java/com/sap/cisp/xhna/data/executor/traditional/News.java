package com.sap.cisp.xhna.data.executor.traditional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.sap.cisp.xhna.data.common.DateUtil;

public class News {
    private String title;
    private String time;
    private String source;
    private String url;
    private String content;
    private final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    private static Logger logger = LoggerFactory.getLogger(News.class);

    public News(String title, String time, String s, String url, String content) {
        super();
        this.title = title;
        this.time = time;
        this.source = s;
        this.url = url;
        this.content = content;
    }

    public News() {
        super();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title.replace("\t", "  ");
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String sOURCE) {
        source = sOURCE;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content.replace("\t", "  ");
        this.content = content.replace("\n", "\\n");
        this.content = content.replace("\r", "\\r");
    }

    @SuppressWarnings("unchecked")
    public void save(Map<String, Object> ctx) {
        if (time == null || "".equals(time)) {
            this.time = sdf.format(Calendar.getInstance().getTime());
        } else {
            Date publishedTime = null;
            try {
                publishedTime = DateUtil.parseDate(time);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                logger.error(
                        "ParseException during populate publish time in the news.",
                        e);
            }
            this.time = sdf.format(publishedTime);
        }
        if (source == null || "".equals(source)) {
            this.source = "";
        }
        ((List<String>) ctx.get("result")).add(JSON.toJSONString(this));

    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%s\t%s\t%s", title, content, time,
                source, url);
    }

}
