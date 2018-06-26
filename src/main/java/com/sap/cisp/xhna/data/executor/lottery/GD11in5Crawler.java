package com.sap.cisp.xhna.data.executor.lottery;

import com.google.common.base.Stopwatch;
import com.sap.cisp.xhna.data.config.ConfigStorage;
import com.sap.cisp.xhna.data.config.DataSource;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutorBuilder;
import com.sap.cisp.xhna.data.model.ForumPostInfo;
import com.sap.cisp.xhna.data.storage.ColumnInfo;
import com.sap.cisp.xhna.data.storage.Connections;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sun.syndication.feed.synd.SyndEntry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("rawtypes")
public class GD11in5Crawler extends TaskBarrierExecutor {
    private static final String className = "GD11in5Crawler";
    private static final String rootUrl = "http://www.gdlottery.cn/odata/zst11xuan5.jspx?method=to11x5kjggzst&date=2015-01-01";
    private static Logger logger = LoggerFactory
            .getLogger(GD11in5Crawler.class);
    Map<String, Object> ctx;


    public GD11in5Crawler(Map<String, Object> ctx) {
        super();
        this.ctx = ctx;
    }

    public GD11in5Crawler() {
    }

    public GD11in5Crawler(CyclicBarrier barrier, String methodName,
                          Class[] parameterTypes, String url, SyndEntry entry,
                          ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, url, entry, parentExecutor);
    }

    public GD11in5Crawler(CyclicBarrier barrier, String methodName,
                          Class[] parameterTypes, String url, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, url, parentExecutor);
    }

    public GD11in5Crawler(CyclicBarrier barrier, String methodName,
                          Class[] parameterTypes, List<String> urlList,
                          ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, urlList, parentExecutor);
    }

    public GD11in5Crawler(CyclicBarrier barrier, String methodName,
                          Class[] parameterTypes,
                          List<Map.Entry<String, SyndEntry>> entryList,
                          boolean isEntryNeeded, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, entryList, isEntryNeeded,
                parentExecutor);
    }

    public static void main(String... args) throws Exception {
    }

    // 1
    public void getLotteryResultHistory(Map<String, Object> ctx,
                                        ITaskExecutor parentExecutor) throws Exception {
        // google guava stopwatch, to caculate the execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();
        getLotteryResultPageList(ctx, parentExecutor);
        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.info("Crawl Lottery result history elapsed Time(Seconds) {}", nanos);
    }

    public void getLotteryResultPageList(Map<String, Object> ctx,
                                         ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();

        ArrayList<String> allLinkList = new ArrayList<String>();
        int totalSize = 0;
        try {

            ArrayList<String> pageList = getResultPageList(rootUrl);
            logger.info("###### The total url {} with thread/post size",
                    pageList.size());
            ctx.put("methodName", "crawlResultPage");
            ctx.put("parameterTypes", new Class[]{String.class, Map.class,
                    ITaskExecutor.class});
            ctx.put("className", className);
            if (parentExecutor != null && !parentExecutor.isCanceled()
                    && !pageList.isEmpty()) {
                CyclicBarrier parentBarrier = new CyclicBarrier(2);
                TaskBarrierExecutorBuilder.getInstance()
                        .buildWebPageTaskBarrier(pageList, ctx,
                                parentExecutor, parentBarrier);
                parentBarrier.await();
            }
        } catch (IOException e) {
            logger.error(
                    "Caught Exception during get semiWiki post link urls.", e);
            throw e;
        } catch (Exception e) {
            logger.error(
                    "Caught Exception during get semiWiki post link urls.", e);
            throw e;
        }

    }

    public String crawlResultPage(String url,
                                  Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws Exception {
        Document doc = Jsoup
                .connect(url)
                .get();

        Elements tableElement = doc.select("table[bordercolorlight=\"#008000\"]");
        Elements tbodyElement = tableElement.first().select("tbody");
        Elements trListElement = tbodyElement.first().select("tr");
        Iterator<Element> it = trListElement.iterator();
        while (it.hasNext()) {
            Element trElement = it.next();
            // Skip the header tr rows
            if (!trElement.attr("bgcolor").isEmpty()) {
                continue;
            }
            //The result number, i.e. 09111101
            Elements tdDateElement = trElement.select("td[bgcolor=\"#FFFFFF\"]");
            //Avoid the splitter
            if (tdDateElement != null && !tdDateElement.isEmpty()) {
                String date = tdDateElement.first().html().trim();
                logger.info("The number of result: " + date);
            }
            //The result
            Elements tdResultElement = trElement.select("td[bgcolor=\"#FFFF99\"]");
            String result = tdResultElement.select("strong").html().trim();
            logger.info("The result: " + result);
        }

        return "";
    }

    public ArrayList<String> getResultPageList(String rootUrl) throws Exception {

        Document doc = Jsoup
                .connect(rootUrl)
                .get();

        Elements datesE = doc.select("select[name=date]");
        Elements dateListE = datesE.first().select("option");
        logger.info("Result page size : {}", dateListE.size());
        ArrayList<String> resultPageList = new ArrayList<String>();
        String prefix = "http://www.gdlottery.cn/odata/zst11xuan5.jspx?method=to11x5kjggzst&date=";
        // Get the result page list
        // Format is: http://www.gdlottery.cn/zst11xuan5.jspx?method=to11x5kjggzst&date={2009-11-11}
        if (!dateListE.isEmpty()) {
            Iterator<Element> it = dateListE.iterator();
            while (it.hasNext()) {
                Element resultDate = it.next();
                String date = resultDate.html().trim();
                if(date.contains("2014")) continue;
                String resultPage = new StringBuilder(prefix).append(date).toString();
                resultPageList.add(resultPage);
                logger.info("The result page is : " + resultPage);
            }
        }
        return resultPageList;
    }

    private String[] addInfo(String[] info, String... additionals) {
        String[] res = new String[info.length + additionals.length];
        System.arraycopy(info, 0, res, 0, info.length);
        for (int i = 0; i < additionals.length; ++i) {
            res[info.length + i] = additionals[i];
        }
        return res;
    }

    private synchronized void writeDB(List<String[]> strucute_data)
            throws Exception {
        String schema = "FORUM";
        String table = "FORUM_POSTS";

        DataSource ds = ConfigStorage.DS_INFO;
        String DriverClassName = ds.getDriver();
        String url = ds.getUrl();
        String username = ds.getUserName();
        String password = ds.getPassword();

        try {
            Class.forName(DriverClassName);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            logger.error("No JDBC Driver found!");
            throw e;
        }
        Connections connections = new Connections(DriverClassName, url,
                username, password);
        java.sql.Connection conn = connections.getConnection();
        ColumnInfo column_info = new ColumnInfo(conn, schema, table,
                strucute_data);
        column_info.insertData();
        logger.info("Write Table [{}.{}] Successed!", schema, table);
    }

    @Override
    public void save(Map<String, Object> ctx, List<String> data)
            throws Exception {
        if (parentExecutor.isCanceled() || parentExecutor.isTestFlagOn())
            return;
        // write to HANA
        if (data == null || data.isEmpty()) {
            logger.warn("No data Needs to be Persisted!");
            return;
        }
        String key = ((ITask) ctx.get("task")).getParam().getString("task_key");
        List<String[]> output = new ArrayList<String[]>();
        for (String row : data) {
            ForumPostInfo di = new ForumPostInfo(row);
            logger.info("*** Construct the data line to insert: {}",
                    (Object[]) addInfo(di.getAttributeValueList(), key));
            output.add(addInfo(di.getAttributeValueList(), key));
        }
        writeDB(output);
    }

}
