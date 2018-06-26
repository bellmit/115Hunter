package com.sap.cisp.xhna.data.executor.forum;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;



import com.alibaba.fastjson.JSON;

public class Post {
    private String threadTitle;
    private String source;
    private String id;
    private String url;
    private String content;
    private String replyToId;
    private String author;
    private String authorUrl;
    private String keywords;
    private String crawlTime;
    private String postDate;
    private String lastModifiedDate;
    private final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
//    private static Logger logger = LoggerFactory.getLogger(Post.class);

    public Post(String title, String source, String author, String authorUrl,String url, String content) {
        super();
        this.threadTitle = title;
//        this.time = time;
        this.source = source;
        this.url = url;
        this.content = content;
    }

    public Post() {
        super();
    }

    public String getTitle() {
        return threadTitle;
    }

    public void setTitle(String title) {
        this.threadTitle = title.replace("\t", "  ");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }
    
    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getCrawlTime() {
        return crawlTime;
    }

    public void setCrawlTime(String crawlTime) {
        this.crawlTime = crawlTime;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
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
        if (source == null || "".equals(source)) {
            this.source = "";
        }
        if (crawlTime == null || "".equals(crawlTime)) {
            this.crawlTime = sdf.format(Calendar.getInstance().getTime());
        }
        ((List<String>) ctx.get("result")).add(JSON.toJSONString(this));

    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", id, threadTitle, keywords, content, author,authorUrl, postDate, lastModifiedDate, crawlTime, replyToId,
                source, url);
    }

}
