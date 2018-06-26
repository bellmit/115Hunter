package com.sap.cisp.xhna.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.sap.cisp.xhna.data.executor.ITaskExecutor;
/**
 * Interface for application container:
 * - Main for server mode
 * - Main for worker mode
 * @author I314100
 *
 */
public interface IApplicationContainer {
    public  void cleanTaskExecute(ITaskExecutor t);
    
    public ConcurrentHashMap<ITaskExecutor, Future<?>> getMonitorTaskMap();
}
