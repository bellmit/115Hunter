package com.sap.cisp.xhna.data.executor.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.stream.iot.watertreatment.IOTRouterExecutor;

public class BatchWriter {
    private List<String> resultList = new ArrayList<String>();
    private ScheduledThreadPoolExecutor batchWriteService = new ScheduledThreadPoolExecutor(
            1);
    private Map<String, Object> ctxMap;
    private ITaskExecutor parentExecutor;
    private AtomicInteger counter = new AtomicInteger(0);
    private static Logger logger = LoggerFactory
            .getLogger(BatchWriter.class);

    public BatchWriter(Map<String, Object> ctx, ITaskExecutor executor, long delay, long period) {
        this.ctxMap = ctx;
        this.parentExecutor = executor;
        batchWriteService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // save result periodically to avoid long wait in case of
                // inadequate result
                synchronized (resultList) {
                    try {
                        logger.debug(
                                "Current result list size(batch wirte service) => {}",
                                resultList.size());
                        save();
                        resultList.clear();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        logger.error("Exception in batch writer.", e);
                    }
                }
            }
        }, delay, period, TimeUnit.SECONDS);
    }
    
    public BatchWriter(Map<String, Object> ctx, String type, ITaskExecutor executor, long delay, long period) {
        this.ctxMap = ctx;
        this.parentExecutor = executor;
        batchWriteService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // save result periodically to avoid long wait in case of
                // inadequate result
                synchronized (resultList) {
                    try {
                        logger.debug(
                                "Current result list size(batch wirte service) => {}",
                                resultList.size());
                        save(type);
                        resultList.clear();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        logger.error("Exception in batch writer.", e);
                    }
                }
            }
        }, delay, period, TimeUnit.SECONDS);
    }

    public void addResult(String result) {
        synchronized (resultList) {
            resultList.add(result);
            counter.incrementAndGet();
            logger.debug("Current result list size(add) => {}",
                    resultList.size());
        }
    }
    
    public void save() throws Exception {
        if(parentExecutor!= null && !parentExecutor.isCanceled() && !parentExecutor.isTestFlagOn()){
            parentExecutor.save(ctxMap, resultList);
        }
    }
    
    public void save(String type) throws Exception {
        if(parentExecutor!= null && !parentExecutor.isCanceled() && !parentExecutor.isTestFlagOn()){
            // Special for IOT, since there maybe multiple tables to save data
            if(parentExecutor instanceof IOTRouterExecutor) {
                ((IOTRouterExecutor) parentExecutor).save(type, resultList);
                return;
            }
            parentExecutor.save(ctxMap, resultList);
        }
    }
    
    public int getTotalCount() {
        return counter.get();
    }

    public void shutdown() {
        // Cancel scheduled but not started task, and avoid new ones
        batchWriteService.shutdown();

        // Wait for the running tasks
        try {
            batchWriteService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Interrupt the threads and shutdown the scheduler
        batchWriteService.shutdownNow();
    }
}
