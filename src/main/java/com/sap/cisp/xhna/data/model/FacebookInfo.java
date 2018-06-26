package com.sap.cisp.xhna.data.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;

public class FacebookInfo {
	private String id;
	private String actions;
	private String application;
	private String call_to_action_context;
	private String caption;
	private String created_time;
	private String description;
	private String feed_targeting_age_max;
	private String feed_targeting_age_min;
	private String feed_targeting_cities;
	private String feed_targeting_college_majors;
	private String feed_targeting_college_networks;
	private String feed_targeting_college_years;
	private String feed_targeting_country;
	private String feed_targeting_education_statuses;
	private String feed_targeting_genders;
	private String feed_targeting_interested_in;
	private String feed_targeting_locales;
	private String feed_targeting_regions;
	private String feed_targeting_relationship_statuses;
	private String feed_targeting_work_networks;
	private String from_category;
	private String from_name;
	private String from_id;
	private String icon;
	private String is_hidden;
	private String link;
	private String message;
	private String message_tags;
	private String name;
	private String object_id;
	private String picture;
	private String place_id;
	private String place_location_zip;
	private String place_location_street;
	private String place_location_state;
	private String place_location_longitude;
	private String place_location_latitude;
	private String place_location_city;
	private String place_location_country;
	private String place_name;
	private String privacy_description;
	private String privacy_value;
	private String privacy_friends;
	private String privacy_allow;
	private String privacy_deny;
	private String properties;
	private String shares_count;
	private String source;
	private String status_type;
	private String story;
	private String to;
	private String type;
	private String updated_time;
	private String with_tags;
	private String likes_count;
	private String likes_data;
	private String likes_paging;
	private String comments_count;
	private String comments_data;
	private String comments_paging;

	//private List<FacebookComment> comments;
	
