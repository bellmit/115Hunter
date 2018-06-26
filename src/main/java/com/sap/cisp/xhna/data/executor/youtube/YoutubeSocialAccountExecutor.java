package com.sap.cisp.xhna.data.executor.youtube;

import java.net.MalformedURLException;
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

public class YoutubeSocialAccountExecutor extends AbstractTaskExecutor {

    private String channelUrlFmt = "https://www.googleapis.com/youtube/v3/"
            + "channels?part=snippet,contentDetails,statistics"
            + "&forUsername=%s" + "&key=%s";
    private String channelUrlByIdFmt = "https://www.googleapis.com/youtube/v3/"
            + "channels?part=snippet,contentDetails,statistics" + "&id=%s"
            + "&key=%s";
    private IToken token;

    @SuppressWarnings("unchecked")
    @Override
    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception {
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
        boolean isRetryById = false;
        List<String> resultList = new LinkedList<String>();
        for (String channel : accountList) {
            int retryNum = 0;
            while (true) {
                if (isCanceled)
                    return null;
                if (channel == null || channel.equalsIgnoreCase("")) {
                    logger.error("Get account error");
                    throw new DataCrawlException("Account is null or empty.");
                } else {
                    logger.info("Account:{}", channel);
                }

                // There is a 1 minutes timeout if no available token, getToken
                // return null
                // For the use case(work mode with server), since get/release
                // token
                // is locally, thread-safe without synchronized
                if (ConfigInstance.runInLocal()) {
                    token = TokenManager.getInstance().getToken(
                            TokenType.YouTube);
                    if (token == null) {
                        logger.error("Get token error");
                        // First time to get token error, just break
                        if (resultList.isEmpty()) {
                         // Throw interrupedException to cancel
                            throw new InterruptedException(
                                    "Failed to get YouTube token at the beginning of getting accout info! Try later.");
                        } else {
                            if (!isTestFlagOn()) {
                                save(ctx, resultList);
                            }
                            resultList.clear();
                            throw new InterruptedException(
                                    "Failed to get YouTube token during getting account info! Try later.");
                        }

                    } else {
                        logger.info("Get Token -> {}", token.getAccessToken());
                    }
                    String url = "";
                    if (!isRetryById) {
                        url = String.format(channelUrlFmt, channel,
                                token.getAccessToken());
                    } else {
                        url = String.format(channelUrlByIdFmt, channel,
                                token.getAccessToken());
                    }
                    logger.info("Begin to request {}", url);
                    try {
                        JSONObject jsonRes = SendHttp.sendGet(new URL(url));
                        if (jsonRes != null) {
                            if (!jsonRes.getJSONArray("items").isEmpty()) {
                                String result = Util.EscapeString(jsonRes
                                        .toJSONString());
                                logger.debug("Get response {}", result);
                                resultList.add(result);
                                break;
                            } else {
                                if (!isRetryById) {
                                    logger.error(
                                            "Channel information is empty with channel {}. Retry by means of ID.",
                                            channel);
                                    isRetryById = true;
                                    continue;
                                } else {
                                    logger.error(
                                            "Channel information is empty with channel {} after retry by means of ID.",
                                            channel);
                                    throw new DataCrawlException(
                                            "YouTube failed to get response from url "
                                                    + url);
                                }
                            }
                        } else {
                            logger.error("Failed to get response from {}", url);
                            throw new DataCrawlException(
                                    "YouTube failed to get response from url "
                                            + url);
                        }

                    } catch (MalformedURLException me) {
                        // TODO Auto-generated catch block
                        throw me;
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
                                    "Failed to crawl YouTube Account Info {} with retry ({}) times.",
                                    channel, retryNum);
                            throw new DataCrawlException(
                                    "Failed to crawl YouTube Account Info "
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
                } else {
                    /**
                     * For distribution deployment, since get/release is
                     * remotely and asynchronously, gearman request is not
                     * thread-safe, must ensure the get/release pair operation
                     * integrity. Error case: - A lot of executors are started
                     * in parallel, bunch of get token requests sent to gearman
                     * server, the release token request can not be handled in
                     * time(executor service, all the executors then be stuck. -
                     * Since IToken is not thread-safe, when get/release token
                     * asynchronously, setState/getState is error prone. Keep
                     * token without lock to make local get/release token
                     * operation more efficient.
                     */
                    synchronized (ConfigInstance.YOUTUBE_LOCK) {
                        token = TokenManager.getInstance().getToken(
                                TokenType.YouTube);
                        if (token == null) {
                            logger.error("Get token error");
                            // First time to get token error, just break
                            if (resultList.isEmpty()) {
                                // Throw interrupedException to cancel
                                throw new InterruptedException(
                                        "Failed to get YouTube token at the beginning of getting accout info! Try later.");
                            } else {
                                if (!isTestFlagOn()) {
                                    save(ctx, resultList);
                                }
                                resultList.clear();
                                throw new InterruptedException(
                                        "Failed to get YouTube token during getting account info! Try later.");
                            }

                        } else {
                            logger.info("Get Token -> {}",
                                    token.getAccessToken());
                        }
                        String url = "";
                        if (!isRetryById) {
                            url = String.format(channelUrlFmt, channel,
                                    token.getAccessToken());
                        } else {
                            url = String.format(channelUrlByIdFmt, channel,
                                    token.getAccessToken());
                        }
                        logger.info("Begin to request {}", url);
                        try {
                            JSONObject jsonRes = SendHttp.sendGet(new URL(url));
                            if (jsonRes != null) {
                                if (!jsonRes.getJSONArray("items").isEmpty()) {
                                    String result = Util.EscapeString(jsonRes
                                            .toJSONString());
                                    logger.debug("Get response {}", result);
                                    resultList.add(result);
                                    break;
                                } else {
                                    if (!isRetryById) {
                                        logger.error(
                                                "Channel information is empty with channel {}. Retry by means of ID.",
                                                channel);
                                        isRetryById = true;
                                        continue;
                                    } else {
                                        logger.error(
                                                "Channel information is empty with channel {} after retry by means of ID.",
                                                channel);
                                        throw new DataCrawlException(
                                                "YouTube failed to get response from url "
                                                        + url);
                                    }
                                }
                            } else {
                                logger.error("Failed to get response from {}",
                                        url);
                                throw new DataCrawlException(
                                        "YouTube failed to get response from url "
                                                + url);
                            }

                        } catch (MalformedURLException me) {
                            // TODO Auto-generated catch block
                            throw me;
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
                                        "Failed to crawl YouTube Account Info {} with retry ({}) times.",
                                        channel, retryNum);
                                throw new DataCrawlException(
                                        "Failed to crawl YouTube Account Info "
                                                + channel + " with retry ("
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
        logger.info("Get accounts info finished, elapsed Time(Seconds) {}",
                nanos);
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
        param.setString("task_key", "UCupvZG-5ko_eiXAupbDfxWw");
        ITask task = TaskFactory.getInstance().createTask(
                com.sap.cisp.xhna.data.TaskType.SocialMedia_AccountData, null,
                param);
        ctx.put("task", task);
        YoutubeSocialAccountExecutor exe = new YoutubeSocialAccountExecutor();
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
