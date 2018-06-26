package com.sap.cisp.xhna.data.executor.barrier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sap.cisp.xhna.data.executor.lottery.GD11in5Crawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.index.LuceneUtils;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.forum.SemiWikiForumCrawler;
import com.sap.cisp.xhna.data.executor.receiver.LocalReceiver;
import com.sap.cisp.xhna.data.executor.traditional.GetNews;
import com.sap.cisp.xhna.data.executor.traditional.NewsCrawler;
import com.sap.cisp.xhna.data.executor.traditional.NewsSpider;
import com.sap.cisp.xhna.data.executor.traditional.RomeCrawler;
import com.sap.cisp.xhna.data.task.ITask;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sap.cisp.xhna.data.executor.forum.stock.GubaCrawler;

public class TaskBarrierExecutorBuilder {
    private static Logger logger = LoggerFactory
            .getLogger(TaskBarrierExecutorBuilder.class);
    private static final int TASK_BUILD_INTERVAL = 10000;
    // Need to restrict the maximum threads to write HIVE
    private final int TASK_BARRIER_BOUND = 5;
    private final int SMALL_TASKS_GROUP_SIZE = 6;
    public final static int MAXIMUM_GROUP_SIZE = 80;
    private final static int BASE_CHOPPED_SIZE = 20;
    private ExecutorService taskExecutorPool = null;

    private TaskBarrierExecutorBuilder() {
        ThreadFactory factory = new TaskThreadFactory();
        taskExecutorPool = new TaskExecutorPool(100, 100, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100),
                factory);
        // Strategy when executor pool reach maximumPoolSize, currently use
        // CallerRunsPolicy
        ((ThreadPoolExecutor) taskExecutorPool)
                .setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static TaskBarrierExecutorBuilder getInstance() {
        return TaskBarrierExecutorBuilderHolder.instance;
    }

    private static class TaskBarrierExecutorBuilderHolder {
        public static TaskBarrierExecutorBuilder instance = new TaskBarrierExecutorBuilder();
    }

