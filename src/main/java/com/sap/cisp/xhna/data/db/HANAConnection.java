package com.sap.cisp.xhna.data.db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HANAConnection {
	private static String host;
	private static String port;
	private static String username;
	private static String password;
	private static String schema;
	static Logger logger = LoggerFactory.getLogger(HANAConnection.class);

	private static Connection connection;

	private HANAConnection() {
	}

	static {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(
					"configurations/hana.properties"));
			host = properties.getProperty("host");
			port = properties.getProperty("port");
			username = properties.getProperty("username");
			password = properties.getProperty("password");
			schema = properties.getProperty("cureentschema");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("FileNotFoundException", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IOException", e);
		}
	}

	public synchronized static Connection getConnection() {

		if (connection == null) {
			try {
				connection = DriverManager.getConnection("jdbc:sap://" + host
						+ ":" + port + "/?currentschema=" + schema, username,
						password);
				connection.setAutoCommit(true);
				logger.info("create a new connection to db host:{},port:{} with username:{}",
				host, port, username);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("SQLException", e);
			}
		}
		logger.info("get a db connection host:{},port:{} with username:{}",
				host, port, username);
		return connection;
	}

}
