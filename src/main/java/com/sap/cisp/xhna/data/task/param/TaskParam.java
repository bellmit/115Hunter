package com.sap.cisp.xhna.data.task.param;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;

public class TaskParam implements IParam, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8912466550720385955L;
	public Map<String, Object> maps = new HashMap<String, Object>();

	public TaskParam(Map<String, String> map, MediaKey mediaKey) {
		// It is more efficient to use an iterator on the entrySet of the map, 
	    // to avoid the Map.get(key) lookup.
	    for (Map.Entry<String, String> entry : map.entrySet()) {
            maps.put(entry.getKey(), entry.getValue());
	    }
		this.putMediaKey(mediaKey);
	}

	public String getString(String key) {
		Object o = maps.get(key);
		if (o != null) {
			return o.toString();
		}
		return null;
	}

	public void setString(String key, String value) {
		maps.put(key, value);
	}

	public void putMediaKey(MediaKey mediaKey) {
		maps.put("mediaKey", mediaKey);
	}

	public MediaKey getMediaKey() {
		return (MediaKey) maps.get("mediaKey");
	}

	@Override
	public String toString() {
		return "TaskParam [maps=" + maps + "]";
	}

	@Override
	public String getStartTime() {
		// TODO Auto-generated method stub
		return getString("start_time");
	}

	@Override
	public String getEndTime() {
		// TODO Auto-generated method stub
		return getString("end_time");
	}

	@Override
	public String getAccount() {
		// TODO Auto-generated method stub
		return getString("task_key");
	}

	@Override
	public String getKeyword() {
		// TODO Auto-generated method stub
		return getString("task_key");
	}

	@Override
	public String getRss() {
		// TODO Auto-generated method stub
		return getString("task_key");
	}

	@Override
	public String getMediaName() {
		// TODO Auto-generated method stub
		return getString("media_name");
	}

	@Override
	public int hashCode() {
		int result = 17;
		for (Map.Entry<String, Object> entry : maps.entrySet()) {
		    result = 37 * result + entry.getValue().hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		// a quick test to see if the objects are identical
		if (this == obj)
			return true;

		// must return false if the explicit parameter is null
		if (obj == null)
			return false;

		if (getClass() != obj.getClass()) {
			return false;
		}

		TaskParam p = (TaskParam) obj;
		if (this.getStartTime().equalsIgnoreCase(p.getStartTime())
				&& this.getEndTime().equalsIgnoreCase(p.getEndTime())
				&& this.getMediaName().equalsIgnoreCase(p.getMediaName())
				&& this.getMediaKey().equals(p.getMediaKey())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void addUpdatedCount(int count) {
		maps.put("updatedCount", getUpdatedCount() + count);
	}

	@Override
	public int getUpdatedCount() {
		Object o = maps.get("updatedCount");
		if (o == null) {
			return 0;
		} else {
			return Integer.parseInt(maps.get("updatedCount").toString());
		}
	}

	@Override
	public String getUrl() {
		return getString("task_key");
	}
}
