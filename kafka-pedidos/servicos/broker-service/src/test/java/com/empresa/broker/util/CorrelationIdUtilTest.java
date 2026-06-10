package com.empresa.broker.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdUtilTest {

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
    }

    @Test
    void deveGerenciarCorrelationIdNoThreadLocal() {
        assertThat(CorrelationIdUtil.get()).isNull();

        CorrelationIdUtil.set("corr-1");
        assertThat(CorrelationIdUtil.get()).isEqualTo("corr-1");

        CorrelationIdUtil.clear();
        assertThat(CorrelationIdUtil.get()).isNull();
    }
}
