package com.sap.cisp.xhna.data.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;

public class ForumPostInfo {
    private String title;
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

	public ForumPostInfo(String js) {
		JSONObject vo = JSON.parseObject(js);
		try{this.title = Util.EscapeString(vo.getString("title"));}catch(Exception e){this.title="";}
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id="";}
		try{this.source = Util.EscapeString(vo.getString("source"));}catch(Exception e){this.source="";}
		try{this.url = Util.EscapeString(vo.getString("url"));}catch(Exception e){this.url="";}
		try{this.content = Util.EscapeString(vo.getString("content"));}catch(Exception e){this.content="";}
		try{this.replyToId = Util.EscapeString(vo.getString("replyToId"));}catch(Exception e){this.replyToId="";}
		try{this.keywords = Util.EscapeString(vo.getString("keywords"));}catch(Exception e){this.keywords="";}
		try{this.crawlTime = Util.EscapeString(vo.getString("crawlTime"));}catch(Exception e){this.crawlTime="";}
		try{this.author = Util.EscapeString(vo.getString("author"));}catch(Exception e){this.author="";}
		try{this.authorUrl = Util.EscapeString(vo.getString("authorUrl"));}catch(Exception e){this.authorUrl="";}
		try{this.postDate = Util.EscapeString(vo.getString("postDate"));}catch(Exception e){this.postDate="";}
		try{this.lastModifiedDate = Util.EscapeString(vo.getString("lastModifiedDate"));}catch(Exception e){this.lastModifiedDate="";}
	}

	@Override
	public String toString() {
		return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", title, id, content, author, authorUrl, keywords, postDate, lastModifiedDate, replyToId, crawlTime, url, source);
	}
	
	public String[] getAttributeValueList() {
		return new String[]{title, id, content, author, authorUrl, keywords, postDate, lastModifiedDate, replyToId, crawlTime, url, source};
	}
}
