package com.empresa.pedidos.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionClassesTest {

    @Test
    void deveInstanciarExcecoes() {
        assertThat(new BusinessException("msg").getMessage()).isEqualTo("msg");
        assertThat(new NotFoundException("nf").getMessage()).isEqualTo("nf");
        assertThat(new ValidationException("val").getMessage()).isEqualTo("val");
        assertThat(new DatabaseException("db", new RuntimeException()).getMessage()).isEqualTo("db");
        assertThat(new RedisException("redis", new RuntimeException()).getMessage()).isEqualTo("redis");
        assertThat(new KafkaConsumeException("consume", new RuntimeException()).getMessage()).isEqualTo("consume");
        assertThat(new KafkaPublishException("publish", new RuntimeException()).getMessage()).isEqualTo("publish");
        assertThat(new TimeoutException("timeout", new RuntimeException()).getMessage()).isEqualTo("timeout");
    }
}
