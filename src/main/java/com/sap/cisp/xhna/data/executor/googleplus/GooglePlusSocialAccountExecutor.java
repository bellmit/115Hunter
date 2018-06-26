package com.sap.cisp.xhna.data.executor.googleplus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

public class GooglePlusSocialAccountExecutor extends AbstractTaskExecutor {
    private static Logger logger = LoggerFactory
            .getLogger(GooglePlusSocialAccountExecutor.class);
    private IToken token;

    @SuppressWarnings("unchecked")
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

        for (String account : accountList) {

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
                        TokenType.GooglePlus);
                if (token == null) {
                    logger.error("Get GooglePlus token error");
                    // First time to get token error, just break
                    if (results.isEmpty()) {
                        // Throw interrupedException to cancel
                        throw new InterruptedException(
                                "Failed to get GooglePlus token at the beginning! Try later.");
                    } else {
                        if (!isTestFlagOn()) {
                            save(ctx, results);
                        }
                        results.clear();
                        throw new InterruptedException(
                                "Failed to get GooglePlus token during crawling! Try later.");
                    }
                } else {
                    logger.info("Get Token -> {}", token);
                }

                try {
                    logger.info("Begin to request account {} info.", account);
                    /* this account is name or id, so guess it as id */
                    JSONObject jsonUserId = null;
                    jsonUserId = BaseGpCrawler.crawlProfileByUserId(
                            token.getAccessToken(), account);
                    if (jsonUserId != null) {
                        results.add(jsonUserId.toString());
                    } else {
                        JSONObject UserIdJson = BaseGpCrawler
                                .crawlProfileByUserName(token.getAccessToken(),
                                        account);
                        String userId = getUserIdByProfile(UserIdJson);
                        if (userId != null) {
                            jsonUserId = BaseGpCrawler.crawlProfileByUserId(
                                    token.getAccessToken(), userId);
                            if (jsonUserId != null) {
                                results.add(jsonUserId.toString());
                            } else {
                                logger.error("Fails to get user info by id");
                                throw new DataCrawlException(
                                        "Fails to get user info by id");
                            }
                        } else {
                            logger.error("Fails to get user id by name");
                            throw new DataCrawlException(
                                    "Fails to get user id by name");
                        }
                    }
                    break;
                } catch (IOException e) {
                    logger.error("Fails Caught GooglePlus", e);
                    throw new DataCrawlException("Fails Caught GooglePlus");
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
                        logger.error("Get GooglePlus token error");
                        // First time to get token error, just break
                        if (results.isEmpty()) {
                            // Throw interrupedException to cancel
                            throw new InterruptedException(
                                    "Failed to get GooglePlus token at the beginning! Try later.");
                        } else {
                            if (!isTestFlagOn()) {
                                save(ctx, results);
                            }
                            results.clear();
                            throw new InterruptedException(
                                    "Failed to get GooglePlus token during crawling! Try later.");
                        }
                    } else {
                        logger.info("Get Token -> {}", token);
                    }

                    try {
                        logger.info("Begin to request account {} info.",
                                account);
                        /* this account is name or id, so guess it as id */
                        JSONObject jsonUserId = null;
                        jsonUserId = BaseGpCrawler.crawlProfileByUserId(
                                token.getAccessToken(), account);
                        if (jsonUserId != null) {
                            results.add(jsonUserId.toString());
                        } else {
                            JSONObject UserIdJson = BaseGpCrawler
                                    .crawlProfileByUserName(
                                            token.getAccessToken(), account);
                            String userId = getUserIdByProfile(UserIdJson);
                            if (userId != null) {
                                jsonUserId = BaseGpCrawler
                                        .crawlProfileByUserId(
                                                token.getAccessToken(), userId);
                                if (jsonUserId != null) {
                                    results.add(jsonUserId.toString());
                                } else {
                                    logger.error("Fails to get user info by id");
                                    throw new DataCrawlException(
                                            "Fails to get user info by id");
                                }
                            } else {
                                logger.error("Fails to get user id by name");
                                throw new DataCrawlException(
                                        "Fails to get user id by name");
                            }
                        }
                        break;
                    } catch (IOException e) {
                        logger.error("Fails Caught GooglePlus", e);
                        throw new DataCrawlException("Fails Caught GooglePlus");
                    } finally {
                        if (token.getState() == StateEnum.Unavailable) {
                            // Release token normally
                            TokenManager.getInstance().releaseToken(token, 0);
                        }
                    }
                }
            }
        }
        logger.info("Get accounts info finished");
        // System.out.println(results);
        return isCanceled ? null : results;
    }

    public String getUserIdByProfile(JSONObject jsonObject) {
        JSONArray users = jsonObject.getJSONArray("items");
        String userId = null;
        if (!users.isNull(0)) {
            JSONObject user = users.getJSONObject(0);
            userId = user.getString("id");
        }
        return userId;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        TokenManager.getInstance().init();
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        List<String> accounts = new ArrayList<String>();
        accounts.add("107045876535773972576");
        // accounts.add("BBC News");
        // accounts.add("dsagdasgafasfdasf33123");
        ctx.put("accounts", accounts);
        TaskParam param = new TaskParam(params, null);
        param.setString("task_key", "107045876535773972576");
//        param.setString("task_key", "cnn");
        ITask task = TaskFactory.getInstance().createTask(
                com.sap.cisp.xhna.data.TaskType.SocialMedia_AccountData, null,
                param);
        ctx.put("task", task);
        GooglePlusSocialAccountExecutor exe = new GooglePlusSocialAccountExecutor();
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService taskExecutorPool = new TaskExecutorPool(10, 10, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                factory);
        exe.setTask(task);
        exe.setTestFlagOn(true);
        try {
            List<String> result = (List<String>) taskExecutorPool.submit(exe)
                    .get();
            logger.info("Crawl result =====> " + result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.exit(0);
    }

}
