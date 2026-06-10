package com.empresa.clientes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties(
        int timeoutMs,
        Retry retry,
        Topics topics
) {

    public record Retry(int maxAttempts, long initialDelayMs, double multiplier) {
    }

    public record Topics(
            String clientCreate,
            String clientCreateDlt,
            String clientResponse,
            String clientResponseDlt
    ) {
    }
}
