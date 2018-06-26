package com.sap.cisp.xhna.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.cisp.xhna.data.db.HANAService;
import com.sap.cisp.xhna.data.model.databasemapping.MediaInfo;
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;

public class ApplicationContext {
    private static Map<MediaKey, MediaInfo> mediaMap = new HashMap<MediaKey, MediaInfo>();
    private static Map<String, MediaKey> mediaKeyMap = new HashMap<String, MediaKey>();

    static {
        List<Map<String, String>> resultList = HANAService.listAllMediaInfos();
        for (int i = 0; i < resultList.size(); i++) {
            MediaKey key = MediaKey.exetractKeyFromSet(resultList.get(i));
            mediaKeyMap.put(
                    key.getMedia_name() + key.getType() + key.getKey_type(),
                    key);
            MediaInfo info = MediaInfo.exetractInfoFromSet(resultList.get(i));
            mediaMap.put(key, info);
        }
    }

    public static MediaInfo getMediaInfoByKey(MediaKey mediaKey) {
        return mediaMap.get(mediaKey);
    }

    public static MediaKey getMediaKey(String media, String type) {
        return mediaKeyMap.get(media + type);
    }

}
