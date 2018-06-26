package com.sap.cisp.xhna.data.executor.twitter;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.token.IToken;
import com.sap.cisp.xhna.data.token.TwitterAccessToken;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterSingleton {
    public static int maxStreamNump = 6;
    public static int currentStreamNum = 0;
    public static int maxStreamNum = 6;
    public static int keyNum = 0;
    public static Properties prop;
    private static Logger logger = LoggerFactory
            .getLogger(TwitterSingleton.class);
    private static final ConcurrentHashMap<IToken, Twitter> twitterCache = new ConcurrentHashMap<IToken, Twitter>();

    private TwitterSingleton() {
    };

    public static TwitterSingleton getInstance() {
        return TwitterSingletonHolder.instance;
    }

    private static class TwitterSingletonHolder {
        public static TwitterSingleton instance = new TwitterSingleton();
    }

    public Twitter getSearchInstance(IToken token) throws TwitterException {
        if (twitterCache.containsKey(token)) {
            return twitterCache.get(token);
        } else {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            // Change to application-only token, 450 calls / 15 minutes
            cb.setApplicationOnlyAuthEnabled(true);
            String consumerKey = ((TwitterAccessToken) token).getConsumerKey();
            String consumerSecret = ((TwitterAccessToken) token)
                    .getConsumerSecret();
            String server = ConfigInstance.PROXY_ADDR;
            String port = ConfigInstance.PROXY_PORT + "";
            cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
                    .setOAuthConsumerSecret(consumerSecret)
                    .setJSONStoreEnabled(true);
            if(ConfigInstance.USE_PROXY) {
                cb.setHttpProxyHost(server)
                .setHttpProxyPort(Integer.parseInt(port));
            }
            Twitter twitter = new TwitterFactory(cb.build()).getInstance();
            OAuth2Token tokenOauth = twitter.getOAuth2Token();
            twitterCache.putIfAbsent(token, twitter);
            logger.info(
                    "Create twitter search instance {}, with OAuth2Token - type {}; token {}.",
                    twitter.toString(), tokenOauth.getTokenType(),
                    tokenOauth.getAccessToken());
            return twitter;
        }
    }
}
