package com.sap.cisp.xhna.data.executor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.common.DataCrawlerTestBase;
import com.sap.cisp.xhna.data.executor.youtube.YoutubeArticleAccountExecutor;
import com.sap.cisp.xhna.data.executor.youtube.YoutubeSocialAccountExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class YouTubeExecutorTest extends DataCrawlerTestBase {
    public static Logger logger = LoggerFactory
            .getLogger(YouTubeExecutorTest.class);

    public YouTubeExecutorTest(String name) {
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

    public void testYouTubeArticleAccountWithRightChannelName() {
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
        YoutubeArticleAccountExecutor exe = new YoutubeArticleAccountExecutor();
        List<String> result = null;
        try {
            result = exe.execute(ctx);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(result);
    }
    
    public void testYouTubeArticleAccountWithRightChannelID() {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.HOUR, -1);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.HOUR, -10);
        String from = sdf.format(currentCal.getTime());
        params.put("task_key", "UCupvZG-5ko_eiXAupbDfxWw");
        params.put("start_time", from.toString());
        params.put("end_time", to.toString());

        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByAccount,
                        null, param);
        ctx.put("task", task);
        YoutubeArticleAccountExecutor exe = new YoutubeArticleAccountExecutor();
        List<String> result = null;
        try {
            result = exe.execute(ctx);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(result);
    }

    public void testYouTubeArticleAccountWithWrongChannelName() {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.HOUR, -1);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.HOUR, -10);
        String from = sdf.format(currentCal.getTime());
        params.put("task_key", "cnn1");
        params.put("start_time", from.toString());
        params.put("end_time", to.toString());

        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByAccount,
                        null, param);
        ctx.put("task", task);
        YoutubeArticleAccountExecutor exe = new YoutubeArticleAccountExecutor();
        List<String> result = null;
        try {
            result = exe.execute(ctx);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNull(result);
    }

    public void testSocialAccountExecutor() {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();

        TaskParam param = new TaskParam(params, null);
        param.setString("task_key", "UCupvZG-5ko_eiXAupbDfxWw");
        ITask task = TaskFactory.getInstance().createTask(
                com.sap.cisp.xhna.data.TaskType.SocialMedia_AccountData, null,
                param);
        ctx.put("task", task);
        YoutubeSocialAccountExecutor exe = new YoutubeSocialAccountExecutor();
        List<String> result = null;
        try {
            result = exe.execute(ctx);
            assertNotNull(result);
            param.setString("task_key", "cnn");
            result = exe.execute(ctx);
            assertNotNull(result);
            param.setString("task_key", "cnn1111");
            result = exe.execute(ctx);
            assertNull(result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
