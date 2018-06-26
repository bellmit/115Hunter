package com.sap.cisp.xhna.data.executor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.common.DataCrawlerTestBase;
import com.sap.cisp.xhna.data.executor.facebook.FacebookArticleAccountExecutor;
import com.sap.cisp.xhna.data.executor.facebook.FacebookSocialAccountExecutor;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.OAuthToken;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class FacebookExecutorTest extends DataCrawlerTestBase {
    public static Logger logger = LoggerFactory
            .getLogger(FacebookExecutorTest.class);

    public FacebookExecutorTest(String name) {
        super(name);
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

    public void testArticleAccountExecutor() throws Exception {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentCal.add(Calendar.HOUR, -1);
        String to = sdf.format(currentCal.getTime());
        currentCal.add(Calendar.HOUR, -20);
        String from = sdf.format(currentCal.getTime());
        params.put("task_key", "barackobama");
        params.put("start_time", from.toString());
        params.put("end_time", to.toString());
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory
                .getInstance()
                .createTask(
                        com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByAccount,
                        null, param);
        ctx.put("task", task);
        FacebookArticleAccountExecutor exe = new FacebookArticleAccountExecutor();
        List<String> result = null;

        try {
            result = exe.execute(ctx);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(result);
    }

    public void testSocialAccountExecutor() throws Exception {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
//        List<String> accounts = new ArrayList<String>();
//        accounts.add("bbcnews");
//        accounts.add("cnn");
//        accounts.add("XinhuaNewsAgency");
        params.put("task_key", "cnn");
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory.getInstance().createTask(
                com.sap.cisp.xhna.data.TaskType.SocialMedia_AccountData, null,
                param);
        ctx.put("task", task);
        FacebookSocialAccountExecutor exe = new FacebookSocialAccountExecutor();
        List<String> result = null;
        try {
            result = exe.execute(ctx);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assertNotNull(result);
    }

    public void testSocialAccountExecutorWithEmptyAccount() throws Exception {
        Map<String, Object> ctx = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();
        List<String> accounts = new ArrayList<String>();
        accounts.add("");
        ctx.put("accounts", accounts);
        TaskParam param = new TaskParam(params, null);
        ITask task = TaskFactory.getInstance().createTask(
                com.sap.cisp.xhna.data.TaskType.SocialMedia_AccountData, null,
                param);
        ctx.put("task", task);
        FacebookSocialAccountExecutor exe = new FacebookSocialAccountExecutor();
        List<String> result = null;

        try {
            result = exe.execute(ctx);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assertNull(result);
    }

    public void testReplaceToken() {
        IToken token = new OAuthToken(TokenType.Facebook,
                "458163071031535|Po8MS2mFOofnuEB7A7PMTfhNQuP");
        String pagingUrl1 = "https://graph.facebook.com/v2.3/228735667216/posts?limit=100&since=1439222400&access_token=458163071031535|Po8MS2mFOofnuEB7A7PMTfhNQuQ&until=1439223213&__paging_token=enc_AdDesBVtaZBizS3fUf1xk9445v2Q503oPEhYKwJMzeqCmv7IsbOl2Dl5EZB1ubLF5xOLKnCL59wvlmdWTyZBXPTIZAQ7EGevl5P3kiTwEov0haJ15wZDZD";
        String pagingUrl2 = "https://graph.facebook.com/v2.3/191347651290/posts?limit=100&since=1438617600&__paging_token=enc_AdApjO2ZCxUoZCNihq1byp9KQXe3sC7lfRa0bsYsy3HqfmX5kRiAU70yL2u6EeA1h3MB044DSfEudDxPaMivUyJU9A95izZBKGPyDb7oVP7E9E2PgZDZD&access_token=458163071031535|Po8MS2mFOofnuEB7A7PMTfhNQuQ&until=1438617601";
        String url1 = FacebookArticleAccountExecutor.replaceToken(pagingUrl1, token);
        assertEquals(
                "https://graph.facebook.com/v2.3/228735667216/posts?limit=100&since=1439222400&access_token=458163071031535|Po8MS2mFOofnuEB7A7PMTfhNQuP&until=1439223213&__paging_token=enc_AdDesBVtaZBizS3fUf1xk9445v2Q503oPEhYKwJMzeqCmv7IsbOl2Dl5EZB1ubLF5xOLKnCL59wvlmdWTyZBXPTIZAQ7EGevl5P3kiTwEov0haJ15wZDZD",
                url1);
        String url2 = FacebookArticleAccountExecutor.replaceToken(pagingUrl2, token);
        assertEquals(
                "https://graph.facebook.com/v2.3/191347651290/posts?limit=100&since=1438617600&__paging_token=enc_AdApjO2ZCxUoZCNihq1byp9KQXe3sC7lfRa0bsYsy3HqfmX5kRiAU70yL2u6EeA1h3MB044DSfEudDxPaMivUyJU9A95izZBKGPyDb7oVP7E9E2PgZDZD&access_token=458163071031535|Po8MS2mFOofnuEB7A7PMTfhNQuP&until=1438617601",
                url2);
        String pagingUrl3 = "https://graph.facebook.com/v2.3/114050161948682/posts?limit=25&since=1442826232&__paging_token=enc_AdCcFLGqdCYPzbf27peTW5LPF1K5tH3yUAZBN08rKEZCfLe0juwfxBn9pLPr0ksRiatKYPFZCa2Ep3UWmJFR7dX6oWObZAqyJplxh9t7hYbs4UiueQZDZD&access_token=458163071031535|Po8MS2mFOofnuEB7A7PMTfhNQuQ&until=1442891286";
        String url3 = FacebookArticleAccountExecutor.replaceToken(pagingUrl3, token);
        logger.debug("url3 -> {}", url3);
        assertEquals("https://graph.facebook.com/v2.3/114050161948682/posts?limit=25&since=1442826232&__paging_token=enc_AdCcFLGqdCYPzbf27peTW5LPF1K5tH3yUAZBN08rKEZCfLe0juwfxBn9pLPr0ksRiatKYPFZCa2Ep3UWmJFR7dX6oWObZAqyJplxh9t7hYbs4UiueQZDZD&access_token=458163071031535|Po8MS2mFOofnuEB7A7PMTfhNQuP&until=1442891286",
                url3);
    }
}
