package com.sap.cisp.xhna.data.executor.facebook;

import java.net.URL;
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

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.common.SendHttp;
import com.sap.cisp.xhna.data.common.Util;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TokenManager;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class FacebookSocialAccountExecutor extends AbstractTaskExecutor {

    private String URL_PREFX_FORMAT = "https://graph.facebook.com/%s?access_token=%s";
    private static Logger logger = LoggerFactory
            .getLogger(FacebookSocialAccountExecutor.class);
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
        // google guava stopwatch, to caculate the execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();

        List<String> resultList = new LinkedList<String>();
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
                        logger.debug("Get Token -> {}", token);
                    }
                    String url = String.format(URL_PREFX_FORMAT, account,
                            token.getAccessToken());
                    logger.info("Begin to request {}", url);
                    try {
                        JSONObject response = SendHttp.sendGet(new URL(url));
                        if (response == null) {
                            logger.error("Failed to get response from {}", url);
                            throw new DataCrawlException(
                                    "Facebook failed to get response from url "
                                            + url);
                        }
                        logger.info("Get response {}", response);
                        String responseStr = Util.EscapeString(response
                                .toJSONString());
                        resultList.add(responseStr);
                        break;
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
                                    "Failed to crawl Facebook Account Info {} with retry ({}) times.",
                                    account, retryNum);
                            throw new DataCrawlException(
                                    "Failed to crawl Facebook Account Info "
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
                            logger.debug("Get Token -> {}", token);
                        }
                        String url = String.format(URL_PREFX_FORMAT, account,
                                token.getAccessToken());
                        logger.info("Begin to request {}", url);
                        try {
                            JSONObject response = SendHttp
                                    .sendGet(new URL(url));
                            if (response == null) {
                                logger.error("Failed to get response from {}",
                                        url);
                                throw new DataCrawlException(
                                        "Facebook failed to get response from url "
                                                + url);
                            }
                            logger.info("Get response {}", response);
                            String responseStr = Util.EscapeString(response
                                    .toJSONString());
                            resultList.add(responseStr);
                            break;
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
                                        "Failed to crawl Facebook Account Info {} with retry ({}) times.",
                                        account, retryNum);
                                throw new DataCrawlException(
                                        "Failed to crawl Facebook Account Info "
                                                + account + " with retry ("
                                                + retryNum + ") times.");
                            }
                        } catch (Exception e) {
                            // For other exception just throw to executor
                            throw e;
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

        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.info(
                "Get accounts info finished. Elapsed Time(Seconds) {};  Result : {}",
                nanos, resultList);
        return isCanceled ? null : resultList;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        TokenManager.getInstance().init();
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        List<String> accounts = new ArrayList<String>();
        accounts.add("bbcnews");
        accounts.add("cnn");
        accounts.add("XinhuaNewsAgency");
        // ctx.put("accounts", accounts);
        TaskParam param = new TaskParam(params, null);
        param.setString("task_key", "bbcnews");
        ITask task = TaskFactory.getInstance().createTask(
                com.sap.cisp.xhna.data.TaskType.SocialMedia_AccountData, null,
                param);
        ctx.put("task", task);
        FacebookSocialAccountExecutor exe = new FacebookSocialAccountExecutor();
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
