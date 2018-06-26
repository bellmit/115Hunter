package com.sap.cisp.xhna.data.executor.youtube;

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
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TokenManager;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class YoutubePostInfoExecutor extends AbstractTaskExecutor {
    private static Logger logger = LoggerFactory
            .getLogger(YoutubePostInfoExecutor.class);
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private IToken token;
    private String channelUrlFmt = "https://www.googleapis.com/youtube/v3/"
            + "channels?part=snippet,contentDetails,statistics"
            + "&forUsername=%s" + "&key=%s";
    private String channelUrlByIdFmt = "https://www.googleapis.com/youtube/v3/"
            + "channels?part=snippet,contentDetails,statistics" + "&id=%s"
            + "&key=%s";
    private String playlistUrlFmt = "https://www.googleapis.com/youtube/v3/"
            + "playlistItems?" + "part=snippet" + "&maxResults=10"
            + "&playlistId=%s"
            + "&fields=items/snippet,nextPageToken,pageInfo,tokenPagination"
            + "&key=%s" + "&pageToken=%s";
    private String videoUrlFmt = "https://www.googleapis.com/youtube/v3/"
            + "videos?part=id,snippet,contentDetails,statistics,status,"
            + "liveStreamingDetails,topicDetails,recordingDetails,player"
            + "&id=%s" + "&key=%s";

    private List<String> videoIdList = new LinkedList<String>();
    private List<String> videoInfoList = new LinkedList<String>();
    private SimpleDateFormat youTubeTimeFmt = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss+0000");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private Date from;
    private Date to;
    String patternStr = "(?:(key=[^&.]*[&]{1}\\b)(?!_))";
    // Less compile
    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE
            | Pattern.DOTALL);

    @Override
    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception {

        videoIdList.clear();
        videoInfoList.clear();

        logger.info("Begin to get posts by account");
        ITask task = (ITask) ctx.get("task");
        String account = task.getParam().getAccount();
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

        // google guava stopwatch, to caculate the execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();

        youTubeTimeFmt.setTimeZone(GMT);
        Date fromDate = DateUtil.stringToDate(fromStr, "yyyy-MM-dd HH:mm:ss");
        Date toDate = DateUtil.stringToDate(toStr, "yyyy-MM-dd HH:mm:ss");
        from = DateUtil.parseDate(youTubeTimeFmt.format(fromDate));
        to = DateUtil.parseDate(youTubeTimeFmt.format(toDate));
        logger.info("From date {} To date {}", from, to);

        String playlistId = getPlaylistId(account, false);
        if (playlistId == null) {
            logger.error(
                    "Failed to get playlist ID by account {}. Retry get playlist by means of ID.",
                    account);
            playlistId = getPlaylistId(account, true);
            if (playlistId == null) {
                logger.error(
                        "Failed to get playlist ID either by account or by means of ID. Account : {}",
                        account);
                return null;
            }
        }

        logger.info("Get Play list ID . Account {} ; Playlist ID {}", account,
                playlistId);
        String pageToken = "";
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
                token = TokenManager.getInstance().getToken(TokenType.YouTube);
                if (token == null) {
                    logger.error("Get token error");
                    // First time to get token error, just break
                    if (videoInfoList.isEmpty()) {
                        // Throw interrupedException to cancel
                        throw new InterruptedException(
                                "Failed to get YouTube token at the beginning! Try later.");
                    } else {
                        if (!isTestFlagOn()) {
                            save(ctx, videoInfoList);
                        }
                        videoInfoList.clear();
                        throw new InterruptedException(
                                "Failed to get YouTube token during crawling! Try later.");
                    }

                } else {
                    logger.info("Get Token -> {}", token.getAccessToken());
                }

                if (currentUrl.equalsIgnoreCase("")) {
                    currentUrl = String.format(playlistUrlFmt, playlistId,
                            token.getAccessToken(), pageToken);
                } else {
                    // replace the access_token only in paging URL
                    // i.e.
                    // https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=10&playlistId=UUupvZG-5ko_eiXAupbDfxWw&fields=items/snippet,nextPageToken,pageInfo,tokenPagination&key=AIzaSyBjyAUlAbpcYizP4N-lyo3dBzcPv8ZVeZI&pageToken=
                    currentUrl = String.format(playlistUrlFmt, playlistId,
                            token.getAccessToken(), pageToken);
                    currentUrl = replaceToken(currentUrl, token);
                }
                logger.info("Get play list : {}", currentUrl);
                try {
                    pageToken = getPlaylist(currentUrl);
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
                                "Failed to crawl YouTube by Account {} with retry ({}) times.",
                                account, retryNum);
                        throw new DataCrawlException(
                                "Failed to crawl YouTube by Account " + account
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
                 * ensure the get/release pair operation integrity. Error case:
                 * - A lot of executors are started in parallel, bunch of get
                 * token requests sent to gearman server, the release token
                 * request can not be handled in time(executor service, all the
                 * executors then be stuck. - Since IToken is not thread-safe,
                 * when get/release token asynchronously, setState/getState is
                 * error prone. Keep token without lock to make local
                 * get/release token operation more efficient.
                 */
                synchronized (ConfigInstance.YOUTUBE_LOCK) {
                    token = TokenManager.getInstance().getToken(
                            TokenType.YouTube);
                    if (token == null) {
                        logger.error("Get token error");
                        // First time to get token error, just break
                        if (videoInfoList.isEmpty()) {
                            // Throw interrupedException to cancel
                            throw new InterruptedException(
                                    "Failed to get YouTube token at the beginning! Try later.");
                        } else {
                            if (!isTestFlagOn()) {
                                save(ctx, videoInfoList);
                            }
                            videoInfoList.clear();
                            throw new InterruptedException(
                                    "Failed to get YouTube token during crawling! Try later.");
                        }

                    } else {
                        logger.info("Get Token -> {}", token.getAccessToken());
                    }

                    if (currentUrl.equalsIgnoreCase("")) {
                        currentUrl = String.format(playlistUrlFmt, playlistId,
                                token.getAccessToken(), pageToken);
                    } else {
                        // replace the access_token only in paging URL
                        // i.e.
                        // https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=10&playlistId=UUupvZG-5ko_eiXAupbDfxWw&fields=items/snippet,nextPageToken,pageInfo,tokenPagination&key=AIzaSyBjyAUlAbpcYizP4N-lyo3dBzcPv8ZVeZI&pageToken=
                        currentUrl = String.format(playlistUrlFmt, playlistId,
                                token.getAccessToken(), pageToken);
                        currentUrl = replaceToken(currentUrl, token);
                    }
                    logger.info("Get play list : {}", currentUrl);
                    try {
                        pageToken = getPlaylist(currentUrl);
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
                                    "Failed to crawl YouTube by Account {} with retry ({}) times.",
                                    account, retryNum);
                            throw new DataCrawlException(
                                    "Failed to crawl YouTube by Account "
                                            + account + " with retry ("
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
                            if (videoInfoList.isEmpty()) {
                             // Throw interrupedException to cancel
                                throw new InterruptedException(
                                        "Failed to get YouTube token at beginning of CrawlVideoInfo! Try later.");
                            } else {
                                if (!isTestFlagOn()) {
                                    save(ctx, videoInfoList);
                                }
                                videoInfoList.clear();
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
                         * operation integrity. Error case: - A lot of executors
                         * are started in parallel, bunch of get token requests
                         * sent to gearman server, the release token request can
                         * not be handled in time(executor service, all the
                         * executors then be stuck. - Since IToken is not
                         * thread-safe, when get/release token asynchronously,
                         * setState/getState is error prone. Keep token without
                         * lock to make local get/release token operation more
                         * efficient.
                         */
                        synchronized (ConfigInstance.YOUTUBE_LOCK) {
                            token = TokenManager.getInstance().getToken(
                                    TokenType.YouTube);
                            if (token == null) {
                                logger.error("Get token error");
                                // First time to get token error, just break
                                if (videoInfoList.isEmpty()) {
                                 // Throw interrupedException to cancel
                                    throw new InterruptedException(
                                            "Failed to get YouTube token at beginning of CrawlVideoInfo! Try later.");
                                } else {
                                    if (!isTestFlagOn()) {
                                        save(ctx, videoInfoList);
                                    }
                                    videoInfoList.clear();
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

        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.info(
                "Crawl YouTube post addtional info:  account {}, video number {}, elasped Time(Seconds) {}",
                account, videoInfoList.size(), nanos);
        return isCanceled ? null : videoInfoList;
    }

    private String getPlaylistId(String channel, boolean isRetryByID) throws Exception {
        int retryNum = 0;
        while (true) {
            // For the use case(work mode with server), since
            // get/release token
            // is locally, thread-safe without synchronized
            if (ConfigInstance.runInLocal()) {
                token = TokenManager.getInstance().getToken(TokenType.YouTube);
                if (token == null) {
                    logger.error("Get token error");
                 // Throw interrupedException to cancel
                    throw new InterruptedException(
                            "Failed to get YouTube token at the get play list id! Try later.");
                }
                String url = "";
                if (!isRetryByID) {
                    url = String.format(channelUrlFmt, channel,
                            token.getAccessToken());
                } else {
                    url = String.format(channelUrlByIdFmt, channel,
                            token.getAccessToken());
                }
                logger.debug("Get playlist url -> {}", url);
                try {
                    JSONObject jsonRes = SendHttp.sendGet(new URL(url));
                    logger.debug("JSON Response --> {}", jsonRes);
                    if (jsonRes != null) {
                        if (!jsonRes.getJSONArray("items").isEmpty()) {
                            JSONObject item = jsonRes.getJSONArray("items")
                                    .getJSONObject(0);
                            return item.getJSONObject("contentDetails")
                                    .getJSONObject("relatedPlaylists")
                                    .getString("uploads");
                        } else {
                            logger.error(
                                    "Play list id is empty with channel {}.",
                                    channel);
                            return null;
                        }
                    }
                } catch (MalformedURLException e) {
                    throw e;
                } catch (JSONException e) {
                    throw e;
                } catch (DataCrawlException de) {
                    TokenManager.getInstance().releaseToken(token,
                            de.getStatusCode(), de);
                    logger.error("Caught DataCrawlException.", de);
                    if (retryNum < RETRY_THRESHOLD) {
                        retryNum++;
                        continue;
                    } else {
                        logger.error(
                                "Failed to crawl YouTube play list id with channel {} with retry ({}) times.",
                                channel, retryNum);
                        throw new DataCrawlException(
                                "Failed to crawl YouTube  play list id with channel "
                                        + channel + " with retry (" + retryNum
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
                 * ensure the get/release pair operation integrity. Error case:
                 * - A lot of executors are started in parallel, bunch of get
                 * token requests sent to gearman server, the release token
                 * request can not be handled in time(executor service, all the
                 * executors then be stuck. - Since IToken is not thread-safe,
                 * when get/release token asynchronously, setState/getState is
                 * error prone. Keep token without lock to make local
                 * get/release token operation more efficient.
                 */
                synchronized (ConfigInstance.YOUTUBE_LOCK) {
                    token = TokenManager.getInstance().getToken(
                            TokenType.YouTube);
                    if (token == null) {
                        logger.error("Get token error");
                     // Throw interrupedException to cancel
                        throw new InterruptedException(
                                "Failed to get YouTube token at the get play list id! Try later.");
                    }
                    String url = "";
                    if (!isRetryByID) {
                        url = String.format(channelUrlFmt, channel,
                                token.getAccessToken());
                    } else {
                        url = String.format(channelUrlByIdFmt, channel,
                                token.getAccessToken());
                    }
                    logger.debug("Get playlist url -> {}", url);
                    try {
                        JSONObject jsonRes = SendHttp.sendGet(new URL(url));
                        logger.debug("JSON Response --> {}", jsonRes);
                        if (jsonRes != null) {
                            if (!jsonRes.getJSONArray("items").isEmpty()) {
                                JSONObject item = jsonRes.getJSONArray("items")
                                        .getJSONObject(0);
                                return item.getJSONObject("contentDetails")
                                        .getJSONObject("relatedPlaylists")
                                        .getString("uploads");
                            } else {
                                logger.error(
                                        "Play list id is empty with channel {}.",
                                        channel);
                                return null;
                            }
                        }
                    } catch (MalformedURLException e) {
                        throw e;
                    } catch (JSONException e) {
                        throw e;
                    } catch (DataCrawlException de) {
                        TokenManager.getInstance().releaseToken(token,
                                de.getStatusCode(), de);
                        logger.error("Caught DataCrawlException.", de);
                        if (retryNum < RETRY_THRESHOLD) {
                            retryNum++;
                            continue;
                        } else {
                            logger.error(
                                    "Failed to crawl YouTube play list id with channel {} with retry ({}) times.",
                                    channel, retryNum);
                            throw new DataCrawlException(
                                    "Failed to crawl YouTube  play list id with channel "
                                            + channel + " with retry ("
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
        }
    }

    private String getPlaylist(String url) throws Exception {
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
                Date publishedTime = DateUtil.parseDate(item.getJSONObject(
                        "snippet").getString("publishedAt"));
                logger.debug("Published Time ====> {}", publishedTime);
                if (publishedTime.before(from))
                    return null;
                else if (publishedTime.after(to))
                    continue;
                String videoId = item.getJSONObject("snippet")
                        .getJSONObject("resourceId").getString("videoId");
                if (videoId != null)
                    videoIdList.add(videoId);
            }
        } catch (JSONException je) {
            throw je;
        } catch (ParseException pe) {
            throw pe;
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
        JSONObject post = null;
        try {
            post = SendHttp.sendGet(new URL(url));
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        } catch (DataCrawlException e) {
            throw e;
        }

        if (post == null) {
            return;
        } else {
            JSONArray videoInfo = post.getJSONArray("items");
            JSONObject item = videoInfo.getJSONObject(0);
            String crawlTime = sdf.format(new Date());

            String postId = item.getString("id");

            JSONObject statistics = item.getJSONObject("statistics");
            int likeCount = statistics.getInteger("likeCount");
            int commentCount = statistics.getInteger("commentCount");
            int viewCount = statistics.getInteger("viewCount");
            int favoriteCount = statistics.getInteger("favoriteCount");
            int dislikeCount = statistics.getInteger("dislikeCount");

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("id", postId);
            jsonObj.put("crawlTime", crawlTime);
            jsonObj.put("favoriteCount", favoriteCount);
            jsonObj.put("dislikeCount", dislikeCount);
            jsonObj.put("likeCount", likeCount);
            jsonObj.put("commentCount", commentCount);
            jsonObj.put("viewCount", viewCount);

            videoInfoList.add(jsonObj.toString());
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
        currentCal.add(Calendar.HOUR, -120);
        String from = sdf.format(currentCal.getTime());
        params.put("task_key", "UC16niRr50-MSBwiO3YDb3RA");
        params.put("start_time", from);
        params.put("end_time", to);

        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_Account_Article,
                        null, param);
        ctx.put("task", task);
        YoutubePostInfoExecutor exe = new YoutubePostInfoExecutor();
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
