package com.sap.cisp.xhna.data.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class TokenHolderYouTubeImpl extends TokenHolderBaseImpl {
    private static Logger logger = LoggerFactory
            .getLogger(TokenHolderYouTubeImpl.class);

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
        case YouTubeErrorCode.QuotaExceeded:
            return getResetTimeUtilNextDay();
        case YouTubeErrorCode.NOT_FOUND:
            // In this case, the token should be ok. no delay to schedule
            return 0;
        case YouTubeErrorCode.BAD_REQUEST:
            // Invalid token in youtube case
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
        case YouTubeErrorCode.UNAUTHORIZED:
        default:
            return DEFAULT_RATE_LIMIT_RESET_TIME;
        }
    }

    @Override
    protected void resetPendingQueue() {
        logger.debug("YouTube token has been exhausted. Trigger token manager to reset the pending queue.");
        TokenManager.getInstance().resetPendingQueue(TokenType.YouTube);
    }

}
