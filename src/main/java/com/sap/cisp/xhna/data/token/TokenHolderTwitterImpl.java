package com.sap.cisp.xhna.data.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

import twitter4j.HttpResponseCode;
import twitter4j.TwitterException;

public class TokenHolderTwitterImpl extends TokenHolderBaseImpl {
    private static Logger logger = LoggerFactory
            .getLogger(TokenHolderTwitterImpl.class);

    @Override
    public long getRetryAfter(String tokenTag, int statusCode, Exception e) {
        long delay = DEFAULT_DELAY;
        if (statusCode == TokenManagementUtils.StateEnum.Available.getInt()
                && e == null) {
            delay = 0;

        } else {
            if (e instanceof TwitterException) {
                delay = checkError(tokenTag, statusCode, (TwitterException) e);
            } 
        }
        return delay;
    }

    private long checkError(String tokenTag, int statusCode, TwitterException e) {
        long delay = DEFAULT_DELAY;
        IToken token = TokenManager.getInstance().getTokenmap().get(tokenTag);
        switch (statusCode) {
        case HttpResponseCode.TOO_MANY_REQUESTS:
            delay = ((TwitterException) e).getRateLimitStatus()
                    .getSecondsUntilReset();
            if(delay < 0) {
                logger.debug("getSecondsUntilReset() return negative value {}, get the abs value instead.", delay); 
                delay = Math.abs(delay);
            }
            logger.info("====> Get twitter retry after delay = {} seconds", delay);
            return delay * 1000;
        case HttpResponseCode.FORBIDDEN:
            // Invalid token for twitter case.
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
        case HttpResponseCode.NOT_FOUND:
            // Should not be token issue, no delay
            return 0;
        default:
            return DEFAULT_RATE_LIMIT_RESET_TIME;
        }
    }

    @Override
    protected void resetPendingQueue() {
        logger.debug("Twitter token has been exhausted. Trigger token manager to reset the pending queue.");
        TokenManager.getInstance().resetPendingQueue(TokenType.Twitter);
    }
}
