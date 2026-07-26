package com.smartqueue.booking_service.util;

import java.util.UUID;

public final class CorrelationIdUtil {

    private CorrelationIdUtil() {
    }

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_LOG_VAR = "correlationId";

    public static String generateCorrelationId() {
        return "sqai-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
