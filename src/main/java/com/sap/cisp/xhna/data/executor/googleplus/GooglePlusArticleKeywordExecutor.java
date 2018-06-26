package com.sap.cisp.xhna.data.executor.googleplus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.TokenManager;

public class GooglePlusArticleKeywordExecutor extends AbstractTaskExecutor {

    @Override
    public List<String> execute(Map<String, Object> ctx) throws Exception {
        KeywordCrawler kc = new KeywordCrawler(ctx, this);
        return kc.crawl(ctx);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        TokenManager.getInstance().init();
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("task_key", "China");
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.HOUR, -200);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.HOUR, -500);
        String from = sdf.format(currentCal.getTime());
        params.put("start_time", from);
        params.put("end_time", to);
        params.put("key", "AIzaSyAmx8zhVoRazSGSdXQ3eQJRTIaTNqOuwaE");
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByKeyword,
                        null, param);
        ctx.put("task", task);
        GooglePlusArticleKeywordExecutor exe = new GooglePlusArticleKeywordExecutor();
        exe.setTestFlagOn(true);
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService taskExecutorPool = new TaskExecutorPool(10, 10, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                factory);
        exe.setTask(task);
        try {
            List<String> result = (List<String>) taskExecutorPool.submit(
                    exe).get();
            logger.info("Crawl result =====> " + result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.exit(0);
    }
}
