package com.empresa.pedidos.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfUtilTest {

    private static final String CPF_VALIDO = "52998224725";

    @Test
    void isValido_quandoCpfValido_deveRetornarTrue() {
        assertThat(CpfUtil.isValido(CPF_VALIDO)).isTrue();
    }

    @Test
    void isValido_quandoCpfInvalido_deveRetornarFalse() {
        assertThat(CpfUtil.isValido("11111111111")).isFalse();
    }
}
