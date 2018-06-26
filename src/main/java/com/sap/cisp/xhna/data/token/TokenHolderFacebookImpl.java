package com.sap.cisp.xhna.data.token;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.HttpResponseCode;

import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.SendHttp;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class TokenHolderFacebookImpl extends TokenHolderBaseImpl implements
        FacebookErrorCode {
    private final long RATE_LIMIT_RESET_TIME = 30 * 60 * 1000; // wait 30
                                                               // minutes to
                                                               // retry from
                                                               // Facebook
                                                               // official
                                                               // statement
    private final long NO_DELAY = 0L;
    private static Logger logger = LoggerFactory
            .getLogger(TokenHolderFacebookImpl.class);
    private static final String TOKEN_VALIDATION_URL = "https://graph.facebook.com/debug_token?input_token=%s&access_token=%s";

    @Override
    public long getRetryAfter(String tokenTag, int statusCode, Exception e) {
        long delay = DEFAULT_DELAY;
        if (statusCode == TokenManagementUtils.StateEnum.Available.getInt()
                && e == null) {
            delay = NO_DELAY;
        } else {
            delay = checkError(tokenTag, statusCode);
        }
        return delay;
    }

    /**
     * Return the rate limit reset time in seconds Default is 30 minutes for
     * Facebook
     * 
     * @param statusCode
     * @return
     * @throws
     */
    private long checkError(String tokenTag, int statusCode) {
        IToken token = TokenManager.getInstance().getTokenmap().get(tokenTag);
        switch (statusCode) {
        case FacebookErrorCode.API_Too_Many_Calls:
        case FacebookErrorCode.API_User_Too_Many_Calls:
        case FacebookErrorCode.Specific_API_Too_Many_Calls:
        case FacebookErrorCode.Application_limit_reached:
            return RATE_LIMIT_RESET_TIME;
        case HttpResponseCode.NOT_FOUND:
            return 0;
        case HttpResponseCode.BAD_REQUEST:
            // Invalid token in facebook case
            if (token.increaseErrorCounter() > OAuthToken.ERROR_THRESHOLD) {
                // set state to invalid to avoid endless errors
                // Consider the auto-throttling is based on the token number,
                // the broken token will still be put in the pending queue as a
                // placeholder.
                logger.error(
                        "==> Http request is broken more than {} times, valid its status...",
                        OAuthToken.ERROR_THRESHOLD);
                if (!validFacebookToken(token)) {
                    logger.error(
                            "Facebook token validation failed. Set state as Invalid. {}",
                            token);
                    token.setState(StateEnum.Invalid);
                } else {
                    logger.error("The Facebook token is valid, please check the bad request and try again.");
                    return NO_DELAY;
                }
            }
            return FORBIDDEN_RESET_TIME;
        default:
            return DEFAULT_RATE_LIMIT_RESET_TIME;
        }
    }

    public boolean validFacebookToken(IToken token) {
        String url = String.format(TOKEN_VALIDATION_URL,
                token.getAccessToken(), token.getAccessToken());
        JSONObject initResponse = null;
        try {
            initResponse = SendHttp.sendGet(new URL(url));
        } catch (MalformedURLException e) {
            logger.error(
                    "Cannot get response during facebook token validation.", e);
        } catch (Exception e) {
            logger.error(
                    "Cannot get response during facebook token validation.", e);
        }
        if (initResponse == null) {
            logger.error("Failed to get response from {}", url);
            return false;
        }
        logger.info("Facebook token validation Response : {}", initResponse);
        JSONObject validation = initResponse.getJSONObject("data");
        if (validation == null)
            return false;
        String validResult = validation.getString("is_valid");
        if (validResult == null)
            return false;
        return validResult.equalsIgnoreCase("true");
    }

    @Override
    protected void resetPendingQueue() {
        logger.debug("Facebook token has been exhausted. Trigger token manager to reset the pending queue.");
        TokenManager.getInstance().resetPendingQueue(TokenType.Facebook);
    }
}
