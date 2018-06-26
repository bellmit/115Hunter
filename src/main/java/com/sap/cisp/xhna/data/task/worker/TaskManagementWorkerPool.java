package com.sap.cisp.xhna.data.task.worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import net.johnewart.gearman.client.NetworkGearmanWorkerPool;
import net.johnewart.gearman.common.events.WorkEvent;
import net.johnewart.gearman.common.interfaces.GearmanWorker;
import net.johnewart.gearman.net.Connection;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskManager;
import com.sap.cisp.xhna.data.common.cache.LRUCacheUtils;
import com.sap.cisp.xhna.data.common.index.LuceneUtils;
import com.sap.cisp.xhna.data.common.index.PostIdsHolder;
import com.sap.cisp.xhna.data.common.index.LuceneUtils.MediaIndexPath;
import com.sap.cisp.xhna.data.common.serializer.KryoUtils;
import com.sap.cisp.xhna.data.common.serializer.Serializer;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.TaskExecutorFactory;
import com.sap.cisp.xhna.data.finder.WorkerTaskFinder;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.worker.TaskManagementUtils.FunctionEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;
import com.sap.cisp.xhna.data.token.TokenManager;

/**
 * Obsolete class - error prone during distribution test
 * 
 * The worker polls jobs from a job server and execute the function. Note: This
 * gearman worker API is aligned with the gearman server version. And currently
 * is used embeddly with gearman server(singlton mode and worker-server mode).
 * Regarding the stand alone usage as worker-only. There is issue of packet
 * lost(NullPointerException). So it's not adopted for worker-only solution.
 * 
 */
public class TaskManagementWorkerPool implements IWorker {

    /** The host address of the job server */
    public static final String GEARMAN_HOST = ConfigInstance
            .getValue("Gearman_Host");

    /** The port number the job server is listening on */
    public static final int PORT = 4730;

    private ConcurrentHashMap<ITaskExecutor, Future<?>> monitorTaskMap = null;

    public static Logger logger = LoggerFactory
            .getLogger(TaskManagementWorkerPool.class);
    private static NetworkGearmanWorkerPool workerPool;
    private Serializer<PostIdsHolder> serializer = new KryoUtils.CustomSerializer<PostIdsHolder>(
            KryoUtils.PostIdsContainerTypeHandler);

    public static void main(String... args) {
    }

    public static TaskManagementWorkerPool getInstance() {
        return TaskManagementWorkerHolder.helper;
    }

    private static class TaskManagementWorkerHolder {
        public static TaskManagementWorkerPool helper = new TaskManagementWorkerPool();
    }

    private TaskManagementWorkerPool() {
        // private constructor
    }

    public TaskManagementWorkerPool init(
            ConcurrentHashMap<ITaskExecutor, Future<?>> monitorTaskMap) {
        this.monitorTaskMap = monitorTaskMap;
        int port = PORT;
        try {
            port = Integer.parseInt(ConfigInstance.getValue("Gearman_Port"));
        } catch (NumberFormatException e) {
            logger.error("Failed  to parse gearman port. Use default {}", PORT,
                    e);
        }
        logger.debug("==> Create worker pool on host {}, port {}",
                GEARMAN_HOST, port);
        workerPool = new NetworkGearmanWorkerPool.Builder().threads(5)
                .withConnection(new Connection(GEARMAN_HOST, port)).build();
        return this;
    }

    @Override
    public byte[] process(WorkEvent workEvent) {

            net.johnewart.gearman.common.Job job = workEvent.job;
            GearmanWorker worker = workEvent.worker;

            logger.debug(String.format("Processing job '%s'",
                    job.getFunctionName()));
            byte[] data = job.getData();
            byte[] result = new byte[0];
            try {
                result = dispatch(job.getFunctionName(), data);
                worker.sendStatus(job, 1, 1);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error("Error occured in processing job.", e);
            }
            return result;
        
    }

