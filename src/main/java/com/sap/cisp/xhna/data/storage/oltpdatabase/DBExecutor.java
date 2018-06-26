package com.sap.cisp.xhna.data.storage.oltpdatabase;

import java.util.List;

import com.sap.cisp.xhna.data.model.databasemapping.MediaInfo;
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;
import com.sap.cisp.xhna.data.storage.AbstractStorageExecutor;

public class DBExecutor extends AbstractStorageExecutor{

	public DBExecutor(String key, String start_time, String end_time,
			MediaKey media_key, MediaInfo media_info, List<String> data) {
		super(key, start_time, end_time, media_key, media_info, data);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process() {
		// TODO Auto-generated method stub
		
	}

}
