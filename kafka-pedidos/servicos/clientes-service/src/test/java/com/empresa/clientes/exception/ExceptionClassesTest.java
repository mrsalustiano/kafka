package com.empresa.clientes.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionClassesTest {

    @Test
    void exceptions_devemSerInstanciaveis() {
        assertThat(new BusinessException("msg").getMessage()).isEqualTo("msg");
        assertThat(new NotFoundException("msg").getMessage()).isEqualTo("msg");
        assertThat(new ValidationException("msg").getMessage()).isEqualTo("msg");
        assertThat(new DatabaseException("msg", new RuntimeException()).getMessage()).isEqualTo("msg");
        assertThat(new KafkaPublishException("msg", new RuntimeException()).getMessage()).isEqualTo("msg");
        assertThat(new KafkaConsumeException("msg", new RuntimeException()).getMessage()).isEqualTo("msg");
        assertThat(new TimeoutException("msg").getMessage()).isEqualTo("msg");
        assertThat(new TimeoutException("msg", new RuntimeException()).getMessage()).isEqualTo("msg");
    }
}
