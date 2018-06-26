package com.sap.cisp.xhna.data.executor.traditional;

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

public class TraditionalArticleRSSExecutor extends AbstractTaskExecutor {

    @SuppressWarnings("unchecked")
    @Override
    public List<String> execute(Map<String, Object> ctx) throws Exception {
        ctx.put("result", new ArrayList<String>());
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        logger.debug("RSS URL --> {}", rssUrl);

        try {
            ConfigInstance.TRADITIONAL_SEMAPHORE.acquire();
            RomeCrawler.RomeMediaEnum mediaEnum = RomeCrawler
                    .getMediaEnumByRss(rssUrl);
            logger.info("The Media enum is : {}", mediaEnum);
            switch (mediaEnum) {
            // JSoup extract
            case CNN:
                RomeCrawler.getCNNRSS(ctx, this);
                break;
            case NEWYORK_TIMES:
                RomeCrawler.getNewYorkTimesRSS(ctx, this);
                break;
            case FOX:
                RomeCrawler.getFoxRSS(ctx, this);
                break;
            case WASHINGTON_POST:
                RomeCrawler.getWashingtonPostRSS(ctx, this);
                break;
            case NBC:
                RomeCrawler.getNbcNewsRSS(ctx, this);
                break;
            case THE_TIMES:
                RomeCrawler.getTheTimesRSS(ctx, this);
                break;
            case GUARDIAN:
                RomeCrawler.getGuardianRSS(ctx, this);
                break;
            case AP:
                RomeCrawler.getAPRSS(ctx, this);
                break;
            case CHINA_DAILY:
                RomeCrawler.getChinadailyRSS(ctx, this);
                break;
            case BBC:
                RomeCrawler.getBBCRSS(ctx, this);
                break;
            case REUTERS:
                RomeCrawler.getReutersRSS(ctx, this);
                break;
            case UPI:
                RomeCrawler.getUPIRSS(ctx, this);
                break;
            default:
                // Common extract
                NewsSpider.getRss(ctx, this);
            }
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
                "http://www.chinadaily.com.cn/rss/cndy_rss.xml");
        params.put("task_key", "http://rss.cnn.com/rss/edition_asia.rss");
        MediaKey mediaKey = new MediaKey("Traditional", "TraditionalArticle",
                "WebPage1");
        TaskParam param = new TaskParam(params, mediaKey);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.TraditionalMedia_ArticleData_ByRSS,
                        "wsj", param);
        ctx.put("task", task);
        TraditionalArticleRSSExecutor exe = new TraditionalArticleRSSExecutor();
        exe.setTask(task);
        exe.setTestFlagOn(true);
        logger.debug("Line separator --> {}",
                System.getProperty("line.separator", "\r\n"));
        List<String> result = null;

        try {
            // result = exe.execute(ctx);
            // exe.execute(ctx);
//            result = (List<String>) taskExecutorPool.submit(exe).get();
//            logger.debug("Result --> {}", result);
//            logger.debug("Get Cache status : {}", LRUCacheConfig.getInstance()
//                    .getStatistics());
//            Thread.sleep(5000);
            for(int i = 0 ; i < 1; i++)
            taskExecutorPool.submit(exe);
            logger.debug("Get Cache status : {}", LRUCacheConfig.getInstance()
                    .getStatistics());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//         System.exit(0);
    }
}
