package com.sap.cisp.xhna.data.executor.twitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.User;

import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TokenManager;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class TwitterSocialAccountExecutor extends AbstractTaskExecutor {

    private static Logger logger = LoggerFactory
            .getLogger(TwitterSocialAccountExecutor.class);
    private IToken token;

    @SuppressWarnings("unchecked")
    @Override
    public List<String> execute(Map<String, Object> ctx) throws Exception {
        logger.info("Begin to get accounts info.");
        List<String> accountList = (List<String>) ctx.get("accounts");
        logger.info("Account List ==> {}", accountList);
        if (accountList == null) {
            ITask task = (ITask) ctx.get("task");
            TaskParam taskParam = task.getParam();
            String account = taskParam.getAccount();
            if (account != null) {
                accountList = new ArrayList<String>();
                accountList.add(account);
            } else {
                logger.error("No account error.");
                throw new DataCrawlException("Account is null or empty.");
            }
        }

        List<String> results = new LinkedList<String>();
        Twitter twitter = null;

        for (String account : accountList) {
            int retryNum = 0;
            while (true) {
                if (isCanceled)
                    return null;
                if (account == null || account.equalsIgnoreCase("")) {
                    logger.error("Get account error");
                    throw new DataCrawlException("Account is null or empty.");
                } else {
                    logger.info("Account:{}", account);
                }

                // There is a 1 minutes timeout if no available token, getToken
                // return null
                // For the use case(work mode with server), since get/release
                // token
                // is locally, thread-safe without synchronized
                if (ConfigInstance.runInLocal()) {
                    token = TokenManager.getInstance().getToken(
                            TokenType.Twitter);
                    if (token == null) {
                        logger.error("Get token error");
                        // First time to get token error, just break
                        if (results.isEmpty()) {
                         // Throw interrupedException to cancel
                            throw new InterruptedException(
                                    "Failed to get Twitter token at the beginning! Try later.");
                        } else {
                            if (!isTestFlagOn()) {
                                save(ctx, results);
                            }
                            results.clear();
                            throw new InterruptedException(
                                    "Failed to get Twitter token during crawling! Try later.");
                        }

                    } else {
                        logger.info("Get Token -> {}", token);
                    }

                    // Initialize twitter instance
                    try {
                        twitter = TwitterSingleton.getInstance()
                                .getSearchInstance(token);
                    } catch (TwitterException e) {
                        logger.error(
                                "Create twitter instance failed. Exception: ",
                                e);
                        throw e;
                    }

                    try {
                        logger.info("Begin to request account {} info.",
                                account);
                        String[] inputNames = { account };
                        ResponseList<User> users = twitter
                                .lookupUsers(inputNames);
                        results.add(TwitterObjectFactory.getRawJSON(users
                                .get(0)));
                        break;
                    } catch (TwitterException e) {
                        TokenManager.getInstance().releaseToken(token,
                                e.getStatusCode(), e);
                        logger.error(
                                "Caught TwitterException. Continue to try other tokens to proceed.",
                                e);
                        if (retryNum < 3) {
                            retryNum++;
                            continue;
                        } else {
                            logger.error(
                                    "Failed to crawl twitter Account Info {} with retry ({}) times.",
                                    account, retryNum);
                            throw new DataCrawlException(
                                    "Failed to crawl twitter Account Info "
                                            + account + " with retry ("
                                            + retryNum + ") times.");
                        }
                    } finally {
                        if (token.getState() == StateEnum.Unavailable) {
                            // Release token normally
                            TokenManager.getInstance().releaseToken(token, 0);
                        }
                    }
                } else {
                    /**
                     * For distribution deployment, since get/release is
                     * remotely and asynchronously, gearman request is not
                     * thread-safe, must ensure the get/release pair operation
                     * integrity. One error case: A lot of executors are started
                     * in parallel, bunch of get token requests sent to gearman
                     * server, the release token request can not be handled in
                     * time, all the executors then be stuck.
                     */
                    synchronized (ConfigInstance.TWITTER_LOCK) {
                        token = TokenManager.getInstance().getToken(
                                TokenType.Twitter);
                        if (token == null) {
                            logger.error("Get token error");
                            // First time to get token error, just break
                            if (results.isEmpty()) {
                             // Throw interrupedException to cancel
                                throw new InterruptedException(
                                        "Failed to get Twitter token at the beginning! Try later.");
                            } else {
                                if (!isTestFlagOn()) {
                                    save(ctx, results);
                                }
                                results.clear();
                                throw new InterruptedException(
                                        "Failed to get Twitter token during crawling! Try later.");
                            }

                        } else {
                            logger.info("Get Token -> {}", token);
                        }

                        // Initialize twitter instance
                        try {
                            twitter = TwitterSingleton.getInstance()
                                    .getSearchInstance(token);
                        } catch (TwitterException e) {
                            logger.error(
                                    "Create twitter instance failed. Exception: ",
                                    e);
                            throw e;
                        }

                        try {
                            logger.info("Begin to request account {} info.",
                                    account);
                            String[] inputNames = { account };
                            ResponseList<User> users = twitter
                                    .lookupUsers(inputNames);
                            results.add(TwitterObjectFactory.getRawJSON(users
                                    .get(0)));
                            break;
                        } catch (TwitterException e) {
                            TokenManager.getInstance().releaseToken(token,
                                    e.getStatusCode(), e);
                            logger.error(
                                    "Caught TwitterException. Continue to try other tokens to proceed.",
                                    e);
                            if (retryNum < 3) {
                                retryNum++;
                                continue;
                            } else {
                                logger.error(
                                        "Failed to crawl twitter Account Info {} with retry ({}) times.",
                                        account, retryNum);
                                throw new DataCrawlException(
                                        "Failed to crawl twitter Account Info "
                                                + account + " with retry ("
                                                + retryNum + ") times.");
                            }
                        } finally {
                            if (token.getState() == StateEnum.Unavailable) {
                                // Release token normally
                                TokenManager.getInstance().releaseToken(token,
                                        0);
                            }
                        }
                    }
                }
            }
        }
        logger.info("Get accounts info finished");
        return isCanceled ? null : results;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        TokenManager.getInstance().init();
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        List<String> accounts = new ArrayList<String>();
        accounts.add("cnn");
        // accounts.add("bbcnews");
        // accounts.add("XinhuaNewsAgency");
        ctx.put("accounts", accounts);
        TaskParam param = new TaskParam(params, null);
        param.setString("task_key", "cnn");
        ITask task = TaskFactory.getInstance().createTask(
                com.sap.cisp.xhna.data.TaskType.SocialMedia_AccountData, null,
                param);
        ctx.put("task", task);
        TwitterSocialAccountExecutor exe = new TwitterSocialAccountExecutor();
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
