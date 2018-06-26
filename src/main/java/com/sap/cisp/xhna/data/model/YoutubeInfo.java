package com.sap.cisp.xhna.data.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.Util;

public class YoutubeInfo {
	private String etag;
	private String player_embedHtml;
	private String status_publicStatsViewable;
	private String status_rejectionReason;
	private String status_privacyStatus;
	private String status_embeddable;
	private String status_uploadStatus;
	private String status_failureReason;
	private String status_license;
	private String status_publishAt;
	private String processingDetails_tagSuggestionsAvailability;
	private String processingDetails_thumbnailsAvailability;
	private String processingDetails_processingStatus;
	private String processingDetails_processingIssuesAvailability;
	private String processingDetails_processingProgress_partsProcessed;
	private String processingDetails_processingProgress_partsTotal;
	private String processingDetails_processingProgress_timeLeftMs;
	private String processingDetails_editorSuggestionsAvailability;
	private String processingDetails_fileDetailsAvailability;
	private String processingDetails_processingFailureReason;
	private String liveStreamingDetails_concurrentViewers;
	private String liveStreamingDetails_actualStartTime;
	private String liveStreamingDetails_scheduledEndTime;
	private String liveStreamingDetails_scheduledStartTime;
	private String liveStreamingDetails_actualEndTime;
	private String snippet_tags;
	private String snippet_publishedAt;
	private String snippet_title;
	private String snippet_channelId;
	private String snippet_description;
	private String snippet_categoryId;
	private String snippet_channelTitle;
	private String snippet_thumbnails_default_height;
	private String snippet_thumbnails_default_width;
	private String snippet_thumbnails_default_url;
	private String snippet_thumbnails_standard_height;
	private String snippet_thumbnails_standard_width;
	private String snippet_thumbnails_standard_url;
	private String snippet_thumbnails_high_height;
	private String snippet_thumbnails_high_width;
	private String snippet_thumbnails_high_url;
	private String snippet_thumbnails_medium_height;
	private String snippet_thumbnails_medium_width;
	private String snippet_thumbnails_medium_url;
	private String snippet_thumbnails_maxres_height;
	private String snippet_thumbnails_maxres_width;
	private String snippet_thumbnails_maxres_url;
	private String snippet_liveBroadcastContent;
	private String snippet_localized_title;
	private String snippet_localized_description;
	private String suggestions_editorSuggestions;
	private String suggestions_processingHints;
	private String suggestions_tagSuggestions;
	private String suggestions_processingWarnings;
	private String suggestions_processingErrors;
	private String kind;
	private String statistics_favoriteCount;
	private String statistics_dislikeCount;
	private String statistics_likeCount;
	private String statistics_commentCount;
	private String statistics_viewCount;
	private String topicDetails_topicIds;
	private String topicDetails_relevantTopicIds;
	private String id;
	private String recordingDetails_recordingDate;
	private String recordingDetails_location_altitude;
	private String recordingDetails_location_longitude;
	private String recordingDetails_location_latitude;
	private String recordingDetails_locationDescription;
	private String contentDetails_dimension;
	private String contentDetails_duration;
	private String contentDetails_licensedContent;
	private String contentDetails_definition;
	private String contentDetails_contentRating_nbcplRating;
	private String contentDetails_contentRating_cicfRating;
	private String contentDetails_contentRating_mccaaRating;
	private String contentDetails_contentRating_eirinRating;
	private String contentDetails_contentRating_cnaRating;
	private String contentDetails_contentRating_bmukkRating;
	private String contentDetails_contentRating_medietilsynetRating;
	private String contentDetails_contentRating_fmocRating;
	private String contentDetails_contentRating_resorteviolenciaRating;
	private String contentDetails_contentRating_nfrcRating;
	private String contentDetails_contentRating_catvfrRating;
	private String contentDetails_contentRating_cscfRating;
	private String contentDetails_contentRating_mccypRating;
	private String contentDetails_contentRating_cceRating;
	private String contentDetails_contentRating_tvpgRating;
	private String contentDetails_contentRating_rtcRating;
	private String contentDetails_contentRating_anatelRating;
	private String contentDetails_contentRating_skfilmRating;
	private String contentDetails_contentRating_kmrbRating;
	private String contentDetails_contentRating_mdaRating;
	private String contentDetails_contentRating_icaaRating;
	private String contentDetails_contentRating_mpaaRating;
	private String contentDetails_contentRating_fskRating;
	private String contentDetails_contentRating_chvrsRating;
	private String contentDetails_contentRating_cccRating;
	private String contentDetails_contentRating_acbRating;
	private String contentDetails_contentRating_mibacRating;
	private String contentDetails_contentRating_russiaRating;
	private String contentDetails_contentRating_djctqRatingReasons;
	private String contentDetails_contentRating_kijkwijzerRating;
	private String contentDetails_contentRating_smaisRating;
	private String contentDetails_contentRating_rcnofRating;
	private String contentDetails_contentRating_csaRating;
	private String contentDetails_contentRating_moctwRating;
	private String contentDetails_contentRating_nfvcbRating;
	private String contentDetails_contentRating_fpbRating;
	private String contentDetails_contentRating_kfcbRating;
	private String contentDetails_contentRating_rteRating;
	private String contentDetails_contentRating_agcomRating;
	private String contentDetails_contentRating_ilfilmRating;
	private String contentDetails_contentRating_mtrcbRating;
	private String contentDetails_contentRating_czfilmRating;
	private String contentDetails_contentRating_fcbmRating;
	private String contentDetails_contentRating_grfilmRating;
	private String contentDetails_contentRating_cbfcRating;
	private String contentDetails_contentRating_lsfRating;
	private String contentDetails_contentRating_oflcRating;
	private String contentDetails_contentRating_egfilmRating;
	private String contentDetails_contentRating_pefilmRating;
	private String contentDetails_contentRating_bfvcRating;
	private String contentDetails_contentRating_catvRating;
	private String contentDetails_contentRating_smsaRating;
	private String contentDetails_contentRating_bbfcRating;
	private String contentDetails_contentRating_chfilmRating;
	private String contentDetails_contentRating_djctqRating;
	private String contentDetails_contentRating_ytRating;
	private String contentDetails_contentRating_nkclvRating;
	private String contentDetails_contentRating_nbcRating;
	private String contentDetails_contentRating_mekuRating;
	private String contentDetails_contentRating_incaaRating;
	private String contentDetails_contentRating_eefilmRating;
	private String contentDetails_contentRating_ifcoRating;
	private String contentDetails_contentRating_mocRating;
	private String contentDetails_contentRating_fcoRating;
	private String contentDetails_caption;
	private String contentDetails_regionRestriction_allowed;
	private String contentDetails_regionRestriction_blocked;
	private String fileDetails_recordingLocation_altitude;
	private String fileDetails_recordingLocation_longitude;
	private String fileDetails_recordingLocation_latitude;
	private String fileDetails_bitrateBps;
	private String fileDetails_fileSize;
	private String fileDetails_durationMs;
	private String fileDetails_fileType;
	private String fileDetails_container;
	private String fileDetails_fileName;
	private String fileDetails_creationTime;
	private String fileDetails_videoStreams;
	private String fileDetails_audioStreams;

