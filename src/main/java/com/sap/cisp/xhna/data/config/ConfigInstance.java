package com.sap.cisp.xhna.data.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskManager;
import com.sap.cisp.xhna.data.TaskManager.WorkMode;

public class ConfigInstance {

    private static Logger logger = LoggerFactory
            .getLogger(ConfigInstance.class);
    public static boolean USE_PROXY;
    public static String PROXY_ADDR;
    public static int PROXY_PORT;

    public static int TWITTER_KEY_NUM = 0;
    public static ArrayList<String> TWITTER_CONSUMER_KEY_LIST = new ArrayList<String>();
    public static ArrayList<String> TWITTER_CONSUMER_SECRET_LIST = new ArrayList<String>();
    public static ArrayList<String> TWITTER_ACCESS_TOKEN_LIST = new ArrayList<String>();
    public static ArrayList<String> TWITTER_ACCESS_TOKEN_SECRET_LIST = new ArrayList<String>();

    public static int FACEBOOK_KEY_NUM = 0;
    public static ArrayList<String> FACEBAOOK_ACCESS_TOKEN_LIST = new ArrayList<String>();

    public static int GPLUS_KEY_NUM = 0;
    public static ArrayList<String> GPLUS_ACCESS_TOKEN_LIST = new ArrayList<String>();

    public static int YOUTUBE_KEY_NUM = 0;
    public static ArrayList<String> YOUTUBE_ACCESS_TOKEN_LIST = new ArrayList<String>();

    public static String FACEBOOK_TOKEN;

    public static String YOUTUBE_TOKEN;
    public static String YOUTUBE_CHANNELPART;
    public static String YOUTUBE_PLAYLISTITEMPART;
    public static String YOUTUBE_VIDEOPART;
    public static long YOUTUBE_NUMPERPAGE;
    
    public static String DATASIFT_USERNAME = "";
    public static String DATASIFT_APIKEY = "";
    //Max concurrent connections per user: 200 
    public static final Semaphore DATASIFT_CONNECTION_SEMAPHORE = new Semaphore(200);
    //Max subscriptions per connection: 200 
    public static final int DATASIFTCLIENT_STREAM_PERMITS_NUMBER = 200;
    

    private static Properties properties = new Properties();
    private static long THREAD_POOL_SIZE_THRESHOLD = 200;
    public final static Object TWITTER_LOCK = new Object();
    public final static Object FACEBOOK_LOCK = new Object();
    public final static Object GPLUS_LOCK = new Object();
    public final static Object YOUTUBE_LOCK = new Object();
    public static int TRADITIONAL_SEMAPHORE_NUM;
    //traditional task barrier is error-prone to cause the executor service stuck if there is no enough threads available
    public static Semaphore TRADITIONAL_SEMAPHORE = null;   
    
    public static int GUBA_START_STOCK = 0;
    public static int GUBA_END_STOCK = 0;
    public static int GUBA_PAGES_ONE_STOCK = 0;
    public static boolean GUBA_REDIS_ON = false;
    public static String GUBA_REDIS_SERVER = "localhost";
    public static int GUBA_REDIS_PORT = 6379;
    static {
        loadConfig();
    }

