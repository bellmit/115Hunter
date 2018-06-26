package com.sap.cisp.xhna.data.storage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.sap.cisp.xhna.data.ApplicationContext;
import com.sap.cisp.xhna.data.config.ConfigStorage;
import com.sap.cisp.xhna.data.model.databasemapping.MediaInfo;
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;
import com.sap.cisp.xhna.data.storage.hive.HiveExecutor;
import com.sap.cisp.xhna.data.storage.iq.IQExecutor;
import com.sap.cisp.xhna.data.storage.oltpdatabase.DBExecutor;

public class StorageTest {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		List<String> data = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader("twitter.csv"));
		String line = br.readLine();
		while (line != null) {
			data.add(line);
			line = br.readLine();
		}
		if (data == null || data.size() == 0) {
			return;
		}
		String key = "kongyang";
		String start_time = "2015-05-16 00:00:00";
		String end_time = "2015-08-16 00:00:00";
		MediaKey media_key = new MediaKey("Twitter", "SocialArticle", "Keyword");
		MediaInfo media_info = ApplicationContext.getMediaInfoByKey(media_key);

		if ("hive".equalsIgnoreCase(ConfigStorage.DATABASE_TYPE)){
			HiveExecutor executor = new HiveExecutor(key, start_time, end_time,
					media_key, media_info, data);
			executor.writeRawFile();
			executor.writeDB();
		}else if("iq".equalsIgnoreCase(ConfigStorage.DATABASE_TYPE)){
			IQExecutor executor = new IQExecutor(key, start_time, end_time,
					media_key, media_info, data);
			executor.writeRawFile();
			executor.writeDB();
		}
		else if("oltpdatabase".equalsIgnoreCase(ConfigStorage.DATABASE_TYPE)){
			DBExecutor executor = new DBExecutor(key, start_time, end_time,
					media_key, media_info, data);
			executor.writeRawFile();
			executor.writeDB();
		}
	}
}
