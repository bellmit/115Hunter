package com.sap.cisp.xhna.data.executor.traditional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.Main;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.common.DataCrawlerTestBase;
import com.sap.cisp.xhna.data.executor.FacebookExecutorTest;
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class TraditionalArticleRSSExecutorTest extends DataCrawlerTestBase {
    public static Logger logger = LoggerFactory
            .getLogger(FacebookExecutorTest.class);

    static {
        System.setProperty("http.proxyHost", "proxy.pal.sap.corp");
        System.setProperty("http.proxyPort", "8080");
    }

    public TraditionalArticleRSSExecutorTest(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @SuppressWarnings("unchecked")
    public void testCrawlByRss() {
        Map<String, String> caseslist = new HashMap<String, String>();
        caseslist.put("cnn", "http://rss.cnn.com/rss/edition_world.rss");
        caseslist.put("newyorktimes",
                "http://www.nytimes.com/services/xml/rss/nyt/HomePage.xml");
        caseslist.put("fox", "http://feeds.foxnews.com/foxnews/latest");
        caseslist.put("washingtonPost",
                "http://feeds.washingtonpost.com/rss/world");
        caseslist.put("nbcNews", "http://feeds.nbcnews.com/feeds/topstories");
        caseslist.put("theTimes",
                "http://www.thetimes.co.uk/tto/news/world/rss");
        caseslist.put("guardian", "http://www.theguardian.com/world/rss");
        caseslist
                .put("ap",
                        "http://hosted2.ap.org/atom/APDEFAULT/cae69a7523db45408eeb2b3a98c0c9c5");
        caseslist.put("chinaDaily",
                "http://www.chinadaily.com.cn/rss/china_rss.xml");

        for (Map.Entry<String, String> entry : caseslist.entrySet()) {
            Map<String, Object> ctx = new HashMap<String, Object>();
            Map<String, String> params = new HashMap<String, String>();

            params.put("media_name", "Traditional");
            params.put("task_key", entry.getValue());
            MediaKey mediaKey = new MediaKey("Traditional",
                    "TraditionalArticle", "WebPage1");
            TaskParam param = new TaskParam(params, mediaKey);
            ITask task = TaskFactory
                    .getInstance()
                    .createTask(
                            com.sap.cisp.xhna.data.TaskType.TraditionalMedia_ArticleData_ByRSS,
                            entry.getKey(), param);
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
                result = (List<String>) Main.getTaskExecutorPool().submit(exe).get();
                logger.debug("Result --> {}", result);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            assertNotNull(result);
        }
    }
}
