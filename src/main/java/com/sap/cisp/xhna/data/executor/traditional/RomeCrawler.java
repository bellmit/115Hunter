package com.sap.cisp.xhna.data.executor.traditional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sun.syndication.feed.synd.SyndEntry;

@SuppressWarnings("rawtypes")
public class RomeCrawler extends TaskBarrierExecutor {
    private static Logger logger = LoggerFactory.getLogger(RomeCrawler.class);
    private static final String className = "RomeCrawler";
    Map<String, Object> ctx;
    public static final String[] cnnUrlList = {
            "http://rss.cnn.com/rss/edition.rss",
            "http://rss.cnn.com/rss/edition_world.rss",
            "http://rss.cnn.com/rss/edition_world.rss",
            "http://rss.cnn.com/rss/edition_africa.rss",
            "http://rss.cnn.com/rss/edition_africa.rss",
            "http://rss.cnn.com/rss/edition_americas.rss",
            "http://rss.cnn.com/rss/edition_americas.rss",
            "http://rss.cnn.com/rss/edition_asia.rss",
            "http://rss.cnn.com/rss/edition_asia.rss",
            "http://rss.cnn.com/rss/edition_europe.rss",
            "http://rss.cnn.com/rss/edition_europe.rss",
            "http://rss.cnn.com/rss/edition_meast.rss",
            "http://rss.cnn.com/rss/edition_meast.rss",
            "http://rss.cnn.com/rss/edition_us.rss",
            "http://rss.cnn.com/rss/edition_us.rss",
            "http://rss.cnn.com/rss/money_news_international.rss",
            "http://rss.cnn.com/rss/money_news_international.rss",
            "http://rss.cnn.com/rss/edition_technology.rss",
            "http://rss.cnn.com/rss/edition_technology.rss",
            "http://rss.cnn.com/rss/edition_space.rss",
            "http://rss.cnn.com/rss/edition_space.rss",
            "http://rss.cnn.com/rss/edition_entertainment.rss",
            "http://rss.cnn.com/rss/edition_entertainment.rss",
            "http://rss.cnn.com/rss/edition_sport.rss",
            "http://rss.cnn.com/rss/edition_sport.rss",
            "http://rss.cnn.com/rss/edition_football.rss",
            "http://rss.cnn.com/rss/edition_football.rss",
            "http://rss.cnn.com/rss/edition_golf.rss",
            "http://rss.cnn.com/rss/edition_golf.rss",
            "http://rss.cnn.com/rss/edition_motorsport.rss",
            "http://rss.cnn.com/rss/edition_motorsport.rss",
            "http://rss.cnn.com/rss/edition_tennis.rss",
            "http://rss.cnn.com/rss/edition_tennis.rss",
            "http://rss.cnn.com/rss/cnn_freevideo.rss",
            "http://rss.cnn.com/rss/cnn_freevideo.rss",
            "http://rss.cnn.com/rss/cnn_latest.rss",
            "http://rss.cnn.com/rss/cnn_latest.rss",
            "http://rss.cnn.com/rss/edition_connecttheworld.rss",
            "http://rss.cnn.com/rss/edition_connecttheworld.rss",
            "http://rss.cnn.com/rss/edition_worldsportblog.rss",
            "http://rss.cnn.com/rss/edition_worldsportblog.rss" };

