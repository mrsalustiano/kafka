package com.empresa.pedidos.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaPropertiesTest {

    @Test
    void deveExporPropriedades() {
        KafkaProperties properties = new KafkaProperties(
                5000,
                new KafkaProperties.Retry(3, 500L, 2.0),
                new KafkaProperties.Topics("pedido-create", "pedido-create-dlt", "pedido-response", "pedido-response-dlt")
        );

        assertThat(properties.timeoutMs()).isEqualTo(5000);
        assertThat(properties.topics().pedidoCreate()).isEqualTo("pedido-create");
        assertThat(properties.topics().pedidoResponseDlt()).isEqualTo("pedido-response-dlt");
    }
}
