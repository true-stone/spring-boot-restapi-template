package com.example.api.filter.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestContextConstants {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID_KEY = "requestId";

    public static final String START_TIME_ATTR = RequestContextConstants.class.getName() + ".START_TIME";
    public static final String REQUEST_ID_ATTR = RequestContextConstants.class.getName() + ".REQUEST_ID";
    public static final String LOGGED_ATTR = RequestContextConstants.class.getName() + ".LOGGED";
}
