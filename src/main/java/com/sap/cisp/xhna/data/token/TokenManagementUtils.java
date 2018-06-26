package com.sap.cisp.xhna.data.token;

public class TokenManagementUtils {
    public static enum StateEnum {
        Available(0), Unavailable(1), Pending(2), Invalid(3);

        private final int index;

        private StateEnum(int index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return Integer.toString(index);
        }

        public int getInt() {
            return index;
        }
    }

    public static enum TokenType {
        Twitter("Twitter"), Facebook("Facebook"), GooglePlus("GooglePlus"), YouTube(
                "YouTube"), Test("Test");

        private String name;

        public String getName() {
            return name;
        }

        private TokenType(String name) {
            this.name = name;
        }
    }

    public static TokenType getTokenTypeByName(String name) {
        switch (name) {
        case "Twitter":
            return TokenType.Twitter;
        case "Facebook":
            return TokenType.Facebook;
        case "GooglePlus":
            return TokenType.GooglePlus;
        case "YouTube":
            return TokenType.YouTube;
        default:
            return null;
        }
    }
}
