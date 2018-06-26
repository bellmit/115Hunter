package com.sap.cisp.xhna.data.executor.stream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datasift.client.stream.Interaction;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.google.common.base.Stopwatch;
import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.common.DateUtil;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.TokenManager;

public class DatasiftStreamAccountExecutor extends AbstractTaskExecutor {
    private static Logger log = LoggerFactory
            .getLogger(DatasiftStreamAccountExecutor.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss+0000");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private Date from;
    private Date to;
    private String CSDL_ACCOUNT_PREFX_FORMAT = "interaction.type == \"%s\" and tencentweibo.author.id contains_any \"%s\"";
    protected String interactionType = "tencentweibo";

    protected BlockingQueue<Interaction> queue = new LinkedBlockingQueue<Interaction>();
    private BatchWriter batchWriter = null;
    // For Junit test
    static {
        sdf.setTimeZone(GMT);
    }

    @Override
    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception {
        ITask task = (ITask) ctx.get("task");
        TaskParam taskParam = task.getParam();
        String account = taskParam.getAccount();
        String startTime = taskParam.getStartTime();
        String endTime = taskParam.getEndTime();

        return getResultsByAccount(ctx, account, startTime, endTime);
    }

    protected List<String> getResultsByAccount(Map<String, Object> ctx,
            String account, String startTime, String endTime) throws Exception {
        List<String> results = new ArrayList<String>();

        if (account == null || account.equalsIgnoreCase("")) {
            logger.error("Get account error");
            throw new DataCrawlException("Account is null or empty.");
        } else {
            logger.info("Account:{}", account);
        }

        if (startTime == null || endTime == null
                || startTime.equalsIgnoreCase("")
                || endTime.equalsIgnoreCase("")) {
            logger.error("Get date limit error");
            throw new DataCrawlException(
                    "Date limit(start time/end time) is null or empty.");
        } else {
            log.info("start_time:" + startTime);
            log.info("end_time:" + endTime);
            Date fromDate = DateUtil.stringToDate(startTime,
                    "yyyy-MM-dd HH:mm:ss");
            Date toDate = DateUtil.stringToDate(endTime, "yyyy-MM-dd HH:mm:ss");
            from = DateUtil.parseDate(sdf.format(fromDate));
            to = DateUtil.parseDate(sdf.format(toDate));
            logger.info("From date {} To date {}", from, to);
        }

        String csdl = String.format(CSDL_ACCOUNT_PREFX_FORMAT, interactionType,
                account);
        log.info("Crawl social interaction of csdl: {}", csdl);
        // google guava stopwatch, to caculate the execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();
        new Thread() {
            public void run() {
                if(!DatasiftUtils.subscribeStreamWithCSDL(csdl, queue)){
                    logger.error("Cannot subscribe stream with csdl {}", csdl);
                    try {
                        cancel(true);
                    } catch (Exception e) {
                        logger.error("Exception occured during cancel datasift keyword executor", e);
                    }
                }
//              DatasiftUtils.subscirbeDummyStreamWithCSDL(csdl, queue);
            }
        }.start();

        Interaction interaction = null;
        batchWriter = new BatchWriter(ctx, this, 10L, 10L);
        while (!isCanceled) {
            try {
                // Block until queue is not null
                interaction = queue.take();
                logger.debug("Queue Size ==> {}", queue.size());
            } catch (InterruptedException e) {
                logger.error("Interrupted datasift keyword executor.", e);
            }
            // Check if it's poison message to stop
            if (interaction.getData() instanceof BooleanNode
                    && ((BooleanNode) interaction.getData()).isBoolean()
                    && ((BooleanNode) interaction.getData()).booleanValue() == false) {
                logger.warn("Received the POISON message. Stop the datasift keyword executor.");
                break;
            }
            // get the tecenweibo json object
            JSONObject json = DatasiftUtils.parseInteraction(interaction)
                    .getJSONObject("tencentweibo");
            logger.info("Parse the Tecent weibo : {}", json);
            batchWriter.addResult(json.toString());
//            batchWriter.addResult(interaction.getData().toString());
        }
        //Canceled task by kill -15:
        //Need to stop the batch wirter service
        batchWriter.shutdown();
        //Need to unsubscribe the stream in some cases
        DatasiftUtils.unsubscribeStreamWithCSDL(csdl);
        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);

        log.info(
                "Schedule done crawl interactions: {} ; Total interactions count: {}; Elapsed Time(seconds): {}",
                csdl, batchWriter.getTotalCount(), nanos);

        return isCanceled ? null : results;
    }

    @Override
    public void cancel(boolean mayInterruptIfRunning) throws Exception {
        if(batchWriter != null) {
            batchWriter.shutdown();
        }
        super.cancel(mayInterruptIfRunning);     
    }
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        TokenManager.getInstance().init();
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("task_key", "t101010china");
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.HOUR, -0);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.DATE, -7);
        String from = sdf.format(currentCal.getTime());
        params.put("start_time", from);
        params.put("end_time", to);
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByKeyword,
                        "Tencentweibo", param);
        ctx.put("task", task);
        DatasiftStreamAccountExecutor exe = new DatasiftStreamAccountExecutor();
        exe.setTestFlagOn(true);
        ThreadFactory factory = new TaskThreadFactory();
        ExecutorService taskExecutorPool = new TaskExecutorPool(10, 10, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                factory);
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
