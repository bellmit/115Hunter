package com.sap.cisp.xhna.data.storage.hive;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
import com.sap.cisp.xhna.data.model.TranditionalAdditionalInfo;
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

public class HiveExecutor extends AbstractStorageExecutor{
	private static Logger log = LoggerFactory.getLogger(HiveExecutor.class);
	private static Configuration conf = new Configuration();
	private static String delimiter = "\t";
	private static String raw_tag = "/RAW/";
	private static String mapreduce_tag = "/MAPREDUCE/";
	
	private FileSystem fs;
	private String csv_path="";
	private int rows = 0;			//used for data rows statistics
	
	public int getRows() {
		return rows;
	}

	public HiveExecutor(String key, String start_time, String end_time,
			MediaKey media_key, MediaInfo media_info, List<String> data) throws Exception {
		super(key, start_time, end_time, media_key, media_info, data);
		String fspath = ConfigStorage.HDFS_URL;
		conf.set("fs.defaultFS", fspath);
		try {
			this.fs = FileSystem.get(conf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Could not get HDFS Information [{}]!",fspath);
			throw e;
		} catch (Throwable t) {
			log.error("Failed to load native-hadoop with error: ", t);
			throw new Exception("Failed to load native-hadoop with error " + t);
		}
	}
	
	@Override
	public void writeRawFile() throws Exception {
		String type = media_key.getType();
		String hdfs_path = media_info.getFilesystem_path();
		FSDataOutputStream out = null;
		String outpath = "";
		System.out.println(Integer.toHexString(key.hashCode()));
		try {
			String current_date = new SimpleDateFormat("yyyyMMdd")
					.format(new Date(System.currentTimeMillis()));
			if ("SocialArticle".equalsIgnoreCase(type)) {
				outpath = String.format("%s/%s%s%s/%s.csv", hdfs_path, key, raw_tag,
						current_date, getCurrentTime());
			} else if ("TraditionalArticle".equalsIgnoreCase(type)||"ForumArticle".equalsIgnoreCase(type)){
				outpath = String.format("%s/%s%s%s/%s.csv", hdfs_path, Integer.toHexString(key.hashCode()), raw_tag,
						current_date, getCurrentTime());
			} else if ("SocialAccount".equalsIgnoreCase(type)||"SocialAccountArticle".equalsIgnoreCase(type)||"ForumAccount".equalsIgnoreCase(type)) {
				outpath = String.format("%s%s%s/%s.csv", hdfs_path, raw_tag,
						current_date, getCurrentTime());
			}
			out = fs.create(new Path(outpath));
		} catch (IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			log.error("Could not Create File [{}]! Error : ",outpath, e);
			throw e;
		} catch(Throwable t) {
			log.error("Could not Create File [{}]! Error : ",outpath, t);
			throw new Exception(t);
		}
		for (String row : data) {
			try {
				out.write(row.getBytes("UTF-8"));
				out.write('\n');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("Write HDFS: [{}] Failed!",outpath);
				throw e;
			} 
		}
		try {
			out.close();
			log.info("Write HDFS: [{}] Successed!", outpath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Write HDFS: [{}] Failed!", outpath);
			throw e;
		}
		csv_path = outpath;
		return;
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
			}else if ("WebPageTrace".equals(key_type)||"RSSTrace".equals(key_type)){
				TranditionalAdditionalInfo di = new TranditionalAdditionalInfo(line);
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
	
	private String processFile() {
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
		
		FSDataOutputStream out;
		try {
			out = fs.create(new Path(mapreduce_path));
		} catch (IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			log.error("MapReduce [{}] Failed, could not create output path!",mapreduce_path);
			try {
				fs.delete(new Path(mapreduce_path),true);
			} catch (IllegalArgumentException | IOException e1) {
				// TODO Auto-generated catch block
				log.error("MapReduce [{}] Failed!",csv_path);
			}
			return "";
		}
		for (String row : data) {
			try {
				out.write(dataTransformation(row, media, type, key_type, crawlJson).getBytes("UTF-8"));
				out.write('\n');
				rows++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("MapReduce [{}] Failed!",csv_path);
				rows = 0;
				return "";
			}
		}
		try {
			out.close();
			log.info("Map-R HDFS: [{}] Successed!",	mapreduce_path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("MapReduce [{}] Failed!",csv_path);
			return "";
		}
		return mapreduce_path;
	}
	
	public void writeDB() throws Exception {
		String hive_schema = media_info.getDatabase_schema();
		String hive_table = media_info.getDatabase_table();
		String partitioned = media_info.getPartitioned();
		String partition_column = media_info.getPartition_column();
		String hiveconn = ConfigStorage.HIVE_URL;
		String data_path = processFile();
		if ("".equals(data_path)){
			log.error("Empty data path, return!"); 
			throw new Exception("Empty data path");
		}
		try {
			Class.forName("org.apache.hadoop.hive.jdbc.HiveDriver");
		} catch (ClassNotFoundException e) {
			log.error("No HIVE JDBC Driver found!");
			throw e ;
		}
		Connection con = null;
		Statement stmt = null;
		try {
			con = DriverManager.getConnection(String.format("%s", hiveconn));
			stmt = con.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Could not Connect to HIVE: [{}]!",hiveconn);
			throw e;
		}
		try {
			stmt.execute(String.format("use %s", hive_schema));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("No Schema: [{}] Found!",hive_schema);
			throw e;
		}
		try {
			if ("NO".equalsIgnoreCase(partitioned)) {
				stmt.execute(String.format(
						"load data inpath '%s' into table %s ", data_path,
						hive_table));
			} else {
				stmt.execute(String
						.format("load data inpath '%s' into table %s partition(%s='%s')",
								data_path, hive_table, partition_column, key));
			}
			log.info("Write HIVE: [{}.{}] Successed!", hive_schema, hive_table);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Write HIVE: [{}.{}] Failed, {}",hive_schema,hive_table,e.getMessage());
			rows = 0;
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
	
	@Override
	public void process() {
		// TODO Auto-generated method stub
		
	}

}
