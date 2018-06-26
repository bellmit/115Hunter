package com.sap.cisp.xhna.data.storage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.common.Util;
import com.sap.cisp.xhna.data.config.ConfigStorage;
import com.sap.cisp.xhna.data.config.DataSource;
import com.sap.cisp.xhna.data.model.FacebookAccountInfo;
import com.sap.cisp.xhna.data.model.FacebookAdditionalInfo;
import com.sap.cisp.xhna.data.model.FacebookInfo;
import com.sap.cisp.xhna.data.model.GplusAccountInfo;
import com.sap.cisp.xhna.data.model.GplusAdditionalInfo;
import com.sap.cisp.xhna.data.model.GplusInfo;
import com.sap.cisp.xhna.data.model.GubaAccountInfo;
import com.sap.cisp.xhna.data.model.GubaPostInfo;
import com.sap.cisp.xhna.data.model.TranditionalMediaInfo;
import com.sap.cisp.xhna.data.model.TwitterAccountInfo;
import com.sap.cisp.xhna.data.model.TwitterAdditionalInfo;
import com.sap.cisp.xhna.data.model.TwitterInfo;
import com.sap.cisp.xhna.data.model.YoutubeAccountInfo;
import com.sap.cisp.xhna.data.model.YoutubeAdditionalInfo;
import com.sap.cisp.xhna.data.model.YoutubeInfo;
import com.sap.cisp.xhna.data.model.databasemapping.MediaInfo;
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;

public abstract class AbstractStorageExecutor implements IStorageExecutor{
	
	protected static Logger log = LoggerFactory.getLogger(AbstractStorageExecutor.class);
	protected String key;
	protected String start_time;
	protected String end_time;
	protected MediaKey media_key;
	protected MediaInfo media_info;
	protected List<String> data;
	private String csv_path;
	private static String raw_tag = "/RAW/";
	
	public AbstractStorageExecutor(String key, String start_time, String end_time, MediaKey media_key, MediaInfo media_info, List<String> data){
		this.key = key;
		this.start_time = start_time;
		this.end_time = end_time;
		this.media_key = media_key;
		this.media_info = media_info;
		this.data = data;
	}
	
	public String getCsv_path(){
		return csv_path;
	}
	
