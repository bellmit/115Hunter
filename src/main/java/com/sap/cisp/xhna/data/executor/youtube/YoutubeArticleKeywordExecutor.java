package com.sap.cisp.xhna.data.executor.youtube;

import java.net.MalformedURLException;
import java.net.URL;
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
import com.sap.cisp.xhna.data.token.TokenManager;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class YoutubeArticleKeywordExecutor extends AbstractTaskExecutor {
    private static Logger logger = LoggerFactory
            .getLogger(YoutubeArticleKeywordExecutor.class);

    private String searchUrlFmt = "https://www.googleapis.com/youtube/v3/search?"
            + "q=%s"
            + "&type=video"
            + "&key=%s"
            + "&part=id"
            + "&publishedAfter=%s"
            + "&publishedBefore=%s"
            + "&maxResults=50"
            + "&pageToken=%s";

    private String videoUrlFmt = "https://www.googleapis.com/youtube/v3/videos?"
            + "part=id,snippet,contentDetails,statistics,status,"
            + "liveStreamingDetails,topicDetails,recordingDetails,player"
            + "&id=%s" + "&key=%s";

    private SimpleDateFormat youTubeTimeFmt = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private IToken token;
    private Date from;
    private Date to;

    private List<String> videoIdList = new LinkedList<String>();
    private List<String> videoInfoList = new LinkedList<String>();
    private ConcurrentHashMap<String, String> videoResultMap = new ConcurrentHashMap<String, String>();
    private AtomicInteger count = new AtomicInteger(0);
    String patternStr = "(?:(key=[^&.]*[&]{1}\\b)(?!_))";
    // Less compile
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE
            | Pattern.DOTALL);

    @Override
    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception {

        logger.info("Begin to get posts by keyword");
        ITask task = (ITask) ctx.get("task");
        String keyword = task.getParam().getKeyword();
        String fromStr = task.getParam().getStartTime();
        String toStr = task.getParam().getEndTime();

        if (keyword == null || keyword.equalsIgnoreCase("")) {
            logger.error("Get keyword error");
            throw new DataCrawlException("Keyword is null or empty.");
        } else {
            logger.info("Keyword:{}", keyword);
        }

        if (fromStr == null || toStr == null || fromStr.equalsIgnoreCase("")
                || toStr.equalsIgnoreCase("")) {
            logger.error("Get date limit error");
            throw new DataCrawlException(
                    "Date limit(start time/end time) is null or empty.");
        } else {
            logger.info("From {} To {}", fromStr, toStr);
        }

        Stopwatch stopwatch = Stopwatch.createStarted();

        youTubeTimeFmt.setTimeZone(GMT);
        Date fromDate = DateUtil.stringToDate(fromStr, "yyyy-MM-dd HH:mm:ss");
        Date toDate = DateUtil.stringToDate(toStr, "yyyy-MM-dd HH:mm:ss");
        from = DateUtil.parseDate(youTubeTimeFmt.format(fromDate));
        to = DateUtil.parseDate(youTubeTimeFmt.format(toDate));
        logger.info("From date {} To date {}", from, to);

        // Get video id from search result
        String pageToken = "";
        String currentUrl = "";
        int retryNum = 0;
        do {
            if (isCanceled)
                return null;
            // There is a 1 minutes timeout if no available token, getToken
            // return null
            // For the use case(work mode with server), since
            // get/release token
            // is locally, thread-safe without synchronized
            if (ConfigInstance.runInLocal()) {
                token = TokenManager.getInstance().getToken(TokenType.YouTube);

                if (token == null) {
                    logger.error("Get token error");
                    // First time to get token error, just break
                    if ((!isCacheable() && videoInfoList.isEmpty())
                            || videoResultMap.isEmpty()) {
                     // Throw interrupedException to cancel
                        throw new InterruptedException(
                                "Failed to get YouTube token at the beginning! Try later.");
                    } else {
                        if (!isTestFlagOn()) {
                            if (isCacheable()) {

                                for (Map.Entry<String, String> entry : videoResultMap
                                        .entrySet()) {
                                    String value = entry.getValue();
                                    videoInfoList.add(value);
                                }

                            }
                            save(ctx, videoInfoList);
                        }
                        videoInfoList.clear();
                        videoResultMap.clear();
                        throw new InterruptedException(
                                "Failed to get YouTube token during crawling! Try later.");
                    }

                } else {
                    logger.info("Get Token -> {}", token.getAccessToken());
                }

                if (currentUrl.equalsIgnoreCase("")) {
                    currentUrl = String.format(searchUrlFmt, keyword,
                            token.getAccessToken(),
                            youTubeTimeFmt.format(fromDate),
                            youTubeTimeFmt.format(toDate), pageToken);
                } else {
                    // replace the access_token only in paging URL
                    // i.e.
                    // https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=10&playlistId=UUupvZG-5ko_eiXAupbDfxWw&fields=items/snippet,nextPageToken,pageInfo,tokenPagination&key=AIzaSyBjyAUlAbpcYizP4N-lyo3dBzcPv8ZVeZI&pageToken=
                    currentUrl = String.format(searchUrlFmt, keyword,
                            token.getAccessToken(),
                            youTubeTimeFmt.format(fromDate),
                            youTubeTimeFmt.format(toDate), pageToken);
                    currentUrl = replaceToken(currentUrl, token);
                }
                logger.info("Get search list : {}", currentUrl);
                try {
                    pageToken = getSearchResultSet(currentUrl);
                    logger.debug("Page token ---> {}", pageToken);
                    // Release token normally
                    TokenManager.getInstance().releaseToken(token, 0);
                } catch (DataCrawlException de) {
                    TokenManager.getInstance().releaseToken(token,
                            de.getStatusCode(), de);
                    logger.error("Caught DataCrawlException.", de);
                    if (retryNum < RETRY_THRESHOLD) {
                        retryNum++;
                        continue;
                    } else {
                        logger.error(
                                "Failed to crawl YouTube by Keyword {} with retry ({}) times.",
                                keyword, retryNum);
                        throw new DataCrawlException(
                                "Failed to crawl YouTube by keyword " + keyword
                                        + " with retry (" + retryNum
                                        + ") times.");
                    }
                } catch (Exception e) {
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
                synchronized (ConfigInstance.YOUTUBE_LOCK) {
                    token = TokenManager.getInstance().getToken(
                            TokenType.YouTube);

                    if (token == null) {
                        logger.error("Get token error");
                        // First time to get token error, just break
                        if ((!isCacheable() && videoInfoList.isEmpty())
                                || videoResultMap.isEmpty()) {
                         // Throw interrupedException to cancel
                            throw new InterruptedException(
                                    "Failed to get YouTube token at the beginning! Try later.");
                        } else {
                            if (!isTestFlagOn()) {
                                if (isCacheable()) {

                                    for (Map.Entry<String, String> entry : videoResultMap
                                            .entrySet()) {
                                        String value = entry.getValue();
                                        videoInfoList.add(value);
                                    }

                                }
                                save(ctx, videoInfoList);
                            }
                            videoInfoList.clear();
                            videoResultMap.clear();
                            throw new InterruptedException(
                                    "Failed to get YouTube token during crawling! Try later.");
                        }

                    } else {
                        logger.info("Get Token -> {}", token.getAccessToken());
                    }

                    if (currentUrl.equalsIgnoreCase("")) {
                        currentUrl = String.format(searchUrlFmt, keyword,
                                token.getAccessToken(),
                                youTubeTimeFmt.format(fromDate),
                                youTubeTimeFmt.format(toDate), pageToken);
                    } else {
                        // replace the access_token only in paging URL
                        // i.e.
                        // https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=10&playlistId=UUupvZG-5ko_eiXAupbDfxWw&fields=items/snippet,nextPageToken,pageInfo,tokenPagination&key=AIzaSyBjyAUlAbpcYizP4N-lyo3dBzcPv8ZVeZI&pageToken=
                        currentUrl = String.format(searchUrlFmt, keyword,
                                token.getAccessToken(),
                                youTubeTimeFmt.format(fromDate),
                                youTubeTimeFmt.format(toDate), pageToken);
                        currentUrl = replaceToken(currentUrl, token);
                    }
                    logger.info("Get search list : {}", currentUrl);
                    try {
                        pageToken = getSearchResultSet(currentUrl);
                        logger.debug("Page token ---> {}", pageToken);
                        // Release token normally
                        TokenManager.getInstance().releaseToken(token, 0);
                    } catch (DataCrawlException de) {
                        TokenManager.getInstance().releaseToken(token,
                                de.getStatusCode(), de);
                        logger.error("Caught DataCrawlException.", de);
                        if (retryNum < RETRY_THRESHOLD) {
                            retryNum++;
                            continue;
                        } else {
                            logger.error(
                                    "Failed to crawl YouTube by Keyword {} with retry ({}) times.",
                                    keyword, retryNum);
                            throw new DataCrawlException(
                                    "Failed to crawl YouTube by keyword "
                                            + keyword + " with retry ("
                                            + retryNum + ") times.");
                        }
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        if (token.getState() == StateEnum.Unavailable) {
                            // Release token normally
                            TokenManager.getInstance().releaseToken(token, 0);
                        }
                    }
                }
            }
        } while (pageToken != null);

        // Get video detail info with video id;
        if (!videoIdList.isEmpty()) {
            for (String id : videoIdList) {
                int retryNumVideo = 0;
                while (true) {
                    if (isCanceled)
                        return null;
                    // There is a 1 minutes timeout if no available token,
                    // getToken
                    // return null
                    // For the use case(work mode with server), since
                    // get/release token
                    // is locally, thread-safe without synchronized
                    if (ConfigInstance.runInLocal()) {
                        token = TokenManager.getInstance().getToken(
                                TokenType.YouTube);
                        if (token == null) {
                            logger.error("Get token error");
                            // First time to get token error, just break
                            if ((!isCacheable() && videoInfoList.isEmpty())
                                    || videoResultMap.isEmpty()) {
                             // Throw interrupedException to cancel
                                throw new InterruptedException(
                                        "Failed to get YouTube token at beginning of CrawlVideoInfo! Try later.");
                            } else {
                                if (!isTestFlagOn()) {
                                    if (isCacheable()) {
                                        for (Map.Entry<String, String> entry : videoResultMap
                                                .entrySet()) {
                                            String value = entry.getValue();
                                            videoInfoList.add(value);
                                        }
                                    }
                                    save(ctx, videoInfoList);
                                }
                                videoInfoList.clear();
                                videoResultMap.clear();
                                throw new InterruptedException(
                                        "Failed to get YouTube token during CrawlVideoInfo! Try later.");
                            }

                        } else {
                            logger.debug("Get Token -> {}",
                                    token.getAccessToken());
                        }

                        currentUrl = String.format(videoUrlFmt, id,
                                token.getAccessToken());
                        logger.info("Crawl video url {}", currentUrl);
                        try {
                            crawlVideoInfo(currentUrl);
                            break;
                        } catch (DataCrawlException de) {
                            TokenManager.getInstance().releaseToken(token,
                                    de.getStatusCode(), de);
                            logger.error(
                                    "Caught DataCrawlException in Crawl Video info.",
                                    de);
                            if (retryNumVideo < RETRY_THRESHOLD) {
                                retryNumVideo++;
                                continue;
                            } else {
                                logger.error(
                                        "Failed to crawl YouTube Video info with id {} with retry ({}) times.",
                                        id, retryNumVideo);
                                throw new DataCrawlException(
                                        "Failed to crawl YouTube Video info with id  "
                                                + id + " with retry ("
                                                + retryNumVideo + ") times.");
                            }
                        } catch (Exception e) {
                            throw e;
                        } finally {
                            if (token.getState() == StateEnum.Unavailable) {
                                // Release token normally
                                TokenManager.getInstance().releaseToken(token,
                                        0);
                            }
                        }
                    } else {
                        /**
                         * For distribution deployment, since get/release is
                         * remotely and asynchronously, gearman request is not
                         * thread-safe, must ensure the get/release pair
                         * operation integrity. 
                         * One error case: A lot of
                         * executors are started in parallel, bunch of get token
                         * requests sent to gearman server, the release token
                         * request can not be handled in time, all the executors
                         * then be stuck.
                         */
                        synchronized (ConfigInstance.YOUTUBE_LOCK) {
                            token = TokenManager.getInstance().getToken(
                                    TokenType.YouTube);
                            if (token == null) {
                                logger.error("Get token error");
                                // First time to get token error, just break
                                if ((!isCacheable() && videoInfoList.isEmpty())
                                        || videoResultMap.isEmpty()) {
                                 // Throw interrupedException to cancel
                                    throw new InterruptedException(
                                            "Failed to get YouTube token at beginning of CrawlVideoInfo! Try later.");
                                } else {
                                    if (!isTestFlagOn()) {
                                        if (isCacheable()) {
                                            for (Map.Entry<String, String> entry : videoResultMap
                                                    .entrySet()) {
                                                String value = entry.getValue();
                                                videoInfoList.add(value);
                                            }
                                        }
                                        save(ctx, videoInfoList);
                                    }
                                    videoInfoList.clear();
                                    videoResultMap.clear();
                                    throw new InterruptedException(
                                            "Failed to get YouTube token during CrawlVideoInfo! Try later.");
                                }

                            } else {
                                logger.debug("Get Token -> {}",
                                        token.getAccessToken());
                            }

                            currentUrl = String.format(videoUrlFmt, id,
                                    token.getAccessToken());
                            logger.info("Crawl video url {}", currentUrl);
                            try {
                                crawlVideoInfo(currentUrl);
                                break;
                            } catch (DataCrawlException de) {
                                TokenManager.getInstance().releaseToken(token,
                                        de.getStatusCode(), de);
                                logger.error(
                                        "Caught DataCrawlException in Crawl Video info.",
                                        de);
                                if (retryNumVideo < RETRY_THRESHOLD) {
                                    retryNumVideo++;
                                    continue;
                                } else {
                                    logger.error(
                                            "Failed to crawl YouTube Video info with id {} with retry ({}) times.",
                                            id, retryNumVideo);
                                    throw new DataCrawlException(
                                            "Failed to crawl YouTube Video info with id  "
                                                    + id + " with retry ("
                                                    + retryNumVideo
                                                    + ") times.");
                                }
                            } catch (Exception e) {
                                throw e;
                            } finally {
                                if (token.getState() == StateEnum.Unavailable) {
                                    // Release token normally
                                    TokenManager.getInstance().releaseToken(
                                            token, 0);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reduce duplicated video info in HIVE
        if (isCacheable()) {
            LuceneUtils.getUnCachedPosts(LuceneUtils.MediaIndexPath.YouTube,
                    videoResultMap, videoInfoList);
        }

        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.info(
                "crawl keyword -> {}; total video number {}, video Info to HIVE count {},  Elapsed Time(Seconds) -> {}",
                keyword, count, videoInfoList.size(), nanos);
        return isCanceled ? null : videoInfoList;
    }

    String getSearchResultSet(String url) throws Exception {
        JSONObject response = null;
        try {
            response = SendHttp.sendGet(new URL(url));
        } catch (MalformedURLException me) {
            throw me;
        } catch (DataCrawlException de) {
            throw de;
        } catch (Exception e) {
            throw e;
        }

        if (response == null) {
            return null;
        }

        try {
            JSONArray playlistSet = response.getJSONArray("items");
            if (playlistSet == null)
                return null;
            for (int i = 0; i < playlistSet.size(); ++i) {
                JSONObject item = playlistSet.getJSONObject(i);
                String videoId = item.getJSONObject("id").getString("videoId");
                if (videoId != null)
                    videoIdList.add(videoId);
            }
        } catch (JSONException je) {
            throw je;
        } catch (Exception e) {
            throw e;
        }

        try {
            String nextToken = response.getString("nextPageToken");
            return nextToken;
        } catch (JSONException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void crawlVideoInfo(String url) throws Exception {
        JSONObject response = null;
        try {
            response = SendHttp.sendGet(new URL(url));
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        } catch (DataCrawlException e) {
            throw e;
        }

        if (response == null) {
            return;
        } else {
            String jsonStr = Util.EscapeString(response.toString());
            logger.debug("Video response --> {}", jsonStr);
            org.json.JSONObject json = new org.json.JSONObject(jsonStr);
            String id = json.getJSONArray("items").getJSONObject(0)
                    .getString("id");
            Date publishedTime = DateUtil.parseDate(json.getJSONArray("items").getJSONObject(0)
                    .getJSONObject("snippet").getString("publishedAt"));
            logger.debug(
                    "Video publish Time: Original {} ==> {}, id {}",
                    json.getJSONArray("items").getJSONObject(0)
                            .getJSONObject("snippet").getString("publishedAt"), publishedTime,
                    id);
            if (isCacheable()) {
                videoResultMap.putIfAbsent(id, jsonStr);
            } else {
                videoInfoList.add(jsonStr);
            }
            count.incrementAndGet();
        }
    }

    private String replaceToken(String url, IToken token) {
        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) {
            // paging url maybe changed, pattern need to be updated.
            return null;
        } else {
            logger.debug(
                    "Match Group count :  {} ; Match Group : {} ; Matcher start : {} ; Matcher end : {} ; Paging url : {}.",
                    matcher.groupCount(), matcher.group(), matcher.start(),
                    matcher.end(), url);
            url = matcher.replaceFirst("key=" + token.getAccessToken() + "&");
        }
        logger.info("Updated token in URL {}", url);
        return url;
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
        currentCal.add(Calendar.HOUR, -20);
        String from = sdf.format(currentCal.getTime());
        params.put("task_key", "tianjin");
        params.put("start_time", from);
        params.put("end_time", to);

        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByKeyword,
                        null, param);
        ctx.put("task", task);
        YoutubeArticleKeywordExecutor exe = new YoutubeArticleKeywordExecutor();
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