    private static void loadConfig() {

        InputStream crawlInputStream = null;
        InputStream mainInputStream = null;
        try {
            crawlInputStream = new FileInputStream(
                    "configurations/crawl.properties");
            properties.load(crawlInputStream);

            USE_PROXY = Boolean.parseBoolean(properties
                    .getProperty("crawl.useProxy"));
            PROXY_ADDR = properties.getProperty("crawl.proxyAddr");
            PROXY_PORT = Integer.parseInt(properties
                    .getProperty("crawl.proxyPort"));
            mainInputStream = new FileInputStream(
                    "configurations/Main.properties");
            properties.load(mainInputStream);
            if(USE_PROXY)
            	setProxies();

            // load twitter tokens
            TWITTER_KEY_NUM = Integer.parseInt(properties
                    .getProperty("twitter.keyNum"));
            for (int i = 0; i < TWITTER_KEY_NUM; i++) {
                TWITTER_CONSUMER_KEY_LIST.add(properties
                        .getProperty("twitter.consumerKey" + i));
                TWITTER_CONSUMER_SECRET_LIST.add(properties
                        .getProperty("twitter.consumerSecret" + i));
                TWITTER_ACCESS_TOKEN_LIST.add(properties
                        .getProperty("twitter.accessToken" + i));
                TWITTER_ACCESS_TOKEN_SECRET_LIST.add(properties
                        .getProperty("twitter.accessTokenSecret" + i));
            }

            // load facebook tokens
            FACEBOOK_KEY_NUM = Integer.parseInt(properties
                    .getProperty("facebook.keyNum"));
            for (int i = 0; i < FACEBOOK_KEY_NUM; i++) {
                FACEBAOOK_ACCESS_TOKEN_LIST.add(properties
                        .getProperty("facebook.accessToken" + i));
            }

            // load gplus tokens
            GPLUS_KEY_NUM = Integer.parseInt(properties
                    .getProperty("gplus.keyNum"));
            for (int i = 0; i < GPLUS_KEY_NUM; i++) {
                GPLUS_ACCESS_TOKEN_LIST.add(properties
                        .getProperty("gplus.accessToken" + i));
            }

            // load youtube tokens
            YOUTUBE_KEY_NUM = Integer.parseInt(properties
                    .getProperty("youtube.keyNum"));
            for (int i = 0; i < YOUTUBE_KEY_NUM; i++) {
                YOUTUBE_ACCESS_TOKEN_LIST.add(properties
                        .getProperty("youtube.accessToken" + i));
            }

            FACEBOOK_TOKEN = properties.getProperty("facebook.appAccessToken");

            YOUTUBE_TOKEN = properties.getProperty("youtube.accessToken0");
            YOUTUBE_CHANNELPART = properties
                    .getProperty("youtube.channel_part");
            YOUTUBE_PLAYLISTITEMPART = properties
                    .getProperty("youtube.playlistitem_part");
            YOUTUBE_VIDEOPART = properties.getProperty("youtube.video_part");
            YOUTUBE_NUMPERPAGE = Long.parseLong(properties
                    .getProperty("youtube.numPerPage"));

            // load datasift configuration
            DATASIFT_USERNAME = properties.getProperty("datasift.userName");
            DATASIFT_APIKEY = properties.getProperty("datasift.apiKey");
            
            TRADITIONAL_SEMAPHORE_NUM = Integer.parseInt(properties
                    .getProperty("traditional.semaphoreNum"));
            TRADITIONAL_SEMAPHORE = new Semaphore(TRADITIONAL_SEMAPHORE_NUM);
            // To avoid  tasks flooding into a fixed-size thread pool, this threshold is sensitive and important!!!
            THREAD_POOL_SIZE_THRESHOLD = Long.parseLong(getValue("ThreadNum"))*2 / 3;
            
            /*2015-11-26*/
//            GUBA_START_STOCK = Integer.parseInt(properties.getProperty("guba.start_stock"));
//            GUBA_END_STOCK = Integer.parseInt(properties.getProperty("guba.end_stock"));
//            GUBA_PAGES_ONE_STOCK = Integer.parseInt(properties.getProperty("guba.pages_one_stock"));
//            GUBA_REDIS_ON = Boolean.parseBoolean(properties.getProperty("guba.redis_on"));
//            GUBA_REDIS_SERVER = properties.getProperty("guba.redis_server");
//            GUBA_REDIS_PORT = Integer.parseInt(properties.getProperty("guba.redis_port"));
        } catch (Exception e) {
            logger.error("Load configuration error!", e);
        } finally {
            if (crawlInputStream != null) {
                try {
                    crawlInputStream.close();
                } catch (IOException e) {
                    logger.error("Load configuration IO error!", e);
                }
            }
            if (mainInputStream != null) {
                try {
                    mainInputStream.close();
                } catch (IOException e) {
                    logger.error("Load configuration IO error!", e);
                }
            }
        }
    }

    private static void setProxies() {
        // Refer to:
        // https://docs.oracle.com/javase/7/docs/technotes/guides/net/proxies.html
        if (ConfigInstance.USE_PROXY) {
            logger.info("*** Need proxy setting!!!");
            System.setProperty("http.proxyHost", ConfigInstance.PROXY_ADDR);
            System.setProperty("http.proxyPort", ConfigInstance.PROXY_PORT + "");
            System.setProperty("https.proxyHost", ConfigInstance.PROXY_ADDR);
            System.setProperty("https.proxyPort", ConfigInstance.PROXY_PORT
                    + "");
            System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1|10.*");
        } else {
            logger.info("*** Do Not Need proxy setting!!!");
        }
    }

    public static String getValue(String name) {
        return (String) properties.get(name);
    }

    public static long getThreadNumber() {
        return Long.parseLong(getValue("ThreadNum"));
    }

    public static long getThreadPoolSizeThreshold() {
        return THREAD_POOL_SIZE_THRESHOLD;
    }

    public static WorkMode getCurrentWorkMode() {
        int mode = Integer.parseInt(getValue("WorkMode"));
        return TaskManager.getWorkModeByProperty(mode);
    }

    public static boolean getDebugFlagOn() {
        final boolean isDebugOn = (ConfigInstance.getValue("Finder")
                .equalsIgnoreCase("com.sap.cisp.xhna.data.finder.TestTaskFinder"));
        return isDebugOn;
    }


    public static boolean runInLocal() {
        /*
         * Here consider the SINGLETON mode also need to throttle the
         * tasks(token dependent) flooding, add synchronization to get/release
         * token pair operation as well.
         */
        final boolean isRunInLocal = false;// (ConfigInstance.getCurrentWorkMode() != TaskManager.WorkMode.WORKER_ONLY);
        return isRunInLocal;
    }

}
