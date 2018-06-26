package com.sap.cisp.xhna.data.executor.traditional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.common.cache.LRUCacheUtils;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutorBuilder;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;

@SuppressWarnings("rawtypes")
public class NewsCrawler extends TaskBarrierExecutor {
    private static Logger logger = LoggerFactory.getLogger(NewsCrawler.class);
    private static final String className = "NewsCrawler";

    // private static final String[] urlList = {
    // "http://www.dpa-international.com/news/asia/",
    // "http://www.dpa-international.com/news/international/",
    // "http://www.dpa-international.com/news/sports2/",
    // "http://www.dpa-international.com/news/Features_Trends/" };

    public NewsCrawler() {
    }

    public NewsCrawler(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, String url, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, url, parentExecutor);
    }

    public NewsCrawler(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, List<String> urlList, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, urlList, parentExecutor);
    }

    public static void crawl(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getDpaNewsLinks(ctx, parentExecutor);
    }

    public static void getDpaNewsLinks(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        List<String> urlItems = new ArrayList<String>();
        Document doc = null;
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rootUrl = params.getUrl();

        try {
            doc = Jsoup.connect(rootUrl).get();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            logger.error("Cannot connect root URL {}. Error : ", rootUrl, e1);
            throw e1;
        }
        Elements elements = doc.getElementsByTag("a");
        Iterator<Element> it = elements.iterator();
        while (it.hasNext()) {
            Element e = it.next();
            Attributes a = e.attributes();
            if (a.size() == 1 && e.attr("href").length() > 1
                    && e.attr("href").indexOf("/") != 0)
                urlItems.add(rootUrl + e.attr("href"));
        }

        ctx.put("methodName", "getDpaNews");
        ctx.put("parameterTypes", new Class[] { String.class, Map.class,
                ITaskExecutor.class });
        ctx.put("className", className);
        if (parentExecutor != null && !parentExecutor.isCanceled()
                && !urlItems.isEmpty()) {
            try {
                CyclicBarrier parentBarrier = new CyclicBarrier(2);
                TaskBarrierExecutorBuilder.getInstance()
                        .buildWebPageTaskBarrier(urlItems, ctx, parentExecutor,
                                parentBarrier);
                parentBarrier.await();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.error(
                        "Caught Exception during crawling DPA News website.", e);
                throw e;
            }
        }
    }

    public static String getDpaNews(String url, Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws IOException {
        if (!LRUCacheUtils.checkIfTraditionalUrlCached(url)
                || !parentExecutor.isCacheable()) {
            logger.debug("Crawl the DPA url {}.", url);
        } else {
            // Here may need more logic, update time is a question mark?
            logger.info("The url {} has been crawled before.", url);
            return null;
        }
        String content = "";
        String description = "";
        String keywords = "";
        String datePublished = "";
        String dateModified = "";
        String author = "";
        String publisher = "";
        String title = "";
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw e;
        }
        title = doc.getElementsByClass("span35").text();
        // datePublished =
        // RomeCrawler.timesTransferDate(doc.getElementsByClass("createdDate").text());
        content = doc.getElementsByClass("articleContent")
                .attr("itemprop", "text").text();

        Elements metaDesc = doc.select("meta[name=description]");
        if (!metaDesc.isEmpty()) {
            description = metaDesc.first().attr("content");
        }

        Elements metaAuthor = doc.select("meta[name=author]");
        if (!metaAuthor.isEmpty()) {
            author = metaAuthor.first().attr("content");
        }

        Elements metaPublisher = doc.select("meta[name=publisher]");
        if (!metaPublisher.isEmpty()) {
            publisher = metaPublisher.first().attr("content");
        }

        Elements metaPublishDate = doc.select("meta[name=date]");
        if (!metaPublishDate.isEmpty()) {
            datePublished = metaPublishDate.first().attr("content");
        }

        Elements metaKeywords = doc.select("meta[name=keywords]");
        if (metaKeywords.isEmpty()) {
            metaKeywords = doc.select("meta[name=news_keywords]");
        }
        if (!metaKeywords.isEmpty()) {
            keywords = metaKeywords.first().attr("content");
        }

        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(content);
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
            newsInfo.setContent(content);
            newsInfo.setMediaName("DPA");
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
}
