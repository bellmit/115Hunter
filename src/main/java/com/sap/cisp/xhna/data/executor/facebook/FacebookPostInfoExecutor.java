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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TokenManager;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class FacebookPostInfoExecutor extends AbstractTaskExecutor {
    private static Logger logger = LoggerFactory
            .getLogger(FacebookPostInfoExecutor.class);
    private String URL_PREFX_FORMAT = "https://graph.facebook.com/%s/posts?access_token=%s&until=%d&since=%d";
    private String LIKE_COUNT_URL_FORMAT = "https://graph.facebook.com/%s/likes?summary=true&access_token=%s";
    private String COMMENT_COUNT_URL_FORMAT = "https://graph.facebook.com/%s/comments?summary=true&access_token=%s";
    private String POST_URL_FORMAT = "https://graph.facebook.com/%s/?access_token=%s";
    private List<String> resultList = new LinkedList<String>();
    private Date from;
    private Date to;
    private String account;
    private SimpleDateFormat facebookTimeFmt = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss+0000");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private IToken token;
    private int count;

    @Override
    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception {
        resultList.clear();
        logger.info("Begin to get posts info");
        ITask task = (ITask) ctx.get("task");
        account = task.getParam().getAccount();
        String fromStr = task.getParam().getStartTime();
        String toStr = task.getParam().getEndTime();

        if (account == null) {
            logger.error("Get account error");
            return null;
        } else {
            logger.info("Account:{}", account);
        }

        if (fromStr == null || toStr == null) {
            logger.error("Get date limit error");
            return null;
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
                    if (resultList.isEmpty()) {
                     // Throw interrupedException to cancel
                        throw new InterruptedException(
                                "Failed to get Facebook token at the beginning! Try later.");
                    } else {
                        if (!isTestFlagOn()) {
                            save(ctx, resultList);
                        }
                        resultList.clear();
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
                    // i.e.
                    // https://graph.facebook.com/v2.3/5550296508/posts?since=1434211200&access_token=458163071031535|Po8MS2mFOofnuEB7A7PMTfhNQuQ&limit=25&until=1434294504&__paging_token=enc_AdCJDbzCNQ4o2cI4yO4L4uxH7vzDJ9X1vSn1o1qeLYJx63Vt7VSCK4MZAlHJzmof6lnZC5gzEtIwZAYTWVESuTO7yBH
                    currentUrl = FacebookArticleAccountExecutor.replaceToken(currentUrl, token);
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
                synchronized (ConfigInstance.FACEBOOK_LOCK) {
                    token = TokenManager.getInstance().getToken(
                            TokenType.Facebook);
                    if (token == null) {
                        logger.error("Get token error");
                        // First time to get token error, just break
                        if (resultList.isEmpty()) {
                            // Throw interrupedException to cancel
                            throw new InterruptedException(
                                    "Failed to get Facebook token at the beginning! Try later.");
                        } else {
                            if (!isTestFlagOn()) {
                                save(ctx, resultList);
                            }
                            resultList.clear();
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
                        // i.e.
                        // https://graph.facebook.com/v2.3/5550296508/posts?since=1434211200&access_token=458163071031535|Po8MS2mFOofnuEB7A7PMTfhNQuQ&limit=25&until=1434294504&__paging_token=enc_AdCJDbzCNQ4o2cI4yO4L4uxH7vzDJ9X1vSn1o1qeLYJx63Vt7VSCK4MZAlHJzmof6lnZC5gzEtIwZAYTWVESuTO7yBH
                        currentUrl = FacebookArticleAccountExecutor.replaceToken(currentUrl, token);
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
                            TokenManager.getInstance().releaseToken(token, 0);
                        }
                    }
                }
            }
        } while (currentUrl != null);

        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.info(
                "Crawl facebook account {} posts info, total posts number {}, elapsed Time(Seconds) {}",
                account, count, nanos);
        return isCanceled ? null : resultList;
    }

    private String getPostsOnEachPage(String url) throws Exception {
        JSONObject initResponse = null;
        try {
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
                logger.debug("Publish Time {}", createTime);
            } catch (ParseException e) {
                logger.error("Failed to get create_time");
                logger.error(e.getMessage());
                continue;
            }

            // Get like count of this post
            try {
                String postId = post.getString("id");
                // Dot not change token for the following two request for now.
                String likeCountUrl = String.format(LIKE_COUNT_URL_FORMAT,
                        postId, token.getAccessToken());
                String commentCountUrl = String.format(
                        COMMENT_COUNT_URL_FORMAT, postId,
                        token.getAccessToken());
                int likeCount = getTrueCount(likeCountUrl);
                if (likeCount < 0) {
                    likeCount = 0;
                    logger.error(
                            "!!! Cannot get the like count. Use the default value {}.",
                            likeCount);
                }
                int commentCount = getTrueCount(commentCountUrl);
                if (commentCount < 0) {
                    commentCount = 0;
                    logger.error(
                            "!!! Cannot get the comment count. Use the default value {}.",
                            commentCount);
                }
                // get shared count
                String postUrl = String.format(POST_URL_FORMAT, postId,
                        token.getAccessToken());
                JSONObject postContent = SendHttp.sendGet(new URL(postUrl));
                int resharedCount = 0;
                if (postContent.getJSONObject("shares") != null) {
                    resharedCount = postContent.getJSONObject("shares")
                            .getInteger("count");
                } else {
                    logger.error("!!! Cannot get the \"shares\" count. Use the default value {}.", resharedCount);
                }

                String crawlTime = sdf.format(new Date());

                JSONObject jsonObj = new JSONObject();
                jsonObj.put("id", postId);
                jsonObj.put("crawlTime", crawlTime);
                jsonObj.put("replyCount", commentCount);
                jsonObj.put("resharedCount", resharedCount);
                jsonObj.put("likeCount", likeCount);

                resultList.add(jsonObj.toString());
                logger.debug(count + ":" + jsonObj.toString());
                count++;
            } catch (JSONException e) {
                logger.error(e.getMessage());
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.HOUR, -1);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.HOUR, -100);
        String from = sdf.format(currentCal.getTime());
        params.put("task_key", "cnn");
        params.put("start_time", from);
        params.put("end_time", to);
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_Account_Article,
                        null, param);
        ctx.put("task", task);
        FacebookPostInfoExecutor exe = new FacebookPostInfoExecutor();
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService taskExecutorPool = new TaskExecutorPool(10, 10, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                factory);
        exe.setTask(task);

        for (int i = 0; i < 1; i++) {
            try {
                List<String> result = (List<String>) taskExecutorPool.submit(
                        exe).get();
                logger.info("Crawl result-----> " + result);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
}
