package com.empresa.pedidos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.redis")
@Getter
@Setter
public class RedisProperties {

    private int ttlMinutes = 60;
    private long timeoutMs = 2000;
    private RetryProperties retry = new RetryProperties();

    @Getter
    @Setter
    public static class RetryProperties {
        private int maxAttempts = 3;
        private long initialDelayMs = 500;
        private double multiplier = 2.0;
    }
}
