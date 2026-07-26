package com.smartqueue.auth_service.util;

import com.smartqueue.auth_service.constant.SecurityConstants;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.UUID;

public final class CorrelationIdUtil {

    private CorrelationIdUtil() {
        // Utility class constructor
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
        String correlationId = MDC.get(SecurityConstants.CORRELATION_ID_LOG_VAR);
        return StringUtils.hasText(correlationId) ? correlationId : generateCorrelationId();
    }
}
