package com.sap.cisp.xhna.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;


public class YoutubeAccountInfo {
	private String etag;
	private String items_id;
	private String items_etag;
	private String items_snippet_publishedAt;
	private String items_snippet_title;
	private String items_snippet_description;
	private String items_snippet_thumbnails_default_url;
	private String items_snippet_thumbnails_high_url;
	private String items_snippet_thumbnails_medium_url;
	private String items_snippet_localized_title;
	private String items_snippet_localized_description;
	private String items_contentDetails_relatedPlaylists_watchLater;
	private String items_contentDetails_relatedPlaylists_watchHistory;
	private String items_contentDetails_relatedPlaylists_likes;
	private String items_contentDetails_relatedPlaylists_favorites;
	private String items_contentDetails_relatedPlaylists_uploads;
	private String items_contentDetails_googlePlusUserId;
	private String items_kind;
	private String items_statistics_subscriberCount;
	private String items_statistics_videoCount;
	private String items_statistics_hiddenSubscriberCount;
	private String items_statistics_commentCount;
	private String items_statistics_viewCount;
	private String kind;
	private String crawl_time;
	private String account_name;
	
	public YoutubeAccountInfo(String JsonString, String key) {
		this.crawl_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
		this.account_name = key;
		
		JSONObject vo = JSON.parseObject(JsonString);
		try{this.etag = Util.EscapeString(vo.getString("etag"));}catch(Exception e){this.etag = "";}
		JSONObject items = (JSONObject) vo.getJSONArray("items").get(0);
		try{this.items_id = Util.EscapeString(items.getString("id"));}catch(Exception e){this.items_id = "";}
		try{this.items_etag = Util.EscapeString(items.getString("etag"));}catch(Exception e){this.items_etag = "";}
		try{this.items_snippet_publishedAt = Util.DateConversion(items.getJSONObject("snippet").getString("publishedAt"), "Youtube");}catch(Exception e){this.items_snippet_publishedAt = "";}
		try{this.items_snippet_title = Util.EscapeString(items.getJSONObject("snippet").getString("title"));}catch(Exception e){this.items_snippet_title = "";}
		try{this.items_snippet_description = Util.EscapeString(items.getJSONObject("snippet").getString("description"));}catch(Exception e){this.items_snippet_description = "";}
		try{this.items_snippet_thumbnails_default_url = Util.EscapeString(items.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url"));}catch(Exception e){this.items_snippet_thumbnails_default_url = "";}
		try{this.items_snippet_thumbnails_high_url = Util.EscapeString(items.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high").getString("url"));}catch(Exception e){this.items_snippet_thumbnails_high_url = "";}
		try{this.items_snippet_thumbnails_medium_url = Util.EscapeString(items.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("medium").getString("url"));}catch(Exception e){this.items_snippet_thumbnails_medium_url = "";}
		try{this.items_snippet_localized_title = Util.EscapeString(items.getJSONObject("snippet").getJSONObject("localized").getString("title"));}catch(Exception e){this.items_snippet_localized_title = "";}
		try{this.items_snippet_localized_description = Util.EscapeString(items.getJSONObject("snippet").getJSONObject("localized").getString("description"));}catch(Exception e){this.items_snippet_localized_description = "";}
		try{this.items_contentDetails_relatedPlaylists_watchLater = Util.EscapeString(items.getJSONObject("contentDetails").getJSONObject("relatedPlaylists").getString("watchLater"));}catch(Exception e){this.items_contentDetails_relatedPlaylists_watchLater = "";}
		try{this.items_contentDetails_relatedPlaylists_watchHistory = Util.EscapeString(items.getJSONObject("contentDetails").getJSONObject("relatedPlaylists").getString("watchHistory"));}catch(Exception e){this.items_contentDetails_relatedPlaylists_watchHistory = "";}
		try{this.items_contentDetails_relatedPlaylists_likes = Util.EscapeString(items.getJSONObject("contentDetails").getJSONObject("relatedPlaylists").getString("likes"));}catch(Exception e){this.items_contentDetails_relatedPlaylists_likes = "";}
		try{this.items_contentDetails_relatedPlaylists_favorites = Util.EscapeString(items.getJSONObject("contentDetails").getJSONObject("relatedPlaylists").getString("favorites"));}catch(Exception e){this.items_contentDetails_relatedPlaylists_favorites = "";}
		try{this.items_contentDetails_relatedPlaylists_uploads = Util.EscapeString(items.getJSONObject("contentDetails").getJSONObject("relatedPlaylists").getString("uploads"));}catch(Exception e){this.items_contentDetails_relatedPlaylists_uploads = "";}
		try{this.items_contentDetails_googlePlusUserId = Util.EscapeString(items.getJSONObject("contentDetails").getString("googlePlusUserId"));}catch(Exception e){this.items_contentDetails_googlePlusUserId = "";}
		try{this.items_kind = Util.EscapeString(items.getString("kind"));}catch(Exception e){this.items_kind = "";}
		try{this.items_statistics_subscriberCount = Util.EscapeString(items.getJSONObject("statistics").getString("subscriberCount"));}catch(Exception e){this.items_statistics_subscriberCount = "";}
		try{this.items_statistics_videoCount = Util.EscapeString(items.getJSONObject("statistics").getString("videoCount"));}catch(Exception e){this.items_statistics_videoCount = "";}
		try{this.items_statistics_hiddenSubscriberCount = Util.EscapeString(items.getJSONObject("statistics").getString("hiddenSubscriberCount"));}catch(Exception e){this.items_statistics_hiddenSubscriberCount = "";}
		try{this.items_statistics_commentCount = Util.EscapeString(items.getJSONObject("statistics").getString("commentCount"));}catch(Exception e){this.items_statistics_commentCount = "";}
		try{this.items_statistics_viewCount = Util.EscapeString(items.getJSONObject("statistics").getString("viewCount"));}catch(Exception e){this.items_statistics_viewCount = "";}
		try{this.kind = Util.EscapeString(vo.getString("kind"));}catch(Exception e){this.kind = "";}
	}
	
	public String toString() {
		return String
				.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
						etag, items_id, items_etag, items_snippet_publishedAt,
						items_snippet_title, items_snippet_description,
						items_snippet_thumbnails_default_url,
						items_snippet_thumbnails_high_url,
						items_snippet_thumbnails_medium_url,
						items_snippet_localized_title,
						items_snippet_localized_description,
						items_contentDetails_relatedPlaylists_watchLater,
						items_contentDetails_relatedPlaylists_watchHistory,
						items_contentDetails_relatedPlaylists_likes,
						items_contentDetails_relatedPlaylists_favorites,
						items_contentDetails_relatedPlaylists_uploads,
						items_contentDetails_googlePlusUserId, items_kind,
						items_statistics_subscriberCount,
						items_statistics_videoCount,
						items_statistics_hiddenSubscriberCount,
						items_statistics_commentCount,
						items_statistics_viewCount, kind,
						account_name, crawl_time);
	}
	
	public String[] getAttributeValueList() {
		return new String[]{etag, items_id, items_etag, items_snippet_publishedAt,
				items_snippet_title, items_snippet_description,
				items_snippet_thumbnails_default_url,
				items_snippet_thumbnails_high_url,
				items_snippet_thumbnails_medium_url,
				items_snippet_localized_title,
				items_snippet_localized_description,
				items_contentDetails_relatedPlaylists_watchLater,
				items_contentDetails_relatedPlaylists_watchHistory,
				items_contentDetails_relatedPlaylists_likes,
				items_contentDetails_relatedPlaylists_favorites,
				items_contentDetails_relatedPlaylists_uploads,
				items_contentDetails_googlePlusUserId, items_kind,
				items_statistics_subscriberCount,
				items_statistics_videoCount,
				items_statistics_hiddenSubscriberCount,
				items_statistics_commentCount,
				items_statistics_viewCount, kind,
				account_name, crawl_time};
	}
}