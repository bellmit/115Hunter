package com.sap.cisp.xhna.data.token;

import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;

public abstract class TokenHolderBaseImpl implements ITokenHolder {
    private static Logger logger = LoggerFactory
            .getLogger(TokenHolderBaseImpl.class);
    protected BlockingQueue<String> availableQueue = new LinkedBlockingQueue<String>();
    protected BlockingQueue<String> pendingQueue = new LinkedBlockingQueue<String>();

    @Override
    public String getTokenTag() throws InterruptedException {
        String tokenTag = null;
        int retryNum = 0;
        IToken token = null;
        while (true) {
            tokenTag = availableQueue.poll(GET_TOKEN_TIMEOUT, TimeUnit.SECONDS);
            if(tokenTag == null) {
                // no more available token(reset delay for rate limit exceed, or remote release message lost)
                // Need to consider reset the token queue in some cases:
                // Failed to release token due to packet lost.
                // java.lang.NullPointerException
                // at
                // net.johnewart.gearman.common.packets.PacketFactory.packetFromBytes(PacketFactory.java:38)
                // ~[gearman-common-0.8.11-20150731.182421-1.jar:?]
                logger.error("**** Get token tag timeout with threshold {} seconds. ****", GET_TOKEN_TIMEOUT);
                resetPendingQueue();
                break;
            }
            token = TokenManager.getInstance().getTokenmap().get(tokenTag);
            logger.debug("Request to get Token...{}", token);
            // not an invalid token
            if (token != null && token.getState() != StateEnum.Invalid) {
                    logger.debug("The token is valid! State : {}",
                            token.getState());
                    token.setState(StateEnum.Unavailable);
                    break;
            } else {
                if (retryNum < GET_TOKEN_RETRY_THRESHOLD) {
                    retryNum++;
                    logger.error(
                            "The token is invalid! State : {}. Return to pending directly. Retry {} time...",
                            token.getState(), retryNum);
                    offerTokenToPending(tokenTag);
                    continue;
                }
                // Here may need token validation?
                logger.error(
                        "There is no valid token available after retry {} times...",
                        retryNum);
                break;
            }
        }
        return tokenTag;
    }
    
    @Override
    public IToken getToken() throws InterruptedException {
        String tokenTag = getTokenTag();
        if(tokenTag == null) {
            return null;
        }
        return TokenManager.getInstance().getTokenmap().get(tokenTag);
    }

    public boolean offerTokenToPending(String tokenTag) {
        logger.debug(
                "Begin to offer token to pending queue. type {} token {}",
                TokenManager.getInstance().getTokenmap().get(tokenTag).getTokenType(),
                TokenManager.getInstance().getTokenmap().get(tokenTag).getAccessToken());
        return pendingQueue.offer(tokenTag);
    }

    public boolean offerTokenToAvailable() {
        String tokenTag = pendingQueue.poll();
        if (tokenTag == null)
            return false;
        logger.debug(
                "Begin to offer token to available queue. type {} token {}",
                TokenManager.getInstance().getTokenmap().get(tokenTag).getTokenType(),
                TokenManager.getInstance().getTokenmap().get(tokenTag).getAccessToken());
        return availableQueue.offer(tokenTag);
    }

    public void run() {
        offerTokenToAvailable();
    }

    @Override
    public void scheduleToken(final String tokenTag, long delay) {
        if (delay == DEFAULT_DELAY) {
            delay = DEFAULT_RATE_LIMIT_RESET_TIME;
        } else {
            //Make sure the delay is > 0
            if(delay < 0) {
                delay = Math.abs(delay);
            }
        }
        logger.debug(
                "Return the {} token to pending queue with delay {} token {}",
                TokenManager.getInstance().getTokenmap().get(tokenTag).getTokenType()
                        .getName(), delay,
                TokenManager.getInstance().getTokenmap().get(tokenTag));
        TokenManager.tokenSchedulerService.schedule(new Runnable() {
            public void run() {
                boolean isOfferingSuccess = offerTokenToPending(tokenTag);
                logger.debug("--> Offer token {} to pending queue {}.", tokenTag, isOfferingSuccess?"Successfully":"Failed");
                if (TokenManager.getInstance().getTokenmap().get(tokenTag).getState() != StateEnum.Invalid)
                    TokenManager.getInstance().getTokenmap().get(tokenTag)
                            .setState(StateEnum.Available);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Rate limit is reset on next day 12:00 AM
     * 
     * @return
     */
    protected long getResetTimeUtilNextDay() {
        Calendar currentDate = Calendar.getInstance();
        long currentDateLong = currentDate.getTime().getTime();
        currentDate.add(Calendar.DATE, 1);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.MILLISECOND, 0);
        long nextResetWindow = currentDate.getTime().getTime();
        long resetAfterTime = nextResetWindow - currentDateLong;
        logger.info("==> Token get reset time util next day 12:00 AM = {}",
                resetAfterTime);
        return resetAfterTime;
    }
    
    protected void resetPendingQueue() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
