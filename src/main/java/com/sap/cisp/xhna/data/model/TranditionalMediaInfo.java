package com.sap.cisp.xhna.data.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;

public class TranditionalMediaInfo {
	private String title;
	private String time;
	private String source;
	private String url;
	private String content;

	public TranditionalMediaInfo(String js) {
		JSONObject vo = JSON.parseObject(js);
		try{this.title = Util.EscapeString(vo.getString("title"));}catch(Exception e){this.title="";}
		try{this.time = Util.EscapeString(vo.getString("time"));}catch(Exception e){this.time="";}
		try{this.source = Util.EscapeString(vo.getString("source"));}catch(Exception e){this.source="";}
		try{this.url = Util.EscapeString(vo.getString("url"));}catch(Exception e){this.url="";}
		try{this.content = Util.EscapeString(vo.getString("content"));}catch(Exception e){this.content="";}
	}

	@Override
	public String toString() {
		return String.format("%s\t%s\t%s\t%s\t%s", title, content, time, url, source);
	}
	
	public String[] getAttributeValueList() {
		return new String[]{title, content, time, url, source};
	}
}
