package com.sap.cisp.xhna.data.executor.facebook;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.common.DateUtil;
import com.sap.cisp.xhna.data.common.SendHttp;
import com.sap.cisp.xhna.data.common.Util;
import com.sap.cisp.xhna.data.common.index.LuceneUtils;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;
import com.sap.cisp.xhna.data.token.TokenManager;

public class FacebookArticleAccountExecutor extends AbstractTaskExecutor {
    private static Logger logger = LoggerFactory
            .getLogger(FacebookArticleAccountExecutor.class);
    private String URL_PREFX_FORMAT = "https://graph.facebook.com/%s/posts?access_token=%s&until=%d&since=%d";
    private String LIKE_COUNT_URL_FORMAT = "https://graph.facebook.com/%s/likes?summary=true&access_token=%s";
    private String COMMENT_COUNT_URL_FORMAT = "https://graph.facebook.com/%s/comments?summary=true&access_token=%s";
    private List<String> resultList = new LinkedList<String>();
    private ConcurrentHashMap<String, String> resultMap = new ConcurrentHashMap<String, String>();
    private Date from;
    private Date to;
    private String account;
    private SimpleDateFormat facebookTimeFmt = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss+0000");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private IToken token;
    private AtomicInteger count = new AtomicInteger(0);
    private static String patternStr = "(?:(access_token=.*&\\b)(?!_))";
    // Less compile
    private static Pattern pattern = Pattern.compile(patternStr,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception {
        resultList.clear();
        resultMap.clear();
        count.set(0);
        logger.info("Begin to get posts by account");
        ITask task = (ITask) ctx.get("task");
        account = task.getParam().getAccount();
        String fromStr = task.getParam().getStartTime();
        String toStr = task.getParam().getEndTime();

        if (account == null || account.equalsIgnoreCase("")) {
            logger.error("Get account error");
            throw new DataCrawlException("Account is null or empty.");
        } else {
            logger.info("Account:{}", account);
        }

        if (fromStr == null || toStr == null || fromStr.equalsIgnoreCase("")
                || toStr.equalsIgnoreCase("")) {
            logger.error("Get date limit error");
            throw new DataCrawlException(
                    "Date limit(start time/end time) is null or empty.");
        } else {
            logger.info("From {} To {}", fromStr, toStr);
        }
        facebookTimeFmt.setTimeZone(GMT);
        Date fromDate = DateUtil.stringToDate(fromStr, "yyyy-MM-dd HH:mm:ss");
        Date toDate = DateUtil.stringToDate(toStr, "yyyy-MM-dd HH:mm:ss");
        from = DateUtil.parseDate(facebookTimeFmt.format(fromDate));
        to = DateUtil.parseDate(facebookTimeFmt.format(toDate));
        logger.info("From date {} To date {}", from, to);

        // google guava stopwatch, to caculate the execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();

        // Crawl from each account
        String currentUrl = "";
        int retryNum = 0;
        do {
            if (isCanceled)
                return null;
            // There is a 1 minutes timeout if no available token, getToken
            // return null
            // For the use case(work mode with server), since get/release token
            // is locally, thread-safe without synchronized
            if (ConfigInstance.runInLocal()) {
                token = TokenManager.getInstance().getToken(TokenType.Facebook);
                if (token == null) {
                    logger.error("Get token error");
                    // First time to get token error, just break
                    if ((!isCacheable() && resultList.isEmpty())
                            || resultMap.isEmpty()) {
                        // Throw interrupedException to cancel
                        throw new InterruptedException(
                                "Failed to get Facebook token at the beginning! Try later.");
                    } else {
                        if (!isTestFlagOn()) {
                            if (isCacheable()) {
                                for (Map.Entry<String, String> entry : resultMap
                                        .entrySet()) {
                                    String value = entry.getValue();
                                    resultList.add(value);
                                }
                            }
                            save(ctx, resultList);
                        }
                        resultList.clear();
                        resultMap.clear();
                        throw new InterruptedException(
                                "Failed to get Facebook token during crawling! Try later.");
                    }

                } else {
                    logger.info("Get Token -> {}", token);
                }
                if (currentUrl.equalsIgnoreCase("")) {
                    currentUrl = String.format(URL_PREFX_FORMAT, account,
                            token.getAccessToken(), to.getTime() / 1000,
                            from.getTime() / 1000);
                } else {
                    // replace the access_token only in paging URL
                    currentUrl = replaceToken(currentUrl, token);
                }

                logger.info("Begin to request from {}", currentUrl);
                try {
                    currentUrl = getPostsOnEachPage(currentUrl);
                } catch (DataCrawlException de) {
                    TokenManager.getInstance().releaseToken(token,
                            de.getStatusCode(), de);
                    logger.error(
                            "Caught DataCrawlException. Continue to try other tokens to proceed.",
                            de);
                    if (retryNum < RETRY_THRESHOLD) {
                        retryNum++;
                        continue;
                    } else {
                        logger.error(
                                "Failed to crawl Facebook by Account {} with retry ({}) times.",
                                account, retryNum);
                        throw new DataCrawlException(
                                "Failed to crawl Facebook by Account "
                                        + account + " with retry (" + retryNum
                                        + ") times.");
                    }
                } catch (Exception e) {
                    // For other exception just throw to executor
                    throw e;
                } finally {
                    if (token.getState() == StateEnum.Unavailable) {
                        // Release token normally
                        TokenManager.getInstance().releaseToken(
                                token.getTokenTag(), 0);
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
                synchronized (ConfigInstance.FACEBOOK_LOCK) {
                    token = TokenManager.getInstance().getToken(
                            TokenType.Facebook);
                    if (token == null) {
                        logger.error("Get token error");
                        // First time to get token error, just break
                        if ((!isCacheable() && resultList.isEmpty())
                                || resultMap.isEmpty()) {
                            // Throw interrupedException to cancel
                            throw new InterruptedException(
                                    "Failed to get Facebook token at the beginning! Try later.");
                        } else {
                            if (!isTestFlagOn()) {
                                if (isCacheable()) {
                                    for (Map.Entry<String, String> entry : resultMap
                                            .entrySet()) {
                                        String value = entry.getValue();
                                        resultList.add(value);
                                    }
                                }
                                save(ctx, resultList);
                            }
                            resultList.clear();
                            resultMap.clear();
                            throw new InterruptedException(
                                    "Failed to get Facebook token during crawling! Try later.");
                        }

                    } else {
                        logger.info("Get Token -> {}", token);
                    }
                    if (currentUrl.equalsIgnoreCase("")) {
                        currentUrl = String.format(URL_PREFX_FORMAT, account,
                                token.getAccessToken(), to.getTime() / 1000,
                                from.getTime() / 1000);
                    } else {
                        // replace the access_token only in paging URL
                        currentUrl = replaceToken(currentUrl, token);
                    }

                    logger.info("Begin to request from {}", currentUrl);
                    try {
                        currentUrl = getPostsOnEachPage(currentUrl);
                    } catch (DataCrawlException de) {
                        TokenManager.getInstance().releaseToken(token,
                                de.getStatusCode(), de);
                        logger.error(
                                "Caught DataCrawlException. Continue to try other tokens to proceed.",
                                de);
                        if (retryNum < RETRY_THRESHOLD) {
                            retryNum++;
                            continue;
                        } else {
                            logger.error(
                                    "Failed to crawl Facebook by Account {} with retry ({}) times.",
                                    account, retryNum);
                            throw new DataCrawlException(
                                    "Failed to crawl Facebook by Account "
                                            + account + " with retry ("
                                            + retryNum + ") times.");
                        }
                    } catch (Exception e) {
                        // For other exception just throw to executor
                        throw e;
                    } finally {
                        if (token.getState() == StateEnum.Unavailable) {
                            // Release token normally
                            TokenManager.getInstance().releaseToken(
                                    token.getTokenTag(), 0);
                        }
                    }
                }
            }
        } while (currentUrl != null);

        // Reduce duplicated tweets in HIVE
        if (isCacheable()) {
            LuceneUtils.getUnCachedPosts(LuceneUtils.MediaIndexPath.Facebook,
                    resultMap, resultList);
        }

        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.info(
                "Crawl facebook by account {}, total posts number {}, Posts to HIVE count {}, elapsed Time(Seconds) {}",
                account, count, resultList.size(), nanos);
        return isCanceled ? null : resultList;
    }

    // Fix loop issue: Facebook paging url format changed.
    // i.e.
    // https://graph.facebook.com/v2.3/191347651290/posts?limit=100&since=1438617600&__paging_token=enc_AdApjO2ZCxUoZCNihq1byp9KQXe3sC7lfRa0bsYsy3HqfmX5kRiAU70yL2u6EeA1h3MB044DSfEudDxPaMivUyJU9A95izZBKGPyDb7oVP7E9E2PgZDZD&access_token=458163071031535|Po8MS2mFOofnuEB7A7PMTfhNQuQ&until=1438617601
    public static String replaceToken(String url, IToken token) {
        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) {
            // paging url maybe changed, pattern need to be updated.
            return null;
        } else {
            logger.debug(
                    "Match Group count :  {} ; Match Group : {} ; Matcher start : {} ; Matcher end : {} ; Paging url : {}.",
                    matcher.groupCount(), matcher.group(), matcher.start(),
                    matcher.end(), url);
            url = matcher.replaceFirst("access_token=" + token.getAccessToken()
                    + "&");
        }
        logger.info("Updated token in URL {}", url);
        return url;
    }

    private String getPostsOnEachPage(String url) throws Exception {
        JSONObject initResponse = null;
        try {
            if (url == null) {
                logger.error("URL is null.");
                throw new DataCrawlException(
                        "Facebook url is null. Please check the paging url format.");
            }
            initResponse = SendHttp.sendGet(new URL(url));
        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
            throw e;
        }

        if (initResponse == null) {
            logger.error("Failed to get response from {}", url);
            throw new DataCrawlException("Failed to get response from " + url);
        }

        JSONArray postSet = initResponse.getJSONArray("data");
        int postCount = postSet.size();
        logger.info("Post count:{}", postCount);
        for (int i = 0; i < postCount; i++) {
            JSONObject post = postSet.getJSONObject(i);
            // These error codes may caused by rate limit exceeded
            checkError(post);
            try {
                Date createTime = facebookTimeFmt.parse(post
                        .getString("created_time"));
                logger.debug("Publish Time {}, post id {}", createTime,
                        post.getString("id"));
            } catch (ParseException e) {
                logger.error("Failed to get create_time");
                logger.error(e.getMessage());
                continue;
            }

            // Get like count of this post
            String postId = "";
            try {
                postId = post.getString("id");
                // Dot not change token for the following two request for now.
                String likeCountUrl = String.format(LIKE_COUNT_URL_FORMAT,
                        postId, token.getAccessToken());
                String commentCountUrl = String.format(
                        COMMENT_COUNT_URL_FORMAT, postId,
                        token.getAccessToken());
                int likeCount = getTrueCount(likeCountUrl);
                post.put("likes_count", likeCount);
                int commentCount = getTrueCount(commentCountUrl);
                post.put("comments_count", commentCount);
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }

            // Save into list
            String content = Util.EscapeString(post.toString());
            if (content != null) {
                if (isCacheable()) {
                    resultMap.putIfAbsent(postId, content);
                } else {
                    resultList.add(content);
                }
                count.incrementAndGet();
            }
        }

        // Get next page url
        try {
            JSONObject pagingUrls = initResponse.getJSONObject("paging");
            if (pagingUrls == null)
                return null;
            String nextPageUrl = pagingUrls.getString("next");

            logger.info("Paging url : {}", nextPageUrl);
            return nextPageUrl;
        } catch (JSONException e) {
            logger.info("Next page url not found, maybe the last page of request");
            logger.error(e.getMessage());
        }
        return null;
    }

    private boolean checkError(JSONObject response) throws DataCrawlException {
        JSONObject errorObject = response.getJSONObject("error");
        if (errorObject == null)
            return false;
        // For rate limit errors, throw new DataCrawlException to change new
        // token.
        int errCode = errorObject.getIntValue("code");
        String errMsg = errorObject.getString("message");
        logger.error(errMsg);
        throw new DataCrawlException(errMsg, null, errCode);
    }

    private int getTrueCount(String url) throws Exception {
        JSONObject likeRes = null;
        try {
            likeRes = SendHttp.sendGet(new URL(url));
        } catch (MalformedURLException e1) {
            logger.error(e1.getMessage());
            return -1;
        } catch (DataCrawlException e) {
            throw e;
        }
        if (likeRes == null) {
            return -1;
        } else {
            int likeCount = likeRes.getJSONObject("summary").getInteger(
                    "total_count");
            return likeCount;
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        TokenManager.getInstance().init();
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        Calendar currentCal = Calendar.getInstance();
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.DATE, 0);
        // String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.DATE, -7);
        // String from = sdf.format(currentCal.getTime());
        params.put("task_key", "cnn");
        params.put("start_time", "2015-10-24 15:57:23.000000000");
        params.put("end_time", "2015-10-28 16:02:57.627000000");
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByAccount,
                        null, param);
        ctx.put("task", task);
        FacebookArticleAccountExecutor exe = new FacebookArticleAccountExecutor();
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService taskExecutorPool = new TaskExecutorPool(10, 10, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                factory);
        exe.setTask(task);
        for (int i = 0; i < 1; i++) {
            try {
                List<String> result = (List<String>) taskExecutorPool.submit(
                        exe).get();
                if (result != null) {
                    for (int k = 0; k < result.size(); k++) {
                        JSONObject vo = JSON.parseObject(result.get(k));
                        logger.debug("Result {} --> {}", k, vo.toString());
                    }
                    logger.debug("Result {} --> {}", result);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        System.exit(0);
    }
}
