package com.empresa.broker.service;

import com.empresa.broker.audit.AuditoriaService;
import com.empresa.broker.audit.OperacaoAuditoria;
import com.empresa.broker.dto.ClienteCreateEvent;
import com.empresa.broker.dto.ClienteResponseEvent;
import com.empresa.broker.exception.DatabaseException;
import com.empresa.broker.exception.KafkaPublishException;
import com.empresa.broker.producer.ClienteResponseProducer;
import com.empresa.broker.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteProcessamentoServiceTest {

    @Mock
    private IdempotenciaService idempotenciaService;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteResponseProducer clienteResponseProducer;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ClienteProcessamentoService clienteProcessamentoService;

    private final ClienteCreateEvent event = new ClienteCreateEvent(
            "evt-1", "corr-1", "Cliente", "Rua 1", "12345-000",
            "Sao Paulo", "SP", "a@test.com", "11999999999");

    @Test
    void processar_quandoJaProcessado_deveIgnorar() {
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(true);

        clienteProcessamentoService.processar(event);

        verify(clienteRepository, never()).insert(any());
        verify(clienteResponseProducer, never()).publicar(any());
        verify(idempotenciaService, never()).registrar("evt-1");
    }

    @Test
    void processar_quandoEventIdAusente_deveIgnorar() {
        ClienteCreateEvent eventoSemId = new ClienteCreateEvent(
                null, "corr-1", "Cliente", "Rua 1", "12345-000",
                "Sao Paulo", "SP", "a@test.com", "11999999999");

        clienteProcessamentoService.processar(eventoSemId);

        verify(idempotenciaService, never()).jaProcessado("evt-1");
        verify(clienteRepository, never()).insert(any());
    }

    @Test
    void processar_quandoEventIdBlank_deveIgnorar() {
        ClienteCreateEvent eventoBlank = new ClienteCreateEvent(
                "  ", "corr-1", "Cliente", "Rua 1", "12345-000",
                "Sao Paulo", "SP", "a@test.com", "11999999999");

        clienteProcessamentoService.processar(eventoBlank);

        verify(idempotenciaService, never()).jaProcessado("evt-1");
        verify(clienteRepository, never()).insert(any());
    }

    @Test
    void processar_quandoSucesso_devePersistirPublicarERegistrarIdempotencia() {
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(false);
        when(clienteRepository.insert(any())).thenReturn(1L);

        clienteProcessamentoService.processar(event);

        ArgumentCaptor<ClienteResponseEvent> captor = ArgumentCaptor.forClass(ClienteResponseEvent.class);
        verify(clienteResponseProducer).publicar(captor.capture());
        assertThat(captor.getValue().eventId()).isEqualTo("evt-1");
        assertThat(captor.getValue().codigoCliente()).isEqualTo(1L);
        assertThat(captor.getValue().status()).isEqualTo("CRIADO");

        verify(idempotenciaService).registrar("evt-1");
        verify(auditoriaService).registrar(
                OperacaoAuditoria.PROCESSAMENTO_KAFKA,
                1L,
                "Cliente criado via broker, eventId=evt-1"
        );
    }

    @Test
    void processar_quandoErroInsert_deveLancarDatabaseException() {
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(false);
        when(clienteRepository.insert(any())).thenThrow(new RuntimeException("erro db"));

        assertThatThrownBy(() -> clienteProcessamentoService.processar(event))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void processar_quandoErroPublicacao_devePropagarKafkaPublishException() {
        when(idempotenciaService.jaProcessado("evt-1")).thenReturn(false);
        when(clienteRepository.insert(any())).thenReturn(1L);
        doThrow(new KafkaPublishException("erro", new RuntimeException()))
                .when(clienteResponseProducer).publicar(any());

        assertThatThrownBy(() -> clienteProcessamentoService.processar(event))
                .isInstanceOf(KafkaPublishException.class);

        verify(idempotenciaService, never()).registrar(eq("evt-1"));
    }
}
