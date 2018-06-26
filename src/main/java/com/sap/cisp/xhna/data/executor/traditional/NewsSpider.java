package com.sap.cisp.xhna.data.executor.traditional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.Main;
import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.cache.LRUCacheConfig;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutorBuilder;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sun.syndication.feed.synd.SyndEntry;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

@SuppressWarnings("rawtypes")
public class NewsSpider extends TaskBarrierExecutor {
    private Map<String, Object> ctx;
    private static final String className = "NewsSpider";
    private static Logger logger = LoggerFactory.getLogger(NewsSpider.class);

    public NewsSpider(Map<String, Object> ctx) {
        super();
        this.ctx = ctx;
        this.ctx.put("result", new ArrayList<String>());
    }

    public NewsSpider(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, String url, SyndEntry entry,
            ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, url, entry, parentExecutor);
    }

    public NewsSpider(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, String url, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, url, parentExecutor);
    }

    public NewsSpider(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, List<Map.Entry<String, SyndEntry>> entryList, boolean isEntryNeeded, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, entryList, isEntryNeeded, parentExecutor);
    }

    @SuppressWarnings("unchecked")
    public static void main(String... args) throws Exception {
        ConfigInstance.getValue("crawl.useProxy");
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("media_name", "telegraph");
        params.put("rss", "http://www.telegraph.co.uk/finance/rss");
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.TraditionalMedia_ArticleData_ByRSS,
                        "telegraph", param);
        ctx.put("task", task);
        TraditionalArticleRSSExecutor exe = new TraditionalArticleRSSExecutor();
        exe.setTask(task);
        exe.setTestFlagOn(true);
        List<String> result = null;
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService taskExecutorPool = new TaskExecutorPool(1000, 1000, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                factory);
        Main.setTaskExecutorPool(taskExecutorPool);
        try {
            result = (List<String>) taskExecutorPool.submit(exe).get();
            logger.debug("Result --> {}", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug(LRUCacheConfig.getInstance().toString());
    }

    public static void getRss(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        Map<String, SyndEntry> urlMap = new HashMap<String, SyndEntry>();
        createEntryMap(urlList, urlMap, parentExecutor);
        ctx.put("methodName", "extractContent");
        ctx.put("parameterTypes", new Class[] { String.class, SyndEntry.class,
                Map.class, ITaskExecutor.class });
        ctx.put("className", className);
        // urlMap size = 0 will cause CycliBarrier
        // java.lang.IllegalArgumentException
        if (parentExecutor != null && !parentExecutor.isCanceled()
                && !urlMap.isEmpty()) {
            try {
                CyclicBarrier parentBarrier = new CyclicBarrier(2);
                TaskBarrierExecutorBuilder.getInstance()
                        .buildWebPageTaskBarrier(urlMap, ctx, parentExecutor,
                                parentBarrier);
                parentBarrier.await();
            } catch (Exception e) {
                logger.error("Caught Exception during crawling RSS.", e);
                throw e;
            }
        }
    }

    public String extractContent(String url, SyndEntry entry,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws Exception {
        StringBuffer html = new StringBuffer();
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String siteName = params.getMediaName();
        BufferedReader in = null;
        try {
            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36");
            conn.addRequestProperty("Referer", "google.com");

            logger.debug("Request URL ... {}", url);

            boolean redirect = false;

            // normally, 3xx is redirect
            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

            logger.debug("Response Code ... {}", status);

            if (redirect) {

                // get redirect url from "location" header field
                String newUrl = conn.getHeaderField("Location");

                // get the cookie if need, for login
                String cookies = conn.getHeaderField("Set-Cookie");

                // open the new connnection again
                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                conn.setRequestProperty("Cookie", cookies);
                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.addRequestProperty("User-Agent", "Mozilla");
                conn.addRequestProperty("Referer", "google.com");
                logger.debug("Redirect to URL : {}", newUrl);
            }

            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                html.append(inputLine).append(NEWLINE);
            }
        } catch (Exception e) {
            logger.error("Extract content exception.", e);
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("Extract content IO error!", e);
                    throw e;
                }
            }
        }

        String text = "";
        String description = "";
        String keywords = "";
        String datePublished = "";
        String dateModified = "";
        String author = "";
        String publisher = "";
        String title = entry.getTitle();
        try {
            text = ArticleExtractor.INSTANCE.getText(html.toString());
        } catch (BoilerpipeProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

        if (text.equals("")) {
            text = entry.getDescription().getValue();
        }
        if (entry.getPublishedDate() != null) {
            datePublished = entry.getPublishedDate().toString();
            logger.debug("Publish Time -> {} ", datePublished);
        } else {
            datePublished = new Date().toString();
        }
        if (entry.getAuthor() != null) {
            author = entry.getAuthor();
        }
        if (entry.getUpdatedDate() != null) {
            dateModified = entry.getUpdatedDate().toString();
        }

        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(text);
            news.setSource(siteName);
            news.setTime(datePublished);
            news.setTitle(entry.getTitle());
            news.setUrl(entry.getLink());
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } else {
            // crawl news info
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.setTitle(title);
            newsInfo.setContent(text);
            newsInfo.setMediaName(siteName);
            newsInfo.setDatePublished(datePublished);
            newsInfo.setDateModified(dateModified);
            newsInfo.setAbstractText(description);
            newsInfo.setKeywords(keywords);
            newsInfo.setUrl(url);
            newsInfo.setPublisher(publisher);
            newsInfo.setAuthor(author);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                newsInfo.save(ctx);
            }
            return newsInfo.toString();
        }
    }

    public static String replaceHtml(String html) {

        String regEx = "<.+?>";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(html);
        String s = m.replaceAll("");
        return s;
    }

}
