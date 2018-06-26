package com.sap.cisp.xhna.data.storage;

public interface IStorageExecutor {
	public void writeRawFile()throws Exception;
	public void process()throws Exception;
	public void writeDB()throws Exception;
}
