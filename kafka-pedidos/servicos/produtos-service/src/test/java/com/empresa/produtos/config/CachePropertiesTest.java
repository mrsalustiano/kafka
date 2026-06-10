package com.empresa.produtos.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CachePropertiesTest {

    @Test
    void gettersAndSetters() {
        CacheProperties properties = new CacheProperties();
        properties.setRefreshMinutes(15);
        assertThat(properties.getRefreshMinutes()).isEqualTo(15);
    }
}