    @SuppressWarnings("rawtypes")
    public void buildWebPageTaskBarrier(List<String> urlList,
            Map<String, Object> ctx, ITaskExecutor parentExecutor,
            final CyclicBarrier parentBarrier) throws Exception {
        // Potential risk : if the available thread size < barrier size, the
        // application will be stuck!
        logger.info("Task barrier for url list : {}, size {}", urlList,
                urlList.size());
        // control the batch size
        int ACTUAL_CHOPPED_SIZE = computeChoppedSize(urlList.size());
        logger.debug("The computed chopped size -> {}", ACTUAL_CHOPPED_SIZE);
        int choppedLen = urlList.size() / ACTUAL_CHOPPED_SIZE;
        choppedLen = Math.max(5, choppedLen);
        logger.debug("The final chopped len -> {}", choppedLen);
        List<List<String>> choppedUrlList = LuceneUtils.chopped(urlList,
                choppedLen);
        // Restrict threads number used by task barrier within
        // TASK_BARRIER_BOUND
        Semaphore semaphore = new Semaphore(TASK_BARRIER_BOUND);
        CyclicBarrier choppedBarrier = new CyclicBarrier(choppedUrlList.size(),
                new BarrierAction(parentBarrier));
        // For small number of url list, can use one thread per url
        if (urlList.size() < SMALL_TASKS_GROUP_SIZE) {
            for (List<String> subUrlList : choppedUrlList) {
                semaphore.acquire();
                CyclicBarrier barrier = new CyclicBarrier(subUrlList.size(),
                        new BarrierAction(choppedBarrier, semaphore));
                logger.info("Task barrier for chopped url list size -> {}.",
                        subUrlList.size());
                String methodName = (String) ctx.get("methodName");

                for (String url : subUrlList) {
                    // Create taskBarrierExecutor
                    Class[] parameterTypes = (Class[]) ctx
                            .get("parameterTypes");
                    String className = (String) ctx.get("className");
                    TaskBarrierExecutor executor = null;
                    switch (className) {
                    case "GetNews":
                        executor = new GetNews(barrier, methodName,
                                parameterTypes, url, parentExecutor);
                        break;
                    case "NewsCrawler":
                        executor = new NewsCrawler(barrier, methodName,
                                parameterTypes, url, parentExecutor);
                        break;
                    case "RomeCrawler":
                        executor = new RomeCrawler(barrier, methodName,
                                parameterTypes, url, parentExecutor);
                        break;
                    case "SemiWikiForumCrawler":
                        executor = new SemiWikiForumCrawler(barrier,
                                methodName, parameterTypes, url, parentExecutor);
                        break;
                    case "GubaCrawler":
                        executor = new GubaCrawler(barrier, methodName,
                                parameterTypes, url, parentExecutor);
                        break;
                    default:
                        logger.error("Unsupported crawler class 1 {}",
                                className);
                        return;
                    }

                    executor.setTask((ITask) ctx.get("task"));
                    logger.info(
                            "<<<<<< Check the task barrier thread pool attributes : Maximum pool size -> {}, Largest pool size -> {}, Current pool size -> {}, Active thread count -> {}, Task count -> {}, completedTaskCount -> {}",
                            ((ThreadPoolExecutor) taskExecutorPool)
                                    .getMaximumPoolSize(),
                            ((ThreadPoolExecutor) taskExecutorPool)
                                    .getLargestPoolSize(),
                            ((ThreadPoolExecutor) taskExecutorPool)
                                    .getPoolSize(),
                            ((ThreadPoolExecutor) taskExecutorPool)
                                    .getActiveCount(),
                            ((ThreadPoolExecutor) taskExecutorPool)
                                    .getTaskCount(),
                            ((ThreadPoolExecutor) taskExecutorPool)
                                    .getCompletedTaskCount());
                    LocalReceiver receiver = new LocalReceiver(executor,
                            taskExecutorPool);
                    receiver.action();
                }
                Thread.sleep(TASK_BUILD_INTERVAL);
            }
        } else { // big size of url list
            for (List<String> subUrlList : choppedUrlList) {
                semaphore.acquire();
                    CyclicBarrier barrier = new CyclicBarrier(1, new BarrierAction(
                        choppedBarrier, semaphore));
                logger.info("Task barrier for chopped url list size -> {}.",
                        subUrlList.size());
                String methodName = (String) ctx.get("methodName");

                // Create taskBarrierExecutor
                Class[] parameterTypes = (Class[]) ctx.get("parameterTypes");
                String className = (String) ctx.get("className");
                TaskBarrierExecutor executor = null;
                switch (className) {
                case "GetNews":
                    executor = new GetNews(barrier, methodName, parameterTypes,
                            subUrlList, parentExecutor);
                    break;
                case "NewsCrawler":
                    executor = new NewsCrawler(barrier, methodName,
                            parameterTypes, subUrlList, parentExecutor);
                    break;
                case "RomeCrawler":
                    executor = new RomeCrawler(barrier, methodName,
                            parameterTypes, subUrlList, parentExecutor);
                    break;
                case "SemiWikiForumCrawler":
                    executor = new SemiWikiForumCrawler(barrier, methodName,
                            parameterTypes, subUrlList, parentExecutor);
                    break;
                case "GubaCrawler":
                    executor = new GubaCrawler(barrier, methodName,
                            parameterTypes, subUrlList, parentExecutor);
                    break;
                case "GD11in5Crawler":
                    executor = new GD11in5Crawler(barrier, methodName,
                            parameterTypes, subUrlList, parentExecutor);
                    break;
                default:
                    logger.error("Unsupported crawler class 2 {}", className);
                    return;
                }

                executor.setTask((ITask) ctx.get("task"));
                logger.info(
                        "<<<<<< Check the task barrier thread pool attributes : Maximum pool size -> {}, Largest pool size -> {}, Current pool size -> {}, Active thread count -> {}, Task count -> {}, completedTaskCount -> {}",
                        ((ThreadPoolExecutor) taskExecutorPool)
                                .getMaximumPoolSize(),
                        ((ThreadPoolExecutor) taskExecutorPool)
                                .getLargestPoolSize(),
                        ((ThreadPoolExecutor) taskExecutorPool).getPoolSize(),
                        ((ThreadPoolExecutor) taskExecutorPool)
                                .getActiveCount(),
                        ((ThreadPoolExecutor) taskExecutorPool).getTaskCount(),
                        ((ThreadPoolExecutor) taskExecutorPool)
                                .getCompletedTaskCount());
                LocalReceiver receiver = new LocalReceiver(executor,
                        taskExecutorPool);
                receiver.action();
                Thread.sleep(TASK_BUILD_INTERVAL);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void buildWebPageTaskBarrier(Map<String, SyndEntry> urlMap,
            Map<String, Object> ctx, ITaskExecutor parentExecutor,
            final CyclicBarrier parentBarrier) throws Exception {
        logger.debug("url map size {}", urlMap.size());
        // control the batch size
        int ACTUAL_CHOPPED_SIZE = computeChoppedSize(urlMap.size());
        logger.debug("The computed chopped size -> {}", ACTUAL_CHOPPED_SIZE);
        int choppedLen = urlMap.size() / ACTUAL_CHOPPED_SIZE;
        choppedLen = Math.max(5, choppedLen);
        logger.debug("The final chopped len -> {}", choppedLen);

        List<List<Map.Entry<String, SyndEntry>>> choopedEntryList = LuceneUtils
                .chopped(urlMap, choppedLen);
        // Restrict threads number used by task barrier within
        // TASK_BARRIER_BOUND
        Semaphore semaphore = new Semaphore(TASK_BARRIER_BOUND);
        CyclicBarrier choppedBarrier = new CyclicBarrier(
                choopedEntryList.size(), new BarrierAction(parentBarrier));
        // For small number of url list, can use one thread per url
        if (urlMap.size() < SMALL_TASKS_GROUP_SIZE) {
            for (List<Map.Entry<String, SyndEntry>> subEntryList : choopedEntryList) {
                semaphore.acquire();
                CyclicBarrier barrier = new CyclicBarrier(subEntryList.size(),
                        new BarrierAction(choppedBarrier, semaphore));
                logger.info("Task barrier for chopped url Map size -> {}.",
                        subEntryList.size());
                String methodName = (String) ctx.get("methodName");

                for (Map.Entry<String, SyndEntry> entry : subEntryList) {
                    String url = entry.getKey();
                    SyndEntry entryValue = entry.getValue();

                    // Create taskBarrierExecutor
                    Class[] parameterTypes = (Class[]) ctx
                            .get("parameterTypes");
                    String className = (String) ctx.get("className");
                    TaskBarrierExecutor executor = null;
                    switch (className) {
                    case "RomeCrawler":
                        executor = new RomeCrawler(barrier, methodName,
                                parameterTypes, url, entryValue, parentExecutor);
                        break;
                    case "NewsSpider":
                        executor = new NewsSpider(barrier, methodName,
                                parameterTypes, url, entryValue, parentExecutor);
                        break;
                    default:
                        logger.error("Unsupported crawler class 3 {}",
                                className);
                        return;
                    }

                    executor.setTask((ITask) ctx.get("task"));
                    logger.info(
                            "<<<<<< Check the task barrier thread pool attributes : Maximum pool size -> {}, Largest pool size -> {}, Current pool size -> {}, Active thread count -> {}, Task count -> {}, completedTaskCount -> {}",
                            ((ThreadPoolExecutor) taskExecutorPool)
                                    .getMaximumPoolSize(),
                            ((ThreadPoolExecutor) taskExecutorPool)
                                    .getLargestPoolSize(),
                            ((ThreadPoolExecutor) taskExecutorPool)
                                    .getPoolSize(),
                            ((ThreadPoolExecutor) taskExecutorPool)
                                    .getActiveCount(),
                            ((ThreadPoolExecutor) taskExecutorPool)
                                    .getTaskCount(),
                            ((ThreadPoolExecutor) taskExecutorPool)
                                    .getCompletedTaskCount());

                    LocalReceiver receiver = new LocalReceiver(executor,
                            taskExecutorPool);
                    receiver.action();
                }
                Thread.sleep(TASK_BUILD_INTERVAL);
            }
        } else {
            for (List<Map.Entry<String, SyndEntry>> subEntryList : choopedEntryList) {
                semaphore.acquire();
                CyclicBarrier barrier = new CyclicBarrier(1, new BarrierAction(
                        choppedBarrier, semaphore));
                logger.info("Task barrier for chopped url Map size -> {}.",
                        subEntryList.size());
                String methodName = (String) ctx.get("methodName");

                // Create taskBarrierExecutor
                Class[] parameterTypes = (Class[]) ctx.get("parameterTypes");
                String className = (String) ctx.get("className");
                TaskBarrierExecutor executor = null;
                switch (className) {
                case "RomeCrawler":
                    executor = new RomeCrawler(barrier, methodName,
                            parameterTypes, subEntryList, true, parentExecutor);
                    break;
                case "NewsSpider":
                    executor = new NewsSpider(barrier, methodName,
                            parameterTypes, subEntryList, true, parentExecutor);
                    break;
                default:
                    logger.error("Unsupported crawler class 4 {}", className);
                    return;
                }

                executor.setTask((ITask) ctx.get("task"));
                logger.info(
                        "<<<<<< Check the task barrier thread pool attributes : Maximum pool size -> {}, Largest pool size -> {}, Current pool size -> {}, Active thread count -> {}, Task count -> {}, completedTaskCount -> {}",
                        ((ThreadPoolExecutor) taskExecutorPool)
                                .getMaximumPoolSize(),
                        ((ThreadPoolExecutor) taskExecutorPool)
                                .getLargestPoolSize(),
                        ((ThreadPoolExecutor) taskExecutorPool).getPoolSize(),
                        ((ThreadPoolExecutor) taskExecutorPool)
                                .getActiveCount(),
                        ((ThreadPoolExecutor) taskExecutorPool).getTaskCount(),
                        ((ThreadPoolExecutor) taskExecutorPool)
                                .getCompletedTaskCount());

                LocalReceiver receiver = new LocalReceiver(executor,
                        taskExecutorPool);
                receiver.action();
                Thread.sleep(TASK_BUILD_INTERVAL);
            }
        }
    }

    public static int computeChoppedSize(int urlListSize) {
        // control the batch size
        int ACTUAL_CHOPPED_SIZE = urlListSize / 20;
        int i = 1;
        while (ACTUAL_CHOPPED_SIZE > MAXIMUM_GROUP_SIZE) {
            ACTUAL_CHOPPED_SIZE = (int) (urlListSize / (20 * i));
            i++;
        }
        return Math.max(ACTUAL_CHOPPED_SIZE, BASE_CHOPPED_SIZE);
    }

    private static class BarrierAction implements Runnable {
        private CyclicBarrier parentBarrier;
        private Semaphore semaphore;

        public BarrierAction(CyclicBarrier parentBarrier, Semaphore semaphore) {
            this.parentBarrier = parentBarrier;
            this.semaphore = semaphore;
        }

        public BarrierAction(CyclicBarrier parentBarrier) {
            this.parentBarrier = parentBarrier;
            this.semaphore = null;
        }

        public void run() {

            try {
                if (semaphore != null) {
                    semaphore.release();
                    logger.debug(
                            "Chopped Task Barrier is done. Release semaphore and available permits -> {}",
                            semaphore.availablePermits());
                } else {
                    logger.debug("Task Barrier is done.");
                }
                parentBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                logger.error("Caunght Exception on task barrier completion.", e);
            }
        }
    }
}
