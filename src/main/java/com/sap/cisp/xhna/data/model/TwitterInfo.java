package com.sap.cisp.xhna.data.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;

public class TwitterInfo {
	private String contributors;
	private String coordinates;
	private String created_at;
	private String current_user_retweet_id;
	private String current_user_retweet_id_str;
	private String entities_hashtags;
	private String entities_symbols;
	private String entities_urls;
	private String entities_user_mentions;
	private String entities_media;
	private String favorite_count;
	private String favorited;
	private String filter_level;
	private String id;
	private String id_str;
	private String in_reply_to_screen_name;
	private String in_reply_to_status_id;
	private String in_reply_to_status_id_str;
	private String in_reply_to_user_id;
	private String in_reply_to_user_id_str;
	private String lang;
	private String place_attributes_street_address;
	private String place_attributes_locality;
	private String place_attributes_region;
	private String place_attributes_iso3;
	private String place_attributes_postal_code;
	private String place_attributes_phone;
	private String place_attributes_twitter;
	private String place_attributes_url;
	private String place_bounding_box_coordinates;
	private String place_bounding_box_type;
	private String place_country;
	private String place_country_code;
	private String place_full_name;
	private String place_id;
	private String place_name;
	private String place_place_type;
	private String place_url;
	private String possibly_sensitive;
	private String retweet_count;
	private String retweeted;
	private String source;
	private String text;
	private String truncated;
	private String user_contributors_enabled;
	private String user_created_at;
	private String user_default_profile;
	private String user_default_profile_image;
	private String user_description;
	private String user_entities_url_urls;
	private String user_entities_description_urls;
	private String user_favourites_count;
	private String user_follow_request_sent;
	private String user_following;
	private String user_followers_count;
	private String user_friends_count;
	private String user_geo_enabled;
	private String user_id;
	private String user_id_str;
	private String user_is_translator;
	private String user_lang;
	private String user_listed_count;
	private String user_location;
	private String user_name;
	private String user_notifications;
	private String user_profile_background_color;
	private String user_profile_background_image_url;
	private String user_profile_background_image_url_https;
	private String user_profile_background_tile;
	private String user_profile_banner_url;
	private String user_profile_image_url;
	private String user_profile_image_url_https;
	private String user_profile_link_color;
	private String user_profile_sidebar_border_color;
	private String user_profile_sidebar_fill_color;
	private String user_profile_text_color;
	private String user_profile_use_background_image;
	private String user_protected;
	private String user_screen_name;
	private String user_show_all_inline_media;
	private String user_status;
	private String user_statuses_count;
	private String user_time_zone;
	private String user_url;
	private String user_utc_offset;
	private String user_verified;
	private String user_withheld_in_countries;
	private String user_withheld_scope;
	private String withheld_copyright;
	private String withheld_in_countries;
	private String withheld_scope;
	private String retweeted_status_contributors;
	private String retweeted_status_coordinates;
	private String retweeted_status_created_at;
	private String retweeted_status_current_user_retweet_id;
	private String retweeted_status_current_user_retweet_id_str;
	private String retweeted_status_entities_hashtags;
	private String retweeted_status_entities_symbols;
	private String retweeted_status_entities_urls;
	private String retweeted_status_entities_user_mentions;
	private String retweeted_status_entities_media_id;
	private String retweeted_status_favorite_count;
	private String retweeted_status_favorited;
	private String retweeted_status_filter_level;
	private String retweeted_status_id;
	private String retweeted_status_id_str;
	private String retweeted_status_in_reply_to_screen_name;
	private String retweeted_status_in_reply_to_status_id;
	private String retweeted_status_in_reply_to_status_id_str;
	private String retweeted_status_in_reply_to_user_id;
	private String retweeted_status_in_reply_to_user_id_str;
	private String retweeted_status_lang;
	private String retweeted_status_place_attributes_street_address;
	private String retweeted_status_place_attributes_locality;
	private String retweeted_status_place_attributes_region;
	private String retweeted_status_place_attributes_iso3;
	private String retweeted_status_place_attributes_postal_code;
	private String retweeted_status_place_attributes_phone;
	private String retweeted_status_place_attributes_twitter;
	private String retweeted_status_place_attributes_url;
	private String retweeted_status_place_bounding_box_coordinates;
	private String retweeted_status_place_bounding_box_type;
	private String retweeted_status_place_country;
	private String retweeted_status_place_country_code;
	private String retweeted_status_place_full_name;
	private String retweeted_status_place_id;
	private String retweeted_status_place_name;
	private String retweeted_status_place_place_type;
	private String retweeted_status_place_url;
	private String retweeted_status_possibly_sensitive;
	private String retweeted_status_retweet_count;
	private String retweeted_status_retweeted;
	private String retweeted_status_source;
	private String retweeted_status_text;
	private String retweeted_status_truncated;
	private String retweeted_status_user_contributors_enabled;
	private String retweeted_status_user_created_at;
	private String retweeted_status_user_default_profile;
	private String retweeted_status_user_default_profile_image;
	private String retweeted_status_user_description;
	private String retweeted_status_user_entities_url_urls;
	private String retweeted_status_user_entities_description_urls;
	private String retweeted_status_user_favourites_count;
	private String retweeted_status_user_follow_request_sent;
	private String retweeted_status_user_following;
	private String retweeted_status_user_followers_count;
	private String retweeted_status_user_friends_count;
	private String retweeted_status_user_geo_enabled;
	private String retweeted_status_user_id;
	private String retweeted_status_user_id_str;
	private String retweeted_status_user_is_translator;
	private String retweeted_status_user_lang;
	private String retweeted_status_user_listed_count;
	private String retweeted_status_user_location;
	private String retweeted_status_user_name;
	private String retweeted_status_user_notifications;
	private String retweeted_status_user_profile_background_color;
	private String retweeted_status_user_profile_background_image_url;
	private String retweeted_status_user_profile_background_image_url_https;
	private String retweeted_status_user_profile_background_tile;
	private String retweeted_status_user_profile_banner_url;
	private String retweeted_status_user_profile_image_url;
	private String retweeted_status_user_profile_image_url_https;
	private String retweeted_status_user_profile_link_color;
	private String retweeted_status_user_profile_sidebar_border_color;
	private String retweeted_status_user_profile_sidebar_fill_color;
	private String retweeted_status_user_profile_text_color;
	private String retweeted_status_user_profile_use_background_image;
	private String retweeted_status_user_protected;
	private String retweeted_status_user_screen_name;
	private String retweeted_status_user_show_all_inline_media;
	private String retweeted_status_user_status;
	private String retweeted_status_user_statuses_count;
	private String retweeted_status_user_time_zone;
	private String retweeted_status_user_url;
	private String retweeted_status_user_utc_offset;
	private String retweeted_status_user_verified;
	private String retweeted_status_user_withheld_in_countries;
	private String retweeted_status_user_withheld_scope;
	private String retweeted_status_withheld_copyright;
	private String retweeted_status_withheld_in_countries;
	private String retweeted_status_withheld_scope;

