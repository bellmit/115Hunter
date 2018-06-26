package com.sap.cisp.xhna.data.executor;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.common.DataCrawlerTestBase;
import com.sap.cisp.xhna.data.executor.twitter.TwitterArticleAccountExecutor;
import com.sap.cisp.xhna.data.executor.twitter.TwitterArticleKeywordExecutor;
import com.sap.cisp.xhna.data.executor.twitter.TwitterSocialAccountExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class TwitterExecutorTest extends DataCrawlerTestBase {

    public static Logger logger = LoggerFactory
            .getLogger(FacebookExecutorTest.class);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    public TwitterExecutorTest(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    public void testTwitterArticleAccountExecutor() throws Exception {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.HOUR, -1);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.HOUR, +10);
        String from = sdf.format(currentCal.getTime());
        params.put("task_key", "cnn");
        params.put("start_time", from.toString());
        params.put("end_time", to.toString());
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByAccount,
                        null, param);
        ctx.put("task", task);
        TwitterArticleAccountExecutor exe = new TwitterArticleAccountExecutor();
        exe.setTestFlagOn(true);
        List<String> result = null;
        try {
            result = exe.execute(ctx);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assertNotNull(result);
    }

    public void testTwitterArticleKeywordExecutor() {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("task_key", "Volcano");
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.MINUTE, -6);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.MINUTE, -10);
        String from = sdf.format(currentCal.getTime());
        params.put("start_time", from.toString());
        params.put("end_time", to.toString());
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByKeyword,
                        null, param);
        ctx.put("task", task);
        TwitterArticleKeywordExecutor exe = new TwitterArticleKeywordExecutor();
        exe.setTestFlagOn(true);
        List<String> result = null;
        try {
            result = exe.execute(ctx);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(result);
    }

    public void testArticleKeywordExecutorSinceAndUtil() throws ParseException {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("task_key", "Volcano");
        String from = "2015-08-03 08:00:00";
        String to = "2015-08-03 15:00:00";
        params.put("start_time", from.toString());
        params.put("end_time", to.toString());
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByKeyword,
                        null, param);
        ctx.put("task", task);
        TwitterArticleKeywordExecutor exe = new TwitterArticleKeywordExecutor();
        exe.setTestFlagOn(true);
        assertEquals(exe.getSinceDate(from), "2015-08-03");
        assertEquals(exe.getUtilDate(to), "2015-08-04");
        assertEquals(exe.getSinceDate("2015-08-03 07:00:00"), "2015-08-02");
        assertEquals(exe.getUtilDate("2015-08-03 08:00:00"), "2015-08-04");
        assertEquals(exe.getSinceDate("2015-08-02 08:00:00"), "2015-08-02");
        assertEquals(exe.getUtilDate("2015-08-03 07:00:00"), "2015-08-03");
        List<String> result = null;
        try {
            result = exe.execute(ctx);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(result);
    }

    public void testArticleAccountWithWrongToken() throws Exception {
        // copy a broken property file
        // init TokenManager
        // test crawler
    }

    public void testFilterPostWhichAccountContainsKeyword() throws UnsupportedEncodingException {
        String keyword = "beck";
        String patternStr = "(?:(?!^@)([ \t]+)|^|#|\"|\'|\\+)\\b" + keyword
                + "\\b[ ,.\n\r\t]*";
        Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE
                | Pattern.DOTALL);
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("task_key", keyword);
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.MINUTE, -6);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.MINUTE, -10);
        String from = sdf.format(currentCal.getTime());
        params.put("start_time", from.toString());
        params.put("end_time", to.toString());
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByKeyword,
                        null, param);
        ctx.put("task", task);
        TwitterArticleKeywordExecutor exe = new TwitterArticleKeywordExecutor();
        exe.setTestFlagOn(true);
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "@doma @hollen_beck hello world becks beck beckam", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "@doma @beck hello world beck This is beck too", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "@hollen_beck hello world beck's friend", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "#beck hello world beck2", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "@beck_ham hello world   +beck	", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "@beck_ham hello world   #beck	", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "@beck_ham hello world		\"beck\"	", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "\"beck hello world", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "Doma hello \"beck world", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "'beck hello world", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "+beck hello world", pattern));
        assertTrue(exe.checkTweetOnlyAccountContainsKeyword(
                "@doma @hollen_beck hello world", pattern));
        assertTrue(exe.checkTweetOnlyAccountContainsKeyword(
                "@doma @hollen_beck hello world 1beck", pattern));
        assertTrue(exe.checkTweetOnlyAccountContainsKeyword(
                "doma @hollen_beck hello world @beck", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "@doma @hollen_beck hello world 'beck", pattern));
        assertTrue(exe.checkTweetOnlyAccountContainsKeyword(
                "@hollen_beck hello world", pattern));
        assertTrue(exe.checkTweetOnlyAccountContainsKeyword(
                "@beck hello world", pattern));
        assertTrue(exe.checkTweetOnlyAccountContainsKeyword(
                "@beckam hello world", pattern));
        assertTrue(exe.checkTweetOnlyAccountContainsKeyword(
                "@doma @hellen @hollen_beck", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword("beck is here",
                pattern));
        keyword = "China beijing";
        patternStr = "(?:(?!^@)([ \t]+)|^|#|\"|\'|\\+)\\b" + keyword
                + "\\b[ ,.\n\r\t]*";
        pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE
                | Pattern.DOTALL);
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "@doma @hollen_beck China beijing China beijing", pattern));
        assertFalse(exe.checkTweetOnlyAccountContainsKeyword(
                "@doma @beck hello 'china Beijing This is beck too", pattern));
        assertTrue(exe.checkTweetOnlyAccountContainsKeyword(
                "@doma @beck hello @China beijing This is beck too", pattern));
        assertTrue(exe.checkTweetOnlyAccountContainsKeyword(
                "@doma @beck hello China This is beck too", pattern));
        assertTrue(exe.checkTweetOnlyAccountContainsKeyword(
                "@doma @beck hello Beijing China This is beck too", pattern));
    }

    public void testSocialAccountExecutor() {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        // List<String> accounts = new ArrayList<String>();
        TaskParam param = new TaskParam(params, null);
        param.setString("task_key", "cnn");
        ITask task = TaskFactory.getInstance().createTask(
                com.sap.cisp.xhna.data.TaskType.SocialMedia_AccountData, null,
                param);
        ctx.put("task", task);
        TwitterSocialAccountExecutor exe = new TwitterSocialAccountExecutor();
        List<String> result = null;
        try {
            result = exe.execute(ctx);
            assertNotNull(result);
            param.setString("task_key", "");
            result = exe.execute(ctx);
            assertNull(result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
