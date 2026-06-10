package com.empresa.pedidos.service;

import com.empresa.pedidos.exception.DatabaseException;
import com.empresa.pedidos.repository.MensagemProcessadaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotenciaServiceTest {

    @Mock
    private MensagemProcessadaRepository mensagemProcessadaRepository;

    @InjectMocks
    private IdempotenciaService idempotenciaService;

    @Test
    void jaProcessado_quandoExiste_deveRetornarTrue() {
        when(mensagemProcessadaRepository.existsByEventId("evt-1")).thenReturn(true);
        assertThat(idempotenciaService.jaProcessado("evt-1")).isTrue();
    }

    @Test
    void jaProcessado_quandoNaoExiste_deveRetornarFalse() {
        when(mensagemProcessadaRepository.existsByEventId("evt-1")).thenReturn(false);
        assertThat(idempotenciaService.jaProcessado("evt-1")).isFalse();
    }

    @Test
    void jaProcessado_quandoErro_deveLancarDatabaseException() {
        when(mensagemProcessadaRepository.existsByEventId("evt-1")).thenThrow(new RuntimeException("erro"));
        assertThatThrownBy(() -> idempotenciaService.jaProcessado("evt-1"))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void registrar_deveInserirEvento() {
        idempotenciaService.registrar("evt-1");
        verify(mensagemProcessadaRepository).insert("evt-1");
    }

    @Test
    void registrar_quandoErro_deveLancarDatabaseException() {
        doThrow(new RuntimeException("erro")).when(mensagemProcessadaRepository).insert("evt-1");
        assertThatThrownBy(() -> idempotenciaService.registrar("evt-1"))
                .isInstanceOf(DatabaseException.class);
    }
}
