package com.sap.cisp.xhna.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;
public class TwitterAccountInfo {
	private String contributors_enabled;    
	private String created_at;              
	private String default_profile;         
	private String default_profile_image;   
	private String description;             
	private String entities_url_urls;       
	private String entities_description_urls;       
	private String favourites_count;        
	private String follow_request_sent;     
	private String following_;             
	private String followers_count;         
	private String friends_count;           
	private String geo_enabled;             
	private String id;                      
	private String id_str;                  
	private String is_translator;           
	private String lang;                    
	private String listed_count;            
	private String location;                
	private String name;                    
	private String notifications;           
	private String profile_background_color;        
	private String profile_background_image_url;    
	private String profile_background_image_url_http;       
	private String profile_background_tile;
	private String profile_banner_url;      
	private String profile_image_url;       
	private String profile_image_url_https; 
	private String profile_link_color;      
	private String profile_sidebar_border_color;    
	private String profile_sidebar_fill_color;      
	private String profile_text_color;      
	private String profile_use_background_image;    
	private String protected_;               
	private String screen_name;             
	private String show_all_inline_media;   
	private String status;                  
	private String statuses_count;          
	private String time_zone;               
	private String url;                    
	private String utc_offset;              
	private String verified;                
	private String withheld_in_countries;   
	private String withheld_scope;
	private String crawl_time;
	
