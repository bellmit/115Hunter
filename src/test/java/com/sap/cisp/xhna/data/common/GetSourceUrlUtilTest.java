package com.sap.cisp.xhna.data.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.traditional.GetSourceUrlUtil;

import junit.framework.TestCase;

public class GetSourceUrlUtilTest extends TestCase {
    public static Logger logger = LoggerFactory
            .getLogger(GetSourceUrlUtilTest.class);

    public GetSourceUrlUtilTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        logger.info("Init Proxy config... {}", ConfigInstance.USE_PROXY);
        super.setUp();
    }

    public void testDummy() {
        // just to suppress warning
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSendGet() {
        // HTTP/1.1 301 Moved Permanently
        String url1 = "http://rss.cnn.com/~r/rss/edition_world/~3/650gMxuGyko/orig-natpkg-uk-plane-sideways-landing-wind.storyful.html";
        String source = GetSourceUrlUtil.sendGet(url1, false, 0);
        System.out.println(source);
        assertNull(source);

        String url2 = "http://rss.nytimes.com/c/34625/f/640388/s/48b3b9e6/sc/7/l/0L0Snytimes0N0Cvideo0Cembedded0Carts0Ctelevision0C10A0A0A0A0A0A0A38359160Ca0Epresidential0Eguest0Bhtml0Dpartner0Frss0Gemc0Frss/story01.htm";
        source = GetSourceUrlUtil.sendGet(url2, false, 0);
        System.out.println(source);
        assertNull(source);
    }
    
    public void testManualComposeUrl() {
        String url = "http://www.cnn.com/politics/cuba-terror-list/index.html?rss=xxx&rss2=xxx";
        Pattern pattern = Pattern
                .compile("^((http://[0-9a-zA-Z\\.]+/)?).*$");
        logger.debug("Manual compose result for url {}", url);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            logger.debug("Matcher group() -->  {}", matcher.group());
            logger.debug("Matcher group(1) -->  {}", matcher.group(1));
        }
        assertEquals("http://www.cnn.com/", matcher.group(1));
    }
    
    public void testTwitterShortLink() {
        Pattern pattern = Pattern.compile(
                "(?:^((?!http://www).)*$)(?!.*((htm)|(html))).*$", Pattern.CASE_INSENSITIVE
                        | Pattern.DOTALL);
        Pattern patternHTML = Pattern.compile(
                "^(?!.*?((htm)|(html))).*$", Pattern.CASE_INSENSITIVE
                | Pattern.DOTALL);
        String url1 = "http://www.usatoday.com/story/sports/ncaab/acc/2016/02/18/roy-williams-north-carolina-tar-heels-botch-final-play-duke-loss/80541482/?utm_source=feedblitz&utm_medium=FeedBlitzRss&utm_campaign=usatodaycomsports-topstories";
        Matcher matcher1 = pattern.matcher(url1);
        assertFalse(matcher1.find());
        String url2 = "http://rssfeeds.com/story/sports/ncaab/acc/2016/02/18/roy-williams-north-carolina-tar-heels-botch-final-play-duke-loss/80541482/?utm_source=feedblitz&utm_medium=FeedBlitzRss&utm_campaign=usatodaycomsports-topstories";
        Matcher matcher2 = pattern.matcher(url2);
        assertTrue(matcher2.find());
        logger.debug("Matcher group() -->  {}", matcher2.group(1));
        String url0 = "http://trib.al/MMlNjmm";
        Matcher matcher3 = pattern.matcher(url0);
        assertTrue(matcher3.find());
        logger.debug("Matcher group() -->  {}", matcher3.group(1));
        String url10 = "http://usa.chinadaily.com.cn/china/2016-01/20/content_23165891.htm";
        Matcher matcher4 = patternHTML.matcher(url10);
        assertFalse(matcher4.find());

        //twitter test cases
        String url = "usat.ly/1osl4jA";
        String url3 = "http://bbc.in/1T3p2vX";
        String url4 = "http://cnn.it/1oVs6ND";
        String url5 = "http://ow.ly/YwcIW  ";
        String url6 = "http://bit.ly/1Mi9aks";
        String source = "";
        source = GetSourceUrlUtil.sendGet(url, false, true, 0);
        System.out.println(source);
        assertNotNull(source);
        source = GetSourceUrlUtil.sendGet(url3, false, true, 0);
        System.out.println(source);
        assertNotNull(source);
        source = GetSourceUrlUtil.sendGet(url4, false, true, 0);
        System.out.println(source);
        assertNotNull(source);
        source = GetSourceUrlUtil.sendGet(url5, false, true, 0);
        System.out.println(source);
        assertNotNull(source);
        source = GetSourceUrlUtil.sendGet(url6, false, true, 0);
        System.out.println(source);
        assertNotNull(source);        
    }
}
