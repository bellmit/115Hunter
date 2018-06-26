package com.sap.cisp.xhna.data.executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.sap.cisp.xhna.data.ApplicationContext;
import com.sap.cisp.xhna.data.IApplicationContainer;
import com.sap.cisp.xhna.data.config.ConfigStorage;
import com.sap.cisp.xhna.data.model.databasemapping.MediaInfo;
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;
import com.sap.cisp.xhna.data.storage.hive.HiveExecutor;
import com.sap.cisp.xhna.data.storage.iq.IQExecutor;
import com.sap.cisp.xhna.data.storage.oltpdatabase.DBExecutor;
import com.sap.cisp.xhna.data.task.ITask;

public abstract class AbstractTaskExecutor implements ITaskExecutor {

    protected ITask task;

    protected volatile boolean isCanceled;

    protected volatile boolean isError;

    protected IApplicationContainer  taskContainer;

    public static Logger logger = LoggerFactory
            .getLogger(AbstractTaskExecutor.class);

    protected boolean isTestFlagOn = false;

    protected boolean isCleanTaskSkipped = false;

    protected boolean isCacheable = true;

    public boolean isCacheable() {
        return isCacheable;
    }

    public void setCacheable(boolean isCacheable) {
        this.isCacheable = isCacheable;
    }

    // For sub-tasks which will not be put into monitor map
    public boolean isCleanTaskSkipped() {
        return isCleanTaskSkipped;
    }

    public void setCleanTaskSkipped(boolean isCleanTaskSkipped) {
        this.isCleanTaskSkipped = isCleanTaskSkipped;
    }

    public boolean isTestFlagOn() {
        return isTestFlagOn;
    }

    public void setTestFlagOn(boolean isTestFlagOn) {
        this.isTestFlagOn = isTestFlagOn;
    }

    public void setTask(ITask task) {
        this.task = task;
    }

    public ITask getTask() {
        return task;
    }

    public void setTaskContainer (IApplicationContainer taskContainer) {
        this.taskContainer = taskContainer;
    }

    public IApplicationContainer getTaskContainer() {
        return taskContainer;
    }

    @Override
    public Object call() throws Exception {

        Map<String, Object> ctx = new HashMap<String, Object>();
        ctx.put("task", task);
        List<String> data = null;
        try {
            start(ctx);
            data = execute(ctx);
            if (!isTestFlagOn) {
                save(ctx, data);
            }
        } catch (InterruptedException e) {
            logger.info("Catch InterruptedException in call() ", e);
            Thread.currentThread().interrupt();
            try {
                if (!isCanceled) {
                    cancel(ctx, e, false);
                }
            } catch (Exception e1) {
                logger.error("Caught Exception during task cancel.", e1);
            }
        } catch (Exception e) {
            try {
                error(ctx, e);
            } catch (Exception e1) {
                logger.error("Caught Exception during task error handling.", e1);
            }
        } finally {

            try {
                complete(ctx);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Caught Exception during task completion.", e);
            }

            if (!isCanceled && !isCleanTaskSkipped && taskContainer != null) {
                taskContainer.cleanTaskExecute(this);
            }
        }
        return data;
    }

    public void stop() throws Exception {
        Thread.currentThread().interrupt();
    }

    @Override
    public void cancel(boolean mayInterruptIfRunning) throws Exception {
        Map<String, Object> ctx = new HashMap<String, Object>();
        ctx.put("task", task);
        cancel(ctx, new InterruptedException("cancel invoked."),
                mayInterruptIfRunning);
    }

