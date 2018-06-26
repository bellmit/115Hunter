package com.sap.cisp.xhna.data.executor.googleplus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.common.DateUtil;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TokenManager;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class GooglePlusPostInfoExecutor extends AbstractTaskExecutor {
    private static Logger logger = LoggerFactory
            .getLogger(GooglePlusPostInfoExecutor.class);
    private SimpleDateFormat gPlusTimeFmt = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss+0000");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private IToken token;

    public List<String> execute(Map<String, Object> ctx) throws Exception {

        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String startTime = params.getStartTime();
        String endTime = params.getEndTime();
        String account = params.getAccount();

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
        }

        // google guava stopwatch, to caculate the execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();

        Date fromDate = DateUtil.stringToDate(startTime, "yyyy-MM-dd HH:mm:ss");
        Date toDate = DateUtil.stringToDate(endTime, "yyyy-MM-dd HH:mm:ss");
        gPlusTimeFmt.setTimeZone(GMT);
        Date fromTime = DateUtil.parseDate(gPlusTimeFmt.format(fromDate));
        Date toTime = DateUtil.parseDate(gPlusTimeFmt.format(toDate));
        logger.info("From {} To {}", startTime, endTime);
        logger.info("From date {} To date {}", fromTime, toTime);

        String nextToken = null;
        int size = 0;
        boolean toBreak = false;

        int count = 0;
        List<String> rs = new ArrayList<String>();
        int retryNum = 0;
        do {
            if (isCanceled)
                return null;

            JSONObject jsonObject = null;
            // There is a 1 minutes timeout if no available token, getToken
            // return null
            // For the use case(work mode with server), since get/release token
            // is locally, thread-safe without synchronized
            if (ConfigInstance.runInLocal()) {
                token = TokenManager.getInstance().getToken(
                        TokenType.GooglePlus);
                if (token == null) {
                    logger.error("Get token error");
                    // First time to get token error, just break
                    if (rs.isEmpty()) {
                        // Throw interrupedException to cancel
                        throw new InterruptedException(
                                "Failed to get Google+ token at the beginning of account crawling! Try later.");
                    } else {
                        if (!isTestFlagOn()) {
                            save(ctx, rs);
                        }
                        rs.clear();
                        throw new InterruptedException(
                                "Failed to get Google+ token during account crawling! Try later.");
                    }

                } else {
                    logger.info("Get Token -> {}", token.getAccessToken());
                }

                String userId = "";
                try {
                    /** Here does not split the two operations with different tokens for now */
                    if (userId.equalsIgnoreCase("")) {
                        JSONObject jsonUserId = BaseGpCrawler
                                .crawlProfileByUserName(token.getAccessToken(),
                                        account);
                        if (jsonUserId == null) {
                            throw new DataCrawlException(
                                    "Cannot get the  User Id Json Object in crawlActivitiesByUser.");
                        }
                        userId = getUserIdByProfile(jsonUserId);
                        logger.info("name: {} /userId: {}, Json {}", account, userId, jsonUserId);
                    }
                    jsonObject = BaseGpCrawler.crawlActivitiesByUser(
                            token.getAccessToken(), userId, nextToken);
                    if (jsonObject == null) {
                        throw new DataCrawlException(
                                "Cannot get the Activity Json Object in crawlActivitiesByUser.");
                    } else if (jsonObject.getJSONArray("items") == null
                            || !(jsonObject.getJSONArray("items").length() > 0)) {
                        break;
                    }
                } catch (DataCrawlException de) {
                    TokenManager.getInstance().releaseToken(token,
                            de.getStatusCode(), de);
                    logger.error(
                            "Caught DataCrawlException. Continue to try other tokens to proceed.",
                            de);
                    if (retryNum < ITaskExecutor.RETRY_THRESHOLD) {
                        retryNum++;
                        continue;
                    } else {
                        logger.error(
                                "Failed to crawl Google+ by Account {} with retry ({}) times.",
                                userId, retryNum);
                        throw new DataCrawlException(
                                "Failed to crawl Google+  by Account " + userId
                                        + " with retry (" + retryNum
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
                synchronized (ConfigInstance.GPLUS_LOCK) {
                    token = TokenManager.getInstance().getToken(
                            TokenType.GooglePlus);
                    if (token == null) {
                        logger.error("Get token error");
                        // First time to get token error, just break
                        if (rs.isEmpty()) {
                            // Throw interrupedException to cancel
                            throw new InterruptedException(
                                    "Failed to get Google+ token at the beginning of account crawling! Try later.");
                        } else {
                            if (!isTestFlagOn()) {
                                save(ctx, rs);
                            }
                            rs.clear();
                            throw new InterruptedException(
                                    "Failed to get Google+ token during account crawling! Try later.");
                        }

                    } else {
                        logger.info("Get Token -> {}", token.getAccessToken());
                    }

                    String userId = "";
                    try {
                        if (userId.equalsIgnoreCase("")) {
                            JSONObject jsonUserId = BaseGpCrawler
                                    .crawlProfileByUserName(
                                            token.getAccessToken(), account);
                            if (jsonUserId == null) {
                                throw new DataCrawlException(
                                        "Cannot get the  User Id Json Object in crawlActivitiesByUser.");
                            }
                            userId = getUserIdByProfile(jsonUserId);
                            logger.info("name:" + account + "/" + "userId:"
                                    + userId);
                        }
                        jsonObject = BaseGpCrawler.crawlActivitiesByUser(
                                token.getAccessToken(), userId, nextToken);
                        if (jsonObject == null) {
                            throw new DataCrawlException(
                                    "Cannot get the Activity Json Object in crawlActivitiesByUser.");
                        } else if (jsonObject.getJSONArray("items") == null
                                || !(jsonObject.getJSONArray("items").length() > 0)) {
                            break;
                        }
                    } catch (DataCrawlException de) {
                        TokenManager.getInstance().releaseToken(token,
                                de.getStatusCode(), de);
                        logger.error(
                                "Caught DataCrawlException. Continue to try other tokens to proceed.",
                                de);
                        if (retryNum < ITaskExecutor.RETRY_THRESHOLD) {
                            retryNum++;
                            continue;
                        } else {
                            logger.error(
                                    "Failed to crawl Google+ by Account {} with retry ({}) times.",
                                    userId, retryNum);
                            throw new DataCrawlException(
                                    "Failed to crawl Google+  by Account "
                                            + userId + " with retry ("
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
            try {
                JSONArray activities = jsonObject.getJSONArray("items");
                size = activities.length();

                for (int i = 0; i < size; i++) {
                    JSONObject activity = activities.getJSONObject(i);
                    GpHelper.plainizeRichText(activity);

                    String published = activity.getString("published");
                    Date publishTime = DateUtil.parseDate(published);
                    logger.debug("Publish Time {}", publishTime);
                    if (publishTime.before(fromTime)) {
                        logger.debug(
                                "Publish Time {} before from time ===> {}",
                                publishTime, fromTime);
                        toBreak = true;
                        break;
                    } else if (publishTime.after(toTime)) {
                        continue;
                    } else if (publishTime.before(toTime)
                            && publishTime.after(fromTime)) {
                        rs.add(getInfo(activity).toString());
                        count++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

            // update next page token
            try {
                nextToken = jsonObject.getString("nextPageToken");
                if (nextToken == null)
                    break;
            } catch (JSONException e) {
                break;
            }

        } while (!toBreak);

        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.debug("Crawl GooglePlus Account {} posts info, count {}, elapsed Time(Seconds) {}",
                account, count, nanos);

        return isCanceled() ? null : rs;
    }

    public static String getUserIdByProfile(JSONObject jsonObject) {
        JSONArray users = jsonObject.getJSONArray("items");
        JSONObject user = users.getJSONObject(0);
        String userId = user.getString("id");
        return userId;
    }

    public JSONObject getInfo(JSONObject activity) {
        String statusId = activity.getString("id");

        JSONObject temp_obj = activity.getJSONObject("object");

        JSONObject repliesObj = temp_obj.getJSONObject("replies");
        int replyCount = 0;
        if (repliesObj != null) {
            replyCount = repliesObj.getInt("totalItems");
        } else {
            logger.error(
                    "!!! Cannot get the reply count. Use the default value {}.",
                    replyCount);
        }

        JSONObject favoriteObj = temp_obj.getJSONObject("plusoners");
        int plusonersCount = 0;
        if (favoriteObj != null) {
            plusonersCount = favoriteObj.getInt("totalItems");
        } else {
            logger.error(
                    "!!! Cannot get the plusonersCount. Use the default value {}.",
                    plusonersCount);
        }

        JSONObject resharedObj = temp_obj.getJSONObject("resharers");
        int resharedCount = 0;
        if (resharedObj != null) {
            resharedCount = resharedObj.getInt("totalItems");
        } else {
            logger.error(
                    "!!! Cannot get the resharedCount. Use the default value {}.",
                    resharedCount);
        }

        int mediaCount = 0;
        if (temp_obj.has("attachments")) {
            JSONArray medias = temp_obj.getJSONArray("attachments");
            mediaCount = medias.length();
        } else {
            logger.error(
                    "!!! Cannot get the media attachements. Use the default value {}.",
                    mediaCount);
        }

        String crawlTime = sdf.format(new Date());

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("id", statusId);
        jsonObj.put("crawlTime", crawlTime);
        jsonObj.put("replyCount", replyCount);
        jsonObj.put("resharedCount", resharedCount);
        jsonObj.put("plusonersCount", plusonersCount);
        jsonObj.put("mediaCount", mediaCount);

        return jsonObj;
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
        currentCal.add(Calendar.HOUR, -480);
        String from = sdf.format(currentCal.getTime());
        params.put("task_key", "bbcnews");
        params.put("start_time", from);
        params.put("end_time", to);

        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_Account_Article,
                        null, param);
        ctx.put("task", task);
        GooglePlusPostInfoExecutor exe = new GooglePlusPostInfoExecutor();
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
