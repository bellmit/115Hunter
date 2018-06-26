package com.sap.cisp.xhna.data.task.worker.main;

import org.apache.commons.lang3.SerializationUtils;
import org.gearman.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.IApplicationContainer;
import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskManager;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.TaskManager.WorkMode;
import com.sap.cisp.xhna.data.common.cache.LRUCacheUtils;
import com.sap.cisp.xhna.data.common.index.LuceneUtils;
import com.sap.cisp.xhna.data.common.index.PostIdsHolder;
import com.sap.cisp.xhna.data.common.index.LuceneUtils.MediaIndexPath;
import com.sap.cisp.xhna.data.common.serializer.KryoUtils;
import com.sap.cisp.xhna.data.common.serializer.Serializer;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.TaskExecutorFactory;
import com.sap.cisp.xhna.data.executor.stream.DatasiftUtils;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.worker.JobSubmitClientHelper;
import com.sap.cisp.xhna.data.task.worker.TaskManagementUtils;
import com.sap.cisp.xhna.data.task.worker.TaskManagementUtils.FunctionEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils;
import com.sap.cisp.xhna.data.token.TokenManager;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * workers agent on host The user scenario: Worker is deployed on one host for
 * multiple functions purpose. The worker agent would be responsible for
 * register and dispatch those function to the real worker function.
 * 
 * This worker API is from the official java-gearman-service project. As a
 * supplement to fufill the stand alone usgae as worker-only mode. There is no
 * packet lost issue as if we handle the task directly with executor service,
 * can not work with blocking queue.
 * 
 */
public class TaskWorkerMain implements IWorkerAgent, IApplicationContainer {
    /** The host address of the job server */
    public static String GEARMAN_HOST = null;

    /** The port number the job server is listening on */
    public static final int PORT = 4730;
    /** create gearman object */
    public final Gearman gearman = Gearman.createGearman();
    /** create worker object */
    public final GearmanWorker worker = gearman.createGearmanWorker();
    private static final int defaultThreadNum = 100;
    public GearmanServer server = null; // create gearman server
    public final int SLEEP_TIME = 2 * 1000;
    private ExecutorService taskExecutorPool = null;
    private ConcurrentHashMap<ITaskExecutor, Future<?>> monitorTaskMap = new ConcurrentHashMap<ITaskExecutor, Future<?>>();
    private AtomicLong taskSize = new AtomicLong(0);

    public Logger logger = LoggerFactory.getLogger(TaskWorkerMain.class);
    private Serializer<PostIdsHolder> serializer = new KryoUtils.CustomSerializer<PostIdsHolder>(
            KryoUtils.PostIdsContainerTypeHandler);
    private WorkMode workMode = TaskManager.WorkMode.WORKER_ONLY;

    public static TaskWorkerMain getInstance() {
        return TaskWorkerAgentHolder.helper;
    }

    private static class TaskWorkerAgentHolder {
        public static TaskWorkerMain helper = new TaskWorkerMain();
    }

    private TaskWorkerMain() {
        init();
    }

    public TaskWorkerMain init(
            ConcurrentHashMap<ITaskExecutor, Future<?>> monitorTaskMap) {
        this.monitorTaskMap = monitorTaskMap;
        return this;
    }