	//preventing generating same timestamp for different threads
	protected static synchronized String getCurrentTime() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String current_time = new SimpleDateFormat("yyyyMMddHHmmssSSS")
		.format(new Date(System.currentTimeMillis()));
		return current_time;
    }
	
	@SuppressWarnings("resource")
	public void writeRawFile() throws Exception{
		String type = media_key.getType();
		String file_path = media_info.getFilesystem_path();

		OutputStreamWriter out = null;
		String outpath = "";
		try {
			String current_date = new SimpleDateFormat("yyyyMMdd")
					.format(new Date(System.currentTimeMillis()));
			if ("SocialArticle".equalsIgnoreCase(type)) {
				outpath = String.format("%s/%s%s%s/%s.csv", file_path, key, raw_tag,
						current_date, getCurrentTime());
			} else if ("TraditionalArticle".equalsIgnoreCase(type)||"ForumArticle".equalsIgnoreCase(type)){
				outpath = String.format("%s/%s%s%s/%s.csv", file_path, Integer.toHexString(key.hashCode()), raw_tag,
						current_date, getCurrentTime());
			} else if ("SocialAccount".equalsIgnoreCase(type)||"SocialAccountArticle".equalsIgnoreCase(type)||"ForumAccount".equalsIgnoreCase(type)) {
				outpath = String.format("%s%s%s/%s.csv", file_path, raw_tag,
						current_date, getCurrentTime());
			}
			Util.createFolderByFilePath(outpath);
			out = new OutputStreamWriter(new FileOutputStream(outpath),"UTF-8");
		} catch (IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			log.error("Could not Create File [{}]!",outpath);
			throw e;
		}
		for (String row : data) {
			try {
				out.write(row);
				out.write('\n');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("Write File System: [{}] Failed!",outpath);
				throw e;
			}
		}
		try {
			out.close();
			log.info("Write File System: [{}] Successed!", outpath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Write File System: [{}] Failed!", outpath);
			throw e;
		}
		csv_path = outpath;
		return;
	}
	
	private String[] addInfo(String[] info, String... additionals) {
		String[] res = new String[info.length + additionals.length];
		System.arraycopy(info, 0, res, 0, info.length);
		for (int i = 0; i < additionals.length; ++i) {
			res[info.length + i] = additionals[i];
		}
		return res;
	}
	private String[] dataTransformation(String line, String media, String type, String key_type, String crawlJson) {
		/* process posts */
		if ("SocialArticle".equals(type) || "TraditionalArticle".equals(type)) {
			if ("Keyword".equals(key_type)) {
				if ("Twitter".equals(media)) {
					TwitterInfo di = new TwitterInfo(line);
					return addInfo(di.getAttributeValueList(), crawlJson, key);
				} else if ("GooglePlus".equals(media)) {
					GplusInfo di = new GplusInfo(line);
					return addInfo(di.getAttributeValueList(), crawlJson, key);
				}
			} else if ("Account".equals(key_type)) {
				if ("Twitter".equals(media)) {
					TwitterInfo di = new TwitterInfo(line);
					return addInfo(di.getAttributeValueList(), key);
				} else if ("GooglePlus".equals(media)) {
					GplusInfo di = new GplusInfo(line);
					return addInfo(di.getAttributeValueList(), key);
				} else if ("Facebook".equals(media)) {
					FacebookInfo di = new FacebookInfo(line);
					return addInfo(di.getAttributeValueList(), key);
				} else if ("Youtube".equals(media)) {
					YoutubeInfo di = new YoutubeInfo(line);
					return addInfo(di.getAttributeValueList(), key);
				}
			} else if ("RSS".equals(key_type) || "WebPage".equals(key_type)) {
				TranditionalMediaInfo di = new TranditionalMediaInfo(line);
				return addInfo(di.getAttributeValueList(),key);
			}
		} else if ("SocialAccount".equals(type)) {
			if ("Twitter".equals(media)) {
				TwitterAccountInfo di = new TwitterAccountInfo(line);
				return addInfo(di.getAttributeValueList(),key);
			}else if ("GooglePlus".equals(media)) {
				GplusAccountInfo di = new GplusAccountInfo(line);
				return addInfo(di.getAttributeValueList(),key);
			} else if ("Facebook".equals(media)) {
				FacebookAccountInfo di = new FacebookAccountInfo(line);
				return addInfo(di.getAttributeValueList(),key);
			} else if ("Youtube".equals(media)) {
				YoutubeAccountInfo di = new YoutubeAccountInfo(line, key);
				return addInfo(di.getAttributeValueList(),key);
			}
		}else if ("AdditionalInfo".equals(type)){
			if ("Twitter".equals(media)){
				TwitterAdditionalInfo di = new TwitterAdditionalInfo(line);
				return addInfo(di.getAttributeValueList(),key);
			}else if ("GooglePlus".equals(media)){
				GplusAdditionalInfo di = new GplusAdditionalInfo(line);
				return addInfo(di.getAttributeValueList(),key);
			}else if ("Facebook".equals(media)){
				FacebookAdditionalInfo di = new FacebookAdditionalInfo(line);
				return addInfo(di.getAttributeValueList(),key);
			}else if ("Youtube".equals(media)){
				YoutubeAdditionalInfo di = new YoutubeAdditionalInfo(line);
				return addInfo(di.getAttributeValueList(),key);
			}
		}else if ("ForumArticle".equals(type)){
			if ("Guba".equals(media)){
				GubaPostInfo di = new GubaPostInfo(line);
				return addInfo(di.getAttributeValueList(),key);
			}
		}else if ("ForumAccount".equals(type)){
			if ("Guba".equals(media)){
				GubaAccountInfo di = new GubaAccountInfo(line);
				return di.getAttributeValueList();
			}
		}
		return null;
	}
	
	private List<String[]> processFile() throws Exception {
		
		String media = media_key.getMedia_name();
		String key_type = media_key.getKey_type();
		String type = media_key.getType();
		String crawlJson = Util.constructJson(media, type, key_type, key, start_time, end_time);
		
		List<String[]> output = new ArrayList<String[]>();
		for (String row : data) {
			output.add(dataTransformation(row, media, type, key_type, crawlJson));
		}
		if (output == null || output.size() == 0) {
			log.error("Process Failed!{},{},{},{}",media,type,key_type,key);
			throw new Exception("Process Failed! media: " + media + "; type: " + type + "; key_type: " + key_type + "; key: " + key);
		} else {
			log.info("Process Successed!");
		}
		return output;
	}
	
	public void writeDB() throws Exception {
		String schema = media_info.getDatabase_schema();
		String table = media_info.getDatabase_table();
		
		DataSource ds = ConfigStorage.DS_INFO;
		String DriverClassName = ds.getDriver();
		String url = ds.getUrl();
		String username = ds.getUserName();
		String password = ds.getPassword();
		
		List<String[]> strucute_data = processFile();
		
		try {
			Class.forName(DriverClassName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			log.error("No JDBC Driver found!");
			throw e ;
		}
		Connections connections = new Connections(DriverClassName, url, username, password);
		Connection conn = connections.getConnection();
		ColumnInfo column_info = new ColumnInfo(conn, schema, table, strucute_data);
		column_info.insertData();
		log.info("Write Table [{}.{}] Successed!", schema, table);
	}
}
