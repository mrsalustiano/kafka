package com.empresa.clientes.util;

import java.util.UUID;

public final class CorrelationIdUtil {

    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

    private CorrelationIdUtil() {
    }

    public static String resolveOrGenerate(String headerValue) {
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue.trim();
        }
        return UUID.randomUUID().toString();
    }

    public static void set(String correlationId) {
        CORRELATION_ID.set(correlationId);
    }

    public static String get() {
        return CORRELATION_ID.get();
    }

    public static String getOrGenerate() {
        String correlationId = get();
        return correlationId != null ? correlationId : resolveOrGenerate(null);
    }

    public static void clear() {
        CORRELATION_ID.remove();
    }
}