    public void init() {
        try {
            int mode = Integer.parseInt(ConfigInstance.getValue("WorkMode"));
            workMode = TaskManager.getWorkModeByProperty(mode);
        } catch (NumberFormatException e) {
            logger.error("Failed to parse work mode {}. Use default {}",
                    ConfigInstance.getValue("WorkMode"), workMode, e);
        }

        GEARMAN_HOST = ConfigInstance.getValue("Gearman_Host");
        int port = PORT;
        try {
            port = Integer.parseInt(ConfigInstance.getValue("Gearman_Port"));
        } catch (NumberFormatException e) {
            logger.error("Failed  to parse gearman port {}. Use default {}",
                    ConfigInstance.getValue("Gearman_Port"), PORT, e);
        }
        logger.debug("Connect to Gearman server Host {}, Port {}",
                GEARMAN_HOST, port);
        server = gearman.createGearmanServer(GEARMAN_HOST, port);

        String threadNumStr = ConfigInstance.getValue("ThreadNum");
        logger.info("Thread num: {}", threadNumStr);
        int threadNum = defaultThreadNum;
        try {
            threadNum = Integer.parseInt(threadNumStr);
        } catch (NumberFormatException ne) {
            logger.error(
                    "Invalid thread Num in properties file {}, use default {}.",
                    threadNumStr, threadNum);
        }
        ThreadFactory factory = new TaskThreadFactory();
        taskExecutorPool = new TaskExecutorPool(threadNum, threadNum, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(
                        threadNum / 10), factory);
        // Strategy when executor pool reach maximumPoolSize, currently use
        // AbortPolicy
        ((ThreadPoolExecutor) taskExecutorPool)
                .setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    }

    public void increaseTaskSize() {
        logger.debug("Increase Task Size ==> {}", taskSize.incrementAndGet());
    }

    public void decreaseTaskSize() {
        logger.debug("Decrease Task Size ==> {}", taskSize.decrementAndGet());
    }

