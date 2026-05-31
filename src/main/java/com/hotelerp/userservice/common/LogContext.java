package com.hotelerp.userservice.common;

import org.slf4j.MDC;
import java.util.UUID;

public class LogContext {

    private static final String LOG_ID_KEY = "logId";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String OPERATION_KEY = "operation";

    public static String getLogId() {
        String logId = MDC.get(LOG_ID_KEY);
        if (logId == null) {
            logId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put(LOG_ID_KEY, logId);
        }
        return logId;
    }

    public static String getRequestId() {
        String requestId = MDC.get(REQUEST_ID_KEY);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put(REQUEST_ID_KEY, requestId);
        }
        return requestId;
    }

    public static void setOperation(String operation) {
        MDC.put(OPERATION_KEY, operation);
    }

    public static void clear() {
        MDC.clear();
    }
}
