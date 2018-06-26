package com.sap.cisp.xhna.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskManager.WorkMode;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.receiver.LocalReceiver;
import com.sap.cisp.xhna.data.executor.receiver.Receiver;
import com.sap.cisp.xhna.data.executor.receiver.RemoteReceiver;
import com.sap.cisp.xhna.data.executor.stream.DatasiftUtils;
import com.sap.cisp.xhna.data.finder.ITaskFinder;
import com.sap.cisp.xhna.data.task.worker.JobSubmitClientHelper;
import com.sap.cisp.xhna.data.task.worker.TaskManagementUtils;
import com.sap.cisp.xhna.data.task.worker.TaskManagementWorkerPool;
import com.sap.cisp.xhna.data.task.worker.main.IWorkerAgent;
import com.sap.cisp.xhna.data.task.worker.main.TaskWorkerMain;
import com.sap.cisp.xhna.data.token.TokenManager;

public class Main implements IApplicationContainer {
    private static Logger logger = LoggerFactory.getLogger(Main.class);
    private static ExecutorService taskExecutorPool = null;
    private static ScheduledThreadPoolExecutor taskFinderService = null;
    private static ScheduledThreadPoolExecutor taskMonitorService = null;
    private static BlockingQueue<ITaskExecutor> executorQueue = null;
    private static BlockingQueue<ITaskExecutor> localExecutorQueue = null;
    private static ConcurrentHashMap<ITaskExecutor, Future<?>> monitorTaskMap = null;
    private static AtomicLong taskSize = new AtomicLong(0);
    private static final int defaultThreadNum = 100;
    private static final String startFile = (System.getProperty("os.name")
            .toLowerCase().indexOf("linux") >= 0) ? "/var/run/start.txt"
            : "./start.txt";
    private static WorkMode workMode = TaskManager.WorkMode.SINGLETON;

