package com.sap.cisp.xhna.data.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColumnInfo {
	private String schema;
	private String table;
	private Connection conn;
	private List<String[]> data;
	private static Logger log = LoggerFactory.getLogger(ColumnInfo.class);
	
	public ColumnInfo(Connection conn, String schema, String table, List<String[]> data){
		this.conn = conn;
		this.schema = schema;
		this.table = table;
		this.data = data;
	}
	
	public ColumnInfo(Connection conn, String schema, String table){
		this.conn = conn;
		this.schema = schema;
		this.table = table;
	}
	
	public String[] getColumnNames(){
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format("select * from %s.%s",schema, table));
			ResultSetMetaData meta_info = rs.getMetaData();
			String[] column_lists = new String[meta_info.getColumnCount()];
			for(int i = 0; i < meta_info.getColumnCount(); ++i){
				column_lists[i] = meta_info.getColumnName(i+1);
			}
			return column_lists;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Could not get [{}.{}] column names !",schema, table);
		}
		return null;
	}
	
	private ColumnType[] getColumnInfo(){
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format("select * from %s.%s",schema, table));
			ResultSetMetaData meta_info = rs.getMetaData();
			ColumnType[] column_lists = new ColumnType[meta_info.getColumnCount()];
			for(int i = 0; i < meta_info.getColumnCount(); ++i){
				ColumnType value = new ColumnType(meta_info.getColumnName(i+1),meta_info.getColumnType(i+1),meta_info.getColumnTypeName(i+1));
				column_lists[i] = value;
			}
			return column_lists;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Could not get [{}.{}] Column Info !",schema, table);
		}
		return null;
	}
	
	public void insertData(){
		ColumnType[] column_types = getColumnInfo();
		if (data.get(0).length != column_types.length){
			log.error("Column count dismatch: {} in database table, but actually {} in data file",column_types.length, data.get(0).length);
		}
		String sql = String.format("insert into %s.%s values(", schema, table);
		for(int i = 0; i < column_types.length; ++i){
			sql = sql + "?,";
		}
		sql = sql.substring(0, sql.length()-1) + ')';
		PreparedStatement prep_stmt = null;
		try {
			conn.setAutoCommit(false);
			prep_stmt = conn.prepareStatement(sql);
			for(String[] row: data){
				for(int i = 0; i < row.length; ++i){
					prep_stmt.setString(i+1, row[i]);
				}
				prep_stmt.addBatch();
			}
			prep_stmt.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.error("SQL {} Execution Error!",sql);
			e.printStackTrace();
		}
		finally{
            if (prep_stmt != null) {
                try {
                    prep_stmt.close();
                    conn.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                }
            }
		}
	}
}
