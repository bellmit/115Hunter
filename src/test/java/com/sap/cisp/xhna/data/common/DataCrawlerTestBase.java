package com.sap.cisp.xhna.data.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.sap.cisp.xhna.data.Main;
import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.token.TokenManager;

public class DataCrawlerTestBase extends TestCase {
    public DataCrawlerTestBase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        TokenManager.getInstance().init();
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService  taskExecutorPool = new TaskExecutorPool(1000, 1000, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),factory);
        Main.setTaskExecutorPool(taskExecutorPool);
    }

    public void testDummy() {
        // just to suppress warning
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        // TokenManager.getInstance().shutDown();
    }

}