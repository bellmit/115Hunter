package com.sap.cisp.xhna.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;


public class GplusAccountInfo {
	private String image_isDefault;
	private String image_url;
	private String kind;
	private String displayName;
	private String verified;
	private String plusOneCount;
	private String url;
	private String objectType;
	private String aboutMe;
	private String isPlusUser;
	private String cover_layout;
	private String cover_coverPhoto_width;
	private String cover_coverPhoto_url;
	private String cover_coverPhoto_height;
	private String cover_coverInfo_topImageOffset;
	private String cover_coverInfo_leftImageOffset;
	private String urls;
	private String tagline;
	private String etag;
	private String id;
	private String circledByCount;
	private String crawl_time;
	
	public GplusAccountInfo(String JsonString) {
		this.crawl_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
		
		JSONObject vo = JSON.parseObject(JsonString);
		try{this.image_isDefault = Util.EscapeString(vo.getJSONObject("image").getString("isDefault"));}catch(Exception e){this.image_isDefault = "";}
		try{this.image_url = Util.EscapeString(vo.getJSONObject("image").getString("url"));}catch(Exception e){this.image_url = "";}
		try{this.kind = Util.EscapeString(vo.getString("kind"));}catch(Exception e){this.kind = "";}
		try{this.displayName = Util.EscapeString(vo.getString("displayName"));}catch(Exception e){this.displayName = "";}
		try{this.verified = Util.EscapeString(vo.getString("verified"));}catch(Exception e){this.verified = "";}
		try{this.plusOneCount = Util.EscapeString(vo.getString("plusOneCount"));}catch(Exception e){this.plusOneCount = "";}
		try{this.url = Util.EscapeString(vo.getString("url"));}catch(Exception e){this.url = "";}
		try{this.objectType = Util.EscapeString(vo.getString("objectType"));}catch(Exception e){this.objectType = "";}
		try{this.aboutMe = Util.EscapeString(vo.getString("aboutMe"));}catch(Exception e){this.aboutMe = "";}
		try{this.isPlusUser = Util.EscapeString(vo.getString("isPlusUser"));}catch(Exception e){this.isPlusUser = "";}
		try{this.cover_layout = Util.EscapeString(vo.getJSONObject("cover").getString("layout"));}catch(Exception e){this.cover_layout = "";}
		try{this.cover_coverPhoto_width = Util.EscapeString(vo.getJSONObject("cover").getJSONObject("coverPhoto").getString("width"));}catch(Exception e){this.cover_coverPhoto_width = "";}
		try{this.cover_coverPhoto_url = Util.EscapeString(vo.getJSONObject("cover").getJSONObject("coverPhoto").getString("url"));}catch(Exception e){this.cover_coverPhoto_url = "";}
		try{this.cover_coverPhoto_height = Util.EscapeString(vo.getJSONObject("cover").getJSONObject("coverPhoto").getString("height"));}catch(Exception e){this.cover_coverPhoto_height = "";}
		try{this.cover_coverInfo_topImageOffset = Util.EscapeString(vo.getJSONObject("cover").getJSONObject("coverInfo").getString("topImageOffset"));}catch(Exception e){this.cover_coverInfo_topImageOffset = "";}
		try{this.cover_coverInfo_leftImageOffset = Util.EscapeString(vo.getJSONObject("cover").getJSONObject("coverInfo").getString("leftImageOffset"));}catch(Exception e){this.cover_coverInfo_leftImageOffset = "";}
		try{this.urls = Util.EscapeString(vo.getString("urls"));}catch(Exception e){this.urls = "";}
		try{this.tagline = Util.EscapeString(vo.getString("tagline"));}catch(Exception e){this.tagline = "";}
		try{this.etag = Util.EscapeString(vo.getString("etag"));}catch(Exception e){this.etag = "";}
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id = "";}
		try{this.circledByCount = Util.EscapeString(vo.getString("circledByCount"));}catch(Exception e){this.circledByCount = "";}
	}
	
	public String toString() {
		return String
				.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
						image_isDefault, image_url, kind, displayName,
						verified, plusOneCount, url, objectType, aboutMe,
						isPlusUser, cover_layout, cover_coverPhoto_width,
						cover_coverPhoto_url, cover_coverPhoto_height,
						cover_coverInfo_topImageOffset,
						cover_coverInfo_leftImageOffset, urls, tagline, etag,
						id, circledByCount, crawl_time);
	}
	public String[] getAttributeValueList() {
		return new String[]{image_isDefault, image_url, kind, displayName,
				verified, plusOneCount, url, objectType, aboutMe,
				isPlusUser, cover_layout, cover_coverPhoto_width,
				cover_coverPhoto_url, cover_coverPhoto_height,
				cover_coverInfo_topImageOffset,
				cover_coverInfo_leftImageOffset, urls, tagline, etag,
				id, circledByCount, crawl_time};
	}
}