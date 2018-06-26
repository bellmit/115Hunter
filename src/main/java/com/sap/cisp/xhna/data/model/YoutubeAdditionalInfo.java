package com.sap.cisp.xhna.data.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;

public class YoutubeAdditionalInfo {
	private String id;
	private String crawlTime;
	private String commentCount;
	private String viewCount;
	private String favoriteCount;
	private String dislikeCount;
	private String likeCount;

	public YoutubeAdditionalInfo(String js) {
		JSONObject vo = JSON.parseObject(js);
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id="";}
		try{this.crawlTime = Util.EscapeString(vo.getString("crawlTime"));}catch(Exception e){this.crawlTime="";}
		try{this.commentCount = Util.EscapeString(vo.getString("commentCount"));}catch(Exception e){this.commentCount="";}
		try{this.viewCount = Util.EscapeString(vo.getString("viewCount"));}catch(Exception e){this.viewCount="";}
		try{this.favoriteCount = Util.EscapeString(vo.getString("favoriteCount"));}catch(Exception e){this.favoriteCount="";}
		try{this.dislikeCount = Util.EscapeString(vo.getString("dislikeCount"));}catch(Exception e){this.dislikeCount="";}
		try{this.likeCount = Util.EscapeString(vo.getString("likeCount"));}catch(Exception e){this.likeCount="";}
	}

	@Override
	public String toString() {
		return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s", id, crawlTime, commentCount, viewCount, favoriteCount, dislikeCount, likeCount);
	}
	
	public String[] getAttributeValueList() {
		return new String[]{id, crawlTime, commentCount, viewCount, favoriteCount, dislikeCount, likeCount};
	}
}