	public YoutubeInfo(String JsonString) {
		// TODO Auto-generated constructor stub
		JSONObject vo = JSON.parseObject(JsonString);
		vo = (JSONObject) vo.getJSONArray("items").get(0);
		try{this.etag = Util.EscapeString(vo.getString("etag"));}catch(Exception e){this.etag="";}
		try{this.player_embedHtml = Util.EscapeString(vo.getJSONObject("player").getString("embedHtml"));}catch(Exception e){this.player_embedHtml = "";}
		try{this.status_publicStatsViewable = Util.EscapeString(vo.getJSONObject("status").getString("publicStatsViewable"));}catch(Exception e){this.status_publicStatsViewable = "";}
		try{this.status_rejectionReason = Util.EscapeString(vo.getJSONObject("status").getString("rejectionReason"));}catch(Exception e){this.status_rejectionReason = "";}
		try{this.status_privacyStatus = Util.EscapeString(vo.getJSONObject("status").getString("privacyStatus"));}catch(Exception e){this.status_privacyStatus = "";}
		try{this.status_embeddable = Util.EscapeString(vo.getJSONObject("status").getString("embeddable"));}catch(Exception e){this.status_embeddable = "";}
		try{this.status_uploadStatus = Util.EscapeString(vo.getJSONObject("status").getString("uploadStatus"));}catch(Exception e){this.status_uploadStatus = "";}
		try{this.status_failureReason = Util.EscapeString(vo.getJSONObject("status").getString("failureReason"));}catch(Exception e){this.status_failureReason = "";}
		try{this.status_license = Util.EscapeString(vo.getJSONObject("status").getString("license"));}catch(Exception e){this.status_license = "";}
		try{this.status_publishAt = Util.DateConversion(vo.getJSONObject("status").getString("publishAt"),"Youtube");}catch(Exception e){this.status_publishAt = "";}
		try{this.processingDetails_tagSuggestionsAvailability = Util.EscapeString(vo.getJSONObject("processingDetails").getString("tagSuggestionsAvailability"));}catch(Exception e){this.processingDetails_tagSuggestionsAvailability = "";}
		try{this.processingDetails_thumbnailsAvailability = Util.EscapeString(vo.getJSONObject("processingDetails").getString("thumbnailsAvailability"));}catch(Exception e){this.processingDetails_thumbnailsAvailability = "";}
		try{this.processingDetails_processingStatus = Util.EscapeString(vo.getJSONObject("processingDetails").getString("processingStatus"));}catch(Exception e){this.processingDetails_processingStatus = "";}
		try{this.processingDetails_processingIssuesAvailability = Util.EscapeString(vo.getJSONObject("processingDetails").getString("processingIssuesAvailability"));}catch(Exception e){this.processingDetails_processingIssuesAvailability = "";}
		try{this.processingDetails_processingProgress_partsProcessed = Util.EscapeString(vo.getJSONObject("processingDetails").getJSONObject("processingProgress").getString("partsProcessed"));}catch(Exception e){this.processingDetails_processingProgress_partsProcessed = "";}
		try{this.processingDetails_processingProgress_partsTotal = Util.EscapeString(vo.getJSONObject("processingDetails").getJSONObject("processingProgress").getString("partsTotal"));}catch(Exception e){this.processingDetails_processingProgress_partsTotal = "";}
		try{this.processingDetails_processingProgress_timeLeftMs = Util.EscapeString(vo.getJSONObject("processingDetails").getJSONObject("processingProgress").getString("timeLeftMs"));}catch(Exception e){this.processingDetails_processingProgress_timeLeftMs = "";}
		try{this.processingDetails_editorSuggestionsAvailability = Util.EscapeString(vo.getJSONObject("processingDetails").getString("editorSuggestionsAvailability"));}catch(Exception e){this.processingDetails_editorSuggestionsAvailability = "";}
		try{this.processingDetails_fileDetailsAvailability = Util.EscapeString(vo.getJSONObject("processingDetails").getString("fileDetailsAvailability"));}catch(Exception e){this.processingDetails_fileDetailsAvailability = "";}
		try{this.processingDetails_processingFailureReason = Util.EscapeString(vo.getJSONObject("processingDetails").getString("processingFailureReason"));}catch(Exception e){this.processingDetails_processingFailureReason = "";}
		try{this.liveStreamingDetails_concurrentViewers = Util.EscapeString(vo.getJSONObject("liveStreamingDetails").getString("concurrentViewers"));}catch(Exception e){this.liveStreamingDetails_concurrentViewers = "";}
		try{this.liveStreamingDetails_actualStartTime = Util.DateConversion(vo.getJSONObject("liveStreamingDetails").getString("actualStartTime"),"Youtube");}catch(Exception e){this.liveStreamingDetails_actualStartTime = "";}
		try{this.liveStreamingDetails_scheduledEndTime = Util.DateConversion(vo.getJSONObject("liveStreamingDetails").getString("scheduledEndTime"),"Youtube");}catch(Exception e){this.liveStreamingDetails_scheduledEndTime = "";}
		try{this.liveStreamingDetails_scheduledStartTime = Util.DateConversion(vo.getJSONObject("liveStreamingDetails").getString("scheduledStartTime"),"Youtube");}catch(Exception e){this.liveStreamingDetails_scheduledStartTime = "";}
		try{this.liveStreamingDetails_actualEndTime = Util.DateConversion(vo.getJSONObject("liveStreamingDetails").getString("actualEndTime"),"Youtube");}catch(Exception e){this.liveStreamingDetails_actualEndTime = "";}
		try{this.snippet_tags = Util.EscapeString(vo.getJSONObject("snippet").getString("tags"));}catch(Exception e){this.snippet_tags = "";}
		try{this.snippet_publishedAt = Util.DateConversion(vo.getJSONObject("snippet").getString("publishedAt"),"Youtube");}catch(Exception e){this.snippet_publishedAt = "";}
		try{this.snippet_title = Util.EscapeString(vo.getJSONObject("snippet").getString("title"));}catch(Exception e){this.snippet_title = "";}
		try{this.snippet_channelId = Util.EscapeString(vo.getJSONObject("snippet").getString("channelId"));}catch(Exception e){this.snippet_channelId = "";}
		try{this.snippet_description = Util.EscapeString(vo.getJSONObject("snippet").getString("description"));}catch(Exception e){this.snippet_description = "";}
		try{this.snippet_categoryId = Util.EscapeString(vo.getJSONObject("snippet").getString("categoryId"));}catch(Exception e){this.snippet_categoryId = "";}
		try{this.snippet_channelTitle = Util.EscapeString(vo.getJSONObject("snippet").getString("channelTitle"));}catch(Exception e){this.snippet_channelTitle = "";}
		try{this.snippet_thumbnails_default_height = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("height"));}catch(Exception e){this.snippet_thumbnails_default_height = "";}
		try{this.snippet_thumbnails_default_width = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("width"));}catch(Exception e){this.snippet_thumbnails_default_width = "";}
		try{this.snippet_thumbnails_default_url = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url"));}catch(Exception e){this.snippet_thumbnails_default_url = "";}
		try{this.snippet_thumbnails_standard_height = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("standard").getString("height"));}catch(Exception e){this.snippet_thumbnails_standard_height = "";}
		try{this.snippet_thumbnails_standard_width = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("standard").getString("width"));}catch(Exception e){this.snippet_thumbnails_standard_width = "";}
		try{this.snippet_thumbnails_standard_url = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("standard").getString("url"));}catch(Exception e){this.snippet_thumbnails_standard_url = "";}
		try{this.snippet_thumbnails_high_height = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high").getString("height"));}catch(Exception e){this.snippet_thumbnails_high_height = "";}
		try{this.snippet_thumbnails_high_width = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high").getString("width"));}catch(Exception e){this.snippet_thumbnails_high_width = "";}
		try{this.snippet_thumbnails_high_url = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high").getString("url"));}catch(Exception e){this.snippet_thumbnails_high_url = "";}
		try{this.snippet_thumbnails_medium_height = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("medium").getString("height"));}catch(Exception e){this.snippet_thumbnails_medium_height = "";}
		try{this.snippet_thumbnails_medium_width = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("medium").getString("width"));}catch(Exception e){this.snippet_thumbnails_medium_width = "";}
		try{this.snippet_thumbnails_medium_url = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("medium").getString("url"));}catch(Exception e){this.snippet_thumbnails_medium_url = "";}
		try{this.snippet_thumbnails_maxres_height = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("maxres").getString("height"));}catch(Exception e){this.snippet_thumbnails_maxres_height = "";}
		try{this.snippet_thumbnails_maxres_width = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("maxres").getString("width"));}catch(Exception e){this.snippet_thumbnails_maxres_width = "";}
		try{this.snippet_thumbnails_maxres_url = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("maxres").getString("url"));}catch(Exception e){this.snippet_thumbnails_maxres_url = "";}
		try{this.snippet_liveBroadcastContent = Util.EscapeString(vo.getJSONObject("snippet").getString("liveBroadcastContent"));}catch(Exception e){this.snippet_liveBroadcastContent = "";}
		try{this.snippet_localized_title = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("localized").getString("title"));}catch(Exception e){this.snippet_localized_title = "";}
		try{this.snippet_localized_description = Util.EscapeString(vo.getJSONObject("snippet").getJSONObject("localized").getString("description"));}catch(Exception e){this.snippet_localized_description = "";}
		try{this.suggestions_editorSuggestions = Util.EscapeString(vo.getJSONObject("suggestions").getString("editorSuggestions"));}catch(Exception e){this.suggestions_editorSuggestions = "";}
		try{this.suggestions_processingHints = Util.EscapeString(vo.getJSONObject("suggestions").getString("processingHints"));}catch(Exception e){this.suggestions_processingHints = "";}
		try{this.suggestions_tagSuggestions = Util.EscapeString(vo.getJSONObject("suggestions").getString("tagSuggestions"));}catch(Exception e){this.suggestions_tagSuggestions = "";}
		try{this.suggestions_processingWarnings = Util.EscapeString(vo.getJSONObject("suggestions").getString("processingWarnings"));}catch(Exception e){this.suggestions_processingWarnings = "";}
		try{this.suggestions_processingErrors = Util.EscapeString(vo.getJSONObject("suggestions").getString("processingErrors"));}catch(Exception e){this.suggestions_processingErrors = "";}
		try{this.kind = Util.EscapeString(vo.getString("kind"));}catch(Exception e){this.kind="";}
		try{this.statistics_favoriteCount = Util.EscapeString(vo.getJSONObject("statistics").getString("favoriteCount"));}catch(Exception e){this.statistics_favoriteCount = "";}
		try{this.statistics_dislikeCount = Util.EscapeString(vo.getJSONObject("statistics").getString("dislikeCount"));}catch(Exception e){this.statistics_dislikeCount = "";}
		try{this.statistics_likeCount = Util.EscapeString(vo.getJSONObject("statistics").getString("likeCount"));}catch(Exception e){this.statistics_likeCount = "";}
		try{this.statistics_commentCount = Util.EscapeString(vo.getJSONObject("statistics").getString("commentCount"));}catch(Exception e){this.statistics_commentCount = "";}
		try{this.statistics_viewCount = Util.EscapeString(vo.getJSONObject("statistics").getString("viewCount"));}catch(Exception e){this.statistics_viewCount = "";}
		try{this.topicDetails_topicIds = Util.EscapeString(vo.getJSONObject("topicDetails").getString("topicIds"));}catch(Exception e){this.topicDetails_topicIds = "";}
		try{this.topicDetails_relevantTopicIds = Util.EscapeString(vo.getJSONObject("topicDetails").getString("relevantTopicIds"));}catch(Exception e){this.topicDetails_relevantTopicIds = "";}
		try{this.id = Util.EscapeString(vo.getString("id"));}catch(Exception e){this.id="";}
		try{this.recordingDetails_recordingDate = Util.DateConversion(vo.getJSONObject("recordingDetails").getString("recordingDate"),"Youtube");}catch(Exception e){this.recordingDetails_recordingDate = "";}
		try{this.recordingDetails_location_altitude = Util.EscapeString(vo.getJSONObject("recordingDetails").getJSONObject("location").getString("altitude"));}catch(Exception e){this.recordingDetails_location_altitude = "";}
		try{this.recordingDetails_location_longitude = Util.EscapeString(vo.getJSONObject("recordingDetails").getJSONObject("location").getString("longitude"));}catch(Exception e){this.recordingDetails_location_longitude = "";}
		try{this.recordingDetails_location_latitude = Util.EscapeString(vo.getJSONObject("recordingDetails").getJSONObject("location").getString("latitude"));}catch(Exception e){this.recordingDetails_location_latitude = "";}
		try{this.recordingDetails_locationDescription = Util.EscapeString(vo.getJSONObject("recordingDetails").getString("locationDescription"));}catch(Exception e){this.recordingDetails_locationDescription = "";}
		try{this.contentDetails_dimension = Util.EscapeString(vo.getJSONObject("contentDetails").getString("dimension"));}catch(Exception e){this.contentDetails_dimension = "";}
		try{this.contentDetails_duration = Util.EscapeString(vo.getJSONObject("contentDetails").getString("duration"));}catch(Exception e){this.contentDetails_duration = "";}
		try{this.contentDetails_licensedContent = Util.EscapeString(vo.getJSONObject("contentDetails").getString("licensedContent"));}catch(Exception e){this.contentDetails_licensedContent = "";}
		try{this.contentDetails_definition = Util.EscapeString(vo.getJSONObject("contentDetails").getString("definition"));}catch(Exception e){this.contentDetails_definition = "";}
		try{this.contentDetails_contentRating_nbcplRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("nbcplRating"));}catch(Exception e){this.contentDetails_contentRating_nbcplRating = "";}
		try{this.contentDetails_contentRating_cicfRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("cicfRating"));}catch(Exception e){this.contentDetails_contentRating_cicfRating = "";}
		try{this.contentDetails_contentRating_mccaaRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("mccaaRating"));}catch(Exception e){this.contentDetails_contentRating_mccaaRating = "";}
		try{this.contentDetails_contentRating_eirinRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("eirinRating"));}catch(Exception e){this.contentDetails_contentRating_eirinRating = "";}
		try{this.contentDetails_contentRating_cnaRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("cnaRating"));}catch(Exception e){this.contentDetails_contentRating_cnaRating = "";}
		try{this.contentDetails_contentRating_bmukkRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("bmukkRating"));}catch(Exception e){this.contentDetails_contentRating_bmukkRating = "";}
		try{this.contentDetails_contentRating_medietilsynetRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("medietilsynetRating"));}catch(Exception e){this.contentDetails_contentRating_medietilsynetRating = "";}
		try{this.contentDetails_contentRating_fmocRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("fmocRating"));}catch(Exception e){this.contentDetails_contentRating_fmocRating = "";}
		try{this.contentDetails_contentRating_resorteviolenciaRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("resorteviolenciaRating"));}catch(Exception e){this.contentDetails_contentRating_resorteviolenciaRating = "";}
		try{this.contentDetails_contentRating_nfrcRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("nfrcRating"));}catch(Exception e){this.contentDetails_contentRating_nfrcRating = "";}
		try{this.contentDetails_contentRating_catvfrRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("catvfrRating"));}catch(Exception e){this.contentDetails_contentRating_catvfrRating = "";}
		try{this.contentDetails_contentRating_cscfRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("cscfRating"));}catch(Exception e){this.contentDetails_contentRating_cscfRating = "";}
		try{this.contentDetails_contentRating_mccypRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("mccypRating"));}catch(Exception e){this.contentDetails_contentRating_mccypRating = "";}
		try{this.contentDetails_contentRating_cceRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("cceRating"));}catch(Exception e){this.contentDetails_contentRating_cceRating = "";}
		try{this.contentDetails_contentRating_tvpgRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("tvpgRating"));}catch(Exception e){this.contentDetails_contentRating_tvpgRating = "";}
		try{this.contentDetails_contentRating_rtcRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("rtcRating"));}catch(Exception e){this.contentDetails_contentRating_rtcRating = "";}
		try{this.contentDetails_contentRating_anatelRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("anatelRating"));}catch(Exception e){this.contentDetails_contentRating_anatelRating = "";}
		try{this.contentDetails_contentRating_skfilmRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("skfilmRating"));}catch(Exception e){this.contentDetails_contentRating_skfilmRating = "";}
		try{this.contentDetails_contentRating_kmrbRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("kmrbRating"));}catch(Exception e){this.contentDetails_contentRating_kmrbRating = "";}
		try{this.contentDetails_contentRating_mdaRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("mdaRating"));}catch(Exception e){this.contentDetails_contentRating_mdaRating = "";}
		try{this.contentDetails_contentRating_icaaRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("icaaRating"));}catch(Exception e){this.contentDetails_contentRating_icaaRating = "";}
		try{this.contentDetails_contentRating_mpaaRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("mpaaRating"));}catch(Exception e){this.contentDetails_contentRating_mpaaRating = "";}
		try{this.contentDetails_contentRating_fskRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("fskRating"));}catch(Exception e){this.contentDetails_contentRating_fskRating = "";}
		try{this.contentDetails_contentRating_chvrsRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("chvrsRating"));}catch(Exception e){this.contentDetails_contentRating_chvrsRating = "";}
		try{this.contentDetails_contentRating_cccRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("cccRating"));}catch(Exception e){this.contentDetails_contentRating_cccRating = "";}
		try{this.contentDetails_contentRating_acbRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("acbRating"));}catch(Exception e){this.contentDetails_contentRating_acbRating = "";}
		try{this.contentDetails_contentRating_mibacRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("mibacRating"));}catch(Exception e){this.contentDetails_contentRating_mibacRating = "";}
		try{this.contentDetails_contentRating_russiaRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("russiaRating"));}catch(Exception e){this.contentDetails_contentRating_russiaRating = "";}
		try{this.contentDetails_contentRating_djctqRatingReasons = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("djctqRatingReasons"));}catch(Exception e){this.contentDetails_contentRating_djctqRatingReasons = "";}
		try{this.contentDetails_contentRating_kijkwijzerRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("kijkwijzerRating"));}catch(Exception e){this.contentDetails_contentRating_kijkwijzerRating = "";}
		try{this.contentDetails_contentRating_smaisRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("smaisRating"));}catch(Exception e){this.contentDetails_contentRating_smaisRating = "";}
		try{this.contentDetails_contentRating_rcnofRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("rcnofRating"));}catch(Exception e){this.contentDetails_contentRating_rcnofRating = "";}
		try{this.contentDetails_contentRating_csaRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("csaRating"));}catch(Exception e){this.contentDetails_contentRating_csaRating = "";}
		try{this.contentDetails_contentRating_moctwRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("moctwRating"));}catch(Exception e){this.contentDetails_contentRating_moctwRating = "";}
		try{this.contentDetails_contentRating_nfvcbRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("nfvcbRating"));}catch(Exception e){this.contentDetails_contentRating_nfvcbRating = "";}
		try{this.contentDetails_contentRating_fpbRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("fpbRating"));}catch(Exception e){this.contentDetails_contentRating_fpbRating = "";}
		try{this.contentDetails_contentRating_kfcbRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("kfcbRating"));}catch(Exception e){this.contentDetails_contentRating_kfcbRating = "";}
		try{this.contentDetails_contentRating_rteRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("rteRating"));}catch(Exception e){this.contentDetails_contentRating_rteRating = "";}
		try{this.contentDetails_contentRating_agcomRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("agcomRating"));}catch(Exception e){this.contentDetails_contentRating_agcomRating = "";}
		try{this.contentDetails_contentRating_ilfilmRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("ilfilmRating"));}catch(Exception e){this.contentDetails_contentRating_ilfilmRating = "";}
		try{this.contentDetails_contentRating_mtrcbRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("mtrcbRating"));}catch(Exception e){this.contentDetails_contentRating_mtrcbRating = "";}
		try{this.contentDetails_contentRating_czfilmRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("czfilmRating"));}catch(Exception e){this.contentDetails_contentRating_czfilmRating = "";}
		try{this.contentDetails_contentRating_fcbmRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("fcbmRating"));}catch(Exception e){this.contentDetails_contentRating_fcbmRating = "";}
		try{this.contentDetails_contentRating_grfilmRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("grfilmRating"));}catch(Exception e){this.contentDetails_contentRating_grfilmRating = "";}
		try{this.contentDetails_contentRating_cbfcRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("cbfcRating"));}catch(Exception e){this.contentDetails_contentRating_cbfcRating = "";}
		try{this.contentDetails_contentRating_lsfRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("lsfRating"));}catch(Exception e){this.contentDetails_contentRating_lsfRating = "";}
		try{this.contentDetails_contentRating_oflcRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("oflcRating"));}catch(Exception e){this.contentDetails_contentRating_oflcRating = "";}
		try{this.contentDetails_contentRating_egfilmRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("egfilmRating"));}catch(Exception e){this.contentDetails_contentRating_egfilmRating = "";}
		try{this.contentDetails_contentRating_pefilmRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("pefilmRating"));}catch(Exception e){this.contentDetails_contentRating_pefilmRating = "";}
		try{this.contentDetails_contentRating_bfvcRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("bfvcRating"));}catch(Exception e){this.contentDetails_contentRating_bfvcRating = "";}
		try{this.contentDetails_contentRating_catvRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("catvRating"));}catch(Exception e){this.contentDetails_contentRating_catvRating = "";}
		try{this.contentDetails_contentRating_smsaRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("smsaRating"));}catch(Exception e){this.contentDetails_contentRating_smsaRating = "";}
		try{this.contentDetails_contentRating_bbfcRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("bbfcRating"));}catch(Exception e){this.contentDetails_contentRating_bbfcRating = "";}
		try{this.contentDetails_contentRating_chfilmRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("chfilmRating"));}catch(Exception e){this.contentDetails_contentRating_chfilmRating = "";}
		try{this.contentDetails_contentRating_djctqRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("djctqRating"));}catch(Exception e){this.contentDetails_contentRating_djctqRating = "";}
		try{this.contentDetails_contentRating_ytRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("ytRating"));}catch(Exception e){this.contentDetails_contentRating_ytRating = "";}
		try{this.contentDetails_contentRating_nkclvRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("nkclvRating"));}catch(Exception e){this.contentDetails_contentRating_nkclvRating = "";}
		try{this.contentDetails_contentRating_nbcRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("nbcRating"));}catch(Exception e){this.contentDetails_contentRating_nbcRating = "";}
		try{this.contentDetails_contentRating_mekuRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("mekuRating"));}catch(Exception e){this.contentDetails_contentRating_mekuRating = "";}
		try{this.contentDetails_contentRating_incaaRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("incaaRating"));}catch(Exception e){this.contentDetails_contentRating_incaaRating = "";}
		try{this.contentDetails_contentRating_eefilmRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("eefilmRating"));}catch(Exception e){this.contentDetails_contentRating_eefilmRating = "";}
		try{this.contentDetails_contentRating_ifcoRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("ifcoRating"));}catch(Exception e){this.contentDetails_contentRating_ifcoRating = "";}
		try{this.contentDetails_contentRating_mocRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("mocRating"));}catch(Exception e){this.contentDetails_contentRating_mocRating = "";}
		try{this.contentDetails_contentRating_fcoRating = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("contentRating").getString("fcoRating"));}catch(Exception e){this.contentDetails_contentRating_fcoRating = "";}
		try{this.contentDetails_caption = Util.EscapeString(vo.getJSONObject("contentDetails").getString("caption"));}catch(Exception e){this.contentDetails_caption = "";}
		try{this.contentDetails_regionRestriction_allowed = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("regionRestriction").getString("allowed"));}catch(Exception e){this.contentDetails_regionRestriction_allowed = "";}
		try{this.contentDetails_regionRestriction_blocked = Util.EscapeString(vo.getJSONObject("contentDetails").getJSONObject("regionRestriction").getString("blocked"));}catch(Exception e){this.contentDetails_regionRestriction_blocked = "";}
		try{this.fileDetails_recordingLocation_altitude = Util.EscapeString(vo.getJSONObject("fileDetails").getJSONObject("recordingLocation").getString("altitude"));}catch(Exception e){this.fileDetails_recordingLocation_altitude = "";}
		try{this.fileDetails_recordingLocation_longitude = Util.EscapeString(vo.getJSONObject("fileDetails").getJSONObject("recordingLocation").getString("longitude"));}catch(Exception e){this.fileDetails_recordingLocation_longitude = "";}
		try{this.fileDetails_recordingLocation_latitude = Util.EscapeString(vo.getJSONObject("fileDetails").getJSONObject("recordingLocation").getString("latitude"));}catch(Exception e){this.fileDetails_recordingLocation_latitude = "";}
		try{this.fileDetails_bitrateBps = Util.EscapeString(vo.getJSONObject("fileDetails").getString("bitrateBps"));}catch(Exception e){this.fileDetails_bitrateBps = "";}
		try{this.fileDetails_fileSize = Util.EscapeString(vo.getJSONObject("fileDetails").getString("fileSize"));}catch(Exception e){this.fileDetails_fileSize = "";}
		try{this.fileDetails_durationMs = Util.EscapeString(vo.getJSONObject("fileDetails").getString("durationMs"));}catch(Exception e){this.fileDetails_durationMs = "";}
		try{this.fileDetails_fileType = Util.EscapeString(vo.getJSONObject("fileDetails").getString("fileType"));}catch(Exception e){this.fileDetails_fileType = "";}
		try{this.fileDetails_container = Util.EscapeString(vo.getJSONObject("fileDetails").getString("container"));}catch(Exception e){this.fileDetails_container = "";}
		try{this.fileDetails_fileName = Util.EscapeString(vo.getJSONObject("fileDetails").getString("fileName"));}catch(Exception e){this.fileDetails_fileName = "";}
		try{this.fileDetails_creationTime = Util.EscapeString(vo.getJSONObject("fileDetails").getString("creationTime"));}catch(Exception e){this.fileDetails_creationTime = "";}
		try{this.fileDetails_videoStreams = Util.EscapeString(vo.getJSONObject("fileDetails").getString("videoStreams"));}catch(Exception e){this.fileDetails_videoStreams = "";}
		try{this.fileDetails_audioStreams = Util.EscapeString(vo.getJSONObject("fileDetails").getString("audioStreams"));}catch(Exception e){this.fileDetails_audioStreams = "";}

	}
	
	public String toString(){
		
		return String
				.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
						etag, player_embedHtml, status_publicStatsViewable,
						status_rejectionReason, status_privacyStatus,
						status_embeddable, status_uploadStatus,
						status_failureReason, status_license, status_publishAt,
						processingDetails_tagSuggestionsAvailability,
						processingDetails_thumbnailsAvailability,
						processingDetails_processingStatus,
						processingDetails_processingIssuesAvailability,
						processingDetails_processingProgress_partsProcessed,
						processingDetails_processingProgress_partsTotal,
						processingDetails_processingProgress_timeLeftMs,
						processingDetails_editorSuggestionsAvailability,
						processingDetails_fileDetailsAvailability,
						processingDetails_processingFailureReason,
						liveStreamingDetails_concurrentViewers,
						liveStreamingDetails_actualStartTime,
						liveStreamingDetails_scheduledEndTime,
						liveStreamingDetails_scheduledStartTime,
						liveStreamingDetails_actualEndTime, snippet_tags,
						snippet_publishedAt, snippet_title, snippet_channelId,
						snippet_description, snippet_categoryId,
						snippet_channelTitle,
						snippet_thumbnails_default_height,
						snippet_thumbnails_default_width,
						snippet_thumbnails_default_url,
						snippet_thumbnails_standard_height,
						snippet_thumbnails_standard_width,
						snippet_thumbnails_standard_url,
						snippet_thumbnails_high_height,
						snippet_thumbnails_high_width,
						snippet_thumbnails_high_url,
						snippet_thumbnails_medium_height,
						snippet_thumbnails_medium_width,
						snippet_thumbnails_medium_url,
						snippet_thumbnails_maxres_height,
						snippet_thumbnails_maxres_width,
						snippet_thumbnails_maxres_url,
						snippet_liveBroadcastContent, snippet_localized_title,
						snippet_localized_description,
						suggestions_editorSuggestions,
						suggestions_processingHints,
						suggestions_tagSuggestions,
						suggestions_processingWarnings,
						suggestions_processingErrors, kind,
						statistics_favoriteCount, statistics_dislikeCount,
						statistics_likeCount, statistics_commentCount,
						statistics_viewCount, topicDetails_topicIds,
						topicDetails_relevantTopicIds, id,
						recordingDetails_recordingDate,
						recordingDetails_location_altitude,
						recordingDetails_location_longitude,
						recordingDetails_location_latitude,
						recordingDetails_locationDescription,
						contentDetails_dimension, contentDetails_duration,
						contentDetails_licensedContent,
						contentDetails_definition,
						contentDetails_contentRating_nbcplRating,
						contentDetails_contentRating_cicfRating,
						contentDetails_contentRating_mccaaRating,
						contentDetails_contentRating_eirinRating,
						contentDetails_contentRating_cnaRating,
						contentDetails_contentRating_bmukkRating,
						contentDetails_contentRating_medietilsynetRating,
						contentDetails_contentRating_fmocRating,
						contentDetails_contentRating_resorteviolenciaRating,
						contentDetails_contentRating_nfrcRating,
						contentDetails_contentRating_catvfrRating,
						contentDetails_contentRating_cscfRating,
						contentDetails_contentRating_mccypRating,
						contentDetails_contentRating_cceRating,
						contentDetails_contentRating_tvpgRating,
						contentDetails_contentRating_rtcRating,
						contentDetails_contentRating_anatelRating,
						contentDetails_contentRating_skfilmRating,
						contentDetails_contentRating_kmrbRating,
						contentDetails_contentRating_mdaRating,
						contentDetails_contentRating_icaaRating,
						contentDetails_contentRating_mpaaRating,
						contentDetails_contentRating_fskRating,
						contentDetails_contentRating_chvrsRating,
						contentDetails_contentRating_cccRating,
						contentDetails_contentRating_acbRating,
						contentDetails_contentRating_mibacRating,
						contentDetails_contentRating_russiaRating,
						contentDetails_contentRating_djctqRatingReasons,
						contentDetails_contentRating_kijkwijzerRating,
						contentDetails_contentRating_smaisRating,
						contentDetails_contentRating_rcnofRating,
						contentDetails_contentRating_csaRating,
						contentDetails_contentRating_moctwRating,
						contentDetails_contentRating_nfvcbRating,
						contentDetails_contentRating_fpbRating,
						contentDetails_contentRating_kfcbRating,
						contentDetails_contentRating_rteRating,
						contentDetails_contentRating_agcomRating,
						contentDetails_contentRating_ilfilmRating,
						contentDetails_contentRating_mtrcbRating,
						contentDetails_contentRating_czfilmRating,
						contentDetails_contentRating_fcbmRating,
						contentDetails_contentRating_grfilmRating,
						contentDetails_contentRating_cbfcRating,
						contentDetails_contentRating_lsfRating,
						contentDetails_contentRating_oflcRating,
						contentDetails_contentRating_egfilmRating,
						contentDetails_contentRating_pefilmRating,
						contentDetails_contentRating_bfvcRating,
						contentDetails_contentRating_catvRating,
						contentDetails_contentRating_smsaRating,
						contentDetails_contentRating_bbfcRating,
						contentDetails_contentRating_chfilmRating,
						contentDetails_contentRating_djctqRating,
						contentDetails_contentRating_ytRating,
						contentDetails_contentRating_nkclvRating,
						contentDetails_contentRating_nbcRating,
						contentDetails_contentRating_mekuRating,
						contentDetails_contentRating_incaaRating,
						contentDetails_contentRating_eefilmRating,
						contentDetails_contentRating_ifcoRating,
						contentDetails_contentRating_mocRating,
						contentDetails_contentRating_fcoRating,
						contentDetails_caption,
						contentDetails_regionRestriction_allowed,
						contentDetails_regionRestriction_blocked,
						fileDetails_recordingLocation_altitude,
						fileDetails_recordingLocation_longitude,
						fileDetails_recordingLocation_latitude,
						fileDetails_bitrateBps, fileDetails_fileSize,
						fileDetails_durationMs, fileDetails_fileType,
						fileDetails_container, fileDetails_fileName,
						fileDetails_creationTime, fileDetails_videoStreams,
						fileDetails_audioStreams);
	}
	
	public String[] getAttributeValueList() {
		return new String[]{etag, player_embedHtml, status_publicStatsViewable,
				status_rejectionReason, status_privacyStatus,
				status_embeddable, status_uploadStatus,
				status_failureReason, status_license, status_publishAt,
				processingDetails_tagSuggestionsAvailability,
				processingDetails_thumbnailsAvailability,
				processingDetails_processingStatus,
				processingDetails_processingIssuesAvailability,
				processingDetails_processingProgress_partsProcessed,
				processingDetails_processingProgress_partsTotal,
				processingDetails_processingProgress_timeLeftMs,
				processingDetails_editorSuggestionsAvailability,
				processingDetails_fileDetailsAvailability,
				processingDetails_processingFailureReason,
				liveStreamingDetails_concurrentViewers,
				liveStreamingDetails_actualStartTime,
				liveStreamingDetails_scheduledEndTime,
				liveStreamingDetails_scheduledStartTime,
				liveStreamingDetails_actualEndTime, snippet_tags,
				snippet_publishedAt, snippet_title, snippet_channelId,
				snippet_description, snippet_categoryId,
				snippet_channelTitle,
				snippet_thumbnails_default_height,
				snippet_thumbnails_default_width,
				snippet_thumbnails_default_url,
				snippet_thumbnails_standard_height,
				snippet_thumbnails_standard_width,
				snippet_thumbnails_standard_url,
				snippet_thumbnails_high_height,
				snippet_thumbnails_high_width,
				snippet_thumbnails_high_url,
				snippet_thumbnails_medium_height,
				snippet_thumbnails_medium_width,
				snippet_thumbnails_medium_url,
				snippet_thumbnails_maxres_height,
				snippet_thumbnails_maxres_width,
				snippet_thumbnails_maxres_url,
				snippet_liveBroadcastContent, snippet_localized_title,
				snippet_localized_description,
				suggestions_editorSuggestions,
				suggestions_processingHints,
				suggestions_tagSuggestions,
				suggestions_processingWarnings,
				suggestions_processingErrors, kind,
				statistics_favoriteCount, statistics_dislikeCount,
				statistics_likeCount, statistics_commentCount,
				statistics_viewCount, topicDetails_topicIds,
				topicDetails_relevantTopicIds, id,
				recordingDetails_recordingDate,
				recordingDetails_location_altitude,
				recordingDetails_location_longitude,
				recordingDetails_location_latitude,
				recordingDetails_locationDescription,
				contentDetails_dimension, contentDetails_duration,
				contentDetails_licensedContent,
				contentDetails_definition,
				contentDetails_contentRating_nbcplRating,
				contentDetails_contentRating_cicfRating,
				contentDetails_contentRating_mccaaRating,
				contentDetails_contentRating_eirinRating,
				contentDetails_contentRating_cnaRating,
				contentDetails_contentRating_bmukkRating,
				contentDetails_contentRating_medietilsynetRating,
				contentDetails_contentRating_fmocRating,
				contentDetails_contentRating_resorteviolenciaRating,
				contentDetails_contentRating_nfrcRating,
				contentDetails_contentRating_catvfrRating,
				contentDetails_contentRating_cscfRating,
				contentDetails_contentRating_mccypRating,
				contentDetails_contentRating_cceRating,
				contentDetails_contentRating_tvpgRating,
				contentDetails_contentRating_rtcRating,
				contentDetails_contentRating_anatelRating,
				contentDetails_contentRating_skfilmRating,
				contentDetails_contentRating_kmrbRating,
				contentDetails_contentRating_mdaRating,
				contentDetails_contentRating_icaaRating,
				contentDetails_contentRating_mpaaRating,
				contentDetails_contentRating_fskRating,
				contentDetails_contentRating_chvrsRating,
				contentDetails_contentRating_cccRating,
				contentDetails_contentRating_acbRating,
				contentDetails_contentRating_mibacRating,
				contentDetails_contentRating_russiaRating,
				contentDetails_contentRating_djctqRatingReasons,
				contentDetails_contentRating_kijkwijzerRating,
				contentDetails_contentRating_smaisRating,
				contentDetails_contentRating_rcnofRating,
				contentDetails_contentRating_csaRating,
				contentDetails_contentRating_moctwRating,
				contentDetails_contentRating_nfvcbRating,
				contentDetails_contentRating_fpbRating,
				contentDetails_contentRating_kfcbRating,
				contentDetails_contentRating_rteRating,
				contentDetails_contentRating_agcomRating,
				contentDetails_contentRating_ilfilmRating,
				contentDetails_contentRating_mtrcbRating,
				contentDetails_contentRating_czfilmRating,
				contentDetails_contentRating_fcbmRating,
				contentDetails_contentRating_grfilmRating,
				contentDetails_contentRating_cbfcRating,
				contentDetails_contentRating_lsfRating,
				contentDetails_contentRating_oflcRating,
				contentDetails_contentRating_egfilmRating,
				contentDetails_contentRating_pefilmRating,
				contentDetails_contentRating_bfvcRating,
				contentDetails_contentRating_catvRating,
				contentDetails_contentRating_smsaRating,
				contentDetails_contentRating_bbfcRating,
				contentDetails_contentRating_chfilmRating,
				contentDetails_contentRating_djctqRating,
				contentDetails_contentRating_ytRating,
				contentDetails_contentRating_nkclvRating,
				contentDetails_contentRating_nbcRating,
				contentDetails_contentRating_mekuRating,
				contentDetails_contentRating_incaaRating,
				contentDetails_contentRating_eefilmRating,
				contentDetails_contentRating_ifcoRating,
				contentDetails_contentRating_mocRating,
				contentDetails_contentRating_fcoRating,
				contentDetails_caption,
				contentDetails_regionRestriction_allowed,
				contentDetails_regionRestriction_blocked,
				fileDetails_recordingLocation_altitude,
				fileDetails_recordingLocation_longitude,
				fileDetails_recordingLocation_latitude,
				fileDetails_bitrateBps, fileDetails_fileSize,
				fileDetails_durationMs, fileDetails_fileType,
				fileDetails_container, fileDetails_fileName,
				fileDetails_creationTime, fileDetails_videoStreams,
				fileDetails_audioStreams};
	}
}
