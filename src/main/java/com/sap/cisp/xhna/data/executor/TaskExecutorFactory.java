package com.sap.cisp.xhna.data.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskType;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.task.ITask;

public class TaskExecutorFactory {

    private static TaskExecutorFactory instance = null;

    private TaskExecutorFactory() {
    }

    private static Logger logger = LoggerFactory
            .getLogger(TaskExecutorFactory.class);

    public static synchronized TaskExecutorFactory getInstance() {
        if (instance == null) {
            instance = new TaskExecutorFactory();
        }

        return instance;
    }

    public ITaskExecutor getTaskExecutor(ITask task) {
        String mediaName = task.getMediaName();
        TaskType taskType = task.getTaskType();
        String name = taskType.getName();
        ITaskExecutor executor = null;
        try {
            String executorName = ConfigInstance.getValue(mediaName + "_"
                    + name);
            if (executorName == null
                    || executorName.equalsIgnoreCase("unsupported")) {
                logger.error(
                        "Unsupported executor: MediaName - {}, FunctionName - {}",
                        mediaName, name);
                return null;
            }
            executor = (ITaskExecutor) Class.forName(executorName)
                    .newInstance();
            executor.setTask(task);
            //If in debug mode, set the test flag on and not save to HDFS, avoid error logs
            if(ConfigInstance.getDebugFlagOn()) {
              ((AbstractTaskExecutor)executor).setTestFlagOn(true);
            }
        } catch (InstantiationException e) {
            logger.error("InstantiationException", e);
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException", e);
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException", e);
        }
        return executor;
    }
}
