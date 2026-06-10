package com.empresa.clientes.consumer;

import com.empresa.clientes.audit.AuditoriaService;
import com.empresa.clientes.audit.OperacaoAuditoria;
import com.empresa.clientes.dto.ClienteResponseEvent;
import com.empresa.clientes.exception.KafkaConsumeException;
import com.empresa.clientes.repository.ClienteStatusRepository;
import com.empresa.clientes.service.IdempotenciaService;
import com.empresa.clientes.util.CorrelationIdUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteResponseConsumerTest {

    @Mock
    private IdempotenciaService idempotenciaService;

    @Mock
    private ClienteStatusRepository clienteStatusRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ClienteResponseConsumer clienteResponseConsumer;

    private final ClienteResponseEvent event = new ClienteResponseEvent("evt-1", 1L, "CRIADO");

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
    }

    @Test
    void consumir_quandoJaProcessado_deveIgnorar() {
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(true);

        clienteResponseConsumer.consumir(event, "corr-1");

        verify(clienteStatusRepository, never()).upsert(eq(1L), eq("CRIADO"));
        verify(idempotenciaService, never()).registrar("evt-1");
        assertThat(CorrelationIdUtil.get()).isNull();
    }

    @Test
    void consumir_quandoDuplicado_deveIgnorarSemProcessar() {
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(true);

        clienteResponseConsumer.consumir(event, null);

        verify(clienteStatusRepository, never()).upsert(eq(1L), eq("CRIADO"));
        verify(auditoriaService, never()).registrar(
                eq(OperacaoAuditoria.PROCESSAMENTO_KAFKA), eq(1L), eq("Cliente criado com status CRIADO, eventId=evt-1"));
    }

    @Test
    void consumir_quandoSucesso_devePersistirStatusERegistrarAuditoria() {
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(false);

        clienteResponseConsumer.consumir(event, "corr-1");

        verify(clienteStatusRepository).upsert(1L, "CRIADO");
        verify(idempotenciaService).registrar("evt-1");
        verify(auditoriaService).registrar(
                OperacaoAuditoria.PROCESSAMENTO_KAFKA,
                1L,
                "Cliente criado com status CRIADO, eventId=evt-1"
        );
        assertThat(CorrelationIdUtil.get()).isNull();
    }

    @Test
    void consumir_quandoEventIdAusente_deveIgnorar() {
        ClienteResponseEvent eventoSemId = new ClienteResponseEvent(null, 1L, "CRIADO");

        clienteResponseConsumer.consumir(eventoSemId, "corr-1");

        verify(idempotenciaService, never()).jaProcessado("evt-1");
        verify(clienteStatusRepository, never()).upsert(eq(1L), eq("CRIADO"));
    }

    @Test
    void consumir_quandoEventIdBlank_deveIgnorar() {
        ClienteResponseEvent eventoBlank = new ClienteResponseEvent("  ", 1L, "CRIADO");

        clienteResponseConsumer.consumir(eventoBlank, "corr-1");

        verify(idempotenciaService, never()).jaProcessado("evt-1");
        verify(clienteStatusRepository, never()).upsert(eq(1L), eq("CRIADO"));
    }

    @Test
    void consumir_quandoErro_deveLancarKafkaConsumeException() {
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(false);
        doThrow(new RuntimeException("erro")).when(clienteStatusRepository).upsert(1L, "CRIADO");

        assertThatThrownBy(() -> clienteResponseConsumer.consumir(event, "corr-1"))
                .isInstanceOf(KafkaConsumeException.class);
        assertThat(CorrelationIdUtil.get()).isNull();
    }
}
