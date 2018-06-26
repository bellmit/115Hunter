package com.sap.cisp.xhna.data.executor.traditional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.Main;
import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.cache.LRUCacheUtils;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutorBuilder;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;

@SuppressWarnings("rawtypes")
public class GetNews extends TaskBarrierExecutor {
    private static Logger logger = LoggerFactory.getLogger(GetNews.class);
    private static final String className = "GetNews";

    public GetNews() {
    }

    public GetNews(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, String url, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, url, parentExecutor);
    }

    public GetNews(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, List<String> urlList, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, urlList, parentExecutor);
    }

    public static void main(String... args) throws Exception {
        System.setProperty("http.proxyHost", "proxy.pal.sap.corp");
        System.setProperty("http.proxyPort", "8080");
        ThreadFactory factory = new TaskThreadFactory();
        Main.setTaskExecutorPool(new TaskExecutorPool(1000, 1000, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), factory));
        // get_reuters_list("040815");
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.TraditionalMedia_ArticleData_ByWebPage,
                        "Reuters", param);
        ctx.put("task", task);
        List<String> result = new ArrayList<String>();
        ctx.put("result", result);
        TraditionalWebPageExecutor exe = new TraditionalWebPageExecutor();
        exe.setTask(task);
        exe.setTestFlagOn(true);
        getReutersList("", ctx, exe);
        getReutersList("", ctx, exe);
        // getUpiList(ctx);
        // analysisAFPMainPage(ctx);
        System.out.println("end.");

    }

    // 1. www.reuters.com
    public static void getReutersList(String date, Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        String webSite = "http://www.reuters.com/news/archive/";
        List<String> list = new ArrayList<String>();
        String url = webSite + "domesticNews?date=" + date;
        list.add(url);

        url = webSite + "euroZone?date=" + date;
        list.add(url);
        url = webSite + "china?date=" + date;
        list.add(url);
        url = webSite + "japan?date=" + date;
        list.add(url);
        url = webSite + "india?date=" + date;
        list.add(url);
        url = webSite + "middle-east?date=" + date;
        list.add(url);
        url = webSite + "africa?date=" + date;
        list.add(url);
        url = webSite + "technologyNews?date=" + date;
        list.add(url);
        url = webSite + "sportsNews?date=" + date;
        list.add(url);
        url = webSite + "scienceNews?date=" + date;
        list.add(url);

        logger.info("Crawl Reuters website list.");
        List<String> urlItems = new ArrayList<String>();
        try {
            for (int i = 0; i < list.size(); i++) {
                Document doc = Jsoup.connect(list.get(i))
                        .timeout(Integer.MAX_VALUE).get();

                Elements items = doc.getElementsByClass("feature");

                for (Element item : items) {
                    Elements eles = item.getElementsByTag("a");
                    String ref = eles.first().attr("href");
                    if (ref.contains("article")) {
                        urlItems.add("http://www.reuters.com" + ref);
                    }
                }
            }
            ctx.put("methodName", "analysisReutersPage");
            ctx.put("parameterTypes", new Class[] { String.class, Map.class,
                    ITaskExecutor.class });
            ctx.put("className", className);
            if (parentExecutor != null && !parentExecutor.isCanceled()
                    && !urlItems.isEmpty()) {
                CyclicBarrier parentBarrier = new CyclicBarrier(2);
                TaskBarrierExecutorBuilder.getInstance()
                        .buildWebPageTaskBarrier(urlItems, ctx, parentExecutor,
                                parentBarrier);
                parentBarrier.await();
            }
        } catch (IOException e) {
            logger.error("Caught Exception during crawling Reuters.", e);
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    public String analysisReutersPage(String url, Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws IOException {
        try {
            if (!LRUCacheUtils.checkIfTraditionalUrlCached(url)
                    || !parentExecutor.isCacheable()) {
                logger.debug("Crawl the source url {}.", url);
            } else {
                // Here may need more logic, update time is a question mark?
                logger.info("The url {} has been crawled before.", url);
                return null;
            }

            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Elements titles = doc.getElementsByClass("article-headline");
            String title = titles.first().text();
            Elements metaDesc = doc.select("meta[name=description]");
            logger.debug("----> Description : {}",
                    metaDesc.first().attr("content"));
            Elements metaKeyword = doc.select("meta[name=keywords]");
            logger.debug("----> Keywords : {}",
                    metaKeyword.first().attr("content"));
            Elements times = doc.getElementsByClass("timestamp");
            String time = times.first().text();

            Element content = doc.getElementById("articleText");

            Elements paras = content.getElementsByTag("p");

            StringBuilder allText = new StringBuilder();
            for (Element para : paras) {
                allText.append(para.text()).append(NEWLINE);
            }
            News news = new News();
            news.setContent(allText.toString());
            news.setSource("REUTERS");
            news.setTime(time);
            news.setTitle(title);
            news.setUrl(url);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();

        } catch (IOException e) {
            logger.error("Caught Exception during analyzing Reuters page.", e);
            throw e;
        } catch (Exception e) {
            logger.error("Caught Exception during crawling Reuters website.", e);
            throw e;
        }

    }

    // 2. www.upi.com
    public static void getUpiList(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        String webSite = "http://www.upi.com/";
        List<String> list = new ArrayList<String>();

        int page_num = 1;
        for (; page_num < 10; page_num++) {
            String url = webSite
                    + String.format("Top_News/World-News/2015/p%d/", page_num);
            list.add(url);
            url = webSite + String.format("Top_News/US/2015/p%d/", page_num);
            list.add(url);
            url = webSite + String.format("Business_News/2015/p%d/", page_num);
            list.add(url);
            url = webSite
                    + String.format("Entertainment_News/2015/p%d/", page_num);
            list.add(url);
        }

        logger.info("Crawl Upi website list.");
        List<String> urlItems = new ArrayList<String>();
        try {
            for (int i = 0; i < list.size(); i++) {
                Document doc = Jsoup.connect(list.get(i))
                        .timeout(Integer.MAX_VALUE).get();

                Elements items = doc.getElementsByClass("upi_item");

                for (Element item : items) {
                    Elements eles = item.getElementsByTag("a");

                    String ref = eles.first().attr("href");
                    urlItems.add(ref);
                }
            }
            ctx.put("methodName", "analysisUpiPage");
            ctx.put("parameterTypes", new Class[] { String.class, Map.class,
                    ITaskExecutor.class });
            ctx.put("className", className);
            if (parentExecutor != null && !parentExecutor.isCanceled()
                    && !urlItems.isEmpty()) {
                CyclicBarrier parentBarrier = new CyclicBarrier(2);
                TaskBarrierExecutorBuilder.getInstance()
                        .buildWebPageTaskBarrier(urlItems, ctx, parentExecutor,
                                parentBarrier);
                parentBarrier.await();
            }
        } catch (IOException e) {
            logger.error("Caught Exception during crawling Upi website.", e);
            throw e;
        } catch (Exception e) {
            logger.error("Caught Exception during crawling Upi website.", e);
            throw e;
        }
    }

    public static String analysisUpiPage(String url, Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws IOException {
        try {
            if (!LRUCacheUtils.checkIfTraditionalUrlCached(url)
                    || !parentExecutor.isCacheable()) {
                logger.debug("Crawl the source url {}.", url);
            } else {
                logger.info("The url {} has been crawled before.", url);
                return null;
            }

            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Elements titles = doc.getElementsByTag("h1");
            String title = titles.first().text();

            Element times = titles.first().nextElementSibling()
                    .nextElementSibling().nextElementSibling();

            String time = times.text();

            Elements contents = doc.getElementsByClass("st_text_c");
            String content = contents.first().text();

            Elements paras = contents.first().getElementsByTag("p");

            for (Element para : paras) {
                int pos = content.indexOf(para.text());
                if (pos > 0) {
                    StringBuilder sb = new StringBuilder();
                    content = sb.append(content).insert(pos, NEWLINE)
                            .toString();
                }
            }

            News news = new News();
            news.setContent(content);
            news.setSource("UPI");
            news.setTime(time);
            news.setTitle(title);
            news.setUrl(url);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } catch (IOException e) {
            logger.error("Caught Exception during analyzing Upi page.", e);
            throw e;
        } catch (Exception e) {
            logger.error("Caught Exception during crawling Upi page.", e);
            throw e;
        }
    }

    // 4 http://www.afp.com/en
    public static void analysisAFPMainPage(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        try {
            // Always crawl base on the AFP main page.
            Document doc = Jsoup.connect("http://www.afp.com/en")
                    .timeout(Integer.MAX_VALUE).get();
            Element top = doc.getElementById("top_stories");
            Elements elems = top.getElementsByTag("h3");
            List<String> urlItems = new ArrayList<String>();
            for (Element elem : elems) {
                Elements refs = elem.getElementsByTag("a");
                String ref = refs.first().attr("href");

                if (ref.contains("/en/news/")) {
                    urlItems.add("http://www.afp.com/" + ref);
                }
            }
            ctx.put("methodName", "analysisAFPPage");
            ctx.put("parameterTypes", new Class[] { String.class, Map.class,
                    ITaskExecutor.class });
            ctx.put("className", className);
            if (parentExecutor != null && !parentExecutor.isCanceled()
                    && !urlItems.isEmpty()) {
                CyclicBarrier parentBarrier = new CyclicBarrier(2);
                TaskBarrierExecutorBuilder.getInstance()
                        .buildWebPageTaskBarrier(urlItems, ctx, parentExecutor,
                                parentBarrier);
                parentBarrier.await();
            }

        } catch (IOException e) {
            logger.error("Caught Exception during analyzing AFP main page.", e);
            throw e;
        } catch (Exception e) {
            logger.error("Caught Exception during analyzing AFP main page.", e);
            throw e;
        }
    }

    public static String analysisAFPPage(String url, Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        try {
            if (!LRUCacheUtils.checkIfTraditionalUrlCached(url)
                    || !parentExecutor.isCacheable()) {
                logger.debug("Crawl the AFP url {}.", url);
            } else {
                // Here may need more logic, update time is a question mark?
                logger.info("The url {} has been crawled before.", url);
                return null;
            }

            StringBuilder allText = new StringBuilder();
            String description = "";
            String keywords = "";
            String datePublished = "";
            String dateModified = "";
            String author = "AFP";
            String publisher = "AFP";
            String title = "";
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Elements titles = doc.getElementsByAttributeValue("itemprop",
                    "name");
            if (titles.first() != null) {
                title = titles.first().text();
            } 

            Elements times = doc.getElementsByAttributeValueContaining(
                    "itemprop", "datePublished");
            datePublished = RomeCrawler.timesTransferDate(times.text());
            logger.debug("Publish date --> {}", datePublished);

            Element content = doc.getElementById("release-content");
            Elements paras = content.getElementsByTag("p");

            for (Element para : paras) {
                if (!para.hasAttr("class")) {
                    allText.append(para.text()).append(NEWLINE);
                }
            }
            if (parentExecutor.isCacheable()) {
                News news = new News();
                news.setContent(allText.toString());
                news.setSource(publisher);
                news.setTime(datePublished);
                news.setTitle(title);
                news.setUrl(url);
                if (parentExecutor != null && !parentExecutor.isCanceled()) {
                    news.save(ctx);
                }
                return news.toString();
            } else {
                // crawl news info
                NewsInfo newsInfo = new NewsInfo();
                newsInfo.setTitle(title);
                newsInfo.setContent(allText.toString());
                newsInfo.setMediaName("AFP");
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
        } catch (Exception e) {
            logger.error("Caught Exception during analyzing AFP page.", e);
            throw e;
        }
    }
    
    // 5 http://www.chicagotribune.com/news
    public static void analysisChicagotribuneMainPage(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        try {
            // Always crawl base on the Chicago tribune breaking news page.
            Document doc = Jsoup.connect("http://www.chicagotribune.com/news/local/breaking/")
                    .timeout(Integer.MAX_VALUE).get();
            Elements elems = doc.getElementsByClass("trb_outfit_group_list_item");

            List<String> urlItems = new ArrayList<String>();
            for (Element elem : elems) {
                String ref = elem.attr("data-content-url");

                if (ref.contains("/news/local/breaking/")) {
                    urlItems.add("http://www.chicagotribune.com" + ref);
                }
            }
            ctx.put("methodName", "analysisChicagotribunePage");
            ctx.put("parameterTypes", new Class[] { String.class, Map.class,
                    ITaskExecutor.class });
            ctx.put("className", className);
            if (parentExecutor != null && !parentExecutor.isCanceled()
                    && !urlItems.isEmpty()) {
                CyclicBarrier parentBarrier = new CyclicBarrier(2);
                TaskBarrierExecutorBuilder.getInstance()
                        .buildWebPageTaskBarrier(urlItems, ctx, parentExecutor,
                                parentBarrier);
                parentBarrier.await();
            }

        } catch (IOException e) {
            logger.error("Caught Exception during analyzing Chicagotribune breaking news page.", e);
            throw e;
        } catch (Exception e) {
            logger.error("Caught Exception during analyzing Chicagotribune breaking news page.", e);
            throw e;
        }
    }

    public static String analysisChicagotribunePage(String url, Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        try {
            if (!LRUCacheUtils.checkIfTraditionalUrlCached(url)
                    || !parentExecutor.isCacheable()) {
                logger.debug("Crawl the Chicagotribune url {}.", url);
            } else {
                // Here may need more logic, update time is a question mark?
                logger.info("The url {} has been crawled before.", url);
                return null;
            }

            StringBuilder allText = new StringBuilder();
            String description = "";
            String keywords = "";
            String datePublished = "";
            String dateModified = "";
            String author = "";
            String publisher = "";
            String title = "";
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();
            
            
            Elements items = doc.select("div[itemprop=articleBody]");
            if (!items.isEmpty()) {
                Element content = items.first();
                Elements paras = content.getElementsByTag("p");

                for (Element para : paras) {
                    if (!para.hasAttr("class")) {
                        allText.append(para.text()).append(NEWLINE);
                    }
                }
            }

            Elements metaTitle = doc.select("meta[name=fb_title]");
            if (!metaTitle.isEmpty()) {
                title = metaTitle.first().attr("content");
            }

            Elements metaDesc = doc.select("meta[name=Description]");
            if (!metaDesc.isEmpty()) {
                description = metaDesc.first().attr("content");
            }

            Elements metaKeyword = doc.select("meta[name=news_keywords]");
            if (!metaKeyword.isEmpty()) {
                keywords = metaKeyword.first().attr("content");
            }
            logger.debug("----> Keywords : {}", keywords);

            Elements metaDateModified = doc
                    .select("meta[itemprop=dateModified]");
            if (!metaDateModified.isEmpty()) {
                dateModified = metaDateModified.first().attr("content");
            }
            logger.debug("----> Date Modified : {}", dateModified);

            Elements metaDatePublisheded = doc
                    .select("meta[itemprop=datePublished]");
            if (!metaDatePublisheded.isEmpty()) {
                datePublished = metaDatePublisheded.first().attr("content");
            }
            logger.debug("----> Date Published : {}", datePublished);
            
            Elements metaAuthor = doc.select("meta[name=author]");
            if (!metaAuthor.isEmpty()) {
                author = metaAuthor.first().attr("content");
            }
            logger.debug("----> author : {}", author);

            Elements metaPublisher = doc.select("meta[property=og:site_name]");
            if (!metaPublisher.isEmpty()) {
                publisher = metaPublisher.first().attr("content");
            }
            logger.debug("----> publisher(site name) : {}", publisher);

            if (parentExecutor.isCacheable()) {
                News news = new News();
                news.setContent(allText.toString());
                news.setSource(publisher);
                news.setTime(datePublished);
                news.setTitle(title);
                news.setUrl(url);
                if (parentExecutor != null && !parentExecutor.isCanceled()) {
                    news.save(ctx);
                }
                return news.toString();
            } else {
                // crawl news info
                NewsInfo newsInfo = new NewsInfo();
                newsInfo.setTitle(title);
                newsInfo.setContent(allText.toString());
                newsInfo.setMediaName("Chicago Tribune");
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
        } catch (Exception e) {
            logger.error("Caught Exception during analyzing AFP page.", e);
            throw e;
        }
    }
}
