package com.sap.cisp.xhna.data.token;

import com.sap.cisp.xhna.data.token.TokenManagementUtils.StateEnum;
import com.sap.cisp.xhna.data.token.TokenManagementUtils.TokenType;

public interface IToken extends java.io.Serializable {
    public String getAccessToken();

    public StateEnum getState();

    public void setState(StateEnum state);

    public TokenType getTokenType();
    
    public String getTokenTag();

    public int increaseErrorCounter();

    public int decreaseErrorCounter();
}
