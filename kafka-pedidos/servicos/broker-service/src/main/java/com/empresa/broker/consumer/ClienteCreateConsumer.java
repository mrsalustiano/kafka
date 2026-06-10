package com.empresa.broker.consumer;

import com.empresa.broker.dto.ClienteCreateEvent;
import com.empresa.broker.exception.KafkaConsumeException;
import com.empresa.broker.service.ClienteProcessamentoService;
import com.empresa.broker.util.CorrelationIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteCreateConsumer {

    private final ClienteProcessamentoService clienteProcessamentoService;

    @KafkaListener(
            topics = "${app.kafka.topics.client-create}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumir(
            @Payload ClienteCreateEvent event,
            @Header(name = "${app.correlation-id.header}", required = false) String correlationId) {
        try {
            definirCorrelationId(correlationId, event);
            clienteProcessamentoService.processar(event);
        } catch (Exception ex) {
            throw new KafkaConsumeException(
                    "Erro ao processar evento client-create: " + event.eventId(), ex
            );
        } finally {
            CorrelationIdUtil.clear();
            MDC.remove("correlationId");
        }
    }

    private void definirCorrelationId(String correlationId, ClienteCreateEvent event) {
        if (correlationId != null && !correlationId.isBlank()) {
            CorrelationIdUtil.set(correlationId);
            MDC.put("correlationId", correlationId);
            return;
        }
        if (event.correlationId() != null && !event.correlationId().isBlank()) {
            CorrelationIdUtil.set(event.correlationId());
            MDC.put("correlationId", event.correlationId());
        }
    }
}
