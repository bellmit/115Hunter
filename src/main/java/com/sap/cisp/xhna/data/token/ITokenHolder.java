package com.sap.cisp.xhna.data.token;

public interface ITokenHolder extends Runnable {
    public String getTokenTag() throws InterruptedException;

    public IToken getToken() throws InterruptedException;
    
    public long getRetryAfter(String tokenTag, int statusCode, Exception e);

    public void scheduleToken(String tokenTag, long delay);

    public boolean offerTokenToPending(String tokenTag);

    public boolean offerTokenToAvailable();

    public final long DEFAULT_DELAY = -1L;
    public final long GET_TOKEN_TIMEOUT = 180L;   // Change longer timeout to avoid frequent timeout due to lack of token
    public final int GET_TOKEN_RETRY_THRESHOLD = 3;
    public final long FORBIDDEN_SCHEDULE = -100L;
    public final long DEFAULT_RATE_LIMIT_RESET_TIME = 5 * 1000; // wait 5
                                                                // seconds to
                                                                // retry for non
                                                                // rate
                                                                // limitation/authentication
                                                                // errors
    public final long FORBIDDEN_RESET_TIME = 10 * 1000;// wait 10 seconds for
                                                       // authentication
                                                       // errors, no more than
                                                       // error threshold
                                                       // times.
}
