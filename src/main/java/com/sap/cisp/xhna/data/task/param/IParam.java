package com.sap.cisp.xhna.data.task.param;

public interface IParam {
	public String getStartTime();

	public String getEndTime();

	public String getAccount();

	public String getKeyword();

	public String getRss();
	
	public String getUrl();

	public String getMediaName();

	public void addUpdatedCount(int count);

	public int getUpdatedCount();
}
