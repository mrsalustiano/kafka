package com.empresa.pedidos.consumer;

import com.empresa.pedidos.audit.AuditoriaService;
import com.empresa.pedidos.audit.OperacaoAuditoria;
import com.empresa.pedidos.dto.PedidoResponseEvent;
import com.empresa.pedidos.exception.KafkaConsumeException;
import com.empresa.pedidos.repository.PedidoRepository;
import com.empresa.pedidos.service.IdempotenciaService;
import com.empresa.pedidos.util.CorrelationIdUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoResponseConsumerTest {

    @Mock
    private IdempotenciaService idempotenciaService;
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private PedidoResponseConsumer pedidoResponseConsumer;

    private final PedidoResponseEvent event = new PedidoResponseEvent("evt-1", 1L, "FINALIZADO");

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
    }

    @Test
    void consumir_quandoJaProcessado_deveIgnorar() {
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(true);

        pedidoResponseConsumer.consumir(event, "corr-1");

        verify(pedidoRepository, never()).atualizarStatus(1L, "FINALIZADO");
        assertThat(CorrelationIdUtil.get()).isNull();
    }

    @Test
    void consumir_quandoSucesso_deveAtualizarStatus() {
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(false);
        when(pedidoRepository.atualizarStatus(1L, "FINALIZADO")).thenReturn(1);

        pedidoResponseConsumer.consumir(event, "corr-1");

        verify(idempotenciaService).registrar("evt-1");
        verify(auditoriaService).registrar(
                OperacaoAuditoria.PROCESSAMENTO_KAFKA, 1L,
                "Status atualizado via pedido-response: FINALIZADO, eventId=evt-1");
    }

    @Test
    void consumir_quandoEventIdAusente_deveIgnorar() {
        PedidoResponseEvent eventoSemId = new PedidoResponseEvent(null, 1L, "FINALIZADO");

        pedidoResponseConsumer.consumir(eventoSemId, "corr-1");

        verify(idempotenciaService, never()).jaProcessado("evt-1");
    }

    @Test
    void consumir_quandoPedidoNaoEncontrado_deveIgnorar() {
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(false);
        when(pedidoRepository.atualizarStatus(1L, "FINALIZADO")).thenReturn(0);

        pedidoResponseConsumer.consumir(event, "corr-1");

        verify(idempotenciaService, never()).registrar("evt-1");
    }

    @Test
    void consumir_quandoStatusInvalido_deveLancarKafkaConsumeException() {
        PedidoResponseEvent eventoInvalido = new PedidoResponseEvent("evt-1", 1L, "INVALIDO");
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(false);

        assertThatThrownBy(() -> pedidoResponseConsumer.consumir(eventoInvalido, "corr-1"))
                .isInstanceOf(KafkaConsumeException.class);
    }

    @Test
    void consumir_quandoErro_deveLancarKafkaConsumeException() {
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(false);
        doThrow(new RuntimeException("erro")).when(pedidoRepository).atualizarStatus(1L, "FINALIZADO");

        assertThatThrownBy(() -> pedidoResponseConsumer.consumir(event, "corr-1"))
                .isInstanceOf(KafkaConsumeException.class);
    }
}
