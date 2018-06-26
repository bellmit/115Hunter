package com.sap.cisp.xhna.data.finder;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.Main;
import com.sap.cisp.xhna.data.TaskManager;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;

public class WorkerTaskFinder extends AbstractTaskFinder {
    private static Logger logger = LoggerFactory
            .getLogger(WorkerTaskFinder.class);

    public static WorkerTaskFinder getInstance() {
        return WorkerTaskFinderHolder.instance;
    }

    private static class WorkerTaskFinderHolder {
        public static WorkerTaskFinder instance = new WorkerTaskFinder();
    }

    private WorkerTaskFinder() {
    }

    @Override
    public void find() {
        // Do nothing in find()
        // Task will be put into queue by gearman worker - TaskManagementWorker
    }

    @Override
    public boolean addTask(ITaskExecutor taskExecutor) {
        logger.debug("add task {} to runnable tasks queue", taskExecutor
                .getTask().toString());
        BlockingQueue<ITaskExecutor> queue = null;
        if (ConfigInstance.getCurrentWorkMode() == TaskManager.WorkMode.WORKER_ONLY) {
            // For worker only mode, use the executor queue directly
            logger.debug("==========================> Add task to executor queue.");
            Main.increaseTaskSize();
            queue = Main.getExecutorQueue();
        } else {
            // For worker-server mode, we need a local queue to differentiate
            // from the queue used by task finder
            logger.debug("==========================> Add task to local queue.");
            queue = Main.getLocalExecutorQueue();
            // Do not decrease the executor queue size to avoid confusion
            taskExecutor.setCleanTaskSkipped(true);
        }
        if (!queue.offer(taskExecutor)) {
            logger.error("Failed to add task to queue {}.", queue);
            return false;
        }
        return true;
    }

}
