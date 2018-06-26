package com.sap.cisp.xhna.data.executor.receiver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.Main;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.task.worker.main.TaskWorkerMain;

/**
 * This local Receiver aims to submit jobs to local scheduler
 *
 */
public class LocalReceiver implements Receiver {
    private ITaskExecutor localTask = null;
    private static Logger logger = LoggerFactory.getLogger(LocalReceiver.class);
    private ExecutorService taskExecutorPool = null;

    public void setLocalJob(ITaskExecutor localTask) {
        this.localTask = localTask;
    }

    public LocalReceiver(ITaskExecutor localTask) {
        this.localTask = localTask;
    }

    public LocalReceiver(ITaskExecutor localTask,
            ExecutorService taskExecutorPool) {
        this.localTask = localTask;
        this.taskExecutorPool = taskExecutorPool;
    }

    @SuppressWarnings("unchecked")
    public Object action() throws Exception {
        try {
            if (taskExecutorPool == null) {
                if (Main.getTaskExecutorPool() == null) {
                    // For worker work mode only, since Main is not initialized,
                    // use
                    // TaskWorkerMain executor service instead.
                    if (TaskWorkerMain.getInstance().getTaskSize() > ConfigInstance
                            .getThreadPoolSizeThreshold()) {
                        logger.error("!!! Discard the new task submission due to maxmiumPoolSize has been exceeded. !!!");
                        if (localTask.getTaskContainer() != null) {
                            localTask.getTask().resetTaskStatus();
                            localTask.setCleanTaskSkipped(true);
                            localTask.getTaskContainer().cleanTaskExecute(
                                    localTask);
                        }
                        return null;
                    }
                    taskExecutorPool = TaskWorkerMain.getInstance()
                            .getTaskExecutorPool();
                } else {
                    // For server
                    taskExecutorPool = Main.getTaskExecutorPool();
                    if (Main.getTaskSize() > ConfigInstance
                            .getThreadPoolSizeThreshold()) {
                        logger.error("!!! Discard the new task submission due to maxmiumPoolSize has been exceeded. !!!");
                        if (localTask.getTaskContainer() != null) {
                            localTask.getTask().resetTaskStatus();
                            localTask.setCleanTaskSkipped(true);
                            localTask.getTaskContainer().cleanTaskExecute(
                                    localTask);
                        }
                        return null;
                    }
                }
                // still cannot get usable executor service
                if (taskExecutorPool == null) {
                    throw new DataCrawlException(
                            "Cannot get usable executor service.");
                }
            }

            Future<?> future = taskExecutorPool.submit(localTask);
            // sub-tasks will not be put into monitor map
            if (!localTask.isCleanTaskSkipped()) {
                localTask.getTaskContainer().getMonitorTaskMap()
                        .put(localTask, future);
            }
            return future;
        } catch (RejectedExecutionException re) {
            localTask.getTask().resetTaskStatus();
            logger.error(
                    "!!! The maximumPoolSize has been reached. Reject the new task submission by pool. !!!",
                    re);
            return null;
        }
    }
}
