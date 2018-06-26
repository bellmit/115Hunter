package com.sap.cisp.xhna.data;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sap.cisp.xhna.data.executor.ITaskExecutor;

public class TaskExecutorPool extends ThreadPoolExecutor {

    public TaskExecutorPool(int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory factory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, factory);
    }

    public TaskExecutorPool(int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected <V> RunnableFuture<V> newTaskFor(Callable<V> callable) {
        if (callable instanceof ITaskExecutor)
            return (RunnableFuture<V>) ((ITaskExecutor) callable).newTask();
        return super.newTaskFor(callable);
    }
}