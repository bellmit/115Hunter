package com.sap.cisp.xhna.data.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;

public class GubaPostInfo {
    private String author;
    private String authorId;
    private String content;
    private String crawlTime;
    private String forwardCount;
    private String id;
    private String postDate;
    private String replyCount;
    private String replyToId;
    private String stockName;
    private String title;
    private String url;

	public GubaPostInfo(String js) {
		JSONObject vo = JSON.parseObject(js);
		try{this.author = Util.EscapeString(vo.getString("author"));}catch(Exception e){this.author="";}
		try{this.authorId = Util.EscapeString(vo.getString("authorId"));}catch(Exception e){this.authorId="";}
		try{this.content = Util.EscapeString(vo.getString("content"));}catch(Exception e){this.content="";}
		try{this.crawlTime = Util.EscapeString(vo.getString("crawlTime"));}catch(Exception e){this.crawlTime="";}
		try{this.forwardCount = Util.EscapeString(vo.getString("forwardCount"));}catch(Exception e){this.forwardCount="";}
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id="";}
		try{this.postDate = Util.EscapeString(vo.getString("postDate"));}catch(Exception e){this.postDate="";}
		try{this.replyCount = Util.EscapeString(vo.getString("replyCount"));}catch(Exception e){this.replyCount="";}
		try{this.replyToId = Util.EscapeString(vo.getString("replyToId"));}catch(Exception e){this.replyToId="";}
		try{this.stockName = Util.EscapeString(vo.getString("stockName"));}catch(Exception e){this.stockName="";}
		try{this.title = Util.EscapeString(vo.getString("title"));}catch(Exception e){this.title="";}
		try{this.url = Util.EscapeString(vo.getString("url"));}catch(Exception e){this.url="";}
	}

	@Override
	public String toString() {
		return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", author, authorId, content, crawlTime, forwardCount, id, postDate, replyCount, replyToId, title, url, stockName);
	}
	
	public String[] getAttributeValueList() {
		return new String[]{author, authorId, content, crawlTime, forwardCount, id, postDate, replyCount, replyToId, title, url, stockName};
	}
}
