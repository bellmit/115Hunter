/*should run on sybase IQ server due to data import*/
/*insert data row by row in IQ is quite slow, data import is much faster*/
package com.sap.cisp.xhna.data.storage.iq;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.common.Util;
import com.sap.cisp.xhna.data.config.ConfigStorage;
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
import com.sap.cisp.xhna.data.storage.AbstractStorageExecutor;
import com.sap.cisp.xhna.data.storage.ColumnInfo;

public class IQExecutor extends AbstractStorageExecutor{
	private static String delimiter = "\t";
	private static String raw_tag = "/RAW/";
	private static String mapreduce_tag = "/MAPREDUCE/";
	private static Logger log = LoggerFactory.getLogger(IQExecutor.class);

	public IQExecutor(String key, String start_time, String end_time,
			MediaKey media_key, MediaInfo media_info, List<String> data) {
		super(key, start_time, end_time, media_key, media_info, data);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void process() {
		// TODO Auto-generated method stub
		
	}

	private String dataTransformation(String line, String media, String type, String key_type, String crawlJson){
		/*process posts*/
		if ("SocialArticle".equals(type)||"TraditionalArticle".equals(type)){
			if ("Keyword".equals(key_type)){
				if ("Twitter".equals(media)){
					TwitterInfo di = new TwitterInfo(line);
					return di.toString()+delimiter+crawlJson+delimiter+key;
				}else if ("GooglePlus".equals(media)){
					GplusInfo di = new GplusInfo(line);
					return di.toString()+delimiter+crawlJson+delimiter+key;
				}
			}else if ("Account".equals(key_type)){
				if ("Twitter".equals(media)){
					TwitterInfo di = new TwitterInfo(line);
					return di.toString()+delimiter+key;
				}else if ("GooglePlus".equals(media)){
					GplusInfo di = new GplusInfo(line);
					return di.toString()+delimiter+key;
				}else if ("Facebook".equals(media)){
					FacebookInfo di = new FacebookInfo(line);
					return di.toString()+delimiter+key;
				}else if ("Youtube".equals(media)){
					YoutubeInfo di = new YoutubeInfo(line);
					return di.toString()+delimiter+key;
				}
			}else if ("RSS".equals(key_type)||"WebPage".equals(key_type)){
				TranditionalMediaInfo di = new TranditionalMediaInfo(line);
				return di.toString()+delimiter+key;
			}
		}else if ("SocialAccount".equals(type)){
			if ("Twitter".equals(media)){
				TwitterAccountInfo di = new TwitterAccountInfo(line);
				return di.toString()+delimiter+key;
			}else if ("GooglePlus".equals(media)){
				GplusAccountInfo di = new GplusAccountInfo(line);
				return di.toString()+delimiter+key;
			}else if ("Facebook".equals(media)){
				FacebookAccountInfo di = new FacebookAccountInfo(line);
				return di.toString()+delimiter+key;
			}else if ("Youtube".equals(media)){
				YoutubeAccountInfo di = new YoutubeAccountInfo(line, key);
				return di.toString()+delimiter+key;
			}
		}else if ("SocialAccountArticle".equals(type)){
			if ("Twitter".equals(media)){
				TwitterAdditionalInfo di = new TwitterAdditionalInfo(line);
				return di.toString()+delimiter+key;
			}else if ("GooglePlus".equals(media)){
				GplusAdditionalInfo di = new GplusAdditionalInfo(line);
				return di.toString()+delimiter+key;
			}else if ("Facebook".equals(media)){
				FacebookAdditionalInfo di = new FacebookAdditionalInfo(line);
				return di.toString()+delimiter+key;
			}else if ("Youtube".equals(media)){
				YoutubeAdditionalInfo di = new YoutubeAdditionalInfo(line);
				return di.toString()+delimiter+key;
			}
		}else if ("ForumArticle".equals(type)){
			if ("Guba".equals(media)){
				GubaPostInfo di = new GubaPostInfo(line);
				return di.toString()+delimiter+key;
			}
		}else if ("ForumAccount".equals(type)){
			if ("Guba".equals(media)){
				GubaAccountInfo di = new GubaAccountInfo(line);
				return di.toString();
			}
		}
		return null;
	}
	
	@SuppressWarnings("resource")
	private String processFile() {
		String csv_path = getCsv_path();
		if ("".equals(csv_path)||csv_path.indexOf(raw_tag)<=0){
			return "";
		}
		
		String mapreduce_path = Util.getOutpath(csv_path, raw_tag,
				mapreduce_tag);
		if (mapreduce_path.endsWith(".csv")) {
			mapreduce_path = mapreduce_path.substring(0,
					mapreduce_path.length() - ".csv".length());
		}
		
		String media = media_key.getMedia_name();
		String key_type = media_key.getKey_type();
		String type = media_key.getType();
		String crawlJson = Util.constructJson(media, type, key_type, key, start_time, end_time);
		
		OutputStreamWriter out = null;
		try {
			Util.createFolderByFilePath(mapreduce_path);
			out = new OutputStreamWriter(new FileOutputStream(mapreduce_path),"UTF-8");
		} catch (UnsupportedEncodingException | FileNotFoundException e1) {
			// TODO Auto-generated catch block
			log.error("Could not Create HDFS File [{}]!",mapreduce_path);
			return "";
		}
		for (String row : data) {
			try {
				out.write(dataTransformation(row, media, type, key_type, crawlJson)+delimiter);
				out.write('\n');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("Process [{}] Failed!",csv_path);
				return "";
			}
		}
		try {
			out.close();
			log.info("Process Raw File: [{}] Successed!",mapreduce_path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Process [{}] Failed!",csv_path);
			return "";
		}
		return mapreduce_path;
	}
	
	public void writeDB() throws Exception {
		String schema = media_info.getDatabase_schema();
		String table = media_info.getDatabase_table();
		
		String url = ConfigStorage.IQ_URL;
		String username = ConfigStorage.IQ_USERNAME;
		String password = ConfigStorage.IQ_PASSWORD;
		
		String data_path = processFile();
		if ("".equals(data_path)){
			log.error("Empty data path, return!"); 
			throw new Exception("Empty data path");
		}
		
		try {
			Class.forName("com.sybase.jdbc4.jdbc.SybDriver");
		} catch (ClassNotFoundException e) {
			log.error("No JDBC Driver found!");
			throw e;
		}
		Connection con = null;
		Statement stmt = null;
		try {
			con = DriverManager.getConnection(url, username, password);
			stmt = con.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Could not Connect to IQ: [{}]!", url);
			throw e;
		}
		
		ColumnInfo column_info = new ColumnInfo(con, schema, table);
		String[] columns = column_info.getColumnNames();
		String cols = "";
		for(int i = 0; i < columns.length; ++i){
			cols += columns[i] + " '\\x09',";
		}
		cols = cols.substring(0, cols.length()-1);
		try {
			/*
			 * stmt.execute(String .format(
			 * "load table %s.%s from '%s' STRIP OFF QUOTES OFF ESCAPES OFF DELIMITED BY '\\t' ROW DELIMITED BY '\\n'"
			 * , schema, table, path));
			 */
			stmt.execute("set temporary option escape_character='on'");
			stmt.execute(String
					.format("load table %s.%s(%s) from '%s' ESCAPES OFF QUOTES OFF format ascii WITH CHECKPOINT ON",
							schema, table, cols, data_path));
			log.info("Write IQ: [{}.{}] Successed!", schema, table);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Write IQ: [{}.{}] Failed, {}", schema, table, e);
			throw e;
		} finally{
			try {
				stmt.close();
				con.close();
			} catch (SQLException e) {
				throw e;
			}
		}
	}
}
