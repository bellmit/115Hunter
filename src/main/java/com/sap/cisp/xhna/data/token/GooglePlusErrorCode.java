package com.sap.cisp.xhna.data.token;

public interface GooglePlusErrorCode {
    int BAD_REQUEST = 400;// Bad Request: The request was invalid. An
                          // accompanying error message will explain why. This
                          // is the status code will be returned during rate
                          // limiting. Grant error indicates that you either
                          // have exchanged an authorization code that has
                          // already been used, or you are trying to use an
                          // expired access token. Reset your credentials!
    int UNAUTHORIZED = 401;// Not Authorized: Authentication credentials were
                           // missing or incorrect. This is a separate type of
                           // error from the 400 and is more specific to your
                           // operations. For example, if you are using Google+
                           // application activities, and you haven’t properly
                           // configured your client to enable writing application
                           // activities to Google+ but you still have a valid
                           // client, you could see a 401 error.
    int FORBIDDEN = 403;// Forbidden: The request is understood, but it has been
                        // refused. An accompanying error message will explain
                        // why.
    int NOT_FOUND = 404;// Not Found: The URI requested is invalid or the
                        // resource requested, such as a user, does not exists.
    int INTERNAL_SERVER_ERROR = 500;// This is an error that you can’t do very
                                    // much about. If you’re encountering this
                                    // issue, please file an issue in the issue
                                    // tracker.
}
