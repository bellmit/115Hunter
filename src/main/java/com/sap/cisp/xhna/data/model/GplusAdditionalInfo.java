package com.sap.cisp.xhna.data.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;

public class GplusAdditionalInfo {
	private String id;
	private String crawlTime;
	private String replyCount;
	private String resharedCount;
	private String plusonersCount;
	private String mediaCount;

	public GplusAdditionalInfo(String js) {
		JSONObject vo = JSON.parseObject(js);
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id="";}
		try{this.crawlTime = Util.EscapeString(vo.getString("crawlTime"));}catch(Exception e){this.crawlTime="";}
		try{this.replyCount = Util.EscapeString(vo.getString("replyCount"));}catch(Exception e){this.replyCount="";}
		try{this.resharedCount = Util.EscapeString(vo.getString("resharedCount"));}catch(Exception e){this.resharedCount="";}
		try{this.plusonersCount = Util.EscapeString(vo.getString("plusonersCount"));}catch(Exception e){this.plusonersCount="";}
		try{this.mediaCount = Util.EscapeString(vo.getString("mediaCount"));}catch(Exception e){this.mediaCount="";}
	}

	@Override
	public String toString() {
		return String.format("%s\t%s\t%s\t%s\t%s\t%s", id, crawlTime, replyCount, resharedCount, plusonersCount, mediaCount);
	}
	
	public String[] getAttributeValueList() {
		return new String[]{id, crawlTime, replyCount, resharedCount, plusonersCount, mediaCount};
	}
}
