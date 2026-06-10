package com.empresa.clientes.util;

import com.empresa.clientes.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfUtilTest {

    private static final String CPF_VALIDO = "52998224725";
    private static final String CPF_VALIDO_FORMATADO = "529.982.247-25";

    @Test
    void normalizar_deveRemoverCaracteresNaoNumericos() {
        assertThat(CpfUtil.normalizar(CPF_VALIDO_FORMATADO)).isEqualTo(CPF_VALIDO);
    }

    @Test
    void isValido_quandoCpfValido_deveRetornarTrue() {
        assertThat(CpfUtil.isValido(CPF_VALIDO)).isTrue();
        assertThat(CpfUtil.isValido(CPF_VALIDO_FORMATADO)).isTrue();
    }

    @Test
    void isValido_quandoCpfInvalido_deveRetornarFalse() {
        assertThat(CpfUtil.isValido("11111111111")).isFalse();
        assertThat(CpfUtil.isValido("123")).isFalse();
        assertThat(CpfUtil.isValido(null)).isFalse();
    }

    @Test
    void validarENormalizar_quandoValido_deveRetornarNormalizado() {
        assertThat(CpfUtil.validarENormalizar(CPF_VALIDO_FORMATADO)).isEqualTo(CPF_VALIDO);
    }

    @Test
    void validarENormalizar_quandoInvalido_deveLancarValidationException() {
        assertThatThrownBy(() -> CpfUtil.validarENormalizar("11111111111"))
                .isInstanceOf(ValidationException.class);
    }
}
