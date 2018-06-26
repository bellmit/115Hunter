package com.sap.cisp.xhna.data.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;

public class TranditionalAdditionalInfo {
	private String title;
	private String abstracts;
	private String keywords;
	private String content;
	private String datepublished;
	private String datemodified;
	private String crawltime;
	private String medianame;
	private String publisher;
	private String url;
	private String author;
	
	public TranditionalAdditionalInfo(String JsonString){
		JSONObject vo = JSON.parseObject(JsonString);
		try{this.title = Util.EscapeString(vo.getString("title"));}catch(Exception e){this.title="";}
		try{this.abstracts = Util.EscapeString(vo.getString("abstractText"));}catch(Exception e){this.abstracts="";}
		try{this.keywords = Util.EscapeString(vo.getString("keywords"));}catch(Exception e){this.keywords="";}
		try{this.content = Util.EscapeString(vo.getString("content"));}catch(Exception e){this.content="";}
		try{this.datepublished = Util.EscapeString(vo.getString("datePublished"));}catch(Exception e){this.datemodified="";}
		try{this.datemodified = Util.EscapeString(vo.getString("dateModified"));}catch(Exception e){this.datemodified="";}
		try{this.crawltime = Util.EscapeString(vo.getString("crawlTime"));}catch(Exception e){this.crawltime="";}
		try{this.medianame = Util.EscapeString(vo.getString("mediaName"));}catch(Exception e){this.medianame="";}
		try{this.publisher = Util.EscapeString(vo.getString("publisher"));}catch(Exception e){this.publisher="";}
		try{this.url = Util.EscapeString(vo.getString("url"));}catch(Exception e){this.url="";}
		try{this.author = Util.EscapeString(vo.getString("author"));}catch(Exception e){this.author="";}
	}
	
	@Override
	public String toString() {
		return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", title, abstracts, 
				keywords, content, datepublished, datemodified,crawltime, medianame, publisher,url, author );
	}
	
	public String[] getAttributeValueList() {
		return new String[]{title, abstracts, 
				keywords, content, datepublished, datemodified,crawltime, medianame, publisher,url,  author };
	}
}
