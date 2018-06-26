package com.sap.cisp.xhna.data.executor.googleplus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.common.DateUtil;
import com.sap.cisp.xhna.data.common.index.LuceneUtils;
import com.sap.cisp.xhna.data.common.language.LanguageDetectorService;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TokenManager;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class KeywordCrawler {
    private String keyword;
    private String startTime;
    private String endTime;
    private ITaskExecutor executor = null;
    private static Logger logger = LoggerFactory
            .getLogger(KeywordCrawler.class);
    private IToken token;
    private SimpleDateFormat gPlusTimeFmt = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss+0000");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    public KeywordCrawler(Map<String, Object> ctx, ITaskExecutor executor) {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        this.startTime = params.getStartTime();
        this.endTime = params.getEndTime();
        this.keyword = params.getKeyword();
        this.executor = executor;
    }

    public List<String> crawl(Map<String, Object> ctx) throws Exception {
        if (keyword.isEmpty()) {
            logger.error("input keyword is empty");
            throw new DataCrawlException("input keyword is empty");
        }
        logger.info("start crawl " + keyword);
        // google guava stopwatch, to caculate the execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();

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

        if (startTime == null || endTime == null
                || startTime.equalsIgnoreCase("")
                || endTime.equalsIgnoreCase("")) {
            logger.error("Get date limit error");
            throw new DataCrawlException(
                    "Date limit(start time/end time) is null or empty.");
        }

        Date fromDate = DateUtil.stringToDate(startTime, "yyyy-MM-dd HH:mm:ss");
        Date toDate = DateUtil.stringToDate(endTime, "yyyy-MM-dd HH:mm:ss");
        gPlusTimeFmt.setTimeZone(GMT);
        Date fromTime = DateUtil.parseDate(gPlusTimeFmt.format(fromDate));
        Date toTime = DateUtil.parseDate(gPlusTimeFmt.format(toDate));
        logger.info("From {} To {}", startTime, endTime);
        logger.info("From date {} To date {}", fromTime, toTime);

        String nextToken = null;
        String lang = LanguageDetectorService.getInstance()
                .getDetectedLanguage(keyword);
        logger.debug("The language detected --> {}", lang);
        int size = 0;
        boolean toBreak = false;

        int count = 0;
        List<String> resultList = new ArrayList<String>();
        ConcurrentHashMap<String, String> resultMap = new ConcurrentHashMap<String, String>();
        int retryNum = 0;
        do {
            if (executor == null || executor.isCanceled())
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
                    if ((!((AbstractTaskExecutor) executor).isCacheable() && resultList
                            .isEmpty()) || resultMap.isEmpty()) {
                        // Throw interrupedException to cancel
                        throw new InterruptedException(
                                "Failed to get Google+ token at the beginning of keyword crawling! Try later.");
                    } else {
                        if (!((AbstractTaskExecutor) executor).isTestFlagOn()) {
                            if (((AbstractTaskExecutor) executor).isCacheable()) {
                                for (Map.Entry<String, String> entry : resultMap
                                        .entrySet()) {
                                    String value = entry.getValue();
                                    resultList.add(value);
                                }
                            }
                            executor.save(ctx, resultList);
                        }
                        resultList.clear();
                        resultMap.clear();
                        throw new InterruptedException(
                                "Failed to get Google+ token during keyword crawling! Try later.");
                    }

                } else {
                    logger.info("Get Token -> {}", token);
                }

                try {
                    jsonObject = BaseGpCrawler.crawlActivitiesByKeyword(
                            token.getAccessToken(), keyword, lang, nextToken);
                    if (jsonObject == null) {
                        throw new DataCrawlException(
                                "Cannot get the Json Object in crawlActivitiesByKeyword.");
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
                                "Failed to crawl Google+ by Keyword {} with retry ({}) times.",
                                keyword, retryNum);
                        throw new DataCrawlException(
                                "Failed to crawl Google+ by Keyword " + keyword
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
                        if ((!((AbstractTaskExecutor) executor).isCacheable() && resultList
                                .isEmpty()) || resultMap.isEmpty()) {
                            // Throw interrupedException to cancel
                            throw new InterruptedException(
                                    "Failed to get Google+ token at the beginning of keyword crawling! Try later.");
                        } else {
                            if (!((AbstractTaskExecutor) executor)
                                    .isTestFlagOn()) {
                                if (((AbstractTaskExecutor) executor)
                                        .isCacheable()) {
                                    for (Map.Entry<String, String> entry : resultMap
                                            .entrySet()) {
                                        String value = entry.getValue();
                                        resultList.add(value);
                                    }
                                }
                                executor.save(ctx, resultList);
                            }
                            resultList.clear();
                            resultMap.clear();
                            throw new InterruptedException(
                                    "Failed to get Google+ token during keyword crawling! Try later.");
                        }

                    } else {
                        logger.info("Get Token -> {}", token);
                    }

                    try {
                        jsonObject = BaseGpCrawler.crawlActivitiesByKeyword(
                                token.getAccessToken(), keyword, lang,
                                nextToken);
                        if (jsonObject == null) {
                            throw new DataCrawlException(
                                    "Cannot get the Json Object in crawlActivitiesByKeyword.");
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
                                    "Failed to crawl Google+ by Keyword {} with retry ({}) times.",
                                    keyword, retryNum);
                            throw new DataCrawlException(
                                    "Failed to crawl Google+ by Keyword "
                                            + keyword + " with retry ("
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

                    String id = activity.getString("id");
                    String published = activity.getString("published");
                    Date publishTime = DateUtil.parseDate(published);
                    logger.debug("Publish Time {}, id {}", publishTime, id);
                    if (publishTime.before(fromTime)) {
                        logger.debug(
                                "Publish Time {} before from time ---> {}.",
                                publishTime, fromTime);
                        toBreak = true;
                        break;
                    } else if (publishTime.after(toTime)) {
                        continue;
                    } else if (publishTime.before(toTime)
                            && publishTime.after(fromTime)) {
                        // Currently only filter English text
                        if (lang.equalsIgnoreCase("en")
                                && checkOnlyAccountInfoContainsKeyword(
                                        activity, pattern)) {
                            continue;
                        }
                        // There are multiple options to do caching. Now Lucene
                        // index cache is used.
                        // maybe we will use redis for cache in future ,but not
                        // now
                        if (((AbstractTaskExecutor) executor).isCacheable()) {
                            resultMap.putIfAbsent(id, activity.toString());
                        } else {
                            resultList.add(activity.toString());
                        }
                        count++;

                        // / Jedis implements Closable. Hence, the jedis
                        // instance will be auto-closed after the last
                        // statement.
                        // try (Jedis jedis =
                        // RedisUtil.getJedisPool().getResource()) {
                        // String id = activity.getString("id");
                        // if ("1".equals(jedis.get(id))) {
                        // logger.info("Get post id -> {} has been Crawlled",
                        // id);
                        // continue;
                        // } else {
                        // jedis.set(id, "1");
                        // }
                        // }
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

        // Reduce duplicated posts in HIVE
        if (((AbstractTaskExecutor) executor).isCacheable()) {
            LuceneUtils.getUnCachedPosts(LuceneUtils.MediaIndexPath.GooglePlus,
                    resultMap, resultList);
        }

        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.debug(
                "G+: keyword  {}, total post count {},  post to HIVE count {}, elapsed Time(Seconds) {}",
                keyword, count, resultList.size(), nanos);

        return executor.isCanceled() ? null : resultList;
    }

    public boolean checkOnlyAccountInfoContainsKeyword(JSONObject activity,
            Pattern pattern) {
        JSONObject tempObj = activity.getJSONObject("object");
        String text = "";
        if (tempObj != null && tempObj.has("content")) {
            text = tempObj.getString("content");
            logger.debug("Text to filter : {}", text);
        }
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

}