    @Override
    public byte[] dispatch(String function, byte[] data) throws Exception {
            FunctionEnum functionEnum = TaskManagementUtils
                    .getEnumByIndex(Integer.parseInt(function));
            logger.debug("Dispatch function : {}", functionEnum);
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
            case TEST_TASK:
                return test(data);
            default:
                break;
            }
            return new byte[0];
    }

    private byte[] addTask(byte[] data) {
       
            ITask task = (ITask) SerializationUtils.deserialize(data);
            boolean result = WorkerTaskFinder.getInstance().addTask(task);
            try {
                return SerializationUtils.serialize(result ? "Done" : "Failed");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error("Failed to add task.", e);
                return new byte[0];
            }
       
    }

    private byte[] getToken(byte[] data) {
            String type = (String) SerializationUtils.deserialize(data);
            TokenType tokenType = TokenManagementUtils.getTokenTypeByName(type);
            logger.debug("<-- Get token from server. type : data {}; type {}", type, tokenType);
            if (tokenType == null) {
                return null;
            }
            try {
                String tokenTag = TokenManager.getInstance().getTokenTag(
                        tokenType);
                byte[] result = SerializationUtils.serialize(tokenTag);
                logger.debug(
                        "--> Return token to worker: tokenTag {}, data size {}",
                        tokenTag, result.length);
                return result;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error("Failed to get token.", e);
                return null;
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
                    "--> Handle Release token from worker : tokenTag {}; statusCode {}; Has Exception {}",
                    tokenTag, statusCode, hasException);
            long delay = -1;
            if (tokenMap.get("delay") != null) {
                delay = ((Long) tokenMap.get("delay")).longValue();
            }
            if (delay > 0) {
                // Case: on worker-only, TwitterException with Error 429(Too
                // many
                // requests.
                // //java.io.NotSerializableException:
                // twitter4j.HttpResponseImpl
                TokenManager.getInstance().releaseToken(tokenTag, statusCode,
                        delay);
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
            MediaIndexPath mediaPath = LuceneUtils
                    .getMediaIndexPathByName(path);
            boolean isUnCached = true;
            try {
                isUnCached = LuceneUtils.checkIfUnCachedPostFromLocal(
                        mediaPath, id);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error("Failed to check uncached posts.", e);
            }
            return SerializationUtils.serialize(isUnCached);
        
    }

    private byte[] getUncachedSocialPosts(byte[] data) throws Exception {
      
            logger.debug("--> Received Data to go size {}", data.length);

            PostIdsHolder content = null;

            try {
                content = serializer.deserialize(data);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error("Failed to get post id list.", e);
                return null;
            }
            String path = content.getPath();
            List<String> idList = content.getIdList();
            logger.debug("--> Worker on Get uncached : path {}, id size {}",
                    path, idList.size());
            MediaIndexPath mediaPath = LuceneUtils
                    .getMediaIndexPathByName(path);
            List<String> idUncachedList = new ArrayList<String>();
            try {
                idUncachedList = LuceneUtils.getUnCachedPostIdsFromLocal(
                        mediaPath, idList);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error("Failed to check uncached posts.", e);
            }
            content.setIdList(idUncachedList);
            return serializer.serialize(content);
        
    }

    private byte[] checkTraditionalUrlCached(byte[] data) {
       
            String url = (String) SerializationUtils.deserialize(data);
            logger.debug(
                    "Check traditional URL {} cached or not on server side.",
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
                        (Map<String, String>) SerializationUtils
                                .deserialize(data));
                ITaskExecutor taskExecutor = TaskExecutorFactory.getInstance()
                        .getTaskExecutor(task);
                Future<?> future = monitorTaskMap.get(taskExecutor);
                // cancel only when task is not done or canceled
                if (future != null
                        && (!(future.isCancelled() || future.isDone()))) {
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
    public void run() {
        startWork();
    }

    public void startWork() {
        /*
         * Create a Gearman work pool
         */
        workerPool.registerCallback(
                TaskManagementUtils.FunctionEnum.FIND_SOCIAL_ACCOUNT_BY_KEYWORD
                        .toString(), this);
        workerPool.registerCallback(
                TaskManagementUtils.FunctionEnum.VERIFY_SOCIAL_ACCOUNT
                        .toString(), this);
        // Server-Only work mode, only register managment call backs, do not
        // handle data crawling tasks
        if (ConfigInstance.getCurrentWorkMode() != TaskManager.WorkMode.SERVER_ONLY) {
            workerPool.registerCallback(
                    TaskManagementUtils.FunctionEnum.ADD_TASK.toString(), this);
        }
        workerPool.registerCallback(
                TaskManagementUtils.FunctionEnum.CANCEL_MULTIPLE_TASK
                        .toString(), this);
        workerPool.registerCallback(
                TaskManagementUtils.FunctionEnum.CANCEL_SINGLE_TASK.toString(),
                this);
        // Only server register following functions call back:
        // - get token
        // - release token
        // - get uncached social posts
        // - check traditional post cached
        if (ConfigInstance.getCurrentWorkMode() != TaskManager.WorkMode.WORKER_ONLY) {
            workerPool.registerCallback(
                    TaskManagementUtils.FunctionEnum.GET_TOKEN_TAG.toString(),
                    this);
            workerPool.registerCallback(
                    TaskManagementUtils.FunctionEnum.RELEASE_TOKEN_TAG
                            .toString(), this);
            workerPool
                    .registerCallback(
                            TaskManagementUtils.FunctionEnum.CHECK_IF_UNCACHED_SOCIAL_POSTS
                                    .toString(), this);
            workerPool
                    .registerCallback(
                            TaskManagementUtils.FunctionEnum.CHECK_TRADITIONAL_POST_CACHED
                                    .toString(), this);
            workerPool.registerCallback(
                    TaskManagementUtils.FunctionEnum.GET_UNCACHED_SOCIAL_POSTS
                            .toString(), this);
        }
//        workerPool.registerCallback(
//                TaskManagementUtils.FunctionEnum.TEST_TASK.toString(), this);

        workerPool.doWork();
    }

    public void shutdown() {
        workerPool.stopWork();
    }
}
