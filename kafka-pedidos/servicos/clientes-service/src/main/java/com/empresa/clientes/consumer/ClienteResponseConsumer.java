package com.empresa.clientes.consumer;

import com.empresa.clientes.audit.AuditoriaService;
import com.empresa.clientes.audit.OperacaoAuditoria;
import com.empresa.clientes.dto.ClienteResponseEvent;
import com.empresa.clientes.exception.KafkaConsumeException;
import com.empresa.clientes.repository.ClienteStatusRepository;
import com.empresa.clientes.service.IdempotenciaService;
import com.empresa.clientes.util.CorrelationIdUtil;
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
public class ClienteResponseConsumer {

    private final IdempotenciaService idempotenciaService;
    private final ClienteStatusRepository clienteStatusRepository;
    private final AuditoriaService auditoriaService;

    @KafkaListener(
            topics = "${app.kafka.topics.client-response}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumir(
            @Payload ClienteResponseEvent event,
            @Header(name = "${app.correlation-id.header}", required = false) String correlationId) {
        try {
            if (correlationId != null && !correlationId.isBlank()) {
                CorrelationIdUtil.set(correlationId);
                MDC.put("correlationId", correlationId);
            }

            if (event.eventId() == null || event.eventId().isBlank()) {
                log.warn("Evento client-response ignorado: eventId ausente");
                return;
            }

            if (idempotenciaService.jaProcessado(event.eventId())) {
                log.warn("Evento duplicado ignorado: eventId={}", event.eventId());
                return;
            }

            log.info("Cliente criado com id {}", event.codigoCliente());

            clienteStatusRepository.upsert(event.codigoCliente(), event.status());
            idempotenciaService.registrar(event.eventId());

            auditoriaService.registrar(
                    OperacaoAuditoria.PROCESSAMENTO_KAFKA,
                    event.codigoCliente(),
                    "Cliente criado com status " + event.status() + ", eventId=" + event.eventId()
            );
        } catch (Exception ex) {
            throw new KafkaConsumeException(
                    "Erro ao processar evento client-response: " + event.eventId(), ex
            );
        } finally {
            CorrelationIdUtil.clear();
            MDC.remove("correlationId");
        }
    }
}
