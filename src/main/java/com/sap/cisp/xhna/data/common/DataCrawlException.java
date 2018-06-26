package com.sap.cisp.xhna.data.common;

import java.io.Serializable;

import twitter4j.HttpResponseCode;

public class DataCrawlException extends Exception implements HttpResponseCode,
        Serializable {
    private static final long serialVersionUID = -2076120965942824123L;
    private int statusCode = -1;
    private int errorCode = -1;
    private String errorMessage = null;

    public DataCrawlException() {
        
    }

    public DataCrawlException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataCrawlException(String message) {
        this(message, (Throwable) null);
    }

    public DataCrawlException(Exception cause) {
        this(cause.getMessage(), cause);
        if (cause instanceof DataCrawlException) {
            ((DataCrawlException) cause).setNested();
        }
    }

    public DataCrawlException(String message, Exception cause, int statusCode) {
        this(message, cause);
        this.statusCode = statusCode;
    }

    @Override
    public String getMessage() {
        StringBuilder value = new StringBuilder();
        if (errorMessage != null && errorCode != -1) {
            value.append("message - ").append(errorMessage).append("\n");
            value.append("code - ").append(errorCode).append("\n");
        } else {
            value.append(super.getMessage());
        }
        if (statusCode != -1) {
            return getCause(statusCode) + "\n" + value.toString();
        } else {
            return value.toString();
        }
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    /**
     * Tests if the exception is caused by network issue
     *
     * @return if the exception is caused by network issue
     */
    public boolean isCausedByNetworkIssue() {
        return getCause() instanceof java.io.IOException;
    }

    public boolean resourceNotFound() {
        return statusCode == NOT_FOUND;
    }

    private boolean nested = false;

    void setNested() {
        nested = true;
    }

    /**
     * Returns error message from the API if available.
     *
     * @return error message from the API
     * @since Twitter4J 2.2.3
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Tests if error message from the API is available
     *
     * @return true if error message from the API is available
     * @since Twitter4J 2.2.3
     */
    public boolean isErrorMessageAvailable() {
        return errorMessage != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DataCrawlException that = (DataCrawlException) o;

        if (errorCode != that.errorCode)
            return false;
        if (nested != that.nested)
            return false;
        if (statusCode != that.statusCode)
            return false;
        if (errorMessage != null ? !errorMessage.equals(that.errorMessage)
                : that.errorMessage != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = statusCode;
        result = 31 * result + errorCode;
        result = 31 * result
                + (errorMessage != null ? errorMessage.hashCode() : 0);
        result = 31 * result + (nested ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return getMessage() + "\nDataCrawlException{ " + "statusCode="
                + statusCode + ", message=" + errorMessage + ", code="
                + errorCode + '}';
    }

    private static String getCause(int statusCode) {
        String cause;
        switch (statusCode) {
        case NOT_MODIFIED:
            cause = "There was no new data to return.";
            break;
        case BAD_REQUEST:
            cause = "The request was invalid. An accompanying error message will explain why.";
            break;
        case UNAUTHORIZED:
            cause = "Authentication credentials were missing or incorrect. Ensure that you have set valid consumer key/secret, access token/secret, and the system clock is in sync.";
            break;
        case FORBIDDEN:
            cause = "The request is understood, but it has been refused. Or Rate Limit/Quota exceeded.";
            break;
        case NOT_FOUND:
            cause = "The URI requested is invalid or the resource requested, such as a user, does not exists. Also returned when the requested format is not supported by the requested method.";
            break;
        case NOT_ACCEPTABLE:
            cause = "Returned when an invalid format is specified in the request.\n"
                    + "Or one or more of the parameters are not suitable for the resource.";
            break;
        case INTERNAL_SERVER_ERROR:
            cause = "Something is broken.";
            break;
        case BAD_GATEWAY:
            cause = "Http Service is down or being upgraded.";
            break;
        case SERVICE_UNAVAILABLE:
            cause = "The Http servers are up, but overloaded with requests. Try again later.";
            break;
        case GATEWAY_TIMEOUT:
            cause = "The Http servers are up, but the request couldn't be serviced due to some failure within our stack. Try again later.";
            break;
        default:
            cause = "";
        }
        return statusCode + ":" + cause;
    }
}
