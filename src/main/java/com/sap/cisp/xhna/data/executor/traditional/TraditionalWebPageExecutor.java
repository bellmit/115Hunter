package com.sap.cisp.xhna.data.executor.traditional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.cisp.xhna.data.Main;
import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.executor.forum.SemiWikiForumCrawler;
import com.sap.cisp.xhna.data.executor.lottery.GD11in5Crawler;
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.executor.forum.stock.GubaAccountInfoCrawler;
import com.sap.cisp.xhna.data.executor.forum.stock.GubaCrawler;

public class TraditionalWebPageExecutor extends AbstractTaskExecutor {
    public static Map<WebPageMediaEnum, Pattern> mediaEnumMap = new HashMap<WebPageMediaEnum, Pattern>();
    // AFP pattern
    private static final String patternAFPStr = "(?:(^http://www.afp.com.*))";
    private static final Pattern patternAFP = Pattern.compile(patternAFPStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // DPA pattern
    private static final String patternDPAStr = "(?:(^http://www.dpa-international.com/news/.*/))";
    private static final Pattern patternDPA = Pattern.compile(patternDPAStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // Chicago Tribune pattern
    private static final String patternChicagoTribuneStr = "(?:(^http://www.chicagotribune.com.*))";
    private static final Pattern patternChicagoTribune = Pattern
            .compile(patternChicagoTribuneStr, Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);
    // Chicago Tribune pattern
    private static final String patternSemiWikiForumStr = "(?:(^https://www.semiwiki.com/forum/.*))";
    private static final Pattern patternSemiWikiForum = Pattern
            .compile(patternSemiWikiForumStr, Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);

    //Guba pattern
    private static final String patternGubaStr = "(?:(^http://guba.eastmoney.com.*/))";
    private static final Pattern patternGuba = Pattern.compile(patternGubaStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final String patternGubaAccountStr = "(?:(^http://passport.eastmoney.com/ajax.*/))";
    private static final Pattern patternGubaAccount = Pattern.compile(patternGubaAccountStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    //Lottery pattern
    private static final String patternGD11in5Str = "(?:(^http://www.gdlottery.cn/.*))";
    private static final Pattern patternGD11in5 = Pattern.compile(patternGD11in5Str, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    static {
        initMediaEnumMap();
    }

    private static void initMediaEnumMap() {
        mediaEnumMap.putIfAbsent(WebPageMediaEnum.AFP, patternAFP);
        mediaEnumMap.putIfAbsent(WebPageMediaEnum.DPA, patternDPA);
        mediaEnumMap.putIfAbsent(WebPageMediaEnum.CHICAGO_TRIBUNE,
                patternChicagoTribune);
        mediaEnumMap.putIfAbsent(WebPageMediaEnum.SEMIWIKI,
                patternSemiWikiForum);
        mediaEnumMap.putIfAbsent(WebPageMediaEnum.GUBA, patternGuba);
        mediaEnumMap.putIfAbsent(WebPageMediaEnum.GUBA_ACCOUNT, patternGubaAccount);

        mediaEnumMap.putIfAbsent(WebPageMediaEnum.GD11in5, patternGD11in5);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> execute(Map<String, Object> ctx) throws Exception {
        ctx.put("result", new ArrayList<String>());
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String url = params.getUrl();
        logger.debug("Webpage URL --> {}", url);
        try {
            ConfigInstance.TRADITIONAL_SEMAPHORE.acquire();
            WebPageMediaEnum mediaEnum = getMediaEnumByURL(url);
            logger.info("The Media enum is : {}", mediaEnum);
            switch (mediaEnum) {
                case AFP:
                    GetNews.analysisAFPMainPage(ctx, this);
                    break;
                case DPA:
                    NewsCrawler.crawl(ctx, this);
                    break;
                case CHICAGO_TRIBUNE:
                    GetNews.analysisChicagotribuneMainPage(ctx, this);
                    break;
                case SEMIWIKI:
                    SemiWikiForumCrawler exe = new SemiWikiForumCrawler();
                    exe.getSemiWikiForums(ctx, this);
                    break;
                case GUBA:
                    GubaCrawler guba_exe = new GubaCrawler();
                    guba_exe.getGubaPosts(ctx, this);
                    break;
                case GUBA_ACCOUNT:
                    GubaAccountInfoCrawler guba_account_exe = new GubaAccountInfoCrawler();
                    guba_account_exe.getGubaAccountInfo(ctx, this);
                    break;
                case GD11in5:
                    GD11in5Crawler gdCrawler = new GD11in5Crawler();
                    gdCrawler.getLotteryResultHistory(ctx, this);
                    break;
                default:
                    throw new DataCrawlException(
                            "Unsupported Webpage URL. Need new implementation.");
            }
        } catch (Exception e) {
            logger.error("Caught Exception during crawling Web page article.",
                    e);
            throw e;
        } finally {
            ConfigInstance.TRADITIONAL_SEMAPHORE.release();
        }
        return isCanceled ? null : (List<String>) ctx.get("result");
    }

    public static enum WebPageMediaEnum {
        AFP(0), DPA(1), CHICAGO_TRIBUNE(2), SEMIWIKI(3), GUBA(4), GUBA_ACCOUNT(5), GD11in5(6), OTHER(99);

        private final int index;

        private WebPageMediaEnum(int index) {
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

    public static WebPageMediaEnum getMediaEnumByURL(String url) {
        WebPageMediaEnum mediaEnum = WebPageMediaEnum.OTHER;
        for (Map.Entry<WebPageMediaEnum, Pattern> entry : mediaEnumMap
                .entrySet()) {
            if (checkURLPatternMatches(url, entry.getValue())) {
                mediaEnum = entry.getKey();
                logger.debug("Match media: {}, webpage url {}", mediaEnum, url);
                return mediaEnum;
            }
        }
        logger.debug("None of pattern  Matched for webpage url {}", url);
        return mediaEnum;
    }

    public static boolean checkURLPatternMatches(String url, Pattern pattern) {
        Matcher matcher = pattern.matcher(url);

        if (!matcher.find()) {
            return false;
        } else {
            logger.debug(
                    "Match Group count :  {} ; Match Group : {} ; Matcher start : {} ; Matcher end : {} ; URL : {}.",
                    matcher.groupCount(), matcher.group(), matcher.start(),
                    matcher.end(), url);
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        System.setProperty("http.proxyHost", "proxy.pvgl.sap.corp");
        System.setProperty("http.proxyPort", "8080");
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService taskExecutorPool = new TaskExecutorPool(1000, 1000, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                factory);
        Main.setTaskExecutorPool(taskExecutorPool);
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        //params.put("media_name", "semiwiki");
        params.put("media_name", "GD11in5");
        params.put("task_key", "http://www.gdlottery.cn/odata/zst11xuan5.jspx?method=to11x5kjggzst&date=2015-01-01");
//        params.put("task_key", "https://www.semiwiki.com/forum/");
//    	params.put("task_key", "http://passport.eastmoney.com/ajax/");
        /*the MediaKey three value decide the hive save path read from mapping table*/
        MediaKey mediaKey = new MediaKey("GD", "Forum", "");
        TaskParam param = new TaskParam(params, mediaKey);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.TraditionalMedia_ArticleData_ByWebPage,
                        "GD11in5", param);
        ctx.put("task", task);
        TraditionalWebPageExecutor exe = new TraditionalWebPageExecutor();
        exe.setTask(task);
        exe.setTestFlagOn(true);
        List<String> result = null;
        for (int i = 0; i < 2; i++) {
            try {
                result = (List<String>) taskExecutorPool.submit(exe).get();
                //logger.debug("Result --> {}", result);
                //Thread.sleep(500);
                //taskExecutorPool.submit(exe);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
