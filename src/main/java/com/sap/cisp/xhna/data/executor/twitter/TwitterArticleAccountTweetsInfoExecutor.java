package com.sap.cisp.xhna.data.executor.twitter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

import com.google.common.base.Stopwatch;
import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.TokenManager;

public class TwitterArticleAccountTweetsInfoExecutor extends
        AbstractTaskExecutor {

    private static Logger logger = LoggerFactory
            .getLogger(TwitterArticleAccountTweetsInfoExecutor.class);
    private  final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception {
        // TODO Auto-generated method stub
        final List<String> results = new ArrayList<String>();

        ITask task = (ITask) ctx.get("task");
        TaskParam taskParam = task.getParam();

        String account = taskParam.getAccount();
        if (account == null || account.equalsIgnoreCase("")) {
            logger.error("Get account error");
            throw new DataCrawlException("Account is null or empty.");
        } else {
            logger.info("Begin to track the account {}...", account);
        }
        String startTime = taskParam.getStartTime();
        String endTime = taskParam.getEndTime();
        logger.info("start_time:" + startTime);
        logger.info("end_time:" + endTime);
        // google guava stopwatch, to caculate the execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();

        List<Status> orginalTweets = new ArrayList<Status>();
        TwitterArticleAccountExecutor accountExecutor = new TwitterArticleAccountExecutor();
        // Do not need to remove duplication since we need snapshot
        accountExecutor.setCacheable(false);
        accountExecutor.getResultsByAccount(ctx, account, startTime, endTime,
                orginalTweets);

        if (orginalTweets != null) {
            String searchKeyword = "@" + account;
            List<Status> replys = new ArrayList<Status>();
            TwitterArticleKeywordExecutor keywordExecutor = new TwitterArticleKeywordExecutor();
            // Do not need to remove duplication since we need snapshot
            keywordExecutor.setCacheable(false);
            keywordExecutor.getResultsByKeyword(ctx, searchKeyword, startTime,
                    endTime, replys);

            for (Status status : orginalTweets) {
                long statusId = status.getId();
                int retweetCount = status.getRetweetCount();
                int favoriteCount = status.getFavoriteCount();
                String crawlTime = sdf.format(new Date());
                int replyCount = 0;
                int mediaCount = 0;

                /* get the reply count */
                for (Status reply : replys) {
                    long inReplyStatusId = reply.getInReplyToStatusId();
                    if (inReplyStatusId == statusId) {
                        replyCount++;
                    }
                }

                /* get the media count from json */
                if (TwitterObjectFactory.getRawJSON(status) != null) {
                    JSONObject original = new JSONObject(
                            TwitterObjectFactory.getRawJSON(status));
                    if (original.has("extended_entities")) {
                        JSONObject extended_entities = original
                                .getJSONObject("extended_entities");
                        if (extended_entities != null) {
                            JSONArray medias = extended_entities
                                    .getJSONArray("media");
                            mediaCount = medias.length();
                        } else {
                            logger.error(
                                    "!!! Cannot get the media count. Use the default value {}.",
                                    mediaCount);
                        }
                    }
                }

                JSONObject jsonObj = new JSONObject();
                jsonObj.put("id", statusId);
                jsonObj.put("crawlTime", crawlTime);
                jsonObj.put("replyCount", replyCount);
                jsonObj.put("retweetCount", retweetCount);
                jsonObj.put("favoriteCount", favoriteCount);
                jsonObj.put("mediaCount", mediaCount);

                /* insert into attachment info table */
                results.add(jsonObj.toString());
            }
        }
        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.info("Crawl account {} posts info; User post number {}, Elapsed Time -> {}",
                account, orginalTweets.size(), nanos);
        return isCanceled ? null : results;
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
        currentCal.add(Calendar.HOUR, -10);
        String from = sdf.format(currentCal.getTime());
        params.put("task_key", "bbcnews");
        params.put("start_time", from);
        System.out.println("Current time: " + System.currentTimeMillis());
        params.put("end_time", to);
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_Account_Article,
                        null, param);
        ctx.put("task", task);
        TwitterArticleAccountTweetsInfoExecutor exe = new TwitterArticleAccountTweetsInfoExecutor();
        exe.setTestFlagOn(true);
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService taskExecutorPool = new TaskExecutorPool(10, 10, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), factory);
        exe.setTask(task);
        for (int i = 0; i < 1; i++) {
            try {
                List<String> result = (List<String>) taskExecutorPool.submit(
                        exe).get();
                logger.debug("Result --> {}", result);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
}
