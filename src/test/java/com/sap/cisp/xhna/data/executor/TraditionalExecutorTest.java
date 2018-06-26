package com.sap.cisp.xhna.data.executor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.Main;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.common.DataCrawlerTestBase;
import com.sap.cisp.xhna.data.common.cache.ConcurrentLRUCache;
import com.sap.cisp.xhna.data.common.cache.LRUCacheConfig;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutorBuilder;
import com.sap.cisp.xhna.data.executor.traditional.RomeCrawler;
import com.sap.cisp.xhna.data.executor.traditional.TraditionalArticleRSSExecutor;
import com.sap.cisp.xhna.data.executor.traditional.TraditionalWebPageExecutor;
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class TraditionalExecutorTest extends DataCrawlerTestBase {
    public static Logger logger = LoggerFactory
            .getLogger(TraditionalExecutorTest.class);

    public TraditionalExecutorTest(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    @SuppressWarnings("unchecked")
    public void testArticleRSSExecutor() {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("media_name", "telegraph");
        params.put("task_key",
                " http://www.thestandard.com.hk/newsfeed/latest/news.xml");
        MediaKey mediaKey = new MediaKey("Traditional", "TraditionalArticle",
                "WebPage1");
        TaskParam param = new TaskParam(params, mediaKey);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.TraditionalMedia_ArticleData_ByRSS,
                        "telegraph", param);
        ctx.put("task", task);
        TraditionalArticleRSSExecutor exe = new TraditionalArticleRSSExecutor();
        exe.setTask(task);
        exe.setTestFlagOn(true);
        logger.debug("Line separator --> {}",
                System.getProperty("line.separator", "\r\n"));
        List<String> result = null;

        try {
            result = (List<String>) Main.getTaskExecutorPool().submit(exe)
                    .get();
            logger.debug("Result --> {}", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Task barrier sub-tasks have saved the result, parent executor do not
        // maintain result anymore
        assertEquals(0, result.size());
    }

    @SuppressWarnings("unchecked")
    /**
     * Test GetNews.java
     */
    public void testWebPageExecutorByGetNews() {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("task_key",
                "http://www.dpa-international.com/news/Features_Trends/");
        MediaKey mediaKey = new MediaKey("Traditional", "TraditionalArticle",
                "WebPage");
        TaskParam param = new TaskParam(params, mediaKey);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.TraditionalMedia_ArticleData_ByWebPage,
                        "dpa", param);
        ctx.put("task", task);
        TraditionalWebPageExecutor exe = new TraditionalWebPageExecutor();
        exe.setTask(task);
        exe.setTestFlagOn(true);
        List<String> result = null;
        try {
            result = (List<String>) Main.getTaskExecutorPool().submit(exe)
                    .get();
            logger.debug("Result --> {}", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Task barrier sub-tasks have saved the result, parent executor do not
        // maintain result anymore
        assertEquals(0, result.size());
    }

    @SuppressWarnings("unchecked")
    /**
     * Test NewsCrawler.java
     */
    public void testWebPageExecutorByNewsCrawler() {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("task_key",
                "http://www.dpa-international.com/news/international/");
        MediaKey mediaKey = new MediaKey("Traditional", "TraditionalArticle",
                "WebPage");
        TaskParam param = new TaskParam(params, mediaKey);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.TraditionalMedia_ArticleData_ByWebPage,
                        "dpa", param);
        ctx.put("task", task);
        TraditionalWebPageExecutor exe = new TraditionalWebPageExecutor();
        exe.setTask(task);
        exe.setTestFlagOn(true);
        List<String> result = null;
        try {
            result = (List<String>) Main.getTaskExecutorPool().submit(exe)
                    .get();
            logger.debug("Result --> {}", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Task barrier sub-tasks have saved the result, parent executor do not
        // maintain result anymore
        assertEquals(0, result.size());
    }

    @SuppressWarnings("rawtypes")
    public void testConcurrentLRUCache() throws Exception {
        int threads = 1000;
        LRUCacheConfig lru = LRUCacheConfig.getInstance();
        Map<String, String> params = new HashMap<>();
        params.put("size", String.valueOf(100));
        params.put("initialSize", "100");
        params.put("cleanupThread", "false");
        params.put("showItems", String.valueOf(2));
        final ConcurrentLRUCache map = (ConcurrentLRUCache) LRUCacheConfig
                .newInstance("map", params);
        map.setAlive(true);
        final CyclicBarrier barrier = new CyclicBarrier(threads);
        for (int i = 0; i < threads; i++) {
            new Thread() {
                @SuppressWarnings("unchecked")
                public void run() {
                    try {
                        for (int i = 0; i < 150; i++)
                            map.put(i, i);
                        for (int i = 0; i < 100; i++)
                            map.get(i);
                        barrier.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        logger.info(lru.getStatistics());
        assertEquals(true, map.size() < 150);

    }

    public void testRSSPatterns() throws Exception {
        for (String rss : RomeCrawler.cnnUrlList) {
            logger.info("Check {}, result {}", rss, RomeCrawler
                    .checkRssPatternMatches(rss, RomeCrawler.patternCNN));
            assertEquals(RomeCrawler.checkRssPatternMatches(rss,
                    RomeCrawler.patternCNN), true);
        }
        for (String rss : RomeCrawler.theTimesUrlList) {
            logger.info("Check {}, result {}", rss, RomeCrawler
                    .checkRssPatternMatches(rss, RomeCrawler.patternTheTimes));
            assertEquals(RomeCrawler.checkRssPatternMatches(rss,
                    RomeCrawler.patternTheTimes), true);
        }

        for (String rss : RomeCrawler.washingtonPostUrlList) {
            logger.info("Check {}, result {}", rss, RomeCrawler
                    .checkRssPatternMatches(rss, RomeCrawler.patternWP));
            assertEquals(RomeCrawler.checkRssPatternMatches(rss,
                    RomeCrawler.patternWP), true);
        }
        for (String rss : RomeCrawler.foxUrlList) {
            logger.info("Check {}, result {}", rss, RomeCrawler
                    .checkRssPatternMatches(rss, RomeCrawler.patternFOX));
            assertEquals(RomeCrawler.checkRssPatternMatches(rss,
                    RomeCrawler.patternFOX), true);
        }
        for (String rss : RomeCrawler.nbcNewsUrlList) {
            logger.info("Check {}, result {}", rss, RomeCrawler
                    .checkRssPatternMatches(rss, RomeCrawler.patternNBC));
            assertEquals(RomeCrawler.checkRssPatternMatches(rss,
                    RomeCrawler.patternNBC), true);
        }
        for (String rss : RomeCrawler.guardianUrlList) {
            logger.info("Check {}, result {}", rss, RomeCrawler
                    .checkRssPatternMatches(rss, RomeCrawler.patternGuardian));
            assertEquals(RomeCrawler.checkRssPatternMatches(rss,
                    RomeCrawler.patternGuardian), true);
        }
        for (String rss : RomeCrawler.chinaDailyUrlList) {
            logger.info("Check {}, result {}", rss, RomeCrawler
                    .checkRssPatternMatches(rss, RomeCrawler.patternCD));
            assertEquals(RomeCrawler.checkRssPatternMatches(rss,
                    RomeCrawler.patternCD), true);
        }
        for (String rss : RomeCrawler.apUrlList) {
            logger.info("Check {}, result {}", rss, RomeCrawler
                    .checkRssPatternMatches(rss, RomeCrawler.patternAP));
            assertEquals(RomeCrawler.checkRssPatternMatches(rss,
                    RomeCrawler.patternAP), true);
        }

        for (String rss : RomeCrawler.newyorkTimesUrlList) {
            logger.info("Check {}, result {}", rss, RomeCrawler
                    .checkRssPatternMatches(rss, RomeCrawler.patternNYT));
            assertEquals(RomeCrawler.checkRssPatternMatches(rss,
                    RomeCrawler.patternNYT), true);
        }

        assertEquals(
                RomeCrawler
                        .getMediaEnumByRss("http://hosted2.ap.org/atom/APDEFAULT/cae69a7523db45408eeb2b3a98c0c9c5"),
                RomeCrawler.RomeMediaEnum.AP);
        assertEquals(
                RomeCrawler
                        .getMediaEnumByRss("http://www.telegraph.co.uk/finance/rss"),
                RomeCrawler.RomeMediaEnum.OTHER);
        String testReplace = "hello world";
        testReplace.replace("\r", "\\r");
        logger.info("Test replace: {}", testReplace);
    }

    public void testJSoupCookie() throws IOException {
        Connection.Response res = Jsoup
                .connect("https://www.semiwiki.com/forum/login.php?do=login")
                .data("user Name", "likesemiwiki", "Password", "12345678")
                .method(Method.POST).execute();

        // Document doc = res.parse();
        Map<String, String> loginCookies = res.cookies();
        logger.info("res doc: {}", res.cookies());

        // __asc d55226f014fd999e9ea95d04a16
        Document doc = Jsoup
                .connect("https://www.semiwiki.com/forum/forum.php")
                .cookies(loginCookies)
                // .cookie("__asc", "d55226f014fd999e9ea95d04a16")
                .get();
        logger.info("Get the rss feed with cookie: {}", doc);
        assertNotNull(doc);
    }

    public void testJSoupGD() throws IOException {
//        Connection.Response res = Jsoup
//                .connect("http://www.gdlottery.cn/zst11xuan5.jspx?method=to11x5kjggzst&date=2009-11-11")
//                .method(Method.POST).execute();
//
//        // Document doc = res.parse();
//        Map<String, String> loginCookies = res.cookies();
//        logger.info("res doc: {}", res.cookies());

        // __asc d55226f014fd999e9ea95d04a16
        Document doc = Jsoup
                .connect("http://baidu.lecai.com/lottery/gd11x5/#shownow")
                // .cookie("__asc", "d55226f014fd999e9ea95d04a16")
                .get();


//        logger.info("Get the rss feed with cookie: {}", doc);
        assertNotNull(doc);
        Document doc1 = Jsoup
                .connect("http://www.gdlottery.cn/zst11xuan5.jspx?method=to11x5kjggzst&date=2009-11-11")
                // .cookie("__asc", "d55226f014fd999e9ea95d04a16")
                .get();
        logger.info("########################################\nGet the rss feed with cookie: {}", doc1);
        assertNotNull(doc1);

    }

    
    public void testChoppedSize() {
        int urlListSize = 2000000;
        int ACTUAL_CHOPPED_SIZE = TaskBarrierExecutorBuilder.computeChoppedSize(urlListSize);
        logger.info("Actual chopped size -> {}", ACTUAL_CHOPPED_SIZE);
        assertTrue(ACTUAL_CHOPPED_SIZE <= TaskBarrierExecutorBuilder.MAXIMUM_GROUP_SIZE);
        
        urlListSize = 60;
        ACTUAL_CHOPPED_SIZE = TaskBarrierExecutorBuilder.computeChoppedSize(urlListSize);
        logger.info("Actual chopped size -> {}", ACTUAL_CHOPPED_SIZE);
        assertTrue(ACTUAL_CHOPPED_SIZE <= TaskBarrierExecutorBuilder.MAXIMUM_GROUP_SIZE);
        
        urlListSize = 100;
        ACTUAL_CHOPPED_SIZE = TaskBarrierExecutorBuilder.computeChoppedSize(urlListSize);
        logger.info("Actual chopped size -> {}", ACTUAL_CHOPPED_SIZE);
        assertTrue(ACTUAL_CHOPPED_SIZE <= TaskBarrierExecutorBuilder.MAXIMUM_GROUP_SIZE);
        
        urlListSize = 400;
        ACTUAL_CHOPPED_SIZE = TaskBarrierExecutorBuilder.computeChoppedSize(urlListSize);
        logger.info("Actual chopped size -> {}", ACTUAL_CHOPPED_SIZE);
        assertTrue(ACTUAL_CHOPPED_SIZE <= TaskBarrierExecutorBuilder.MAXIMUM_GROUP_SIZE);
        
        urlListSize = 700;
        ACTUAL_CHOPPED_SIZE = TaskBarrierExecutorBuilder.computeChoppedSize(urlListSize);
        logger.info("Actual chopped size -> {}", ACTUAL_CHOPPED_SIZE);
        assertTrue(ACTUAL_CHOPPED_SIZE <= TaskBarrierExecutorBuilder.MAXIMUM_GROUP_SIZE);
        
        urlListSize = 1000;
        ACTUAL_CHOPPED_SIZE = TaskBarrierExecutorBuilder.computeChoppedSize(urlListSize);
        logger.info("Actual chopped size -> {}", ACTUAL_CHOPPED_SIZE);
        assertTrue(ACTUAL_CHOPPED_SIZE <= TaskBarrierExecutorBuilder.MAXIMUM_GROUP_SIZE);
        
        urlListSize = 2000;
        ACTUAL_CHOPPED_SIZE = TaskBarrierExecutorBuilder.computeChoppedSize(urlListSize);
        logger.info("Actual chopped size -> {}", ACTUAL_CHOPPED_SIZE);
        assertTrue(ACTUAL_CHOPPED_SIZE <= TaskBarrierExecutorBuilder.MAXIMUM_GROUP_SIZE);
        
        urlListSize = 100000;
        ACTUAL_CHOPPED_SIZE = TaskBarrierExecutorBuilder.computeChoppedSize(urlListSize);
        logger.info("Actual chopped size -> {}", ACTUAL_CHOPPED_SIZE);
        assertTrue(ACTUAL_CHOPPED_SIZE <= TaskBarrierExecutorBuilder.MAXIMUM_GROUP_SIZE);
    }
}
