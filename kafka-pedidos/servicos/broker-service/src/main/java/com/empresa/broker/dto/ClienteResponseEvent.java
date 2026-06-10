package com.empresa.broker.dto;

public record ClienteResponseEvent(
        String eventId,
        Long codigoCliente,
        String status
) {
}
