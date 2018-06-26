package com.sap.cisp.xhna.data.executor.forum.stock;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class GubaAccountInfo {

	private String id;
	private String name;
	private String imageUrl;
	private String stockCount;
	private String followingCount;
	private String fansCount;
	private String influence;
	private String logonDate;
	private String totalVisit;
	private String todayVisit;

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String crawlTime = sdf.format(Calendar.getInstance().getTime());
	
	
	public GubaAccountInfo(String uid, String name, String url, 
			String stockCount, String followingCount, String fansCount) {
		this.id = uid;
		this.name = name;
		this.imageUrl = url;
		this.stockCount = stockCount;
		this.followingCount = followingCount;
		this.fansCount = fansCount;
	}

	@SuppressWarnings("unchecked")
	public void save(Map<String, Object> ctx) {
        if (crawlTime == null || "".equals(crawlTime)) {
            this.crawlTime = sdf.format(Calendar.getInstance().getTime());
        }
		System.out.println("GubaAccountInfo--save");
		System.out.println(this.crawlTime);
		((List<String>) ctx.get("result")).add(JSON.toJSONString(this));
	}

	@Override
	public String toString() {
		return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", 
				id, name, imageUrl, influence, totalVisit, todayVisit, 
				fansCount, logonDate, followingCount,stockCount, crawlTime);
	}

	public String getId() {
		return id;
	}

	public void setUid(String uid) {
		this.id = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getStockCount() {
		return stockCount;
	}

	public void setStockCount(String stockCount) {
		this.stockCount = stockCount;
	}

	public String getFollowingCount() {
		return followingCount;
	}

	public void setFollowingCount(String followingCount) {
		this.followingCount = followingCount;
	}

	public String getFansCount() {
		return fansCount;
	}

	public void setFansCount(String fansCount) {
		this.fansCount = fansCount;
	}

	public String getInfluence() {
		return influence;
	}

	public void setInfluence(String influence) {
		this.influence = influence;
	}

	public String getLogonDate() {
		return logonDate;
	}

	public void setLogonDate(String logonDate) {
		this.logonDate = logonDate;
	}

	public String getTotalVisit() {
		return totalVisit;
	}

	public void setTotalVisit(String totalVisit) {
		this.totalVisit = totalVisit;
	}

	public String getTodayVisit() {
		return todayVisit;
	}

	public void setTodayVisit(String todayVisit) {
		this.todayVisit = todayVisit;
	}
	
	public void setCrawlTime(String time) {
		this.crawlTime = time;
	}
	
	public String getCrawlTime() {
		return this.crawlTime;
	}

}
