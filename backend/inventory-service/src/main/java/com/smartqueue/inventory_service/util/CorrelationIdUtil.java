package com.smartqueue.inventory_service.util;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.UUID;

public final class CorrelationIdUtil {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_LOG_VAR = "correlationId";

    private CorrelationIdUtil() {
    }

    public static String generateCorrelationId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getOrGenerateCorrelationId(String headerValue) {
        if (StringUtils.hasText(headerValue)) {
            return headerValue;
        }
        return generateCorrelationId();
    }

    public static String getCurrentCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_LOG_VAR);
        return StringUtils.hasText(correlationId) ? correlationId : generateCorrelationId();
    }
}
