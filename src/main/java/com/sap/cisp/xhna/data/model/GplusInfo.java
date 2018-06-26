package com.sap.cisp.xhna.data.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;
public class GplusInfo {
	private String kind;
	private String title;
	private String published;
	private String updated;
	private String id;
	private String url;
	private String actor_id;
	private String actor_displayName;
	private String actor_name;
	private String actor_name_familyName;
	private String actor_name_givenName;
	private String actor_url;
	private String actor_image_url;
	private String verb;
	private String object_objectType;
	private String object_id;
	private String object_actor_id;
	private String object_actor_displayName;
	private String object_actor_url;
	private String object_actor_image_url;
	private String object_content;
	private String object_originalContent;
	private String object_url;
	private String object_replies_totalItems;
	private String object_plusoners_totalItems;
	private String object_resharers_totalItems;
	private String object_attachments;
	private String object_replies_selfLink;
	private String object_plusoners_selfLink;
	private String object_resharers_selfLink;
	private String annotation;
	private String crosspostSource;
	private String provider_title;
	private String access_kind;
	private String access_description;
	private String access_items;
	private String geocode;
	private String address;
	private String radius;
	private String placeId;
	private String placeName;
	private String etag;
	public GplusInfo(String JsonString) {
		// TODO Auto-generated constructor stub
		JSONObject vo = JSON.parseObject(JsonString);
		try{this.kind = Util.EscapeString(vo.getString("kind"));}catch(Exception e){this.kind="";}
		try{this.title = Util.EscapeString(vo.getString("title"));}catch(Exception e){this.title="";}
		try{this.published = Util.DateConversion(vo.getString("published"),"Gplus");}catch(Exception e){this.published="";}
		try{this.updated = Util.DateConversion(vo.getString("updated"),"Gplus");}catch(Exception e){this.updated="";}
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id="";}
		try{this.url = Util.EscapeString(vo.getString("url"));}catch(Exception e){this.url="";}
		try{this.actor_id = Util.EscapeString(vo.getJSONObject("actor").getString("id"));}catch(Exception e){this.actor_id = "";}
		try{this.actor_displayName = Util.EscapeString(vo.getJSONObject("actor").getString("displayName"));}catch(Exception e){this.actor_displayName = "";}
		try{this.actor_name = Util.EscapeString(vo.getJSONObject("actor").getString("name"));}catch(Exception e){this.actor_name = "";}
		try{this.actor_name_familyName = Util.EscapeString(vo.getJSONObject("actor").getJSONObject("name").getString("familyName"));}catch(Exception e){this.actor_name_familyName = "";}
		try{this.actor_name_givenName = Util.EscapeString(vo.getJSONObject("actor").getJSONObject("name").getString("givenName"));}catch(Exception e){this.actor_name_givenName = "";}
		try{this.actor_url = Util.EscapeString(vo.getJSONObject("actor").getString("url"));}catch(Exception e){this.actor_url = "";}
		try{this.actor_image_url = Util.EscapeString(vo.getJSONObject("actor").getJSONObject("image").getString("url"));}catch(Exception e){this.actor_image_url = "";}
		try{this.verb = Util.EscapeString(vo.getString("verb"));}catch(Exception e){this.verb="";}
		try{this.object_objectType = Util.EscapeString(vo.getJSONObject("object").getString("objectType"));}catch(Exception e){this.object_objectType = "";}
		try{this.object_id = Util.EscapeString(vo.getJSONObject("object").getString("id"));}catch(Exception e){this.object_id = "";}
		try{this.object_actor_id = Util.EscapeString(vo.getJSONObject("object").getJSONObject("actor").getString("id"));}catch(Exception e){this.object_actor_id = "";}
		try{this.object_actor_displayName = Util.EscapeString(vo.getJSONObject("object").getJSONObject("actor").getString("displayName"));}catch(Exception e){this.object_actor_displayName = "";}
		try{this.object_actor_url = Util.EscapeString(vo.getJSONObject("object").getJSONObject("actor").getString("url"));}catch(Exception e){this.object_actor_url = "";}
		try{this.object_actor_image_url = Util.EscapeString(vo.getJSONObject("object").getJSONObject("actor").getJSONObject("image").getString("url"));}catch(Exception e){this.object_actor_image_url = "";}
		try{this.object_content = Util.EscapeString(vo.getJSONObject("object").getString("content"));}catch(Exception e){this.object_content = "";}
		try{this.object_originalContent = Util.EscapeString(vo.getJSONObject("object").getString("originalContent"));}catch(Exception e){this.object_originalContent = "";}
		try{this.object_url = Util.EscapeString(vo.getJSONObject("object").getString("url"));}catch(Exception e){this.object_url = "";}
		try{this.object_replies_totalItems = Util.EscapeString(vo.getJSONObject("object").getJSONObject("replies").getString("totalItems"));}catch(Exception e){this.object_replies_totalItems = "";}
		try{this.object_plusoners_totalItems = Util.EscapeString(vo.getJSONObject("object").getJSONObject("plusoners").getString("totalItems"));}catch(Exception e){this.object_plusoners_totalItems = "";}
		try{this.object_resharers_totalItems = Util.EscapeString(vo.getJSONObject("object").getJSONObject("resharers").getString("totalItems"));}catch(Exception e){this.object_resharers_totalItems = "";}
		try{this.object_attachments = Util.EscapeString(vo.getJSONObject("object").getString("attachments"));}catch(Exception e){this.object_attachments = "";}
		try{this.annotation = Util.EscapeString(vo.getString("annotation"));}catch(Exception e){this.annotation="";}
		try{this.crosspostSource = Util.EscapeString(vo.getString("crosspostSource"));}catch(Exception e){this.crosspostSource="";}
		try{this.provider_title = Util.EscapeString(vo.getJSONObject("provider").getString("title"));}catch(Exception e){this.provider_title = "";}
		try{this.access_kind = Util.EscapeString(vo.getJSONObject("access").getString("kind"));}catch(Exception e){this.access_kind = "";}
		try{this.access_description = Util.EscapeString(vo.getJSONObject("access").getString("description"));}catch(Exception e){this.access_description = "";}
		try{this.access_items = Util.EscapeString(vo.getJSONObject("access").getString("items"));}catch(Exception e){this.access_items = "";}
		try{this.geocode = Util.EscapeString(vo.getString("geocode"));}catch(Exception e){this.geocode="";}
		try{this.address = Util.EscapeString(vo.getString("address"));}catch(Exception e){this.address="";}
		try{this.radius = Util.EscapeString(vo.getString("radius"));}catch(Exception e){this.radius="";}
		try{this.placeId = Util.EscapeString(vo.getString("placeId"));}catch(Exception e){this.placeId="";}
		try{this.placeName = Util.EscapeString(vo.getString("placeName"));}catch(Exception e){this.placeName="";}
		try{this.object_replies_selfLink = Util.EscapeString(vo.getJSONObject("object").getJSONObject("replies").getString("selfLink"));}catch(Exception e){this.object_replies_selfLink = "";}
		try{this.object_plusoners_selfLink = Util.EscapeString(vo.getJSONObject("object").getJSONObject("plusoners").getString("selfLink"));}catch(Exception e){this.object_plusoners_selfLink = "";}
		try{this.object_resharers_selfLink = Util.EscapeString(vo.getJSONObject("object").getJSONObject("resharers").getString("selfLink"));}catch(Exception e){this.object_resharers_selfLink = "";}
		try{this.etag = Util.EscapeString(vo.getString("etag"));}catch(Exception e){this.etag="";}

	}
	
	public String toString(){
		return String
				.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
						kind, title, published, updated, id, url, actor_id,
						actor_displayName, actor_name, actor_name_familyName,
						actor_name_givenName, actor_url, actor_image_url, verb,
						object_objectType, object_id, object_actor_id,
						object_actor_displayName, object_actor_url,
						object_actor_image_url, object_content,
						object_originalContent, object_url,
						object_replies_totalItems, object_plusoners_totalItems,
						object_resharers_totalItems, object_attachments,
						annotation, crosspostSource, provider_title,
						access_kind, access_description, access_items, geocode,
						address, radius, placeId, placeName,
						object_replies_selfLink, object_plusoners_selfLink,
						object_resharers_selfLink, etag);
	}
	
	public String[] getAttributeValueList() {
		return new String[]{kind, title, published, updated, id, url, actor_id,
				actor_displayName, actor_name, actor_name_familyName,
				actor_name_givenName, actor_url, actor_image_url, verb,
				object_objectType, object_id, object_actor_id,
				object_actor_displayName, object_actor_url,
				object_actor_image_url, object_content, object_originalContent,
				object_url, object_replies_totalItems,
				object_plusoners_totalItems, object_resharers_totalItems,
				object_attachments, annotation, crosspostSource,
				provider_title, access_kind, access_description, access_items,
				geocode, address, radius, placeId, placeName,
				object_replies_selfLink, object_plusoners_selfLink,
				object_resharers_selfLink, etag};

	}

}
