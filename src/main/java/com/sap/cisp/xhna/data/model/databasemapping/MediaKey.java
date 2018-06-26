package com.sap.cisp.xhna.data.model.databasemapping;

import java.io.Serializable;
import java.util.Map;

public class MediaKey implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 2789852263886375175L;

    public String getMedia_name() {
        return media_name;
    }

    public String getType() {
        return type;
    }

    public String getKey_type() {
        return key_type;
    }

    private String media_name;
    private String type;
    private String key_type;

    public MediaKey(String media_name, String type, String key_type) {
        this.media_name = media_name;
        this.type = type;
        this.key_type = key_type;
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof MediaKey) {
            MediaKey anotherObject = (MediaKey) anObject;
            if (anotherObject.getMedia_name().equals(this.media_name)
                    && anotherObject.getKey_type().equals(this.key_type)
                    && anotherObject.getType().equals(this.type)) {
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        return getMedia_name().hashCode() + getType().hashCode()
                + getKey_type().hashCode();
    }

    public static MediaKey exetractKeyFromSet(Map<String, String> map) {
        // TODO Auto-generated method stub
        MediaKey mediaKey = new MediaKey(map.get("media_name"),
                map.get("type"), map.get("key_type"));
        return mediaKey;
    }

    @Override
    public String toString() {
        return "MediaKey [media_name=" + media_name + ", type=" + type
                + ", key_type=" + key_type + "]";
    }

}
