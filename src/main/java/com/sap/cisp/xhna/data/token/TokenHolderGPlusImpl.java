package com.sap.cisp.xhna.data.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class TokenHolderGPlusImpl extends TokenHolderBaseImpl implements
        GooglePlusErrorCode {
    private final long DEFAULT_RATE_LIMIT_RESET_TIME = 5 * 1000; // wait 5
                                                                 // seconds
                                                                 // to retry
                                                                 // for non
                                                                 // rate
                                                                 // limitation
                                                                 // errors
    private static Logger logger = LoggerFactory
            .getLogger(TokenHolderGPlusImpl.class);

    @Override
    public long getRetryAfter(String tokenTag, int statusCode, Exception e) {
        long delay = DEFAULT_DELAY;
        if (statusCode == TokenManagementUtils.StateEnum.Available.getInt()
                && e == null) {
            delay = 0;
        } else {
            delay = checkError(tokenTag, statusCode);
        }
        return delay;
    }

    /**
     * Return the rate limit reset time in seconds Default is next day 12:00 AM
     * 
     * @param statusCode
     * @return
     */
    private long checkError(String tokenTag, int statusCode) {
        IToken token = TokenManager.getInstance().getTokenmap().get(tokenTag);
        switch (statusCode) {
        case GooglePlusErrorCode.FORBIDDEN:
            return getResetTimeUtilNextDay();
        case GooglePlusErrorCode.NOT_FOUND:
            return 0;
        case GooglePlusErrorCode.BAD_REQUEST:
            // Invalid token in gplus case
            if (token.increaseErrorCounter() > OAuthToken.ERROR_THRESHOLD) {
                // set state to invalid to avoid endless errors
                // Consider the auto-throttling is based on the token number,
                // the broken token will still be put in the pending queue as a
                // placeholder.
                logger.error(
                        "==> Token is broken more than {} times, change state to Invalid.",
                        OAuthToken.ERROR_THRESHOLD);
                token.setState(StateEnum.Invalid);
            }
            return FORBIDDEN_RESET_TIME;
        case GooglePlusErrorCode.INTERNAL_SERVER_ERROR:
        case GooglePlusErrorCode.UNAUTHORIZED:
        default:
            return DEFAULT_RATE_LIMIT_RESET_TIME;
        }
    }

    @Override
    protected void resetPendingQueue() {
        logger.debug("Google Plus token has been exhausted. Trigger token manager to reset the pending queue.");
        TokenManager.getInstance().resetPendingQueue(TokenType.GooglePlus);
    }

    public static void main(String... args) {
        TokenHolderGPlusImpl test = new TokenHolderGPlusImpl();
        test.getResetTimeUtilNextDay();
    }
}
