package com.empresa.pedidos.producer;

import com.empresa.pedidos.config.KafkaProperties;
import com.empresa.pedidos.dto.PedidoCreateEvent;
import com.empresa.pedidos.exception.KafkaPublishException;
import com.empresa.pedidos.exception.TimeoutException;
import com.empresa.pedidos.util.CorrelationIdUtil;
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

import java.math.BigDecimal;
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
class PedidoCreateProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private KafkaProperties kafkaProperties;

    @InjectMocks
    private PedidoCreateProducer pedidoCreateProducer;

    private PedidoCreateEvent event;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pedidoCreateProducer, "correlationHeader", "X-Correlation-Id");
        when(kafkaProperties.topics()).thenReturn(
                new KafkaProperties.Topics("pedido-create", "pedido-create-dlt", "pedido-response", "pedido-response-dlt")
        );
        when(kafkaProperties.timeoutMs()).thenReturn(5000);
        event = new PedidoCreateEvent("evt-1", "corr-1", 1L, 1L, 2L, BigDecimal.TEN, 3, "EM_PROCESSAMENTO");
    }

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
    }

    @Test
    void publicar_quandoSucesso_deveEnviar() throws Exception {
        CorrelationIdUtil.set("corr-abc");
        CompletableFuture<SendResult<String, Object>> sendFuture = new CompletableFuture<>();
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(sendFuture);
        sendFuture.complete(criarSendResult());

        pedidoCreateProducer.publicar(event);
    }

    @Test
    void publicar_comCorrelationIdBlank_naoDeveAdicionarHeader() throws Exception {
        CorrelationIdUtil.set("  ");
        CompletableFuture<SendResult<String, Object>> sendFuture = new CompletableFuture<>();
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(sendFuture);
        sendFuture.complete(criarSendResult());

        pedidoCreateProducer.publicar(event);

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

        assertThatThrownBy(() -> pedidoCreateProducer.publicar(event))
                .isInstanceOf(TimeoutException.class);
    }

    @Test
    void publicar_quandoExecutionException_deveLancarKafkaPublishException() throws Exception {
        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class)))
                .thenThrow(new ExecutionException(new RuntimeException("kafka error")));

        assertThatThrownBy(() -> pedidoCreateProducer.publicar(event))
                .isInstanceOf(KafkaPublishException.class);
    }

    @Test
    void publicar_quandoInterrompido_deveLancarKafkaPublishException() throws Exception {
        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException("interrupted"));

        assertThatThrownBy(() -> pedidoCreateProducer.publicar(event))
                .isInstanceOf(KafkaPublishException.class);
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        Thread.interrupted();
    }

    @Test
    void publicar_quandoErroGenerico_deveLancarKafkaPublishException() throws Exception {
        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class))).thenThrow(new RuntimeException("generic"));

        assertThatThrownBy(() -> pedidoCreateProducer.publicar(event))
                .isInstanceOf(KafkaPublishException.class);
    }

    private SendResult<String, Object> criarSendResult() {
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("pedido-create", 0), 0, 0, 0, 0, 0);
        return new SendResult<>(new ProducerRecord<>("pedido-create", "evt-1", event), metadata);
    }
}
