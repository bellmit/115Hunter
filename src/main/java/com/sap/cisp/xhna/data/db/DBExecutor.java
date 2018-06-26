package com.sap.cisp.xhna.data.db;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DBExecutor {
	public List<Map<String,String>> executeQuery(String sql);
	public Set<String> executeQueryWithSetResult(String sql);
	public boolean executeInsert(String sql, String...strings );
	public boolean executeUpdate(String sql, String...args);
}
