package com.empresa.clientes.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CpfValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private final CpfValidator validator = new CpfValidator();

    @Test
    void isValid_quandoCpfValido_deveRetornarTrue() {
        assertThat(validator.isValid("52998224725", context)).isTrue();
    }

    @Test
    void isValid_quandoCpfInvalido_deveRetornarFalse() {
        assertThat(validator.isValid("11111111111", context)).isFalse();
    }

    @Test
    void isValid_quandoBlank_deveRetornarTrue() {
        assertThat(validator.isValid("  ", context)).isTrue();
    }
}
