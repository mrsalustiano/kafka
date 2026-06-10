package com.empresa.clientes.dto;

public record ClienteAcceptedResponse(
        String eventId,
        String correlationId,
        String status
) {
}
