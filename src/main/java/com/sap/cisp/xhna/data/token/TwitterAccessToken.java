package com.sap.cisp.xhna.data.token;

import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public class TwitterAccessToken extends OAuthToken {
    private static final long serialVersionUID = 8609848569920504051L;
    private String consumerKey;
    private String consumerSecret;
    private String accessTokenSecret;

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    @Override
    public String getAccessToken() {
        return getConsumerKey();
    }

    public TwitterAccessToken(String consumerKey, String consumerSecret,
            String accessToken, String accessTokenSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.type = TokenType.Twitter;
        this.state = StateEnum.Pending;
    }

    @Override
    public String toString() {
        return "ConsumerKey: " + this.consumerKey + "\t"
                + "ConsumerKeySecret: " + this.consumerSecret + "\t" + "Type: "
                + this.type + "\t" + "State: " + this.state;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + this.type.hashCode();
        result = 37 * result + this.consumerKey.hashCode();
        result = 37 * result + this.consumerSecret.hashCode();
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

        TwitterAccessToken p = (TwitterAccessToken) obj;

        if (this.type.getName().equalsIgnoreCase(p.getTokenType().getName())
                && (this.consumerKey.equalsIgnoreCase(p.getConsumerKey()) && this.consumerSecret
                        .equalsIgnoreCase(p.getConsumerSecret()))) {
            return true;
        } else {
            return false;
        }
    }

}
