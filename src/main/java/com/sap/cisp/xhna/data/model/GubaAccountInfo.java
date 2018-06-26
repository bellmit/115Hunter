package com.sap.cisp.xhna.data.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;

public class GubaAccountInfo {
    private String id;
    private String name;
    private String imageUrl;
    private String influence;
    private String totalVisit;
    private String todayVisit;
    private String fansCount;
    private String logonDate;
    private String followingCount;
    private String stockCount;

	public GubaAccountInfo(String js) {
		JSONObject vo = JSON.parseObject(js);
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id="";}
		try{this.name = Util.EscapeString(vo.getString("name"));}catch(Exception e){this.name="";}
		try{this.imageUrl = Util.EscapeString(vo.getString("imageUrl"));}catch(Exception e){this.imageUrl="";}
		try{this.influence = Util.EscapeString(vo.getString("influence"));}catch(Exception e){this.influence="";}
		try{this.totalVisit = Util.EscapeString(vo.getString("totalVisit"));}catch(Exception e){this.totalVisit="";}
		try{this.todayVisit = Util.EscapeString(vo.getString("todayVisit"));}catch(Exception e){this.todayVisit="";}
		try{this.fansCount = Util.EscapeString(vo.getString("fansCount"));}catch(Exception e){this.fansCount="";}
		try{this.logonDate = Util.EscapeString(vo.getString("logonDate"));}catch(Exception e){this.logonDate="";}
		try{this.followingCount = Util.EscapeString(vo.getString("followingCount"));}catch(Exception e){this.followingCount="";}
		try{this.stockCount = Util.EscapeString(vo.getString("stockCount"));}catch(Exception e){this.stockCount="";}
	}

	@Override
	public String toString() {
		return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", id, name, imageUrl, influence, totalVisit, todayVisit, fansCount, logonDate, followingCount, stockCount);
	}
	
	public String[] getAttributeValueList() {
		return new String[]{id, name, imageUrl, influence, totalVisit, todayVisit, fansCount, logonDate, followingCount, stockCount};
	}
}
