package com.sap.cisp.xhna.data.executor.forum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.sap.cisp.xhna.data.Main;
import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.cache.LRUCacheConfig;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class ForumThreadExecutor extends AbstractTaskExecutor {

    @SuppressWarnings("unchecked")
    @Override
    public List<String> execute(Map<String, Object> ctx) throws Exception {
        ctx.put("result", new ArrayList<String>());
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        logger.debug("Forum URL --> {}", rssUrl);

        try {
            ConfigInstance.TRADITIONAL_SEMAPHORE.acquire();
            SemiWikiForumCrawler exe = new SemiWikiForumCrawler();
            exe.getSemiWikiForums(ctx, this);
            
        } catch (Exception e) {
            logger.error("Caught Exception during crawling RSS article.", e);
            throw e;
        } finally {
            ConfigInstance.TRADITIONAL_SEMAPHORE.release();
        }

        return isCanceled ? null : (List<String>) ctx.get("result");
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        System.setProperty("http.proxyHost", "proxy.pal.sap.corp");
        System.setProperty("http.proxyPort", "8080");
        // Task barrier need to create big enough pool size...
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService taskExecutorPool = new TaskExecutorPool(1000, 1000, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                factory);
        Main.setTaskExecutorPool(taskExecutorPool);
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        // params.put("media_name", "boston");
        // params.put("rss", "http://feeds.boston.com/boston/news/nation");
        params.put("media_name", "wsj");
        params.put("task_key",
                "http://www.chinadaily.com.cn/rss/world_rss.xml");
//        params.put("task_key", "http://www.thetimes.co.uk/tto/news/world/rss");
        MediaKey mediaKey = new MediaKey("Traditional", "TraditionalArticle",
                "WebPage1");
        TaskParam param = new TaskParam(params, mediaKey);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.TraditionalMedia_ArticleData_ByRSS,
                        "wsj", param);
        ctx.put("task", task);
        ForumThreadExecutor exe = new ForumThreadExecutor();
        exe.setTask(task);
//        exe.setTestFlagOn(true);
        logger.debug("Line separator --> {}",
                System.getProperty("line.separator", "\r\n"));
        List<String> result = null;

        try {
            // result = exe.execute(ctx);
            // exe.execute(ctx);
            result = (List<String>) taskExecutorPool.submit(exe).get();
            logger.debug("Result --> {}", result);
            logger.debug("Get Cache status : {}", LRUCacheConfig.getInstance()
                    .getStatistics());
            Thread.sleep(5000);
            taskExecutorPool.submit(exe);
            logger.debug("Get Cache status : {}", LRUCacheConfig.getInstance()
                    .getStatistics());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // System.exit(0);
    }
}
