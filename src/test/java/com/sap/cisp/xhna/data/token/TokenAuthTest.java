package com.sap.cisp.xhna.data.token;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.*;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.common.DataCrawlerTestBase;
import com.sap.cisp.xhna.data.common.SendHttp;
import com.sap.cisp.xhna.data.common.index.PostIdsHolder;
import com.sap.cisp.xhna.data.common.serializer.KryoUtils;
import com.sap.cisp.xhna.data.common.serializer.Serializer;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.executor.googleplus.BaseGpCrawler;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;
import com.sap.cisp.xhna.data.token.TokenManager;
import com.sap.cisp.xhna.data.token.TwitterAccessToken;

public class TokenAuthTest extends DataCrawlerTestBase {
    public static Logger logger = LoggerFactory.getLogger(TokenAuthTest.class);
    private ConfigurationBuilder builder;
    String server;
    int port;
    IToken token;

    public TokenAuthTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TokenManager.getInstance().init();
        builder = new ConfigurationBuilder();
        builder.setApplicationOnlyAuthEnabled(true);
    }

    // --- Authentication

    public void testTwitterAuthWithTokenManager() throws Exception {
        // setup
        int i = 0;
        while (i < 10000) {
            Twitter twitter = null;
            try {
                server = ConfigInstance.PROXY_ADDR;
                port = ConfigInstance.PROXY_PORT;
                token = TokenManager.getInstance().getToken(TokenType.Twitter);

                String consumerKey = ((TwitterAccessToken) token)
                        .getConsumerKey();
                String consumerSecret = ((TwitterAccessToken) token)
                        .getConsumerSecret();
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setApplicationOnlyAuthEnabled(true);
                builder.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
                        .setOAuthConsumerSecret(consumerSecret)
                        .setHttpProxyHost(server).setHttpProxyPort(port)
                        .setJSONStoreEnabled(true);
                twitter = new TwitterFactory(builder.build()).getInstance();

                // exercise & verify
                OAuth2Token tokenOauth = twitter.getOAuth2Token();
                assertEquals("bearer", tokenOauth.getTokenType());

                // Search tweets
                Query query = new Query("BeckHam");
                query.setCount(100);
                QueryResult result = twitter.search(query);
                System.out.println("========> Result : "
                        + result.getRateLimitStatus());
                TokenManager.getInstance().releaseToken(token.getTokenTag(), 0);
            } catch (TwitterException e) {
                assertEquals(429, e.getStatusCode());
                assertEquals(88, e.getErrorCode());
                System.out.println("========> StatusCode " + e.getStatusCode()
                        + " ErrorCode : " + e.getErrorCode()
                        + " Exception : \n" + e.getMessage());
                TokenManager.getInstance().releaseToken(token,
                        e.getStatusCode(), e);

            }
            try {
                twitter.getAccountSettings();
                fail("should throw TwitterException");

            } catch (TwitterException e) {
                assertEquals(403, e.getStatusCode());
                assertEquals(220, e.getErrorCode());
                assertEquals(
                        "Your credentials do not allow access to this resource",
                        e.getErrorMessage());
            }
            i++;
        }
        String tokenTag = (String) SerializationUtils
                .deserialize(SerializationUtils.serialize(""));
        logger.info("token tag {}, length {}", tokenTag, tokenTag.length());
        Serializer<PostIdsHolder> serializer = new KryoUtils.CustomSerializer<PostIdsHolder>(
                KryoUtils.PostIdsContainerTypeHandler);
        PostIdsHolder content = new PostIdsHolder("", new ArrayList<String>());
        serializer.serialize(content);  
    }

    public void testFacebookToken() throws Exception {
        JSONObject initResponse = null;
        URL url1 = new URL(
                "https://graph.facebook.com/debug_token?input_token=407921379400682|92OlgqOvVOMAm1p2SvDMGigwNCo&access_token=407921379400682|92OlgqOvVOMAm1p2SvDMGigwNCo");
        URL url2 = new URL(
                "https://graph.facebook.com/debug_token?input_token=458163071031535|Po8MS2mFOofnuEB7A7PMTfhNQuQ&access_token=458163071031535|Po8MS2mFOofnuEB7A7PMTfhNQuQ");
        URL[] urls = { url1, url2 };
        for (URL url : urls) {
            try {
                initResponse = SendHttp.sendGet(url);
            } catch (MalformedURLException e) {
                logger.error(e.getMessage());
                throw e;
            }
            if (initResponse == null) {
                logger.error("Failed to get response from {}", url);
                throw new DataCrawlException("Failed to get response from "
                        + url);
            }
            JSONObject validation = initResponse.getJSONObject("data");

            String isValid = validation.getString("is_valid");
            assertEquals(isValid, "true");

            logger.info("Response : {}", initResponse);
            logger.info("Application : {}", validation.getString("application"));
            logger.info("App id : {}", validation.getString("app_id"));
        }
    }

    /**
     * Currently Google API Key(for server app) is used, so the access token
     * test is invalid Just keep it as a record
     * 
     * @throws Exception
     */
    public void testGooglePlusToken() throws Exception {
        String initResponse = null;
        String urlStr = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=%s";
        URL url = new URL(String.format(urlStr,
                "AIzaSyCAume9d2DtikYdTE2jhpTtrjYE_rdqFm4"));
        try {
            initResponse = BaseGpCrawler.httpGetString(String.format(urlStr,
                    "AIzaSyCAume9d2DtikYdTE2jhpTtrjYE_rdqFm4"));
        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
        }
        if (initResponse == null) {
            logger.error("Failed to get response from {}", url);
        }
        logger.info("Response : {}", initResponse);
    }
}
