package com.sap.cisp.xhna.data.executor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.common.DataCrawlerTestBase;
import com.sap.cisp.xhna.data.executor.googleplus.BaseGpCrawler;
import com.sap.cisp.xhna.data.executor.googleplus.GooglePlusArticleAccountExecutor;
import com.sap.cisp.xhna.data.executor.googleplus.GooglePlusArticleKeywordExecutor;
import com.sap.cisp.xhna.data.executor.googleplus.GooglePlusPostInfoExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TokenManager;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class GooglePlusExecutorTest extends DataCrawlerTestBase {
    private static Logger logger = LoggerFactory
            .getLogger(GooglePlusExecutorTest.class);

    public GooglePlusExecutorTest(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    public void testGPlusArticleAccountExecutor() throws Exception {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.HOUR, -1);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.HOUR, -10);
        String from = sdf.format(currentCal.getTime());
        params.put("task_key", "cnn");
        params.put("start_time", from.toString());
        params.put("end_time", to.toString());
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByAccount,
                        null, param);
        ctx.put("task", task);
        GooglePlusArticleAccountExecutor exe = new GooglePlusArticleAccountExecutor();
        List<String> result = null;

        try {
            result = exe.execute(ctx);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(result);
    }

    public void testGPlusArticleKeywordExecutor() {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("task_key", "Volcano");
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.MINUTE, -6);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.MINUTE, -10);
        String from = sdf.format(currentCal.getTime());
        params.put("start_time", from.toString());
        params.put("end_time", to.toString());
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByKeyword,
                        null, param);
        ctx.put("task", task);
        GooglePlusArticleKeywordExecutor exe = new GooglePlusArticleKeywordExecutor();
        exe.setTestFlagOn(true);
        List<String> result = null;
        try {
            result = exe.execute(ctx);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    public void testGPlusPostInfoExecutor() throws Exception {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.HOUR, -10);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.HOUR, -20);
        String from = sdf.format(currentCal.getTime());
        params.put("task_key", "bbcnews");
        params.put("start_time", from.toString());
        params.put("end_time", to.toString());
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByAccount,
                        "GooglePlus", param);
        ctx.put("task", task);
        GooglePlusPostInfoExecutor exe = new GooglePlusPostInfoExecutor();
        exe.setTask(task);
        exe.setTestFlagOn(true);
        List<String> result = null;
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService  taskExecutorPool = new TaskExecutorPool(10, 10, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),factory);
        try {
            // result = exe.execute(ctx);
            result = (List<String>) taskExecutorPool.submit(exe).get();
            logger.info("Post info -> {}", result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(result);
    }
    
    public void testGPlusCrawlProfileByUserName() throws Exception {
        String account = "sap";
        IToken token = TokenManager.getInstance().getToken(
                TokenType.GooglePlus);
        JSONObject jsonUserId = BaseGpCrawler
                .crawlProfileByUserName(token.getAccessToken(),
                        account);
        if (jsonUserId == null) {
            throw new DataCrawlException(
                    "Cannot get the  User Id Json Object in crawlActivitiesByUser.");
        }
        String userId = GooglePlusPostInfoExecutor.getUserIdByProfile(jsonUserId);
        logger.info("name: {} /userId: {}, Json {}", account, userId, jsonUserId);
    }

}
