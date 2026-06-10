package com.empresa.broker.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaPropertiesTest {

    @Test
    void deveExporPropriedades() {
        KafkaProperties properties = new KafkaProperties(
                5000,
                new KafkaProperties.Retry(3, 500L, 2.0),
                new KafkaProperties.Topics("client-create", "client-create-dlt", "client-response", "client-response-dlt")
        );

        assertThat(properties.timeoutMs()).isEqualTo(5000);
        assertThat(properties.retry().maxAttempts()).isEqualTo(3);
        assertThat(properties.topics().clientCreate()).isEqualTo("client-create");
        assertThat(properties.topics().clientCreateDlt()).isEqualTo("client-create-dlt");
        assertThat(properties.topics().clientResponse()).isEqualTo("client-response");
        assertThat(properties.topics().clientResponseDlt()).isEqualTo("client-response-dlt");
    }
}
