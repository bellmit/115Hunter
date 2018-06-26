package com.sap.cisp.xhna.data.task.worker.main;

import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;

/**
 * 
 * @author hluan
 *
 */
public interface IWorkerAgent  extends GearmanFunction,Runnable {
	
	/*
	 * Register the function with a corresponding actual worker
	 */
//	public void registerWorkHandler(String function,  GearmanFunction workhandler);

	/*
	 * Dispatch the fuction work with data
	 */
	public  byte[] dispatch(String function, byte[] data, GearmanFunctionCallback callback) throws Exception;

}
