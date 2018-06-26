package com.sap.cisp.xhna.data.executor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.common.DataCrawlerTestBase;
import com.sap.cisp.xhna.data.executor.stream.DatasiftUtils;


public class DatasiftExecutorTest extends DataCrawlerTestBase {
    private static Logger logger = LoggerFactory
            .getLogger(DatasiftExecutorTest.class);

    public DatasiftExecutorTest(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    public void testGetIPAddress() throws Exception {
       String ipAddress = DatasiftUtils.getIP();
       assertEquals("10.128.80.118", ipAddress);
    }
}
       