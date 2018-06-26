package com.sap.cisp.xhna.data.executor.barrier;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CyclicBarrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.sap.cisp.xhna.data.common.cache.LRUCacheUtils;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.traditional.GetSourceUrlUtil;
import com.sap.cisp.xhna.data.task.ITask;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

@SuppressWarnings("rawtypes")
public class TaskBarrierExecutor extends AbstractTaskExecutor {
    public static final String NEWLINE = System.getProperty("line.separator",
            "\r\n");
    public static final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss+0000");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private CyclicBarrier barrier;
    private String methodName;
    private String url = "";
    private List<String> urlList = null;
    private List<Map.Entry<String, SyndEntry>> entryList = null;
    private boolean isEntryNeeded;
    private SyndEntry entry;
    private Class[] parameterTypes;
    protected ITaskExecutor parentExecutor;
    private HashMap<String,Boolean> resultMap = new HashMap<String, Boolean>();

    static {
        sdf.setTimeZone(GMT);
    }

    private static Logger logger = LoggerFactory
            .getLogger(TaskBarrierExecutor.class);

    public TaskBarrierExecutor() {

    }

    public TaskBarrierExecutor(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, String url, ITaskExecutor parentExecutor) {
        this.barrier = barrier;
        this.methodName = methodName;
        this.url = url;
        this.parameterTypes = parameterTypes;
        this.parentExecutor = parentExecutor;
        this.isEntryNeeded = false;
        this.setCleanTaskSkipped(true);
    }

    public TaskBarrierExecutor(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, List<String> urlList, ITaskExecutor parentExecutor) {
        this.barrier = barrier;
        this.methodName = methodName;
        this.urlList = urlList;
        this.parameterTypes = parameterTypes;
        this.parentExecutor = parentExecutor;
        this.isEntryNeeded = false;
        this.setCleanTaskSkipped(true);
    }

    public TaskBarrierExecutor(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, String url, SyndEntry entry,
            ITaskExecutor parentExecutor) {
        this(barrier, methodName, parameterTypes, url, parentExecutor);
        this.entry = entry;
        this.isEntryNeeded = true;
    }
    
    public TaskBarrierExecutor(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, List<Map.Entry<String, SyndEntry>> entryList, boolean isEntryNeed,
            ITaskExecutor parentExecutor) {
        this.barrier = barrier;
        this.methodName = methodName;
        this.entryList = entryList;
        this.parameterTypes = parameterTypes;
        this.parentExecutor = parentExecutor;
        this.isEntryNeeded = true;
        this.setCleanTaskSkipped(true);
    }

    @Override
    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception {
        Method method = this.findMethod(methodName, parameterTypes);
        List<String> result = new ArrayList<String>();
        ctx.put("result", result);
    
        Object resultObj = null;
        if (isEntryNeeded) {
            if(entryList != null && !entryList.isEmpty()) {
                for (Map.Entry<String, SyndEntry> entry : entryList) {
                    String url = entry.getKey();
                    SyndEntry entryValue = entry.getValue();
                    Object resultObjTmp = null;
                    try{
                        resultObjTmp = method.invoke(this, url, entryValue, ctx, parentExecutor);
                    }catch(Exception e) {
                        logger.error("Caught Exception during crawl url {}. Continue with the other url.", url, e);
                        continue;
                        
                    } finally {
                        if(isError) {
                            isError = (resultObjTmp == null);
                        }
                        resultMap.putIfAbsent(url, (resultObjTmp == null));
                        logger.debug("Task Barrier sub-task(url list: url -> {}), result --> {}", url, (resultObjTmp == null)?"null":JSON.toJSONString(resultObjTmp));
                    }
                }
                logger.debug("Task Barrier sub-task(url list) done. Result size--> {}", result.size());
            } else {
                resultObj = method.invoke(this, url, entry, ctx, parentExecutor); 
                isError = (resultObj == null);
                logger.debug("Task Barrier sub-task result --> {}", result);
            }  
        } else {
            if(urlList != null && !urlList.isEmpty()) {
                for (String url : urlList) {
                    Object resultObjTmp = null;
                    try{
                        resultObjTmp = method.invoke(this, url, ctx, parentExecutor);
                        //Avoid too frequent access
                        Thread.sleep(5000);
                    }catch(Exception e) {
                        logger.error("Caught Exception during crawl url {}. Continue with the other url.", url, e);
                        Thread.sleep(60000);
                        continue;
                    } finally {
                        if(isError) {
                            isError = (resultObjTmp == null);
                        }
                        logger.debug("Task Barrier sub-task(url list: url -> {}), result --> {}", url, (resultObjTmp == null)?"null":JSON.toJSONString(resultObjTmp));
                        resultMap.putIfAbsent(url, (resultObjTmp == null));    
                    }
                }
                logger.debug("Task Barrier sub-task(url list) done. Result size --> {}", result.size());
            } else {
                resultObj = method.invoke(this, url, ctx, parentExecutor);
                isError = (resultObj == null);
                logger.debug("Task Barrier sub-task result --> {}", result);
            }
        }       
        return result;
    }

    @Override
    public void error(Map<String, Object> ctx, Exception e) throws Exception {
        isError = true;
        ITask task = (ITask) ctx.get("task");
        if(entryList != null || urlList != null) {
            logger.error(
                    "execute sub-task(url list) with error: Id {}, MediaName {}, Type {}, sub-url list size {},  error --> ",
                    task.getTaskId(), task.getMediaName(), task.getTaskType(), (entryList != null )? entryList.size():urlList.size(),
                    e);
        } else {
        logger.error(
                "execute sub-task with error: Id {}, MediaName {}, Type {}, sub-url {},  error --> ",
                task.getTaskId(), task.getMediaName(), task.getTaskType(), url,
                e);
        }
    }

