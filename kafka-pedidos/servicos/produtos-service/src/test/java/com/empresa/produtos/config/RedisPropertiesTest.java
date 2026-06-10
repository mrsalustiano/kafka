package com.empresa.produtos.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedisPropertiesTest {

    @Test
    void gettersAndSetters() {
        RedisProperties properties = new RedisProperties();
        properties.setTtlMinutes(30);
        properties.setTimeoutMs(3000);

        RedisProperties.RetryProperties retry = new RedisProperties.RetryProperties();
        retry.setMaxAttempts(5);
        retry.setInitialDelayMs(100);
        retry.setMultiplier(3.0);
        properties.setRetry(retry);

        assertThat(properties.getTtlMinutes()).isEqualTo(30);
        assertThat(properties.getTimeoutMs()).isEqualTo(3000);
        assertThat(properties.getRetry().getMaxAttempts()).isEqualTo(5);
    }
}