	public FacebookInfo(String JsonString) {
		// TODO Auto-generated constructor stub
		JSONObject vo = JSON.parseObject(JsonString);
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id="";}
		try{this.actions = Util.EscapeString(vo.getString("actions"));}catch(Exception e){this.actions="";}
		try{this.application = Util.EscapeString(vo.getString("application"));}catch(Exception e){this.application="";}
		try{this.call_to_action_context = Util.EscapeString(vo.getJSONObject("call_to_action").getString("context"));}catch(Exception e){this.call_to_action_context = "";}
		try{this.caption = Util.EscapeString(vo.getString("caption"));}catch(Exception e){this.caption="";}
		try{this.created_time = Util.DateConversion(vo.getString("created_time"),"Facebook");;}catch(Exception e){this.created_time="";}
		try{this.description = Util.EscapeString(vo.getString("description"));}catch(Exception e){this.description="";}
		try{this.feed_targeting_age_max = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("age_max"));}catch(Exception e){this.feed_targeting_age_max = "";}
		try{this.feed_targeting_age_min = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("age_min"));}catch(Exception e){this.feed_targeting_age_min = "";}
		try{this.feed_targeting_cities = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("cities"));}catch(Exception e){this.feed_targeting_cities = "";}
		try{this.feed_targeting_college_majors = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("college_majors"));}catch(Exception e){this.feed_targeting_college_majors = "";}
		try{this.feed_targeting_college_networks = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("college_networks"));}catch(Exception e){this.feed_targeting_college_networks = "";}
		try{this.feed_targeting_college_years = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("college_years"));}catch(Exception e){this.feed_targeting_college_years = "";}
		try{this.feed_targeting_country = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("country"));}catch(Exception e){this.feed_targeting_country = "";}
		try{this.feed_targeting_education_statuses = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("education_statuses"));}catch(Exception e){this.feed_targeting_education_statuses = "";}
		try{this.feed_targeting_genders = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("genders"));}catch(Exception e){this.feed_targeting_genders = "";}
		try{this.feed_targeting_interested_in = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("interested_in"));}catch(Exception e){this.feed_targeting_interested_in = "";}
		try{this.feed_targeting_locales = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("locales"));}catch(Exception e){this.feed_targeting_locales = "";}
		try{this.feed_targeting_regions = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("regions"));}catch(Exception e){this.feed_targeting_regions = "";}
		try{this.feed_targeting_relationship_statuses = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("relationship_statuses"));}catch(Exception e){this.feed_targeting_relationship_statuses = "";}
		try{this.feed_targeting_work_networks = Util.EscapeString(vo.getJSONObject("feed_targeting").getString("work_networks"));}catch(Exception e){this.feed_targeting_work_networks = "";}
		
		try{this.from_category = Util.EscapeString(vo.getJSONObject("from").getString("category"));}catch(Exception e){this.from_category = "";}
		try{this.from_name = Util.EscapeString(vo.getJSONObject("from").getString("name"));}catch(Exception e){this.from_name = "";}
		try{this.from_id = Util.EscapeString(vo.getJSONObject("from").getString("id"));}catch(Exception e){this.from_id = "";}
		
		try{this.icon = Util.EscapeString(vo.getString("icon"));}catch(Exception e){this.icon="";}
		try{this.is_hidden = Util.EscapeString(vo.getString("is_hidden"));}catch(Exception e){this.is_hidden="";}
		try{this.link = Util.EscapeString(vo.getString("link"));}catch(Exception e){this.link="";}
		try{this.message = Util.EscapeString(vo.getString("message"));}catch(Exception e){this.message="";}
		try{this.message_tags = Util.EscapeString(vo.getString("message_tags"));}catch(Exception e){this.message_tags="";}
		try{this.name = Util.EscapeString(vo.getString("name"));}catch(Exception e){this.name="";}
		try{this.object_id = Util.EscapeString(vo.getString("object_id"));}catch(Exception e){this.object_id="";}
		try{this.picture = Util.EscapeString(vo.getString("picture"));}catch(Exception e){this.picture="";}
		
		try{this.place_id = Util.EscapeString(vo.getJSONObject("place").getString("id"));}catch(Exception e){this.place_id = "";}
		try{this.place_name = Util.EscapeString(vo.getJSONObject("place").getString("name"));}catch(Exception e){this.place_name = "";}
		try{this.place_location_zip = Util.EscapeString(vo.getJSONObject("place").getJSONObject("location").getString("zip"));}catch(Exception e){this.place_location_zip = "";}
		try{this.place_location_street = Util.EscapeString(vo.getJSONObject("place").getJSONObject("location").getString("street"));}catch(Exception e){this.place_location_street = "";}
		try{this.place_location_state = Util.EscapeString(vo.getJSONObject("place").getJSONObject("location").getString("state"));}catch(Exception e){this.place_location_state = "";}
		try{this.place_location_longitude = Util.EscapeString(vo.getJSONObject("place").getJSONObject("location").getString("longitude"));}catch(Exception e){this.place_location_longitude = "";}
		try{this.place_location_latitude = Util.EscapeString(vo.getJSONObject("place").getJSONObject("location").getString("latitude"));}catch(Exception e){this.place_location_latitude = "";}
		try{this.place_location_city = Util.EscapeString(vo.getJSONObject("place").getJSONObject("location").getString("city"));}catch(Exception e){this.place_location_city = "";}
		try{this.place_location_country = Util.EscapeString(vo.getJSONObject("place").getJSONObject("location").getString("country"));}catch(Exception e){this.place_location_country = "";}
		
		try{this.privacy_description = Util.EscapeString(vo.getJSONObject("privacy").getString("description"));}catch(Exception e){this.privacy_description = "";}
		try{this.privacy_value = Util.EscapeString(vo.getJSONObject("privacy").getString("value"));}catch(Exception e){this.privacy_value = "";}
		try{this.privacy_friends = Util.EscapeString(vo.getJSONObject("privacy").getString("friends"));}catch(Exception e){this.privacy_friends = "";}
		try{this.privacy_allow = Util.EscapeString(vo.getJSONObject("privacy").getString("allow"));}catch(Exception e){this.privacy_allow = "";}
		try{this.privacy_deny = Util.EscapeString(vo.getJSONObject("privacy").getString("deny"));}catch(Exception e){this.privacy_deny = "";}
		try{this.properties = Util.EscapeString(vo.getString("properties"));}catch(Exception e){this.properties="";}
		try{this.shares_count = Util.EscapeString(vo.getJSONObject("shares").getString("count"));}catch(Exception e){this.shares_count="";}
		try{this.source = Util.EscapeString(vo.getString("source"));}catch(Exception e){this.source="";}
		try{this.status_type = Util.EscapeString(vo.getString("status_type"));}catch(Exception e){this.status_type="";}
		try{this.story = Util.EscapeString(vo.getString("story"));}catch(Exception e){this.story="";}
		try{this.to = Util.EscapeString(vo.getString("to"));}catch(Exception e){this.to="";}
		try{this.type = Util.EscapeString(vo.getString("type"));}catch(Exception e){this.type="";}
		try{this.updated_time = Util.DateConversion(vo.getString("updated_time"),"Facebook");}catch(Exception e){this.updated_time="";}
		try{this.with_tags = Util.EscapeString(vo.getString("with_tags"));}catch(Exception e){this.with_tags="";}
		try{this.likes_count = Util.EscapeString(vo.getString("likes_count"));}catch(Exception e){this.likes_count = "";}
		try{this.likes_data = Util.EscapeString(vo.getJSONObject("likes").getString("data"));}catch(Exception e){this.likes_data = "";}
		try{this.likes_paging = Util.EscapeString(vo.getJSONObject("likes").getString("paging"));}catch(Exception e){this.likes_paging = "";}
		try{this.comments_count = Util.EscapeString(vo.getString("comments_count"));}catch(Exception e){this.comments_count = "";}
		try{this.comments_data = Util.EscapeString(vo.getJSONObject("comments").getString("data"));}catch(Exception e){this.comments_data = "";}
		try{this.comments_paging = Util.EscapeString(vo.getJSONObject("comments").getString("paging"));}catch(Exception e){this.comments_paging = "";}
	}
	
	public String toString(){
		return String
				.format("%s\t%s\t%s\t%s\t" +
						"%s\t%s\t%s\t" +
						"%s\t%s\t" +
						"%s\t%s\t" +
						"%s\t%s\t" +
						"%s\t%s\t" +
						"%s\t%s\t" +
						"%s\t%s\t" +
						"%s\t%s\t" +
						"%s\t%s\t%s\t%s\t%s\t" +
						"%s\t%s\t%s\t%s\t%s\t%s\t" +
						"%s\t%s\t" +
						"%s\t%s\t%s\t" +
						"%s\t%s\t%s\t%s\t" +
						"%s\t%s\t" +
						"%s\t%s\t%s\t" +
						"%s\t%s\t%s\t%s\t%s\t%s\t" +
						"%s\t%s\t%s\t%s\t%s\t" + 
						"%s\t%s\t%s\t%s",
						id, actions, application, call_to_action_context,
						caption, created_time, description,
						feed_targeting_age_max, feed_targeting_age_min,
						feed_targeting_cities, feed_targeting_college_majors,
						feed_targeting_college_networks, feed_targeting_college_years, 
						feed_targeting_country,	feed_targeting_education_statuses,
						feed_targeting_genders, feed_targeting_interested_in,
						feed_targeting_locales, feed_targeting_regions,
						feed_targeting_relationship_statuses, feed_targeting_work_networks, 
						from_category, from_name, from_id, icon, is_hidden,
						link, message, message_tags, name, object_id, picture,
						place_id, place_name, 
						place_location_zip, place_location_street, place_location_state, 
						place_location_longitude, place_location_latitude, place_location_city, place_location_country,
						privacy_description, privacy_value,
						privacy_friends, privacy_allow, privacy_deny,
						properties, shares_count, source, status_type, story, to,
						type, updated_time, with_tags, likes_count, likes_data,
						likes_paging, comments_count, comments_data, comments_paging);
	}
	
	public String[] getAttributeValueList() {
		return new String[]{id, actions, application, call_to_action_context,
				caption, created_time, description,
				feed_targeting_age_max, feed_targeting_age_min,
				feed_targeting_cities, feed_targeting_college_majors,
				feed_targeting_college_networks, feed_targeting_college_years, 
				feed_targeting_country,	feed_targeting_education_statuses,
				feed_targeting_genders, feed_targeting_interested_in,
				feed_targeting_locales, feed_targeting_regions,
				feed_targeting_relationship_statuses, feed_targeting_work_networks, 
				from_category, from_name, from_id, icon, is_hidden,
				link, message, message_tags, name, object_id, picture,
				place_id, place_name, 
				place_location_zip, place_location_street, place_location_state, 
				place_location_longitude, place_location_latitude, place_location_city, place_location_country,
				privacy_description, privacy_value,
				privacy_friends, privacy_allow, privacy_deny,
				properties, shares_count, source, status_type, story, to,
				type, updated_time, with_tags, likes_count, likes_data,
				likes_paging, comments_count, comments_data, comments_paging};
	}
}