	public TwitterInfo(String JsonString) {
		// TODO Auto-generated constructor stub
		JSONObject vo = JSON.parseObject(JsonString);
		try{this.contributors = Util.EscapeString(vo.getString("contributors"));}catch(Exception e){this.contributors="";}
		try{this.coordinates = Util.EscapeString(vo.getString("coordinates"));}catch(Exception e){this.coordinates="";}
		try{this.created_at = Util.DateConversion(vo.getString("created_at"),"Twitter");}catch(Exception e){this.created_at="";}
		try{this.current_user_retweet_id = Util.EscapeString(vo.getJSONObject("current_user_retweet").getString("id"));}catch(Exception e){this.current_user_retweet_id = "";}
		try{this.current_user_retweet_id_str = Util.EscapeString(vo.getJSONObject("current_user_retweet").getString("id_str"));}catch(Exception e){this.current_user_retweet_id_str = "";}
		try{this.entities_hashtags = Util.EscapeString(vo.getJSONObject("entities").getString("hashtags"));}catch(Exception e){this.entities_hashtags = "";}
		try{this.entities_symbols = Util.EscapeString(vo.getJSONObject("entities").getString("symbols"));}catch(Exception e){this.entities_symbols = "";}
		try{this.entities_urls = Util.EscapeString(vo.getJSONObject("entities").getString("urls"));}catch(Exception e){this.entities_urls = "";}
		try{this.entities_user_mentions = Util.EscapeString(vo.getJSONObject("entities").getString("user_mentions"));}catch(Exception e){this.entities_user_mentions = "";}
		try{this.entities_media = Util.EscapeString(vo.getJSONObject("entities").getString("media"));}catch(Exception e){this.entities_media = "";}
		try{this.favorite_count = Util.EscapeString(vo.getString("favorite_count"));}catch(Exception e){this.favorite_count="";}
		try{this.favorited = Util.EscapeString(vo.getString("favorited"));}catch(Exception e){this.favorited="";}
		try{this.filter_level = Util.EscapeString(vo.getString("filter_level"));}catch(Exception e){this.filter_level="";}
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id="";}
		try{this.id_str = Util.EscapeString(vo.getString("id_str"));}catch(Exception e){this.id_str="";}
		try{this.in_reply_to_screen_name = Util.EscapeString(vo.getString("in_reply_to_screen_name"));}catch(Exception e){this.in_reply_to_screen_name="";}
		try{this.in_reply_to_status_id = Util.EscapeString(vo.getString("in_reply_to_status_id"));}catch(Exception e){this.in_reply_to_status_id="";}
		try{this.in_reply_to_status_id_str = Util.EscapeString(vo.getString("in_reply_to_status_id_str"));}catch(Exception e){this.in_reply_to_status_id_str="";}
		try{this.in_reply_to_user_id = Util.EscapeString(vo.getString("in_reply_to_user_id"));}catch(Exception e){this.in_reply_to_user_id="";}
		try{this.in_reply_to_user_id_str = Util.EscapeString(vo.getString("in_reply_to_user_id_str"));}catch(Exception e){this.in_reply_to_user_id_str="";}
		try{this.lang = Util.EscapeString(vo.getString("lang"));}catch(Exception e){this.lang="";}
		try{this.place_attributes_street_address = Util.EscapeString(vo.getJSONObject("place").getJSONObject("attributes").getString("street_address"));}catch(Exception e){this.place_attributes_street_address = "";}
		try{this.place_attributes_locality = Util.EscapeString(vo.getJSONObject("place").getJSONObject("attributes").getString("locality"));}catch(Exception e){this.place_attributes_locality = "";}
		try{this.place_attributes_region = Util.EscapeString(vo.getJSONObject("place").getJSONObject("attributes").getString("region"));}catch(Exception e){this.place_attributes_region = "";}
		try{this.place_attributes_iso3 = Util.EscapeString(vo.getJSONObject("place").getJSONObject("attributes").getString("iso3"));}catch(Exception e){this.place_attributes_iso3 = "";}
		try{this.place_attributes_postal_code = Util.EscapeString(vo.getJSONObject("place").getJSONObject("attributes").getString("postal_code"));}catch(Exception e){this.place_attributes_postal_code = "";}
		try{this.place_attributes_phone = Util.EscapeString(vo.getJSONObject("place").getJSONObject("attributes").getString("phone"));}catch(Exception e){this.place_attributes_phone = "";}
		try{this.place_attributes_twitter = Util.EscapeString(vo.getJSONObject("place").getJSONObject("attributes").getString("twitter"));}catch(Exception e){this.place_attributes_twitter = "";}
		try{this.place_attributes_url = Util.EscapeString(vo.getJSONObject("place").getJSONObject("attributes").getString("url"));}catch(Exception e){this.place_attributes_url = "";}
		try{this.place_bounding_box_coordinates = Util.EscapeString(vo.getJSONObject("place").getJSONObject("bounding_box").getString("coordinates"));}catch(Exception e){this.place_bounding_box_coordinates = "";}
		try{this.place_bounding_box_type = Util.EscapeString(vo.getJSONObject("place").getJSONObject("bounding_box").getString("type"));}catch(Exception e){this.place_bounding_box_type = "";}
		try{this.place_country = Util.EscapeString(vo.getJSONObject("place").getString("country"));}catch(Exception e){this.place_country = "";}
		try{this.place_country_code = Util.EscapeString(vo.getJSONObject("place").getString("country_code"));}catch(Exception e){this.place_country_code = "";}
		try{this.place_full_name = Util.EscapeString(vo.getJSONObject("place").getString("full_name"));}catch(Exception e){this.place_full_name = "";}
		try{this.place_id = Util.EscapeString(vo.getJSONObject("place").getString("id"));}catch(Exception e){this.place_id = "";}
		try{this.place_name = Util.EscapeString(vo.getJSONObject("place").getString("name"));}catch(Exception e){this.place_name = "";}
		try{this.place_place_type = Util.EscapeString(vo.getJSONObject("place").getString("place_type"));}catch(Exception e){this.place_place_type = "";}
		try{this.place_url = Util.EscapeString(vo.getJSONObject("place").getString("url"));}catch(Exception e){this.place_url = "";}
		try{this.possibly_sensitive = Util.EscapeString(vo.getString("possibly_sensitive"));}catch(Exception e){this.possibly_sensitive="";}
		try{this.retweet_count = Util.EscapeString(vo.getString("retweet_count"));}catch(Exception e){this.retweet_count="";}
		try{this.retweeted = Util.EscapeString(vo.getString("retweeted"));}catch(Exception e){this.retweeted="";}
		try{this.source = Util.EscapeString(vo.getString("source"));}catch(Exception e){this.source="";}
		try{this.text = Util.EscapeString(vo.getString("text"));}catch(Exception e){this.text="";}
		try{this.truncated = Util.EscapeString(vo.getString("truncated"));}catch(Exception e){this.truncated="";}
		try{this.user_contributors_enabled = Util.EscapeString(vo.getJSONObject("user").getString("contributors_enabled"));}catch(Exception e){this.user_contributors_enabled = "";}
		try{this.user_created_at = Util.DateConversion(vo.getJSONObject("user").getString("created_at"),"Twitter");}catch(Exception e){this.user_created_at = "";}
		try{this.user_default_profile = Util.EscapeString(vo.getJSONObject("user").getString("default_profile"));}catch(Exception e){this.user_default_profile = "";}
		try{this.user_default_profile_image = Util.EscapeString(vo.getJSONObject("user").getString("default_profile_image"));}catch(Exception e){this.user_default_profile_image = "";}
		try{this.user_description = Util.EscapeString(vo.getJSONObject("user").getString("description"));}catch(Exception e){this.user_description = "";}
		try{this.user_entities_url_urls = Util.EscapeString(vo.getJSONObject("user").getJSONObject("entities").getJSONObject("url").getString("urls"));}catch(Exception e){this.user_entities_url_urls = "";}
		try{this.user_entities_description_urls = Util.EscapeString(vo.getJSONObject("user").getJSONObject("entities").getJSONObject("description").getString("urls"));}catch(Exception e){this.user_entities_description_urls = "";}
		try{this.user_favourites_count = Util.EscapeString(vo.getJSONObject("user").getString("favourites_count"));}catch(Exception e){this.user_favourites_count = "";}
		try{this.user_follow_request_sent = Util.EscapeString(vo.getJSONObject("user").getString("follow_request_sent"));}catch(Exception e){this.user_follow_request_sent = "";}
		try{this.user_following = Util.EscapeString(vo.getJSONObject("user").getString("following"));}catch(Exception e){this.user_following = "";}
		try{this.user_followers_count = Util.EscapeString(vo.getJSONObject("user").getString("followers_count"));}catch(Exception e){this.user_followers_count = "";}
		try{this.user_friends_count = Util.EscapeString(vo.getJSONObject("user").getString("friends_count"));}catch(Exception e){this.user_friends_count = "";}
		try{this.user_geo_enabled = Util.EscapeString(vo.getJSONObject("user").getString("geo_enabled"));}catch(Exception e){this.user_geo_enabled = "";}
		try{this.user_id = Util.EscapeString(vo.getJSONObject("user").getString("id"));}catch(Exception e){this.user_id = "";}
		try{this.user_id_str = Util.EscapeString(vo.getJSONObject("user").getString("id_str"));}catch(Exception e){this.user_id_str = "";}
		try{this.user_is_translator = Util.EscapeString(vo.getJSONObject("user").getString("is_translator"));}catch(Exception e){this.user_is_translator = "";}
		try{this.user_lang = Util.EscapeString(vo.getJSONObject("user").getString("lang"));}catch(Exception e){this.user_lang = "";}
		try{this.user_listed_count = Util.EscapeString(vo.getJSONObject("user").getString("listed_count"));}catch(Exception e){this.user_listed_count = "";}
		try{this.user_location = Util.EscapeString(vo.getJSONObject("user").getString("location"));}catch(Exception e){this.user_location = "";}
		try{this.user_name = Util.EscapeString(vo.getJSONObject("user").getString("name"));}catch(Exception e){this.user_name = "";}
		try{this.user_notifications = Util.EscapeString(vo.getJSONObject("user").getString("notifications"));}catch(Exception e){this.user_notifications = "";}
		try{this.user_profile_background_color = Util.EscapeString(vo.getJSONObject("user").getString("profile_background_color"));}catch(Exception e){this.user_profile_background_color = "";}
		try{this.user_profile_background_image_url = Util.EscapeString(vo.getJSONObject("user").getString("profile_background_image_url"));}catch(Exception e){this.user_profile_background_image_url = "";}
		try{this.user_profile_background_image_url_https = Util.EscapeString(vo.getJSONObject("user").getString("profile_background_image_url_https"));}catch(Exception e){this.user_profile_background_image_url_https = "";}
		try{this.user_profile_background_tile = Util.EscapeString(vo.getJSONObject("user").getString("profile_background_tile"));}catch(Exception e){this.user_profile_background_tile = "";}
		try{this.user_profile_banner_url = Util.EscapeString(vo.getJSONObject("user").getString("profile_banner_url"));}catch(Exception e){this.user_profile_banner_url = "";}
		try{this.user_profile_image_url = Util.EscapeString(vo.getJSONObject("user").getString("profile_image_url"));}catch(Exception e){this.user_profile_image_url = "";}
		try{this.user_profile_image_url_https = Util.EscapeString(vo.getJSONObject("user").getString("profile_image_url_https"));}catch(Exception e){this.user_profile_image_url_https = "";}
		try{this.user_profile_link_color = Util.EscapeString(vo.getJSONObject("user").getString("profile_link_color"));}catch(Exception e){this.user_profile_link_color = "";}
		try{this.user_profile_sidebar_border_color = Util.EscapeString(vo.getJSONObject("user").getString("profile_sidebar_border_color"));}catch(Exception e){this.user_profile_sidebar_border_color = "";}
		try{this.user_profile_sidebar_fill_color = Util.EscapeString(vo.getJSONObject("user").getString("profile_sidebar_fill_color"));}catch(Exception e){this.user_profile_sidebar_fill_color = "";}
		try{this.user_profile_text_color = Util.EscapeString(vo.getJSONObject("user").getString("profile_text_color"));}catch(Exception e){this.user_profile_text_color = "";}
		try{this.user_profile_use_background_image = Util.EscapeString(vo.getJSONObject("user").getString("profile_use_background_image"));}catch(Exception e){this.user_profile_use_background_image = "";}
		try{this.user_protected = Util.EscapeString(vo.getJSONObject("user").getString("protected"));}catch(Exception e){this.user_protected = "";}
		try{this.user_screen_name = Util.EscapeString(vo.getJSONObject("user").getString("screen_name"));}catch(Exception e){this.user_screen_name = "";}
		try{this.user_show_all_inline_media = Util.EscapeString(vo.getJSONObject("user").getString("show_all_inline_media"));}catch(Exception e){this.user_show_all_inline_media = "";}
		try{this.user_status = Util.EscapeString(vo.getJSONObject("user").getString("status"));}catch(Exception e){this.user_status = "";}
		try{this.user_statuses_count = Util.EscapeString(vo.getJSONObject("user").getString("statuses_count"));}catch(Exception e){this.user_statuses_count = "";}
		try{this.user_time_zone = Util.EscapeString(vo.getJSONObject("user").getString("time_zone"));}catch(Exception e){this.user_time_zone = "";}
		try{this.user_url = Util.EscapeString(vo.getJSONObject("user").getString("url"));}catch(Exception e){this.user_url = "";}
		try{this.user_utc_offset = Util.EscapeString(vo.getJSONObject("user").getString("utc_offset"));}catch(Exception e){this.user_utc_offset = "";}
		try{this.user_verified = Util.EscapeString(vo.getJSONObject("user").getString("verified"));}catch(Exception e){this.user_verified = "";}
		try{this.user_withheld_in_countries = Util.EscapeString(vo.getJSONObject("user").getString("withheld_in_countries"));}catch(Exception e){this.user_withheld_in_countries = "";}
		try{this.user_withheld_scope = Util.EscapeString(vo.getJSONObject("user").getString("withheld_scope"));}catch(Exception e){this.user_withheld_scope = "";}
		try{this.withheld_copyright = Util.EscapeString(vo.getString("withheld_copyright"));}catch(Exception e){this.withheld_copyright="";}
		try{this.withheld_in_countries = Util.EscapeString(vo.getString("withheld_in_countries"));}catch(Exception e){this.withheld_in_countries="";}
		try{this.withheld_scope = Util.EscapeString(vo.getString("withheld_scope"));}catch(Exception e){this.withheld_scope="";}
		try{this.retweeted_status_contributors = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("contributors"));}catch(Exception e){this.retweeted_status_contributors = "";}
		try{this.retweeted_status_coordinates = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("coordinates"));}catch(Exception e){this.retweeted_status_coordinates = "";}
		try{this.retweeted_status_created_at = Util.DateConversion(vo.getJSONObject("retweeted_status").getString("created_at"),"Twitter");}catch(Exception e){this.retweeted_status_created_at = "";}
		try{this.retweeted_status_current_user_retweet_id = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("current_user_retweet").getString("id"));}catch(Exception e){this.retweeted_status_current_user_retweet_id = "";}
		try{this.retweeted_status_current_user_retweet_id_str = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("current_user_retweet").getString("id_str"));}catch(Exception e){this.retweeted_status_current_user_retweet_id_str = "";}
		try{this.retweeted_status_entities_hashtags = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("entities").getString("hashtags"));}catch(Exception e){this.retweeted_status_entities_hashtags = "";}
		try{this.retweeted_status_entities_symbols = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("entities").getString("symbols"));}catch(Exception e){this.retweeted_status_entities_symbols = "";}
		try{this.retweeted_status_entities_urls = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("entities").getString("urls"));}catch(Exception e){this.retweeted_status_entities_urls = "";}
		try{this.retweeted_status_entities_user_mentions = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("entities").getString("user_mentions"));}catch(Exception e){this.retweeted_status_entities_user_mentions = "";}
		try{this.retweeted_status_entities_media_id = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("entities").getJSONObject("media").getString("id"));}catch(Exception e){this.retweeted_status_entities_media_id = "";}
		try{this.retweeted_status_favorite_count = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("favorite_count"));}catch(Exception e){this.retweeted_status_favorite_count = "";}
		try{this.retweeted_status_favorited = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("favorited"));}catch(Exception e){this.retweeted_status_favorited = "";}
		try{this.retweeted_status_filter_level = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("filter_level"));}catch(Exception e){this.retweeted_status_filter_level = "";}
		try{this.retweeted_status_id = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("id"));}catch(Exception e){this.retweeted_status_id = "";}
		try{this.retweeted_status_id_str = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("id_str"));}catch(Exception e){this.retweeted_status_id_str = "";}
		try{this.retweeted_status_in_reply_to_screen_name = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("in_reply_to_screen_name"));}catch(Exception e){this.retweeted_status_in_reply_to_screen_name = "";}
		try{this.retweeted_status_in_reply_to_status_id = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("in_reply_to_status_id"));}catch(Exception e){this.retweeted_status_in_reply_to_status_id = "";}
		try{this.retweeted_status_in_reply_to_status_id_str = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("in_reply_to_status_id_str"));}catch(Exception e){this.retweeted_status_in_reply_to_status_id_str = "";}
		try{this.retweeted_status_in_reply_to_user_id = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("in_reply_to_user_id"));}catch(Exception e){this.retweeted_status_in_reply_to_user_id = "";}
		try{this.retweeted_status_in_reply_to_user_id_str = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("in_reply_to_user_id_str"));}catch(Exception e){this.retweeted_status_in_reply_to_user_id_str = "";}
		try{this.retweeted_status_lang = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("lang"));}catch(Exception e){this.retweeted_status_lang = "";}
		try{this.retweeted_status_place_attributes_street_address = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getJSONObject("attributes").getString("street_address"));}catch(Exception e){this.retweeted_status_place_attributes_street_address = "";}
		try{this.retweeted_status_place_attributes_locality = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getJSONObject("attributes").getString("locality"));}catch(Exception e){this.retweeted_status_place_attributes_locality = "";}
		try{this.retweeted_status_place_attributes_region = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getJSONObject("attributes").getString("region"));}catch(Exception e){this.retweeted_status_place_attributes_region = "";}
		try{this.retweeted_status_place_attributes_iso3 = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getJSONObject("attributes").getString("iso3"));}catch(Exception e){this.retweeted_status_place_attributes_iso3 = "";}
		try{this.retweeted_status_place_attributes_postal_code = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getJSONObject("attributes").getString("postal_code"));}catch(Exception e){this.retweeted_status_place_attributes_postal_code = "";}
		try{this.retweeted_status_place_attributes_phone = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getJSONObject("attributes").getString("phone"));}catch(Exception e){this.retweeted_status_place_attributes_phone = "";}
		try{this.retweeted_status_place_attributes_twitter = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getJSONObject("attributes").getString("twitter"));}catch(Exception e){this.retweeted_status_place_attributes_twitter = "";}
		try{this.retweeted_status_place_attributes_url = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getJSONObject("attributes").getString("url"));}catch(Exception e){this.retweeted_status_place_attributes_url = "";}
		try{this.retweeted_status_place_bounding_box_coordinates = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getJSONObject("bounding_box").getString("coordinates"));}catch(Exception e){this.retweeted_status_place_bounding_box_coordinates = "";}
		try{this.retweeted_status_place_bounding_box_type = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getJSONObject("bounding_box").getString("type"));}catch(Exception e){this.retweeted_status_place_bounding_box_type = "";}
		try{this.retweeted_status_place_country = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getString("country"));}catch(Exception e){this.retweeted_status_place_country = "";}
		try{this.retweeted_status_place_country_code = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getString("country_code"));}catch(Exception e){this.retweeted_status_place_country_code = "";}
		try{this.retweeted_status_place_full_name = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getString("full_name"));}catch(Exception e){this.retweeted_status_place_full_name = "";}
		try{this.retweeted_status_place_id = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getString("id"));}catch(Exception e){this.retweeted_status_place_id = "";}
		try{this.retweeted_status_place_name = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getString("name"));}catch(Exception e){this.retweeted_status_place_name = "";}
		try{this.retweeted_status_place_place_type = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getString("place_type"));}catch(Exception e){this.retweeted_status_place_place_type = "";}
		try{this.retweeted_status_place_url = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("place").getString("url"));}catch(Exception e){this.retweeted_status_place_url = "";}
		try{this.retweeted_status_possibly_sensitive = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("possibly_sensitive"));}catch(Exception e){this.retweeted_status_possibly_sensitive = "";}
		try{this.retweeted_status_retweet_count = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("retweet_count"));}catch(Exception e){this.retweeted_status_retweet_count = "";}
		try{this.retweeted_status_retweeted = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("retweeted"));}catch(Exception e){this.retweeted_status_retweeted = "";}
		try{this.retweeted_status_source = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("source"));}catch(Exception e){this.retweeted_status_source = "";}
		try{this.retweeted_status_text = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("text"));}catch(Exception e){this.retweeted_status_text = "";}
		try{this.retweeted_status_truncated = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("truncated"));}catch(Exception e){this.retweeted_status_truncated = "";}
		try{this.retweeted_status_user_contributors_enabled = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("contributors_enabled"));}catch(Exception e){this.retweeted_status_user_contributors_enabled = "";}
		try{this.retweeted_status_user_created_at = Util.DateConversion(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("created_at"),"Twitter");}catch(Exception e){this.retweeted_status_user_created_at = "";}
		try{this.retweeted_status_user_default_profile = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("default_profile"));}catch(Exception e){this.retweeted_status_user_default_profile = "";}
		try{this.retweeted_status_user_default_profile_image = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("default_profile_image"));}catch(Exception e){this.retweeted_status_user_default_profile_image = "";}
		try{this.retweeted_status_user_description = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("description"));}catch(Exception e){this.retweeted_status_user_description = "";}
		try{this.retweeted_status_user_entities_url_urls = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getJSONObject("entities").getJSONObject("url").getString("urls"));}catch(Exception e){this.retweeted_status_user_entities_url_urls = "";}
		try{this.retweeted_status_user_entities_description_urls = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getJSONObject("entities").getJSONObject("description").getString("urls"));}catch(Exception e){this.retweeted_status_user_entities_description_urls = "";}
		try{this.retweeted_status_user_favourites_count = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("favourites_count"));}catch(Exception e){this.retweeted_status_user_favourites_count = "";}
		try{this.retweeted_status_user_follow_request_sent = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("follow_request_sent"));}catch(Exception e){this.retweeted_status_user_follow_request_sent = "";}
		try{this.retweeted_status_user_following = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("following"));}catch(Exception e){this.retweeted_status_user_following = "";}
		try{this.retweeted_status_user_followers_count = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("followers_count"));}catch(Exception e){this.retweeted_status_user_followers_count = "";}
		try{this.retweeted_status_user_friends_count = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("friends_count"));}catch(Exception e){this.retweeted_status_user_friends_count = "";}
		try{this.retweeted_status_user_geo_enabled = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("geo_enabled"));}catch(Exception e){this.retweeted_status_user_geo_enabled = "";}
		try{this.retweeted_status_user_id = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("id"));}catch(Exception e){this.retweeted_status_user_id = "";}
		try{this.retweeted_status_user_id_str = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("id_str"));}catch(Exception e){this.retweeted_status_user_id_str = "";}
		try{this.retweeted_status_user_is_translator = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("is_translator"));}catch(Exception e){this.retweeted_status_user_is_translator = "";}
		try{this.retweeted_status_user_lang = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("lang"));}catch(Exception e){this.retweeted_status_user_lang = "";}
		try{this.retweeted_status_user_listed_count = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("listed_count"));}catch(Exception e){this.retweeted_status_user_listed_count = "";}
		try{this.retweeted_status_user_location = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("location"));}catch(Exception e){this.retweeted_status_user_location = "";}
		try{this.retweeted_status_user_name = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("name"));}catch(Exception e){this.retweeted_status_user_name = "";}
		try{this.retweeted_status_user_notifications = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("notifications"));}catch(Exception e){this.retweeted_status_user_notifications = "";}
		try{this.retweeted_status_user_profile_background_color = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("profile_background_color"));}catch(Exception e){this.retweeted_status_user_profile_background_color = "";}
		try{this.retweeted_status_user_profile_background_image_url = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("profile_background_image_url"));}catch(Exception e){this.retweeted_status_user_profile_background_image_url = "";}
		try{this.retweeted_status_user_profile_background_image_url_https = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("profile_background_image_url_https"));}catch(Exception e){this.retweeted_status_user_profile_background_image_url_https = "";}
		try{this.retweeted_status_user_profile_background_tile = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("profile_background_tile"));}catch(Exception e){this.retweeted_status_user_profile_background_tile = "";}
		try{this.retweeted_status_user_profile_banner_url = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("profile_banner_url"));}catch(Exception e){this.retweeted_status_user_profile_banner_url = "";}
		try{this.retweeted_status_user_profile_image_url = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("profile_image_url"));}catch(Exception e){this.retweeted_status_user_profile_image_url = "";}
		try{this.retweeted_status_user_profile_image_url_https = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("profile_image_url_https"));}catch(Exception e){this.retweeted_status_user_profile_image_url_https = "";}
		try{this.retweeted_status_user_profile_link_color = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("profile_link_color"));}catch(Exception e){this.retweeted_status_user_profile_link_color = "";}
		try{this.retweeted_status_user_profile_sidebar_border_color = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("profile_sidebar_border_color"));}catch(Exception e){this.retweeted_status_user_profile_sidebar_border_color = "";}
		try{this.retweeted_status_user_profile_sidebar_fill_color = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("profile_sidebar_fill_color"));}catch(Exception e){this.retweeted_status_user_profile_sidebar_fill_color = "";}
		try{this.retweeted_status_user_profile_text_color = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("profile_text_color"));}catch(Exception e){this.retweeted_status_user_profile_text_color = "";}
		try{this.retweeted_status_user_profile_use_background_image = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("profile_use_background_image"));}catch(Exception e){this.retweeted_status_user_profile_use_background_image = "";}
		try{this.retweeted_status_user_protected = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("protected"));}catch(Exception e){this.retweeted_status_user_protected = "";}
		try{this.retweeted_status_user_screen_name = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("screen_name"));}catch(Exception e){this.retweeted_status_user_screen_name = "";}
		try{this.retweeted_status_user_show_all_inline_media = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("show_all_inline_media"));}catch(Exception e){this.retweeted_status_user_show_all_inline_media = "";}
		try{this.retweeted_status_user_status = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("status"));}catch(Exception e){this.retweeted_status_user_status = "";}
		try{this.retweeted_status_user_statuses_count = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("statuses_count"));}catch(Exception e){this.retweeted_status_user_statuses_count = "";}
		try{this.retweeted_status_user_time_zone = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("time_zone"));}catch(Exception e){this.retweeted_status_user_time_zone = "";}
		try{this.retweeted_status_user_url = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("url"));}catch(Exception e){this.retweeted_status_user_url = "";}
		try{this.retweeted_status_user_utc_offset = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("utc_offset"));}catch(Exception e){this.retweeted_status_user_utc_offset = "";}
		try{this.retweeted_status_user_verified = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("verified"));}catch(Exception e){this.retweeted_status_user_verified = "";}
		try{this.retweeted_status_user_withheld_in_countries = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("withheld_in_countries"));}catch(Exception e){this.retweeted_status_user_withheld_in_countries = "";}
		try{this.retweeted_status_user_withheld_scope = Util.EscapeString(vo.getJSONObject("retweeted_status").getJSONObject("user").getString("withheld_scope"));}catch(Exception e){this.retweeted_status_user_withheld_scope = "";}
		try{this.retweeted_status_withheld_copyright = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("withheld_copyright"));}catch(Exception e){this.retweeted_status_withheld_copyright = "";}
		try{this.retweeted_status_withheld_in_countries = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("withheld_in_countries"));}catch(Exception e){this.retweeted_status_withheld_in_countries = "";}
		try{this.retweeted_status_withheld_scope = Util.EscapeString(vo.getJSONObject("retweeted_status").getString("withheld_scope"));}catch(Exception e){this.retweeted_status_withheld_scope = "";}
	}

	public String toString() {
		return String
				.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
						contributors,
						coordinates,
						created_at,
						current_user_retweet_id,
						current_user_retweet_id_str,
						entities_hashtags,
						entities_symbols,
						entities_urls,
						entities_user_mentions,
						entities_media,
						favorite_count,
						favorited,
						filter_level,
						id,
						id_str,
						in_reply_to_screen_name,
						in_reply_to_status_id,
						in_reply_to_status_id_str,
						in_reply_to_user_id,
						in_reply_to_user_id_str,
						lang,
						place_attributes_street_address,
						place_attributes_locality,
						place_attributes_region,
						place_attributes_iso3,
						place_attributes_postal_code,
						place_attributes_phone,
						place_attributes_twitter,
						place_attributes_url,
						place_bounding_box_coordinates,
						place_bounding_box_type,
						place_country,
						place_country_code,
						place_full_name,
						place_id,
						place_name,
						place_place_type,
						place_url,
						possibly_sensitive,
						retweet_count,
						retweeted,
						source,
						text,
						truncated,
						user_contributors_enabled,
						user_created_at,
						user_default_profile,
						user_default_profile_image,
						user_description,
						user_entities_url_urls,
						user_entities_description_urls,
						user_favourites_count,
						user_follow_request_sent,
						user_following,
						user_followers_count,
						user_friends_count,
						user_geo_enabled,
						user_id,
						user_id_str,
						user_is_translator,
						user_lang,
						user_listed_count,
						user_location,
						user_name,
						user_notifications,
						user_profile_background_color,
						user_profile_background_image_url,
						user_profile_background_image_url_https,
						user_profile_background_tile,
						user_profile_banner_url,
						user_profile_image_url,
						user_profile_image_url_https,
						user_profile_link_color,
						user_profile_sidebar_border_color,
						user_profile_sidebar_fill_color,
						user_profile_text_color,
						user_profile_use_background_image,
						user_protected,
						user_screen_name,
						user_show_all_inline_media,
						user_status,
						user_statuses_count,
						user_time_zone,
						user_url,
						user_utc_offset,
						user_verified,
						user_withheld_in_countries,
						user_withheld_scope,
						withheld_copyright,
						withheld_in_countries,
						withheld_scope,
						retweeted_status_contributors,
						retweeted_status_coordinates,
						retweeted_status_created_at,
						retweeted_status_current_user_retweet_id,
						retweeted_status_current_user_retweet_id_str,
						retweeted_status_entities_hashtags,
						retweeted_status_entities_symbols,
						retweeted_status_entities_urls,
						retweeted_status_entities_user_mentions,
						retweeted_status_entities_media_id,
						retweeted_status_favorite_count,
						retweeted_status_favorited,
						retweeted_status_filter_level,
						retweeted_status_id,
						retweeted_status_id_str,
						retweeted_status_in_reply_to_screen_name,
						retweeted_status_in_reply_to_status_id,
						retweeted_status_in_reply_to_status_id_str,
						retweeted_status_in_reply_to_user_id,
						retweeted_status_in_reply_to_user_id_str,
						retweeted_status_lang,
						retweeted_status_place_attributes_street_address,
						retweeted_status_place_attributes_locality,
						retweeted_status_place_attributes_region,
						retweeted_status_place_attributes_iso3,
						retweeted_status_place_attributes_postal_code,
						retweeted_status_place_attributes_phone,
						retweeted_status_place_attributes_twitter,
						retweeted_status_place_attributes_url,
						retweeted_status_place_bounding_box_coordinates,
						retweeted_status_place_bounding_box_type,
						retweeted_status_place_country,
						retweeted_status_place_country_code,
						retweeted_status_place_full_name,
						retweeted_status_place_id,
						retweeted_status_place_name,
						retweeted_status_place_place_type,
						retweeted_status_place_url,
						retweeted_status_possibly_sensitive,
						retweeted_status_retweet_count,
						retweeted_status_retweeted,
						retweeted_status_source,
						retweeted_status_text,
						retweeted_status_truncated,
						retweeted_status_user_contributors_enabled,
						retweeted_status_user_created_at,
						retweeted_status_user_default_profile,
						retweeted_status_user_default_profile_image,
						retweeted_status_user_description,
						retweeted_status_user_entities_url_urls,
						retweeted_status_user_entities_description_urls,
						retweeted_status_user_favourites_count,
						retweeted_status_user_follow_request_sent,
						retweeted_status_user_following,
						retweeted_status_user_followers_count,
						retweeted_status_user_friends_count,
						retweeted_status_user_geo_enabled,
						retweeted_status_user_id,
						retweeted_status_user_id_str,
						retweeted_status_user_is_translator,
						retweeted_status_user_lang,
						retweeted_status_user_listed_count,
						retweeted_status_user_location,
						retweeted_status_user_name,
						retweeted_status_user_notifications,
						retweeted_status_user_profile_background_color,
						retweeted_status_user_profile_background_image_url,
						retweeted_status_user_profile_background_image_url_https,
						retweeted_status_user_profile_background_tile,
						retweeted_status_user_profile_banner_url,
						retweeted_status_user_profile_image_url,
						retweeted_status_user_profile_image_url_https,
						retweeted_status_user_profile_link_color,
						retweeted_status_user_profile_sidebar_border_color,
						retweeted_status_user_profile_sidebar_fill_color,
						retweeted_status_user_profile_text_color,
						retweeted_status_user_profile_use_background_image,
						retweeted_status_user_protected,
						retweeted_status_user_screen_name,
						retweeted_status_user_show_all_inline_media,
						retweeted_status_user_status,
						retweeted_status_user_statuses_count,
						retweeted_status_user_time_zone,
						retweeted_status_user_url,
						retweeted_status_user_utc_offset,
						retweeted_status_user_verified,
						retweeted_status_user_withheld_in_countries,
						retweeted_status_user_withheld_scope,
						retweeted_status_withheld_copyright,
						retweeted_status_withheld_in_countries,
						retweeted_status_withheld_scope);
	}
	public String[] getAttributeValueList() {
		return new String[]{contributors, coordinates, created_at,
				current_user_retweet_id, current_user_retweet_id_str,
				entities_hashtags, entities_symbols, entities_urls,
				entities_user_mentions, entities_media, favorite_count,
				favorited, filter_level, id, id_str, in_reply_to_screen_name,
				in_reply_to_status_id, in_reply_to_status_id_str,
				in_reply_to_user_id, in_reply_to_user_id_str, lang,
				place_attributes_street_address, place_attributes_locality,
				place_attributes_region, place_attributes_iso3,
				place_attributes_postal_code, place_attributes_phone,
				place_attributes_twitter, place_attributes_url,
				place_bounding_box_coordinates, place_bounding_box_type,
				place_country, place_country_code, place_full_name, place_id,
				place_name, place_place_type, place_url, possibly_sensitive,
				retweet_count, retweeted, source, text, truncated,
				user_contributors_enabled, user_created_at,
				user_default_profile, user_default_profile_image,
				user_description, user_entities_url_urls,
				user_entities_description_urls, user_favourites_count,
				user_follow_request_sent, user_following, user_followers_count,
				user_friends_count, user_geo_enabled, user_id, user_id_str,
				user_is_translator, user_lang, user_listed_count,
				user_location, user_name, user_notifications,
				user_profile_background_color,
				user_profile_background_image_url,
				user_profile_background_image_url_https,
				user_profile_background_tile, user_profile_banner_url,
				user_profile_image_url, user_profile_image_url_https,
				user_profile_link_color, user_profile_sidebar_border_color,
				user_profile_sidebar_fill_color, user_profile_text_color,
				user_profile_use_background_image, user_protected,
				user_screen_name, user_show_all_inline_media, user_status,
				user_statuses_count, user_time_zone, user_url, user_utc_offset,
				user_verified, user_withheld_in_countries, user_withheld_scope,
				withheld_copyright, withheld_in_countries, withheld_scope,
				retweeted_status_contributors, retweeted_status_coordinates,
				retweeted_status_created_at,
				retweeted_status_current_user_retweet_id,
				retweeted_status_current_user_retweet_id_str,
				retweeted_status_entities_hashtags,
				retweeted_status_entities_symbols,
				retweeted_status_entities_urls,
				retweeted_status_entities_user_mentions,
				retweeted_status_entities_media_id,
				retweeted_status_favorite_count, retweeted_status_favorited,
				retweeted_status_filter_level, retweeted_status_id,
				retweeted_status_id_str,
				retweeted_status_in_reply_to_screen_name,
				retweeted_status_in_reply_to_status_id,
				retweeted_status_in_reply_to_status_id_str,
				retweeted_status_in_reply_to_user_id,
				retweeted_status_in_reply_to_user_id_str,
				retweeted_status_lang,
				retweeted_status_place_attributes_street_address,
				retweeted_status_place_attributes_locality,
				retweeted_status_place_attributes_region,
				retweeted_status_place_attributes_iso3,
				retweeted_status_place_attributes_postal_code,
				retweeted_status_place_attributes_phone,
				retweeted_status_place_attributes_twitter,
				retweeted_status_place_attributes_url,
				retweeted_status_place_bounding_box_coordinates,
				retweeted_status_place_bounding_box_type,
				retweeted_status_place_country,
				retweeted_status_place_country_code,
				retweeted_status_place_full_name, retweeted_status_place_id,
				retweeted_status_place_name, retweeted_status_place_place_type,
				retweeted_status_place_url,
				retweeted_status_possibly_sensitive,
				retweeted_status_retweet_count, retweeted_status_retweeted,
				retweeted_status_source, retweeted_status_text,
				retweeted_status_truncated,
				retweeted_status_user_contributors_enabled,
				retweeted_status_user_created_at,
				retweeted_status_user_default_profile,
				retweeted_status_user_default_profile_image,
				retweeted_status_user_description,
				retweeted_status_user_entities_url_urls,
				retweeted_status_user_entities_description_urls,
				retweeted_status_user_favourites_count,
				retweeted_status_user_follow_request_sent,
				retweeted_status_user_following,
				retweeted_status_user_followers_count,
				retweeted_status_user_friends_count,
				retweeted_status_user_geo_enabled, retweeted_status_user_id,
				retweeted_status_user_id_str,
				retweeted_status_user_is_translator,
				retweeted_status_user_lang, retweeted_status_user_listed_count,
				retweeted_status_user_location, retweeted_status_user_name,
				retweeted_status_user_notifications,
				retweeted_status_user_profile_background_color,
				retweeted_status_user_profile_background_image_url,
				retweeted_status_user_profile_background_image_url_https,
				retweeted_status_user_profile_background_tile,
				retweeted_status_user_profile_banner_url,
				retweeted_status_user_profile_image_url,
				retweeted_status_user_profile_image_url_https,
				retweeted_status_user_profile_link_color,
				retweeted_status_user_profile_sidebar_border_color,
				retweeted_status_user_profile_sidebar_fill_color,
				retweeted_status_user_profile_text_color,
				retweeted_status_user_profile_use_background_image,
				retweeted_status_user_protected,
				retweeted_status_user_screen_name,
				retweeted_status_user_show_all_inline_media,
				retweeted_status_user_status,
				retweeted_status_user_statuses_count,
				retweeted_status_user_time_zone, retweeted_status_user_url,
				retweeted_status_user_utc_offset,
				retweeted_status_user_verified,
				retweeted_status_user_withheld_in_countries,
				retweeted_status_user_withheld_scope,
				retweeted_status_withheld_copyright,
				retweeted_status_withheld_in_countries,
				retweeted_status_withheld_scope};
	}
}