    private static final String patternCNNStr = "(?:(rss.cnn.com/rss.*))";
    // Less compile
    public static final Pattern patternCNN = Pattern.compile(patternCNNStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static final String[] newyorkTimesUrlList = {
            "http://www.nytimes.com/services/xml/rss/nyt/HomePage.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/InternationalHome.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/World.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Africa.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Americas.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/AsiaPacific.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Europe.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/MiddleEast.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/US.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Education.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Politics.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Upshot.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/NYRegion.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/EnergyEnvironment.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/InternationalBusiness.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/SmallBusiness.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Economy.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Dealbook.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/MediaandAdvertising.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/YourMoney.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/PersonalTech.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Sports.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/InternationalSports.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Baseball.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/CollegeBasketball.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/CollegeFootball.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Golf.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Hockey.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/ProBasketball.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/ProFootball.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Soccer.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Tennis.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Science.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Environment.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Space.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Health.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Research.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Nutrition.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/HealthCarePolicy.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Views.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Arts.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/InternationalArts.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/ArtandDesign.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Books.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/SundayBookReview.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Dance.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Movies.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Music.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Television.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Theater.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/FashionandStyle.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/InternationalStyle.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/DiningandWine.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/InternationalDiningandWine.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/HomeandGarden.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Weddings.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/tmagazine.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Travel.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/JobMarket.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/RealEstate.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Commercial.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Automobiles.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/Obituaries.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/pop_top.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/MostShared.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/MostViewed.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/sunday-review.xml",
            "http://www.nytimes.com/services/xml/rss/nyt/InternationalOpinion.xml" };

    private static final String patternNYTStr = "(?:(^http://www.nytimes.com/services/xml/rss.*))";
    // Less compile
    public static final Pattern patternNYT = Pattern.compile(patternNYTStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static final String[] foxUrlList = {
            "http://feeds.foxnews.com/foxnews/latest",
            "http://feeds.foxnews.com/foxnews/world" };

    private static final String patternFOXStr = "(?:(^http://feeds.foxnews.com.*))";
    // Less compile
    public static final Pattern patternFOX = Pattern.compile(patternFOXStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static final String[] washingtonPostUrlList = {
            "http://feeds.washingtonpost.com/rss/world",
            "http://feeds.washingtonpost.com/rss/politics",
            "http://feeds.washingtonpost.com/rss/national",
            "http://feeds.washingtonpost.com/rss/business" };

    private static final String patternWPStr = "(?:(^http://feeds.washingtonpost.com/rss.*))";
    // Less compile
    public static final Pattern patternWP = Pattern.compile(patternWPStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static final String[] nbcNewsUrlList = { "http://feeds.nbcnews.com/feeds/topstories" };

    private static final String patternNBCStr = "(?:(^http://feeds.nbcnews.com.*))";
    // Less compile
    public static final Pattern patternNBC = Pattern.compile(patternNBCStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static final String[] theTimesUrlList = {
            "http://www.thetimes.co.uk/tto/news/world/rss",
            "http://www.thetimes.co.uk/tto/news/world/asia/rss",
            "http://www.thetimes.co.uk/tto/news/world/middleeast/rss" };

    private static final String patternTheTimesStr = "(?:(^http://www.thetimes.co.uk.*rss.*))";
    // Less compile
    public static final Pattern patternTheTimes = Pattern.compile(
            patternTheTimesStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static final String[] guardianUrlList = {
            "http://www.theguardian.com/world/rss",
            "http://www.theguardian.com/theguardian/rss" };

    private static final String patternGuardianStr = "(?:(^http://www.theguardian.com.*rss.*))";
    // Less compile
    public static final Pattern patternGuardian = Pattern.compile(
            patternGuardianStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static final String[] apUrlList = {
            "http://hosted2.ap.org/atom/APDEFAULT/cae69a7523db45408eeb2b3a98c0c9c5",
            "http://hosted2.ap.org/atom/APDEFAULT/3d281c11a96b4ad082fe88aa0db04305",
            "http://hosted2.ap.org/atom/APDEFAULT/f70471f764144b2fab526d39972d37b3" };

    private static final String patternAPStr = "(?:(^http://.*ap.org.*))";
    // Less compile
    public static final Pattern patternAP = Pattern.compile(patternAPStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static final String[] chinaDailyUrlList = {
            "http://www.chinadaily.com.cn/rss/china_rss.xml",
            "http://www.chinadaily.com.cn/rss/world_rss.xml" };

    private static final String patternCDStr = "(?:(^http://www.chinadaily.com.*rss.*))";
    // Less compile
    public static final Pattern patternCD = Pattern.compile(patternCDStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final String patternBBCStr = "(?:(^http://feeds.bbci.co.uk/news.*rss.*))";
    private static final Pattern patternBBC = Pattern.compile(patternBBCStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final String patternReutersStr = "(?:(^http://feeds.reuters.com/reuters/.*))";
    private static final Pattern patternReuters = Pattern.compile(patternReutersStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final String patternUPIStr = "(?:(^http://rss.upi.com/news/.*))";
    private static final Pattern patternUPI = Pattern.compile(patternUPIStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    // For BBC to extract last updated time
    private static final Pattern patternLastUpdatedDate = Pattern.compile(
            "\"last_updated\".*\"date\".*}", Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);
    private static final Pattern patternDate = Pattern
            .compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}");
    private static final Pattern patternTimeZone = Pattern.compile(
            "(?:(?!^})(\"timezone\":.*))", Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);

    public static Map<RomeMediaEnum, Pattern> mediaEnumMap = new HashMap<RomeMediaEnum, Pattern>();

    static {
        initMediaEnumMap();
    }

    private static void initMediaEnumMap() {
        mediaEnumMap.putIfAbsent(RomeMediaEnum.CNN, patternCNN);
        mediaEnumMap.putIfAbsent(RomeMediaEnum.NEWYORK_TIMES, patternNYT);
        mediaEnumMap.putIfAbsent(RomeMediaEnum.FOX, patternFOX);
        mediaEnumMap.putIfAbsent(RomeMediaEnum.WASHINGTON_POST, patternWP);
        mediaEnumMap.putIfAbsent(RomeMediaEnum.NBC, patternNBC);
        mediaEnumMap.putIfAbsent(RomeMediaEnum.THE_TIMES, patternTheTimes);
        mediaEnumMap.putIfAbsent(RomeMediaEnum.GUARDIAN, patternGuardian);
        mediaEnumMap.putIfAbsent(RomeMediaEnum.AP, patternAP);
        mediaEnumMap.putIfAbsent(RomeMediaEnum.CHINA_DAILY, patternCD);
        mediaEnumMap.putIfAbsent(RomeMediaEnum.BBC, patternBBC);
        mediaEnumMap.putIfAbsent(RomeMediaEnum.REUTERS, patternReuters);
        mediaEnumMap.putIfAbsent(RomeMediaEnum.UPI, patternUPI);
    }

    public RomeCrawler(Map<String, Object> ctx) {
        super();
        this.ctx = ctx;
    }

    public RomeCrawler() {
    }

    public RomeCrawler(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, String url, SyndEntry entry,
            ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, url, entry, parentExecutor);
    }

    public RomeCrawler(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, String url, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, url, parentExecutor);
    }

    public RomeCrawler(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, List<String> urlList, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, urlList, parentExecutor);
    }

    public RomeCrawler(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, List<Map.Entry<String, SyndEntry>> entryList, boolean isEntryNeeded, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, entryList, isEntryNeeded, parentExecutor);
    }
    

    // 1 CNN
    public static void getCNNRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getCNNDomainRSS(ctx, parentExecutor);
    }

    public static void getCNNDomainRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        Map<String, SyndEntry> urlMap = new HashMap<String, SyndEntry>();
        createEntryMap(urlList, urlMap, parentExecutor);
        // createEntryMap(cnnUrlList, urlMap, parentExecutor);
        ctx.put("methodName", "analysisCNNPage");
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
                logger.error("Caught Exception during crawling FOX News RSS.",
                        e);
                throw e;
            }
        }
    }

    public String analysisCNNPage(String url, SyndEntry entry,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws IOException {
        StringBuilder content = new StringBuilder();
        String description = "";
        String keywords = "";
        String dateModified = "";
        String author = "";
        String publisher = "";
        try {
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Elements items = doc.getElementsByClass("zn-body__paragraph");
            if (!items.isEmpty()) {
                for (Element item : items) {
                    content.append(item.text()).append(NEWLINE);
                }
            }

            Elements metaDesc = doc.select("meta[name=description]");
            if (!metaDesc.isEmpty()) {
                description = metaDesc.first().attr("content");
            }

            Elements metaKeyword = doc.select("meta[name=keywords]");
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

            Elements metaAuthor = doc.select("meta[name=author]");
            if (!metaAuthor.isEmpty()) {
                author = metaAuthor.first().attr("content");
            }
            logger.debug("----> author : {}", author);

            Elements metaPublisher = doc.select("link[rel=publisher]");
            if (!metaPublisher.isEmpty()) {
                publisher = metaPublisher.first().attr("href");
            }
            logger.debug("----> Publish Time: {}", entry.getPublishedDate());
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        if (content.toString().equals("")) {
            content.append(entry.getDescription().getValue());
        }
        // crawl news
        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(content.toString());
            news.setSource("CNN");
            news.setTime(sdf.format(entry.getPublishedDate()));
            news.setTitle(entry.getTitle());
            news.setUrl(entry.getLink());
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } else {
            // crawl news info
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.setTitle(entry.getTitle());
            newsInfo.setContent(content.toString());
            newsInfo.setMediaName("CNN");
            newsInfo.setDatePublished(sdf.format(entry.getPublishedDate()));
            newsInfo.setDateModified(dateModified);
            newsInfo.setAbstractText(description);
            newsInfo.setKeywords(keywords);
            newsInfo.setUrl(entry.getLink());
            newsInfo.setPublisher(publisher);
            newsInfo.setAuthor(author);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                newsInfo.save(ctx);
            }
            return newsInfo.toString();
        }
    }

    // 2 Newyork Times
    public static void getNewYorkTimesRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getNewYorkTimesDomainRss(ctx, parentExecutor);
    }

    public static void getNewYorkTimesDomainRss(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        Map<String, SyndEntry> urlMap = new HashMap<String, SyndEntry>();
        createEntryMap(urlList, urlMap, parentExecutor);
        // createEntryMap(newyorkTimesUrlList, urlMap, parentExecutor);

        ctx.put("methodName", "analysisNewYorkTimesPage");
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
                logger.error("Caught Exception during crawling FOX News RSS.",
                        e);
                throw e;
            }
        }
    }

