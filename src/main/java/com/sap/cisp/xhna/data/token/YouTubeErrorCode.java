package com.sap.cisp.xhna.data.token;

/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
public interface YouTubeErrorCode {
    // Core API errors. Forbidden: Access forbidden. The request may not be
    // properly authorized.
    // Core API errors. quotaExceeded:The request cannot be completed because
    // you have exceeded your quota.
    int QuotaExceeded = 403;
    int BAD_REQUEST = 400;// Bad Request: The request was invalid. An
                          // accompanying error message will explain why. This
                          // is the status code will be returned during rate
                          // limiting.
    int UNAUTHORIZED = 401;// Not Authorized: Authentication credentials were
                           // missing or incorrect.
    int NOT_FOUND = 404;// Not Found: The URI requested is invalid or the
                        // resource requested, such as a user, does not exists.
}
