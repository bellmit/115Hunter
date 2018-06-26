package com.sap.cisp.xhna.data.finder;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.Main;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.TaskExecutorFactory;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.TaskManager;

public abstract class AbstractTaskFinder implements ITaskFinder {

    private BlockingQueue<ITaskExecutor> tasks = new LinkedBlockingQueue<ITaskExecutor>();
    private static Logger logger = LoggerFactory
            .getLogger(AbstractTaskFinder.class);

    @Override
    public void run() {

        logger.info("Start to find tasks.");
        find();
        logger.info("Finish to find tasks.");
    }

    @Override
    public BlockingQueue<ITaskExecutor> getTasks() {
        return tasks;
    }

    public void setTasks(BlockingQueue<ITaskExecutor> tasks) {
        this.tasks = tasks;
    }

    public boolean addTask(ITask task) {
        ITaskExecutor taskExecutor = TaskExecutorFactory.getInstance()
                .getTaskExecutor(task);
        if (taskExecutor != null) {
            return addTask(taskExecutor);
        } else {
            // Cannot create task executor, may due to wrong media name(case
            // sensitive)
            // Change the task as stopped to avoid endless "initial" state
            task.changeTaskStatusToStopped();
            return false;
        }
    }

    public boolean addTask(ITaskExecutor taskExecutor) {
        // Only reject flooding tasks in SINGLETON work mode
        if ((ConfigInstance.getCurrentWorkMode() == TaskManager.WorkMode.SINGLETON) && Main.getTaskSize() > ConfigInstance
                .getThreadPoolSizeThreshold()) {
            logger.error("!!! Discard the new task submission due to maxmiumPoolSize has been exceeded. !!!");
            taskExecutor.getTask().resetTaskStatus();
            return false;
        }
        logger.debug("add task {} to runnable tasks queue", taskExecutor
                .getTask().toString());
        Main.increaseTaskSize();
        if (!tasks.offer(taskExecutor)) {
            logger.error("Failed to add task to queue.");
            return false;
        }
        return true;
    }

    protected void addTasks(List<ITask> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            addTask(tasks.get(i));
        }
    }

}