    private String newYorkTimesTransferDate(String input) {
        String year = input.substring(0, 4);
        String month = input.substring(4, 6);
        String day = input.substring(6, 8);
        String hour = input.substring(8, 10);
        String minute = input.substring(10, 12);
        String second = input.substring(12, 14);

        return year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":"
                + second;
    }

    public String analysisNewYorkTimesPage(String url, SyndEntry entry,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws IOException {
        StringBuilder content = new StringBuilder();
        String description = "";
        String keywords = "";
        String dateModified = "";
        String author = "";
        String publisher = "";

        try {
            Document doc = Jsoup
                    .connect(entry.getLink())
                    .cookie("RMID", "007f0101056e5524c6680008")
                    .cookie("NYT-S",
                            "0MYzMgAvU2lLTDXrmvxADeHJct0i81KKP/deFz9JchiAIUFL2BEX5FWcV.Ynx4rkFI")
                    .timeout(Integer.MAX_VALUE).get();

            Elements items = doc.getElementsByAttributeValueMatching(
                    "itemprop", "articleBody");
            if (!items.isEmpty()) {
                for (Element item : items) {
                    content.append(item.text()).append(NEWLINE);
                }
            }

            Elements metaDesc = doc.select("meta[name=description]");
            if (!metaDesc.isEmpty()) {
                description = metaDesc.first().attr("content");
            }

            Elements metaKeyword = doc.select("meta[name=keywords]");
            if (!metaKeyword.isEmpty()) {
                keywords = metaKeyword.first().attr("content");
            }

            Elements metaDateModified = doc.select("meta[name=utime]");
            if (!metaDateModified.isEmpty()) {
                dateModified = newYorkTimesTransferDate(metaDateModified
                        .first().attr("content"));
            }

            Elements metaAuthor = doc.select("meta[name=author]");
            if (!metaAuthor.isEmpty()) {
                author = metaAuthor.first().attr("content");
            }

        } catch (IOException e) {
            logger.error("Caught Exception during crawling NewYork Times RSS.",
                    e);
            throw e;
        }
        if (content.toString().equals("")) {
            content.append(entry.getDescription().getValue());
        }
        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(content.toString());
            news.setSource("NEWYORKTIMES");
            news.setTime(sdf.format(entry.getPublishedDate()));
            news.setTitle(entry.getTitle());
            news.setUrl(entry.getLink());
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } else {
            // crawl news info
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.setTitle(entry.getTitle());
            newsInfo.setContent(content.toString());
            newsInfo.setMediaName("NEWYORKTIMES");
            newsInfo.setDatePublished(sdf.format(entry.getPublishedDate()));
            newsInfo.setDateModified(dateModified);
            newsInfo.setAbstractText(description);
            newsInfo.setKeywords(keywords);
            newsInfo.setUrl(entry.getLink());
            newsInfo.setPublisher(publisher);
            newsInfo.setAuthor(author);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                newsInfo.save(ctx);
            }
            return newsInfo.toString();
        }
    }

    // 3 foxnews Times
    public static void getFoxRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getFoxNewsDomainRSS(ctx, parentExecutor);
    }

    public static void getFoxNewsDomainRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        Map<String, SyndEntry> urlMap = new HashMap<String, SyndEntry>();
        createEntryMap(urlList, urlMap, parentExecutor);
        // createEntryMap(foxUrlList, urlMap, parentExecutor);
        ctx.put("methodName", "analysisFoxNewsPage");
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
                logger.error("Caught Exception during crawling FOX News RSS.",
                        e);
                throw e;
            }
        }
    }

    public String analysisFoxNewsPage(String url, SyndEntry entry,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws IOException {
        StringBuilder content = new StringBuilder();
        String description = "";
        String keywords = "";
        String dateModified = "";
        String author = "";
        String publisher = "";
        try {
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Elements items = doc.getElementsByAttributeValueMatching(
                    "itemprop", "articleBody");
            if (!items.isEmpty()) {
                Elements paras = items.first().getElementsByTag("p");

                for (Element para : paras) {
                    if (!para.hasClass("advert-txt")) {
                        content.append(para.text()).append(NEWLINE);
                    }
                }
            }

            Elements metaDesc = doc.select("meta[name=dc.description]");
            if (!metaDesc.isEmpty()) {
                description = metaDesc.first().attr("content");
            }

            Elements metaKeyword = doc.select("meta[name=keywords]");
            if (!metaKeyword.isEmpty()) {
                keywords = metaKeyword.first().attr("content");
            }

            Elements metaDateModified = doc
                    .select("meta[name=dcterms.modified]");
            if (!metaDateModified.isEmpty()) {
                dateModified = metaDateModified.first().attr("content");
            }

            Elements metaPublisher = doc.select("meta[name=dc.publisher]");
            if (!metaPublisher.isEmpty()) {
                publisher = metaPublisher.first().attr("content");
            }
        } catch (IOException e) {
            logger.error("Caught Exception during crawling Fox News RSS.", e);
            throw e;
        }
        if (content.toString().equals("")) {
            content.append(entry.getDescription().getValue());
        }
        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(content.toString());
            news.setSource("FOXNEWS");
            news.setTime(sdf.format(entry.getPublishedDate()));
            news.setTitle(entry.getTitle());
            news.setUrl(entry.getLink());
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } else {
            // crawl news info
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.setTitle(entry.getTitle());
            newsInfo.setContent(content.toString());
            newsInfo.setMediaName("FOXNEWS");
            newsInfo.setDatePublished(sdf.format(entry.getPublishedDate()));
            newsInfo.setDateModified(dateModified);
            newsInfo.setAbstractText(description);
            newsInfo.setKeywords(keywords);
            newsInfo.setUrl(entry.getLink());
            newsInfo.setPublisher(publisher);
            newsInfo.setAuthor(author);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                newsInfo.save(ctx);
            }
            return newsInfo.toString();
        }
    }

    // 4 washingtonpost
    public static void getWashingtonPostRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getWashingtonPostDomainRSS(ctx, parentExecutor);
    }

    public static void getWashingtonPostDomainRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        Map<String, SyndEntry> urlMap = new HashMap<String, SyndEntry>();
        createEntryMap(urlList, urlMap, parentExecutor);
        // createEntryMap(washingtonPostUrlList, urlMap, parentExecutor);
        ctx.put("methodName", "analysisWashingtonPostPage");
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
                logger.error("Caught Exception during crawling FOX News RSS.",
                        e);
                throw e;
            }
        }
    }

    public String analysisWashingtonPostPage(String url, SyndEntry entry,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws IOException {
        StringBuilder content = new StringBuilder();
        String description = "";
        String keywords = "";
        String dateModified = "";
        String author = "";
        String publisher = "";
        try {
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Elements items = doc.getElementsByTag("article");
            if (!items.isEmpty()) {
                Elements paras = items.first().getElementsByTag("p");

                for (Element para : paras) {
                    content.append(para.text()).append(NEWLINE);
                }
            }

            Elements metaDesc = doc.select("meta[name=description]");
            if (!metaDesc.isEmpty()) {
                description = metaDesc.first().attr("content");
            }

            Elements metaKeyword = doc.select("meta[name=keywords]");
            if (!metaKeyword.isEmpty()) {
                keywords = metaKeyword.first().attr("content");
            }

            /*
             * Elements metaDateModified =
             * doc.select("meta[name=dcterms.modified]");
             * if(!metaDateModified.isEmpty()) { dateModified =
             * metaDateModified.first().attr("content"); }
             */

            Elements metaPublisher = doc
                    .select("meta[property=article:publisher]");
            if (!metaPublisher.isEmpty()) {
                publisher = metaPublisher.first().attr("content");
            }

            Elements metaAuthor = doc.select("meta[property=author]");
            if (!metaAuthor.isEmpty()) {
                author = metaAuthor.first().attr("content");
            }
        } catch (IOException e) {
            logger.error(
                    "Caught Exception during crawling Washington Post RSS.", e);
            throw e;
        }
        if (content.toString().equals("")) {
            content.append(entry.getDescription().getValue());
        }
        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(content.toString());
            news.setSource("WASHINGTONPOST");
            news.setTime(sdf.format(entry.getPublishedDate()));
            news.setTitle(entry.getTitle());
            news.setUrl(entry.getLink());
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } else {
            // crawl news info
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.setTitle(entry.getTitle());
            newsInfo.setContent(content.toString());
            newsInfo.setMediaName("WASHINGTONPOST");
            newsInfo.setDatePublished(sdf.format(entry.getPublishedDate()));
            newsInfo.setDateModified(dateModified);
            newsInfo.setAbstractText(description);
            newsInfo.setKeywords(keywords);
            newsInfo.setUrl(entry.getLink());
            newsInfo.setPublisher(publisher);
            newsInfo.setAuthor(author);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                newsInfo.save(ctx);
            }
            return newsInfo.toString();
        }
    }

    // 5 nbcNews
    public static void getNbcNewsRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getNbcNewsDomainRss(ctx, parentExecutor);
    }

    public static void getNbcNewsDomainRss(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        Map<String, SyndEntry> urlMap = new HashMap<String, SyndEntry>();
        createEntryMap(urlList, urlMap, parentExecutor);
        // createEntryMap(nbcNewsUrlList, urlMap, parentExecutor);
        ctx.put("methodName", "analysisNbcNewsPage");
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
                logger.error("Caught Exception during crawling NBC News RSS.",
                        e);
                throw e;
            }
        }
    }

    private String nbcTimeTransfer(String input) {
        return input.replace(".000Z", "").replace(" Z", "");
    }

    public String analysisNbcNewsPage(String url, SyndEntry entry,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws IOException {
        StringBuilder content = new StringBuilder();
        String description = "";
        String keywords = "";
        String dateModified = "";
        String author = "";
        String publisher = "";
        try {
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Elements items = doc.getElementsByClass("stack-l-content");
            if (!items.isEmpty()) {
                Elements paras = items.first().getElementsByTag("p");

                for (Element para : paras) {
                    content.append(para.text()).append(NEWLINE);
                }
            }

            Elements metaDesc = doc.select("meta[name=description]");
            if (!metaDesc.isEmpty()) {
                description = metaDesc.first().attr("content");
            }

            Elements metaKeyword = doc.select("meta[name=news_keywords]");
            if (!metaKeyword.isEmpty()) {
                keywords = metaKeyword.first().attr("content");
            }

            Elements metaDateModified = doc.getElementsByTag("time");
            if (!metaDateModified.isEmpty()) {
                dateModified = nbcTimeTransfer(metaDateModified.first().attr(
                        "datetime"));
            }

            Elements metaPublisher = doc
                    .select("meta[property=article:publisher]");
            if (!metaPublisher.isEmpty()) {
                publisher = metaPublisher.first().attr("content");
            }
        } catch (IOException e) {
            logger.error("Caught Exception during crawling NBC News RSS.", e);
            throw e;
        }
        if (content.toString().equals("")) {
            content.append(entry.getDescription().getValue());
        }
        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(content.toString());
            news.setSource("NBCNEWS");
            news.setTime(sdf.format(entry.getPublishedDate()));
            news.setTitle(entry.getTitle());
            news.setUrl(entry.getLink());
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } else {
            // crawl news info
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.setTitle(entry.getTitle());
            newsInfo.setContent(content.toString());
            newsInfo.setMediaName("NBCNEWS");
            newsInfo.setDatePublished(sdf.format(entry.getPublishedDate()));
            newsInfo.setDateModified(dateModified);
            newsInfo.setAbstractText(description);
            newsInfo.setKeywords(keywords);
            newsInfo.setUrl(entry.getLink());
            newsInfo.setPublisher(publisher);
            newsInfo.setAuthor(author);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                newsInfo.save(ctx);
            }
            return newsInfo.toString();
        }
    }

    // 6 The Times
    public static void getTheTimesRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getTheTimesDomainRss(ctx, parentExecutor);
    }

    private static void createRefList(String[] urlList, List<String> urlItems)
            throws Exception {
        for (String rssUrl : urlList) {
            BufferedReader br = null;
            HttpURLConnection httpcon = null;
            try {
                URL url = new URL(rssUrl);
                httpcon = (HttpURLConnection) url.openConnection();

                InputStream stream = httpcon.getInputStream();
                InputStreamReader isReader = new InputStreamReader(stream);

                // put output stream into a string
                br = new BufferedReader(isReader);

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("<script type=\"text/javascript\">")) {
                        break;
                    }
                    if (line.contains("<link><![CDATA[http://www.thetimes.co.uk/tto/")) {
                        int start = line.indexOf("http");
                        int end = line.indexOf("]]></link>");
                        String ref = line.substring(start, end);
                        urlItems.add(ref);
                    }
                }
            } catch (IOException e) {
                logger.error(
                        "Caught IOException during createRefList for RSS.", e);
                throw e;
            } catch (Exception e) {
                logger.error("Caught Exception during createRefList for RSS.",
                        e);
                throw e;
            } finally {
                if (br != null) {
                    br.close();
                }
                if (httpcon != null) {
                    httpcon.disconnect();
                }
            }
        }
    }

    public static void getTheTimesDomainRss(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        List<String> urlItems = new ArrayList<String>();
        createRefList(urlList, urlItems);
        // createRefList(theTimesUrlList, urlItems);
        ctx.put("methodName", "analysisTheTimesPage");
        ctx.put("parameterTypes", new Class[] { String.class, Map.class,
                ITaskExecutor.class });
        ctx.put("className", className);
        // urlItems size = 0 will cause CycliBarrier
        // java.lang.IllegalArgumentException
        if (parentExecutor != null && !parentExecutor.isCanceled()
                && !urlItems.isEmpty()) {
            try {
                CyclicBarrier parentBarrier = new CyclicBarrier(2);
                TaskBarrierExecutorBuilder.getInstance()
                        .buildWebPageTaskBarrier(urlItems, ctx, parentExecutor,
                                parentBarrier);
                parentBarrier.await();
            } catch (Exception e) {
                logger.error("Caught Exception during crawling The Times RSS.",
                        e);
                throw e;
            }
        }
    }

    public static String timesTransferDate(String input) {
        String year = "";
        Pattern pattern = Pattern.compile("[0-9]{4}");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            year = matcher.group();
        }

        String month = "";
        pattern = Pattern.compile("[A-Za-z]{3}");
        matcher = pattern.matcher(input);
        if (matcher.find(3)) {
            month = matcher.group();
            switch (month) {
            case "Jan":
            case "January":
                month = "01";
                break;
            case "Feb":
            case "February":
                month = "02";
                break;
            case "Mar":
            case "March":
                month = "03";
                break;
            case "Apr":
            case "April":
                month = "04";
                break;
            case "May":
                month = "05";
                break;
            case "Jun":
            case "June":
                month = "06";
                break;
            case "Jul":
            case "July":
                month = "07";
                break;
            case "Aug":
            case "August":
                month = "08";
                break;
            case "Sep":
            case "September":
                month = "09";
                break;
            case "Oct":
            case "October":
                month = "10";
                break;
            case "Nov":
            case "November":
                month = "11";
                break;
            case "Dec":
            case "December":
                month = "12";
                break;
            default:
                break;
            }
        }

        String day = "";
        pattern = Pattern.compile("[0-9]{1,2}");
        matcher = pattern.matcher(input);
        if (matcher.find()) {
            day = matcher.group();
        }

        String hour = "";
        pattern = Pattern.compile("[0-9]{2}:[0-9]{2}");
        matcher = pattern.matcher(input);
        if (matcher.find()) {
            hour = matcher.group();
        } else {
            //give a default time if there is no time information
            hour = "08:00";
        }

        return year + "-" + month + "-" + day + "T" + hour;
    }

    public String analysisTheTimesPage(String url, Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws IOException {
        try {
            if (!LRUCacheUtils.checkIfTraditionalUrlCached(url)
                    || !parentExecutor.isCacheable()) {
                logger.debug("Crawl the The Times url {}.", url);
            } else {
                // Here may need more logic, update time is a question mark?
                logger.info("The url {} has been crawled before.", url);
                return null;
            }

            StringBuilder content = new StringBuilder();
            String description = "";
            String keywords = "";
            String datePublished = "";
            String dateModified = "";
            String author = "";
            String publisher = "";
            String title = "";
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Elements titles = doc.getElementsByTag("h1");
            if (!titles.isEmpty()) {
                title = titles.first().text();
            }

            Elements authors = doc.getElementsByClass("f-author");
            if (!authors.isEmpty()) {
                author = authors.first().text();
            }

            Element item = doc.getElementById("page-1");

            Elements paras = item.getElementsByTag("p");
            if (!paras.isEmpty()) {
                for (Element para : paras) {
                    content.append(para.text()).append(NEWLINE);
                }
            }
            Elements metaDesc = doc.select("meta[name=description]");
            if (!metaDesc.isEmpty()) {
                description = metaDesc.first().attr("content");
            }

            Elements metaDatePublished = doc
                    .select("meta[name=dashboard_published_date]");
            if (!metaDatePublished.isEmpty()) {
                datePublished = metaDatePublished.first().attr("content");
            }
            Elements metaDateModified = doc
                    .select("meta[name=dashboard_updated_date]");
            if (!metaDateModified.isEmpty()) {
                dateModified = metaDateModified.first().attr("content");
            }

            Elements metaPublisher = doc
                    .select("meta[name=dashboard_publisher]");
            if (!metaPublisher.isEmpty()) {
                publisher = metaPublisher.first().attr("content");
            }

            if (parentExecutor.isCacheable()) {
                News news = new News();
                news.setContent(content.toString());
                news.setSource("TIMES");
                news.setTime(timesTransferDate(datePublished));
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
                newsInfo.setContent(content.toString());
                newsInfo.setMediaName("TIMES");
                newsInfo.setDatePublished(timesTransferDate(datePublished));
                newsInfo.setDateModified(timesTransferDate(dateModified));
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
        } catch (IOException e) {
            logger.error("Caught Exception during crawling The Times RSS.", e);
            throw e;
        }
    }

    // 7 The Guardian
    public static void getGuardianRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getGuardianDomainRss(ctx, parentExecutor);
    }

    public static void getGuardianDomainRss(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        Map<String, SyndEntry> urlMap = new HashMap<String, SyndEntry>();
        createEntryMap(urlList, urlMap, parentExecutor);
        // createEntryMap(guardianUrlList, urlMap, parentExecutor);
        ctx.put("methodName", "analysisGuardianPage");
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
                logger.error("Caught Exception during crawling Guardian RSS.",
                        e);
                throw e;
            }
        }
    }

    public String analysisGuardianPage(String url, SyndEntry entry,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws IOException {
        StringBuilder content = new StringBuilder();
        String description = "";
        String keywords = "";
        String dateModified = "";
        String author = "";
        String publisher = "";

        if (!url.contains("http://www.theguardian.com/")) {
            return content.toString();
        }
        try {
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Elements items = doc.getElementsByAttributeValue("data-test-id",
                    "article-review-body");
            if (!items.isEmpty()) {
                Elements paras = items.first().getElementsByTag("p");

                for (Element para : paras) {
                    content.append(para.text()).append(NEWLINE);
                }
            }

            Elements metaDesc = doc.select("meta[name=description]");
            if (!metaDesc.isEmpty()) {
                description = metaDesc.first().attr("content");
            }

            Elements metaKeyword = doc.select("meta[name=keywords]");
            if (!metaKeyword.isEmpty()) {
                keywords = metaKeyword.first().attr("content");
            }

            Elements metaDateModified = doc
                    .select("meta[property=article:modified_time]");
            if (!metaDateModified.isEmpty()) {
                dateModified = metaDateModified.first().attr("content");
            }

            Elements metaAuthor = doc.select("meta[name=author]");
            if (!metaAuthor.isEmpty()) {
                author = metaAuthor.first().attr("content");
            }

            Elements metaPublisher = doc.select("link[rel=publisher]");
            if (!metaPublisher.isEmpty()) {
                publisher = metaPublisher.first().attr("href");
            }

        } catch (IOException e) {
            logger.error(
                    "Caught IOException during crawling the Guardian RSS.", e);
            throw e;
        }
        if (content.toString().equals("")) {
            content.append(entry.getDescription().getValue());
        }

        // crawl news
        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(content.toString());
            news.setSource("THEGARDIAN");
            news.setTime(sdf.format(entry.getPublishedDate()));
            news.setTitle(entry.getTitle());
            news.setUrl(entry.getLink());
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } else {
            // crawl news info
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.setTitle(entry.getTitle());
            newsInfo.setContent(content.toString());
            newsInfo.setMediaName("THEGARDIAN");
            newsInfo.setDatePublished(sdf.format(entry.getPublishedDate()));
            newsInfo.setDateModified(dateModified);
            newsInfo.setAbstractText(description);
            newsInfo.setKeywords(keywords);
            newsInfo.setUrl(entry.getLink());
            newsInfo.setPublisher(publisher);
            newsInfo.setAuthor(author);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                newsInfo.save(ctx);
            }
            return newsInfo.toString();
        }
    }

    // 8 http://www.ap.org/
    public static void getAPRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getAPDomainRss(ctx, parentExecutor);
    }

    public static void getAPDomainRss(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        Map<String, SyndEntry> urlMap = new HashMap<String, SyndEntry>();
        createEntryMap(urlList, urlMap, parentExecutor);
        // createEntryMap(apUrlList, urlMap, parentExecutor);
        ctx.put("methodName", "analysisAPPage");
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
                logger.error("Caught Exception during crawling AP RSS.", e);
                throw e;
            }
        }
    }

    public String analysisAPPage(String url, SyndEntry entry,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws IOException {
        StringBuilder content = new StringBuilder();
        String description = "";
        String keywords = "";
        String dateModified = "";
        String author = "";
        String publisher = "";
        try {
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Elements items = doc.getElementsByClass("ap_para");
            if (!items.isEmpty()) {
                for (Element para : items) {
                    content.append(para.text()).append(NEWLINE);
                }
            }

            Elements metaDateModified = doc.getElementsByClass("dtstamp");
            if (!metaDateModified.isEmpty()) {
                dateModified = metaDateModified.first().attr("title");
            }
        } catch (IOException e) {
            logger.error("Caught IOException during crawling the AP RSS.", e);
            throw e;
        }
        if (content.toString().equals("")) {
            content.append(entry.getDescription().getValue());
        }

        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(content.toString());
            news.setSource("AP");
            news.setTime(sdf.format(entry.getPublishedDate()));
            news.setTitle(entry.getTitle());
            news.setUrl(entry.getLink());
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } else {
            // crawl news info
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.setTitle(entry.getTitle());
            newsInfo.setContent(content.toString());
            newsInfo.setMediaName("AP");
            newsInfo.setDatePublished(sdf.format(entry.getPublishedDate()));
            newsInfo.setDateModified(dateModified);
            newsInfo.setAbstractText(description);
            newsInfo.setKeywords(keywords);
            newsInfo.setUrl(entry.getLink());
            newsInfo.setPublisher(publisher);
            newsInfo.setAuthor(author);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                newsInfo.save(ctx);
            }
            return newsInfo.toString();
        }
    }

    // 9 http://www.chinadaily.com/
    public static void getChinadailyRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getChinadailyDomainRss(ctx, parentExecutor);
    }

    public static void getChinadailyDomainRss(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        Map<String, SyndEntry> urlMap = new HashMap<String, SyndEntry>();
        createEntryMap(urlList, urlMap, parentExecutor);
        // createEntryMap(chinaDailyUrlList, urlMap, parentExecutor);
        ctx.put("methodName", "analysisChinadailyPage");
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
                logger.error(
                        "Caught Exception during crawling Chinadaily RSS.", e);
                throw e;
            }
        }
    }

    private String chinadailyTimeTransfer(String input) {
        String date = "";
        Pattern pattern = Pattern
                .compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            date = matcher.group();
        }
        return date;
    }

    public String analysisChinadailyPage(String url, SyndEntry entry,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws IOException {
        StringBuilder text = new StringBuilder();
        StringBuffer publish_time = new StringBuffer("");
        String description = "";
        String keywords = "";
        String dateModified = "";
        String author = "";
        String publisher = "";
        try {
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            if (doc.getElementById("Title_e") != null) {
                Elements title = doc.getElementById("Title_e").children();
                if (title.size() == 3) {
                    Element time = title.get(2);
                    Pattern pattern = Pattern
                            .compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}");
                    Matcher matcher = pattern.matcher(time.text());
                    if (matcher.find()) {
                        publish_time.append(matcher.group());
                    }
                }
            }
            Element content = doc.getElementById("Content");
            if (content != null) {
                Elements paras = content.getElementsByTag("p");

                for (Element para : paras) {
                    text.append(para.text()).append(NEWLINE);
                }
            }

            Elements metaDesc = doc.select("meta[name=description]");
            if (!metaDesc.isEmpty()) {
                description = metaDesc.first().attr("content");
            }
            Elements metaKeyword = doc.select("meta[name=keywords]");
            if (!metaKeyword.isEmpty()) {
                keywords = metaKeyword.first().attr("content");
            }

            Elements metaDateModified = doc.getElementsByClass("greyTxt6");
            if (!metaDateModified.isEmpty()) {
                dateModified = chinadailyTimeTransfer(metaDateModified.first()
                        .text());
            }
            Elements metaAuthor = doc.select("meta[name=author]");
            if (!metaAuthor.isEmpty()) {
                author = metaAuthor.first().attr("content");
            }

        } catch (IOException e) {
            logger.error(
                    "Caught IOException during crawling the Chinadaily RSS. url -> {}", url, e);
            throw e;
        }
        if (text.toString().equals("")) {
            text.append(entry.getDescription().getValue());
        }
        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(text.toString());
            news.setSource("CHINADAILY");
            news.setTime(publish_time.toString());
            news.setTitle(entry.getTitle());
            news.setUrl(entry.getLink());
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } else {
            // crawl news info
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.setTitle(entry.getTitle());
            newsInfo.setContent(text.toString());
            newsInfo.setMediaName("CHINADAILY");
            newsInfo.setDatePublished(publish_time.toString());
            newsInfo.setDateModified(dateModified);
            newsInfo.setAbstractText(description);
            newsInfo.setKeywords(keywords);
            newsInfo.setUrl(entry.getLink());
            newsInfo.setPublisher(publisher);
            newsInfo.setAuthor(author);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                newsInfo.save(ctx);
            }
            return newsInfo.toString();
        }
    }

    // 10 http://www.reuters.com/
    public static void getReutersRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getReutersDomainRss(ctx, parentExecutor);
    }

    public static void getReutersDomainRss(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        Map<String, SyndEntry> urlMap = new HashMap<String, SyndEntry>();
        createEntryMap(urlList, urlMap, parentExecutor);

        ctx.put("methodName", "analysisReutersPage");
        ctx.put("parameterTypes", new Class[] { String.class, SyndEntry.class,
                Map.class, ITaskExecutor.class });
        ctx.put("className", className);

        if (parentExecutor != null && !parentExecutor.isCanceled()
                && !urlMap.isEmpty()) {
            try {
                CyclicBarrier parentBarrier = new CyclicBarrier(2);
                TaskBarrierExecutorBuilder.getInstance()
                        .buildWebPageTaskBarrier(urlMap, ctx, parentExecutor,
                                parentBarrier);
                parentBarrier.await();
            } catch (Exception e) {
                logger.error("Caught Exception during crawling Reuters RSS.", e);
                throw e;
            }
        }
    }

    public String analysisReutersPage(String url, SyndEntry entry,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws IOException {
        StringBuilder content = new StringBuilder();
        String description = "";
        String keywords = "";
        String dateModified = "";
        String author = "";
        String publisher = "";
        try {
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Element cont = doc.getElementById("articleText");

            Elements paras = cont.getElementsByTag("p");
            if (!paras.isEmpty()) {
                for (Element para : paras) {
                    content.append(para.text()).append(NEWLINE);
                }
            }

            Elements metaDesc = doc.select("meta[name=description]");
            if (!metaDesc.isEmpty()) {
                description = metaDesc.first().attr("content");
            }

            Elements metaKeyword = doc.select("meta[name=keywords]");
            if (!metaKeyword.isEmpty()) {
                keywords = metaKeyword.first().attr("content");
            }

            Elements metaDateModified = doc.select("meta[name=sailthru.date]");
            if (!metaDateModified.isEmpty()) {
                dateModified = metaDateModified.first().attr("content");
            }

            Elements metaAuthor = doc.select("meta[name=author]");
            if (!metaAuthor.isEmpty()) {
                author = metaAuthor.first().attr("content");
            }

            Elements metaPublisher = doc.select("link[rel=publisher]");
            if (!metaPublisher.isEmpty()) {
                publisher = metaPublisher.first().attr("href");
            }
        } catch (IOException e) {
            logger.error("Caught IOException during crawling the REUTERS RSS.",
                    e);
            throw e;
        }
        if (content.toString().equals("")) {
            content.append(entry.getDescription().getValue());
        }

        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(content.toString());
            news.setSource("REUTERS");
            news.setTime(sdf.format(entry.getPublishedDate()));
            news.setTitle(entry.getTitle());
            news.setUrl(entry.getLink());
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } else {
            // crawl news info
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.setTitle(entry.getTitle());
            newsInfo.setContent(content.toString());
            newsInfo.setMediaName("REUTERS");
            newsInfo.setDatePublished(sdf.format(entry.getPublishedDate()));
            newsInfo.setDateModified(dateModified);
            newsInfo.setAbstractText(description);
            newsInfo.setKeywords(keywords);
            newsInfo.setUrl(entry.getLink());
            newsInfo.setPublisher(publisher);
            newsInfo.setAuthor(author);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                newsInfo.save(ctx);
            }
            return newsInfo.toString();
        }
    }

    // 11 http://www.upi.com/
    public static void getUPIRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getUPIDomainRss(ctx, parentExecutor);
    }

    public static void getUPIDomainRss(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        Map<String, SyndEntry> urlMap = new HashMap<String, SyndEntry>();
        createEntryMap(urlList, urlMap, parentExecutor);

        ctx.put("methodName", "analysisUPIPage");
        ctx.put("parameterTypes", new Class[] { String.class, SyndEntry.class,
                Map.class, ITaskExecutor.class });
        ctx.put("className", className);

        if (parentExecutor != null && !parentExecutor.isCanceled()
                && !urlMap.isEmpty()) {
            try {
                CyclicBarrier parentBarrier = new CyclicBarrier(2);
                TaskBarrierExecutorBuilder.getInstance()
                        .buildWebPageTaskBarrier(urlMap, ctx, parentExecutor,
                                parentBarrier);
                parentBarrier.await();
            } catch (Exception e) {
                logger.error("Caught Exception during crawling UPI RSS.", e);
                throw e;
            }
        }
    }

    private String getUpiAuthor(String input) {
        return input.substring(3, input.indexOf("|"));
    }

    public String analysisUPIPage(String url, SyndEntry entry,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws IOException {
        StringBuilder content = new StringBuilder();
        String description = "";
        String keywords = "";
        String dateModified = "";
        String author = "";
        String publisher = "";
        try {
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Elements contents = doc.getElementsByClass("st_text_c");
            if (!contents.isEmpty()) {
                Elements paras = contents.first().getElementsByTag("p");
                if (!paras.isEmpty()) {
                    for (Element para : paras) {
                        content.append(para.text()).append(NEWLINE);
                    }
                }
            }

            Elements metaDesc = doc.select("meta[itemprop=description]");
            if (!metaDesc.isEmpty()) {
                description = metaDesc.first().attr("content");
            }

            Elements metaKeyword = doc.select("meta[name=keywords]");
            if (!metaKeyword.isEmpty()) {
                keywords = metaKeyword.first().attr("content");
            }

            Elements metaPublisher = doc.select("meta[itemprop=publisher]");
            if (!metaPublisher.isEmpty()) {
                publisher = metaPublisher.first().attr("href");
            }

            Elements metaAuthor = doc.getElementsByClass("meta");
            if (!metaAuthor.isEmpty()) {
                author = getUpiAuthor(metaAuthor.first().text());
                logger.debug("author:" + author);
            }

        } catch (IOException e) {
            logger.error("Caught IOException during crawling the UPI RSS.", e);
            throw e;
        }
        if (content.toString().equals("")) {
            content.append(entry.getDescription().getValue());
        }

        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(content.toString());
            news.setSource("UPI");
            news.setTime(sdf.format(entry.getPublishedDate()));
            news.setTitle(entry.getTitle());
            news.setUrl(entry.getLink());
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } else {
            // crawl news info
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.setTitle(entry.getTitle());
            newsInfo.setContent(content.toString());
            newsInfo.setMediaName("UPI");
            newsInfo.setDatePublished(sdf.format(entry.getPublishedDate()));
            newsInfo.setDateModified(dateModified);
            newsInfo.setAbstractText(description);
            newsInfo.setKeywords(keywords);
            newsInfo.setUrl(entry.getLink());
            newsInfo.setPublisher(publisher);
            newsInfo.setAuthor(author);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                newsInfo.save(ctx);
            }
            return newsInfo.toString();
        }
    }

    //12 http://www.bbc.com/
    public static void getBBCRSS(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        getBBCDomainRss(ctx, parentExecutor);
    }

    public static void getBBCDomainRss(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rssUrl = params.getRss();
        String[] urlList = { rssUrl };
        Map<String, SyndEntry> urlMap = new HashMap<String, SyndEntry>();
        createEntryMap(urlList, urlMap, parentExecutor);
        ctx.put("methodName", "analysisBBCPage");
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
                logger.error("Caught Exception during crawling BBC RSS.", e);
                throw e;
            }
        }
    }

    public String analysisBBCPage(String url, SyndEntry entry,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws IOException {
        StringBuilder content = new StringBuilder();
        String description = "";
        String keywords = "";
        String dateModified = "";
        String author = "";
        String publisher = "BBC News";
        try {
            Document doc = Jsoup.connect(url).timeout(Integer.MAX_VALUE).get();

            Elements contents = doc.getElementsByClass("story-body__inner");
            if (!contents.isEmpty()) {
                Elements paras = contents.first().getElementsByTag("p");
                for (Element para : paras) {
                    content.append(para.text()).append(NEWLINE);
                }
            }
            Elements metaDesc = doc.select("meta[name=description]");
            if (!metaDesc.isEmpty()) {
                description = metaDesc.first().attr("content");
            }
            Elements metaAuthor = doc
                    .select("meta[property=og:article:author]");
            if (!metaAuthor.isEmpty()) {
                author = metaAuthor.first().attr("content");
            }

            // Extract the "last_updated" date from the script data
            Elements metaDateModified = doc.select("script[id=news-loader]");
            StringBuilder dateBuilder = new StringBuilder();
            if (!metaDateModified.isEmpty()) {
                String scriptData = metaDateModified.first().data();

                Matcher matcher = patternLastUpdatedDate.matcher(scriptData);
                if (matcher.find()) {
                    String dateStr = matcher.group();

                    Matcher matcherDate = patternDate.matcher(dateStr);
                    String date = "";
                    if (matcherDate.find()) {
                        date = matcherDate.group();
                    }

                    Matcher matcherTimeZone = patternTimeZone.matcher(dateStr);
                    TimeZone timeZone = TimeZone.getTimeZone("Europe/London");
                    if (matcherTimeZone.find()) {
                        timeZone = TimeZone.getTimeZone(matcherTimeZone.group()
                                .split(":")[1]);
                    }

                    dateModified = dateBuilder.append(date.split(" ")[0])
                            .append("T").append(date.split(" ")[1])
                            .append(timeZone.getID()).toString();
                }
            }
            logger.debug("----> Published time {}", entry.getPublishedDate());
            logger.debug("----> Last updated time {}", dateModified);
        } catch (IOException e) {
            logger.error("Caught IOException during crawling the BBC RSS.", e);
            throw e;
        }

        if (content.toString().isEmpty()) {
            if (!entry.getDescription().getValue().isEmpty()) {
                content.append(entry.getDescription().getValue());
            } else {
                content.append(description);
            }
        }

        if (parentExecutor.isCacheable()) {
            News news = new News();
            news.setContent(content.toString());
            news.setSource("BBC News");
            news.setTime(sdf.format(entry.getPublishedDate()));
            news.setTitle(entry.getTitle());
            news.setUrl(entry.getLink());
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                news.save(ctx);
            }
            return news.toString();
        } else {
            // crawl news info
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.setTitle(entry.getTitle());
            newsInfo.setContent(content.toString());
            newsInfo.setMediaName("BBC News");
            newsInfo.setDatePublished(sdf.format(entry.getPublishedDate()));
            newsInfo.setDateModified(dateModified);
            newsInfo.setAbstractText(description);
            newsInfo.setKeywords(keywords);
            newsInfo.setUrl(entry.getLink());
            newsInfo.setPublisher(publisher);
            newsInfo.setAuthor(author);
            if (parentExecutor != null && !parentExecutor.isCanceled()) {
                newsInfo.save(ctx);
            }
            return newsInfo.toString();
        }
    }

    public static enum RomeMediaEnum {
        CNN(0), NEWYORK_TIMES(1), FOX(2), WASHINGTON_POST(3), NBC(4), THE_TIMES(
                5), GUARDIAN(6), AP(7), CHINA_DAILY(8), BBC(9), REUTERS(10), UPI(11), OTHER(99);

        private final int index;

        private RomeMediaEnum(int index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return Integer.toString(index);
        }

        public int getInt() {
            return index;
        }
    }

    public static RomeMediaEnum getMediaEnumByRss(String rss) {
        RomeMediaEnum mediaEnum = RomeMediaEnum.OTHER;
        for (Map.Entry<RomeMediaEnum, Pattern> entry : mediaEnumMap.entrySet()) {
            if (checkRssPatternMatches(rss, entry.getValue())) {
                mediaEnum = entry.getKey();
                logger.debug("Match media: {}, rss url {}", mediaEnum, rss);
                return mediaEnum;
            }
        }
        logger.debug("None of pattern  Matched for rss url {}", rss);
        return mediaEnum;
    }

    public static boolean checkRssPatternMatches(String rss, Pattern pattern) {
        Matcher matcher = pattern.matcher(rss);

        if (!matcher.find()) {
            return false;
        } else {
            logger.debug(
                    "Match Group count :  {} ; Match Group : {} ; Matcher start : {} ; Matcher end : {} ; RSS : {}.",
                    matcher.groupCount(), matcher.group(), matcher.start(),
                    matcher.end(), rss);
            return true;
        }
    }

    public static void main(String... args) throws Exception {
        System.setProperty("http.proxyHost", "proxy.pal.sap.corp");
        System.setProperty("http.proxyPort", "8080");
        System.out.println("starting...");
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService  taskExecutorPool = new TaskExecutorPool(1000, 1000, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),factory);
        Main.setTaskExecutorPool(taskExecutorPool);
        // get_reuters_list("040815");
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("task_key", "http://rss.upi.com/news/news.rss");
        MediaKey mediaKey = new MediaKey("Traditional", "TraditionalArticle",
                "WebPage");
        TaskParam param = new TaskParam(params, mediaKey);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.TraditionalMedia_ArticleData_ByWebPage,
                        "cnn", param);
        ctx.put("task", task);
        // TraditionalWebPageExecutor exe = new TraditionalWebPageExecutor();
        RomeCrawler exe = new RomeCrawler();
        exe.setTask(task);
        exe.setTestFlagOn(true);
        exe.setCacheable(false);
        List<String> result = new ArrayList<String>();
        ctx.put("result", result);
        getUPIRSS(ctx, exe);
        // getReutersRSS(ctx, exe);
        // getChinadailyRSS(ctx, exe);
        // getAPRSS(ctx, exe);
        // getNbcNewsRSS(ctx, exe);
        // getWashingtonPostRSS(ctx, exe);
        // getNewYorkTimesRSS(ctx, exe);
        // getFoxRSS(ctx, exe);
        // getTheTimesRSS(ctx, exe);
        // getGuardianRSS(ctx, exe);
        // getCNNRSS(ctx, exe);
        // getUpiList(ctx);
        // analysisAFPMainPage(ctx);
        System.out.println("end.");

    }
}