    public void cancel(Map<String, Object> ctx, InterruptedException e,
            boolean mayInterruptIfRunning) throws Exception {
        isCanceled = true;
        if (mayInterruptIfRunning) {
            Thread.currentThread().interrupt();
        }
        ITask task = (ITask) ctx.get("task");
        logger.error(
                "cancel task: Id {}, MediaName {}, Type {}, Param {}, InterruptedException {}",
                task.getTaskId(), task.getMediaName(), task.getTaskType(),
                task.getParam(), e.getMessage());
        // reset the task status for next time running
        task.resetTaskStatus();
        if(taskContainer != null) {
           taskContainer.cleanTaskExecute(this);
        }
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RunnableFuture<?> newTask() {
        return new FutureTask<Object>(this) {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                try {
                    AbstractTaskExecutor.this.cancel(mayInterruptIfRunning);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return super.cancel(mayInterruptIfRunning);
            }
        };
    }

    public void error(Map<String, Object> ctx, Exception e) throws Exception {
        isError = true;
        ITask task = (ITask) ctx.get("task");
        logger.error(
                "execute task with error: Id {}, MediaName {}, Type {}, error --> ",
                task.getTaskId(), task.getMediaName(), task.getTaskType(), e);
        // Change error task to stopped
        task.changeTaskStatusToStopped();
    }

    @Override
    public void start(Map<String, Object> ctx) throws Exception {
        ITask task = (ITask) ctx.get("task");
        logger.info("start task:{},{},{}", task.getMediaName(),
                task.getTaskType(), task.getParam());
        task.startTask();
    }

    @Override
    public void complete(Map<String, Object> ctx) throws Exception {
        ITask task = (ITask) ctx.get("task");
        // Two cases does not update the last crawl time for now
        // 1.Canceled task
        // 2.Execution error
        if (!isCanceled && !isError && !isTestFlagOn()) {
            logger.info("update task information accordingly:{},{}", task.getMediaName(),
                    task.getTaskType());
            task.completeTask();
        }
        logger.info("complete task:{},{}", task.getMediaName(),
                task.getTaskType());
    }

    @Override
    public void save(Map<String, Object> ctx, List<String> data)
            throws Exception {

        if (data == null || data.size() == 0) {
            logger.warn("No data Needs to be Persisted!");
            return;
        }
        // For now, write RAW and wirte DB is "ALL OR NONE"
        if (isCanceled) {
            logger.warn("Task interrupted. Do not proceed persistance.");
            return;
        }
        logger.info("saving...");
        // google guava stopwatch, to caculate the storage execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();
        String key = ((ITask) ctx.get("task")).getParam().getString("task_key");
        String start_time = ((ITask) ctx.get("task")).getParam().getStartTime();
        String end_time = ((ITask) ctx.get("task")).getParam().getEndTime();
        if (start_time == null) {
            start_time = "";
        }
        if (end_time == null) {
            end_time = "";
        }
        MediaKey media_key = ((ITask) ctx.get("task")).getParam().getMediaKey();
        MediaInfo media_info = ApplicationContext.getMediaInfoByKey(media_key);
        if ("hive".equalsIgnoreCase(ConfigStorage.DATABASE_TYPE)) {
            HiveExecutor executor = new HiveExecutor(key, start_time, end_time,
                    media_key, media_info, data);
            logger.info(
                    "Begin to write raw file to HDFS... Media Key: {}; Media Info: {}",
                    media_key, media_info);
            executor.writeRawFile();
            logger.info("Begin to write to HIVE...");
            executor.writeDB();
            int count = executor.getRows();
            ((ITask) ctx.get("task")).getParam().addUpdatedCount(count);
        } else if ("iq".equalsIgnoreCase(ConfigStorage.DATABASE_TYPE)) {
            IQExecutor executor = new IQExecutor(key, start_time, end_time,
                    media_key, media_info, data);
            logger.info("Begin to write raw file to HDFS...");
            executor.writeRawFile();
            logger.info("Begin to write to IQ...");
            executor.writeDB();
        } else if ("oltpdatabase".equalsIgnoreCase(ConfigStorage.DATABASE_TYPE)) {
            DBExecutor executor = new DBExecutor(key, start_time, end_time,
                    media_key, media_info, data);
            logger.info("Begin to write raw file to HDFS...");
            executor.writeRawFile();
            logger.info("Begin to write to oltp database...");
            executor.writeDB();
        }
        /*2016-01-08 only save as local files*/
        else{
            DBExecutor executor = new DBExecutor(key, start_time, end_time,
                    media_key, media_info, data);
            logger.info("Begin to write raw file to local...");
            executor.writeRawFile();
        }
        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        logger.info(
                "====> Save task Id:{}, MediaName:{}, Type:{} with execution duration {} ms.",
                task.getTaskId(), task.getMediaName(), task.getTaskType(),
                nanos);
    }

}
