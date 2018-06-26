package com.sap.cisp.xhna.data.executor.twitter;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Paging;
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
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TokenManager;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class TwitterArticleAccountExecutor extends AbstractTaskExecutor {

    private static Logger log = LoggerFactory
            .getLogger(TwitterArticleAccountExecutor.class);
    private IToken token;
    private static final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss+0000");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private Date from;
    private Date to;

    // For Junit test
    static {
        sdf.setTimeZone(GMT);
    }

    @Override
    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception {
        ITask task = (ITask) ctx.get("task");
        TaskParam taskParam = task.getParam();
        String account = taskParam.getAccount();
        String startTime = taskParam.getStartTime();
        String endTime = taskParam.getEndTime();
        return getResultsByAccount(ctx, account, startTime, endTime, null);
    }

    protected List<String> getResultsByAccount(Map<String, Object> ctx,
            String account, String startTime, String endTime,
            List<Status> originalTweets) throws Exception {
        final List<String> results = new ArrayList<String>();
        ConcurrentHashMap<String, String> resultMap = new ConcurrentHashMap<String, String>();

        if (account == null || account.equalsIgnoreCase("")) {
            logger.error("Get account error");
            throw new DataCrawlException("Account is null or empty.");
        } else {
            logger.info("Account:{}", account);
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
        // google guava stopwatch, to caculate the execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();

        Twitter twitter = null;
        List<Status> statuses = null;
        String user;
        long maxid = -1;
        int count = 0;
        boolean isPostAfterStartTime = true;
        int retryNum = 0;
        do {
            if (isCanceled)
                return null;
            // There is a 1 minutes timeout if no available token, getToken
            // return null
            // For the use case(work mode with server), since get/release token
            // is locally, thread-safe without synchronized
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

                user = account;
                Paging page = new Paging();
                page.setCount(200);
                if (maxid > 0) {
                    page.setMaxId(maxid - 1);
                }

                try {
                    twitter = TwitterSingleton.getInstance().getSearchInstance(
                            token);
                    statuses = twitter.getUserTimeline(user, page);
                    if (statuses == null) {
                        log.error("api can't search this account:" + user);
                        throw new DataCrawlException(
                                "api can't search this account:" + user);
                    } else if (statuses.isEmpty()) {
                        logger.debug("No more tweets. Break the query loop.");
                        break;
                    }
                } catch (TwitterException e) {
                    TokenManager.getInstance().releaseToken(token,
                            e.getStatusCode(), e);
                    logger.error(
                            "Caught TwitterException. Continue to try other tokens to proceed.",
                            e);
                    if (retryNum < RETRY_THRESHOLD) {
                        logger.debug("Retry Num ----> {}", retryNum);
                        retryNum++;
                        continue;
                    } else {
                        logger.error(
                                "Failed to crawl twitter by Account {} with retry ({}) times.",
                                user, retryNum);
                        throw new DataCrawlException(
                                "Failed to crawl twitter by Account " + user
                                        + " with retry (" + retryNum
                                        + ") times.");
                    }
                } finally {
                    if (token.getState() == StateEnum.Unavailable) {
                        // Release token normally
                        TokenManager.getInstance().releaseToken(token, 0);
                    }
                }
            } else {
                /**
                 * For distribution deployment, since get/release is remotely
                 * and asynchronously, gearman request is not thread-safe, must
                 * ensure the get/release pair operation integrity. One error
                 * case: A lot of executors are started in parallel, bunch of
                 * get token requests sent to gearman server, the release token
                 * request can not be handled in time, all the executors then be
                 * stuck.
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

                    user = account;
                    Paging page = new Paging();
                    page.setCount(200);
                    if (maxid > 0) {
                        page.setMaxId(maxid - 1);
                    }

                    try {
                        twitter = TwitterSingleton.getInstance()
                                .getSearchInstance(token);
                        statuses = twitter.getUserTimeline(user, page);
                        if (statuses == null) {
                            log.error("api can't search this account:" + user);
                            throw new DataCrawlException(
                                    "api can't search this account:" + user);
                        } else if (statuses.isEmpty()) {
                            logger.debug("No more tweets. Break the query loop.");
                            break;
                        }
                    } catch (TwitterException e) {
                        TokenManager.getInstance().releaseToken(token,
                                e.getStatusCode(), e);
                        logger.error(
                                "Caught TwitterException. Continue to try other tokens to proceed.",
                                e);
                        if (retryNum < RETRY_THRESHOLD) {
                            logger.debug("Retry Num ----> {}", retryNum);
                            retryNum++;
                            continue;
                        } else {
                            logger.error(
                                    "Failed to crawl twitter by Account {} with retry ({}) times.",
                                    user, retryNum);
                            throw new DataCrawlException(
                                    "Failed to crawl twitter by Account "
                                            + user + " with retry (" + retryNum
                                            + ") times.");
                        }
                    } finally {
                        if (token.getState() == StateEnum.Unavailable) {
                            // Release token normally
                            TokenManager.getInstance().releaseToken(token, 0);
                        }
                    }
                }
            }

            for (Status status : statuses) {
                maxid = status.getId();
                String str = TwitterObjectFactory.getRawJSON(status);
                Date postTime = DateUtil.parseDate(sdf.format(status
                        .getCreatedAt()));
                if (postTime.before(from)) {
                    logger.debug("Post time {} before =====> {}", postTime,
                            from);
                    isPostAfterStartTime = false;
                    break;
                }
                if (postTime.after(to)) {
                    continue;
                }
                count++;
                if (originalTweets != null) {
                    // Called from post info crawler, no need to enable cache
                    setCacheable(false);
                    originalTweets.add(status);
                } else {
                    if (isCacheable()) {
                        resultMap.putIfAbsent(maxid + "", str);
                    } else {
                        results.add(str);
                    }
                }
            }
        } while (isPostAfterStartTime);

        // Reduce duplicated tweets in HIVE
        if (isCacheable()) {
            LuceneUtils.getUnCachedPosts(LuceneUtils.MediaIndexPath.Twitter,
                    resultMap, results);
        }

        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        if (isCacheable()) {
            log.info(
                    "Crawl account -> {}; UserPosts Count -> {}; Tweets to HIVE Count: {} ;Elapsed Time(Seconds) -> {}",
                    account, count, results.size(), nanos);
        } else {
            log.info(
                    "Crawl account {} post info. Step 1: Crawl UserPosts Count -> {}, Elapsed Time(Seconds) -> {}",
                    account, count, nanos);
        }
        return isCanceled ? null : results;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        Map<String, Object> ctx = new HashMap<String, Object>();
        TokenManager.getInstance().init();
        Map<String, String> params = new HashMap<String, String>();
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.HOUR, 0);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.DATE, -25);
        String from = sdf.format(currentCal.getTime());
        params.put("task_key", "bbc");
        params.put("start_time", from);
        params.put("end_time", to);
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByAccount,
                        "Twitter", param);
        ctx.put("task", task);
        TwitterArticleAccountExecutor exe = new TwitterArticleAccountExecutor();
//        exe.setTestFlagOn(true);
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService taskExecutorPool = new TaskExecutorPool(10, 10, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                factory);
        exe.setTask(task);
        try {
            List<String> result = (List<String>) taskExecutorPool.submit(
                    exe).get();
            logger.info("Crawl result =====> " + result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.exit(0);
    }

}