	public TwitterAccountInfo(String JsonString){
		this.crawl_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
		
		JSONObject vo = JSON.parseObject(JsonString);
		try{this.contributors_enabled = Util.EscapeString(vo.getString("contributors_enabled"));}catch(Exception e){this.contributors_enabled = "";}
		try{this.created_at = Util.EscapeString(vo.getString("created_at"));}catch(Exception e){this.created_at = "";}
		try{this.default_profile = Util.EscapeString(vo.getString("default_profile"));}catch(Exception e){this.default_profile = "";}
		try{this.default_profile_image = Util.EscapeString(vo.getString("default_profile_image"));}catch(Exception e){this.default_profile_image = "";}
		try{this.description = Util.EscapeString(vo.getString("description"));}catch(Exception e){this.description = "";}
		try{this.entities_url_urls = Util.EscapeString(vo.getJSONObject("entities").getJSONObject("url").getString("urls"));}catch(Exception e){this.entities_url_urls = "";}
		try{this.entities_description_urls = Util.EscapeString(vo.getJSONObject("entities").getJSONObject("description").getString("urls"));}catch(Exception e){this.entities_description_urls = "";}
		try{this.favourites_count = Util.EscapeString(vo.getString("favourites_count"));}catch(Exception e){this.favourites_count = "";}
		try{this.follow_request_sent = Util.EscapeString(vo.getString("follow_request_sent"));}catch(Exception e){this.follow_request_sent = "";}
		try{this.following_ = Util.EscapeString(vo.getString("following_"));}catch(Exception e){this.following_ = "";}
		try{this.followers_count = Util.EscapeString(vo.getString("followers_count"));}catch(Exception e){this.followers_count = "";}
		try{this.friends_count = Util.EscapeString(vo.getString("friends_count"));}catch(Exception e){this.friends_count = "";}
		try{this.geo_enabled = Util.EscapeString(vo.getString("geo_enabled"));}catch(Exception e){this.geo_enabled = "";}
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id = "";}
		try{this.id_str = Util.EscapeString(vo.getString("id_str"));}catch(Exception e){this.id_str = "";}
		try{this.is_translator = Util.EscapeString(vo.getString("is_translator"));}catch(Exception e){this.is_translator = "";}
		try{this.lang = Util.EscapeString(vo.getString("lang"));}catch(Exception e){this.lang = "";}
		try{this.listed_count = Util.EscapeString(vo.getString("listed_count"));}catch(Exception e){this.listed_count = "";}
		try{this.location = Util.EscapeString(vo.getString("location"));}catch(Exception e){this.location = "";}
		try{this.name = Util.EscapeString(vo.getString("name"));}catch(Exception e){this.name = "";}
		try{this.notifications = Util.EscapeString(vo.getString("notifications"));}catch(Exception e){this.notifications = "";}
		try{this.profile_background_color = Util.EscapeString(vo.getString("profile_background_color"));}catch(Exception e){this.profile_background_color = "";}
		try{this.profile_background_image_url = Util.EscapeString(vo.getString("profile_background_image_url"));}catch(Exception e){this.profile_background_image_url = "";}
		try{this.profile_background_image_url_http = Util.EscapeString(vo.getString("profile_background_image_url_http"));}catch(Exception e){this.profile_background_image_url_http = "";}
		try{this.profile_background_tile = Util.EscapeString(vo.getString("profile_background_tile"));}catch(Exception e){this.profile_background_tile = "";}
		try{this.profile_banner_url = Util.EscapeString(vo.getString("profile_banner_url"));}catch(Exception e){this.profile_banner_url = "";}
		try{this.profile_image_url = Util.EscapeString(vo.getString("profile_image_url"));}catch(Exception e){this.profile_image_url = "";}
		try{this.profile_image_url_https = Util.EscapeString(vo.getString("profile_image_url_https"));}catch(Exception e){this.profile_image_url_https = "";}
		try{this.profile_link_color = Util.EscapeString(vo.getString("profile_link_color"));}catch(Exception e){this.profile_link_color = "";}
		try{this.profile_sidebar_border_color = Util.EscapeString(vo.getString("profile_sidebar_border_color"));}catch(Exception e){this.profile_sidebar_border_color = "";}
		try{this.profile_sidebar_fill_color = Util.EscapeString(vo.getString("profile_sidebar_fill_color"));}catch(Exception e){this.profile_sidebar_fill_color = "";}
		try{this.profile_text_color = Util.EscapeString(vo.getString("profile_text_color"));}catch(Exception e){this.profile_text_color = "";}
		try{this.profile_use_background_image = Util.EscapeString(vo.getString("profile_use_background_image"));}catch(Exception e){this.profile_use_background_image = "";}
		try{this.protected_ = Util.EscapeString(vo.getString("protected_"));}catch(Exception e){this.protected_ = "";}
		try{this.screen_name = Util.EscapeString(vo.getString("screen_name"));}catch(Exception e){this.screen_name = "";}
		try{this.show_all_inline_media = Util.EscapeString(vo.getString("show_all_inline_media"));}catch(Exception e){this.show_all_inline_media = "";}
		try{this.status = Util.EscapeString(vo.getString("status"));}catch(Exception e){this.status = "";}
		try{this.statuses_count = Util.EscapeString(vo.getString("statuses_count"));}catch(Exception e){this.statuses_count = "";}
		try{this.time_zone = Util.EscapeString(vo.getString("time_zone"));}catch(Exception e){this.time_zone = "";}
		try{this.url = Util.EscapeString(vo.getString("url"));}catch(Exception e){this.url = "";}
		try{this.utc_offset = Util.EscapeString(vo.getString("utc_offset"));}catch(Exception e){this.utc_offset = "";}
		try{this.verified = Util.EscapeString(vo.getString("verified"));}catch(Exception e){this.verified = "";}
		try{this.withheld_in_countries = Util.EscapeString(vo.getString("withheld_in_countries"));}catch(Exception e){this.withheld_in_countries = "";}
		try{this.withheld_scope = Util.EscapeString(vo.getString("withheld_scope"));}catch(Exception e){this.withheld_scope = "";}
	}
	
	public String toString(){
		return String
				.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
						contributors_enabled,    
						created_at,              
						default_profile,         
						default_profile_image,   
						description,             
						entities_url_urls,       
						entities_description_urls,       
						favourites_count,        
						follow_request_sent,     
						following_,             
						followers_count,         
						friends_count,           
						geo_enabled,             
						id,                      
						id_str,                  
						is_translator,           
						lang,                    
						listed_count,            
						location,                
						name,                    
						notifications,           
						profile_background_color,        
						profile_background_image_url,    
						profile_background_image_url_http,       
						profile_background_tile,
						profile_banner_url,      
						profile_image_url,       
						profile_image_url_https, 
						profile_link_color,      
						profile_sidebar_border_color,    
						profile_sidebar_fill_color,      
						profile_text_color,      
						profile_use_background_image,    
						protected_,               
						screen_name,             
						show_all_inline_media,   
						status,                  
						statuses_count,          
						time_zone,               
						url,                    
						utc_offset,              
						verified,                
						withheld_in_countries,   
						withheld_scope,
						crawl_time);
	}
	
	public String[] getAttributeValueList() {
		return new String[]{contributors_enabled,    
				created_at,              
				default_profile,         
				default_profile_image,   
				description,             
				entities_url_urls,       
				entities_description_urls,       
				favourites_count,        
				follow_request_sent,     
				following_,             
				followers_count,         
				friends_count,           
				geo_enabled,             
				id,                      
				id_str,                  
				is_translator,           
				lang,                    
				listed_count,            
				location,                
				name,                    
				notifications,           
				profile_background_color,        
				profile_background_image_url,    
				profile_background_image_url_http,       
				profile_background_tile,
				profile_banner_url,      
				profile_image_url,       
				profile_image_url_https, 
				profile_link_color,      
				profile_sidebar_border_color,    
				profile_sidebar_fill_color,      
				profile_text_color,      
				profile_use_background_image,    
				protected_,               
				screen_name,             
				show_all_inline_media,   
				status,                  
				statuses_count,          
				time_zone,               
				url,                    
				utc_offset,              
				verified,                
				withheld_in_countries,   
				withheld_scope,
				crawl_time};
	}

}
