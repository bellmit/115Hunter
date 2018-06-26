package com.sap.cisp.xhna.data.token;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.HttpResponseCode;
import twitter4j.TwitterException;

import com.sap.cisp.xhna.data.TaskManager;
import com.sap.cisp.xhna.data.TaskManager.WorkMode;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.task.worker.JobSubmitClientHelperOfficialVersion;
import com.sap.cisp.xhna.data.task.worker.TaskManagementUtils;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class TokenManager {
    protected static ScheduledThreadPoolExecutor tokenSchedulerService = new ScheduledThreadPoolExecutor(
            10);

    private static Logger logger = LoggerFactory.getLogger(TokenManager.class);
    private final ConcurrentHashMap<TokenType, ITokenHolder> tokenHolderMap = new ConcurrentHashMap<TokenType, ITokenHolder>();
    private static WorkMode workMode = null;
    private final ConcurrentHashMap<String, IToken> tokenMap = new ConcurrentHashMap<String, IToken>();

    public static synchronized TokenManager getInstance() {
        return TokenManagerHolder.instance;
    }

    private static class TokenManagerHolder {
        public static TokenManager instance = new TokenManager();
    }

    private TokenManager() {
    }

    public ConcurrentHashMap<String, IToken> getTokenmap() {
        return tokenMap;
    }

    public void init() {
        workMode = ConfigInstance.getCurrentWorkMode();
        logger.info("TokenManager initialization start... {}",
                TokenType.Twitter);
        tokenHolderMap.clear();
        ITokenHolder twitterHolder = new TokenHolderTwitterImpl();
        ITokenHolder facebookHolder = new TokenHolderFacebookImpl();
        ITokenHolder gplusHolder = new TokenHolderGPlusImpl();
        ITokenHolder youtubeHolder = new TokenHolderYouTubeImpl();
        tokenHolderMap.put(TokenType.Twitter, twitterHolder);
        tokenHolderMap.put(TokenType.Facebook, facebookHolder);
        tokenHolderMap.put(TokenType.GooglePlus, gplusHolder);
        tokenHolderMap.put(TokenType.YouTube, youtubeHolder);

        // Create twitter tokens
        for (int i = 0; i < ConfigInstance.TWITTER_KEY_NUM; i++) {
            TwitterAccessToken token = new TwitterAccessToken(
                    ConfigInstance.TWITTER_CONSUMER_KEY_LIST.get(i),
                    ConfigInstance.TWITTER_CONSUMER_SECRET_LIST.get(i),
                    ConfigInstance.TWITTER_ACCESS_TOKEN_LIST.get(i),
                    ConfigInstance.TWITTER_ACCESS_TOKEN_SECRET_LIST.get(i));
            token.setTokenTag(TokenType.Twitter.getName() + i);
            tokenMap.putIfAbsent(token.getTokenTag(), token);
            tokenHolderMap.get(TokenType.Twitter).offerTokenToPending(
                    token.getTokenTag());
        }

        // Create facebook tokens
        for (int i = 0; i < ConfigInstance.FACEBOOK_KEY_NUM; i++) {
            OAuthToken token = new OAuthToken(TokenType.Facebook,
                    ConfigInstance.FACEBAOOK_ACCESS_TOKEN_LIST.get(i));
            token.setTokenTag(TokenType.Facebook.getName() + i);
            tokenMap.putIfAbsent(token.getTokenTag(), token);
            tokenHolderMap.get(TokenType.Facebook).offerTokenToPending(
                    token.getTokenTag());
        }

        // Create gplus tokens
        for (int i = 0; i < ConfigInstance.GPLUS_KEY_NUM; i++) {
            OAuthToken token = new OAuthToken(TokenType.GooglePlus,
                    ConfigInstance.GPLUS_ACCESS_TOKEN_LIST.get(i));
            token.setTokenTag(TokenType.GooglePlus.getName() + i);
            tokenMap.putIfAbsent(token.getTokenTag(), token);
            tokenHolderMap.get(TokenType.GooglePlus).offerTokenToPending(
                    token.getTokenTag());
        }

        // Create YouTube tokens
        for (int i = 0; i < ConfigInstance.YOUTUBE_KEY_NUM; i++) {
            OAuthToken token = new OAuthToken(TokenType.YouTube,
                    ConfigInstance.YOUTUBE_ACCESS_TOKEN_LIST.get(i));
            token.setTokenTag(TokenType.YouTube.getName() + i);
            tokenMap.putIfAbsent(token.getTokenTag(), token);
            tokenHolderMap.get(TokenType.YouTube).offerTokenToPending(
                    token.getTokenTag());
        }
        logger.info("Token Map... {}", tokenHolderMap);
        // Create worker thread take token from pending queue to available queue
        // Here need to consider auto-throttling

        /*
         * Twitter Rate Limit: 15 minutes Window - 450 call for Application-only
         * token, 180 call for user token period = (900 seconds / 450(limit)*
         * (n-1)) n>1 900 / 450*(3-1) = 1 s
         */
        long twitterTokenPeriod = 2000L;
        if (ConfigInstance.TWITTER_KEY_NUM > 1) {
            twitterTokenPeriod = Math
                    .round(950.0 * 1000 / (450 * (ConfigInstance.TWITTER_KEY_NUM - 1)));
        }
        logger.info("Twitter token period --------> {} ms", twitterTokenPeriod);
        if (workMode != TaskManager.WorkMode.WORKER_ONLY) {
            tokenSchedulerService.scheduleAtFixedRate(twitterHolder, 0L,
                    twitterTokenPeriod, TimeUnit.MILLISECONDS);
        }

        /*
         * Facebook Rate Limit: If you exceed, or plan to exceed, any of the
         * following thresholds please contact us as you may be subject to
         * additional terms: (>5M MAU) or (>100M API calls per day) or (>50M
         * impressions per day).(Mau means monthly users), so limit <1157 call/s
         * period = 1s / 1000 = 1 ms
         */
        logger.info("Facebook token period --------> {} ms", 1);
        if (workMode != TaskManager.WorkMode.WORKER_ONLY) {
            tokenSchedulerService.scheduleAtFixedRate(facebookHolder, 0L, 1L,
                    TimeUnit.MILLISECONDS);
        }
        /*
         * Google+ Rate Limit(Quota): 1. The rate limit is both per day (10,000)
         * and per second (5). 2. The error message is shared between the types.
         * 3. The time until the next window is not provided. Some reference,
         * make applications wait rand(1,5) seconds per call, and if they are
         * rate limited again, wait 3600seconds before retrying.
         * 
         * 6 call / per token / per minute
         * 
         * 60/6*(n-1) = 1 s (n=11)
         */
        long googlePlusTokenPeriod = 1000L;
        if (ConfigInstance.GPLUS_KEY_NUM > 1) {
            googlePlusTokenPeriod = Math
                    .round(60.0 * 1000 / (6 * (ConfigInstance.GPLUS_KEY_NUM - 1)));
        }
        logger.info("GooglePlus token period --------> {} ms",
                googlePlusTokenPeriod);
        if (workMode != TaskManager.WorkMode.WORKER_ONLY) {
            tokenSchedulerService.scheduleAtFixedRate(gplusHolder, 0L,
                    googlePlusTokenPeriod, TimeUnit.MILLISECONDS);
        }
        /*
         * YouTube Data API V3 Rate Limit(Quota): Quota Unit Calculation rules:
         * 1) A simple read operation that only retrieves the ID of each
         * returned resource has a cost of approximately 1unit. 2) An API
         * request that returns resource data must specify the resource parts
         * that the request retrieves. Each part then adds approximately 2 units
         * to the request's quota cost.So each part cost of 3 units. 3) Playlist
         * - 2 parts(snippet and status), 5 units for request all. Channel - 6
         * parts, 13 units for all. Video - 10 parts, 21 units for all.
         * 
         * Rate Limit: 1) daily quota of 5,000,000 units; 2) 3,000 call per
         * second/ per user
         * 
         * Your application could have any of the following approximate limits:
         * - 1,000,000 read operations that each retrieve two resource parts. -
         * 50,000 write operations and 450,000 additional read operations that
         * each retrieve two resource parts. - 2000 video uploads, 7000 write
         * operations, and 200,000 read operations that each retrieve three
         * resource parts. Important: Only retrieving the resource parts that
         * your application needs conserves your daily quota and make the entire
         * system more efficient.
         * 
         * Current usage: - Current Data Crawler usage : Per request cost =
         * Channel (7 units) + Playlist (x * 3 units) + video (y * 19 units), x=
         * playlist size, y=video list size e.g. Rough estimation: If average
         * Per request cost estimation is 400 units (2 playlist, 20 videos),
         * thus call per day = 5000000 * n/400= 12500 *n, call per minute ~= 8 *
         * n ( must < 3000 * 60) Token period (second) = 60/8*(n-1) n>1, n =
         * token number
         */
        long youTubeTokenPeriod = 1000L;
        if (ConfigInstance.YOUTUBE_KEY_NUM > 1) {
            youTubeTokenPeriod = Math
                    .round(60.0 * 1000 / (8 * (ConfigInstance.YOUTUBE_KEY_NUM - 1)));
        }
        logger.info("YouTube token period --------> {} ms", youTubeTokenPeriod);
        if (workMode != TaskManager.WorkMode.WORKER_ONLY) {
            tokenSchedulerService.scheduleAtFixedRate(youtubeHolder, 0L,
                    youTubeTokenPeriod, TimeUnit.MILLISECONDS);
        }
        logger.info("TokenManager initialization done...");
    }

    /**
     * In distribution cases, token release message maybe lost, which cause the token pending queue exhausted.
     * This method aims to reset the pending queue.
     */
    public void resetPendingQueue(TokenType tokenType) {
        ITokenHolder holder = tokenHolderMap.get(tokenType);
        // put the available token to pending queue, for the token with pending, it maybe in the reset delay, do not reset it.
        for (Map.Entry<String, IToken> entry : tokenMap.entrySet()) {
            if (entry.getKey().contains(tokenType.getName()) && (entry.getValue().getState() != StateEnum.Pending)) {
                holder.offerTokenToPending(entry.getKey());
            }
        }
    }

    /**
     * Get access token. For the use case get token remotely, use a separate
     * gearman client instance from release token
     *
     * @param type
     * @return
     * @throws InterruptedException
     */
    public IToken getToken(TokenType type) throws InterruptedException {
        // get token locally, thread-safe
        if (workMode != TaskManager.WorkMode.WORKER_ONLY) {
            return tokenHolderMap.get(type).getToken();
        } else {
            // get token remotely, not thread-safe since there is remote
            // communication(get/release pair)
            byte[] data = SerializationUtils.serialize(type.getName());
            logger.debug(
                    "<-- Get token remotely request, type {}, data size {} bytes.",
                    type.getName(), data.length);
            byte[] jobReturn = null;
            try {
                jobReturn = JobSubmitClientHelperOfficialVersion
                        .getInstance()
                        .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.GET)
                        .submitJob(
                                TaskManagementUtils.FunctionEnum.GET_TOKEN_TAG
                                        .toString(),
                                data);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error("<-- Get token remotely error.", e);
            }
            if (jobReturn != null && jobReturn.length != 0) {
                String tokenTag = (String) SerializationUtils
                        .deserialize(jobReturn);
                if (tokenTag.equalsIgnoreCase("")) {
                    return null;
                }
                IToken token = tokenMap.get(tokenTag);
                logger.debug("<-- Get token remotely: tokenTag {}, token {}",
                        tokenTag, token);
                synchronized (token) {
                    if (token != null) {
                        token.setState(TokenManagementUtils.StateEnum.Unavailable);
                    }
                    logger.debug(
                            "<-- Set token state : tokenTag {},  State {}",
                            tokenTag, token.getState());
                    return token;
                }
            }
            return null;
        }
    }

    public String getTokenTag(TokenType type) throws InterruptedException {
        return tokenHolderMap.get(type).getTokenTag();
    }

    public void releaseToken(String tokenTag, int statusCode, long delay) {
        IToken token = TokenManager.getInstance().getTokenmap().get(tokenTag);
        // release token on server side locally
        if (workMode != TaskManager.WorkMode.WORKER_ONLY) {
            if (statusCode == 0) {
                token.setState(StateEnum.Available);
            } else {
                // With error
                token.setState(StateEnum.Pending);
            }
            TokenType type = token.getTokenType();
            logger.info("#####> Release Token on server with delay: Type {} ; schedule delay {}", type,
                    delay);
            scheduleToken(tokenTag, delay);
        }
    }

    /**
     * Note that, here there is thread-safe issue, since remote release/set
     * token state is a composed operation. An error case: release request to
     * server -> server released -> get token request -> set token to
     * unavailable -> release response arrived and set state to available again.
     *
     * @param tokenTag
     * @param statusCode
     * @param e
     */
    public void releaseToken(String tokenTag, int statusCode, Exception e) {
        IToken token = TokenManager.getInstance().getTokenmap().get(tokenTag);
        TokenType type = token.getTokenType();
        // release token on server side locally, thread-safe
        if (workMode != TaskManager.WorkMode.WORKER_ONLY) {
            if (statusCode == 0 || e == null) {
                token.setState(StateEnum.Available);
            } else {
                // With error
                token.setState(StateEnum.Pending);
            }

            long delay = tokenHolderMap.get(type).getRetryAfter(tokenTag,
                    statusCode, e);
            logger.info(
                    "--> Release Token local: Type {} ; schedule delay {} ms; Exception {}",
                    type, delay, e);
            scheduleToken(tokenTag, delay);
        } else {
            // release token remotely, not thread-safe since there is remote
            // communication(get/release pair)
            synchronized (token) {
                if (statusCode == 0 || e == null) {
                    token.setState(StateEnum.Available);
                } else {
                    // With error
                    token.setState(StateEnum.Pending);
                }
            }

            HashMap<String, Object> tokenMap = new HashMap<String, Object>();
            tokenMap.put("0", tokenTag);
            tokenMap.put("1", statusCode);
            if (e == null) {
                tokenMap.put("2", false);
            } else {
                // With error
                tokenMap.put("2", true);
                // only twitter error 429 use the exception to compute retry after
                if (e instanceof TwitterException
                        && statusCode == HttpResponseCode.TOO_MANY_REQUESTS) {
                    // java.io.NotSerializableException:
                    // twitter4j.HttpResponseImpl
                    // So just cache what we need from TwitterException
                    long delay = tokenHolderMap.get(type).getRetryAfter(tokenTag,
                            statusCode, e);
                    logger.error("****> Detected twitter rate limit exeception, set the reset delay {}", delay);
                    tokenMap.put("3", delay);
                }
            }

            byte[] data = SerializationUtils.serialize(tokenMap);
            logger.debug(
                    "--> Release token remotely request: tokenTag {}, token {}, statusCode {}, hasException {}, delay {}, exception {}, data size {} bytes.",
                    tokenMap.get("0"), token, tokenMap.get("1"), tokenMap.get("2"), tokenMap.get("3"), e, data.length);
            byte[] jobReturn = null;
            try {
                jobReturn = JobSubmitClientHelperOfficialVersion
                        .getInstance()
                        .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.RELEASE)
                        .submitJob(
                                TaskManagementUtils.FunctionEnum.RELEASE_TOKEN_TAG
                                        .toString(), data);
            } catch (Exception ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
                logger.error("--> Release token remotely error.", ex);
            }
            if (jobReturn != null) {
                try {
                    logger.debug("--> Release token remotely:  Result {}",
                            SerializationUtils.deserialize(jobReturn));
                } catch (SerializationException se) {
                    //catch runtime exception, just ignore the error since token has been released on server side.
                    logger.error("java.io.EOFException during deserialize release job return.", se);
                }
            } else {
                // remote release failed, restore state to unavailable for next
                // try.
                synchronized (token) {
                    token.setState(StateEnum.Unavailable);
                }
            }
        }
    }

    public void releaseToken(IToken token, int statusCode, Exception e) {
        releaseToken(token.getTokenTag(), statusCode, e);
    }

    public void releaseToken(IToken token, int statusCode) {
        releaseToken(token.getTokenTag(), statusCode);
    }

    public void releaseToken(String tokenTag, int statusCode,
                             boolean hasException) {
        if (hasException) {
            releaseToken(tokenTag, statusCode, new Exception(
                    "Dummy Exception for remote release."));
        } else {
            releaseToken(tokenTag, statusCode);
        }
    }

    public void releaseToken(String tokenTag, int statusCode) {
        releaseToken(tokenTag, statusCode, null);
    }

    public void scheduleToken(String tokenTag, long delay) {
        TokenType type = tokenMap.get(tokenTag).getTokenType();
        tokenHolderMap.get(type).scheduleToken(tokenTag, delay);
    }

    public void shutDown() {
        tokenSchedulerService.shutdown();
        // Wait for the running tasks
        try {
            tokenSchedulerService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Interrupt the threads and shutdown the scheduler
        tokenSchedulerService.shutdownNow();

        logger.info("Stop token Scheduler Service {} "
                + (tokenSchedulerService.isShutdown() ? "Done." : "Failed."));
    }

    public static void main(String... args) throws Exception {
        logger.info("Start token Scheduler Service");
        TokenManager.getInstance().getToken(TokenType.Twitter);
        IToken token = TokenManager.getInstance().getToken(TokenType.Twitter);
        ;
        TokenManager.getInstance().releaseToken(token.getTokenTag(), 429,
                new TwitterException("Test"));
        TokenManager.getInstance().getToken(TokenType.Twitter);
        TokenManager.getInstance().releaseToken(token.getTokenTag(), 429,
                new TwitterException("Test"));
        TokenManager.getInstance().getToken(TokenType.Twitter);
        TokenManager.getInstance().releaseToken(token.getTokenTag(), 429,
                new TwitterException("Test"));
        TokenManager.getInstance().getToken(TokenType.Twitter);
    }
}
