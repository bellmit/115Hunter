package com.sap.cisp.xhna.data.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HANAExecutor implements DBExecutor {
	private static Logger logger=LoggerFactory.getLogger(HANAExecutor.class);
	@Override
	public List<Map<String, String>> executeQuery(String sql) {
		// TODO Auto-generated method stub
		Connection connection=HANAConnection.getConnection();
		List<Map<String,String>> resultMapList=new ArrayList<Map<String,String>>();
		List<String> columnsList=new ArrayList<String>();
		try {
			PreparedStatement ps=connection.prepareStatement(sql);
			logger.info("execute multiple set result query: sql:{}",sql);
			ResultSet rs=ps.executeQuery();
			for(int i=0;i<rs.getMetaData().getColumnCount();i++){
				columnsList.add(rs.getMetaData().getColumnLabel(i+1));
			}
			while(rs.next()){
				Map<String,String> maps=new HashMap<String,String>();
				extractMapFromResultSet(maps,columnsList,rs);
				resultMapList.add(maps);
			}
			logger.info("sql {} returns result:{}",sql,resultMapList);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resultMapList;
	}
	
	@Override
	public Set<String> executeQueryWithSetResult(String sql){
		Connection connection=HANAConnection.getConnection();
		Set<String> resultsSet=new HashSet<String>();
		try {
			PreparedStatement ps=connection.prepareStatement(sql);
			logger.info("execure one set result query: sql:{}",sql);
			ResultSet rs=ps.executeQuery();
			while(rs.next()){
				resultsSet.add(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("sql {} returns result:{}",sql,resultsSet);
		return resultsSet;
	}

	private void extractMapFromResultSet(
			Map<String, String> maps, List<String> columnsList, ResultSet rs) {
		// TODO Auto-generated method stub
		for(int i=0;i<columnsList.size();i++){
			String column=columnsList.get(i);
			try {
				maps.put(column.toLowerCase(), rs.getString(column));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean executeInsert(String sql, String...strings ) {
		// TODO Auto-generated method stub
		Connection connection=HANAConnection.getConnection();
		PreparedStatement ps=null;
		try {
			ps=connection.prepareStatement(sql);
			for(int i=0;i<strings.length;i++){
				ps.setString(i+1, strings[i]);
			}
			logger.info("execute insert: sql:{}, params:{}",sql,strings);
			return ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public boolean executeUpdate(String sql, String...args) {
		// TODO Auto-generated method stub
		Connection connection=HANAConnection.getConnection();
		PreparedStatement ps=null;
		try {
			ps=connection.prepareStatement(sql);
			for(int i=0;i<args.length;i++){
				ps.setString(i+1, args[i]);
			}
			logger.info("execute update: sql:{}, params:{}",sql,args);
			return ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
