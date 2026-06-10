package com.empresa.broker.consumer;

import com.empresa.broker.dto.ClienteCreateEvent;
import com.empresa.broker.exception.KafkaConsumeException;
import com.empresa.broker.service.ClienteProcessamentoService;
import com.empresa.broker.util.CorrelationIdUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClienteCreateConsumerTest {

    @Mock
    private ClienteProcessamentoService clienteProcessamentoService;

    @InjectMocks
    private ClienteCreateConsumer clienteCreateConsumer;

    private final ClienteCreateEvent event = new ClienteCreateEvent(
            "evt-1", "corr-1", "Cliente", "52998224725", "Rua 1", "12345-000",
            "Sao Paulo", "SP", "a@test.com", "11999999999");

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
    }

    @Test
    void consumir_quandoSucesso_deveProcessarComCorrelationIdDoHeader() {
        clienteCreateConsumer.consumir(event, "corr-header");

        verify(clienteProcessamentoService).processar(event);
        assertThat(CorrelationIdUtil.get()).isNull();
    }

    @Test
    void consumir_quandoHeaderAusente_deveUsarCorrelationIdDoEvento() {
        clienteCreateConsumer.consumir(event, null);

        verify(clienteProcessamentoService).processar(event);
        assertThat(CorrelationIdUtil.get()).isNull();
    }

    @Test
    void consumir_quandoHeaderBlank_deveUsarCorrelationIdDoEvento() {
        clienteCreateConsumer.consumir(event, "  ");

        verify(clienteProcessamentoService).processar(event);
        assertThat(CorrelationIdUtil.get()).isNull();
    }

    @Test
    void consumir_quandoErro_deveLancarKafkaConsumeException() {
        doThrow(new RuntimeException("erro")).when(clienteProcessamentoService).processar(event);

        assertThatThrownBy(() -> clienteCreateConsumer.consumir(event, "corr-1"))
                .isInstanceOf(KafkaConsumeException.class);
        assertThat(CorrelationIdUtil.get()).isNull();
    }
}