    @Override
    public void start(Map<String, Object> ctx) throws Exception {
        ITask task = (ITask) ctx.get("task");
        if(entryList != null || urlList != null) {
            logger.info(
                    "start sub-task(url list): Media Name {}, Task type {}, Parameter {}, sub-url list size {}",
                    task.getMediaName(), task.getTaskType(), task.getParam(), (entryList != null )? entryList.size():urlList.size());
        } else {
        logger.info(
                "start sub-task: Media Name {}, Task type {}, Parameter {}, sub-url {}",
                task.getMediaName(), task.getTaskType(), task.getParam(), url);
        }
    }

    @Override
    public void complete(Map<String, Object> ctx) throws Exception {
        ITask task = (ITask) ctx.get("task");
        if(entryList != null || urlList != null) {
            logger.info("complete sub-task(url list):{},{}, sub-url list size {}", task.getMediaName(),
                    task.getTaskType(), (entryList != null )? entryList.size():urlList.size());
        } else {
            logger.info("complete sub-task:{},{}, sub-url {}", task.getMediaName(),
                    task.getTaskType(), url);
        }

        // Only cache the source url when no error occurs and parent executor is not canceled
        // Need to add finally to release barrier. Sometimes if the gearman payload is too heavy.
        //java.lang.NullPointerException
        //at net.johnewart.gearman.common.packets.PacketFactory.packetFromBytes(PacketFactory.java:38) ~[gearman-common-0.8.11-20150731.182421-1.jar:?]
        try {
            if (entryList != null || urlList != null) {
                if (!isError && !resultMap.isEmpty()
                        && !parentExecutor.isCanceled()
                        && parentExecutor.isCacheable()) {
                    for (Map.Entry<String, Boolean> entry : resultMap
                            .entrySet()) {
                        if (!entry.getValue().booleanValue()) {
                            logger.debug("Cache the source url --> {}",
                                    entry.getKey());
                            LRUCacheUtils.addTraditionalUrlToCache(entry
                                    .getKey());
                        }
                    }
                }
            } else {
                if (!isError && !parentExecutor.isCanceled()
                        && parentExecutor.isCacheable()) {
                    logger.debug("Cache the source url --> {}", url);
                    LRUCacheUtils.addTraditionalUrlToCache(url);
                }
            }
        } catch (Exception e) {
            logger.error("Exception occured during add url to cache.", e);
            throw e;
        } finally {
            barrier.await();
            logger.info("Release barrier done.");
        }
    }

    @Override
    public void save(Map<String, Object> ctx, List<String> data)
            throws Exception {
        if (parentExecutor.isCanceled() || parentExecutor.isTestFlagOn())
            return;
        super.save(ctx, data);
    }

    /**
     * Reflection to get declared methods
     * 
     * @param name
     * @param parameterTypes
     * @return
     * @throws NoSuchMethodException
     */
    public Method findMethod(String name, Class[] parameterTypes)
            throws NoSuchMethodException {
        Class<?> cl = getClass();
        Method method = null;
        while (method == null) {
            try {
                method = cl.getDeclaredMethod(name, parameterTypes);
            } catch (NoSuchMethodException e) {
                logger.error("no method define");
                cl = cl.getSuperclass();
                if (cl == null) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        logger.debug("Method found --> {}", method);
        return method;
    }

    @SuppressWarnings("unchecked")
    protected static void createEntryMap(String[] urlList,
            Map<String, SyndEntry> urlMap, ITaskExecutor parentExecutor) throws Exception {
        for (String rssUrl : urlList) {
            try {
                URL url = new URL(rssUrl);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                // Reading the feed
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(conn));
                List<SyndEntry> entries = feed.getEntries();
                Iterator<SyndEntry> itEntries = entries.iterator();

                while (itEntries.hasNext()) {
                    SyndEntry entry = itEntries.next();
                    String sourceUrl = "";
                    sourceUrl = GetSourceUrlUtil.sendGet(entry.getLink(),
                            false, 0);
                    if (sourceUrl == null) {
                        logger.error("Cannot get the source url for url {}.",
                                url);
                        sourceUrl = entry.getLink();
                        if(sourceUrl.isEmpty()) continue;
                    }
                    //If need to enable cache to reduce duplication
                    //or if does not enable cache for tracing the article statistics
                    if (! LRUCacheUtils.checkIfTraditionalUrlCached(sourceUrl) || ! parentExecutor.isCacheable()) {
                        logger.debug("Crawl the Post RSS url {}.",
                                sourceUrl);
                        urlMap.put(sourceUrl, entry);
                    } else {
                        // Here may need more logic, update time is a question
                        // mark?
                        logger.info("The url {} has been crawled before.", url,
                                sourceUrl);
                        continue;
                    }
                }
            } catch (Exception e) {
                logger.error("Caught Exception during createEntryMap for RSS url {}.", rssUrl,
                        e);
                throw e;
            }
        }
    }

    public static void main(String... args) throws Exception {
        TaskBarrierExecutor taskBarrier = new TaskBarrierExecutor();
        taskBarrier.findMethod("complete", new Class[] { Map.class });
    }
}
