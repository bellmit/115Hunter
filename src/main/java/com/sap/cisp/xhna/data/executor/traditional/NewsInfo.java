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

public class NewsInfo {
    private String title = "";
    private String abstractText = "";
    private String keywords = "";
    private String content = "";
    private String datePublished = "";
    private String dateModified = "";
    private String crawlTime = "";
    private String mediaName = "";
    private String publisher = "";
    private String url = "";
    private String author = "";
    private final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    private static Logger logger = LoggerFactory.getLogger(NewsInfo.class);

    public NewsInfo(String title, String abstractText, String keywords,
            String content, String datePublished, String dateModified,
            String crawlTime, String mediaName, String publisher, String url,
            String author) {
        super();
        this.title = title;
        this.abstractText = abstractText;
        this.keywords = keywords;
        this.content = content;
        this.datePublished = datePublished;
        this.dateModified = dateModified;
        this.crawlTime = crawlTime;
        this.mediaName = mediaName;
        this.publisher = publisher;
        this.url = url;
        this.author = author;
    }

    public NewsInfo() {
        super();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title.replace("\t", "  ");
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

    // Need verification
    public void setContent(String content) {
        this.content = content.replace("\t", "  ");
        this.content = content.replace("\n", "\\n");
        this.content = content.replace("\r", "\\r");
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(String datePublished) {
        this.datePublished = datePublished;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public String getCrawlTime() {
        return crawlTime;
    }

    public void setCrawlTime(String crawlTime) {
        this.crawlTime = crawlTime;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @SuppressWarnings("unchecked")
    public void save(Map<String, Object> ctx) {
        populateNewsInfo();
        ((List<String>) ctx.get("result")).add(JSON.toJSONString(this));
    }

    public void populateNewsInfo() {
        if (crawlTime == null || "".equals(crawlTime)) {
            this.crawlTime = sdf.format(Calendar.getInstance().getTime());
        }
        if (!datePublished.equalsIgnoreCase("")) {
            Date publishedTime = null;
            try {
                publishedTime = DateUtil.parseDate(datePublished);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                logger.error(
                        "ParseException during populate publish time in the news info.",
                        e);
            }
            this.datePublished = sdf.format(publishedTime);
        }
        if (!dateModified.equalsIgnoreCase("")) {
            Date modifiedTime = null;
            try {
                modifiedTime = DateUtil.parseDate(dateModified);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                logger.error(
                        "ParseException during populate modified time in the news info.",
                        e);
            }
            this.dateModified = sdf.format(modifiedTime);
        }
    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                title, abstractText, keywords, content, datePublished,
                dateModified, crawlTime, mediaName, publisher, url, author);
    }

}
