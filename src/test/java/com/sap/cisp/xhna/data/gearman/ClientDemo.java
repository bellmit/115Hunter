package com.sap.cisp.xhna.data.gearman;

import net.johnewart.gearman.client.NetworkGearmanClient;
import net.johnewart.gearman.common.JobStatus;
import net.johnewart.gearman.common.events.GearmanClientEventListener;
import net.johnewart.gearman.constants.JobPriority;
import net.johnewart.gearman.exceptions.NoServersAvailableException;
import net.johnewart.gearman.exceptions.WorkException;
import net.johnewart.gearman.exceptions.WorkExceptionException;
import net.johnewart.gearman.exceptions.WorkFailException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.index.PostIdsHolder;
import com.sap.cisp.xhna.data.common.serializer.KryoUtils;
import com.sap.cisp.xhna.data.common.serializer.Serializer;
import com.sap.cisp.xhna.data.executor.stream.DatasiftStreamKeywordExecutor;
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.task.worker.JobSubmitClientHelper;
import com.sap.cisp.xhna.data.task.worker.JobSubmitClientHelperOfficialVersion;
import com.sap.cisp.xhna.data.task.worker.TaskManagementUtils;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TokenManagementUtils;
import com.sap.cisp.xhna.data.token.TokenManager;

public class ClientDemo {
    private ClientDemo() {
    }

    private final static Logger logger = LoggerFactory
            .getLogger(ClientDemo.class);

    public static void testWorkerToWorker() {
        for (int k = 0; k < 20000; k++) {
            byte[] jobReturn1 = null;
            try {
                jobReturn1 = JobSubmitClientHelperOfficialVersion
                        .getInstance()
                        .submitJob(
                                TaskManagementUtils.FunctionEnum.GET_TOKEN_TAG
                                        .toString(),
                                SerializationUtils
                                        .serialize(TokenManagementUtils.TokenType.Twitter
                                                .getName()));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            HashMap<String, Object> tokenMap = new HashMap<String, Object>();
            String tokenTag = (String) SerializationUtils
                    .deserialize(jobReturn1);
            IToken token = TokenManager.getInstance().getTokenmap()
                    .get(tokenTag);
            logger.debug("===> Test Get Token task {}: tokenTag {}, token {}",
                    k, SerializationUtils.deserialize(jobReturn1), token);
            tokenMap.put("0", tokenTag);
            tokenMap.put("1", 0);
            tokenMap.put("2", false);

            byte[] mapDataApa = SerializationUtils.serialize(tokenMap);
            logger.debug(
                    "===> Test Release Token task {} : with apache data size {}",
                    k, mapDataApa.length);
            try {
                jobReturn1 = JobSubmitClientHelperOfficialVersion
                        .getInstance().init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER)
                        .submitJob(
                                TaskManagementUtils.FunctionEnum.RELEASE_TOKEN_TAG
                                        .toString(),
                                SerializationUtils.serialize(tokenMap));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.debug("===> Test Release Token task remotely {}",
                    SerializationUtils.deserialize(jobReturn1));
        }
    }

    public static void testTask() throws InterruptedException {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("task_key", "Beckham");
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.DATE, 0);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.HOUR, -1);
        String from = sdf.format(currentCal.getTime());
        params.put("start_time", from.toString());
        params.put("end_time", to.toString());
        params.put("media_name", "Twitter");
        MediaKey mediaKey = new MediaKey("Twitter", "SocialArticle", "Keyword");
        TaskParam param = new TaskParam(params, mediaKey);

        Map<String, Object> ctx1 = new HashMap<String, Object>();
        Map<String, String> params1 = new HashMap<String, String>();
        params1.put("task_key", "cnn");
        Calendar currentCal1 = Calendar.getInstance();
        currentCal1.add(Calendar.DATE, 0);
        String to1 = sdf.format(currentCal1.getTime());
        currentCal1.add(Calendar.HOUR, -1);
        String from1 = sdf.format(currentCal1.getTime());
        params1.put("start_time", from1.toString());
        params1.put("end_time", to1.toString());
        params1.put("media_name", "Twitter");
        MediaKey mediaKey1 = new MediaKey("Twitter", "SocialArticle", "Account");
        TaskParam param1 = new TaskParam(params1, mediaKey1);
        
