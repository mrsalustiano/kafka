package com.empresa.pedidos.util;

import com.empresa.pedidos.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoStatusUtilTest {

    @Test
    void validar_quandoStatusValido_naoDeveLancar() {
        assertThatCode(() -> PedidoStatusUtil.validar("FINALIZADO")).doesNotThrowAnyException();
    }

    @Test
    void validar_quandoStatusInvalido_deveLancar() {
        assertThatThrownBy(() -> PedidoStatusUtil.validar("INVALIDO"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validar_quandoStatusBlank_deveLancar() {
        assertThatThrownBy(() -> PedidoStatusUtil.validar("  "))
                .isInstanceOf(ValidationException.class);
    }
}
