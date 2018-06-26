package com.sap.cisp.xhna.data.executor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;

import com.sap.cisp.xhna.data.IApplicationContainer;
import com.sap.cisp.xhna.data.task.ITask;

@SuppressWarnings("rawtypes")
public interface ITaskExecutor extends Callable {

    public void start(Map<String, Object> ctx) throws Exception;

    public void error(Map<String, Object> ctx, Exception e) throws Exception;

    public void complete(Map<String, Object> ctx) throws Exception;

    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception;

    public void save(Map<String, Object> ctx, List<String> data)
            throws Exception;

    public void stop() throws Exception;

    public void cancel(boolean mayInterruptIfRunning) throws Exception;

    public void cancel(Map<String, Object> ctx, InterruptedException e,
            boolean mayInterruptIfRunning) throws Exception;

    public boolean isCanceled();

    public boolean isTestFlagOn();

    public boolean isCleanTaskSkipped();
    
    public void setCleanTaskSkipped(boolean isCleanTaskSkipped);

    public boolean isCacheable();

    public void setTask(ITask task);

    public ITask getTask();

    public RunnableFuture<?> newTask();
    
    public void setTaskContainer (IApplicationContainer taskContainer);
    
    public IApplicationContainer getTaskContainer();

    public static final int RETRY_THRESHOLD = 5;
}