    public static void main(String[] args) {
        init();
        // Add JVM shutdownhook
        // Invoked when receive signal: SIGTERM(15) SIGHUP(1) SIGINT(2)
        addShutDownHook();
 
        logger.debug("start...");
        if (args.length == 1) {
            if (args[0].equals("stop")) {
                File f = new File(startFile);
                if(f.delete()) {
                    logger.info("Start file is deleted sucessfully.");
                } else {
                    logger.error("Failed to delete start file.");
                }
            }
        } else if (args.length == 0 || args[0].equals("start")) {
            logger.info("Start SAP.XHNA.Data Service");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(startFile);
                fos.flush();
            } catch (FileNotFoundException e) {
                logger.error("FileNotFoundException", e);
            } catch (IOException e) {
                logger.error("IOException", e);
            } finally {
                if(fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        logger.error("IOException", e);
                    }
                }
            }
            if (workMode != TaskManager.WorkMode.WORKER_ONLY) {
                startService();
            } else {
                /**
                 * Since there are a lot of static data structures declared.
                 * Working in worker-only mode will cause Gearman worker packet
                 * lost.
                 */
                logger.error("This service can ONLY work in server mode.");
                System.exit(-1);
            }
        }
    }

    public static void init() {
        int threadNum = defaultThreadNum;
        String threadNumStr = ConfigInstance.getValue("ThreadNum");
        try {
            logger.info("> Thread num: {}", threadNumStr);        
            threadNum = Integer.parseInt(threadNumStr);  
        } catch (NumberFormatException ne) {
            logger.error("Invalid thread number {}, use default {}.",
                    threadNumStr, threadNum);
        }
        try {
            workMode = ConfigInstance.getCurrentWorkMode();
            logger.info("************* Work mode : {} ***************",
                    workMode);  
        } catch (NumberFormatException ne) {
            logger.error("Invalid work mode {}, use default {}.",
                    ConfigInstance.getValue("WorkMode"), workMode);
        }
        if (workMode != TaskManager.WorkMode.WORKER_ONLY) {
            taskMonitorService = new ScheduledThreadPoolExecutor(20);
            executorQueue = new LinkedBlockingQueue<ITaskExecutor>();
            localExecutorQueue = new LinkedBlockingQueue<ITaskExecutor>();
            monitorTaskMap = new ConcurrentHashMap<ITaskExecutor, Future<?>>();
            taskFinderService = new ScheduledThreadPoolExecutor(3);
            ThreadFactory factory = new TaskThreadFactory();
            taskExecutorPool = new TaskExecutorPool(threadNum, threadNum, 0L,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(threadNum*5),factory);
            //Strategy when executor pool reach maximumPoolSize, currently use AbortPolicy
            ((ThreadPoolExecutor) taskExecutorPool).setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        }
        //initialize token manager in advance, in case there are bunch of tasks starting with
        //very short token period.
        TokenManager.getInstance().init();
    }

    private static void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.error("Shutdown hook called");
                Main.stop();
            }
        });
    }

    private static void startService() {
        final IApplicationContainer container = new Main();
       
        try {
            String finderClass = ConfigInstance.getValue("Finder");
            logger.info("Finder class : {}", finderClass);

            ITaskFinder finder = (ITaskFinder) Class.forName(finderClass)
                    .newInstance();
            // Set BlockingQueue to the finder (producer)
            finder.setTasks(executorQueue);

            taskFinderService.scheduleAtFixedRate(finder, 2L, 15L,
                    TimeUnit.SECONDS);
            taskMonitorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (!(new File(startFile)).exists()) {
                        stop();
                    }
                }
            }, 2L, 2L, TimeUnit.SECONDS);

            // The job follow sequence: From task finder -> Gearman
            // server
            // queue -> Gearman worker -> local queue
            taskFinderService.execute(new Runnable() {
                @Override
                public void run() {
                    ITaskExecutor taskExecutor = null;
                    while (true) {
                        try {
                            // Block until queue is not null
                            taskExecutor = executorQueue.take();
                            logger.debug("Queue Size ==> {}",
                                    executorQueue.size());
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage());
                        }

                        if (workMode == TaskManager.WorkMode.SINGLETON) {
                            Receiver localReceiver = new LocalReceiver(
                                    taskExecutor);
                            // set the task container for clean task callback
                            taskExecutor.setTaskContainer(container);
                            try {
                                // Just submit the task to executor
                                // service
                                localReceiver.action();
                            } catch (Throwable t) {
                                // TODO Auto-generated catch block
                                logger.error("Local receiver error.", t);
                            }
                        } else if (workMode == TaskManager.WorkMode.WORKER_AND_SERVER
                                || workMode == TaskManager.WorkMode.SERVER_ONLY) {
                            try {
                                // Just submit the task to Gearman
                                // server
                                // and to be processed in worker
                                // set the task container for clean task callback
                                taskExecutor.setTaskContainer(container);
                                logger.debug(
                                        "Submit task {} to Gearman server.",
                                        taskExecutor.getTask());
                                Receiver remoteReceiver = new RemoteReceiver(
                                        TaskManagementUtils.FunctionEnum.ADD_TASK
                                                .toString(), SerializationUtils
                                                .serialize(taskExecutor
                                                        .getTask()));
                                remoteReceiver.action();
                            } catch (Throwable t) {
                                // TODO Auto-generated catch block
                                logger.error(
                                        "Cannot submit task {} to Gearman server. Please check Gearman server is available.",taskExecutor.getTask(),
                                        t);
                            } finally {
                                //after submission, decrease task size
                                if (taskExecutor.getTaskContainer() != null) {
                                    taskExecutor.setCleanTaskSkipped(true);
                                    taskExecutor.getTaskContainer()
                                            .cleanTaskExecute(taskExecutor);
                                }
                            }
                        }

                    }
                }
            });

            taskFinderService.execute(new Runnable() {
                @Override
                public void run() {
                    ITaskExecutor taskExecutor = null;
                    while (true) {
                        try {
                            // Block until queue is not null
                            taskExecutor = localExecutorQueue.take();
                            logger.debug("Local Queue Size ==> {}",
                                    localExecutorQueue.size());
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage());
                        }

                        Receiver localReceiver = new LocalReceiver(taskExecutor);
                        try {
                            localReceiver.action();
                        } catch (Throwable t) {
                            // TODO Auto-generated catch block
                            logger.error("Local receiver error.", t);
                        }
                    }
                }
            });

            /** TaskManagementWorkerPool class is obsolete after distribution test 
             *  Use java-gearman-service worker API
             */
            IWorkerAgent taskworker = TaskWorkerMain.getInstance().init(
                    monitorTaskMap);
            // Create gearman server thread and worker thread according
            // to
            // the work mode.
            logger.debug("Start with work mode {}", workMode);
            TaskManager.createGearman(taskMonitorService, taskworker);

        } catch (InstantiationException e) {
            logger.error("InstantiationException", e);
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException", e);
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static ScheduledThreadPoolExecutor getTaskMonitorService() {
        return taskMonitorService;
    }

    public static ExecutorService getTaskExecutorPool() {
        return taskExecutorPool;
    }

    public static void setTaskExecutorPool(ExecutorService taskExecutorPool) {
        Main.taskExecutorPool = taskExecutorPool;
    }

    public ConcurrentHashMap<ITaskExecutor, Future<?>> getMonitorTaskMap() {
        return monitorTaskMap;
    }

    public static BlockingQueue<ITaskExecutor> getExecutorQueue() {
        return executorQueue;
    }

    public static BlockingQueue<ITaskExecutor> getLocalExecutorQueue() {
        return localExecutorQueue;
    }

    public static void increaseTaskSize() {
        logger.debug("Increase Task Size ==> {}", taskSize.incrementAndGet());
    }

    public static void decreaseTaskSize() {
        logger.debug("Decrease Task Size ==> {}", taskSize.decrementAndGet());
    }
    
    public static long getTaskSize() {
        return taskSize.get();
    }

    private static void stop() {
        logger.info("Stop SAP.XHNA.Data TaskFinder Service");
        if (workMode == TaskManager.WorkMode.WORKER_ONLY) {
            System.exit(-1);
        }
        // Cancel scheduled but not started task, and avoid new ones
        taskFinderService.shutdown();

        // Wait for the running tasks
        try {
            taskFinderService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Interrupt the threads and shutdown the scheduler
        taskFinderService.shutdownNow();

        // Cancel scheduled but not started task, and avoid new ones
        taskExecutorPool.shutdown();

        // Stop tasks which has not been put into executor pool, not start
        synchronized (Main.class) {
            for (ITaskExecutor t : executorQueue) {
                try {
                    logger.info("Cancel queued tasks, not put in executor pool yet.");
                    t.cancel(true);
                } catch (Exception e) {
                    logger.error("Exception", e);
                }
            }
            executorQueue.clear();
        }

        // cancel the running threads without interrupt
        if (taskSize.get() > 0) {
            logger.info(
                    "Cancel the running threads threads without interrupt, taskSize {} : ",
                    taskSize.get());

            for (ITaskExecutor executor : monitorTaskMap.keySet()) {
                try {
                    Future<?> future = monitorTaskMap.get(executor);
                    // cancel only when task is not done or canceled
                    if (future != null
                            && (!(future.isCancelled() || future.isDone()))) {
                        future.cancel(false);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    logger.error("Exception", e);
                }
            }
        }

        boolean waitTerminationOk = false;
        int tryNum = 0;
        while (!waitTerminationOk && tryNum < 12) {
            // Wait for the running tasks with long operations, 5 seconds
            // timeout
            try {
                waitTerminationOk = taskExecutorPool.awaitTermination(5,
                        TimeUnit.SECONDS);
                logger.info("Wait for running tasks to complete "
                        + (waitTerminationOk ? "successfully." : "timeout.")
                        + "RetryNum: {}", tryNum);
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
            tryNum++;
        }

        // Interrupt the remaining threads and change the task status
        if (taskSize.get() > 0 || !waitTerminationOk) {
            logger.info(
                    "Interrupt the remaining threads and change the task status to STOPPED, taskSize {} : ",
                    taskSize.get());

            for (ITaskExecutor executor : monitorTaskMap.keySet()) {
                try {
                    Future<?> future = monitorTaskMap.get(executor);
                    // cancel only when task is not done or canceled
                    if (future != null
                            && (!(future.isCancelled() || future.isDone()))) {
                        future.cancel(true);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    logger.error("Exception", e);
                }
            }
            monitorTaskMap.clear();
        }

        // Interrupt the threads and shutdown the scheduler
        taskExecutorPool.shutdownNow();

        // Shutdown the Gearman client
        JobSubmitClientHelper.getInstance().shutdown();

        // Shutdown the Gearman worker
        TaskManagementWorkerPool.getInstance().shutdown();

        // Shutdown the Gearman server
        TaskManager.shutdown();

        // Shutdown the token manager
        TokenManager.getInstance().shutDown();

        // Cancel scheduled but not started task, and avoid new ones
        taskMonitorService.shutdown();

        // Wait for the running tasks
        try {
            taskMonitorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }

        // Interrupt the threads and shutdown the monitor
        taskMonitorService.shutdownNow();
        
        // Shutdown the datasift clients if workmode is not server only
        if(workMode != TaskManager.WorkMode.SERVER_ONLY ) {
            DatasiftUtils.shutdown();
        }

        logger.info("Stop SAP.XHNA.Data TaskFinder Service Done");
        // when using redis for cache, should uncomment this line
        // RedisUtil.destroyJedisPool();
        System.exit(0);
    }

    // This method keep here for test purpose
    public void cleanTaskExecute(ITaskExecutor t) {
        if (monitorTaskMap.remove(t) != null || t.isCleanTaskSkipped()) {
            decreaseTaskSize();
        }
    }

}
