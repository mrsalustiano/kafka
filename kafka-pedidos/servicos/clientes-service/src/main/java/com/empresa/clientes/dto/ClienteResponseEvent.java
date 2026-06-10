package com.empresa.clientes.dto;

public record ClienteResponseEvent(
        String eventId,
        Long codigoCliente,
        String status
) {
}
