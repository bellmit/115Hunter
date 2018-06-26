package com.sap.cisp.xhna.data.db;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class HANAService implements Serializable {
    private static final long serialVersionUID = 1292350887765933232L;
    private static HANAExecutor hanaExecutor=new HANAExecutor();
	
	
	public static List<Map<String,String>> listAllMediaInfos(){
		String sql=QuerySqls.getSql("listAllMediaInfos");
		return hanaExecutor.executeQuery(sql);
	}
	

	public void updateTaskStatus(String taskId, String status) {
		// TODO Auto-generated method stub
		String sql=QuerySqls.getSql("updateTaskStatus");
		hanaExecutor.executeUpdate(sql, status,taskId);
	}

	public void updateTaskDataPart(String taskId,String endTime) {
		// TODO Auto-generated method stub
		String sql=QuerySqls.getSql("updateTaskDataPart");
		hanaExecutor.executeUpdate(sql, endTime,endTime,endTime,taskId);
	}

	public List<Map<String, String>> listSocialTasksMaps() {
		String sql=QuerySqls.getSql("listRunnableSocialTasks");
		return hanaExecutor.executeQuery(sql);
	}

	public List<Map<String, String>> listSocialAccountArticleTasksMaps() {
		String sql=QuerySqls.getSql("listRunnableAccountArticleTasks");
		return hanaExecutor.executeQuery(sql);
	}
	
	public List<Map<String, String>> listTraditionalTasksMaps() {
		String sql=QuerySqls.getSql("listRunnableTraditionalTasks");
		return hanaExecutor.executeQuery(sql);
	}
	
}
