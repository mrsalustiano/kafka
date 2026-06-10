package com.empresa.broker.util;

import com.empresa.broker.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfUtilTest {

    private static final String CPF_VALIDO = "52998224725";

    @Test
    void isValido_quandoCpfValido_deveRetornarTrue() {
        assertThat(CpfUtil.isValido(CPF_VALIDO)).isTrue();
    }

    @Test
    void validarENormalizar_quandoInvalido_deveLancarValidationException() {
        assertThatThrownBy(() -> CpfUtil.validarENormalizar("00000000000"))
                .isInstanceOf(ValidationException.class);
    }
}
