package com.sap.cisp.xhna.data.finder;

import java.util.concurrent.BlockingQueue;

import com.sap.cisp.xhna.data.executor.ITaskExecutor;

public interface ITaskFinder extends Runnable {

    public BlockingQueue<ITaskExecutor> getTasks();

    public void setTasks(BlockingQueue<ITaskExecutor> tasks);

    public void find();
}
