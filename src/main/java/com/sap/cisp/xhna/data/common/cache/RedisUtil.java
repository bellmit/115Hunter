package com.sap.cisp.xhna.data.common.cache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.sap.cisp.xhna.data.db.HANAConnection;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class RedisUtil {

	/**
	 * @param args
	 */
	static Logger logger = LoggerFactory.getLogger(HANAConnection.class);

//	private static JedisPool pool;
//	private final static Object syncLock = new Object();
//
//	public static JedisPool getJedisPool() {
//		if (pool == null) {
//			synchronized (syncLock) {
//				if (pool == null) {
//					pool = new JedisPool(new JedisPoolConfig(), "localhost");
//				}
//			}
//		}
//		return pool;
//	}
//
//	public static void destroyJedisPool() {
//		pool.destroy();
//	}

	/************************************* Below is used for Redis Cluster *************************************************/
	private static JedisCluster jc;
	static {		
		Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(
					"configurations/redis.properties"));
			String host = null;
			String port = null;
			for (int i = 1;; i++) {
				host = properties.getProperty("host" + i);
				port = properties.getProperty("port" + i);
				if(host == null || port == null)
					break;
				jedisClusterNodes.add(new HostAndPort(host, Integer.parseInt(port)));
			}
			jc = new JedisCluster(jedisClusterNodes);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("FileNotFoundException", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IOException", e);
		} catch (Exception e) {
			logger.error("Exception", e);
		}		
	}

	public static boolean setKeyIfNotExistByCluster(String key) {
		long status = jc.setnx(key, "ok");
		if (status == 1)
			return true; // return true if set key successfully
		return false; // return false if key exists already
	}

	public static void main(String[] args) {
		final Stopwatch stopwatch = Stopwatch.createStarted();
		String key = "key:" + "foo";
		boolean status = RedisUtil.setKeyIfNotExistByCluster(key);
		stopwatch.stop();

		if (status) {
			System.out.println(key + " Cached sussfully.");
		} else {
			System.out.println(key + " Already cached before.");
		}

	}

}
