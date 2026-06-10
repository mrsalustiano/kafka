package com.empresa.pedidos.util;

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

    @Test
    void resolveOrGenerate_comHeader_deveRetornarTrimado() {
        assertThat(CorrelationIdUtil.resolveOrGenerate("  corr-1  ")).isEqualTo("corr-1");
    }

    @Test
    void resolveOrGenerate_semHeader_deveGerarUuid() {
        assertThat(CorrelationIdUtil.resolveOrGenerate("  ")).isNotBlank();
    }

    @Test
    void getOrGenerate_quandoDefinido_deveRetornarValor() {
        CorrelationIdUtil.set("corr-2");
        assertThat(CorrelationIdUtil.getOrGenerate()).isEqualTo("corr-2");
    }

    @Test
    void getOrGenerate_quandoAusente_deveGerarUuid() {
        assertThat(CorrelationIdUtil.getOrGenerate()).isNotBlank();
    }
}