        Map<String, Object> ctx2 = new HashMap<String, Object>();
        Map<String, String> params2 = new HashMap<String, String>();
        
        Calendar currentCal2 = Calendar.getInstance();
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal2.add(Calendar.HOUR, -0);
        String to2 = sdf2.format(currentCal2.getTime());
        currentCal2.add(Calendar.DATE, -7);
        String from2 = sdf2.format(currentCal2.getTime());
        params2.put("start_time", from2);
        params2.put("end_time", to2);
        MediaKey mediaKey2 = new MediaKey("Tencentweibo", "SocialArticle", "Keyword");

        
        
        for (int j = 0; j < 1; j++) {
            ITask task = TaskFactory
                    .getInstance()
                    .createTask(
                            com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByKeyword,
                            "Twitter", param);
            ctx.put("task", task);
            
            ITask task1 = TaskFactory
                    .getInstance()
                    .createTask(
                            com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByAccount,
                            "Twitter", param1);
            ctx1.put("task", task1);
            
            params2.put("task_key", "中国");
//            params2.put("task_key", "zsouun");
            TaskParam param2 = new TaskParam(params2, mediaKey2);
            ITask task2 = TaskFactory
                    .getInstance()
                    .createTask(
                            com.sap.cisp.xhna.data.TaskType.SocialMedia_Datasift_ByKeyword,
                            "Tencentweibo", param2);
            ctx2.put("task", task2);
//            
//            JobSubmitClientHelperOfficialVersion.getInstance().init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER).submitJob(
//                    TaskManagementUtils.FunctionEnum.ADD_TASK
//                            .toString(), SerializationUtils
//                            .serialize(task));
            JobSubmitClientHelperOfficialVersion.getInstance().init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER).submitJob(
                    TaskManagementUtils.FunctionEnum.ADD_TASK
                            .toString(), SerializationUtils
                            .serialize(task1));
            JobSubmitClientHelperOfficialVersion.getInstance().init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER).submitJob(
                    TaskManagementUtils.FunctionEnum.ADD_TASK
                            .toString(), SerializationUtils
                            .serialize(task2));
        }
    }
    
    public static boolean cancelDatasiftKeywordTask(String interactionType, String interactionLang, String keyword) throws InterruptedException {
        String format = "interaction.type == \"%s\" and interaction.content contains_any [language(%s)] \"%s\"";
        String csdl = String.format(format, interactionType,
                interactionLang, keyword);
        logger.info("Cancel social interaction of csdl: {}", csdl);
        byte[] jobReturn = null;
        boolean result = false;
        jobReturn = JobSubmitClientHelperOfficialVersion.getInstance().init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER).submitJob(
                TaskManagementUtils.FunctionEnum.UNSUBSCRIBE_STREAM_CSDL
                        .toString(), SerializationUtils
                        .serialize(csdl));
        if (jobReturn != null && jobReturn.length != 0) {
            result = (boolean) SerializationUtils.deserialize(jobReturn);
        }
        return result;
    }
    
    public static boolean cancelDatasiftAccountTask(String interactionType, String account) throws InterruptedException {
        String format = "interaction.type == \"%s\" and tencentweibo.author.id contains_any \"%s\"";
        String csdl = String.format(format, interactionType, account);
        logger.info("Cancel social interaction of csdl: {}", csdl);
        byte[] jobReturn = null;
        boolean result = false;
        jobReturn = JobSubmitClientHelperOfficialVersion.getInstance().init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER).submitJob(
                TaskManagementUtils.FunctionEnum.UNSUBSCRIBE_STREAM_CSDL
                        .toString(), SerializationUtils
                        .serialize(csdl));
        if (jobReturn != null && jobReturn.length != 0) {
            result = (boolean) SerializationUtils.deserialize(jobReturn);
        }
        return result;
    }

    public static void main(String... args) throws WorkException,
            NoServersAvailableException {
        TokenManager.getInstance().init();
        GearmanClientEventListener eventListener = new GearmanClientEventListener() {
            @Override
            public void handleWorkData(String jobHandle, byte[] data) {
                System.err.println("Received data update for job " + jobHandle);
            }

            @Override
            public void handleWorkWarning(String jobHandle, byte[] warning) {
                System.err.println("Received warning for job " + jobHandle);
            }

            @Override
            public void handleWorkStatus(String jobHandle, JobStatus jobStatus) {
                System.err.println("Received status update for job "
                        + jobHandle);
                System.err.println("Status: " + jobStatus.getNumerator()
                        + " / " + jobStatus.getDenominator());
            }
        };

        try {
            byte data[] = SerializationUtils.serialize("This is a test");
            NetworkGearmanClient client = // null;
            new NetworkGearmanClient("localhost", 4730);
            // client.addHostToList("localhost", 4731);
            client.registerEventListener(eventListener);
            int i = 0;
            while (i < 0) {
                i++;
                try {
                    byte[] result = client.submitJob("99", data,
                            JobPriority.NORMAL);
                    System.err.println("==="
                            + SerializationUtils.deserialize(result));
                } catch (WorkException e) {
                    if (e instanceof WorkFailException)
                        System.err.println("Job " + e.getJobHandle()
                                + " failed.");
                    else
                        System.err.println("Job " + e.getJobHandle()
                                + " exception: "
                                + ((WorkExceptionException) e).getMessage());

                    e.printStackTrace();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            testTask();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

           String interactionType = "tencentweibo";
           String interactionLang = "zh";
           for(int n = 0; n < 1; n++) {
           String keyword = "中国";
           cancelDatasiftKeywordTask(interactionType, interactionLang, keyword);
//             String account = "zsouun";
//             cancelDatasiftAccountTask(interactionType, account);
           }
//            testWorkerToWorker();

            // test kryo
            Serializer<PostIdsHolder> serializer = new KryoUtils.CustomSerializer<PostIdsHolder>(
                    KryoUtils.PostIdsContainerTypeHandler);

            List<String> idList = new ArrayList<String>();
            // Strict test for the packet buffer underflow caused by gearman
            // Currently, each data to go size = 1800 bytes is suitbale
            for (int m = 0; m < 0; m++) {
                idList.clear();
                for (int k = 0; k < 1000; k++) {
                    // twitter id
                    // idList.add("625236407351521280");
                    // facebook id
                    // idList.add("5550296508_10153888099886509");
                    // g+ id
                    String id = "z13ls52zom2isjgor04cidmz3svdxpq5v0c34235gfeegrhrjtjwfwefdwq"
                            + k;
                    idList.add(id);
                    // youtube id
                    // idList.add("SR9Af22AVjw");
                }
                PostIdsHolder content = new PostIdsHolder();
                // content.setPath("./index/tweet_index");
                // content.setPath("./index/facebook_index");
                content.setPath("./index/gplus_index");
                // content.setPath("./index/youtube_index");
                content.setIdList(idList);
                byte[] dataToGo = serializer.serialize(content);
                logger.debug("Data to go size {} bytes", dataToGo.length);
                byte[] jobReturn = null;
                try {
                    jobReturn = JobSubmitClientHelper
                            .getInstance()
                            .submitJob(
                                    TaskManagementUtils.FunctionEnum.GET_UNCACHED_SOCIAL_POSTS
                                            .toString(), dataToGo);
                } catch (Exception | NoServersAvailableException
                        | WorkException e) {
                    logger.error("Check social post cached  remotely error.", e);
                    throw new Exception(e);
                }

                if (jobReturn != null && jobReturn.length != 0) {
                    PostIdsHolder toBeCachedContent = (PostIdsHolder) serializer
                            .deserialize(jobReturn);
                    logger.debug("--> To be cached Post id list size {}",
                            toBeCachedContent.getIdList().size());
                }
                PostIdsHolder newcontent = null;
                try {
                    newcontent = serializer.deserialize(dataToGo);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                logger.debug("---> id list deserialized:  {}, size {}",
                        newcontent.getPath(), newcontent.getIdList().size());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}