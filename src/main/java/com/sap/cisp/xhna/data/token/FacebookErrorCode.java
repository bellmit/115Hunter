package com.sap.cisp.xhna.data.token;

public interface FacebookErrorCode {
    int API_Session = 102;// Login status or access token has expired, been
                          // revoked, or is otherwise invalid - Handle expired
                          // access tokens (unless subcode is present)
    int API_Unknown = 1;// Possibly a temporary issue due to downtime - retry
                        // the operation after waiting, if it occurs again,
                        // check you are requesting an existing API.
    int API_Service = 2;// Temporary issue due to downtime - retry the operation
                        // after waiting.
    int API_Too_Many_Calls = 4;// Temporary issue due to throttling - retry the
                               // operation after waiting and examine your API
                               // request volume.
    int API_User_Too_Many_Calls = 17;// Temporary issue due to throttling -
                                     // retry the operation after waiting and
                                     // examine your API request volume.
    int API_Permission_Denied = 10;// Permission is either not granted or has
                                   // been removed - Handle the missing
                                   // permissions
    // 200-299 API Permission (Multiple values depending on permission)
    int Application_limit_reached = 341;// Temporary issue due to downtime or
                                        // throttling - retry the operation
                                        // after waiting and examine your API
                                        // request volume.
    int Specific_API_Too_Many_Calls = 613; // This rate limiting is applied at
                                           // user/app pair level to selected
                                           // number of api calls.Calls to this
                                           // api have exceeded the rate limit
    int Expired = 463; // Login status or access token has expired, been
                       // revoked, or is otherwise invalid - Handle expired
                       // access tokens
    int Invalid_access_token = 467; // Access token has expired, been revoked,
                                    // or is otherwise invalid -Handle expired
                                    // access tokens
    int Access_Token_Expired = 190; //
}