    public long getTaskSize() {
        return taskSize.longValue();
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public ExecutorService getTaskExecutorPool() {
        return taskExecutorPool;
    }

    @SuppressWarnings("unchecked")
    public void addTask(ITaskExecutor taskExecutor) {
        logger.debug("add task {} to executor pool.", taskExecutor.getTask()
                .toString());
        taskExecutor.setTaskContainer(this);
        logger.info(
                "***** Check the thread pool attributes : Maximum pool size -> {}, Largest pool size -> {}, Current pool size -> {}, Active thread count -> {}, Task count -> {}, getTaskSize -> {}, completedTaskCount -> {}",
                ((ThreadPoolExecutor) taskExecutorPool).getMaximumPoolSize(),
                ((ThreadPoolExecutor) taskExecutorPool).getLargestPoolSize(),
                ((ThreadPoolExecutor) taskExecutorPool).getPoolSize(),
                ((ThreadPoolExecutor) taskExecutorPool).getActiveCount(),
                ((ThreadPoolExecutor) taskExecutorPool).getTaskCount(),
                getTaskSize(),
                ((ThreadPoolExecutor) taskExecutorPool).getCompletedTaskCount());
        if (getTaskSize() > ConfigInstance.getThreadPoolSizeThreshold()) {
            taskExecutor.getTask().resetTaskStatus();
            logger.error("!!! Discard the new task submission due to maxmiumPoolSize has been exceeded. !!!");
            /*
             * Try to give up CPU for a while for executor service running in
             * case when there are tasks flooding and exceed maximum size
             */
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }

        increaseTaskSize();
        // After test, the task executor can not be put into a Blocking queue
        // here, otherwise gearman will send WORK_FAIL
        try {
            Future<?> future = taskExecutorPool.submit(taskExecutor);
            if (!taskExecutor.isCleanTaskSkipped()) {
                monitorTaskMap.put(taskExecutor, future);
            }
        } catch (RejectedExecutionException re) {
            taskExecutor.getTask().resetTaskStatus();
            logger.error(
                    "!!! The taskWorkerMain maximumPoolSize has been reached. Reject the new task submission by pool. !!!",
                    re);
        }
        /*
         * Try to give up CPU for a while for executor service running in case
         * when there are tasks flooding and exceed maximum size
         */
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public byte[] dispatch(String function, byte[] data,
            GearmanFunctionCallback callback) throws Exception {
        logger.debug("*** Dispatch function : {}", function);
        //The functions registered as IP@PID
        if(function.equalsIgnoreCase(DatasiftUtils.getWorkerIdentifier())) {
            return parseWorkerSpecifiedJob(data);
        }
        FunctionEnum functionEnum = TaskManagementUtils.getEnumByIndex(Integer
                .parseInt(function));
        switch (functionEnum) {
        case FIND_SOCIAL_ACCOUNT_BY_KEYWORD:
        case VERIFY_SOCIAL_ACCOUNT:
            break;
        case CANCEL_SINGLE_TASK:
            return cancelSingleTask(data);
        case CANCEL_MULTIPLE_TASK:
            break;
        case ADD_TASK:
            // Add task executor to local queue
            return addTask(data);
        case GET_TOKEN_TAG:
            return getToken(data);
        case RELEASE_TOKEN_TAG:
            return releaseToken(data);
        case CHECK_IF_UNCACHED_SOCIAL_POSTS:
            return checkIfUnCachedSoicalPost(data);
        case CHECK_TRADITIONAL_POST_CACHED:
            return checkTraditionalUrlCached(data);
        case ADD_TRADITIONAL_POST_TO_CACHE:
            return addTraditionalUrlToCache(data);
        case GET_UNCACHED_SOCIAL_POSTS:
            return getUncachedSocialPosts(data);
        case REGISTER_WORKER_IDENTIFIER:
            return registerDatasiftClientWithWorkerIdentifier(data);
        case DEREGISTER_WORKER_IDENTIFIER:
            return deregisterDatasiftClientWithWorkerIdentifier(data);
        case UNSUBSCRIBE_STREAM_CSDL:
            return unsubscribeStreamCSDL(data);
        case REGISTER_CSDL_WITH_WORKER_IDENTIFIER:
            return registerCsdlWithWorkerIdentifier(data);
        case TEST_TASK:
            return test(data);
        default:          
            break;
        }
        return SerializationUtils.serialize("");
    }

    @SuppressWarnings("unchecked")
    private byte[] parseWorkerSpecifiedJob(byte[] data) {
        logger.debug("==> Worker {} received the specific function request.", DatasiftUtils.getWorkerIdentifier());
        boolean result = false;
        HashMap<String, String> workerFunctionMap = (HashMap<String, String>)SerializationUtils.deserialize(data);
        String functionName = workerFunctionMap.get("0");
        String parameter = workerFunctionMap.get("1");
        if(functionName.equalsIgnoreCase(WorkerSpecificFunctionEnum.UNSUBSCRIBE_STREAM_CSDL.toString())) {
            logger.debug("<-- Release subscription from worker. csdl: {}", parameter);
            //Here may cause gearman timeout?
            result = DatasiftUtils.releaseSubscriptionFromLocal(parameter);
        }
        return SerializationUtils.serialize(result);
    }

    @SuppressWarnings("unchecked")
    private byte[] registerCsdlWithWorkerIdentifier(byte[] data) {
        boolean isCsdlRegistrationOk = false;
        HashMap<String, String> csdlMap = (HashMap<String, String>)SerializationUtils.deserialize(data);
        String csdl = csdlMap.get("0");
        String workerIdentifier = csdlMap.get("1");
        isCsdlRegistrationOk = DatasiftUtils.registerCsdlOnServer(csdl, workerIdentifier);
        logger.debug("Register csdl {} with worker {}, result -> {}", csdl, workerIdentifier, isCsdlRegistrationOk?"successfully":"failed. Maybe it has been already running.");
        return SerializationUtils.serialize(isCsdlRegistrationOk);
    }

    private byte[] deregisterDatasiftClientWithWorkerIdentifier(byte[] data) {
        boolean isDeregistrationOk = false;
        String workerIdentifer = (String)SerializationUtils.deserialize(data);
        isDeregistrationOk = DatasiftUtils.deregisterWorkerIdentifierOnServer(workerIdentifer);
        logger.debug("Deregister the datsift client with worker {}, result: {}.", workerIdentifer, isDeregistrationOk);
        return SerializationUtils.serialize(isDeregistrationOk);
    }

    private byte[] registerDatasiftClientWithWorkerIdentifier(byte[] data) {
        boolean isRegistrationOk = false;
        String workerIdentifer = (String)SerializationUtils.deserialize(data);
        isRegistrationOk = DatasiftUtils.registerWorkerIdentifierOnServer(workerIdentifer);
        logger.debug("Register the datsift client with worker {}, result: {}.", workerIdentifer, isRegistrationOk);
        return SerializationUtils.serialize(isRegistrationOk);
    }

    private byte[] addTask(byte[] data) {
        ITask task = (ITask) SerializationUtils.deserialize(data);
        ITaskExecutor taskExecutor = TaskExecutorFactory.getInstance()
                .getTaskExecutor(task);
        if (taskExecutor != null) {
            addTask(taskExecutor);
        }
        try {
            return SerializationUtils.serialize("Done");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("Failed to add task.", e);
            return SerializationUtils.serialize("Failed");
        }

    }

    private byte[] getToken(byte[] data) {
        String type = (String) SerializationUtils.deserialize(data);
        TokenType tokenType = TokenManagementUtils.getTokenTypeByName(type);
        logger.debug("Get token type : data {}; type {}", type, tokenType);
        // Avoid EOF exception caused by return new byte[0]
        byte[] result = SerializationUtils.serialize("");
        if (tokenType == null) {
            return result;
        }
        try {
            String tokenTag = TokenManager.getInstance().getTokenTag(tokenType);

            if (tokenTag != null) {
                result = SerializationUtils.serialize(tokenTag);
            }
            logger.debug(
                    "--> Return token to worker: tokenTag {}, data size {}",
                    tokenTag, result.length);
            return result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("Failed to get token.", e);
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    private byte[] releaseToken(byte[] data) {
        HashMap<String, Object> tokenMap = (HashMap<String, Object>) SerializationUtils
                .deserialize(data);
        String tokenTag = (String) tokenMap.get("0");
        int statusCode = ((Integer) tokenMap.get("1")).intValue();
        boolean hasException = ((Boolean) tokenMap.get("2")).booleanValue();
        logger.debug(
                "--> Release token by worker: tokenTag {}; statusCode {}; Has Exception {}",
                tokenTag, statusCode, hasException);
        long delay = -1;
        // get delay if any
        if (tokenMap.get("3") != null) {
            // In some case delay is < 0, i.e. twitter secondsUntilReset=-633
            delay = Math.abs(((Long) tokenMap.get("3")).longValue());
        }
        if (delay > 0) {
            // Case: on worker-only, TwitterException with Error 429(Too
            // many
            // requests.
            // //java.io.NotSerializableException:
            // twitter4j.HttpResponseImpl
            logger.debug(
                    "--> Release token with delay by worker: tokenTag {}; statusCode {}; delay {} ms",
                    tokenTag, statusCode, delay);
            TokenManager.getInstance()
                    .releaseToken(tokenTag, statusCode, delay);
        } else {
            TokenManager.getInstance().releaseToken(tokenTag, statusCode,
                    hasException);
        }
        return SerializationUtils.serialize("Done");
    }

    @SuppressWarnings("unchecked")
    private byte[] checkIfUnCachedSoicalPost(byte[] data) {
        HashMap<String, String> resultMap = (HashMap<String, String>) SerializationUtils
                .deserialize(data);
        String path = (String) resultMap.get("path");
        String id = (String) resultMap.get("id");
        logger.debug("Get uncached : path {} id {}", path, id);
        MediaIndexPath mediaPath = LuceneUtils.getMediaIndexPathByName(path);
        boolean isUnCached = true;
        try {
            isUnCached = LuceneUtils
                    .checkIfUnCachedPostFromLocal(mediaPath, id);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("Failed to check uncached posts.", e);
        }
        return SerializationUtils.serialize(isUnCached);
    }

    private byte[] getUncachedSocialPosts(byte[] data) throws Exception {
        logger.debug("--> Received remote Data to go size {}", data.length);

        // Use a empty object instead of null to avoid serialization error
        PostIdsHolder content = new PostIdsHolder("", new ArrayList<String>());

        try {
            content = serializer.deserialize(data);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("Failed to get post id list.", e);
            return serializer.serialize(content);
        }
        String path = content.getPath();
        List<String> idList = content.getIdList();
        logger.debug("--> Worker on Get uncached : path {}, id size {}", path,
                idList.size());
        MediaIndexPath mediaPath = LuceneUtils.getMediaIndexPathByName(path);
        List<String> idUncachedList = new ArrayList<String>();
        try {
            idUncachedList = LuceneUtils.getUnCachedPostIdsFromLocal(mediaPath,
                    idList);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("Failed to check uncached posts.", e);
        }
        content.setIdList(idUncachedList);
        return serializer.serialize(content);
    }

    private byte[] unsubscribeStreamCSDL(byte[] data) {
        String csdl = (String) SerializationUtils.deserialize(data);
        logger.debug("--> Receive the csdl to unsubscribe on worker side: {}", csdl);
        boolean result = DatasiftUtils.unsubscribeStreamWithCSDL(csdl);
        return SerializationUtils.serialize(result);
    }

    private byte[] checkTraditionalUrlCached(byte[] data) {
        String url = (String) SerializationUtils.deserialize(data);
        logger.debug("Check traditional URL {} cached or not on server side.",
                url);
        boolean result = LRUCacheUtils.checkIfTraditionalUrlCached(url);
        return SerializationUtils.serialize(result);
    }

    private byte[] addTraditionalUrlToCache(byte[] data) {
        String url = (String) SerializationUtils.deserialize(data);
        logger.debug("Add traditional URL {} to cache on server side.", url);
        LRUCacheUtils.addTraditionalUrlToCache(url);
        return SerializationUtils.serialize(true);
    }

    @SuppressWarnings("unchecked")
    private byte[] cancelSingleTask(byte[] data) {
        StringBuffer result = new StringBuffer("");
        try {
            ITask task = TaskFactory.getInstance().createTaskFromMap(
                    (Map<String, String>) SerializationUtils.deserialize(data));
            ITaskExecutor taskExecutor = TaskExecutorFactory.getInstance()
                    .getTaskExecutor(task);
            Future<?> future = monitorTaskMap.get(taskExecutor);
            // cancel only when task is not done or canceled
            if (future != null && (!(future.isCancelled() || future.isDone()))) {
                result.append(future.cancel(true));
            } else
                result.append(false);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("Exception", e);
            throw e;
        }
        return result.toString().getBytes();
    }

    private byte[] test(byte[] data) throws Exception {
        /*
         * The work method performs the gearman function. In this case, the echo
         * function simply returns the data it received
         */
        logger.debug("--> Test data {}", SerializationUtils.deserialize(data));
        return data;
    }

    @Override
    // Worker do the real job here!
    public byte[] work(String function, byte[] data,
            GearmanFunctionCallback callback) throws Exception {
        return dispatch(function, data, callback);
    }

    public static void main(String[] args) {
        final TaskWorkerMain agent = TaskWorkerMain.getInstance();
        agent.logger.info("************* Work mode : {} ***************",
                agent.workMode);
        if (agent.getWorkMode() != TaskManager.WorkMode.WORKER_ONLY) {
            agent.logger
                    .error("This service can ONLY work in worker_only mode.");
            System.exit(-1);
        }
        // Add JVM shutdownhook
        // Invoked when receive signal: SIGTERM(15) SIGHUP(1) SIGINT(2)
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                agent.logger.error("Shutdown hook called");
                agent.stop();
            }
        });

        agent.worker.setReconnectPeriod(2, TimeUnit.SECONDS); // reconnection
                                                              // time
        agent.worker.setMaximumConcurrency(5); // Maximum concurrency

        /* Register the worker functions */
        agent.worker.addFunction(
                TaskManagementUtils.FunctionEnum.ADD_TASK.toString(), agent);
        agent.worker.addFunction(
                TaskManagementUtils.FunctionEnum.TEST_TASK.toString(), agent);
        //Register a function named with worker identifier, this can be a common interface for distributed functions
        agent.worker.addFunction(
                DatasiftUtils.getWorkerIdentifier(), agent);
        //Add the gearman server
        agent.worker.addServer(agent.server);
        // initialize token manager in advance, in case there are bunch of tasks
        // starting with
        // very short token period.
        TokenManager.getInstance().init();
    }

    @Override
    public void run() {
        worker.setReconnectPeriod(2, TimeUnit.SECONDS); // reconnection time
        worker.setMaximumConcurrency(5); // Maximum concurrency

        /* Register the worker functions */
        if (workMode != TaskManager.WorkMode.WORKER_ONLY) {
            worker.addFunction(
                    TaskManagementUtils.FunctionEnum.GET_TOKEN_TAG.toString(),
                    this);
            worker.addFunction(
                    TaskManagementUtils.FunctionEnum.RELEASE_TOKEN_TAG
                            .toString(), this);
            worker.addFunction(
                    TaskManagementUtils.FunctionEnum.CHECK_TRADITIONAL_POST_CACHED
                            .toString(), this);
            worker.addFunction(
                    TaskManagementUtils.FunctionEnum.GET_UNCACHED_SOCIAL_POSTS
                            .toString(), this);
            //Following functions are declared on server side for distributed datasift clients
            worker.addFunction(
                    TaskManagementUtils.FunctionEnum.REGISTER_WORKER_IDENTIFIER.toString(), this);
            worker.addFunction(
                    TaskManagementUtils.FunctionEnum.DEREGISTER_WORKER_IDENTIFIER.toString(), this); 
            worker.addFunction(
                    TaskManagementUtils.FunctionEnum.REGISTER_CSDL_WITH_WORKER_IDENTIFIER.toString(), this);  
            worker.addFunction(
                    TaskManagementUtils.FunctionEnum.UNSUBSCRIBE_STREAM_CSDL.toString(), this);
        }
        if (workMode != TaskManager.WorkMode.SERVER_ONLY) {
            worker.addFunction(
                    TaskManagementUtils.FunctionEnum.ADD_TASK.toString(), this);
            worker.addFunction(
                    TaskManagementUtils.FunctionEnum.TEST_TASK.toString(), this);
            //Register a function named with worker identifier, this can be a common interface for distributed functions
            worker.addFunction(
                    DatasiftUtils.getWorkerIdentifier(), this);
        }
        worker.addServer(server);
    }

    public void stop() {

        logger.info("Stop SAP.XHNA.Data TaskWorker Service");

        // Cancel scheduled but not started task, and avoid new ones
        taskExecutorPool.shutdown();

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

        // Shutdown the Gearman worker/server
        worker.shutdown();

        server.shutdown();

        gearman.shutdown();

        // Shutdown the token manager
        TokenManager.getInstance().shutDown();
        
        // Shutdown datasift clients
        DatasiftUtils.shutdown();

        logger.info("Stop SAP.XHNA.Data TaskWorker Service Done");

        System.exit(0);
    }

    @Override
    public void cleanTaskExecute(ITaskExecutor t) {
        if (monitorTaskMap.remove(t) != null || t.isCleanTaskSkipped()) {
            decreaseTaskSize();
        }
    }

    @Override
    public ConcurrentHashMap<ITaskExecutor, Future<?>> getMonitorTaskMap() {
        return monitorTaskMap;
    }
    
    public static enum WorkerSpecificFunctionEnum {
        UNSUBSCRIBE_STREAM_CSDL(0), TEST_TASK(99);

        private final int index;

        private WorkerSpecificFunctionEnum(int index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return Integer.toString(index);
        }

        public int getInt() {
            return index;
        }
    }
}