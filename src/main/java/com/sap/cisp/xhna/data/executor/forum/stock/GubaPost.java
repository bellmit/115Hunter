package com.sap.cisp.xhna.data.executor.forum.stock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class GubaPost {
    private String title;
    private String postId;
    private String url;
    private String author;
    private String authorId;
    private String content;
    private String replyToId;
    private String crawlTime;
    private String postDate;
    private String replyCount;
    private String forwardCount;
    private String stockName;
    
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public GubaPost(String title, String author, String authorId,String url, String content) {
        this.title = title;
        this.author = author;
        this.authorId = authorId;
        this.url = url;
        this.content = content;
    }

    public GubaPost() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title.replace("\t", "  ");
    }

    public String getId() {
        return postId;
    }
   
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public void setId(String id) {
        this.postId = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content.replace("\t", "  ");
        this.content = content.replace("\n", "\\n");
        this.content = content.replace("\r", "\\r");
    }
    
    public String getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
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

    public String getReplyCount()
    {
    	return replyCount;
    }
    
    public void setReplyCount(String replyCount)
    {
    	this.replyCount = replyCount;
    }
    
    public String getForwardCount()
    {
    	return forwardCount;
    }
    
    public void setForwardCount(String forwardCount)
    {
    	this.forwardCount = forwardCount;
    }
    
    public String getStockName()
    {
    	return stockName;
    }
    
    public void setStockName(String stockName)
    {
    	this.stockName = stockName;
    }
    
    @SuppressWarnings("unchecked")
    public void save(Map<String, Object> ctx) {
        if (crawlTime == null || "".equals(crawlTime)) {
            this.crawlTime = sdf.format(Calendar.getInstance().getTime());
        }
        ((List<String>) ctx.get("result")).add(JSON.toJSONString(this));
    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", 
        		postId, title, url, author, authorId,content,replyToId,crawlTime,postDate,replyCount,forwardCount);
    }
}
