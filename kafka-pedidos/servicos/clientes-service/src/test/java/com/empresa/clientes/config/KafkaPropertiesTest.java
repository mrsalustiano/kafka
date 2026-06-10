package com.empresa.clientes.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaPropertiesTest {

    @Test
    void record_deveExporValores() {
        KafkaProperties.Retry retry = new KafkaProperties.Retry(3, 500L, 2.0);
        KafkaProperties.Topics topics = new KafkaProperties.Topics(
                "client-create", "client-create-dlt", "client-response", "client-response-dlt"
        );
        KafkaProperties properties = new KafkaProperties(5000, retry, topics);

        assertThat(properties.timeoutMs()).isEqualTo(5000);
        assertThat(properties.retry().maxAttempts()).isEqualTo(3);
        assertThat(properties.retry().initialDelayMs()).isEqualTo(500L);
        assertThat(properties.retry().multiplier()).isEqualTo(2.0);
        assertThat(properties.topics().clientCreate()).isEqualTo("client-create");
        assertThat(properties.topics().clientCreateDlt()).isEqualTo("client-create-dlt");
        assertThat(properties.topics().clientResponse()).isEqualTo("client-response");
        assertThat(properties.topics().clientResponseDlt()).isEqualTo("client-response-dlt");
    }
}
