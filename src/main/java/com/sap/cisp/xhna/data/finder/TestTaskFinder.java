package com.sap.cisp.xhna.data.finder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.atomic.AtomicInteger;

//import com.sap.cisp.xhna.data.Main;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class TestTaskFinder extends AbstractTaskFinder {
    private final AtomicInteger count = new AtomicInteger();
    @Override
    public void find() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("task_key", "Beckham");
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.DATE, 0);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.HOUR, -1);
        String from = sdf.format(currentCal.getTime());
        params.put("start_time", from.toString());
        params.put("end_time", to.toString());
        params.put("media_name", "Twitter");
        MediaKey mediaKey = new MediaKey("Twitter", "SocialArticle", "Keyword");
        TaskParam param = new TaskParam(params, mediaKey);

        Map<String, String> params1 = new HashMap<String, String>();
        params1.put("task_key", "cnn");
        Calendar currentCal1 = Calendar.getInstance();
        currentCal1.add(Calendar.DATE, 0);
        String to1 = sdf.format(currentCal1.getTime());
        currentCal1.add(Calendar.HOUR, -1);
        String from1 = sdf.format(currentCal1.getTime());
        params1.put("start_time", from1.toString());
        params1.put("end_time", to1.toString());
        params1.put("media_name", "Twitter");
        MediaKey mediaKey1 = new MediaKey("Twitter", "SocialArticle", "Account");
        TaskParam param1 = new TaskParam(params1, mediaKey1);

        Map<String, String> caseslist = new HashMap<String, String>();
        caseslist.put("cnn", "http://rss.cnn.com/rss/edition_world.rss");
        caseslist.put("newyorktimes",
                "http://www.nytimes.com/services/xml/rss/nyt/HomePage.xml");
        caseslist.put("fox", "http://feeds.foxnews.com/foxnews/latest");
        caseslist.put("washingtonPost",
                "http://feeds.washingtonpost.com/rss/world");
        caseslist.put("nbcNews", "http://feeds.nbcnews.com/feeds/topstories");
        caseslist.put("theTimes",
                "http://www.thetimes.co.uk/tto/news/world/rss");
        caseslist.put("guardian", "http://www.theguardian.com/world/rss");
        caseslist
                .put("ap",
                        "http://hosted2.ap.org/atom/APDEFAULT/cae69a7523db45408eeb2b3a98c0c9c5");
        caseslist.put("chinaDaily",
                "http://www.chinadaily.com.cn/rss/china_rss.xml");

        Map<String, String> params3 = new HashMap<String, String>();
        params3.put("task_key", "Beckham");
        Calendar currentCal3 = Calendar.getInstance();
        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal3.add(Calendar.DATE, 0);
        String to3 = sdf3.format(currentCal3.getTime());
        currentCal3.add(Calendar.HOUR, -1);
        String from3 = sdf3.format(currentCal3.getTime());
        params3.put("start_time", from3.toString());
        params3.put("end_time", to3.toString());
        params3.put("media_name", "Facebook");
        MediaKey mediaKey3 = new MediaKey("Facebook", "SocialArticle",
                "Account");
        TaskParam param3 = new TaskParam(params3, mediaKey3);

        Map<String, String> params4 = new HashMap<String, String>();
        params4.put("task_key", "世锦赛 北京");
        Calendar currentCal4 = Calendar.getInstance();
        SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal4.add(Calendar.HOUR, -200);
        String to4 = sdf4.format(currentCal4.getTime());
        currentCal4.add(Calendar.HOUR, -500);
        String from4 = sdf4.format(currentCal4.getTime());
        params4.put("start_time", from4);
        params4.put("end_time", to4);
        params4.put("key", "AIzaSyAmx8zhVoRazSGSdXQ3eQJRTIaTNqOuwaE");
        TaskParam param4 = new TaskParam(params4, null);

        for (int i = 0; i < 100; i++) {
            if (count.getAndIncrement() > 50000)
                return;
            ITask task = TaskFactory
                    .getInstance()
                    .createTask(
                            com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByKeyword,
                            "Twitter", param);
            addTask(task);
            ITask task1 = TaskFactory
                    .getInstance()
                    .createTask(
                            com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByAccount,
                            "Twitter", param1);
            addTask(task1);
            ITask task5 = TaskFactory
                    .getInstance()
                    .createTask(
                            com.sap.cisp.xhna.data.TaskType.SocialMedia_Account_Article,
                            "Twitter", param1);
            addTask(task5);

            ITask task3 = TaskFactory
                    .getInstance()
                    .createTask(
                            com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByAccount,
                            "Facebook", param3);
            addTask(task3);

            ITask task4 = TaskFactory
                    .getInstance()
                    .createTask(
                            com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByKeyword,
                            "GooglePlus", param4);
            addTask(task4);

            for (Map.Entry<String, String> entry : caseslist.entrySet()) {
                Map<String, String> params2 = new HashMap<String, String>();

                params2.put("media_name", "Traditional");
                params2.put("task_key", entry.getValue());
                MediaKey mediaKey2 = new MediaKey("Traditional",
                        "TraditionalArticle", "WebPage1");
                TaskParam param2 = new TaskParam(params2, mediaKey2);
                ITask task2 = TaskFactory
                        .getInstance()
                        .createTask(
                                com.sap.cisp.xhna.data.TaskType.TraditionalMedia_ArticleData_ByRSS,
                                "Traditional", param2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
                addTask(task2);
            }
        }
    }
}