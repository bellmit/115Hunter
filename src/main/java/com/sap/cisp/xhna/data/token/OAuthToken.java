package com.sap.cisp.xhna.data.token;

import java.util.concurrent.atomic.AtomicInteger;

import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class OAuthToken implements IToken {
    private static final long serialVersionUID = 8181158829331980830L;

    protected String accessToken;

    protected volatile StateEnum state;

    protected TokenType type;

    protected  AtomicInteger errorCounter = new AtomicInteger(0);

    public static  final int ERROR_THRESHOLD = 3;
    
    protected String tokenTag;

    public TokenType getTokenType() {
        return type;
    }

    public String getTokenTag() {
        return tokenTag;
    }

    public void setTokenTag(String tokenTag) {
        this.tokenTag = tokenTag;
    }

    public OAuthToken() {
    }

    public OAuthToken(TokenType type, String accessToken) {
        this.type = type;
        this.accessToken = accessToken;
        this.state = StateEnum.Pending;
    }

    @Override
    public String getAccessToken() {
        return this.accessToken;
    }

    @Override
    public StateEnum getState() {
        return this.state;
    }

    @Override
    public void setState(StateEnum state) {
        this.state = state;
    }

    public int increaseErrorCounter() {
        return this.errorCounter.incrementAndGet();
    }

    public int decreaseErrorCounter() {
        return this.errorCounter.decrementAndGet();
    }

    public String toString() {
        return "Token Type : " + type + "; State : " + state
                + " ; accessToken :" + accessToken;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + this.type.hashCode();
        result = 37 * result + this.accessToken.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        // a quick test to see if the objects are identical
        if (this == obj)
            return true;

        // must return false if the explicit parameter is null
        if (obj == null)
            return false;

        if (getClass() != obj.getClass()) {
            return false;
        }

        OAuthToken p = (OAuthToken) obj;

        if (this.type.getName().equalsIgnoreCase(p.getTokenType().getName())
                && (this.accessToken.equalsIgnoreCase(p.getAccessToken()))) {
            return true;
        } else {
            return false;
        }
    }
}
