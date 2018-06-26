package com.sap.cisp.xhna.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;


public class FacebookAccountInfo {
	private String id;
	private String about;
	private String affiliation;
	
	private String awards;
	private String can_post;
	private String category;
	
	private String checkins;
	private String company_overview;
	
	private String cover_cover_id;
	private String cover_offset_x;
	private String cover_offset_y;
	private String cover_source;
	private String cover_id;
	
	private String description;
	private String founded;
	private String has_added_app;
	
	private String is_community_page;
	private String is_published;
	private String likes;
	private String link;
	
	private String location_city;
	private String location_country;
	private String location_latitude;
	private String location_longtitude;
	private String location_street;
	private String location_state;
	private String location_zip;
	
	private String mission;
	private String name;
	
	private String parking_lot;
	private String parking_street;
	private String parking_valet;
	
	private String phone;
	private String talking_about_count;
	private String username;			
	private String website;		
	private String were_here_count;
	private String crawl_time;
	
	public FacebookAccountInfo(String JsonString) {
		this.crawl_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
		
		JSONObject vo = JSON.parseObject(JsonString);
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id="";}
		try{this.about = Util.EscapeString(vo.getString("about"));}catch(Exception e){this.about="";}
		try{this.affiliation = Util.EscapeString(vo.getString("affiliation"));}catch(Exception e){this.affiliation="";}
		
		try{this.awards= Util.EscapeString(vo.getString("awards"));}catch(Exception e){this.awards="";}
		try{this.can_post = Util.EscapeString(vo.getString("can_post"));}catch(Exception e){this.can_post="";}
		try{this.category = Util.EscapeString(vo.getString("category"));}catch(Exception e){this.category="";}
		
		try{this.checkins = Util.EscapeString(vo.getString("checkins"));}catch(Exception e){this.checkins="";}
		try{this.company_overview = Util.EscapeString(vo.getString("company_overview"));}catch(Exception e){this.company_overview="";}
		
		try{this.cover_cover_id = Util.EscapeString(vo.getJSONObject("cover").getString("cover_id"));}catch(Exception e){this.cover_cover_id = "";}
		try{this.cover_offset_x = Util.EscapeString(vo.getJSONObject("cover").getString("offset_x"));}catch(Exception e){this.cover_offset_x = "";}
		try{this.cover_offset_y = Util.EscapeString(vo.getJSONObject("cover").getString("offset_y"));}catch(Exception e){this.cover_offset_y = "";}
		try{this.cover_source = Util.EscapeString(vo.getJSONObject("cover").getString("source"));}catch(Exception e){this.cover_source = "";}
		try{this.cover_id = Util.EscapeString(vo.getJSONObject("cover").getString("id"));}catch(Exception e){this.cover_id = "";}
		
		try{this.description = Util.EscapeString(vo.getString("description"));}catch(Exception e){this.description="";}
		try{this.founded = Util.EscapeString(vo.getString("founded"));}catch(Exception e){this.founded="";}
		try{this.has_added_app = Util.EscapeString(vo.getString("has_added_app"));}catch(Exception e){this.has_added_app="";}
		
		try{this.is_community_page = Util.EscapeString(vo.getString("is_community_page"));}catch(Exception e){this.is_community_page="";}
		try{this.is_published = Util.EscapeString(vo.getString("is_published"));}catch(Exception e){this.is_published="";}
		try{this.likes = Util.EscapeString(vo.getString("likes"));}catch(Exception e){this.likes="";}
		try{this.link = Util.EscapeString(vo.getString("link"));}catch(Exception e){this.link="";}
		
		try{this.location_city = Util.EscapeString(vo.getJSONObject("location").getString("city"));}catch(Exception e){this.location_city = "";}
		try{this.location_country = Util.EscapeString(vo.getJSONObject("location").getString("country"));}catch(Exception e){this.location_country = "";}
		try{this.location_latitude = Util.EscapeString(vo.getJSONObject("location").getString("latitude"));}catch(Exception e){this.location_latitude = "";}
		try{this.location_longtitude = Util.EscapeString(vo.getJSONObject("location").getString("longtitude"));}catch(Exception e){this.location_longtitude = "";}
		try{this.location_street = Util.EscapeString(vo.getJSONObject("location").getString("street"));}catch(Exception e){this.location_street = "";}
		try{this.location_state = Util.EscapeString(vo.getJSONObject("location").getString("state"));}catch(Exception e){this.location_state = "";}
		try{this.location_zip = Util.EscapeString(vo.getJSONObject("location").getString("zip"));}catch(Exception e){this.location_zip = "";}
		
		try{this.mission = Util.EscapeString(vo.getString("mission"));}catch(Exception e){this.mission="";}
		try{this.name = Util.EscapeString(vo.getString("name"));}catch(Exception e){this.name="";}
		
		try{this.parking_lot = Util.EscapeString(vo.getJSONObject("parking").getString("lot"));}catch(Exception e){this.parking_lot = "";}
		try{this.parking_street = Util.EscapeString(vo.getJSONObject("parking").getString("street"));}catch(Exception e){this.parking_street = "";}
		try{this.parking_valet = Util.EscapeString(vo.getJSONObject("parking").getString("valetl"));}catch(Exception e){this.parking_valet = "";}
		
		try{this.phone = Util.EscapeString(vo.getString("phone"));}catch(Exception e){this.phone="";}
		try{this.talking_about_count = Util.EscapeString(vo.getString("talking_about_count"));}catch(Exception e){this.talking_about_count="";}
		try{this.username = Util.EscapeString(vo.getString("username"));}catch(Exception e){this.username="";}
		try{this.website = Util.EscapeString(vo.getString("website"));}catch(Exception e){this.website="";}
		try{this.were_here_count = Util.EscapeString(vo.getString("were_here_count"));}catch(Exception e){this.were_here_count="";}
	}
	
	public String toString() {
		return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
				id, about, affiliation, 
				awards, can_post, category, 
				checkins, company_overview, 
				cover_cover_id, cover_offset_x, cover_offset_y, cover_source, cover_id, 
				description, founded, has_added_app, 
				is_community_page, is_published, likes, link, 
				location_city, location_country, location_latitude, location_longtitude, location_street, location_state, location_zip, 
				mission, name,
				parking_lot, parking_street, parking_valet, 
				phone, talking_about_count,	username, website, were_here_count, crawl_time);
	}
	public String[] getAttributeValueList() {
		return new String[]{id, about, affiliation, 
				awards, can_post, category, 
				checkins, company_overview, 
				cover_cover_id, cover_offset_x, cover_offset_y, cover_source, cover_id, 
				description, founded, has_added_app, 
				is_community_page, is_published, likes, link, 
				location_city, location_country, location_latitude, location_longtitude, location_street, location_state, location_zip, 
				mission, name,
				parking_lot, parking_street, parking_valet, 
				phone, talking_about_count,	username, website, were_here_count, crawl_time};
	}
}