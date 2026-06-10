package com.empresa.pedidos.consumer;

import com.empresa.pedidos.audit.AuditoriaService;
import com.empresa.pedidos.audit.OperacaoAuditoria;
import com.empresa.pedidos.dto.PedidoResponseEvent;
import com.empresa.pedidos.exception.KafkaConsumeException;
import com.empresa.pedidos.repository.PedidoRepository;
import com.empresa.pedidos.service.IdempotenciaService;
import com.empresa.pedidos.util.CorrelationIdUtil;
import com.empresa.pedidos.util.PedidoStatusUtil;
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
public class PedidoResponseConsumer {

    private final IdempotenciaService idempotenciaService;
    private final PedidoRepository pedidoRepository;
    private final AuditoriaService auditoriaService;

    @KafkaListener(
            topics = "${app.kafka.topics.pedido-response}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumir(
            @Payload PedidoResponseEvent event,
            @Header(name = "${app.correlation-id.header}", required = false) String correlationId) {
        try {
            if (correlationId != null && !correlationId.isBlank()) {
                CorrelationIdUtil.set(correlationId);
                MDC.put("correlationId", correlationId);
            }

            if (event.eventId() == null || event.eventId().isBlank()) {
                log.warn("Evento pedido-response ignorado: eventId ausente");
                return;
            }

            if (idempotenciaService.jaProcessado(event.eventId())) {
                log.warn("Evento duplicado ignorado: eventId={}", event.eventId());
                return;
            }

            PedidoStatusUtil.validar(event.status());

            int atualizados = pedidoRepository.atualizarStatus(event.codigoPedido(), event.status());
            if (atualizados == 0) {
                log.warn("Pedido nao encontrado para atualizacao de status: codigoPedido={}", event.codigoPedido());
                return;
            }

            log.info("Pedido atualizado com id {} para status {}", event.codigoPedido(), event.status());

            idempotenciaService.registrar(event.eventId());
            auditoriaService.registrar(
                    OperacaoAuditoria.PROCESSAMENTO_KAFKA,
                    event.codigoPedido(),
                    "Status atualizado via pedido-response: " + event.status() + ", eventId=" + event.eventId()
            );
        } catch (Exception ex) {
            throw new KafkaConsumeException(
                    "Erro ao processar evento pedido-response: " + event.eventId(), ex
            );
        } finally {
            CorrelationIdUtil.clear();
            MDC.remove("correlationId");
        }
    }
}
