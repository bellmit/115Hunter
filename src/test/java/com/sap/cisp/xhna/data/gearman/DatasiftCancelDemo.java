package com.sap.cisp.xhna.data.gearman;

import net.johnewart.gearman.exceptions.NoServersAvailableException;
import net.johnewart.gearman.exceptions.WorkException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.task.worker.JobSubmitClientHelperOfficialVersion;
import com.sap.cisp.xhna.data.task.worker.TaskManagementUtils;

public class DatasiftCancelDemo {
    private DatasiftCancelDemo() {
    }

    private final static Logger logger = LoggerFactory
            .getLogger(DatasiftCancelDemo.class);

    /**
     * This is a test method to submit datasift task to gearman server.
     * 
     * @throws InterruptedException
     */
    public static void testTask() throws InterruptedException {
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
        MediaKey mediaKey2 = new MediaKey("Tencentweibo", "SocialArticle",
                "Keyword");

        for (int j = 0; j < 1; j++) {
            params2.put("task_key", "中国");
            // params2.put("task_key", "zsouun");
            TaskParam param2 = new TaskParam(params2, mediaKey2);
            ITask task2 = TaskFactory
                    .getInstance()
                    .createTask(
                            com.sap.cisp.xhna.data.TaskType.SocialMedia_Datasift_ByKeyword,
                            "Tencentweibo", param2);
            ctx2.put("task", task2);
            JobSubmitClientHelperOfficialVersion
                    .getInstance()
                    .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER)
                    .submitJob(
                            TaskManagementUtils.FunctionEnum.ADD_TASK
                                    .toString(),
                            SerializationUtils.serialize(task2));
        }
    }

    /**
     * Method to cancel datasift stream task which is crawling by keyword
     * @param interactionType
     * @param interactionLang
     * @param keyword
     * @return true/false
     * @throws InterruptedException
     */
    public static boolean cancelDatasiftKeywordTask(String interactionType,
            String interactionLang, String keyword) throws InterruptedException {
        String format = "interaction.type == \"%s\" and interaction.content contains_any [language(%s)] \"%s\"";
        String csdl = String.format(format, interactionType, interactionLang,
                keyword);
        logger.info("Cancel social interaction of csdl: {}", csdl);
        byte[] jobReturn = null;
        boolean result = false;
        jobReturn = JobSubmitClientHelperOfficialVersion
                .getInstance()
                .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER)
                .submitJob(
                        TaskManagementUtils.FunctionEnum.UNSUBSCRIBE_STREAM_CSDL
                                .toString(), SerializationUtils.serialize(csdl));
        if (jobReturn != null && jobReturn.length != 0) {
            result = (boolean) SerializationUtils.deserialize(jobReturn);
        }
        return result;
    }

    /**
     * Method to cancel datasift stream task which is crawling by account.
     * @param interactionType
     * @param account
     * @return true/false
     * @throws InterruptedException
     */
    public static boolean cancelDatasiftAccountTask(String interactionType,
            String account) throws InterruptedException {
        String format = "interaction.type == \"%s\" and tencentweibo.author.id contains_any \"%s\"";
        String csdl = String.format(format, interactionType, account);
        logger.info("Cancel social interaction of csdl: {}", csdl);
        byte[] jobReturn = null;
        boolean result = false;
        jobReturn = JobSubmitClientHelperOfficialVersion
                .getInstance()
                .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER)
                .submitJob(
                        TaskManagementUtils.FunctionEnum.UNSUBSCRIBE_STREAM_CSDL
                                .toString(), SerializationUtils.serialize(csdl));
        if (jobReturn != null && jobReturn.length != 0) {
            result = (boolean) SerializationUtils.deserialize(jobReturn);
        }
        return result;
    }

    public static void main(String... args) throws WorkException,
            NoServersAvailableException, InterruptedException {
        testTask();
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String interactionType = "tencentweibo";
        String interactionLang = "zh";
        for (int n = 0; n < 1; n++) {
            String keyword = "中国";
            cancelDatasiftKeywordTask(interactionType, interactionLang, keyword);
            // String account = "zsouun";
            // cancelDatasiftAccountTask(interactionType, account);
        }
    }
}