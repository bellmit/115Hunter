package com.sap.cisp.xhna.data.executor.twitter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import com.google.common.base.Stopwatch;
import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.common.DateUtil;
import com.sap.cisp.xhna.data.common.index.LuceneUtils;
import com.sap.cisp.xhna.data.common.language.LanguageDetectorService;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TokenManager;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class TwitterArticleKeywordExecutor extends AbstractTaskExecutor {
    private static Logger log = LoggerFactory
            .getLogger(TwitterArticleKeywordExecutor.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss+0000");
    private static final SimpleDateFormat sdfDate = new SimpleDateFormat(
            "yyyy-MM-dd");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private Date from;
    private Date to;
    private IToken token;

    // For Junit test
    static {
        sdf.setTimeZone(GMT);
    }

    @Override
    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception {
        ITask task = (ITask) ctx.get("task");
        TaskParam taskParam = task.getParam();

        String keyword = taskParam.getKeyword();
        String startTime = taskParam.getStartTime();
        String endTime = taskParam.getEndTime();
        return getResultsByKeyword(ctx, keyword, startTime, endTime, null);
    }

    protected List<String> getResultsByKeyword(Map<String, Object> ctx,
            String keyword, String startTime, String endTime,
            List<Status> originalTweets) throws Exception {
        List<String> results = new ArrayList<String>();
        ConcurrentHashMap<String, String> resultMap = new ConcurrentHashMap<String, String>();

        if (keyword.length() < 1) {
            // System.out.println("input keyword is null");
            log.error("input keyword is null");
            throw new DataCrawlException("input keyword is null");
        }

        if (startTime == null || endTime == null
                || startTime.equalsIgnoreCase("")
                || endTime.equalsIgnoreCase("")) {
            logger.error("Get date limit error");
            throw new DataCrawlException(
                    "Date limit(start time/end time) is null or empty.");
        } else {
            log.info("start_time:" + startTime);
            log.info("end_time:" + endTime);
            Date fromDate = DateUtil.stringToDate(startTime,
                    "yyyy-MM-dd HH:mm:ss");
            Date toDate = DateUtil.stringToDate(endTime, "yyyy-MM-dd HH:mm:ss");
            from = DateUtil.parseDate(sdf.format(fromDate));
            to = DateUtil.parseDate(sdf.format(toDate));
            logger.info("From date {} To date {}", from, to);
        }

        log.info("Crawl Tweets of Keyword:" + keyword);
        // google guava stopwatch, to caculate the execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();

        String lang = LanguageDetectorService.getInstance()
                .getDetectedLanguage(keyword);
        int count = 0;
        Query query = new Query(keyword);
        query.setCount(100);
        // Can use Since and Util to reduce data request.
        // Limitation : only provide date, and only count from 08:00AM -
        // yesterday's 8:00AM
        logger.debug("Set twitter Since date: {} ; Util Date: {}",
                getSinceDate(startTime), getUtilDate(endTime));
        query.setSince(getSinceDate(startTime));
        query.setUntil(getUtilDate(endTime));
        QueryResult result = null;
        Twitter twitter = null;
        int tweets_num = 0;
        int retryNum = 0;
        long lastID = 0;
        boolean isPostAfterStartTime = true;
        // Not begin with @
        // Option 1) Start with one or more space/tab, and can has a ' or "
        // before keyword (\\b is mandatory)
        // or
        // Option 2) Start with the keyword and together with \\b, can be end
        // with none or more ,.\n\r\t or space
        // Option 3) Start with hash tag #, +, ', "
        String patternStr = "(?:(?!^@)([ \t]+)|^|#|\"|\'|\\+)\\b" + keyword
                + "\\b[ ,.\n\r\t]*";
        // Less compile
        Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE
                | Pattern.DOTALL);
        do {
            if (isCanceled)
                return null;
            /**
             * There is a 90s timeout if no available token, getToken return
             * null
             */
            // For the use case(work mode with server), since get/release token
            // is locally, it's thread-safe. But in case there are bunch of tasks flooding in,
            // the executor pool may got blocked. So currently in all cases will add synchronization.
            if (ConfigInstance.runInLocal()) {
                token = TokenManager.getInstance().getToken(TokenType.Twitter);
                if (token == null) {
                    logger.error("Get token error");
                    // First time to get token error, just break
                    if ((!isCacheable() && results.isEmpty())
                            || resultMap.isEmpty()) {
                        // Throw interrupedException to cancel
                        throw new InterruptedException(
                                "Failed to get Twitter token at the beginning! Try later.");
                    } else {
                        // save current results then quit
                        if (!isTestFlagOn()) {
                            if (isCacheable()) {
                                for (Map.Entry<String, String> entry : resultMap
                                        .entrySet()) {
                                    String value = entry.getValue();
                                    results.add(value);
                                }
                            }
                            save(ctx, results);
                        }
                        results.clear();
                        resultMap.clear();
                        throw new InterruptedException(
                                "Failed to get Twitter token during crawling! Try later.");
                    }
                } else {
                    logger.info("Get Token -> {}", token);
                }

                try {
                    twitter = TwitterSingleton.getInstance().getSearchInstance(
                            token);
                    result = twitter.search(query);
                    if (result == null) {
                        log.error("api can't search this keyword:" + keyword);
                        throw new DataCrawlException(
                                "Twitter api can't search this keyword:"
                                        + keyword);
                    }
                } catch (TwitterException e) {
                    TokenManager.getInstance().releaseToken(token,
                            e.getStatusCode(), e);
                    logger.error(
                            "Caught TwitterException. Continue to try other tokens to proceed.",
                            e);
                    if (retryNum < RETRY_THRESHOLD) {
                        retryNum++;
                        continue;
                    } else {
                        logger.error(
                                "Failed to crawl twitter by Keyword {} with retry ({}) times.",
                                keyword, retryNum);
                        throw new DataCrawlException(
                                "Failed to crawl twitter by Keyword " + keyword
                                        + " with retry (" + retryNum
                                        + ") times.");
                    }
                } finally {
                    if (token.getState() == StateEnum.Unavailable) {
                        // Release token normally
                        logger.info("Release Token normally -> {}", token);
                        TokenManager.getInstance().releaseToken(token, 0);
                    }
                }
            } else {
                /**
                 * For distribution deployment, since get/release is remotely
                 * and asynchronously, gearman request is not thread-safe, must
                 * ensure the get/release pair operation integrity. Error case:
                 * - A lot of executors are started in parallel, bunch of get
                 * token requests sent to gearman server, the release token
                 * request can not be handled in time(executor service, all the
                 * executors then be stuck. - Since IToken is not thread-safe,
                 * when get/release token asynchronously, setState/getState is
                 * error prone. Keep token without lock to make local
                 * get/release token operation more efficient.
                 */
                synchronized (ConfigInstance.TWITTER_LOCK) {
                    token = TokenManager.getInstance().getToken(
                            TokenType.Twitter);
                    if (token == null) {
                        logger.error("Get token error");
                        // First time to get token error, just break
                        if ((!isCacheable() && results.isEmpty())
                                || resultMap.isEmpty()) {
                            // Throw interrupedException to cancel
                            throw new InterruptedException(
                                    "Failed to get Twitter token at the beginning! Try later.");
                        } else {
                            // save current results then quit
                            if (!isTestFlagOn()) {
                                if (isCacheable()) {
                                    for (Map.Entry<String, String> entry : resultMap
                                            .entrySet()) {
                                        String value = entry.getValue();
                                        results.add(value);
                                    }
                                }
                                save(ctx, results);
                            }
                            results.clear();
                            resultMap.clear();
                            throw new InterruptedException(
                                    "Failed to get Twitter token during crawling! Try later.");
                        }
                    } else {
                        logger.info("Get Token -> {}", token);
                    }

                    try {
                        twitter = TwitterSingleton.getInstance()
                                .getSearchInstance(token);
                        result = twitter.search(query);
                        if (result == null) {
                            log.error("api can't search this keyword:"
                                    + keyword);
                            throw new DataCrawlException(
                                    "Twitter api can't search this keyword:"
                                            + keyword);
                        }
                    } catch (TwitterException e) {
                        TokenManager.getInstance().releaseToken(token,
                                e.getStatusCode(), e);
                        logger.error(
                                "Caught TwitterException. Continue to try other tokens to proceed.",
                                e);
                        if (retryNum < RETRY_THRESHOLD) {
                            retryNum++;
                            continue;
                        } else {
                            logger.error(
                                    "Failed to crawl twitter by Keyword {} with retry ({}) times.",
                                    keyword, retryNum);
                            throw new DataCrawlException(
                                    "Failed to crawl twitter by Keyword "
                                            + keyword + " with retry ("
                                            + retryNum + ") times.");
                        }
                    } finally {
                        if (token.getState() == StateEnum.Unavailable) {
                            // Release token normally
                            logger.info("Release Token normally -> {}", token);
                            TokenManager.getInstance().releaseToken(token, 0);
                        }
                    }
                }
            }
            List<Status> tweets = result.getTweets();
            //There is no more tweets to be fetched
            if(tweets.isEmpty()) {
                break;
            }
            Date postTime = null;
            String text = "";
            for (Status tweet : tweets) {
                count++;
                lastID = tweet.getId();
                postTime = DateUtil.parseDate(sdf.format(tweet.getCreatedAt()));
                 logger.debug("Post time -------> {}, Tweet ID ------> {}", postTime,lastID);
                if (postTime.before(from)) {
                    logger.debug("Post time {}  before =====> {}", postTime,
                            from);
                    isPostAfterStartTime = false;
                    break;
                }
                if (postTime.after(to)) {
                    continue;
                }

                String str = TwitterObjectFactory.getRawJSON(tweet);

                if (originalTweets != null) {
                    // Called from post info crawler, no need to enable cache
                    setCacheable(false);
                    originalTweets.add(tweet);
                } else {
                    text = tweet.getText();
                    // Currently only filter the English tweet
                    if (lang.equalsIgnoreCase("en")
                            && checkTweetOnlyAccountContainsKeyword(text,
                                    pattern)) {
                        continue;
                    }
                    if (isCacheable()) {
                        resultMap.putIfAbsent(lastID + "", str);
                    } else {
                        results.add(str);
                    }
                }
                tweets_num++;
            }

            query = result.nextQuery();
            //Fix the issue: when search keywords China, the nextQuery return null soon. 
            //Solution: Reset the query to return the old tweets less than the given ID, since recent tweets will be returned first.
            if (query == null) {
                log.warn("!!! The nextQuery return null, need to reset the query manually !!!, lastID --> {}", lastID);
                query = new Query(keyword);
                query.setCount(100);
                query.setMaxId(lastID - 1);
            }
            log.info(keyword + "/" + "Tweets SUM:" + count
                    + "/tweets to hive(duplication to be reduced):"
                    + tweets_num);
            log.info("Check query is null or not ---> {}, count --> {}", query == null, result.getCount());
        } while (query != null && isPostAfterStartTime);

        // Reduce duplicated tweets in HIVE
        if (isCacheable()) {
            LuceneUtils.getUnCachedPosts(LuceneUtils.MediaIndexPath.Twitter,
                    resultMap, results);
        }

        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        if (isCacheable()) {
            log.info(
                    "Schedule done crawl Tweets: {} ;  Tweets searched count: {}; Tweets to HIVE Count: {} ; Elapsed Time(seconds): {}",
                    keyword, tweets_num, results.size(), nanos);
        } else {
            log.info(
                    "Crawl account post info. Step 2: Crawl keyword {}  Count -> {}, Elapsed Time(Seconds) -> {}",
                    keyword, tweets_num, nanos);
        }
        return isCanceled ? null : results;
    }

    public boolean checkTweetOnlyAccountContainsKeyword(String text,
            Pattern pattern) {
        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            // keyword may only occurs in account name
            return true;
        } else {
            logger.debug(
                    "Match Group count :  {} ; Match Group : {} ; Matcher start : {} ; Matcher end : {} ; Text : {}.",
                    matcher.groupCount(), matcher.group(), matcher.start(),
                    matcher.end(), text);
            return false;
        }
    }

    /**
     * Get the since date for query API
     * 
     * @param startTime
     *            - "yyyy-MM-dd HH:mm:ss"
     * @return Since - "yyyy-MM-dd"
     * @throws ParseException
     */
    public String getSinceDate(String startTime) throws ParseException {
        Date fromDate = DateUtil.stringToDate(startTime, "yyyy-MM-dd HH:mm:ss");
        Date from = DateUtil.parseDate(sdf.format(fromDate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(from);
        if (cal.get(Calendar.HOUR_OF_DAY) < 8) {
            cal.add(Calendar.DATE, -1);
        }
        // sdfDate can not be set timezone as GMT again
        return sdfDate.format(cal.getTime());
    }

    /**
     * Get the Util date for query API
     * 
     * @param endTime
     *            - "yyyy-MM-dd HH:mm:ss"
     * @return Util - "yyyy-MM-dd"
     * @throws ParseException
     */
    public String getUtilDate(String endTime) throws ParseException {
        Date toDate = DateUtil.stringToDate(endTime, "yyyy-MM-dd HH:mm:ss");
        Date to = DateUtil.parseDate(sdf.format(toDate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(to);
        if (cal.get(Calendar.HOUR_OF_DAY) >= 8) {
            cal.add(Calendar.DATE, +1);
        }
        // sdfDate can not be set timezone as GMT again
        return sdfDate.format(cal.getTime());
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        TokenManager.getInstance().init();
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("task_key", "China");
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.DATE, -0);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.DATE, -1);
        String from = sdf.format(currentCal.getTime());
        params.put("start_time", from);
        params.put("end_time", to);
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByKeyword,
                        "Twitter", param);
        ctx.put("task", task);
        TwitterArticleKeywordExecutor exe = new TwitterArticleKeywordExecutor();
        exe.setTestFlagOn(true);
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService taskExecutorPool = new TaskExecutorPool(10, 10, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), factory);
        exe.setTask(task);
        for (int i = 0; i < 1; i++) {
            try {
                List<String> result = (List<String>) taskExecutorPool.submit(
                        exe).get();
                logger.debug("Result --> {}", result.size());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
}
