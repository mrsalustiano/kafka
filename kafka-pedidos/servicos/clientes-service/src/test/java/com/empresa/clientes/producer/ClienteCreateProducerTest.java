package com.empresa.clientes.producer;

import com.empresa.clientes.config.KafkaProperties;
import com.empresa.clientes.dto.ClienteCreateEvent;
import com.empresa.clientes.exception.KafkaPublishException;
import com.empresa.clientes.exception.TimeoutException;
import com.empresa.clientes.util.CorrelationIdUtil;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteCreateProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaProperties kafkaProperties;

    @InjectMocks
    private ClienteCreateProducer clienteCreateProducer;

    private ClienteCreateEvent event;
    private CompletableFuture<SendResult<String, Object>> sendFuture;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(clienteCreateProducer, "correlationHeader", "X-Correlation-Id");
        when(kafkaProperties.topics()).thenReturn(
                new KafkaProperties.Topics("client-create", "client-create-dlt", "client-response", "client-response-dlt")
        );
        when(kafkaProperties.timeoutMs()).thenReturn(5000);

        event = new ClienteCreateEvent("evt-1", "corr-1", "Cliente", "Rua 1", "12345-000",
                "Sao Paulo", "SP", "a@test.com", "11999999999");
    }

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
    }

    @Test
    void publicar_quandoSucesso_deveEnviarComCorrelationHeader() throws Exception {
        CorrelationIdUtil.set("corr-abc");
        sendFuture = new CompletableFuture<>();
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(sendFuture);
        SendResult<String, Object> sendResult = criarSendResult();
        sendFuture.complete(sendResult);

        clienteCreateProducer.publicar(event);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        org.mockito.Mockito.verify(kafkaTemplate).send(captor.capture());
        assertThat(captor.getValue().topic()).isEqualTo("client-create");
        assertThat(captor.getValue().headers().lastHeader("X-Correlation-Id")).isNotNull();
    }

    @Test
    void publicar_comCorrelationIdBlank_naoDeveAdicionarHeader() throws Exception {
        CorrelationIdUtil.set("  ");
        sendFuture = new CompletableFuture<>();
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(sendFuture);
        sendFuture.complete(criarSendResult());

        clienteCreateProducer.publicar(event);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        org.mockito.Mockito.verify(kafkaTemplate).send(captor.capture());
        assertThat(captor.getValue().headers().lastHeader("X-Correlation-Id")).isNull();
    }

    @Test
    void publicar_semCorrelationId_naoDeveAdicionarHeader() throws Exception {
        sendFuture = new CompletableFuture<>();
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(sendFuture);
        SendResult<String, Object> sendResult = criarSendResult();
        sendFuture.complete(sendResult);

        clienteCreateProducer.publicar(event);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        org.mockito.Mockito.verify(kafkaTemplate).send(captor.capture());
        assertThat(captor.getValue().headers().lastHeader("X-Correlation-Id")).isNull();
    }

    @Test
    void publicar_quandoTimeout_deveLancarTimeoutException() throws Exception {
        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class)))
                .thenThrow(new java.util.concurrent.TimeoutException("timeout"));

        assertThatThrownBy(() -> clienteCreateProducer.publicar(event))
                .isInstanceOf(TimeoutException.class);
    }

    @Test
    void publicar_quandoExecutionException_deveLancarKafkaPublishException() throws Exception {
        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class)))
                .thenThrow(new ExecutionException(new RuntimeException("kafka error")));

        assertThatThrownBy(() -> clienteCreateProducer.publicar(event))
                .isInstanceOf(KafkaPublishException.class)
                .hasMessageContaining("evt-1");
    }

    @Test
    void publicar_quandoInterrompido_deveLancarKafkaPublishExceptionERestaurarFlag() throws Exception {
        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException("interrupted"));

        assertThatThrownBy(() -> clienteCreateProducer.publicar(event))
                .isInstanceOf(KafkaPublishException.class);

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        Thread.interrupted();
    }

    @Test
    void publicar_quandoErroGenerico_deveLancarKafkaPublishException() throws Exception {
        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class))).thenThrow(new RuntimeException("generic"));

        assertThatThrownBy(() -> clienteCreateProducer.publicar(event))
                .isInstanceOf(KafkaPublishException.class);
    }

    private SendResult<String, Object> criarSendResult() {
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("client-create", 0), 0, 0, 0, 0, 0);
        return new SendResult<>(new ProducerRecord<>("client-create", "evt-1", event), metadata);
    }
}
