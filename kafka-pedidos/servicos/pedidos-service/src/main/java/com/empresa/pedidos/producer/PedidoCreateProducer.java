package com.empresa.pedidos.producer;

import com.empresa.pedidos.config.KafkaProperties;
import com.empresa.pedidos.dto.PedidoCreateEvent;
import com.empresa.pedidos.exception.KafkaPublishException;
import com.empresa.pedidos.exception.TimeoutException;
import com.empresa.pedidos.util.CorrelationIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoCreateProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    @Value("${app.correlation-id.header}")
    private String correlationHeader;

    @Retryable(
            retryFor = KafkaPublishException.class,
            maxAttemptsExpression = "${app.kafka.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${app.kafka.retry.initial-delay-ms:500}",
                    multiplierExpression = "${app.kafka.retry.multiplier:2.0}"
            )
    )
    public void publicar(PedidoCreateEvent event) {
        try {
            String topic = kafkaProperties.topics().pedidoCreate();
            ProducerRecord<String, Object> record = new ProducerRecord<>(topic, event.eventId(), event);

            String correlationId = CorrelationIdUtil.get();
            if (correlationId != null && !correlationId.isBlank()) {
                record.headers().add(new RecordHeader(
                        correlationHeader,
                        correlationId.getBytes(StandardCharsets.UTF_8)
                ));
            }

            SendResult<String, Object> result = kafkaTemplate.send(record)
                    .get(kafkaProperties.timeoutMs(), TimeUnit.MILLISECONDS);

            log.info("Evento pedido-create publicado: eventId={}, codigoPedido={}, topico={}, particao={}",
                    event.eventId(), event.codigoPedido(), topic, result.getRecordMetadata().partition());
        } catch (java.util.concurrent.TimeoutException ex) {
            throw new TimeoutException("Timeout ao publicar evento no Kafka: " + event.eventId(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new KafkaPublishException("Publicacao Kafka interrompida: " + event.eventId(), ex);
        } catch (ExecutionException ex) {
            throw new KafkaPublishException("Erro ao publicar evento no Kafka: " + event.eventId(), ex.getCause());
        } catch (Exception ex) {
            throw new KafkaPublishException("Erro ao publicar evento no Kafka: " + event.eventId(), ex);
        }
    }
}
