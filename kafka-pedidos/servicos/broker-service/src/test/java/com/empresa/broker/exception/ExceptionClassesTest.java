package com.empresa.broker.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionClassesTest {

    @Test
    void deveInstanciarExcecoes() {
        assertThat(new BusinessException("msg").getMessage()).isEqualTo("msg");
        assertThat(new DatabaseException("db", new RuntimeException()).getMessage()).isEqualTo("db");
        assertThat(new KafkaConsumeException("consume", new RuntimeException()).getMessage()).isEqualTo("consume");
        assertThat(new KafkaPublishException("publish", new RuntimeException()).getMessage()).isEqualTo("publish");
        assertThat(new TimeoutException("timeout", new RuntimeException()).getMessage()).isEqualTo("timeout");
    }
}
