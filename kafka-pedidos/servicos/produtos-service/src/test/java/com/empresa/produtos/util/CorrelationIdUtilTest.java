package com.empresa.produtos.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdUtilTest {

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
    }

    @Test
    void setGetClear() {
        CorrelationIdUtil.set("abc");
        assertThat(CorrelationIdUtil.get()).isEqualTo("abc");
        CorrelationIdUtil.clear();
        assertThat(CorrelationIdUtil.get()).isNull();
    }
}
