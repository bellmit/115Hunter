package com.sap.cisp.xhna.data.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;

public class TwitterAdditionalInfo {
	private String id;
	private String crawlTime;
	private String replyCount;
	private String retweetCount;
	private String favoriteCount;
	private String mediaCount;

	public TwitterAdditionalInfo(String JsonString) {
		JSONObject vo = JSON.parseObject(JsonString);
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id="";}
		try{this.crawlTime = Util.EscapeString(vo.getString("crawlTime"));}catch(Exception e){this.crawlTime="";}
		try{this.replyCount = Util.EscapeString(vo.getString("replyCount"));}catch(Exception e){this.replyCount="";}
		try{this.retweetCount = Util.EscapeString(vo.getString("retweetCount"));}catch(Exception e){this.retweetCount="";}
		try{this.favoriteCount = Util.EscapeString(vo.getString("favoriteCount"));}catch(Exception e){this.favoriteCount="";}
		try{this.mediaCount = Util.EscapeString(vo.getString("mediaCount"));}catch(Exception e){this.mediaCount="";}
	}

	@Override
	public String toString() {
		return String.format("%s\t%s\t%s\t%s\t%s\t%s", id, crawlTime, replyCount, retweetCount, favoriteCount, mediaCount);
	}
	
	public String[] getAttributeValueList() {
		return new String[]{id, crawlTime, replyCount, retweetCount, favoriteCount, mediaCount};
	}
}
